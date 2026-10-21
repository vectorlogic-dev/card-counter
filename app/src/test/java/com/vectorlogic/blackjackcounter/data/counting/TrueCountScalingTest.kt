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
    fun `native thresholds keep one decimal of the scaled hi lo value`() {
        val zen = TrueCountScaling.hiLoEquivalentScale(ZenCountSystem)

        assertEquals(0.0, TrueCountScaling.toNativeThreshold(0, zen), 0.0)
        assertEquals(1.8, TrueCountScaling.toNativeThreshold(1, zen), 0.0)
        assertEquals(5.4, TrueCountScaling.toNativeThreshold(3, zen), 0.0)
        assertEquals(8.9, TrueCountScaling.toNativeThreshold(5, zen), 0.0)
        assertEquals(-1.8, TrueCountScaling.toNativeThreshold(-1, zen), 0.0)
        assertEquals(-3.6, TrueCountScaling.toNativeThreshold(-2, zen), 0.0)
        assertEquals(4.0, TrueCountScaling.toNativeThreshold(4, 1.0), 0.0)
    }

    @Test
    fun `omega ii plus two is not rounded up to a whole native count`() {
        val omega = TrueCountScaling.hiLoEquivalentScale(OmegaIICountingSystem)
        val hiOpt = TrueCountScaling.hiLoEquivalentScale(HiOptIICountingSystem)

        // 2 x 1.789 = 3.578. Rounding to 4 would ask for Hi-Lo +2.24 before betting 4 units.
        assertEquals(3.6, TrueCountScaling.toNativeThreshold(2, omega), 0.0)
        assertEquals(3.3, TrueCountScaling.toNativeThreshold(2, hiOpt), 0.0)
    }
}
