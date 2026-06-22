package com.vitor.controlefinanceiro.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MoreScreen(onCategories: () -> Unit, onBackup: () -> Unit, onRecurring: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mais")
        Button(onClick = onCategories, modifier = Modifier.fillMaxWidth()) { Text("Categorias") }
        Button(onClick = onRecurring, modifier = Modifier.fillMaxWidth()) { Text("Recorrencias") }
        Button(onClick = onBackup, modifier = Modifier.fillMaxWidth()) { Text("Backup") }
    }
}
