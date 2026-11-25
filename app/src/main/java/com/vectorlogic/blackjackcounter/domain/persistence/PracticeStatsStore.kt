package com.vectorlogic.blackjackcounter.domain.persistence

import com.vectorlogic.blackjackcounter.domain.stats.PracticeHistory

/** Keeps drill and quiz results across launches, apart from the shoe being counted. */
interface PracticeStatsStore {
    fun load(): PracticeHistory
    fun save(history: PracticeHistory)
}
