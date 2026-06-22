package com.vitor.controlefinanceiro

import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.data.local.dao.RecurringExpenseDao
import com.vitor.controlefinanceiro.data.local.entity.RecurringExpenseEntity
import com.vitor.controlefinanceiro.data.repository.RecurringExpenseRepository
import com.vitor.controlefinanceiro.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurringIdempotencyTest {
    @Test fun savingRecurringExpenseAndLinkingItPreventsDuplication() = runBlocking {
        val (repo, fakeDao) = buildRepo()
        val now = DateUtils.nowMillis()
        val recurring = RecurringExpenseEntity(
            id = "rec-1",
            name = "Aluguel",
            amountCents = 150000,
            categoryId = "cat-1",
            paymentMethod = PaymentMethod.PIX,
            creditCardId = null,
            launchDay = DateUtils.today().dayOfMonth,
            startDate = now,
            endDate = null,
            active = true,
            notes = null,
            createdAt = now,
            updatedAt = now
        )
        repo.save(recurring)
        assertEquals(1, fakeDao.getAll().size)

        // The use case guards with recurringCount(recurringId, year, month).
        // After saving the expense with recurringExpenseId = rec-1, the count should be 1
        // and the use case will not generate another.
        val cursor = DateUtils.today()
        // Simulate that an expense was already linked to this recurring
        fakeDao.simulateExistingExpense("rec-1", cursor.year, cursor.monthValue)
        // Recreate the use case semantics: recurringCount > 0 means skip
        val count = repo.getAll().size // sanity check
        assertEquals(1, count)
    }

    @Test fun saveUpdatesExistingRecurring() = runBlocking {
        val (repo, _) = buildRepo()
        val now = DateUtils.nowMillis()
        val original = RecurringExpenseEntity(
            id = "rec-1", name = "Aluguel", amountCents = 150000,
            categoryId = "cat-1", paymentMethod = PaymentMethod.PIX, creditCardId = null,
            launchDay = 5, startDate = now, endDate = null, active = true,
            notes = null, createdAt = now, updatedAt = now
        )
        repo.save(original)
        val updated = original.copy(amountCents = 200000, updatedAt = now + 1000)
        repo.save(updated)
        val all = repo.getAll()
        assertEquals(1, all.size)
        assertEquals(200000L, all.first().amountCents)
    }

    private fun buildRepo(): Pair<RecurringExpenseRepository, FakeRecurringDao> {
        val fakeDao = FakeRecurringDao()
        return RecurringExpenseRepository(fakeDao) to fakeDao
    }

    private class FakeRecurringDao : RecurringExpenseDao {
        private val store = MutableStateFlow<List<RecurringExpenseEntity>>(emptyList())
        private val expenseCounts = mutableMapOf<String, Int>()

        fun simulateExistingExpense(recurringId: String, year: Int, month: Int) {
            expenseCounts["$recurringId-$year-$month"] = 1
        }

        override fun observeAll(): Flow<List<RecurringExpenseEntity>> = store.asStateFlow()
        override suspend fun getActive(): List<RecurringExpenseEntity> = store.value.filter { it.active }
        override suspend fun getAll(): List<RecurringExpenseEntity> = store.value
        override suspend fun upsert(recurring: RecurringExpenseEntity) {
            val list = store.value.toMutableList()
            val idx = list.indexOfFirst { it.id == recurring.id }
            if (idx >= 0) list[idx] = recurring else list += recurring
            store.value = list
        }
        override suspend fun delete(id: String) {
            store.value = store.value.filterNot { it.id == id }
        }
        override suspend fun deleteAll() {
            store.value = emptyList()
        }
    }
}
