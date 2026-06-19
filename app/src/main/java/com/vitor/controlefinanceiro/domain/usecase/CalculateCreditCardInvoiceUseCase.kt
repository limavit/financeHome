package com.vitor.controlefinanceiro.domain.usecase

import com.vitor.controlefinanceiro.core.date.DateUtils
import java.time.LocalDate
import java.time.YearMonth

data class InvoiceInfo(
    val invoiceYear: Int,
    val invoiceMonth: Int,
    val closingDate: LocalDate,
    val dueDate: LocalDate
)

class CalculateCreditCardInvoiceUseCase {
    operator fun invoke(
        purchaseDate: LocalDate,
        closingDay: Int,
        dueDay: Int
    ): InvoiceInfo {
        val invoiceMonth = if (purchaseDate.dayOfMonth <= closingDay) {
            YearMonth.from(purchaseDate)
        } else {
            YearMonth.from(purchaseDate).plusMonths(1)
        }
        val closingDate = DateUtils.safeDate(invoiceMonth.year, invoiceMonth.monthValue, closingDay)
        val dueDate = DateUtils.safeDate(invoiceMonth.year, invoiceMonth.monthValue, dueDay)
        return InvoiceInfo(invoiceMonth.year, invoiceMonth.monthValue, closingDate, dueDate)
    }
}
