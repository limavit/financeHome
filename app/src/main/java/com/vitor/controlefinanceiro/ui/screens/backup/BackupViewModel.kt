package com.vitor.controlefinanceiro.ui.screens.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.result.AppResult
import com.vitor.controlefinanceiro.data.backup.BackupDto
import com.vitor.controlefinanceiro.data.preferences.AppPreferencesRepository
import com.vitor.controlefinanceiro.domain.usecase.ExportBackupUseCase
import com.vitor.controlefinanceiro.domain.usecase.ImportBackupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BackupViewModel(
    private val exportBackup: ExportBackupUseCase,
    private val importBackup: ImportBackupUseCase,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {
    val message = MutableStateFlow<String?>(null)
    val pendingImport = MutableStateFlow<BackupDto?>(null)
    val exportContent = MutableStateFlow<String?>(null)
    val lastExportAt = appPreferencesRepository.lastExportAt.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val lastImportAt = appPreferencesRepository.lastImportAt.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun prepareExport() = viewModelScope.launch {
        runCatching { exportBackup() }
            .onSuccess { exportContent.value = it }
            .onFailure { message.value = it.message ?: "Nao foi possivel exportar." }
    }

    fun validateImport(text: String) {
        when (val result = importBackup.validate(text)) {
            is AppResult.Success -> pendingImport.value = result.data
            is AppResult.Error -> message.value = result.message
        }
    }

    fun confirmReplaceImport() = viewModelScope.launch {
        val dto = pendingImport.value ?: return@launch
        runCatching { importBackup.replace(dto) }
            .onSuccess {
                pendingImport.value = null
                message.value = "Backup importado com sucesso."
            }
            .onFailure { message.value = it.message ?: "Nao foi possivel importar." }
    }

    fun consumeExport(): String? = exportContent.value.also { exportContent.value = null }
    fun consumeMessage() { message.value = null }

    fun formatLastBackup(millis: Long?): String {
        if (millis == null) return "Nunca"
        val date = DateUtils.millisToDate(millis)
        val today = DateUtils.today()
        val time = java.time.Instant.ofEpochMilli(millis).atZone(DateUtils.zone).toLocalTime()
        val timeStr = "%02d:%02d".format(time.hour, time.minute)
        return "${DateUtils.formatBrDate(date)} as $timeStr"
    }
}
