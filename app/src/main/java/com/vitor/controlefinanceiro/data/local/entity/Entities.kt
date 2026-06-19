package com.vitor.controlefinanceiro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vitor.controlefinanceiro.domain.model.CategoryType
import com.vitor.controlefinanceiro.domain.model.ExpenseStatus
import com.vitor.controlefinanceiro.domain.model.IncomeType
import com.vitor.controlefinanceiro.domain.model.PaymentMethod

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: CategoryType,
    val isSystem: Boolean,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey val id: String,
    val description: String,
    val amountCents: Long,
    val type: IncomeType,
    val date: Long,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "credit_cards"
)
data class CreditCardEntity(
    @PrimaryKey val id: String,
    val nickname: String,
    val institution: String?,
    val limitCents: Long?,
    val closingDay: Int,
    val dueDay: Int,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(CategoryEntity::class, ["id"], ["categoryId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(CreditCardEntity::class, ["id"], ["creditCardId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [
        Index("categoryId"),
        Index("creditCardId"),
        Index("recurringExpenseId"),
        Index("recurringYear", "recurringMonth"),
        Index("invoiceYear", "invoiceMonth")
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val amountCents: Long,
    val purchaseDate: Long,
    val dueDate: Long?,
    val paymentDate: Long?,
    val categoryId: String,
    val paymentMethod: PaymentMethod,
    val status: ExpenseStatus,
    val creditCardId: String?,
    val recurring: Boolean,
    val recurringExpenseId: String?,
    val recurringYear: Int?,
    val recurringMonth: Int?,
    val invoiceYear: Int?,
    val invoiceMonth: Int?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "recurring_expenses",
    foreignKeys = [
        ForeignKey(CategoryEntity::class, ["id"], ["categoryId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(CreditCardEntity::class, ["id"], ["creditCardId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("categoryId"), Index("creditCardId")]
)
data class RecurringExpenseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amountCents: Long,
    val categoryId: String,
    val paymentMethod: PaymentMethod,
    val creditCardId: String?,
    val launchDay: Int,
    val startDate: Long,
    val endDate: Long?,
    val active: Boolean,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String
)
