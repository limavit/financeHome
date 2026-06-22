package com.vitor.controlefinanceiro.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.data.repository.CategoryRepository
import com.vitor.controlefinanceiro.data.repository.DashboardRepository
import com.vitor.controlefinanceiro.data.repository.DashboardSummary
import com.vitor.controlefinanceiro.data.repository.RecentTransaction
import com.vitor.controlefinanceiro.domain.usecase.GenerateRecurringExpensesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val year: Int = DateUtils.today().year,
    val month: Int = DateUtils.today().monthValue,
    val summary: DashboardSummary = DashboardSummary(),
    val recent: List<RecentTransaction> = emptyList()
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val categoryRepository: CategoryRepository,
    private val generateRecurringExpenses: GenerateRecurringExpensesUseCase
) : ViewModel() {
    private val selected = MutableStateFlow(DateUtils.today().let { it.year to it.monthValue })

    val uiState = selected
        .flatMapLatest { (year, month) ->
            dashboardRepository.observeSummary(year, month)
                .combine(dashboardRepository.observeRecent()) { summary, recent ->
                    DashboardUiState(year, month, summary, recent)
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    init {
        viewModelScope.launch {
            categoryRepository.ensureDefaults()
            generateRecurringExpenses()
        }
    }

    fun previousMonth() {
        val (year, month) = selected.value
        selected.value = if (month == 1) year - 1 to 12 else year to month - 1
    }

    fun nextMonth() {
        val (year, month) = selected.value
        selected.value = if (month == 12) year + 1 to 1 else year to month + 1
    }
}
