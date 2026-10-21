package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.CountRampStep
import com.vectorlogic.blackjackcounter.domain.model.RunningCountReferences

data class CountingSystemProfile(
    // Native true count / trueCountScale = Hi-Lo-equivalent true count. See TrueCountScaling.
    val trueCountScale: Double = 1.0,
    // Native true-count thresholds, compared with CounterCalculations.decisionTrueCount.
    val balancedBetRamp: List<CountRampStep> = emptyList(),
    val runningCountReferencesByDeck: Map<Int, RunningCountReferences> = emptyMap(),
    val runningCountBetRampByDeck: Map<Int, List<CountRampStep>> = emptyMap()
) {
    fun initialRunningCount(totalDecks: Int): Int {
        return runningCountReferencesByDeck[totalDecks]?.initialRunningCount ?: 0
    }

    fun keyCount(totalDecks: Int): Int? {
        return runningCountReferencesByDeck[totalDecks]?.keyCount
    }

    fun pivot(totalDecks: Int): Int? {
        return runningCountReferencesByDeck[totalDecks]?.pivot
    }

    fun runningCountBetRamp(totalDecks: Int): List<CountRampStep> {
        return runningCountBetRampByDeck[totalDecks].orEmpty()
    }
}

object CountingSystemProfiles {
    // Hi-Lo true count to units. Every balanced system uses these thresholds scaled to its own
    // true count (one decimal, see TrueCountScaling.toNativeThreshold).
    private val hiLoBalancedBetRamp: List<Pair<Int, Int>> = listOf(1 to 2, 2 to 4, 3 to 8, 4 to 12)

    // Knock-Out Blackjack (Vancura & Fuchs) publishes key counts for these deck counts only;
    // the 2 - decks formula matches 6D and 8D but not 1D (+2) or 2D (+1).
    private val koPublishedKeyCounts: Map<Int, Int> = mapOf(1 to 2, 2 to 1, 6 to -4, 8 to -6)

    private val koReferencesByDeck: Map<Int, RunningCountReferences> = (1..8).associateWith { decks ->
        RunningCountReferences(
            initialRunningCount = 4 - (4 * decks),
            keyCount = koPublishedKeyCounts[decks] ?: (2 - decks),
            pivot = 4
        )
    }

    private val rekoReferencesByDeck: Map<Int, RunningCountReferences> = (1..8).associateWith { decks ->
        val initialCount = 1 - (3 * decks)
        val keyCount = 3 - decks
        RunningCountReferences(
            initialRunningCount = initialCount,
            keyCount = keyCount,
            pivot = 4
        )
    }

    fun scaledBalancedBetRamp(trueCountScale: Double): List<CountRampStep> {
        return hiLoBalancedBetRamp.map { (hiLoTrueCount, units) ->
            CountRampStep(
                minCountInclusive = TrueCountScaling.toNativeThreshold(hiLoTrueCount, trueCountScale),
                units = units
            )
        }
    }

    private fun balancedProfile(system: CountingSystem): CountingSystemProfile {
        val scale = TrueCountScaling.hiLoEquivalentScale(system)
        return CountingSystemProfile(
            trueCountScale = scale,
            balancedBetRamp = scaledBalancedBetRamp(scale)
        )
    }

    private fun buildUnbalancedRampByDeck(
        referencesByDeck: Map<Int, RunningCountReferences>
    ): Map<Int, List<CountRampStep>> {
        return referencesByDeck.mapValues { (_, refs) ->
            listOf(
                CountRampStep(minCountInclusive = refs.keyCount.toDouble(), units = 2),
                CountRampStep(minCountInclusive = refs.pivot.toDouble(), units = 4),
                CountRampStep(minCountInclusive = (refs.pivot + 4).toDouble(), units = 8),
                CountRampStep(minCountInclusive = (refs.pivot + 8).toDouble(), units = 12)
            )
        }
    }

    private val profiles: Map<String, CountingSystemProfile> = mapOf(
        HiLoCountingSystem.id to balancedProfile(HiLoCountingSystem),
        HiOptIICountingSystem.id to balancedProfile(HiOptIICountingSystem),
        OmegaIICountingSystem.id to balancedProfile(OmegaIICountingSystem),
        ZenCountSystem.id to balancedProfile(ZenCountSystem),
        WongHalvesSystem.id to balancedProfile(WongHalvesSystem),
        KOCountingSystem.id to CountingSystemProfile(
            runningCountReferencesByDeck = koReferencesByDeck,
            runningCountBetRampByDeck = buildUnbalancedRampByDeck(koReferencesByDeck)
        ),
        ReKOCountingSystem.id to CountingSystemProfile(
            runningCountReferencesByDeck = rekoReferencesByDeck,
            runningCountBetRampByDeck = buildUnbalancedRampByDeck(rekoReferencesByDeck)
        )
    )

    fun profileFor(systemId: String): CountingSystemProfile {
        return profiles[systemId] ?: CountingSystemProfile(
            balancedBetRamp = scaledBalancedBetRamp(1.0)
        )
    }
}
