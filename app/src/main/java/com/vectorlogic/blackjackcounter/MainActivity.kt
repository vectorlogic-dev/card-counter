package com.vectorlogic.blackjackcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vectorlogic.blackjackcounter.data.monetization.AdsConsentManager
import com.vectorlogic.blackjackcounter.presentation.counter.CounterScreen
import com.vectorlogic.blackjackcounter.ui.theme.BlackjackCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsConsentManager.requestConsentAndInitializeAds(this)
        enableEdgeToEdge()
        setContent {
            BlackjackCounterTheme {
                CounterScreen()
            }
        }
    }
}
