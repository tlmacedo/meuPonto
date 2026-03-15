// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import br.com.tlmacedo.meuponto.presentation.components.AusenciaBanner
import br.com.tlmacedo.meuponto.presentation.components.CicloBanner
import br.com.tlmacedo.meuponto.presentation.components.DateNavigator
import br.com.tlmacedo.meuponto.presentation.components.EdicaoPontoModal
import br.com.tlmacedo.meuponto.presentation.components.EmpregoSelectorBottomSheet
import br.com.tlmacedo.meuponto.presentation.components.EmpregoSelectorChip
import br.com.tlmacedo.meuponto.presentation.components.ExcluirPontoDialog
import br.com.tlmacedo.meuponto.presentation.components.FechamentoCicloBanner
import br.com.tlmacedo.meuponto.presentation.components.FeriadoBanner
import br.com.tlmacedo.meuponto.presentation.components.FotoPontoModal
import br.com.tlmacedo.meuponto.presentation.components.IntervaloCard
import br.com.tlmacedo.meuponto.presentation.components.LocalizacaoModal
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.NsrInputDialog
import br.com.tlmacedo.meuponto.presentation.components.RegistrarPontoButton
import br.com.tlmacedo.meuponto.presentation.components.RegistrarPontoManualButton
import br.com.tlmacedo.meuponto.presentation.components.ResumoCard
import br.com.tlmacedo.meuponto.presentation.components.TimePickerDialog
import br.com.tlmacedo.meuponto.presentation.components.foto.ComprovanteImagePicker
import br.com.tlmacedo.meuponto.presentation.screen.home.components.FechamentoCicloDialog
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Tela principal do aplicativo Meu Ponto.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 6.2.0 - Adicionado suporte a ciclos de banco de horas
 * @updated 7.2.0 - Substituída edição inline por modais + swipe
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    dataSelecionadaInicial: String? = null,
    onNavigateToHistorico: () -> Unit = {},
    onNavigateToConfiguracoes: () -> Unit = {},
    onNavigateToEditarPonto: (Long) -> Unit = {},
    onNavigateToNovoEmprego: () -> Unit = {},
    onNavigateToEditarEmprego: (Long) -> Unit = {},
    onNavigateToHistoricoCiclos: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ══════════════════════════════════════════════════════════════════════
    // RECARREGAR CONFIGURAÇÃO QUANDO A TELA VOLTA AO FOCO
    // ══════════════════════════════════════════════════════════════════════
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.onAction(HomeAction.RecarregarConfiguracaoEmprego)
        }
    }

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
                    onNavigateToHistorico()
                }

                is HomeUiEvent.NavegarParaConfiguracoes -> {
                    onNavigateToConfiguracoes()
                }

                is HomeUiEvent.NavegarParaEditarPonto -> {
                    onNavigateToEditarPonto(event.pontoId)
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

                is HomeUiEvent.NavegarParaHistoricoCiclos -> {
                    onNavigateToHistoricoCiclos()
                }
            }
        }
    }

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

    // ══════════════════════════════════════════════════════════════════════
    // DIALOGS
    // ══════════════════════════════════════════════════════════════════════

    // TimePicker para registro de ponto
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

    // DatePicker
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

    // NSR Dialog
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

    // Emprego Selector
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

    // Fechamento de Ciclo Dialog
    if (uiState.showFechamentoCicloDialog && uiState.estadoCiclo is EstadoCiclo.Pendente) {
        FechamentoCicloDialog(
            estadoCiclo = uiState.estadoCiclo as EstadoCiclo.Pendente,
            onConfirmar = { viewModel.onAction(HomeAction.ConfirmarFechamentoCiclo) },
            onCancelar = { viewModel.onAction(HomeAction.FecharDialogFechamentoCiclo) }
        )
    }

    // ══════════════════════════════════════════════════════════════════════
    // MODAIS DE PONTO
    // ══════════════════════════════════════════════════════════════════════

    // Modal de Edição
    uiState.edicaoModal?.let { modalState ->
        EdicaoPontoModal(
            ponto = modalState.ponto,
            tipoDescricao = modalState.tipoDescricao,
            onDismiss = { viewModel.onAction(HomeAction.FecharEdicaoModal) },
            onSalvar = { hora, nsr, motivo, detalhes ->
                viewModel.onAction(
                    HomeAction.SalvarEdicaoModal(
                        pontoId = modalState.ponto.id,
                        hora = hora,
                        nsr = nsr,
                        motivo = motivo,
                        detalhes = detalhes
                    )
                )
            },
            mostrarNsr = uiState.nsrHabilitado
        )
    }

    // Modal de Exclusão
    uiState.exclusaoModal?.let { modalState ->
        ExcluirPontoDialog(
            ponto = modalState.ponto,
            tipoDescricao = modalState.tipoDescricao,
            onDismiss = { viewModel.onAction(HomeAction.FecharExclusaoModal) },
            onConfirmar = { motivo ->
                viewModel.onAction(
                    HomeAction.ConfirmarExclusaoModal(
                        pontoId = modalState.ponto.id,
                        motivo = motivo
                    )
                )
            },
            isLoading = modalState.isDeleting
        )
    }

    // Modal de Localização
    uiState.localizacaoModal?.let { modalState ->
        LocalizacaoModal(
            ponto = modalState.ponto,
            tipoDescricao = modalState.tipoDescricao,
            onDismiss = { viewModel.onAction(HomeAction.FecharLocalizacaoModal) }
        )
    }

    // Modal de Foto
    uiState.fotoModal?.let { modalState ->
        FotoPontoModal(
            ponto = modalState.ponto,
            tipoDescricao = modalState.tipoDescricao,
            fotoPath = modalState.fotoPath,
            onDismiss = { viewModel.onAction(HomeAction.FecharFotoModal) }
        )
    }

    // ══════════════════════════════════════════════════════════════════════
    // SCAFFOLD
    // ══════════════════════════════════════════════════════════════════════

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

@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {

        // Banner de ciclo (fixo no topo do conteúdo)
        CicloBanner(
            estadoCiclo = uiState.estadoCiclo,
            onFecharCiclo = { onAction(HomeAction.AbrirDialogFechamentoCiclo) },
            onVerHistorico = { onAction(HomeAction.NavegarParaHistoricoCiclos) }
        )

        // Header fixo
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
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

            ResumoCard(
                horaAtual = uiState.horaAtual,
                resumoDia = uiState.resumoDia,
                bancoHoras = uiState.bancoHoras,
                versaoJornada = uiState.versaoJornadaAtual,
                dataHoraInicioContador = uiState.dataHoraInicioContador,
                mostrarContador = uiState.deveExibirContador
            )

            // Banner de fechamento de ciclo anterior
            if (uiState.deveExibirBannerFechamentoCiclo) {
                uiState.fechamentoCicloAnterior?.let { fechamento ->
                    FechamentoCicloBanner(fechamento = fechamento)
                }
            }

            if (uiState.temAusencia) {
                uiState.ausenciaDoDia?.let { ausencia ->
                    AusenciaBanner(ausencia = ausencia)
                }
            }

            // Seletor de foto de comprovante
            if (uiState.fotoHabilitada) {
                ComprovanteImagePicker(
                    showSourceDialog = uiState.showFotoSourceDialog,
                    onDismissSourceDialog = { onAction(HomeAction.FecharFotoSourceDialog) },
                    cameraUri = uiState.cameraUri,
                    onCameraResult = { success ->
                        if (success) {
                            onAction(HomeAction.ConfirmarFotoCamera)
                        } else {
                            onAction(HomeAction.FecharFotoSourceDialog)
                        }
                    },
                    onGalleryResult = { uri ->
                        if (uri != null) {
                            onAction(HomeAction.SelecionarFotoComprovante(uri))
                        } else {
                            onAction(HomeAction.FecharFotoSourceDialog)
                        }
                    },
                    onPermissionDenied = { mensagem ->
                        onAction(HomeAction.FecharFotoSourceDialog)
                    }
                )
            }

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

            if (uiState.isFeriado) {
                FeriadoBanner(feriados = uiState.feriadosDoDia)
            }

            if (uiState.isFuturo) {
                FutureDateWarning()
            }

            if (!uiState.temEmpregoAtivo) {
                NoEmpregoWarning()
            }
        }

        // Conteúdo scrollável
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.temPontos) {
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
                            mostrarContadorTempoReal = uiState.isHoje && !uiState.temModalAberto,
                            mostrarNsr = uiState.nsrHabilitado,
                            onEditar = { ponto ->
                                onAction(HomeAction.AbrirEdicaoModal(ponto))
                            },
                            onExcluir = { ponto ->
                                onAction(HomeAction.AbrirExclusaoModal(ponto))
                            },
                            onVerFoto = { ponto ->
                                onAction(HomeAction.AbrirFotoModal(ponto))
                            },
                            onVerLocalizacao = { ponto ->
                                onAction(HomeAction.AbrirLocalizacaoModal(ponto))
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
