package com.vectorlogic.blackjackcounter.data.counting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CountingSystemsCatalogTest {

    @Test
    fun `catalog ids are unique`() {
        val ids = CountingSystemsCatalog.allSystems.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `getOrDefault returns hi lo for unknown ids`() {
        val resolved = CountingSystemsCatalog.getOrDefault("unknown_system")
        assertSame(CountingSystemsCatalog.defaultSystem, resolved)
    }
}
