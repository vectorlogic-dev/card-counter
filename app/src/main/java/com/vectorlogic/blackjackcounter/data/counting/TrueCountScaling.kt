package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.CardRank
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Converts between a balanced system's native true count and a Hi-Lo-equivalent true count.
 *
 * Bet ramps, playing indices and the edge slope in this app are calibrated for Hi-Lo. Level-2
 * systems produce true counts roughly 1.7-1.8x larger for the same shoe, so sharing Hi-Lo's
 * thresholds would make them bet and deviate far too early. The scale is the ratio of the
 * system's per-card tag standard deviation (full-deck composition) to Hi-Lo's:
 * Hi-Opt II ~1.67, Omega II / Zen ~1.79, Wong Halves ~1.05.
 */
object TrueCountScaling {

    fun tagStandardDeviation(system: CountingSystem): Double {
        val tags = CardRank.entries.map { rank -> system.getTagValue(rank) }
        val mean = tags.average()
        val variance = tags.sumOf { tag -> (tag - mean) * (tag - mean) } / tags.size
        return sqrt(variance)
    }

    /** Native true count divided by this value gives a Hi-Lo-equivalent true count. */
    fun hiLoEquivalentScale(system: CountingSystem): Double {
        return tagStandardDeviation(system) / tagStandardDeviation(HiLoCountingSystem)
    }

    /** Converts a Hi-Lo index or ramp threshold to the nearest native integer true count. */
    fun toNativeThreshold(hiLoThreshold: Int, scale: Double): Int {
        return (hiLoThreshold * scale).roundToInt()
    }
}
