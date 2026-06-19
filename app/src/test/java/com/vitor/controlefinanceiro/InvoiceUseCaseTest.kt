package com.vitor.controlefinanceiro

import com.vitor.controlefinanceiro.domain.usecase.CalculateCreditCardInvoiceUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class InvoiceUseCaseTest {
    private val useCase = CalculateCreditCardInvoiceUseCase()

    @Test fun sameMonthBeforeClosing() {
        val invoice = useCase(LocalDate.of(2026, 6, 5), 10, 20)
        assertEquals(2026, invoice.invoiceYear)
        assertEquals(6, invoice.invoiceMonth)
        assertEquals(LocalDate.of(2026, 6, 20), invoice.dueDate)
    }

    @Test fun nextMonthAfterClosing() {
        val invoice = useCase(LocalDate.of(2026, 6, 11), 10, 20)
        assertEquals(2026, invoice.invoiceYear)
        assertEquals(7, invoice.invoiceMonth)
    }

    @Test fun clampsFebruaryDueDay() {
        val invoice = useCase(LocalDate.of(2026, 2, 5), 10, 31)
        assertEquals(LocalDate.of(2026, 2, 28), invoice.dueDate)
    }

    @Test fun decemberAfterClosingGoesToJanuary() {
        val invoice = useCase(LocalDate.of(2026, 12, 20), 10, 20)
        assertEquals(2027, invoice.invoiceYear)
        assertEquals(1, invoice.invoiceMonth)
    }
}
