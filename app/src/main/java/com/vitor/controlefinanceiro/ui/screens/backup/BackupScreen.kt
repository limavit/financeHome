package com.vitor.controlefinanceiro.ui.screens.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vitor.controlefinanceiro.ui.components.AppDialog
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun BackupScreen(viewModel: BackupViewModel = koinViewModel()) {
    val context = LocalContext.current
    val message by viewModel.message.collectAsState()
    val pending by viewModel.pendingImport.collectAsState()
    var exportText by remember { mutableStateOf<String?>(null) }

    val createLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val content = exportText
        if (uri != null && content != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
        }
        exportText = null
    }
    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
            viewModel.validateImport(text)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exportContent.collect { content ->
            if (content != null) {
                exportText = content
                val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))
                createLauncher.launch("controle-financeiro-backup-$stamp.json")
                viewModel.consumeExport()
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Backup")
        Button(onClick = { viewModel.prepareExport() }, modifier = Modifier.fillMaxWidth()) { Text("Exportar JSON") }
        Button(onClick = { openLauncher.launch(arrayOf("application/json", "text/*", "*/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Importar JSON") }
        Text("A importacao substitui todos os dados atuais apos confirmacao.")
    }

    if (pending != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Confirmar importacao") },
            text = { Text("Atencao: a importacao vai substituir todos os dados atuais deste aparelho. Deseja continuar?") },
            confirmButton = { Button(onClick = viewModel::confirmReplaceImport) { Text("Substituir") } },
            dismissButton = { TextButton(onClick = { viewModel.pendingImport.value = null }) { Text("Cancelar") } }
        )
    }
    message?.let { AppDialog("Mensagem", { viewModel.consumeMessage() }, { viewModel.consumeMessage() }, "OK") { Text(it) } }
}
