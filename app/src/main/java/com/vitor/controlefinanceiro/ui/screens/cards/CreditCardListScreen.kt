package com.vitor.controlefinanceiro.ui.screens.cards

import androidx.compose.foundation.layout.Arrangement
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
import com.vitor.controlefinanceiro.ui.components.AppDialog
import com.vitor.controlefinanceiro.ui.components.EmptyText
import com.vitor.controlefinanceiro.ui.components.LabeledTextField
import com.vitor.controlefinanceiro.ui.components.SectionCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreditCardListScreen(viewModel: CreditCardViewModel = koinViewModel()) {
    val cards by viewModel.cards.collectAsState()
    val invoice by viewModel.invoiceExpenses.collectAsState()
    val selected by viewModel.selectedInvoice.collectAsState()
    val message by viewModel.message.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { showForm = true }) { Text("+") } }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Cartoes") }
            if (cards.isEmpty()) item { EmptyText("Nenhum cartao cadastrado.") }
            items(cards) { card ->
                SectionCard(card.nickname) {
                    Text(card.institution.orEmpty())
                    Text("Fechamento ${card.closingDay} - Vencimento ${card.dueDay}")
                    Text("Status: ${if (card.active) "ativo" else "inativo"}")
                    TextButton(onClick = { viewModel.selectInvoice(card.id, selected.second, selected.third) }) { Text("Ver fatura do mes") }
                    TextButton(onClick = { viewModel.setActive(card, !card.active) }) { Text(if (card.active) "Desativar" else "Ativar") }
                }
            }
            if (selected.first.isNotBlank()) {
                item {
                    SectionCard("Fatura ${selected.third}/${selected.second}") {
                        Text("Total: ${MoneyFormatter.formatCentsToBrl(invoice.sumOf { it.amountCents })}")
                    }
                }
                items(invoice) { expense ->
                    SectionCard(expense.name) {
                        Text("${MoneyFormatter.formatCentsToBrl(expense.amountCents)} - ${expense.status}")
                    }
                }
            }
        }
    }
    if (showForm) CardFormDialog(
        onDismiss = { showForm = false },
        onSave = { form ->
            viewModel.save(form, MoneyFormatter.parseBrlToCents(form.limit).takeIf { form.limit.isNotBlank() })
            showForm = false
        }
    )
    message?.let { AppDialog("Mensagem", { viewModel.consumeMessage() }, { viewModel.consumeMessage() }, "OK") { Text(it) } }
}

@Composable
private fun CardFormDialog(onDismiss: () -> Unit, onSave: (CardFormState) -> Unit) {
    var form by remember { mutableStateOf(CardFormState()) }
    AppDialog("Novo cartao", onDismiss, { onSave(form) }) {
        LabeledTextField("Apelido", form.nickname, { form = form.copy(nickname = it) })
        LabeledTextField("Banco ou instituicao", form.institution, { form = form.copy(institution = it) })
        LabeledTextField("Limite opcional", form.limit, { form = form.copy(limit = it) })
        LabeledTextField("Dia de fechamento", form.closingDay, { form = form.copy(closingDay = it) })
        LabeledTextField("Dia de vencimento", form.dueDay, { form = form.copy(dueDay = it) })
    }
}
