package com.vectorlogic.blackjackcounter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CounterScreenSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainScreenShowsCoreCounterControls() {
        // The counter screen is a scrolling column, so scroll each control into
        // view before asserting; lower controls are off-screen on short devices.
        listOf("Running", "True", "Edge %", "Strategy Table", "SHOE", "Reset").forEach { label ->
            composeRule.onNodeWithText(label)
                .performScrollTo()
                .assertIsDisplayed()
        }
    }
}
