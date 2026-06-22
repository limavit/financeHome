package com.vitor.controlefinanceiro.data.backup

import androidx.room.withTransaction
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.json.JsonConfig
import com.vitor.controlefinanceiro.core.result.AppResult
import com.vitor.controlefinanceiro.data.local.AppDatabase
import com.vitor.controlefinanceiro.data.local.entity.AppMetadataEntity
import com.vitor.controlefinanceiro.data.preferences.AppPreferencesRepository
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString

class BackupRepository(
    private val db: AppDatabase,
    private val appPreferencesRepository: AppPreferencesRepository
) {
    suspend fun exportJson(): String {
        val exportedAt = DateUtils.nowMillis()
        val dto = BackupDto(
            exportedAt = exportedAt,
            categories = db.categoryDao().getAll().map { it.toBackup() },
            incomes = db.incomeDao().getAll().map { it.toBackup() },
            expenses = db.expenseDao().getAll().map { it.toBackup() },
            creditCards = db.creditCardDao().getAll().map { it.toBackup() },
            recurringExpenses = db.recurringExpenseDao().getAll().map { it.toBackup() },
            metadata = db.metadataDao().getAll().associate { it.toPair() }
        )
        db.metadataDao().upsert(AppMetadataEntity("last_export_at", exportedAt.toString()))
        appPreferencesRepository.setLastExportAt(exportedAt)
        return JsonConfig.json.encodeToString(dto)
    }

    fun validate(jsonText: String): AppResult<BackupDto> {
        return try {
            val dto = JsonConfig.json.decodeFromString<BackupDto>(jsonText)
            val error = BackupValidator.validate(dto)
            if (error == null) AppResult.Success(dto) else AppResult.Error(error)
        } catch (e: IllegalArgumentException) {
            AppResult.Error("Arquivo de backup invalido.", e)
        } catch (e: SerializationException) {
            AppResult.Error("Arquivo de backup invalido.", e)
        }
    }

    suspend fun importReplacing(dto: BackupDto) {
        val importedAt = DateUtils.nowMillis()
        db.withTransaction {
            db.expenseDao().deleteAll()
            db.recurringExpenseDao().deleteAll()
            db.incomeDao().deleteAll()
            db.creditCardDao().deleteAll()
            db.categoryDao().deleteAll()
            db.metadataDao().deleteAll()
            dto.categories.forEach { db.categoryDao().upsert(it.toEntity()) }
            dto.creditCards.forEach { db.creditCardDao().upsert(it.toEntity()) }
            dto.incomes.forEach { db.incomeDao().upsert(it.toEntity()) }
            dto.recurringExpenses.forEach { db.recurringExpenseDao().upsert(it.toEntity()) }
            dto.expenses.forEach { db.expenseDao().upsert(it.toEntity()) }
            dto.metadata.forEach { (key, value) -> db.metadataDao().upsert(AppMetadataEntity(key, value)) }
            db.metadataDao().upsert(AppMetadataEntity("last_import_at", importedAt.toString()))
        }
        appPreferencesRepository.setLastImportAt(importedAt)
    }
}

object BackupValidator {
    fun validate(dto: BackupDto): String? {
        if (dto.schemaVersion != 1) return "Versao de backup nao suportada."
        if (dto.categories.any { it.id.isBlank() || it.name.isBlank() }) return "Backup com categoria invalida."
        if (dto.incomes.any { it.id.isBlank() || it.description.isBlank() || it.amountCents < 0 }) return "Backup com entrada invalida."
        if (dto.expenses.any { it.id.isBlank() || it.name.isBlank() || it.amountCents < 0 || it.categoryId.isBlank() }) return "Backup com gasto invalido."
        if (dto.creditCards.any { it.id.isBlank() || it.nickname.isBlank() || it.closingDay !in 1..31 || it.dueDay !in 1..31 }) return "Backup com cartao invalido."
        if (dto.recurringExpenses.any { it.id.isBlank() || it.name.isBlank() || it.amountCents < 0 || it.launchDay !in 1..31 }) return "Backup com recorrencia invalida."
        return null
    }
}
