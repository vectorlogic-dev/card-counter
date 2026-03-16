package com.vectorlogic.blackjackcounter.domain.model

data class CountRampStep(
    val minCountInclusive: Int,
    val units: Int
)

data class RunningCountReferences(
    val initialRunningCount: Int,
    val keyCount: Int,
    val pivot: Int
)
