package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.TrueCountIntegerization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CountingSystemProfilesTest {

    @Test
    fun `balanced systems define true count integerization`() {
        val hiLo = CountingSystemProfiles.profileFor(HiLoCountingSystem.id)
        val wong = CountingSystemProfiles.profileFor(WongHalvesSystem.id)

        assertEquals(TrueCountIntegerization.TRUNCATE, hiLo.trueCountIntegerization)
        assertEquals(TrueCountIntegerization.TRUNCATE, wong.trueCountIntegerization)
        assertEquals(4, hiLo.balancedBetRamp.size)
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

        assertEquals(listOf(1, 2, 3, 4), thresholds(HiLoCountingSystem.id))
        assertEquals(listOf(1, 2, 3, 4), thresholds(WongHalvesSystem.id))
        assertEquals(listOf(2, 3, 5, 7), thresholds(HiOptIICountingSystem.id))
        assertEquals(listOf(2, 4, 5, 7), thresholds(OmegaIICountingSystem.id))
        assertEquals(listOf(2, 4, 5, 7), thresholds(ZenCountSystem.id))
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
