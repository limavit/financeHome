package com.vitor.controlefinanceiro.domain.usecase

import com.vitor.controlefinanceiro.core.result.AppResult
import com.vitor.controlefinanceiro.data.backup.BackupDto
import com.vitor.controlefinanceiro.data.backup.BackupRepository

class ExportBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(): String = repository.exportJson()
}

class ImportBackupUseCase(private val repository: BackupRepository) {
    fun validate(jsonText: String): AppResult<BackupDto> = repository.validate(jsonText)
    suspend fun replace(dto: BackupDto) = repository.importReplacing(dto)
}
