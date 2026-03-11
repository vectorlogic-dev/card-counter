package com.vectorlogic.blackjackcounter.data.counting

object CountingSystemsCatalog {
    val allSystems: List<CountingSystem> = listOf(
        HiLoCountingSystem,
        KOCountingSystem,
        ReKOCountingSystem,
        HiOptIICountingSystem,
        OmegaIICountingSystem,
        ZenCountSystem,
        WongHalvesSystem
    )

    val systemsById: Map<String, CountingSystem> = allSystems.associateBy { it.id }

    val defaultSystem: CountingSystem = HiLoCountingSystem

    fun getOrDefault(systemId: String): CountingSystem {
        return systemsById[systemId] ?: defaultSystem
    }
}
