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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class ExpenseFormState(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val amount: String = "",
    val purchaseDateInput: String = DateUtils.formatBrDate(DateUtils.today()),
    val dueDateInput: String = "",
    val categoryId: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.PIX,
    val creditCardId: String = "",
    val status: ExpenseStatus = ExpenseStatus.ABERTO,
    val recurring: Boolean = false,
    val notes: String = ""
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ExpenseViewModel(
    private val repository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    cardRepository: CreditCardRepository,
    private val recurringRepository: RecurringExpenseRepository
) : ViewModel() {
    val categories = categoryRepository.observeExpenseCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<CategoryEntity>())
    val cards = cardRepository.observeActive().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<CreditCardEntity>())
    val recurring = recurringRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<RecurringExpenseEntity>())
    val message = MutableStateFlow<String?>(null)

    private val selectedPeriod = MutableStateFlow(DateUtils.today().let { it.year to it.monthValue })
    val expenses = selectedPeriod
        .flatMapLatest { (year, month) -> repository.observeMonth(year, month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun save(form: ExpenseFormState, amountCents: Long?) = viewModelScope.launch {
        runCatching {
            require(amountCents != null && amountCents > 0) { "Informe um valor valido." }
            require(form.name.isNotBlank()) { "Informe o nome do gasto." }
            require(form.categoryId.isNotBlank()) { "Selecione uma categoria." }
            if (form.paymentMethod == PaymentMethod.CARTAO_CREDITO) require(form.creditCardId.isNotBlank()) { "Selecione um cartao de credito." }
            val purchaseDate = DateUtils.parseBrDateOrNull(form.purchaseDateInput)
                ?: throw IllegalArgumentException("Data invalida. Use dd/MM/yyyy.")
            val dueDate = if (form.dueDateInput.isBlank()) null
                else DateUtils.parseBrDateOrNull(form.dueDateInput)
                    ?: throw IllegalArgumentException("Data de vencimento invalida. Use dd/MM/yyyy.")
            val now = DateUtils.nowMillis()
            val isEdit = !form.id.isNullOrBlank()
            val expenseId = form.id ?: UUID.randomUUID().toString()

            var recurringIdForExpense: String? = null
            var recurringYearForExpense: Int? = null
            var recurringMonthForExpense: Int? = null
            var recurringEntity: RecurringExpenseEntity? = null

            if (form.recurring) {
                val existingRecurring = recurring.value.firstOrNull { it.name.equals(form.name.trim(), ignoreCase = true) && it.categoryId == form.categoryId }
                recurringEntity = existingRecurring?.copy(
                    name = form.name.trim(),
                    amountCents = amountCents,
                    paymentMethod = form.paymentMethod,
                    creditCardId = form.creditCardId.takeIf { form.paymentMethod == PaymentMethod.CARTAO_CREDITO },
                    notes = form.notes.takeIf { it.isNotBlank() },
                    active = true,
                    updatedAt = now
                ) ?: RecurringExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    name = form.name.trim(),
                    amountCents = amountCents,
                    categoryId = form.categoryId,
                    paymentMethod = form.paymentMethod,
                    creditCardId = form.creditCardId.takeIf { form.paymentMethod == PaymentMethod.CARTAO_CREDITO },
                    launchDay = purchaseDate.dayOfMonth,
                    startDate = purchaseDate.toMillis(),
                    endDate = null,
                    active = true,
                    notes = form.notes.takeIf { it.isNotBlank() },
                    createdAt = now,
                    updatedAt = now
                )
                recurringRepository.save(recurringEntity)
                recurringIdForExpense = recurringEntity.id
                recurringYearForExpense = purchaseDate.year
                recurringMonthForExpense = purchaseDate.monthValue
            }

            val existing = if (isEdit) repository.getAll().firstOrNull { it.id == expenseId } else null
            val baseCreatedAt = existing?.createdAt ?: now

            repository.save(
                ExpenseEntity(
                    id = expenseId,
                    name = form.name.trim(),
                    description = form.description.takeIf { it.isNotBlank() },
                    amountCents = amountCents,
                    purchaseDate = purchaseDate.toMillis(),
                    dueDate = if (form.paymentMethod == PaymentMethod.CARTAO_CREDITO) null else (dueDate?.toMillis() ?: purchaseDate.toMillis()),
                    paymentDate = if (form.status == ExpenseStatus.PAGO) (existing?.paymentDate ?: now) else null,
                    categoryId = form.categoryId,
                    paymentMethod = form.paymentMethod,
                    status = form.status,
                    creditCardId = form.creditCardId.takeIf { form.paymentMethod == PaymentMethod.CARTAO_CREDITO },
                    recurring = form.recurring,
                    recurringExpenseId = if (form.recurring) recurringIdForExpense else existing?.recurringExpenseId,
                    recurringYear = if (form.recurring) recurringYearForExpense else existing?.recurringYear,
                    recurringMonth = if (form.recurring) recurringMonthForExpense else existing?.recurringMonth,
                    invoiceYear = if (form.paymentMethod == PaymentMethod.CARTAO_CREDITO) null else existing?.invoiceYear,
                    invoiceMonth = if (form.paymentMethod == PaymentMethod.CARTAO_CREDITO) null else existing?.invoiceMonth,
                    notes = form.notes.takeIf { it.isNotBlank() },
                    createdAt = baseCreatedAt,
                    updatedAt = now
                )
            )
            message.value = if (isEdit) "Gasto atualizado." else "Gasto salvo."
        }.onFailure { message.value = it.message ?: "Nao foi possivel salvar." }
    }

    fun toFormState(expense: ExpenseEntity, categoryName: String? = null): ExpenseFormState {
        val purchase = DateUtils.millisToDate(expense.purchaseDate)
        val due = expense.dueDate?.let { DateUtils.millisToDate(it) }
        return ExpenseFormState(
            id = expense.id,
            name = expense.name,
            description = expense.description.orEmpty(),
            amount = (expense.amountCents / 100.0).toString().replace('.', ','),
            purchaseDateInput = DateUtils.formatBrDate(purchase),
            dueDateInput = if (expense.paymentMethod == PaymentMethod.CARTAO_CREDITO) "" else due?.let { DateUtils.formatBrDate(it) }.orEmpty(),
            categoryId = expense.categoryId,
            paymentMethod = expense.paymentMethod,
            creditCardId = expense.creditCardId.orEmpty(),
            status = expense.status,
            recurring = expense.recurring,
            notes = expense.notes.orEmpty()
        )
    }

    fun markPaid(expense: ExpenseEntity) = viewModelScope.launch { repository.markPaid(expense) }
    fun cancel(expense: ExpenseEntity) = viewModelScope.launch { repository.save(expense.copy(status = ExpenseStatus.CANCELADO, updatedAt = DateUtils.nowMillis())) }
    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
    fun consumeMessage() { message.value = null }
}
