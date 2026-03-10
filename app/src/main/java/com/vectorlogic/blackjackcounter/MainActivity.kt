package com.vectorlogic.blackjackcounter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vectorlogic.blackjackcounter.data.monetization.AdsConsentManager
import com.vectorlogic.blackjackcounter.presentation.counter.CounterScreen
import com.vectorlogic.blackjackcounter.ui.theme.BlackjackCounterTheme

class MainActivity : ComponentActivity() {
    private companion object {
        private const val LOG_TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            AdsConsentManager.requestConsentAndInitializeAds(this)
        }.onFailure { error ->
            Log.e(LOG_TAG, "Failed to initialize consent/ads", error)
        }
        enableEdgeToEdge()
        setContent {
            BlackjackCounterTheme {
                CounterScreen()
            }
        }
    }
}
