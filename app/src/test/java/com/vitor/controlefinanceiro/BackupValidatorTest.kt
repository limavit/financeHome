package com.vitor.controlefinanceiro

import com.vitor.controlefinanceiro.data.backup.BackupDto
import com.vitor.controlefinanceiro.data.backup.BackupValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupValidatorTest {
    @Test fun validatesSchemaVersion() {
        val dto = BackupDto(
            schemaVersion = 2,
            exportedAt = 1,
            categories = emptyList(),
            incomes = emptyList(),
            expenses = emptyList(),
            creditCards = emptyList(),
            recurringExpenses = emptyList(),
            metadata = emptyMap()
        )
        assertEquals("Versao de backup nao suportada.", BackupValidator.validate(dto))
    }

    @Test fun acceptsEmptyValidBackup() {
        val dto = BackupDto(
            exportedAt = 1,
            categories = emptyList(),
            incomes = emptyList(),
            expenses = emptyList(),
            creditCards = emptyList(),
            recurringExpenses = emptyList(),
            metadata = emptyMap()
        )
        assertNull(BackupValidator.validate(dto))
    }
}
