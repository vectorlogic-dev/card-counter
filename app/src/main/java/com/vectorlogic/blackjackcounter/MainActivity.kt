package com.vectorlogic.blackjackcounter

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vectorlogic.blackjackcounter.data.security.AppIntegrityChecker
import com.vectorlogic.blackjackcounter.presentation.counter.CounterScreen
import com.vectorlogic.blackjackcounter.ui.theme.BlackjackCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AppIntegrityChecker.isIntegrityOk(this)) {
            Toast.makeText(this, "This app copy is not authorized.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrim = AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(scrim = AndroidColor.TRANSPARENT)
        )
        setContent {
            BlackjackCounterTheme {
                CounterScreen()
            }
        }
    }
}
