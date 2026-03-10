package com.vectorlogic.blackjackcounter.presentation.counter.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.vectorlogic.blackjackcounter.BuildConfig
import com.vectorlogic.blackjackcounter.R
import com.vectorlogic.blackjackcounter.data.monetization.AdsConsentManager

private const val AD_LOG_TAG = "FreeTierBannerAd"

@Composable
fun FreeTierBannerAd(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val canRequestAds by AdsConsentManager.canRequestAds.collectAsState()
    if (!isVisible || !canRequestAds) return

    val context = LocalContext.current
    val adUnitId = stringResource(id = R.string.admob_banner_ad_unit_id)
    val adView = remember(context, adUnitId) {
        runCatching {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                if (BuildConfig.DEBUG) {
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d(AD_LOG_TAG, "Banner loaded.")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.e(
                                AD_LOG_TAG,
                                "Banner failed: code=${loadAdError.code}, message=${loadAdError.message}"
                            )
                        }
                    }
                }
            }
        }.onFailure { error ->
            Log.e(AD_LOG_TAG, "Failed to create AdView; ads disabled for this session.", error)
        }.getOrNull()
    }

    if (adView == null) {
        return
    }

    DisposableEffect(adView) {
        runCatching {
            adView.loadAd(AdRequest.Builder().build())
        }.onFailure { error ->
            Log.e(AD_LOG_TAG, "Failed to load banner; skipping ad.", error)
        }
        onDispose {
            runCatching {
                adView.destroy()
            }.onFailure { error ->
                Log.w(AD_LOG_TAG, "Error while destroying AdView.", error)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.height(50.dp),
            factory = { adView }
        )
    }
}
