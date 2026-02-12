// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.IntervaloCard
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.RegistrarPontoButton
import br.com.tlmacedo.meuponto.presentation.components.ResumoCard
import br.com.tlmacedo.meuponto.presentation.components.TimePickerDialog

/**
 * Tela principal do aplicativo Meu Ponto.
 *
 * Exibe o resumo do dia, botão de registro de ponto,
 * e lista de intervalos trabalhados de forma visual e intuitiva.
 *
 * @param viewModel ViewModel da tela
 * @param onNavigateToHistory Callback para navegar ao histórico
 * @param onNavigateToSettings Callback para navegar às configurações
 * @param onNavigateToEditPonto Callback para navegar à edição de ponto
 *
 * @author Thiago
 * @since 1.0.0
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditPonto: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Coleta eventos
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(event.mensagem)
                }
                is HomeUiEvent.MostrarErro -> {
                    snackbarHostState.showSnackbar(event.mensagem)
                }
                is HomeUiEvent.NavegarParaHistorico -> {
                    onNavigateToHistory()
                }
                is HomeUiEvent.NavegarParaConfiguracoes -> {
                    onNavigateToSettings()
                }
                is HomeUiEvent.NavegarParaEdicao -> {
                    onNavigateToEditPonto(event.pontoId)
                }
            }
        }
    }

    // Dialog de TimePicker
    if (uiState.showTimePickerDialog) {
        TimePickerDialog(
            titulo = "Registrar ${uiState.proximoTipo.descricao}",
            horaInicial = uiState.horaAtual,
            onConfirm = { hora ->
                viewModel.onAction(HomeAction.RegistrarPontoManual(hora))
            },
            onDismiss = {
                viewModel.onAction(HomeAction.FecharTimePickerDialog)
            }
        )
    }

    // Dialog de confirmação de exclusão
    if (uiState.showDeleteConfirmDialog && uiState.pontoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(HomeAction.CancelarExclusao) },
            title = { Text("Excluir Ponto") },
            text = {
                Text(
                    "Deseja realmente excluir o registro de ${uiState.pontoParaExcluir!!.tipo.descricao} às ${
                        uiState.pontoParaExcluir!!.hora.format(
                            java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                        )
                    }?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onAction(HomeAction.ConfirmarExclusao) }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onAction(HomeAction.CancelarExclusao) }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Meu Ponto",
                showHistoryButton = true,
                showSettingsButton = true,
                onHistoryClick = { viewModel.onAction(HomeAction.NavegarParaHistorico) },
                onSettingsClick = { viewModel.onAction(HomeAction.NavegarParaConfiguracoes) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
            }
        } else {
            HomeContent(
                uiState = uiState,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Conteúdo principal da tela Home.
 */
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Data atual
        item {
            Text(
                text = uiState.dataFormatada,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Card de Resumo
        item {
            ResumoCard(
                resumoDia = uiState.resumoDia,
                bancoHoras = uiState.bancoHoras
            )
        }

        // Botão de Registrar Ponto
        item {
            RegistrarPontoButton(
                proximoTipo = uiState.proximoTipo,
                horaAtual = uiState.horaAtual,
                onRegistrarAgora = { onAction(HomeAction.RegistrarPontoAgora) },
                onRegistrarManual = { onAction(HomeAction.AbrirTimePickerDialog) }
            )
        }

        // Título da seção de intervalos
        if (uiState.temPontos) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Registros de Hoje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Lista de intervalos
            items(
                items = uiState.resumoDia.intervalos,
                key = { it.entrada.id }
            ) { intervalo ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    IntervaloCard(intervalo = intervalo)
                }
            }
        } else {
            // Estado vazio
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "😴",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum ponto registrado hoje",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Toque no botão acima para começar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
