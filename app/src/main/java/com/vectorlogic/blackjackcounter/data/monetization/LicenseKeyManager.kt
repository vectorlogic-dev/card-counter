package com.vectorlogic.blackjackcounter.data.monetization

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages license key validation against LemonSqueezy's API and stores
 * activation state in encrypted SharedPreferences backed by Android Keystore.
 *
 * Setup instructions:
 * 1. Create products on LemonSqueezy (one per paid system + unlock-all).
 * 2. Enable "License keys" on each product in LemonSqueezy dashboard.
 * 3. Map each LemonSqueezy product variant to the corresponding system ID
 *    in [CountingSystemMonetization].
 *
 * API reference: https://docs.lemonsqueezy.com/api/license-keys
 */
class LicenseKeyManager(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _ownedSystemIds = MutableStateFlow<Set<String>>(emptySet())
    val ownedSystemIds: StateFlow<Set<String>> = _ownedSystemIds.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    fun start() {
        loadStoredActivations()
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    /**
     * Validate and activate a license key.
     * Calls LemonSqueezy's /v1/licenses/activate endpoint.
     */
    fun activateLicenseKey(licenseKey: String) {
        if (_isValidating.value) return
        val trimmedKey = licenseKey.trim()
        if (trimmedKey.isEmpty()) {
            _statusMessage.value = "Please enter a license key."
            return
        }

        _isValidating.value = true
        scope.launch {
            try {
                val result = callActivateApi(trimmedKey)
                if (result.success) {
                    val systemIds = resolveSystemIds(result.productId, result.variantId)
                    if (systemIds.isEmpty()) {
                        _statusMessage.value = "License key is valid but not linked to any system."
                    } else {
                        storeActivation(trimmedKey, systemIds)
                        loadStoredActivations()
                        _statusMessage.value = "License activated successfully!"
                    }
                } else {
                    _statusMessage.value = result.errorMessage ?: "Invalid license key."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Could not validate key. Check your connection."
            } finally {
                _isValidating.value = false
            }
        }
    }

    /**
     * Remove all stored activations (e.g., for troubleshooting).
     */
    fun clearActivations() {
        prefs.edit().clear().apply()
        _ownedSystemIds.value = emptySet()
    }

    // ---- LemonSqueezy API ---------------------------------------------------

    private fun callActivateApi(licenseKey: String): ActivationResult {
        val url = URL("$LEMONSQUEEZY_API_BASE/v1/licenses/activate")
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000

            val instanceName = getInstanceName()
            val body = JSONObject().apply {
                put("license_key", licenseKey)
                put("instance_name", instanceName)
            }
            connection.outputStream.bufferedWriter().use { it.write(body.toString()) }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            parseActivationResponse(responseCode, responseBody)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseActivationResponse(code: Int, body: String): ActivationResult {
        return try {
            val json = JSONObject(body)

            if (code == 200 && json.optBoolean("activated", false)) {
                val meta = json.optJSONObject("meta") ?: JSONObject()
                ActivationResult(
                    success = true,
                    productId = meta.optLong("product_id", -1),
                    variantId = meta.optLong("variant_id", -1)
                )
            } else {
                val error = json.optString("error", "Activation failed.")
                ActivationResult(success = false, errorMessage = error)
            }
        } catch (e: Exception) {
            ActivationResult(success = false, errorMessage = "Unexpected response from server.")
        }
    }

    /**
     * Map a LemonSqueezy product/variant to system IDs.
     *
     * TODO: Replace these placeholder IDs with your actual LemonSqueezy
     *       product and variant IDs once you create the products.
     */
    private fun resolveSystemIds(productId: Long, variantId: Long): Set<String> {
        // Check if this is the "unlock all" product first.
        if (productId == PRODUCT_ID_UNLOCK_ALL || variantId == VARIANT_ID_UNLOCK_ALL) {
            return CountingSystemMonetization.paidSystemIds
        }

        // Map individual product/variant IDs to system IDs.
        val systemId = VARIANT_TO_SYSTEM_ID[variantId]
            ?: PRODUCT_TO_SYSTEM_ID[productId]
        return if (systemId != null) setOf(systemId) else emptySet()
    }

    // ---- Encrypted local storage --------------------------------------------

    private fun storeActivation(licenseKey: String, systemIds: Set<String>) {
        val existing = getStoredSystemIds()
        val updated = existing + systemIds
        val encrypted = encrypt(updated.joinToString(","))
        prefs.edit()
            .putString(KEY_ACTIVATED_SYSTEMS, encrypted)
            .putString(KEY_LICENSE_HASH, hashKey(licenseKey))
            .apply()
    }

    private fun loadStoredActivations() {
        val stored = getStoredSystemIds()
        _ownedSystemIds.value = stored
    }

    private fun getStoredSystemIds(): Set<String> {
        val encrypted = prefs.getString(KEY_ACTIVATED_SYSTEMS, null) ?: return emptySet()
        return try {
            val decrypted = decrypt(encrypted)
            decrypted.split(",").filter { it.isNotBlank() }.toSet()
        } catch (e: Exception) {
            // Tampered or corrupted data — clear it.
            prefs.edit().remove(KEY_ACTIVATED_SYSTEMS).apply()
            emptySet()
        }
    }

    // ---- Android Keystore encryption ----------------------------------------

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val keyGen = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )
        keyGen.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGen.generateKey()
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        // Store IV + ciphertext together, base64 encoded.
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val cipherText = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    private fun hashKey(licenseKey: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(licenseKey.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun getInstanceName(): String {
        // Use a stable device identifier (Android ID).
        val androidId = android.provider.Settings.Secure.getString(
            appContext.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        return androidId ?: "unknown-device"
    }

    // ---- Constants ----------------------------------------------------------

    private data class ActivationResult(
        val success: Boolean,
        val productId: Long = -1,
        val variantId: Long = -1,
        val errorMessage: String? = null
    )

    companion object {
        private const val LEMONSQUEEZY_API_BASE = "https://api.lemonsqueezy.com"
        private const val PREFS_NAME = "license_activations"
        private const val KEY_ACTIVATED_SYSTEMS = "activated_systems"
        private const val KEY_LICENSE_HASH = "license_hash"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS = "cardcounter_license_key"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128

        // TODO: Replace these with your actual LemonSqueezy product/variant IDs.
        // You get these from the LemonSqueezy dashboard after creating products.
        private const val PRODUCT_ID_UNLOCK_ALL = -1L
        private const val VARIANT_ID_UNLOCK_ALL = -1L

        private val PRODUCT_TO_SYSTEM_ID: Map<Long, String> = mapOf(
            // e.g., 123456L to CountingSystemId.HI_OPT_II,
        )
        private val VARIANT_TO_SYSTEM_ID: Map<Long, String> = mapOf(
            // e.g., 789012L to CountingSystemId.HI_OPT_II,
        )
    }
}
