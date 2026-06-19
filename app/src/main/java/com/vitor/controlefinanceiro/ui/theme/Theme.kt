package com.vitor.controlefinanceiro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = lightColorScheme(
    primary = Color(0xFF176B5A),
    secondary = Color(0xFF7A5C16),
    tertiary = Color(0xFF315F8A),
    surface = Color(0xFFFAFAF7),
    background = Color(0xFFF7F8F5),
    error = Color(0xFFB3261E)
)

@Composable
fun ControleFinanceiroTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
