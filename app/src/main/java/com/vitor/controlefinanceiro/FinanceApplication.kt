package com.vitor.controlefinanceiro

import android.app.Application
import androidx.room.Room
import com.vitor.controlefinanceiro.data.backup.BackupRepository
import com.vitor.controlefinanceiro.data.local.AppDatabase
import com.vitor.controlefinanceiro.data.preferences.AppPreferencesRepository
import com.vitor.controlefinanceiro.data.repository.CategoryRepository
import com.vitor.controlefinanceiro.data.repository.CreditCardRepository
import com.vitor.controlefinanceiro.data.repository.DashboardRepository
import com.vitor.controlefinanceiro.data.repository.ExpenseRepository
import com.vitor.controlefinanceiro.data.repository.IncomeRepository
import com.vitor.controlefinanceiro.data.repository.RecurringExpenseRepository
import com.vitor.controlefinanceiro.domain.usecase.CalculateCreditCardInvoiceUseCase
import com.vitor.controlefinanceiro.domain.usecase.ExportBackupUseCase
import com.vitor.controlefinanceiro.domain.usecase.GenerateRecurringExpensesUseCase
import com.vitor.controlefinanceiro.domain.usecase.ImportBackupUseCase
import com.vitor.controlefinanceiro.ui.screens.backup.BackupViewModel
import com.vitor.controlefinanceiro.ui.screens.cards.CreditCardViewModel
import com.vitor.controlefinanceiro.ui.screens.categories.CategoryViewModel
import com.vitor.controlefinanceiro.ui.screens.dashboard.DashboardViewModel
import com.vitor.controlefinanceiro.ui.screens.expenses.ExpenseViewModel
import com.vitor.controlefinanceiro.ui.screens.incomes.IncomeViewModel
import com.vitor.controlefinanceiro.ui.screens.recurring.RecurringViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class FinanceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@FinanceApplication)
            modules(appModule)
        }
    }
}

val appModule = module {
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "controle-financeiro.db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().incomeDao() }
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().creditCardDao() }
    single { get<AppDatabase>().recurringExpenseDao() }
    single { get<AppDatabase>().metadataDao() }
    single { CalculateCreditCardInvoiceUseCase() }
    single { CategoryRepository(get(), get()) }
    single { IncomeRepository(get()) }
    single { CreditCardRepository(get()) }
    single { ExpenseRepository(get(), get(), get()) }
    single { RecurringExpenseRepository(get()) }
    single { DashboardRepository(get(), get()) }
    single { BackupRepository(get(), get()) }
    single { AppPreferencesRepository(get()) }
    single { GenerateRecurringExpensesUseCase(get(), get(), get(), get()) }
    single { ExportBackupUseCase(get()) }
    single { ImportBackupUseCase(get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { IncomeViewModel(get()) }
    viewModel { ExpenseViewModel(get(), get(), get(), get()) }
    viewModel { CreditCardViewModel(get(), get()) }
    viewModel { CategoryViewModel(get()) }
    viewModel { BackupViewModel(get(), get(), get()) }
    viewModel { RecurringViewModel(get(), get(), get()) }
}
