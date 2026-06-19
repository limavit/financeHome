package com.vitor.controlefinanceiro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vitor.controlefinanceiro.data.local.converters.EnumConverters
import com.vitor.controlefinanceiro.data.local.dao.CategoryDao
import com.vitor.controlefinanceiro.data.local.dao.CreditCardDao
import com.vitor.controlefinanceiro.data.local.dao.ExpenseDao
import com.vitor.controlefinanceiro.data.local.dao.IncomeDao
import com.vitor.controlefinanceiro.data.local.dao.MetadataDao
import com.vitor.controlefinanceiro.data.local.dao.RecurringExpenseDao
import com.vitor.controlefinanceiro.data.local.entity.AppMetadataEntity
import com.vitor.controlefinanceiro.data.local.entity.CategoryEntity
import com.vitor.controlefinanceiro.data.local.entity.CreditCardEntity
import com.vitor.controlefinanceiro.data.local.entity.ExpenseEntity
import com.vitor.controlefinanceiro.data.local.entity.IncomeEntity
import com.vitor.controlefinanceiro.data.local.entity.RecurringExpenseEntity

@Database(
    entities = [
        CategoryEntity::class,
        IncomeEntity::class,
        ExpenseEntity::class,
        CreditCardEntity::class,
        RecurringExpenseEntity::class,
        AppMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun metadataDao(): MetadataDao
}
