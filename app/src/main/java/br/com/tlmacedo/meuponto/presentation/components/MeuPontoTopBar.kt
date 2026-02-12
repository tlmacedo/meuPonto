// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/MeuPontoTopBar.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * TopAppBar customizada do aplicativo Meu Ponto.
 *
 * Barra superior com título centralizado e ações opcionais
 * de navegação e configurações, com design moderno.
 *
 * @param title Título a ser exibido
 * @param showBackButton Se deve exibir botão de voltar
 * @param showHistoryButton Se deve exibir botão de histórico
 * @param showSettingsButton Se deve exibir botão de configurações
 * @param onBackClick Callback para ação de voltar
 * @param onHistoryClick Callback para ação de histórico
 * @param onSettingsClick Callback para ação de configurações
 * @param modifier Modificador opcional
 *
 * @author Thiago
 * @since 1.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeuPontoTopBar(
    title: String,
    showBackButton: Boolean = false,
    showHistoryButton: Boolean = false,
    showSettingsButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        actions = {
            if (showHistoryButton) {
                IconButton(onClick = onHistoryClick) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Histórico"
                    )
                }
            }
            if (showSettingsButton) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurações"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}
