package com.vitor.controlefinanceiro.ui.screens.incomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.date.DateUtils.toMillis
import com.vitor.controlefinanceiro.data.local.entity.IncomeEntity
import com.vitor.controlefinanceiro.data.repository.IncomeRepository
import com.vitor.controlefinanceiro.domain.model.IncomeType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class IncomeFormState(
    val description: String = "",
    val amount: String = "",
    val type: IncomeType = IncomeType.SALARIO,
    val notes: String = ""
)

class IncomeViewModel(private val repository: IncomeRepository) : ViewModel() {
    val incomes = repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val message = MutableStateFlow<String?>(null)

    fun save(form: IncomeFormState, amountCents: Long?) = viewModelScope.launch {
        runCatching {
            require(amountCents != null && amountCents > 0) { "Informe um valor valido." }
            val now = DateUtils.nowMillis()
            repository.save(
                IncomeEntity(
                    id = UUID.randomUUID().toString(),
                    description = form.description.trim(),
                    amountCents = amountCents,
                    type = form.type,
                    date = DateUtils.today().toMillis(),
                    notes = form.notes.takeIf { it.isNotBlank() },
                    createdAt = now,
                    updatedAt = now
                )
            )
            message.value = "Entrada salva."
        }.onFailure { message.value = it.message ?: "Nao foi possivel salvar." }
    }

    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
    fun consumeMessage() { message.value = null }
}
