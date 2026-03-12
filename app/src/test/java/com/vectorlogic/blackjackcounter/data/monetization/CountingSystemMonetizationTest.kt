package com.vectorlogic.blackjackcounter.data.monetization

import com.vectorlogic.blackjackcounter.data.counting.CountingSystemsCatalog
import com.vectorlogic.blackjackcounter.domain.model.CountingSystemId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CountingSystemMonetizationTest {

    @Test
    fun `paid products map to known non-free systems`() {
        val systemsById = CountingSystemsCatalog.systemsById

        CountingSystemMonetization.paidSystemProducts.forEach { paidProduct ->
            val system = systemsById[paidProduct.systemId]
            assertTrue(system != null)
            assertFalse(CountingSystemMonetization.isFreeLevel(system!!.level))
        }
    }

    @Test
    fun `unlock all product unlocks every paid system`() {
        val unlocked = CountingSystemMonetization.purchasedSystemIdsForProducts(
            listOf(CountingSystemMonetization.unlockAllProductId())
        )

        assertEquals(CountingSystemMonetization.paidSystemIds, unlocked)
    }

    @Test
    fun `fallback pricing matches new plan`() {
        assertEquals("\$1.99", CountingSystemMonetization.fallbackPriceForLevel(2))
        assertEquals("\$2.99", CountingSystemMonetization.fallbackPriceForLevel(3))
        assertEquals("\$5.99", CountingSystemMonetization.fallbackUnlockAllPrice())
        assertTrue(
            CountingSystemMonetization.catalogProductIds().contains(
                CountingSystemMonetization.unlockAllProductId()
            )
        )
        assertEquals(
            CountingSystemId.WONG_HALVES,
            CountingSystemMonetization.paidSystemProducts.last().systemId
        )
    }
}
