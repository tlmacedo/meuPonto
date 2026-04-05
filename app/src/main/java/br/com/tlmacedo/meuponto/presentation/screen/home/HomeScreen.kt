// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
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
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.presentation.components.AusenciaBanner
import br.com.tlmacedo.meuponto.presentation.components.CicloBanner
import br.com.tlmacedo.meuponto.presentation.components.EdicaoModal
import br.com.tlmacedo.meuponto.presentation.components.ExclusaoModal
import br.com.tlmacedo.meuponto.presentation.components.FeriadoBanner
import br.com.tlmacedo.meuponto.presentation.components.FotoPontoModal
import br.com.tlmacedo.meuponto.presentation.components.IntervaloCard
import br.com.tlmacedo.meuponto.presentation.components.LocalizacaoModal
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.ProximoPontoCard
import br.com.tlmacedo.meuponto.presentation.components.RegistrarPontoModal
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

                is HomeUiEvent.SolicitarPermissaoLocalizacao -> {
                    context.findActivity()?.let { activity ->
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ),
                            1001
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(dataSelecionadaInicial) {
        if (dataSelecionadaInicial != null) {
            viewModel.onAction(HomeAction.SelecionarData(LocalDate.parse(dataSelecionadaInicial)))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MODAIS E DIALOGS
    // ══════════════════════════════════════════════════════════════════════

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataSelecionada.atStartOfDay(ZoneOffset.UTC)
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onAction(HomeAction.FecharDatePicker) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onAction(HomeAction.SelecionarData(it.toLocalDateFromDatePicker()))
                    }
                    viewModel.onAction(HomeAction.FecharDatePicker)
                }) { Text("Confirmar") }
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

    if (uiState.showFechamentoCicloDialog) {
        (uiState.estadoCiclo as? EstadoCiclo.Pendente)?.let { estadoPendente ->
            FechamentoCicloDialog(
                estadoCiclo = estadoPendente,
                onConfirmar = {
                    viewModel.onAction(
                        HomeAction.ConfirmarFechamentoCiclo(
                            saldoAnterior = estadoPendente.ciclo.saldoAtualMinutos.toLong(),
                            motivo = "Fechamento de ciclo"
                        )
                    )
                },
                onCancelar = { viewModel.onAction(HomeAction.FecharDialogFechamentoCiclo) }
            )
        }
    }

    // Gerenciador de fotos (Câmera e Galeria)
    // IMPORTANTE: Deve permanecer na composição para que os launchers funcionem corretamente
    ComprovanteImagePicker(
        showSourceDialog = uiState.showFotoSourceDialog,
        onDismissSourceDialog = { viewModel.onAction(HomeAction.FecharFotoSourceDialog) },
        cameraUri = uiState.cameraUri,
        onCameraResult = { success ->
            if (success) {
                if (uiState.registrarPontoModal != null) {
                    viewModel.onAction(HomeAction.AtualizarFotoRegistroModal(uiState.cameraUri))
                } else {
                    viewModel.onAction(HomeAction.ConfirmarFotoCamera)
                }
            }
        },
        onGalleryResult = { uri ->
            uri?.let {
                if (uiState.registrarPontoModal != null) {
                    viewModel.onAction(HomeAction.AtualizarFotoRegistroModal(it))
                } else {
                    viewModel.onAction(HomeAction.SelecionarFotoComprovante(it))
                }
            }
        },
        onPermissionDenied = { /* O erro de permissão pode ser tratado aqui se necessário */ }
    )

    // Modal de Edição
    uiState.edicaoModal?.let { modalState ->
        EdicaoModal(
            ponto = modalState.ponto,
            tipoDescricao = modalState.tipoDescricao,
            isSaving = modalState.isSaving,
            onConfirmar = { hora, nsr, motivo, detalhes ->
                viewModel.onAction(HomeAction.SalvarEdicaoModal(modalState.ponto.id, hora, nsr, motivo, detalhes))
            },
            onDismiss = { viewModel.onAction(HomeAction.FecharEdicaoModal) }
        )
    }

    // Modal de Exclusão
    uiState.exclusaoModal?.let { modalState ->
        ExclusaoModal(
            ponto = modalState.ponto,
            tipoDescricao = modalState.tipoDescricao,
            isDeleting = modalState.isDeleting,
            onConfirmar = { viewModel.onAction(HomeAction.ConfirmarExclusaoModal(modalState.ponto.id, "Removido pelo usuário")) },
            onDismiss = { viewModel.onAction(HomeAction.FecharExclusaoModal) }
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

    // Modal de Registro Unificado
    uiState.registrarPontoModal?.let { modalState ->
        RegistrarPontoModal(
            state = modalState,
            proximoTipo = uiState.proximoTipo,
            nsrHabilitado = uiState.nsrHabilitado,
            tipoNsr = uiState.tipoNsr,
            fotoHabilitada = uiState.fotoHabilitada,
            fotoObrigatoria = uiState.fotoObrigatoria,
            configLocalizacaoHabilitada = uiState.localizacaoHabilitada,
            onNsrChange = { viewModel.onAction(HomeAction.AtualizarNsrRegistroModal(it)) },
            onCapturarFoto = { viewModel.onAction(HomeAction.AbrirFotoSourceDialog) },
            onRemoverFoto = { viewModel.onAction(HomeAction.AtualizarFotoRegistroModal(null)) },
            onCapturarLocalizacao = { viewModel.onAction(HomeAction.CapturarLocalizacaoRegistroModal) },
            onAbrirTimePicker = { viewModel.onAction(HomeAction.AbrirTimePickerRegistroModal) },
            onFecharTimePicker = { viewModel.onAction(HomeAction.FecharTimePickerRegistroModal) },
            onHoraSelecionada = { viewModel.onAction(HomeAction.AtualizarHoraRegistroModal(it)) },
            onConfirmar = { viewModel.onAction(HomeAction.ConfirmarRegistroPontoModal) },
            onDismiss = { viewModel.onAction(HomeAction.FecharRegistrarPontoModal) }
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

private fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Data selecionada
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onAction(HomeAction.AbrirDatePicker) }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            uiState.isHoje -> "Hoje, ${uiState.dataSelecionada.format(HomeUiState.formatterDataCurta)}"
                            uiState.isOntem -> "Ontem, ${uiState.dataSelecionada.format(HomeUiState.formatterDataCurta)}"
                            uiState.isAmanha -> "Amanhã, ${uiState.dataSelecionada.format(HomeUiState.formatterDataCurta)}"
                            else -> uiState.dataSelecionada.format(HomeUiState.formatterDataCompleta)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Resumo do Dia
            ResumoCard(
                resumoDia = uiState.resumoDia,
                bancoHoras = uiState.bancoHoras,
                modifier = Modifier.fillMaxWidth()
            )

            // Próximo Ponto (Botão Registrar)
            ProximoPontoCard(
                proximo = uiState.proximoTipo,
                horaAtual = uiState.horaAtual,
                onClick = {
                    if (!uiState.isFuturo) {
                        onAction(HomeAction.RegistrarPontoAgora)
                    }
                },
                habilitado = !uiState.isFuturo && uiState.empregoAtivo != null,
                modifier = Modifier.fillMaxWidth()
            )

            // Banners de feriado ou ausência (abaixo dos cards principais)
            FeriadoBanner(feriados = uiState.feriadosDoDia)
            uiState.ausenciaDoDia?.let { ausencia ->
                AusenciaBanner(ausencia = ausencia)
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Lista de pontos (Scrollable)
        Box(modifier = Modifier.weight(1f)) {
            if (uiState.pontosHoje.isEmpty() && !uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhum registro para esta data",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.resumoDia.intervalos,
                        key = { it.entrada.id }
                    ) { intervalo ->
                        IntervaloCard(
                            intervalo = intervalo,
                            mostrarContadorTempoReal = uiState.isHoje,
                            mostrarNsr = uiState.nsrHabilitado,
                            onEditar = { onAction(HomeAction.AbrirEdicaoModal(it)) },
                            onExcluir = { onAction(HomeAction.AbrirExclusaoModal(it)) },
                            onVerFoto = { onAction(HomeAction.AbrirFotoModal(it)) },
                            onVerLocalizacao = { onAction(HomeAction.AbrirLocalizacaoModal(it)) }
                        )
                    }
                }
            }
        }
    }
}
