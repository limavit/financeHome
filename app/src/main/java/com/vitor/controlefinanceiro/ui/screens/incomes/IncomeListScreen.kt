package com.vitor.controlefinanceiro.ui.screens.incomes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.money.MoneyFormatter
import com.vitor.controlefinanceiro.domain.model.IncomeType
import com.vitor.controlefinanceiro.ui.components.AppDialog
import com.vitor.controlefinanceiro.ui.components.ChipSelector
import com.vitor.controlefinanceiro.ui.components.EmptyText
import com.vitor.controlefinanceiro.ui.components.LabeledTextField
import com.vitor.controlefinanceiro.ui.components.SectionCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun IncomeListScreen(viewModel: IncomeViewModel = koinViewModel()) {
    val incomes by viewModel.incomes.collectAsState()
    val message by viewModel.message.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true }) { Text("+") } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Entradas") }
            if (incomes.isEmpty()) item { EmptyText("Nenhuma entrada cadastrada.") }
            items(incomes) { income ->
                SectionCard(income.description) {
                    Text("${MoneyFormatter.formatCentsToBrl(income.amountCents)} - ${income.type.label()}")
                    Text(DateUtils.millisToDate(income.date).toString())
                    TextButton(onClick = { viewModel.delete(income.id) }) { Text("Excluir") }
                }
            }
        }
    }
    if (showForm) IncomeFormDialog(onDismiss = { showForm = false }, onSave = { form ->
        viewModel.save(form, MoneyFormatter.parseBrlToCents(form.amount))
        showForm = false
    })
    message?.let {
        AppDialog("Mensagem", { viewModel.consumeMessage() }, { viewModel.consumeMessage() }, "OK") { Text(it) }
    }
}

@Composable
private fun IncomeFormDialog(onDismiss: () -> Unit, onSave: (IncomeFormState) -> Unit) {
    var form by remember { mutableStateOf(IncomeFormState()) }
    AppDialog("Nova entrada", onDismiss, { onSave(form) }) {
        LabeledTextField("Descricao", form.description, { form = form.copy(description = it) })
        LabeledTextField("Valor", form.amount, { form = form.copy(amount = it) })
        ChipSelector(IncomeType.entries.toList(), form.type, { it.label() }, { form = form.copy(type = it) })
        LabeledTextField("Observacao", form.notes, { form = form.copy(notes = it) })
    }
}

private fun IncomeType.label(): String = when (this) {
    IncomeType.SALARIO -> "Salario"
    IncomeType.ADIANTAMENTO -> "Adiantamento"
    IncomeType.RETIRADA_INVESTIMENTO -> "Retirada de investimento"
    IncomeType.PRESTACAO_SERVICO -> "Prestacao de servico"
    IncomeType.OUTROS -> "Outros"
}
