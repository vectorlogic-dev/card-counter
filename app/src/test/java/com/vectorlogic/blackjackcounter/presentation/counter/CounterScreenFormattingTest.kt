package com.vectorlogic.blackjackcounter.presentation.counter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CounterScreenFormattingTest {

    @Test
    fun `system traits label names balance and count type`() {
        assertEquals(
            "Level 2 · Balanced · True count",
            systemTraitsLabel(level = 2, isBalanced = true, usesTrueCount = true)
        )
        assertEquals(
            "Level 1 · Unbalanced · Running count only",
            systemTraitsLabel(level = 1, isBalanced = false, usesTrueCount = false)
        )
    }

    @Test
    fun `switching mid-shoe reports the recount and the running count change`() {
        assertEquals(
            "Recounted 20 cards with Omega II: RC +13 → +22",
            systemSwitchMessage(
                previousSystemId = "hi_lo",
                previousRunningCount = 13.0,
                newSystemId = "omega_ii",
                newSystemName = "Omega II",
                newRunningCount = 22.0,
                cardsSeen = 20
            )
        )
        assertEquals(
            "Recounted 1 card with Wong Halves: RC +1 → +0.5",
            systemSwitchMessage("hi_lo", 1.0, "wong_halves", "Wong Halves", 0.5, cardsSeen = 1)
        )
    }

    @Test
    fun `switching on an empty shoe only reports a non-zero starting count`() {
        assertEquals(
            "KO starts at RC -20",
            systemSwitchMessage("hi_lo", 0.0, "ko", "KO", -20.0, cardsSeen = 0)
        )
        assertNull(systemSwitchMessage("hi_lo", 0.0, "zen", "Zen Count", 0.0, cardsSeen = 0))
    }

    @Test
    fun `reselecting the current system says nothing`() {
        assertNull(systemSwitchMessage("hi_lo", 5.0, "hi_lo", "Hi-Lo", 5.0, cardsSeen = 12))
    }
}
