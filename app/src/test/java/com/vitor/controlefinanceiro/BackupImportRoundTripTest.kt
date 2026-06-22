package com.vitor.controlefinanceiro

import com.vitor.controlefinanceiro.core.json.JsonConfig
import com.vitor.controlefinanceiro.data.backup.BackupDto
import com.vitor.controlefinanceiro.data.backup.BackupValidator
import com.vitor.controlefinanceiro.domain.model.CategoryType
import com.vitor.controlefinanceiro.domain.model.ExpenseStatus
import com.vitor.controlefinanceiro.domain.model.IncomeType
import com.vitor.controlefinanceiro.domain.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupImportRoundTripTest {
    @Test fun exportParseImportRoundTripPreservesData() {
        val now = 1_700_000_000_000L
        val original = BackupDto(
            schemaVersion = 1,
            exportedAt = now,
            categories = listOf(com.vitor.controlefinanceiro.data.backup.CategoryBackupDto("cat-1", "Outros", CategoryType.GASTO, true, true, now, now)),
            incomes = listOf(com.vitor.controlefinanceiro.data.backup.IncomeBackupDto("inc-1", "Salario", 500000, IncomeType.SALARIO, now, null, now, now)),
            expenses = listOf(
                com.vitor.controlefinanceiro.data.backup.ExpenseBackupDto(
                    "exp-1", "Mercado", null, 20000, now, now, null, "cat-1", PaymentMethod.PIX,
                    ExpenseStatus.PAGO, null, false, null, null, null, null, null, null, now, now
                )
            ),
            creditCards = listOf(com.vitor.controlefinanceiro.data.backup.CreditCardBackupDto("card-1", "Cartao X", "Banco", 100000, 10, 20, true, now, now)),
            recurringExpenses = listOf(
                com.vitor.controlefinanceiro.data.backup.RecurringExpenseBackupDto("rec-1", "Aluguel", 150000, "cat-1", PaymentMethod.PIX, null, 5, now, null, true, null, now, now)
            ),
            metadata = mapOf("first_run_completed" to "true")
        )
        val json = JsonConfig.json.encodeToString(BackupDto.serializer(), original)
        assertTrue(json.contains("\"schemaVersion\""))
        assertTrue(json.contains("\"exportedAt\""))

        val parsed = JsonConfig.json.decodeFromString(BackupDto.serializer(), json)
        assertEquals(original, parsed)
        assertEquals(null, BackupValidator.validate(parsed))

        val category = parsed.categories.first()
        val income = parsed.incomes.first()
        val expense = parsed.expenses.first()
        val card = parsed.creditCards.first()
        val recurring = parsed.recurringExpenses.first()
        assertNotNull(category)
        assertEquals("Outros", category.name)
        assertEquals(500000L, income.amountCents)
        assertEquals("Mercado", expense.name)
        assertEquals(10, card.closingDay)
        assertEquals(5, recurring.launchDay)
    }
}
