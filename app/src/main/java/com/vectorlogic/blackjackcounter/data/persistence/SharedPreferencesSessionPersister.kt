package com.vectorlogic.blackjackcounter.data.persistence

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.vectorlogic.blackjackcounter.data.counting.CountingSystemsCatalog
import com.vectorlogic.blackjackcounter.domain.model.BlackjackPayoutRule
import com.vectorlogic.blackjackcounter.domain.model.CardRank
import com.vectorlogic.blackjackcounter.domain.model.CounterSessionState
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
import com.vectorlogic.blackjackcounter.domain.model.SeenCard
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.TableRules
import com.vectorlogic.blackjackcounter.domain.persistence.SessionPersister

class SharedPreferencesSessionPersister(context: Context) : SessionPersister {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): CounterSessionState? {
        if (!prefs.contains(KEY_SCHEMA_VERSION)) return null
        if (prefs.getInt(KEY_SCHEMA_VERSION, 0) != SCHEMA_VERSION) return null

        val systemId = prefs.getString(KEY_SYSTEM_ID, null) ?: return null
        val system = CountingSystemsCatalog.systemsById[systemId] ?: return null

        val totalDecks = prefs.getInt(KEY_TOTAL_DECKS, -1).takeIf { it in 1..8 } ?: return null
        val decksRemaining = readDouble(KEY_DECKS_REMAINING) ?: return null
        val runningCount = readDouble(KEY_RUNNING_COUNT) ?: return null
        val acesSeen = prefs.getInt(KEY_ACES_SEEN, 0).coerceAtLeast(0)

        val seenRanks = prefs.getString(KEY_SEEN_RANKS, "")
            .orEmpty()
            .split(',')
            .filter { it.isNotBlank() }
            .mapNotNull { token -> token.toIntOrNull()?.let { CardRank.entries.getOrNull(it) } }

        // Older saves have no per-card penetration; treat each card as one card's worth.
        val decksAdvanced = prefs.getString(KEY_SEEN_DECKS_ADVANCED, "")
            .orEmpty()
            .split(',')
            .filter { it.isNotBlank() }
            .map { token -> token.toDoubleOrNull()?.coerceAtLeast(0.0) }
            .takeIf { values -> values.size == seenRanks.size && values.all { it != null } }

        val shouldTrackAces = system.recommendsAceSideCount()
        val seenCards = seenRanks.mapIndexed { i, rank ->
            SeenCard(
                rank = rank,
                deltaApplied = system.getTagValue(rank),
                countedAce = shouldTrackAces && rank == CardRank.ACE,
                decksAdvanced = decksAdvanced?.get(i) ?: SeenCard.DECKS_PER_CARD
            )
        }

        return CounterSessionState(
            runningCount = runningCount,
            totalDecks = totalDecks,
            decksRemaining = decksRemaining,
            acesSeen = acesSeen,
            selectedCountingSystemId = systemId,
            seenCards = seenCards,
            tableRules = readTableRules()
        )
    }

    override fun save(state: CounterSessionState) {
        prefs.edit {
            putInt(KEY_SCHEMA_VERSION, SCHEMA_VERSION)
            putString(KEY_SYSTEM_ID, state.selectedCountingSystemId)
            putInt(KEY_TOTAL_DECKS, state.totalDecks)
            putLong(KEY_DECKS_REMAINING, java.lang.Double.doubleToRawLongBits(state.decksRemaining))
            putLong(KEY_RUNNING_COUNT, java.lang.Double.doubleToRawLongBits(state.runningCount))
            putInt(KEY_ACES_SEEN, state.acesSeen)
            putString(
                KEY_SEEN_RANKS,
                state.seenCards.joinToString(",") { it.rank.ordinal.toString() }
            )
            putString(
                KEY_SEEN_DECKS_ADVANCED,
                state.seenCards.joinToString(",") { it.decksAdvanced.toString() }
            )
            writeTableRules(this, state.tableRules)
        }
    }

    private fun readDouble(key: String): Double? {
        if (!prefs.contains(key)) return null
        return java.lang.Double.longBitsToDouble(prefs.getLong(key, 0L))
    }

    private fun readTableRules(): TableRules {
        val defaults = TableRules()
        return TableRules(
            dealerSoft17Rule = readEnum(KEY_S17, defaults.dealerSoft17Rule),
            dealerPeekRule = readEnum(KEY_PEEK, defaults.dealerPeekRule),
            blackjackPayoutRule = readEnum(KEY_PAYOUT, defaults.blackjackPayoutRule),
            doubleDownRule = readEnum(KEY_DOUBLE, defaults.doubleDownRule),
            doubleAfterSplit = prefs.getBoolean(KEY_DAS, defaults.doubleAfterSplit),
            surrenderRule = readEnum(KEY_SURRENDER, defaults.surrenderRule),
            resplitAces = prefs.getBoolean(KEY_RESPLIT_ACES, defaults.resplitAces),
            hitSplitAces = prefs.getBoolean(KEY_HIT_SPLIT_ACES, defaults.hitSplitAces),
            maxSplitHands = prefs.getInt(KEY_MAX_SPLIT, defaults.maxSplitHands).coerceIn(2, 6),
            sixCardCharlieAutoWin = prefs.getBoolean(KEY_CHARLIE, defaults.sixCardCharlieAutoWin)
        )
    }

    private fun writeTableRules(editor: SharedPreferences.Editor, rules: TableRules) {
        editor.putString(KEY_S17, rules.dealerSoft17Rule.name)
        editor.putString(KEY_PEEK, rules.dealerPeekRule.name)
        editor.putString(KEY_PAYOUT, rules.blackjackPayoutRule.name)
        editor.putString(KEY_DOUBLE, rules.doubleDownRule.name)
        editor.putBoolean(KEY_DAS, rules.doubleAfterSplit)
        editor.putString(KEY_SURRENDER, rules.surrenderRule.name)
        editor.putBoolean(KEY_RESPLIT_ACES, rules.resplitAces)
        editor.putBoolean(KEY_HIT_SPLIT_ACES, rules.hitSplitAces)
        editor.putInt(KEY_MAX_SPLIT, rules.maxSplitHands)
        editor.putBoolean(KEY_CHARLIE, rules.sixCardCharlieAutoWin)
    }

    private inline fun <reified E : Enum<E>> readEnum(key: String, default: E): E {
        val name = prefs.getString(key, null) ?: return default
        return runCatching { enumValueOf<E>(name) }.getOrDefault(default)
    }

    private companion object {
        const val PREFS_NAME = "counter_session"
        const val SCHEMA_VERSION = 1

        const val KEY_SCHEMA_VERSION = "schema_version"
        const val KEY_SYSTEM_ID = "system_id"
        const val KEY_TOTAL_DECKS = "total_decks"
        const val KEY_DECKS_REMAINING = "decks_remaining_bits"
        const val KEY_RUNNING_COUNT = "running_count_bits"
        const val KEY_ACES_SEEN = "aces_seen"
        const val KEY_SEEN_RANKS = "seen_ranks_csv"
        const val KEY_SEEN_DECKS_ADVANCED = "seen_decks_advanced_csv"

        const val KEY_S17 = "rule_s17"
        const val KEY_PEEK = "rule_peek"
        const val KEY_PAYOUT = "rule_payout"
        const val KEY_DOUBLE = "rule_double"
        const val KEY_DAS = "rule_das"
        const val KEY_SURRENDER = "rule_surrender"
        const val KEY_RESPLIT_ACES = "rule_resplit_aces"
        const val KEY_HIT_SPLIT_ACES = "rule_hit_split_aces"
        const val KEY_MAX_SPLIT = "rule_max_split"
        const val KEY_CHARLIE = "rule_charlie"
    }
}
