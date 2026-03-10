package com.vectorlogic.blackjackcounter.data.monetization

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface MonetizationRepository {
    val status: StateFlow<MonetizationStatus>

    fun refresh()
    fun launchProPurchase(activity: Activity)
    fun launchFullPurchase(activity: Activity)
    fun clearMessage()
    fun release()
}
