package com.vitor.controlefinanceiro.ui.screens.incomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.date.DateUtils.toMillis
import com.vitor.controlefinanceiro.data.local.entity.IncomeEntity
import com.vitor.controlefinanceiro.data.repository.IncomeRepository
import com.vitor.controlefinanceiro.domain.model.IncomeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class IncomeFormState(
    val id: String? = null,
    val description: String = "",
    val amount: String = "",
    val type: IncomeType = IncomeType.SALARIO,
    val dateInput: String = DateUtils.formatBrDate(DateUtils.today()),
    val notes: String = ""
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class IncomeViewModel(private val repository: IncomeRepository) : ViewModel() {
    private val selectedPeriod = MutableStateFlow(DateUtils.today().let { it.year to it.monthValue })
    val incomes = selectedPeriod
        .flatMapLatest { (year, month) -> repository.observeMonth(year, month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val message = MutableStateFlow<String?>(null)

    val selectedYear: Int get() = selectedPeriod.value.first
    val selectedMonth: Int get() = selectedPeriod.value.second

    fun previousMonth() {
        val (year, month) = selectedPeriod.value
        selectedPeriod.value = if (month == 1) year - 1 to 12 else year to month - 1
    }

    fun nextMonth() {
        val (year, month) = selectedPeriod.value
        selectedPeriod.value = if (month == 12) year + 1 to 1 else year to month + 1
    }

    fun save(form: IncomeFormState, amountCents: Long?) = viewModelScope.launch {
        runCatching {
            require(amountCents != null && amountCents > 0) { "Informe um valor valido." }
            require(form.description.isNotBlank()) { "Informe a descricao." }
            val date = DateUtils.parseBrDateOrNull(form.dateInput) ?: throw IllegalArgumentException("Data invalida. Use dd/MM/yyyy.")
            val now = DateUtils.nowMillis()
            val isEdit = !form.id.isNullOrBlank()
            val existing = if (isEdit) repository.getAll().firstOrNull { it.id == form.id } else null
            val id = form.id ?: UUID.randomUUID().toString()
            repository.save(
                IncomeEntity(
                    id = id,
                    description = form.description.trim(),
                    amountCents = amountCents,
                    type = form.type,
                    date = date.toMillis(),
                    notes = form.notes.takeIf { it.isNotBlank() },
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
            message.value = if (isEdit) "Entrada atualizada." else "Entrada salva."
        }.onFailure { message.value = it.message ?: "Nao foi possivel salvar." }
    }

    fun toFormState(income: IncomeEntity): IncomeFormState = IncomeFormState(
        id = income.id,
        description = income.description,
        amount = (income.amountCents / 100.0).toString().replace('.', ','),
        type = income.type,
        dateInput = DateUtils.formatBrDate(DateUtils.millisToDate(income.date)),
        notes = income.notes.orEmpty()
    )

    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
    fun consumeMessage() { message.value = null }
}
