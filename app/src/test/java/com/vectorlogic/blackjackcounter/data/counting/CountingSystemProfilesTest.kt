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
}
