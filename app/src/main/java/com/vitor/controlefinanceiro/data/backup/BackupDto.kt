package com.vitor.controlefinanceiro.data.backup

import com.vitor.controlefinanceiro.data.local.entity.AppMetadataEntity
import com.vitor.controlefinanceiro.data.local.entity.CategoryEntity
import com.vitor.controlefinanceiro.data.local.entity.CreditCardEntity
import com.vitor.controlefinanceiro.data.local.entity.ExpenseEntity
import com.vitor.controlefinanceiro.data.local.entity.IncomeEntity
import com.vitor.controlefinanceiro.data.local.entity.RecurringExpenseEntity
import com.vitor.controlefinanceiro.domain.model.CategoryType
import com.vitor.controlefinanceiro.domain.model.ExpenseStatus
import com.vitor.controlefinanceiro.domain.model.IncomeType
import com.vitor.controlefinanceiro.domain.model.PaymentMethod
import kotlinx.serialization.Serializable

@Serializable
data class BackupDto(
    val schemaVersion: Int = 1,
    val exportedAt: Long,
    val appName: String = "Controle Financeiro Local",
    val categories: List<CategoryBackupDto>,
    val incomes: List<IncomeBackupDto>,
    val expenses: List<ExpenseBackupDto>,
    val creditCards: List<CreditCardBackupDto>,
    val recurringExpenses: List<RecurringExpenseBackupDto>,
    val metadata: Map<String, String>
)

@Serializable
data class CategoryBackupDto(val id: String, val name: String, val type: CategoryType, val isSystem: Boolean, val active: Boolean, val createdAt: Long, val updatedAt: Long)
@Serializable
data class IncomeBackupDto(val id: String, val description: String, val amountCents: Long, val type: IncomeType, val date: Long, val notes: String?, val createdAt: Long, val updatedAt: Long)
@Serializable
data class ExpenseBackupDto(val id: String, val name: String, val description: String?, val amountCents: Long, val purchaseDate: Long, val dueDate: Long?, val paymentDate: Long?, val categoryId: String, val paymentMethod: PaymentMethod, val status: ExpenseStatus, val creditCardId: String?, val recurring: Boolean, val recurringExpenseId: String?, val recurringYear: Int?, val recurringMonth: Int?, val invoiceYear: Int?, val invoiceMonth: Int?, val notes: String?, val createdAt: Long, val updatedAt: Long)
@Serializable
data class CreditCardBackupDto(val id: String, val nickname: String, val institution: String?, val limitCents: Long?, val closingDay: Int, val dueDay: Int, val active: Boolean, val createdAt: Long, val updatedAt: Long)
@Serializable
data class RecurringExpenseBackupDto(val id: String, val name: String, val amountCents: Long, val categoryId: String, val paymentMethod: PaymentMethod, val creditCardId: String?, val launchDay: Int, val startDate: Long, val endDate: Long?, val active: Boolean, val notes: String?, val createdAt: Long, val updatedAt: Long)

fun CategoryEntity.toBackup() = CategoryBackupDto(id, name, type, isSystem, active, createdAt, updatedAt)
fun IncomeEntity.toBackup() = IncomeBackupDto(id, description, amountCents, type, date, notes, createdAt, updatedAt)
fun ExpenseEntity.toBackup() = ExpenseBackupDto(id, name, description, amountCents, purchaseDate, dueDate, paymentDate, categoryId, paymentMethod, status, creditCardId, recurring, recurringExpenseId, recurringYear, recurringMonth, invoiceYear, invoiceMonth, notes, createdAt, updatedAt)
fun CreditCardEntity.toBackup() = CreditCardBackupDto(id, nickname, institution, limitCents, closingDay, dueDay, active, createdAt, updatedAt)
fun RecurringExpenseEntity.toBackup() = RecurringExpenseBackupDto(id, name, amountCents, categoryId, paymentMethod, creditCardId, launchDay, startDate, endDate, active, notes, createdAt, updatedAt)

fun CategoryBackupDto.toEntity() = CategoryEntity(id, name, type, isSystem, active, createdAt, updatedAt)
fun IncomeBackupDto.toEntity() = IncomeEntity(id, description, amountCents, type, date, notes, createdAt, updatedAt)
fun ExpenseBackupDto.toEntity() = ExpenseEntity(id, name, description, amountCents, purchaseDate, dueDate, paymentDate, categoryId, paymentMethod, status, creditCardId, recurring, recurringExpenseId, recurringYear, recurringMonth, invoiceYear, invoiceMonth, notes, createdAt, updatedAt)
fun CreditCardBackupDto.toEntity() = CreditCardEntity(id, nickname, institution, limitCents, closingDay, dueDay, active, createdAt, updatedAt)
fun RecurringExpenseBackupDto.toEntity() = RecurringExpenseEntity(id, name, amountCents, categoryId, paymentMethod, creditCardId, launchDay, startDate, endDate, active, notes, createdAt, updatedAt)
fun AppMetadataEntity.toPair(): Pair<String, String> = key to value
