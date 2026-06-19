package com.vitor.controlefinanceiro.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitor.controlefinanceiro.ui.components.MoneyCard
import com.vitor.controlefinanceiro.ui.components.monthLabel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onCards: () -> Unit,
    onCategories: () -> Unit,
    onBackup: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAddIncome, modifier = Modifier.fillMaxWidth()) { Text("Adicionar entrada") }
                    Button(onClick = onAddExpense, modifier = Modifier.fillMaxWidth()) { Text("Adicionar gasto") }
                    Button(onClick = onCards, modifier = Modifier.fillMaxWidth()) { Text("Cartoes") }
                    Button(onClick = onCategories, modifier = Modifier.fillMaxWidth()) { Text("Categorias") }
                    Button(onClick = onBackup, modifier = Modifier.fillMaxWidth()) { Text("Backup") }
                }
            }
        }
    }
}
