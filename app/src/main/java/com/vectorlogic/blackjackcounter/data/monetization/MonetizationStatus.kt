package com.vectorlogic.blackjackcounter.data.monetization

import com.vectorlogic.blackjackcounter.domain.model.EntitlementTier

data class MonetizationStatus(
    val tier: EntitlementTier = EntitlementTier.FREE,
    val hasFullUnlock: Boolean = false,
    val isBillingReady: Boolean = false,
    val isPurchaseInFlight: Boolean = false,
    val proDisplayPrice: String = "\$1.99",
    val fullDisplayPrice: String = "\$3.99",
    val message: String? = null
)
