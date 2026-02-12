package br.com.tlmacedo.meuponto.presentation.screen.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditPonto: (Long) -> Unit = {}
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Histórico",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Tela de Histórico\n(em desenvolvimento)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
