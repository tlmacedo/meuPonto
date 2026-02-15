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
import br.com.tlmacedo.meuponto.presentation.components.DateNavigator
import br.com.tlmacedo.meuponto.presentation.components.EmpregoSelectorBottomSheet
import br.com.tlmacedo.meuponto.presentation.components.EmpregoSelectorChip
import br.com.tlmacedo.meuponto.presentation.components.IntervaloCard
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.RegistrarPontoButton
import br.com.tlmacedo.meuponto.presentation.components.ResumoCard
import br.com.tlmacedo.meuponto.presentation.components.TimePickerDialog
import java.time.format.DateTimeFormatter

/**
 * Tela principal do aplicativo Meu Ponto.
 *
 * Exibe o resumo do dia, botão de registro de ponto,
 * navegação por data, seleção de emprego e lista de intervalos
 * trabalhados de forma visual e intuitiva.
 *
 * @param viewModel ViewModel da tela
 * @param onNavigateToHistory Callback para navegar ao histórico
 * @param onNavigateToSettings Callback para navegar às configurações
 * @param onNavigateToEditPonto Callback para navegar à edição de ponto
 *
 * @author Thiago
 * @since 2.0.0
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
                is HomeUiEvent.EmpregoTrocado -> {
                    snackbarHostState.showSnackbar("Emprego alterado: ${event.nomeEmprego}")
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
        DeletePontoConfirmDialog(
            ponto = uiState.pontoParaExcluir!!,
            onConfirm = { viewModel.onAction(HomeAction.ConfirmarExclusao) },
            onDismiss = { viewModel.onAction(HomeAction.CancelarExclusao) }
        )
    }

    // Bottom Sheet de seleção de emprego
    if (uiState.showEmpregoSelector) {
        EmpregoSelectorBottomSheet(
            empregos = uiState.empregosDisponiveis,
            empregoAtivoId = uiState.empregoAtivo?.id,
            onSelecionarEmprego = { emprego ->
                viewModel.onAction(HomeAction.SelecionarEmprego(emprego))
            },
            onDismiss = {
                viewModel.onAction(HomeAction.FecharSeletorEmprego)
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
        if (uiState.isLoading && uiState.pontosHoje.isEmpty()) {
            // Loading inicial
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
internal fun HomeContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Seletor de Emprego (se houver múltiplos)
        item {
            EmpregoSelectorChip(
                empregoAtivo = uiState.empregoAtivo,
                temMultiplosEmpregos = uiState.temMultiplosEmpregos,
                onClick = { onAction(HomeAction.AbrirSeletorEmprego) }
            )
        }

        // Navegador de Data
        item {
            DateNavigator(
                dataFormatada = uiState.dataFormatada,
                dataFormatadaCurta = uiState.dataFormatadaCurta,
                isHoje = uiState.isHoje,
                podeNavegarAnterior = uiState.podeNavegaAnterior,
                podeNavegarProximo = uiState.podeNavegarProximo,
                onDiaAnterior = { onAction(HomeAction.DiaAnterior) },
                onProximoDia = { onAction(HomeAction.ProximoDia) },
                onIrParaHoje = { onAction(HomeAction.IrParaHoje) }
            )
        }

        // Card de Resumo
        item {
            ResumoCard(
                resumoDia = uiState.resumoDia,
                bancoHoras = uiState.bancoHoras
            )
        }

        // Botão de Registrar Ponto (apenas se puder registrar)
        if (uiState.podeRegistrarPonto) {
            item {
                RegistrarPontoButton(
                    proximoTipo = uiState.proximoTipo,
                    horaAtual = uiState.horaAtual,
                    onRegistrarAgora = { onAction(HomeAction.RegistrarPontoAgora) },
                    onRegistrarManual = { onAction(HomeAction.AbrirTimePickerDialog) }
                )
            }
        }

        // Aviso de data futura
        if (uiState.isFuturo) {
            item {
                FutureDateWarning()
            }
        }

        // Aviso de sem emprego
        if (!uiState.temEmpregoAtivo) {
            item {
                NoEmpregoWarning()
            }
        }

        // Título da seção de intervalos
        if (uiState.temPontos) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Registros ${if (uiState.isHoje) "de Hoje" else "do Dia"}",
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
        } else if (uiState.temEmpregoAtivo && !uiState.isFuturo) {
            // Estado vazio
            item {
                EmptyPontosState()
            }
        }
    }
}

/**
 * Dialog de confirmação de exclusão de ponto.
 */
@Composable
private fun DeletePontoConfirmDialog(
    ponto: br.com.tlmacedo.meuponto.domain.model.Ponto,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Ponto") },
        text = {
            Text(
                "Deseja realmente excluir o registro de ${ponto.tipo.descricao} às ${
                    ponto.hora.format(DateTimeFormatter.ofPattern("HH:mm"))
                }?"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Excluir", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Aviso de data futura.
 */
@Composable
private fun FutureDateWarning() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "📅",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Data futura",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Não é possível registrar pontos em datas futuras",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Aviso de sem emprego ativo.
 */
@Composable
private fun NoEmpregoWarning() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "🏢",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nenhum emprego configurado",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Configure um emprego nas Configurações para começar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Estado vazio - sem pontos registrados.
 */
@Composable
private fun EmptyPontosState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Text(
            text = "😴",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nenhum ponto registrado",
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
