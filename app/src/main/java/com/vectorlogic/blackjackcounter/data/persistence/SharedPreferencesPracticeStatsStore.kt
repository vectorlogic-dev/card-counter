package com.vectorlogic.blackjackcounter.data.persistence

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.vectorlogic.blackjackcounter.domain.persistence.PracticeStatsStore
import com.vectorlogic.blackjackcounter.domain.stats.PracticeHistory
import com.vectorlogic.blackjackcounter.domain.stats.PracticeHistoryCodec

/** Its own preferences file, so resetting or losing the shoe never touches practice history. */
class SharedPreferencesPracticeStatsStore(context: Context) : PracticeStatsStore {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): PracticeHistory = PracticeHistory(
        drills = PracticeHistoryCodec.decodeDrills(prefs.getString(KEY_DRILLS, null).orEmpty()),
        quizzes = PracticeHistoryCodec.decodeQuizzes(prefs.getString(KEY_QUIZZES, null).orEmpty())
    )

    override fun save(history: PracticeHistory) {
        prefs.edit {
            putString(KEY_DRILLS, PracticeHistoryCodec.encodeDrills(history.drills))
            putString(KEY_QUIZZES, PracticeHistoryCodec.encodeQuizzes(history.quizzes))
        }
    }

    private companion object {
        const val PREFS_NAME = "practice_stats"
        // The version is in the key: a new format gets new keys and leaves old data unread.
        const val KEY_DRILLS = "drills_v1"
        const val KEY_QUIZZES = "quizzes_v1"
    }
}
