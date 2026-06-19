package com.vitor.controlefinanceiro.domain.usecase

import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.date.DateUtils.toMillis
import com.vitor.controlefinanceiro.data.local.entity.ExpenseEntity
import com.vitor.controlefinanceiro.data.repository.CreditCardRepository
import com.vitor.controlefinanceiro.data.repository.ExpenseRepository
import com.vitor.controlefinanceiro.data.repository.RecurringExpenseRepository
import com.vitor.controlefinanceiro.domain.model.ExpenseStatus
import com.vitor.controlefinanceiro.domain.model.PaymentMethod
import java.time.YearMonth
import java.util.UUID

class GenerateRecurringExpensesUseCase(
    private val recurringRepository: RecurringExpenseRepository,
    private val expenseRepository: ExpenseRepository,
    private val creditCardRepository: CreditCardRepository,
    private val invoiceUseCase: CalculateCreditCardInvoiceUseCase
) {
    suspend operator fun invoke() {
        val today = DateUtils.today()
        val currentMonth = YearMonth.from(today)
        val generated = mutableListOf<ExpenseEntity>()
        recurringRepository.getActive().forEach { recurring ->
            var cursor = YearMonth.from(DateUtils.millisToDate(recurring.startDate))
            val end = recurring.endDate?.let { YearMonth.from(DateUtils.millisToDate(it)) } ?: currentMonth
            val last = if (end < currentMonth) end else currentMonth
            while (!cursor.isAfter(last)) {
                if (expenseRepository.recurringCount(recurring.id, cursor.year, cursor.monthValue) == 0) {
                    val purchaseDate = DateUtils.safeDate(cursor.year, cursor.monthValue, recurring.launchDay)
                    val now = DateUtils.nowMillis()
                    var due = purchaseDate.toMillis()
                    var invoiceYear: Int? = null
                    var invoiceMonth: Int? = null
                    if (recurring.paymentMethod == PaymentMethod.CARTAO_CREDITO && recurring.creditCardId != null) {
                        val card = creditCardRepository.getById(recurring.creditCardId)
                        if (card != null) {
                            val invoice = invoiceUseCase(purchaseDate, card.closingDay, card.dueDay)
                            due = invoice.dueDate.toMillis()
                            invoiceYear = invoice.invoiceYear
                            invoiceMonth = invoice.invoiceMonth
                        }
                    }
                    generated += ExpenseEntity(
                        id = UUID.randomUUID().toString(),
                        name = recurring.name,
                        description = null,
                        amountCents = recurring.amountCents,
                        purchaseDate = purchaseDate.toMillis(),
                        dueDate = due,
                        paymentDate = null,
                        categoryId = recurring.categoryId,
                        paymentMethod = recurring.paymentMethod,
                        status = ExpenseStatus.ABERTO,
                        creditCardId = recurring.creditCardId.takeIf { recurring.paymentMethod == PaymentMethod.CARTAO_CREDITO },
                        recurring = true,
                        recurringExpenseId = recurring.id,
                        recurringYear = cursor.year,
                        recurringMonth = cursor.monthValue,
                        invoiceYear = invoiceYear,
                        invoiceMonth = invoiceMonth,
                        notes = recurring.notes,
                        createdAt = now,
                        updatedAt = now
                    )
                }
                cursor = cursor.plusMonths(1)
            }
        }
        expenseRepository.insertGenerated(generated)
    }
}
