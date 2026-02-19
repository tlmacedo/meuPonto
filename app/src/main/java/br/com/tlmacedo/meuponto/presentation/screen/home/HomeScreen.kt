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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.presentation.components.DateNavigator
import br.com.tlmacedo.meuponto.presentation.components.EmpregoSelectorBottomSheet
import br.com.tlmacedo.meuponto.presentation.components.EmpregoSelectorChip
import br.com.tlmacedo.meuponto.presentation.components.IntervaloCard
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.RegistrarPontoButton
import br.com.tlmacedo.meuponto.presentation.components.RegistrarPontoManualButton
import br.com.tlmacedo.meuponto.presentation.components.ResumoCard
import br.com.tlmacedo.meuponto.presentation.components.TimePickerDialog
import br.com.tlmacedo.meuponto.presentation.components.FeriadoBanner
import java.time.LocalDate
import java.time.ZoneOffset
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
 * @updated 2.6.0 - Nome do emprego movido para TopBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    dataSelecionadaInicial: String? = null,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditPonto: (Long) -> Unit = {},
    onNavigateToNovoEmprego: () -> Unit = {},
    onNavigateToEditarEmprego: (Long) -> Unit = {}
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
                is HomeUiEvent.NavegarParaNovoEmprego -> {
                    onNavigateToNovoEmprego()
                }
                is HomeUiEvent.NavegarParaEditarEmprego -> {
                    onNavigateToEditarEmprego(event.empregoId)
                }
            }
        }
    }

    // Navegar para data específica quando vindo do histórico
    LaunchedEffect(dataSelecionadaInicial) {
        dataSelecionadaInicial?.let { dataString ->
            try {
                val data = LocalDate.parse(dataString)
                viewModel.onAction(HomeAction.SelecionarData(data))
            } catch (e: Exception) {
                // Ignora se a data for inválida
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

    // Dialog de DatePicker
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataSelecionada
                .atStartOfDay()
                .atZone(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { viewModel.onAction(HomeAction.FecharDatePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.onAction(HomeAction.SelecionarData(selectedDate))
                        }
                        viewModel.onAction(HomeAction.FecharDatePicker)
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(HomeAction.FecharDatePicker) }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Dialog de confirmação de exclusão
    if (uiState.showDeleteConfirmDialog && uiState.pontoParaExcluir != null) {
        DeletePontoConfirmDialog(
            ponto = uiState.pontoParaExcluir!!,
            pontosHoje = uiState.pontosHoje,
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
                subtitle = uiState.empregoAtivo?.nome,
                showTodayButton = !uiState.isHoje,
                showHistoryButton = true,
                showSettingsButton = true,
                onTodayClick = { viewModel.onAction(HomeAction.IrParaHoje) },
                onHistoryClick = { viewModel.onAction(HomeAction.NavegarParaHistorico) },
                onSettingsClick = { viewModel.onAction(HomeAction.NavegarParaConfiguracoes) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.pontosHoje.isEmpty()) {
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
        // Chip de emprego (compacto) - apenas para ações de seleção/edição
        // Só mostra se não tiver emprego ou tiver múltiplos
        if (!uiState.temEmpregoAtivo || uiState.temMultiplosEmpregos) {
            item {
                EmpregoSelectorChip(
                    empregoAtivo = uiState.empregoAtivo,
                    temMultiplosEmpregos = uiState.temMultiplosEmpregos,
                    showMenu = uiState.showEmpregoMenu,
                    onClick = {
                        if (uiState.empregoAtivo == null) {
                            onAction(HomeAction.NavegarParaNovoEmprego)
                        } else if (uiState.temMultiplosEmpregos) {
                            onAction(HomeAction.AbrirSeletorEmprego)
                        }
                    },
                    onLongClick = {
                        onAction(HomeAction.AbrirMenuEmprego)
                    },
                    onNovoEmprego = {
                        onAction(HomeAction.NavegarParaNovoEmprego)
                    },
                    onEditarEmprego = {
                        onAction(HomeAction.NavegarParaEditarEmprego)
                    },
                    onDismissMenu = {
                        onAction(HomeAction.FecharMenuEmprego)
                    }
                )
            }
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
                onSelecionarData = { onAction(HomeAction.AbrirDatePicker) }
            )
        }

        // Banner de Feriado (se houver)
        if (uiState.isFeriado) {
            item {
                FeriadoBanner(
                    feriados = uiState.feriadosDoDia
                )
            }
        }

        // Card de Resumo com contador em tempo real
        item {
            ResumoCard(
                resumoDia = uiState.resumoDia,
                bancoHoras = uiState.bancoHoras,
                versaoJornada = uiState.versaoJornadaAtual,
                dataHoraInicioContador = uiState.dataHoraInicioContador,
                mostrarContador = uiState.deveExibirContador
            )
        }
        // Botão de Registrar Ponto
        if (uiState.podeRegistrarPontoAutomatico) {
            item {
                RegistrarPontoButton(
                    proximoTipo = uiState.proximoTipo,
                    horaAtual = uiState.horaAtual,
                    onRegistrarAgora = { onAction(HomeAction.RegistrarPontoAgora) },
                    onRegistrarManual = { onAction(HomeAction.AbrirTimePickerDialog) }
                )
            }
        } else if (uiState.podeRegistrarPontoManual) {
            item {
                RegistrarPontoManualButton(
                    proximoTipo = uiState.proximoTipo,
                    dataFormatada = uiState.dataFormatadaCurta,
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
                    IntervaloCard(
                        intervalo = intervalo,
                        mostrarContadorTempoReal = uiState.isHoje
                    )
                }
            }
        } else if (uiState.temEmpregoAtivo && !uiState.isFuturo) {
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
    pontosHoje: List<br.com.tlmacedo.meuponto.domain.model.Ponto>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val pontosOrdenados = pontosHoje.sortedBy { it.dataHora }
    val indice = pontosOrdenados.indexOfFirst { it.id == ponto.id }
    val tipoDescricao = if (indice >= 0) {
        TipoPonto.getTipoPorIndice(indice).descricao
    } else {
        "ponto"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Ponto") },
        text = {
            Text(
                "Deseja realmente excluir o registro de $tipoDescricao às ${
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
