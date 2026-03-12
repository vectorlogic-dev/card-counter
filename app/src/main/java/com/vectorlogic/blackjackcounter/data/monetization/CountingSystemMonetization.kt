package com.vectorlogic.blackjackcounter.data.monetization

import com.vectorlogic.blackjackcounter.domain.model.CountingSystemId

data class PaidSystemUnlockProduct(
    val systemId: String,
    val productId: String,
    val level: Int
)

object CountingSystemMonetization {
    private const val FREE_MAX_LEVEL = 1
    private const val UNLOCK_ALL = "unlock_all"
    private const val UNLOCK_HI_OPT_II = "unlock_hi_opt_ii"
    private const val UNLOCK_OMEGA_II = "unlock_omega_ii"
    private const val UNLOCK_ZEN_COUNT = "unlock_zen_count"
    private const val UNLOCK_WONG_HALVES = "unlock_wong_halves"
    private const val FALLBACK_UNLOCK_ALL_PRICE = "\$5.99"

    private val fallbackPriceByLevel: Map<Int, String> = mapOf(
        2 to "\$1.99",
        3 to "\$2.99"
    )

    // Keep these IDs aligned with Play Console one-time in-app product IDs.
    val paidSystemProducts: List<PaidSystemUnlockProduct> = listOf(
        PaidSystemUnlockProduct(
            systemId = CountingSystemId.HI_OPT_II,
            productId = UNLOCK_HI_OPT_II,
            level = 2
        ),
        PaidSystemUnlockProduct(
            systemId = CountingSystemId.OMEGA_II,
            productId = UNLOCK_OMEGA_II,
            level = 2
        ),
        PaidSystemUnlockProduct(
            systemId = CountingSystemId.ZEN_COUNT,
            productId = UNLOCK_ZEN_COUNT,
            level = 2
        ),
        PaidSystemUnlockProduct(
            systemId = CountingSystemId.WONG_HALVES,
            productId = UNLOCK_WONG_HALVES,
            level = 3
        )
    )

    private val systemIdByProductId: Map<String, String> = paidSystemProducts.associate {
        it.productId to it.systemId
    }
    private val productIdBySystemId: Map<String, String> = paidSystemProducts.associate {
        it.systemId to it.productId
    }
    val paidSystemIds: Set<String> = paidSystemProducts.map { it.systemId }.toSet()

    fun isFreeLevel(level: Int): Boolean = level <= FREE_MAX_LEVEL

    fun fallbackPriceForLevel(level: Int): String? = fallbackPriceByLevel[level]

    fun fallbackUnlockAllPrice(): String = FALLBACK_UNLOCK_ALL_PRICE

    fun unlockAllProductId(): String = UNLOCK_ALL

    fun catalogProductIds(): Set<String> = productIdBySystemId.values.toSet() + UNLOCK_ALL

    fun productIdForSystem(systemId: String): String? = productIdBySystemId[systemId]

    fun systemIdForProduct(productId: String): String? = systemIdByProductId[productId]

    fun isUnlockAllProduct(productId: String): Boolean = productId == UNLOCK_ALL

    fun purchasedSystemIdsForProducts(productIds: Collection<String>): Set<String> {
        if (UNLOCK_ALL in productIds) {
            return paidSystemIds
        }
        return productIds.mapNotNull { productId -> systemIdByProductId[productId] }.toSet()
    }
}
