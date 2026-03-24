package com.vectorlogic.blackjackcounter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainScreenShowsCoreCounterControls() {
        composeRule.onNodeWithText("Running").assertIsDisplayed()
        composeRule.onNodeWithText("True").assertIsDisplayed()
        composeRule.onNodeWithText("Edge %").assertIsDisplayed()
        composeRule.onNodeWithText("Shoe").assertIsDisplayed()
        composeRule.onNodeWithText("Reset").assertIsDisplayed()
        composeRule.onNodeWithText("Strategy").assertIsDisplayed()
    }
}
