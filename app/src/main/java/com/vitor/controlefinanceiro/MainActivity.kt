package com.vitor.controlefinanceiro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vitor.controlefinanceiro.ui.navigation.AppNavHost
import com.vitor.controlefinanceiro.ui.theme.ControleFinanceiroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControleFinanceiroTheme {
                AppNavHost()
            }
        }
    }
}
