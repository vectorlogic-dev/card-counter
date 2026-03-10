package com.yourname.blackjackcounter.data.monetization

import com.yourname.blackjackcounter.domain.model.EntitlementTier

data class MonetizationStatus(
    val tier: EntitlementTier = EntitlementTier.FREE,
    val hasFullUnlock: Boolean = false,
    val isBillingReady: Boolean = false,
    val isPurchaseInFlight: Boolean = false,
    val proDisplayPrice: String = "\$2.99",
    val fullDisplayPrice: String = "\$4.99",
    val message: String? = null
)
