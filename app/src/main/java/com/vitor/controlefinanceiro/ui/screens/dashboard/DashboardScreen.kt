package com.vitor.controlefinanceiro.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitor.controlefinanceiro.core.date.DateUtils
import com.vitor.controlefinanceiro.core.money.MoneyFormatter
import com.vitor.controlefinanceiro.data.repository.RecentTransaction
import com.vitor.controlefinanceiro.ui.components.EmptyText
import com.vitor.controlefinanceiro.ui.components.MoneyCard
import com.vitor.controlefinanceiro.ui.components.SectionCard
import com.vitor.controlefinanceiro.ui.components.monthLabel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var fabExpanded by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { fabExpanded = true }) {
                    Text("+")
                }
                DropdownMenu(expanded = fabExpanded, onDismissRequest = { fabExpanded = false }) {
                    DropdownMenuItem(text = { Text("Nova entrada") }, onClick = { fabExpanded = false; onAddIncome() })
                    DropdownMenuItem(text = { Text("Novo gasto") }, onClick = { fabExpanded = false; onAddExpense() })
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = viewModel::previousMonth) { Text("<") }
                    Text(monthLabel(state.year, state.month), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = viewModel::nextMonth) { Text(">") }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MoneyCard("Entradas do mes", state.summary.totalEntradas, Modifier.weight(1f))
                        MoneyCard("Gastos do mes", state.summary.totalGastos, Modifier.weight(1f))
                    }
                    MoneyCard("Saldo do mes", state.summary.saldo, Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MoneyCard("Gastos em aberto", state.summary.totalAberto, Modifier.weight(1f))
                        MoneyCard("Gastos pagos", state.summary.totalPago, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MoneyCard("Cartao de credito", state.summary.totalCartaoCredito, Modifier.weight(1f))
                        MoneyCard("Outros pagamentos", state.summary.totalDinheiro + state.summary.totalPix + state.summary.totalDebito + state.summary.totalBoleto, Modifier.weight(1f))
                    }
                }
            }
            item { Text("Ultimos lancamentos", style = MaterialTheme.typography.titleMedium) }
            if (state.recent.isEmpty()) item { EmptyText("Nenhum lancamento ainda.") }
            items(state.recent) { tx ->
                when (tx) {
                    is RecentTransaction.Expense -> SectionCard(tx.item.name) {
                        Text("- ${MoneyFormatter.formatCentsToBrl(tx.item.amountCents)} - ${tx.item.paymentMethod}")
                        Text("Compra: ${DateUtils.formatBrDate(DateUtils.millisToDate(tx.item.purchaseDate))}")
                    }
                    is RecentTransaction.Income -> SectionCard(tx.item.description) {
                        Text("+ ${MoneyFormatter.formatCentsToBrl(tx.item.amountCents)} - ${tx.item.type.name}")
                        Text(DateUtils.formatBrDate(DateUtils.millisToDate(tx.item.date)))
                    }
                }
            }
        }
    }
}
