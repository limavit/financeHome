package com.vitor.controlefinanceiro.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class AppPreferencesRepository(private val context: Context) {
    private val lastExportKey = longPreferencesKey("last_export_at")
    private val lastImportKey = longPreferencesKey("last_import_at")

    val lastExportAt: Flow<Long?> = context.dataStore.data.map { it[lastExportKey] }
    val lastImportAt: Flow<Long?> = context.dataStore.data.map { it[lastImportKey] }

    suspend fun setLastExportAt(value: Long) {
        context.dataStore.edit { it[lastExportKey] = value }
    }

    suspend fun setLastImportAt(value: Long) {
        context.dataStore.edit { it[lastImportKey] = value }
    }
}
