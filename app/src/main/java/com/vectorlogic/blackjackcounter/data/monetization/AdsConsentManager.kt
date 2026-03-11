package com.vectorlogic.blackjackcounter.data.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.vectorlogic.blackjackcounter.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdsConsentManager {

    private const val LOG_TAG = "AdsConsentManager"

    private val mutableCanRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = mutableCanRequestAds.asStateFlow()
    private val mutableCanShowPrivacyOptions = MutableStateFlow(false)
    val canShowPrivacyOptions: StateFlow<Boolean> = mutableCanShowPrivacyOptions.asStateFlow()

    @Volatile
    private var mobileAdsInitialized: Boolean = false

    fun requestConsentAndInitializeAds(activity: Activity) {
        try {
            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

            // Respect previously stored consent immediately while refreshing status in background.
            updateConsentState(consentInformation, activity.applicationContext)

            val requestParameters = ConsentRequestParameters.Builder().build()
            consentInformation.requestConsentInfoUpdate(
                activity,
                requestParameters,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        if (formError != null && BuildConfig.DEBUG) {
                            Log.w(LOG_TAG, "Consent form dismissed with error: ${formError.message}")
                        }
                        updateConsentState(consentInformation, activity.applicationContext)
                    }
                },
                { requestError ->
                    if (BuildConfig.DEBUG) {
                        logConsentError("Consent info update failed", requestError)
                    }
                    updateConsentState(consentInformation, activity.applicationContext)
                }
            )
        } catch (error: Throwable) {
            mutableCanRequestAds.value = false
            mutableCanShowPrivacyOptions.value = false
            Log.e(LOG_TAG, "Consent flow initialization failed", error)
        }
    }

    fun showPrivacyOptionsForm(activity: Activity) {
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                if (formError != null && BuildConfig.DEBUG) {
                    Log.w(LOG_TAG, "Privacy options form error: ${formError.message}")
                }
                val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
                updateConsentState(consentInformation, activity.applicationContext)
            }
        } catch (error: Throwable) {
            Log.e(LOG_TAG, "Privacy options form failed", error)
        }
    }

    private fun updateConsentState(
        consentInformation: ConsentInformation,
        context: Context
    ) {
        val canRequest = consentInformation.canRequestAds()
        mutableCanRequestAds.value = canRequest
        mutableCanShowPrivacyOptions.value =
            consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        if (canRequest) {
            initializeMobileAds(context)
        }
    }

    @Synchronized
    private fun initializeMobileAds(context: Context) {
        if (mobileAdsInitialized) return
        try {
            MobileAds.initialize(context)
            mobileAdsInitialized = true
        } catch (error: Throwable) {
            mutableCanRequestAds.value = false
            Log.e(LOG_TAG, "Mobile ads initialization failed", error)
        }
    }

    private fun logConsentError(prefix: String, error: FormError) {
        Log.w(LOG_TAG, "$prefix: ${error.errorCode} ${error.message}")
    }
}
