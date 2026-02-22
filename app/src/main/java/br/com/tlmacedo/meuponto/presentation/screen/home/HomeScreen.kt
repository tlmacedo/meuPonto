// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import br.com.tlmacedo.meuponto.presentation.components.AusenciaBanner
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
import br.com.tlmacedo.meuponto.presentation.components.NsrInputDialog
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


/**
 * Tela principal do aplicativo Meu Ponto.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 5.2.0 - DateNavigator, ResumoCard e botão de registro fixos
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

                is HomeUiEvent.NavegarParaEditarPonto -> {
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
                            val selectedDate = millis.toLocalDateFromDatePicker()
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
            motivo = uiState.motivoExclusao,
            onMotivoChange = { viewModel.onAction(HomeAction.AtualizarMotivoExclusao(it)) },
            onConfirm = { viewModel.onAction(HomeAction.ConfirmarExclusao) },
            onDismiss = { viewModel.onAction(HomeAction.CancelarExclusao) }
        )
    }

    // Dialog de NSR
    if (uiState.showNsrDialog) {
        NsrInputDialog(
            tipoNsr = uiState.tipoNsr,
            valor = uiState.nsrPendente,
            tipoPonto = uiState.proximoTipo.descricao,
            onValorChange = { viewModel.onAction(HomeAction.AtualizarNsr(it)) },
            onConfirm = { viewModel.onAction(HomeAction.ConfirmarRegistroComNsr) },
            onDismiss = { viewModel.onAction(HomeAction.CancelarNsrDialog) }
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
 * Header fixo (DateNavigator + ResumoCard + Botão Registro) + Lista scrollável.
 */
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // ================================================================
        // HEADER FIXO - DateNavigator + ResumoCard + Ausência + Botão (não rola)
        // ================================================================
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Chip de emprego (apenas se necessário)
            if (!uiState.temEmpregoAtivo || uiState.temMultiplosEmpregos) {
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
                    onLongClick = { onAction(HomeAction.AbrirMenuEmprego) },
                    onNovoEmprego = { onAction(HomeAction.NavegarParaNovoEmprego) },
                    onEditarEmprego = { onAction(HomeAction.NavegarParaEditarEmprego) },
                    onDismissMenu = { onAction(HomeAction.FecharMenuEmprego) }
                )
            }

            // Navegador de Data (FIXO)
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

            // Card de Resumo (FIXO)
            ResumoCard(
                horaAtual = uiState.horaAtual,
                resumoDia = uiState.resumoDia,
                bancoHoras = uiState.bancoHoras,
                versaoJornada = uiState.versaoJornadaAtual,
                dataHoraInicioContador = uiState.dataHoraInicioContador,
                mostrarContador = uiState.deveExibirContador
            )

            // Banner de Ausência (FIXO)
            if (uiState.temAusencia) {
                uiState.ausenciaDoDia?.let { ausencia ->
                    AusenciaBanner(ausencia = ausencia)
                }
            }

            // Botão de Registrar Ponto (FIXO)
            if (uiState.podeRegistrarPontoAutomatico) {
                RegistrarPontoButton(
                    proximoTipo = uiState.proximoTipo,
                    horaAtual = uiState.horaAtual,
                    onRegistrarAgora = { onAction(HomeAction.RegistrarPontoAgora) },
                    onRegistrarManual = { onAction(HomeAction.AbrirTimePickerDialog) }
                )
            } else if (uiState.podeRegistrarPontoManual) {
                RegistrarPontoManualButton(
                    proximoTipo = uiState.proximoTipo,
                    dataFormatada = uiState.dataFormatadaCurta,
                    onRegistrarManual = { onAction(HomeAction.AbrirTimePickerDialog) }
                )
            }

            // Banner de Feriado (se houver)
            if (uiState.isFeriado) {
                FeriadoBanner(feriados = uiState.feriadosDoDia)
            }

            // Aviso de data futura
            if (uiState.isFuturo) {
                FutureDateWarning()
            }

            // Aviso de sem emprego
            if (!uiState.temEmpregoAtivo) {
                NoEmpregoWarning()
            }
        }

        // ================================================================
        // CONTEÚDO SCROLLÁVEL - Banners, avisos e registros
        // ================================================================
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Seção de Registros de Ponto
            if (uiState.temPontos) {
                // Divisor e título
                item(key = "registros_header") {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Registros ${if (uiState.isHoje) "de Hoje" else "do Dia"}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Lista de intervalos
                items(
                    items = uiState.resumoDia.intervalos,
                    key = { "intervalo_${it.entrada.id}" }
                ) { intervalo ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        IntervaloCard(
                            intervalo = intervalo,
                            mostrarContadorTempoReal = uiState.isHoje,
                            mostrarNsr = uiState.nsrHabilitado,
                            onEditarEntrada = { pontoId ->
                                onAction(HomeAction.EditarPonto(pontoId))
                            },
                            onEditarSaida = { pontoId ->
                                onAction(HomeAction.EditarPonto(pontoId))
                            }
                        )
                    }
                }
            } else if (uiState.temEmpregoAtivo && !uiState.isFuturo) {
                item(key = "empty_state") {
                    EmptyPontosState()
                }
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
    motivo: String,
    onMotivoChange: (String) -> Unit,
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

    val motivoValido = motivo.trim().length >= 5

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Ponto") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Deseja realmente excluir o registro de $tipoDescricao às ${
                        ponto.hora.format(DateTimeFormatter.ofPattern("HH:mm"))
                    }?"
                )

                OutlinedTextField(
                    value = motivo,
                    onValueChange = onMotivoChange,
                    label = { Text("Motivo da exclusão *") },
                    placeholder = { Text("Ex: Registro duplicado") },
                    supportingText = {
                        if (motivo.isNotEmpty() && !motivoValido) {
                            Text(
                                "Mínimo 5 caracteres",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    isError = motivo.isNotEmpty() && !motivoValido,
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = motivoValido
            ) {
                Text(
                    "Excluir",
                    color = if (motivoValido)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
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
            style = MaterialTheme.typography.displaySmall
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
            style = MaterialTheme.typography.displaySmall
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
            style = MaterialTheme.typography.displaySmall
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
