package com.vitor.controlefinanceiro.ui.screens.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
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
import com.vitor.controlefinanceiro.domain.model.ExpenseStatus
import com.vitor.controlefinanceiro.domain.model.PaymentMethod
import com.vitor.controlefinanceiro.ui.components.AppDialog
import com.vitor.controlefinanceiro.ui.components.ChipSelector
import com.vitor.controlefinanceiro.ui.components.EmptyText
import com.vitor.controlefinanceiro.ui.components.LabeledTextField
import com.vitor.controlefinanceiro.ui.components.SectionCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExpenseListScreen(viewModel: ExpenseViewModel = koinViewModel()) {
    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val message by viewModel.message.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = { showForm = true }) { Text("+") } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Gastos") }
            if (expenses.isEmpty()) item { EmptyText("Nenhum gasto cadastrado.") }
            items(expenses) { expense ->
                SectionCard(expense.name) {
                    Text("${MoneyFormatter.formatCentsToBrl(expense.amountCents)} - ${expense.paymentMethod} - ${expense.status}")
                    Text("Compra: ${DateUtils.millisToDate(expense.purchaseDate)}")
                    if (expense.invoiceYear != null) Text("Fatura: ${expense.invoiceMonth}/${expense.invoiceYear}")
                    TextButton(onClick = { viewModel.markPaid(expense) }, enabled = expense.status != ExpenseStatus.PAGO) { Text("Marcar pago") }
                    TextButton(onClick = { viewModel.cancel(expense) }, enabled = expense.status != ExpenseStatus.CANCELADO) { Text("Cancelar") }
                    TextButton(onClick = { viewModel.delete(expense.id) }) { Text("Excluir") }
                }
            }
        }
    }
    if (showForm) ExpenseFormDialog(
        categories = categories.map { it.id to it.name },
        cards = cards.map { it.id to it.nickname },
        onDismiss = { showForm = false },
        onSave = { form ->
            viewModel.save(form, MoneyFormatter.parseBrlToCents(form.amount))
            showForm = false
        }
    )
    message?.let { AppDialog("Mensagem", { viewModel.consumeMessage() }, { viewModel.consumeMessage() }, "OK") { Text(it) } }
}

@Composable
private fun ExpenseFormDialog(
    categories: List<Pair<String, String>>,
    cards: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSave: (ExpenseFormState) -> Unit
) {
    var form by remember { mutableStateOf(ExpenseFormState(categoryId = categories.firstOrNull()?.first.orEmpty())) }
    AppDialog("Novo gasto", onDismiss, { onSave(form) }) {
        LabeledTextField("Nome", form.name, { form = form.copy(name = it) })
        LabeledTextField("Descricao", form.description, { form = form.copy(description = it) })
        LabeledTextField("Valor", form.amount, { form = form.copy(amount = it) })
        Text("Categoria")
        ChipSelector(categories, categories.firstOrNull { it.first == form.categoryId } ?: categories.firstOrNull().orEmptyPair(), { it.second }, { form = form.copy(categoryId = it.first) })
        Text("Forma de pagamento")
        ChipSelector(PaymentMethod.entries.toList(), form.paymentMethod, { it.name }, { form = form.copy(paymentMethod = it) })
        if (form.paymentMethod == PaymentMethod.CARTAO_CREDITO) {
            Text("Cartao")
            ChipSelector(cards, cards.firstOrNull { it.first == form.creditCardId } ?: cards.firstOrNull().orEmptyPair(), { it.second }, { form = form.copy(creditCardId = it.first) })
        }
        Text("Status")
        ChipSelector(ExpenseStatus.entries.toList(), form.status, { it.name }, { form = form.copy(status = it) })
        androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Recorrente")
            Checkbox(checked = form.recurring, onCheckedChange = { form = form.copy(recurring = it) })
        }
        LabeledTextField("Observacao", form.notes, { form = form.copy(notes = it) })
    }
}

private fun Pair<String, String>?.orEmptyPair(): Pair<String, String> = this ?: "" to "Nenhum"
