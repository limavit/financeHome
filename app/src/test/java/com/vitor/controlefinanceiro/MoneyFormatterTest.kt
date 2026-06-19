package com.vitor.controlefinanceiro

import com.vitor.controlefinanceiro.core.money.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatterTest {
    @Test fun parseBrlToCents() {
        assertEquals(1050L, MoneyFormatter.parseBrlToCents("10,50"))
        assertEquals(100000L, MoneyFormatter.parseBrlToCents("1.000,00"))
        assertEquals(100000L, MoneyFormatter.parseBrlToCents("1000"))
        assertNull(MoneyFormatter.parseBrlToCents("-1"))
    }

    @Test fun formatCentsToBrl() {
        assertEquals("R$ 10,50", MoneyFormatter.formatCentsToBrl(1050))
    }
}
