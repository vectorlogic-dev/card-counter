package com.vectorlogic.blackjackcounter.data.counting

import com.vectorlogic.blackjackcounter.domain.model.TrueCountIntegerization

data class CountRampStep(
    val minCountInclusive: Int,
    val units: Int
)

data class RunningCountReferences(
    val initialRunningCount: Int,
    val keyCount: Int,
    val pivot: Int
)

data class CountingSystemProfile(
    val trueCountIntegerization: TrueCountIntegerization? = null,
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
    private val defaultBalancedBetRamp: List<CountRampStep> = listOf(
        CountRampStep(minCountInclusive = 1, units = 2),
        CountRampStep(minCountInclusive = 2, units = 4),
        CountRampStep(minCountInclusive = 3, units = 8),
        CountRampStep(minCountInclusive = 4, units = 12)
    )

    private val koReferencesByDeck: Map<Int, RunningCountReferences> = (1..8).associateWith { decks ->
        val keyCount = 2 - decks
        RunningCountReferences(
            initialRunningCount = 4 - (4 * decks),
            keyCount = keyCount,
            pivot = 4
        )
    }.toMutableMap().also { map ->
        map[1] = RunningCountReferences(
            initialRunningCount = 0,
            keyCount = 1,
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

    private fun buildUnbalancedRampByDeck(
        referencesByDeck: Map<Int, RunningCountReferences>
    ): Map<Int, List<CountRampStep>> {
        return referencesByDeck.mapValues { (_, refs) ->
            listOf(
                CountRampStep(minCountInclusive = refs.keyCount, units = 2),
                CountRampStep(minCountInclusive = refs.pivot, units = 4),
                CountRampStep(minCountInclusive = refs.pivot + 4, units = 8),
                CountRampStep(minCountInclusive = refs.pivot + 8, units = 12)
            )
        }
    }

    private val profiles: Map<String, CountingSystemProfile> = mapOf(
        HiLoCountingSystem.id to CountingSystemProfile(
            trueCountIntegerization = TrueCountIntegerization.TRUNCATE,
            balancedBetRamp = defaultBalancedBetRamp
        ),
        HiOptIICountingSystem.id to CountingSystemProfile(
            trueCountIntegerization = TrueCountIntegerization.TRUNCATE,
            balancedBetRamp = defaultBalancedBetRamp
        ),
        OmegaIICountingSystem.id to CountingSystemProfile(
            trueCountIntegerization = TrueCountIntegerization.TRUNCATE,
            balancedBetRamp = defaultBalancedBetRamp
        ),
        ZenCountSystem.id to CountingSystemProfile(
            trueCountIntegerization = TrueCountIntegerization.TRUNCATE,
            balancedBetRamp = defaultBalancedBetRamp
        ),
        WongHalvesSystem.id to CountingSystemProfile(
            trueCountIntegerization = TrueCountIntegerization.TRUNCATE,
            balancedBetRamp = defaultBalancedBetRamp
        ),
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
            trueCountIntegerization = TrueCountIntegerization.TRUNCATE,
            balancedBetRamp = defaultBalancedBetRamp
        )
    }
}
