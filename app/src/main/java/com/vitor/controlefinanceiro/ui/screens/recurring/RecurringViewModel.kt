package com.vitor.controlefinanceiro.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.date.DateUtils.toMillis
import com.vitor.controlefinanceiro.data.local.entity.CategoryEntity
import com.vitor.controlefinanceiro.data.local.entity.CreditCardEntity
import com.vitor.controlefinanceiro.data.local.entity.RecurringExpenseEntity
import com.vitor.controlefinanceiro.data.repository.CategoryRepository
import com.vitor.controlefinanceiro.data.repository.CreditCardRepository
import com.vitor.controlefinanceiro.data.repository.RecurringExpenseRepository
import com.vitor.controlefinanceiro.domain.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class RecurringFormState(
    val id: String? = null,
    val name: String = "",
    val amount: String = "",
    val categoryId: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.PIX,
    val creditCardId: String = "",
    val launchDay: String = DateUtils.today().dayOfMonth.toString(),
    val startDateInput: String = DateUtils.formatBrDate(DateUtils.today()),
    val endDateInput: String = "",
    val notes: String = ""
)

class RecurringViewModel(
    private val repository: RecurringExpenseRepository,
    categoryRepository: CategoryRepository,
    cardRepository: CreditCardRepository
) : ViewModel() {
    val recurring = repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<RecurringExpenseEntity>())
    val categories = categoryRepository.observeExpenseCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<CategoryEntity>())
    val cards = cardRepository.observeActive().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<CreditCardEntity>())
    val message = MutableStateFlow<String?>(null)

    fun save(form: RecurringFormState, amountCents: Long?) = viewModelScope.launch {
        runCatching {
            require(form.name.isNotBlank()) { "Informe o nome da recorrencia." }
            require(amountCents != null && amountCents > 0) { "Informe um valor valido." }
            require(form.categoryId.isNotBlank()) { "Selecione uma categoria." }
            if (form.paymentMethod == PaymentMethod.CARTAO_CREDITO) require(form.creditCardId.isNotBlank()) { "Selecione um cartao de credito." }
            val launchDay = form.launchDay.toIntOrNull()?.takeIf { it in 1..31 } ?: throw IllegalArgumentException("Dia de lancamento invalido (1-31).")
            val startDate = DateUtils.parseBrDateOrNull(form.startDateInput) ?: throw IllegalArgumentException("Data de inicio invalida. Use dd/MM/yyyy.")
            val endDate = if (form.endDateInput.isBlank()) null else DateUtils.parseBrDateOrNull(form.endDateInput) ?: throw IllegalArgumentException("Data final invalida. Use dd/MM/yyyy.")
            val now = DateUtils.nowMillis()
            val isEdit = !form.id.isNullOrBlank()
            val existing = if (isEdit) repository.getAll().firstOrNull { it.id == form.id } else null
            val id = form.id ?: UUID.randomUUID().toString()
            repository.save(
                RecurringExpenseEntity(
                    id = id,
                    name = form.name.trim(),
                    amountCents = amountCents,
                    categoryId = form.categoryId,
                    paymentMethod = form.paymentMethod,
                    creditCardId = form.creditCardId.takeIf { form.paymentMethod == PaymentMethod.CARTAO_CREDITO },
                    launchDay = launchDay,
                    startDate = startDate.toMillis(),
                    endDate = endDate?.toMillis(),
                    active = existing?.active ?: true,
                    notes = form.notes.takeIf { it.isNotBlank() },
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
            message.value = if (isEdit) "Recorrencia atualizada." else "Recorrencia criada."
        }.onFailure { message.value = it.message ?: "Nao foi possivel salvar." }
    }

    fun toFormState(recurring: RecurringExpenseEntity): RecurringFormState = RecurringFormState(
        id = recurring.id,
        name = recurring.name,
        amount = (recurring.amountCents / 100.0).toString().replace('.', ','),
        categoryId = recurring.categoryId,
        paymentMethod = recurring.paymentMethod,
        creditCardId = recurring.creditCardId.orEmpty(),
        launchDay = recurring.launchDay.toString(),
        startDateInput = DateUtils.formatBrDate(DateUtils.millisToDate(recurring.startDate)),
        endDateInput = recurring.endDate?.let { DateUtils.formatBrDate(DateUtils.millisToDate(it)) }.orEmpty(),
        notes = recurring.notes.orEmpty()
    )

    fun toggleActive(recurring: RecurringExpenseEntity) = viewModelScope.launch {
        repository.setActive(recurring, !recurring.active)
    }

    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
    fun consumeMessage() { message.value = null }
}
