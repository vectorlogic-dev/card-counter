package com.vectorlogic.blackjackcounter.domain.model

data class CountRampStep(
    // A true count for balanced systems (may be fractional once scaled), a running count otherwise.
    val minCountInclusive: Double,
    val units: Int
)

data class RunningCountReferences(
    val initialRunningCount: Int,
    val keyCount: Int,
    val pivot: Int
)
