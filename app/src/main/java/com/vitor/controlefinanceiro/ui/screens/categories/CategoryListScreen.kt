package com.vitor.controlefinanceiro.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitor.controlefinanceiro.domain.model.CategoryType
import com.vitor.controlefinanceiro.ui.components.AppDialog
import com.vitor.controlefinanceiro.ui.components.ChipSelector
import com.vitor.controlefinanceiro.ui.components.EmptyText
import com.vitor.controlefinanceiro.ui.components.LabeledTextField
import com.vitor.controlefinanceiro.ui.components.SectionCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun CategoryListScreen(viewModel: CategoryViewModel = koinViewModel()) {
    val categories by viewModel.categories.collectAsState()
    val message by viewModel.message.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { showForm = true }) { Text("+") } }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Categorias") }
            if (categories.isEmpty()) item { EmptyText("Nenhuma categoria cadastrada.") }
            items(categories) { category ->
                SectionCard(category.name) {
                    Text("${category.type} - ${if (category.active) "ativa" else "inativa"}")
                    Text(if (category.isSystem) "Padrao do sistema" else "Personalizada")
                    TextButton(onClick = { viewModel.setActive(category, !category.active) }) { Text(if (category.active) "Desativar" else "Ativar") }
                }
            }
        }
    }
    if (showForm) CategoryFormDialog(
        onDismiss = { showForm = false },
        onSave = {
            viewModel.save(it)
            showForm = false
        }
    )
    message?.let { AppDialog("Mensagem", { viewModel.consumeMessage() }, { viewModel.consumeMessage() }, "OK") { Text(it) } }
}

@Composable
private fun CategoryFormDialog(onDismiss: () -> Unit, onSave: (CategoryFormState) -> Unit) {
    var form by remember { mutableStateOf(CategoryFormState()) }
    AppDialog("Nova categoria", onDismiss, { onSave(form) }) {
        LabeledTextField("Nome", form.name, { form = form.copy(name = it) })
        ChipSelector(CategoryType.entries.toList(), form.type, { it.name }, { form = form.copy(type = it) })
    }
}
