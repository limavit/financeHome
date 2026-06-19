package com.vitor.controlefinanceiro.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vitor.controlefinanceiro.ui.screens.backup.BackupScreen
import com.vitor.controlefinanceiro.ui.screens.cards.CreditCardListScreen
import com.vitor.controlefinanceiro.ui.screens.categories.CategoryListScreen
import com.vitor.controlefinanceiro.ui.screens.dashboard.DashboardScreen
import com.vitor.controlefinanceiro.ui.screens.expenses.ExpenseListScreen
import com.vitor.controlefinanceiro.ui.screens.incomes.IncomeListScreen
import com.vitor.controlefinanceiro.ui.screens.more.MoreScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val tabs = listOf(
        Routes.Dashboard to "Inicio",
        Routes.Expenses to "Gastos",
        Routes.Incomes to "Entradas",
        Routes.Cards to "Cartoes",
        Routes.More to "Mais"
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { (route, label) ->
                    NavigationBarItem(
                        selected = current == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(Routes.Dashboard) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(label.take(1)) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Routes.Dashboard, modifier = Modifier.padding(padding)) {
            composable(Routes.Dashboard) {
                DashboardScreen(
                    onAddIncome = { navController.navigate(Routes.Incomes) },
                    onAddExpense = { navController.navigate(Routes.Expenses) },
                    onCards = { navController.navigate(Routes.Cards) },
                    onCategories = { navController.navigate(Routes.Categories) },
                    onBackup = { navController.navigate(Routes.Backup) }
                )
            }
            composable(Routes.Expenses) { ExpenseListScreen() }
            composable(Routes.Incomes) { IncomeListScreen() }
            composable(Routes.Cards) { CreditCardListScreen() }
            composable(Routes.More) { MoreScreen(onCategories = { navController.navigate(Routes.Categories) }, onBackup = { navController.navigate(Routes.Backup) }) }
            composable(Routes.Categories) { CategoryListScreen() }
            composable(Routes.Backup) { BackupScreen() }
        }
    }
}
