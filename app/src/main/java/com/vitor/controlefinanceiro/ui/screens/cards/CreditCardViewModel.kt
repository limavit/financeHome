package com.vitor.controlefinanceiro.ui.screens.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.data.local.entity.CreditCardEntity
import com.vitor.controlefinanceiro.data.local.entity.ExpenseEntity
import com.vitor.controlefinanceiro.data.repository.CreditCardRepository
import com.vitor.controlefinanceiro.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class CardFormState(
    val id: String? = null,
    val nickname: String = "",
    val institution: String = "",
    val limit: String = "",
    val closingDay: String = "10",
    val dueDay: String = "20"
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CreditCardViewModel(
    private val repository: CreditCardRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    val cards = repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val selectedInvoice = MutableStateFlow(DateUtils.today().let { Triple("", it.year, it.monthValue) })
    val invoiceExpenses = selectedInvoice.flatMapLatest { (cardId, year, month) ->
        if (cardId.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList()) else expenseRepository.observeInvoice(cardId, year, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<ExpenseEntity>())
    val message = MutableStateFlow<String?>(null)

    fun selectInvoice(cardId: String, year: Int, month: Int) { selectedInvoice.value = Triple(cardId, year, month) }

    fun changeInvoiceMonth(delta: Int) {
        val (cardId, year, month) = selectedInvoice.value
        if (cardId.isBlank()) return
        val newMonth = month + delta
        val (y, m) = when {
            newMonth < 1 -> (year - 1) to 12
            newMonth > 12 -> (year + 1) to 1
            else -> year to newMonth
        }
        selectedInvoice.value = Triple(cardId, y, m)
    }

    fun save(form: CardFormState, limitCents: Long?) = viewModelScope.launch {
        runCatching {
            val now = DateUtils.nowMillis()
            val isEdit = !form.id.isNullOrBlank()
            val existing = if (isEdit) repository.getById(form.id!!) else null
            val id = form.id ?: UUID.randomUUID().toString()
            repository.save(
                CreditCardEntity(
                    id = id,
                    nickname = form.nickname.trim(),
                    institution = form.institution.takeIf { it.isNotBlank() },
                    limitCents = limitCents,
                    closingDay = form.closingDay.toIntOrNull() ?: 0,
                    dueDay = form.dueDay.toIntOrNull() ?: 0,
                    active = existing?.active ?: true,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
            message.value = if (isEdit) "Cartao atualizado." else "Cartao salvo."
        }.onFailure { message.value = it.message ?: "Nao foi possivel salvar." }
    }

    fun toFormState(card: CreditCardEntity): CardFormState = CardFormState(
        id = card.id,
        nickname = card.nickname,
        institution = card.institution.orEmpty(),
        limit = card.limitCents?.let { (it / 100.0).toString().replace('.', ',') }.orEmpty(),
        closingDay = card.closingDay.toString(),
        dueDay = card.dueDay.toString()
    )

    fun setActive(card: CreditCardEntity, active: Boolean) = viewModelScope.launch {
        repository.save(card.copy(active = active, updatedAt = DateUtils.nowMillis()))
    }
    fun consumeMessage() { message.value = null }
}
