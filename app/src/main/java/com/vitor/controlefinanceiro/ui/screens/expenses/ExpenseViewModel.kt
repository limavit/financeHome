package com.vitor.controlefinanceiro.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.date.DateUtils.toMillis
import com.vitor.controlefinanceiro.data.local.entity.CategoryEntity
import com.vitor.controlefinanceiro.data.local.entity.CreditCardEntity
import com.vitor.controlefinanceiro.data.local.entity.ExpenseEntity
import com.vitor.controlefinanceiro.data.local.entity.RecurringExpenseEntity
import com.vitor.controlefinanceiro.data.repository.CategoryRepository
import com.vitor.controlefinanceiro.data.repository.CreditCardRepository
import com.vitor.controlefinanceiro.data.repository.ExpenseRepository
import com.vitor.controlefinanceiro.data.repository.RecurringExpenseRepository
import com.vitor.controlefinanceiro.domain.model.ExpenseStatus
import com.vitor.controlefinanceiro.domain.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ExpenseFormState(
    val name: String = "",
    val description: String = "",
    val amount: String = "",
    val categoryId: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.PIX,
    val creditCardId: String = "",
    val status: ExpenseStatus = ExpenseStatus.ABERTO,
    val recurring: Boolean = false,
    val notes: String = ""
)

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    cardRepository: CreditCardRepository,
    private val recurringRepository: RecurringExpenseRepository
) : ViewModel() {
    val expenses = repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = categoryRepository.observeExpenseCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<CategoryEntity>())
    val cards = cardRepository.observeActive().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<CreditCardEntity>())
    val recurring = recurringRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<RecurringExpenseEntity>())
    val message = MutableStateFlow<String?>(null)

    fun save(form: ExpenseFormState, amountCents: Long?) = viewModelScope.launch {
        runCatching {
            require(amountCents != null && amountCents > 0) { "Informe um valor valido." }
            require(form.categoryId.isNotBlank()) { "Selecione uma categoria." }
            if (form.paymentMethod == PaymentMethod.CARTAO_CREDITO) require(form.creditCardId.isNotBlank()) { "Selecione um cartao de credito." }
            val now = DateUtils.nowMillis()
            val id = UUID.randomUUID().toString()
            if (form.recurring) {
                recurringRepository.save(
                    RecurringExpenseEntity(
                        id = UUID.randomUUID().toString(),
                        name = form.name.trim(),
                        amountCents = amountCents,
                        categoryId = form.categoryId,
                        paymentMethod = form.paymentMethod,
                        creditCardId = form.creditCardId.takeIf { form.paymentMethod == PaymentMethod.CARTAO_CREDITO },
                        launchDay = DateUtils.today().dayOfMonth,
                        startDate = DateUtils.today().toMillis(),
                        endDate = null,
                        active = true,
                        notes = form.notes.takeIf { it.isNotBlank() },
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
            repository.save(
                ExpenseEntity(
                    id = id,
                    name = form.name.trim(),
                    description = form.description.takeIf { it.isNotBlank() },
                    amountCents = amountCents,
                    purchaseDate = DateUtils.today().toMillis(),
                    dueDate = DateUtils.today().toMillis(),
                    paymentDate = if (form.status == ExpenseStatus.PAGO) now else null,
                    categoryId = form.categoryId,
                    paymentMethod = form.paymentMethod,
                    status = form.status,
                    creditCardId = form.creditCardId.takeIf { form.paymentMethod == PaymentMethod.CARTAO_CREDITO },
                    recurring = form.recurring,
                    recurringExpenseId = null,
                    recurringYear = null,
                    recurringMonth = null,
                    invoiceYear = null,
                    invoiceMonth = null,
                    notes = form.notes.takeIf { it.isNotBlank() },
                    createdAt = now,
                    updatedAt = now
                )
            )
            message.value = "Gasto salvo."
        }.onFailure { message.value = it.message ?: "Nao foi possivel salvar." }
    }

    fun markPaid(expense: ExpenseEntity) = viewModelScope.launch { repository.markPaid(expense) }
    fun cancel(expense: ExpenseEntity) = viewModelScope.launch { repository.save(expense.copy(status = ExpenseStatus.CANCELADO, updatedAt = DateUtils.nowMillis())) }
    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
    fun consumeMessage() { message.value = null }
}
