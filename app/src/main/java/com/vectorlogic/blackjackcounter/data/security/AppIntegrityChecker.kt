package com.vectorlogic.blackjackcounter.data.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import java.io.File
import java.security.MessageDigest

/**
 * Runtime integrity checks to detect tampering, repackaging, and debugging.
 */
object AppIntegrityChecker {

    @Volatile
    private var cachedResult: Boolean? = null

    private const val EXPECTED_PACKAGE = "com.vectorlogic.blackjackcounter"

    // SHA-256 of the release signing certificate.
    // Replace with your actual release certificate fingerprint (lowercase, no colons).
    // Run: keytool -list -v -keystore your.keystore | grep SHA256
    // then strip colons and lowercase.
    // Until set, this check is skipped.
    private val RELEASE_CERT_SHA256: String? = null

    private val ALLOWED_INSTALLERS = setOf(
        "com.android.vending",           // Google Play Store
        "com.google.android.packageinstaller", // device default
    )

    /**
     * Returns true if the app appears to be running in a legitimate, untampered state.
     * Caches result after first call since values don't change at runtime.
     */
    fun isIntegrityOk(context: Context): Boolean {
        cachedResult?.let { return it }
        val result = runChecks(context)
        cachedResult = result
        return result
    }

    private fun runChecks(context: Context): Boolean {
        if (isDebuggerAttached()) return false
        if (!isPackageNameValid(context)) return false
        if (!isInstallerAllowed(context)) return false
        if (RELEASE_CERT_SHA256 != null && !isSignatureValid(context)) return false
        if (isTestKeysRom()) return false
        return true
    }

    /** Detect an attached debugger (JDWP). */
    private fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    /** Verify the app's package name hasn't been changed (repackaged). */
    private fun isPackageNameValid(context: Context): Boolean {
        return context.packageName == EXPECTED_PACKAGE
    }

    /**
     * Verify the app was installed from an allowed source.
     * Side-loaded APKs will have a null installer.
     */
    private fun isInstallerAllowed(context: Context): Boolean {
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
        // During development / testing, allow null installer (sideloaded).
        // In production, you can make this strict by returning false for null.
        if (installer == null) return true
        return installer in ALLOWED_INSTALLERS
    }

    /**
     * Verify the signing certificate matches the expected release cert.
     * Prevents running a re-signed APK.
     */
    @Suppress("DEPRECATION", "PackageManagerGetSignatures")
    private fun isSignatureValid(context: Context): Boolean {
        val expectedHash = RELEASE_CERT_SHA256 ?: return true

        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo
            if (signingInfo == null) {
                null
            } else if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                .signatures
        }

        if (signatures.isNullOrEmpty()) return false

        val digest = MessageDigest.getInstance("SHA-256")
        return signatures.any { sig ->
            val hash = digest.digest(sig.toByteArray())
            hash.joinToString("") { "%02x".format(it) } == expectedHash
        }
    }

    /** Basic check for test-keys ROM (common on emulators / rooted devices). */
    private fun isTestKeysRom(): Boolean {
        val tags = Build.TAGS
        return tags != null && tags.contains("test-keys")
    }

    /**
     * Check for common root indicators.
     * Returns true if the device appears rooted.
     */
    fun isDeviceLikelyRooted(): Boolean {
        val suPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
        )
        return suPaths.any { File(it).exists() }
    }
}
