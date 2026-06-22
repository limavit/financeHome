package com.vitor.controlefinanceiro.ui.screens.incomes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import com.vitor.controlefinanceiro.data.local.entity.IncomeEntity
import com.vitor.controlefinanceiro.domain.model.IncomeType
import com.vitor.controlefinanceiro.ui.components.AppDialog
import com.vitor.controlefinanceiro.ui.components.ChipSelector
import com.vitor.controlefinanceiro.ui.components.EmptyText
import com.vitor.controlefinanceiro.ui.components.LabeledTextField
import com.vitor.controlefinanceiro.ui.components.SectionCard
import com.vitor.controlefinanceiro.ui.components.monthLabel
import org.koin.androidx.compose.koinViewModel

@Composable
fun IncomeListScreen(viewModel: IncomeViewModel = koinViewModel()) {
    val incomes by viewModel.incomes.collectAsState()
    val message by viewModel.message.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<IncomeEntity?>(null) }
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = { editing = null; showForm = true }) { Text("+") } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = viewModel::previousMonth) { Text("<") }
                    Text(monthLabel(viewModel.selectedYear, viewModel.selectedMonth), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = viewModel::nextMonth) { Text(">") }
                }
            }
            item { Text("Entradas") }
            if (incomes.isEmpty()) item { EmptyText("Nenhuma entrada neste mes.") }
            items(incomes) { income ->
                SectionCard(income.description) {
                    Text("${MoneyFormatter.formatCentsToBrl(income.amountCents)} - ${income.type.label()}")
                    Text(DateUtils.formatBrDate(DateUtils.millisToDate(income.date)))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { editing = income; showForm = true }) { Text("Editar") }
                        TextButton(onClick = { viewModel.delete(income.id) }) { Text("Excluir") }
                    }
                }
            }
        }
    }
    if (showForm) IncomeFormDialog(
        initial = editing?.let { viewModel.toFormState(it) },
        onDismiss = { showForm = false; editing = null },
        onSave = { form ->
            viewModel.save(form, MoneyFormatter.parseBrlToCents(form.amount))
            showForm = false
            editing = null
        }
    )
    message?.let {
        AppDialog("Mensagem", { viewModel.consumeMessage() }, { viewModel.consumeMessage() }, "OK") { Text(it) }
    }
}

@Composable
private fun IncomeFormDialog(initial: IncomeFormState?, onDismiss: () -> Unit, onSave: (IncomeFormState) -> Unit) {
    var form by remember { mutableStateOf(initial ?: IncomeFormState()) }
    val title = if (initial?.id != null) "Editar entrada" else "Nova entrada"
    AppDialog(title, onDismiss, { onSave(form) }) {
        LabeledTextField("Descricao", form.description, { form = form.copy(description = it) })
        LabeledTextField("Valor", form.amount, { form = form.copy(amount = it) })
        LabeledTextField("Data (dd/MM/yyyy)", form.dateInput, { form = form.copy(dateInput = it) })
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
