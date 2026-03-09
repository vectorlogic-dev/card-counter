package com.yourname.blackjackcounter.data.counting

import com.yourname.blackjackcounter.domain.model.CardRank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CountingSystemsTest {

    @Test
    fun `hi opt ii has expected ace and ten tags`() {
        assertEquals(0.0, HiOptIICountingSystem.getTagValue(CardRank.ACE), 0.0001)
        assertEquals(-2.0, HiOptIICountingSystem.getTagValue(CardRank.TEN), 0.0001)
        assertTrue(HiOptIICountingSystem.recommendsAceSideCount())
    }

    @Test
    fun `reko is unbalanced running count system`() {
        assertFalse(ReKOCountingSystem.usesTrueCount())
        assertFalse(ReKOCountingSystem.isBalanced)
        assertEquals(1.0, ReKOCountingSystem.getTagValue(CardRank.SEVEN), 0.0001)
    }
}
