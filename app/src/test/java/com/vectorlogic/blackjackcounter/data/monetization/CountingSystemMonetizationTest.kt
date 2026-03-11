package com.vectorlogic.blackjackcounter.data.monetization

import com.vectorlogic.blackjackcounter.data.counting.CountingSystemsCatalog
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
}
