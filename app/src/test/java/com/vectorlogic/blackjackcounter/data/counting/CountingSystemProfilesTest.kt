package com.vectorlogic.blackjackcounter.data.counting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CountingSystemProfilesTest {

    @Test
    fun `balanced systems use a four step ramp`() {
        assertEquals(4, CountingSystemProfiles.profileFor(HiLoCountingSystem.id).balancedBetRamp.size)
        assertEquals(4, CountingSystemProfiles.profileFor(WongHalvesSystem.id).balancedBetRamp.size)
        assertEquals(4, CountingSystemProfiles.profileFor("unknown").balancedBetRamp.size)
    }

    @Test
    fun `ko and reko include deck aware references`() {
        val koSixDeck = CountingSystemProfiles.profileFor(KOCountingSystem.id)
            .runningCountReferencesByDeck[6]
        val rekoSixDeck = CountingSystemProfiles.profileFor(ReKOCountingSystem.id)
            .runningCountReferencesByDeck[6]

        assertNotNull(koSixDeck)
        assertNotNull(rekoSixDeck)
        assertEquals(-20, koSixDeck?.initialRunningCount)
        assertEquals(-4, koSixDeck?.keyCount)
        assertEquals(4, koSixDeck?.pivot)
        assertEquals(-17, rekoSixDeck?.initialRunningCount)
        assertEquals(-3, rekoSixDeck?.keyCount)
        assertEquals(4, rekoSixDeck?.pivot)
    }

    @Test
    fun `ko key counts match published rookie table`() {
        val ko = CountingSystemProfiles.profileFor(KOCountingSystem.id)

        assertEquals(2, ko.keyCount(1))
        assertEquals(1, ko.keyCount(2))
        assertEquals(-4, ko.keyCount(6))
        assertEquals(-6, ko.keyCount(8))
        assertEquals(0, ko.initialRunningCount(1))
        assertEquals(-4, ko.initialRunningCount(2))
        assertEquals(-28, ko.initialRunningCount(8))
    }

    @Test
    fun `balanced bet ramps are hi lo thresholds converted to native true counts`() {
        fun thresholds(systemId: String) =
            CountingSystemProfiles.profileFor(systemId).balancedBetRamp.map { it.minCountInclusive }

        // Hi-Lo TC 1 / 2 / 3 / 4 times each system's scale, kept to one decimal (README table).
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0), thresholds(HiLoCountingSystem.id))
        assertEquals(listOf(1.0, 2.1, 3.1, 4.2), thresholds(WongHalvesSystem.id))
        assertEquals(listOf(1.7, 3.3, 5.0, 6.7), thresholds(HiOptIICountingSystem.id))
        assertEquals(listOf(1.8, 3.6, 5.4, 7.2), thresholds(OmegaIICountingSystem.id))
        assertEquals(listOf(1.8, 3.6, 5.4, 7.2), thresholds(ZenCountSystem.id))
        assertEquals(
            listOf(2, 4, 8, 12),
            CountingSystemProfiles.profileFor(ZenCountSystem.id).balancedBetRamp.map { it.units }
        )
    }

    @Test
    fun `balanced profiles carry the hi lo equivalent scale`() {
        assertEquals(1.0, CountingSystemProfiles.profileFor(HiLoCountingSystem.id).trueCountScale, 0.0)
        assertEquals(
            TrueCountScaling.hiLoEquivalentScale(ZenCountSystem),
            CountingSystemProfiles.profileFor(ZenCountSystem.id).trueCountScale,
            0.0
        )
    }
}
