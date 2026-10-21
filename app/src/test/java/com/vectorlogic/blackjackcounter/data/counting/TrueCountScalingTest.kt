package com.vectorlogic.blackjackcounter.data.counting

import org.junit.Assert.assertEquals
import org.junit.Test

class TrueCountScalingTest {

    @Test
    fun `tag standard deviations match full-deck composition`() {
        assertEquals(0.877, TrueCountScaling.tagStandardDeviation(HiLoCountingSystem), 0.001)
        assertEquals(1.468, TrueCountScaling.tagStandardDeviation(HiOptIICountingSystem), 0.001)
        assertEquals(1.569, TrueCountScaling.tagStandardDeviation(OmegaIICountingSystem), 0.001)
        assertEquals(1.569, TrueCountScaling.tagStandardDeviation(ZenCountSystem), 0.001)
        assertEquals(0.920, TrueCountScaling.tagStandardDeviation(WongHalvesSystem), 0.001)
    }

    @Test
    fun `hi lo equivalent scale is one for hi lo and larger for level two systems`() {
        assertEquals(1.0, TrueCountScaling.hiLoEquivalentScale(HiLoCountingSystem), 0.0)
        assertEquals(1.674, TrueCountScaling.hiLoEquivalentScale(HiOptIICountingSystem), 0.001)
        assertEquals(1.789, TrueCountScaling.hiLoEquivalentScale(OmegaIICountingSystem), 0.001)
        assertEquals(1.789, TrueCountScaling.hiLoEquivalentScale(ZenCountSystem), 0.001)
        assertEquals(1.049, TrueCountScaling.hiLoEquivalentScale(WongHalvesSystem), 0.001)
    }

    @Test
    fun `native thresholds round the scaled hi lo value`() {
        val zen = TrueCountScaling.hiLoEquivalentScale(ZenCountSystem)

        assertEquals(0, TrueCountScaling.toNativeThreshold(0, zen))
        assertEquals(2, TrueCountScaling.toNativeThreshold(1, zen))
        assertEquals(5, TrueCountScaling.toNativeThreshold(3, zen))
        assertEquals(9, TrueCountScaling.toNativeThreshold(5, zen))
        assertEquals(-2, TrueCountScaling.toNativeThreshold(-1, zen))
        assertEquals(-4, TrueCountScaling.toNativeThreshold(-2, zen))
        assertEquals(4, TrueCountScaling.toNativeThreshold(4, 1.0))
    }
}
