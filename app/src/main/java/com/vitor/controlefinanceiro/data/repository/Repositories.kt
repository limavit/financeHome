package com.vitor.controlefinanceiro.data.repository

import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.date.DateUtils.toMillis
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
import com.vitor.controlefinanceiro.domain.model.CategoryType
import com.vitor.controlefinanceiro.domain.model.ExpenseStatus
import com.vitor.controlefinanceiro.domain.model.PaymentMethod
import com.vitor.controlefinanceiro.domain.usecase.CalculateCreditCardInvoiceUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.util.UUID

class CategoryRepository(private val dao: CategoryDao, private val metadataDao: MetadataDao) {
    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()
    fun observeExpenseCategories(): Flow<List<CategoryEntity>> = dao.observeActiveExpenseCategories()
    suspend fun getAll(): List<CategoryEntity> = dao.getAll()
    suspend fun getById(id: String): CategoryEntity? = dao.getById(id)
    suspend fun save(name: String, type: CategoryType, id: String = UUID.randomUUID().toString(), isSystem: Boolean = false, active: Boolean = true) {
        require(name.isNotBlank()) { "Informe o nome da categoria." }
        val now = DateUtils.nowMillis()
        val existing = dao.getById(id)
        dao.upsert(CategoryEntity(id, name.trim(), type, existing?.isSystem ?: isSystem, active, existing?.createdAt ?: now, now))
    }
    suspend fun setActive(category: CategoryEntity, active: Boolean) = dao.upsert(category.copy(active = active, updatedAt = DateUtils.nowMillis()))
    suspend fun ensureDefaults() {
        if (metadataDao.get("first_run_completed")?.value == "true") return
        val now = DateUtils.nowMillis()
        val names = listOf("Moradia", "Alimentacao", "Mercado", "Padaria", "Transporte", "Combustivel", "Saude", "Farmacia", "Educacao", "Filhos", "Lazer", "Restaurante", "Cartao de Credito", "Contas Fixas", "Energia", "Agua", "Internet", "Telefone", "Investimentos", "Impostos", "Servicos", "Outros")
        dao.insertAll(names.map { CategoryEntity("cat-${it.lowercase().replace(" ", "-")}", it, CategoryType.AMBOS, true, true, now, now) })
        metadataDao.upsert(AppMetadataEntity("first_run_completed", "true"))
    }
}

class IncomeRepository(private val dao: IncomeDao) {
    fun observeAll(): Flow<List<IncomeEntity>> = dao.observeAll()
    fun observeMonth(year: Int, month: Int): Flow<List<IncomeEntity>> = dao.observeByPeriod(DateUtils.monthStartMillis(year, month), DateUtils.monthEndMillis(year, month))
    suspend fun getAll(): List<IncomeEntity> = dao.getAll()
    suspend fun save(income: IncomeEntity) {
        require(income.description.isNotBlank()) { "Informe a descricao." }
        require(income.amountCents > 0) { "Informe um valor valido." }
        dao.upsert(income)
    }
    suspend fun delete(id: String) = dao.delete(id)
}

class CreditCardRepository(private val dao: CreditCardDao) {
    fun observeAll(): Flow<List<CreditCardEntity>> = dao.observeAll()
    fun observeActive(): Flow<List<CreditCardEntity>> = dao.observeActive()
    suspend fun getAll(): List<CreditCardEntity> = dao.getAll()
    suspend fun getById(id: String): CreditCardEntity? = dao.getById(id)
    suspend fun save(card: CreditCardEntity) {
        require(card.nickname.isNotBlank()) { "Informe o apelido do cartao." }
        require(card.closingDay in 1..31) { "O dia de fechamento deve estar entre 1 e 31." }
        require(card.dueDay in 1..31) { "O dia de vencimento deve estar entre 1 e 31." }
        dao.upsert(card)
    }
}

class ExpenseRepository(
    private val dao: ExpenseDao,
    private val cardDao: CreditCardDao,
    private val invoiceUseCase: CalculateCreditCardInvoiceUseCase
) {
    fun observeAll(): Flow<List<ExpenseEntity>> = dao.observeAll()
    fun observeMonth(year: Int, month: Int): Flow<List<ExpenseEntity>> = dao.observeFiltered(year, month)
    fun observeInvoice(cardId: String, year: Int, month: Int): Flow<List<ExpenseEntity>> = dao.observeInvoice(cardId, year, month)
    suspend fun getAll(): List<ExpenseEntity> = dao.getAll()
    suspend fun recurringCount(recurringId: String, year: Int, month: Int): Int = dao.recurringCount(recurringId, year, month)
    suspend fun save(expense: ExpenseEntity) {
        require(expense.name.isNotBlank()) { "Informe o nome do gasto." }
        require(expense.amountCents > 0) { "Informe um valor valido." }
        val normalized = normalizeCardFields(expense)
        dao.upsert(normalized)
    }
    suspend fun insertGenerated(expenses: List<ExpenseEntity>) = dao.insertAll(expenses)
    suspend fun delete(id: String) = dao.delete(id)
    suspend fun markPaid(expense: ExpenseEntity) = dao.upsert(expense.copy(status = ExpenseStatus.PAGO, paymentDate = DateUtils.nowMillis(), updatedAt = DateUtils.nowMillis()))

    suspend fun normalizeCardFields(expense: ExpenseEntity): ExpenseEntity {
        if (expense.paymentMethod != PaymentMethod.CARTAO_CREDITO) {
            return expense.copy(creditCardId = null, invoiceYear = null, invoiceMonth = null)
        }
        val cardId = requireNotNull(expense.creditCardId) { "Selecione um cartao de credito." }
        val card = cardDao.getById(cardId) ?: error("Cartao de credito nao encontrado.")
        val info = invoiceUseCase(DateUtils.millisToDate(expense.purchaseDate), card.closingDay, card.dueDay)
        return expense.copy(
            creditCardId = cardId,
            dueDate = info.dueDate.toMillis(),
            invoiceYear = info.invoiceYear,
            invoiceMonth = info.invoiceMonth
        )
    }
}

class RecurringExpenseRepository(private val dao: RecurringExpenseDao) {
    fun observeAll(): Flow<List<RecurringExpenseEntity>> = dao.observeAll()
    suspend fun getActive(): List<RecurringExpenseEntity> = dao.getActive()
    suspend fun getAll(): List<RecurringExpenseEntity> = dao.getAll()
    suspend fun save(recurring: RecurringExpenseEntity) {
        require(recurring.name.isNotBlank()) { "Informe o nome da recorrencia." }
        require(recurring.amountCents > 0) { "Informe um valor valido." }
        require(recurring.launchDay in 1..31) { "O dia deve estar entre 1 e 31." }
        dao.upsert(recurring)
    }
}

data class DashboardSummary(
    val totalEntradas: Long = 0,
    val totalGastos: Long = 0,
    val saldo: Long = 0,
    val totalPago: Long = 0,
    val totalAberto: Long = 0,
    val totalCartaoCredito: Long = 0,
    val totalDinheiro: Long = 0,
    val totalPix: Long = 0,
    val totalDebito: Long = 0,
    val totalBoleto: Long = 0
)

class DashboardRepository(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) {
    fun observeSummary(year: Int, month: Int): Flow<DashboardSummary> {
        return combine(incomeRepository.observeMonth(year, month), expenseRepository.observeMonth(year, month)) { incomes, expenses ->
            val entries = incomes.sumOf { it.amountCents }
            val considered = expenses.filter { it.status != ExpenseStatus.CANCELADO }
            DashboardSummary(
                totalEntradas = entries,
                totalGastos = considered.sumOf { it.amountCents },
                saldo = entries - considered.sumOf { it.amountCents },
                totalPago = considered.filter { it.status == ExpenseStatus.PAGO }.sumOf { it.amountCents },
                totalAberto = considered.filter { it.status == ExpenseStatus.ABERTO }.sumOf { it.amountCents },
                totalCartaoCredito = considered.filter { it.paymentMethod == PaymentMethod.CARTAO_CREDITO }.sumOf { it.amountCents },
                totalDinheiro = considered.filter { it.paymentMethod == PaymentMethod.DINHEIRO }.sumOf { it.amountCents },
                totalPix = considered.filter { it.paymentMethod == PaymentMethod.PIX }.sumOf { it.amountCents },
                totalDebito = considered.filter { it.paymentMethod == PaymentMethod.DEBITO }.sumOf { it.amountCents },
                totalBoleto = considered.filter { it.paymentMethod == PaymentMethod.BOLETO }.sumOf { it.amountCents }
            )
        }
    }
}
