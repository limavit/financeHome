package com.vitor.controlefinanceiro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.vitor.controlefinanceiro.data.local.entity.AppMetadataEntity
import com.vitor.controlefinanceiro.data.local.entity.CategoryEntity
import com.vitor.controlefinanceiro.data.local.entity.CreditCardEntity
import com.vitor.controlefinanceiro.data.local.entity.ExpenseEntity
import com.vitor.controlefinanceiro.data.local.entity.IncomeEntity
import com.vitor.controlefinanceiro.data.local.entity.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY active DESC, name")
    fun observeAll(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories WHERE active = 1 AND (type = 'GASTO' OR type = 'AMBOS') ORDER BY name")
    fun observeActiveExpenseCategories(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories WHERE active = 1 AND (type = 'ENTRADA' OR type = 'AMBOS') ORDER BY name")
    fun observeActiveIncomeCategories(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<CategoryEntity>
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?
    @Upsert suspend fun upsert(category: CategoryEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(categories: List<CategoryEntity>)
    @Query("DELETE FROM categories WHERE isSystem = 0 AND id NOT IN (SELECT categoryId FROM expenses)")
    suspend fun deleteUnusedCustomCategories()
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<IncomeEntity>>
    @Query("SELECT * FROM incomes WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun observeByPeriod(start: Long, end: Long): Flow<List<IncomeEntity>>
    @Query("SELECT * FROM incomes")
    suspend fun getAll(): List<IncomeEntity>
    @Upsert suspend fun upsert(income: IncomeEntity)
    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun delete(id: String)
    @Query("DELETE FROM incomes")
    suspend fun deleteAll()
}

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY active DESC, nickname")
    fun observeAll(): Flow<List<CreditCardEntity>>
    @Query("SELECT * FROM credit_cards WHERE active = 1 ORDER BY nickname")
    fun observeActive(): Flow<List<CreditCardEntity>>
    @Query("SELECT * FROM credit_cards")
    suspend fun getAll(): List<CreditCardEntity>
    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getById(id: String): CreditCardEntity?
    @Upsert suspend fun upsert(card: CreditCardEntity)
    @Query("DELETE FROM credit_cards")
    suspend fun deleteAll()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY purchaseDate DESC, createdAt DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>
    @Query("""SELECT * FROM expenses WHERE 
        (:year IS NULL OR ((paymentMethod = 'CARTAO_CREDITO' AND invoiceYear = :year AND invoiceMonth = :month) OR (paymentMethod != 'CARTAO_CREDITO' AND strftime('%Y', datetime(COALESCE(dueDate, purchaseDate)/1000, 'unixepoch', 'localtime')) = printf('%04d', :year) AND strftime('%m', datetime(COALESCE(dueDate, purchaseDate)/1000, 'unixepoch', 'localtime')) = printf('%02d', :month))))
        ORDER BY purchaseDate DESC""")
    fun observeFiltered(year: Int?, month: Int?): Flow<List<ExpenseEntity>>
    @Query("SELECT * FROM expenses WHERE creditCardId = :cardId AND invoiceYear = :year AND invoiceMonth = :month ORDER BY purchaseDate")
    fun observeInvoice(cardId: String, year: Int, month: Int): Flow<List<ExpenseEntity>>
    @Query("SELECT * FROM expenses")
    suspend fun getAll(): List<ExpenseEntity>
    @Query("SELECT COUNT(*) FROM expenses WHERE recurringExpenseId = :recurringId AND recurringYear = :year AND recurringMonth = :month")
    suspend fun recurringCount(recurringId: String, year: Int, month: Int): Int
    @Upsert suspend fun upsert(expense: ExpenseEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(expenses: List<ExpenseEntity>)
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: String)
    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses ORDER BY active DESC, name")
    fun observeAll(): Flow<List<RecurringExpenseEntity>>
    @Query("SELECT * FROM recurring_expenses WHERE active = 1")
    suspend fun getActive(): List<RecurringExpenseEntity>
    @Query("SELECT * FROM recurring_expenses")
    suspend fun getAll(): List<RecurringExpenseEntity>
    @Upsert suspend fun upsert(recurring: RecurringExpenseEntity)
    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun delete(id: String)
    @Query("DELETE FROM recurring_expenses")
    suspend fun deleteAll()
}

@Dao
interface MetadataDao {
    @Query("SELECT * FROM app_metadata WHERE `key` = :key")
    suspend fun get(key: String): AppMetadataEntity?
    @Query("SELECT * FROM app_metadata")
    suspend fun getAll(): List<AppMetadataEntity>
    @Upsert suspend fun upsert(metadata: AppMetadataEntity)
    @Query("DELETE FROM app_metadata")
    suspend fun deleteAll()
}
