package com.yourname.blackjackcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yourname.blackjackcounter.presentation.counter.CounterScreen
import com.yourname.blackjackcounter.ui.theme.BlackjackCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlackjackCounterTheme {
                CounterScreen()
            }
        }
    }
}
