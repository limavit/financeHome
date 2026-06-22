package com.vitor.controlefinanceiro

import com.vitor.controlefinanceiro.core.date.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {
    @Test fun parseValidDates() {
        val result = DateUtils.parseBrDateOrNull("15/06/2026")
        assertEquals(LocalDate.of(2026, 6, 15), result)
    }

    @Test fun parseInvalidDates() {
        assertNull(DateUtils.parseBrDateOrNull(""))
        assertNull(DateUtils.parseBrDateOrNull("   "))
        assertNull(DateUtils.parseBrDateOrNull("31/02/2026"))
        assertNull(DateUtils.parseBrDateOrNull("2026-06-15"))
        assertNull(DateUtils.parseBrDateOrNull("15-06-2026"))
        assertNull(DateUtils.parseBrDateOrNull("abc"))
    }

    @Test fun formatBrDate() {
        val date = LocalDate.of(2026, 6, 15)
        assertEquals("15/06/2026", DateUtils.formatBrDate(date))
    }

    @Test fun roundTrip() {
        val original = LocalDate.of(2026, 12, 31)
        val parsed = DateUtils.parseBrDateOrNull(DateUtils.formatBrDate(original))
        assertNotNull(parsed)
        assertEquals(original, parsed)
    }
}
