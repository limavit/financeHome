package com.vitor.controlefinanceiro.ui.screens.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.vitor.controlefinanceiro.core.money.MoneyFormatter
import com.vitor.controlefinanceiro.data.local.entity.RecurringExpenseEntity
import com.vitor.controlefinanceiro.domain.model.PaymentMethod
import com.vitor.controlefinanceiro.ui.components.AppDialog
import com.vitor.controlefinanceiro.ui.components.ChipSelector
import com.vitor.controlefinanceiro.ui.components.EmptyText
import com.vitor.controlefinanceiro.ui.components.LabeledTextField
import com.vitor.controlefinanceiro.ui.components.SectionCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecurringListScreen(viewModel: RecurringViewModel = koinViewModel()) {
    val recurring by viewModel.recurring.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val message by viewModel.message.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RecurringExpenseEntity?>(null) }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { editing = null; showForm = true }) { Text("+") } }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Recorrencias") }
            if (recurring.isEmpty()) item { EmptyText("Nenhuma recorrencia cadastrada.") }
            items(recurring) { r ->
                SectionCard("${if (r.active) "" else "[inativa] "}${r.name}") {
                    Text("${MoneyFormatter.formatCentsToBrl(r.amountCents)} - ${r.paymentMethod} - dia ${r.launchDay}")
                    Text(if (r.active) "Ativa" else "Inativa")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { viewModel.toggleActive(r) }) { Text(if (r.active) "Desativar" else "Ativar") }
                        TextButton(onClick = { editing = r; showForm = true }) { Text("Editar") }
                        TextButton(onClick = { viewModel.delete(r.id) }) { Text("Excluir") }
                    }
                }
            }
        }
    }
    if (showForm) RecurringFormDialog(
        initial = editing?.let { viewModel.toFormState(it) },
        categories = categories.map { it.id to it.name },
        cards = cards.map { it.id to it.nickname },
        onDismiss = { showForm = false; editing = null },
        onSave = { form ->
            viewModel.save(form, MoneyFormatter.parseBrlToCents(form.amount))
            showForm = false
            editing = null
        }
    )
    message?.let { AppDialog("Mensagem", { viewModel.consumeMessage() }, { viewModel.consumeMessage() }, "OK") { Text(it) } }
}

@Composable
private fun RecurringFormDialog(
    initial: RecurringFormState?,
    categories: List<Pair<String, String>>,
    cards: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSave: (RecurringFormState) -> Unit
) {
    var form by remember {
        mutableStateOf(
            initial ?: RecurringFormState(categoryId = categories.firstOrNull()?.first.orEmpty())
        )
    }
    val title = if (initial?.id != null) "Editar recorrencia" else "Nova recorrencia"
    AppDialog(title, onDismiss, { onSave(form) }) {
        LabeledTextField("Nome", form.name, { form = form.copy(name = it) })
        LabeledTextField("Valor", form.amount, { form = form.copy(amount = it) })
        Text("Categoria")
        ChipSelector(categories, categories.firstOrNull { it.first == form.categoryId } ?: categories.firstOrNull().orEmptyPair(), { it.second }, { form = form.copy(categoryId = it.first) })
        Text("Forma de pagamento")
        ChipSelector(PaymentMethod.entries.toList(), form.paymentMethod, { it.name }, { form = form.copy(paymentMethod = it) })
        if (form.paymentMethod == PaymentMethod.CARTAO_CREDITO) {
            Text("Cartao")
            ChipSelector(cards, cards.firstOrNull { it.first == form.creditCardId } ?: cards.firstOrNull().orEmptyPair(), { it.second }, { form = form.copy(creditCardId = it.first) })
        }
        LabeledTextField("Dia de lancamento (1-31)", form.launchDay, { form = form.copy(launchDay = it) })
        LabeledTextField("Data de inicio (dd/MM/yyyy)", form.startDateInput, { form = form.copy(startDateInput = it) })
        LabeledTextField("Data final (dd/MM/yyyy) opcional", form.endDateInput, { form = form.copy(endDateInput = it) })
        LabeledTextField("Observacao", form.notes, { form = form.copy(notes = it) })
    }
}

private fun Pair<String, String>?.orEmptyPair(): Pair<String, String> = this ?: "" to "Nenhum"
