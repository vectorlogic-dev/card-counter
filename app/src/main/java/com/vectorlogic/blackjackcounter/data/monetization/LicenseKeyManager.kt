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
 * Manages payment verification against PayMongo's API and stores
 * activation state in encrypted SharedPreferences backed by Android Keystore.
 *
 * Flow:
 * 1. User taps "Buy" → opens PayMongo payment link in browser.
 * 2. User pays via GCash, Maya, or card.
 * 3. User enters the PayMongo reference number shown after payment.
 * 4. App verifies the payment via PayMongo API → unlocks systems.
 *
 * Setup:
 * 1. Create a PayMongo account at https://paymongo.com
 * 2. Create Payment Links in the PayMongo dashboard for each product.
 * 3. Set PAYMONGO_SECRET_KEY to your secret key (sk_live_... or sk_test_...).
 * 4. Set the payment link URLs below.
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
     * Returns the PayMongo payment link URL for the unlock-all product.
     */
    fun getPaymentLinkUrl(): String = PAYMENT_LINK_UNLOCK_ALL

    /**
     * Verify a PayMongo payment reference number and unlock systems.
     *
     * Calls PayMongo's API to check if the payment link reference
     * corresponds to a successful payment.
     */
    fun verifyPayment(referenceNumber: String) {
        if (_isValidating.value) return
        val trimmed = referenceNumber.trim()
        if (trimmed.isEmpty()) {
            _statusMessage.value = "Please enter your payment reference number."
            return
        }

        _isValidating.value = true
        scope.launch {
            try {
                val result = verifyPaymentViaApi(trimmed)
                if (result.success) {
                    storeActivation(trimmed, result.unlockedSystemIds)
                    loadStoredActivations()
                    _statusMessage.value = "Payment verified! Systems unlocked."
                } else {
                    _statusMessage.value = result.errorMessage
                        ?: "Could not verify payment. Check your reference number."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Could not verify payment. Check your connection."
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

    // ---- PayMongo API -------------------------------------------------------

    /**
     * Verify a payment by looking up the payment link reference.
     *
     * PayMongo flow:
     * 1. Retrieve the link by reference_number to get its payments.
     * 2. Check if any payment has status "paid".
     */
    private fun verifyPaymentViaApi(referenceNumber: String): VerificationResult {
        // First, search for the payment link by reference number.
        val linksUrl = URL("$PAYMONGO_API_BASE/v1/links?reference_number=$referenceNumber")
        val linksResponse = makeAuthenticatedRequest(linksUrl)

        if (!linksResponse.success) {
            return VerificationResult(
                success = false,
                errorMessage = "Could not look up reference. Please check and try again."
            )
        }

        val linksJson = JSONObject(linksResponse.body)
        val dataArray = linksJson.optJSONArray("data")
        if (dataArray == null || dataArray.length() == 0) {
            return VerificationResult(
                success = false,
                errorMessage = "Reference number not found. Please check and try again."
            )
        }

        // Check the first matching link.
        val link = dataArray.getJSONObject(0)
        val attributes = link.optJSONObject("attributes") ?: return VerificationResult(
            success = false,
            errorMessage = "Invalid payment data."
        )

        val status = attributes.optString("status", "")
        if (status != "paid") {
            return VerificationResult(
                success = false,
                errorMessage = when (status) {
                    "unpaid" -> "This payment has not been completed yet."
                    "expired" -> "This payment link has expired."
                    else -> "Payment status: $status. Please complete payment first."
                }
            )
        }

        // Payment is verified — determine which systems to unlock.
        val amount = attributes.optInt("amount", 0)
        val systemIds = resolveSystemIdsFromAmount(amount)

        return if (systemIds.isNotEmpty()) {
            VerificationResult(success = true, unlockedSystemIds = systemIds)
        } else {
            // Default to unlock-all if amount doesn't match specific tiers.
            VerificationResult(
                success = true,
                unlockedSystemIds = CountingSystemMonetization.paidSystemIds
            )
        }
    }

    private fun makeAuthenticatedRequest(url: URL): ApiResponse {
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            // PayMongo uses Basic auth with secret key as username, empty password.
            val credentials = Base64.encodeToString(
                "$PAYMONGO_SECRET_KEY:".toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            connection.setRequestProperty("Authorization", "Basic $credentials")
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            ApiResponse(success = responseCode in 200..299, body = responseBody)
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Map payment amount (in centavos) to system IDs.
     * Adjust these amounts to match your PayMongo payment link prices.
     */
    private fun resolveSystemIdsFromAmount(amountCentavos: Int): Set<String> {
        // Default: any payment unlocks all systems (unlock-all product).
        // If you create separate payment links for individual systems,
        // map amounts here, e.g.:
        // 19900 (₱199.00) -> setOf(CountingSystemId.HI_OPT_II)
        return CountingSystemMonetization.paidSystemIds
    }

    // ---- Encrypted local storage --------------------------------------------

    private fun storeActivation(reference: String, systemIds: Set<String>) {
        val existing = getStoredSystemIds()
        val updated = existing + systemIds
        val encrypted = encrypt(updated.joinToString(","))
        prefs.edit()
            .putString(KEY_ACTIVATED_SYSTEMS, encrypted)
            .putString(KEY_PAYMENT_HASH, hashValue(reference))
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

    private fun hashValue(value: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(value.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    // ---- Constants ----------------------------------------------------------

    private data class ApiResponse(
        val success: Boolean,
        val body: String
    )

    private data class VerificationResult(
        val success: Boolean,
        val unlockedSystemIds: Set<String> = emptySet(),
        val errorMessage: String? = null
    )

    companion object {
        private const val PAYMONGO_API_BASE = "https://api.paymongo.com"
        private const val PREFS_NAME = "license_activations"
        private const val KEY_ACTIVATED_SYSTEMS = "activated_systems"
        private const val KEY_PAYMENT_HASH = "payment_hash"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS = "cardcounter_license_key"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128

        // TODO: Replace with your PayMongo secret key (sk_test_... or sk_live_...).
        private const val PAYMONGO_SECRET_KEY = "sk_test_REPLACE_WITH_YOUR_KEY"

        // TODO: Replace with your PayMongo payment link URL.
        // Create this in PayMongo Dashboard > Payment Links.
        private const val PAYMENT_LINK_UNLOCK_ALL = "https://pm.link/REPLACE_WITH_YOUR_LINK"
    }
}
