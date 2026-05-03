// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.usecase.feriado.VerificarDiaEspecialUseCase
import br.com.tlmacedo.meuponto.domain.model.inconsistencia.StatusDiaResumo
import br.com.tlmacedo.meuponto.presentation.components.AlertaInlineCard
import br.com.tlmacedo.meuponto.presentation.components.AusenciaBanner
import br.com.tlmacedo.meuponto.presentation.components.CicloBanner
import br.com.tlmacedo.meuponto.presentation.components.TipoAlertaInline
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
import br.com.tlmacedo.meuponto.presentation.components.foto.ComprovanteImagePicker
import br.com.tlmacedo.meuponto.presentation.components.theme.ThemedBackground
import br.com.tlmacedo.meuponto.presentation.screen.camera.CameraCaptureScreen
import br.com.tlmacedo.meuponto.presentation.screen.home.components.FechamentoCicloDialog
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import br.com.tlmacedo.meuponto.util.findActivity
import br.com.tlmacedo.meuponto.util.foto.DocumentScannerWrapper
import br.com.tlmacedo.meuponto.util.helper.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.helper.toLocalDateFromDatePicker
import coil.compose.AsyncImage
import java.time.LocalDate

/**
 * Tela principal do aplicativo Meu Ponto.
 * Padronizada com separação entre Screen (Estado/Efeitos) e Content (Stateless UI).
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 12.1.0 - Refatoração Screen/Content e Integração OCR
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    dataSelecionadaInicial: String? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onNavigateToHistorico: () -> Unit = {},
    onNavigateToConfiguracoes: () -> Unit = {},
    onNavigateToEditarPonto: (Long) -> Unit = {},
    onNavigateToNovoEmprego: () -> Unit = {},
    onNavigateToEditarEmprego: (Long) -> Unit = {},
    onNavigateToEditarJornada: (Long) -> Unit = {},
    onNavigateToHistoricoCiclos: () -> Unit = {},
    onNavigateToFotoVisualizacao: (Long) -> Unit = {}
) {
    var fabMenuExpandido by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scanResult =
                com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent(
                    result.data
                )
            scanResult?.pages?.firstOrNull()?.imageUri?.let { uri ->
                viewModel.onAction(HomeAction.SelecionarFotoComprovante(uri, FotoOrigem.CAMERA))
            }
        }
    }

    val docScanner = remember { DocumentScannerWrapper(context) }

    // ══════════════════════════════════════════════════════════════════════
    // EFEITOS E EVENTOS
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
                    viewModel.onAction(HomeAction.SetErroInline(event.mensagem))
                }

                is HomeUiEvent.NavegarParaHistorico -> onNavigateToHistorico()
                is HomeUiEvent.NavegarParaConfiguracoes -> onNavigateToConfiguracoes()
                is HomeUiEvent.NavegarParaEditarPonto -> onNavigateToEditarPonto(event.pontoId)
                is HomeUiEvent.EmpregoTrocado -> snackbarHostState.showSnackbar("Emprego alterado: ${event.nomeEmprego}")
                is HomeUiEvent.NavegarParaNovoEmprego -> onNavigateToNovoEmprego()
                is HomeUiEvent.NavegarParaEditarEmprego -> onNavigateToEditarEmprego(event.empregoId)
                is HomeUiEvent.NavegarParaHistoricoCiclos -> onNavigateToHistoricoCiclos()
                is HomeUiEvent.NavegarParaEditarJornada -> onNavigateToEditarJornada(event.empregoId)
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
    // UI COMPOSITION
    // ══════════════════════════════════════════════════════════════════════

    if (uiState.showCameraCapture) {
        CameraCaptureScreen(
            onImageCaptured = { uri ->
                viewModel.onAction(HomeAction.SelecionarFotoComprovante(uri, FotoOrigem.CAMERA))
                viewModel.onAction(HomeAction.FecharCameraCapture)
            },
            onBack = { viewModel.onAction(HomeAction.FecharCameraCapture) }
        )
    } else {
        HomeDialogs(
            uiState = uiState,
            onAction = viewModel::onAction,
            scannerLauncher = scannerLauncher,
            docScanner = docScanner
        )

        ThemedBackground(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                floatingActionButton = {
                    Box {
                        FloatingActionButton(
                            onClick = { fabMenuExpandido = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar"
                            )
                        }

                        DropdownMenu(
                            expanded = fabMenuExpandido,
                            onDismissRequest = { fabMenuExpandido = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Lançar ausência") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.NoteAdd,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    fabMenuExpandido = false
                                    viewModel.onAction(HomeAction.AdicionarAusencia)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Lançar feriado") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    fabMenuExpandido = false
                                    viewModel.onAction(HomeAction.AdicionarFeriado)
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                topBar = {
                    MeuPontoTopBar(
                        title = "Meu Ponto",
                        subtitle = uiState.empregoAtivo?.apelido?.uppercase(),
                        logo = uiState.empregoAtivo?.logo,
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
                        onNavigateToFotoVisualizacao = onNavigateToFotoVisualizacao,
                        modifier = Modifier.padding(paddingValues),
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        verificarDiaEspecialUseCase = viewModel.verificarDiaEspecialUseCase,
                        ausenciaRepository = viewModel.ausenciaRepository
                    )
                }
            }
        }
    }
}


/**
 * Componente interno para gerenciar todos os diálogos e modais da Home.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeDialogs(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    scannerLauncher: ActivityResultLauncher<IntentSenderRequest>,
    docScanner: DocumentScannerWrapper
) {
    LocalContext.current
    // 1. Date Picker
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataSelecionada.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onAction(HomeAction.FecharDatePicker) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onAction(HomeAction.SelecionarData(it.toLocalDateFromDatePicker())) }
                    onAction(HomeAction.FecharDatePicker)
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(HomeAction.FecharDatePicker) }) { Text("Cancelar") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // 2. Fechamento de Ciclo
    if (uiState.showFechamentoCicloDialog) {
        (uiState.estadoCiclo as? EstadoCiclo.Pendente)?.let { estadoPendente ->
            FechamentoCicloDialog(
                estadoCiclo = estadoPendente,
                onConfirmar = {
                    onAction(
                        HomeAction.ConfirmarFechamentoCiclo(
                            estadoPendente.ciclo.saldoAtualMinutos.toLong(),
                            "Fechamento de ciclo"
                        )
                    )
                },
                onCancelar = { onAction(HomeAction.FecharDialogFechamentoCiclo) }
            )
        }
    }

    // 3. Image Picker ( Launcher Helper )
    ComprovanteImagePicker(
        showSourceDialog = uiState.showFotoSourceDialog,
        onDismissSourceDialog = { onAction(HomeAction.FecharFotoSourceDialog) },
        cameraUri = uiState.cameraUri,
        onCameraResult = { success, origem -> if (success) onAction(HomeAction.ConfirmarFotoCamera) },
        onGalleryResult = { uri, origem ->
            uri?.let {
                onAction(
                    HomeAction.SelecionarFotoComprovante(
                        it,
                        origem
                    )
                )
            }
        },
        onPermissionDenied = { onAction(HomeAction.MostrarMensagem(it)) },
        onLaunchCustomCamera = { onAction(HomeAction.AbrirCameraCapture) },
        docScanner = docScanner,
        scannerLauncher = scannerLauncher
    )

    // 4. Modais de Ponto
    uiState.edicaoModal?.let { modal ->
        EdicaoModal(
            ponto = modal.ponto,
            tipoDescricao = modal.tipoDescricao,
            isSaving = modal.isSaving,
            nsrHabilitado = uiState.nsrHabilitado,
            tipoNsr = uiState.tipoNsr,
            fotoHabilitada = uiState.fotoHabilitada,
            fotoUri = modal.fotoUri,
            fotoPathAbsoluto = modal.fotoPathAbsoluto,
            fotoRemovida = modal.fotoRemovida,
            isProcessingOcr = modal.isProcessingOcr,
            onCapturarFoto = { onAction(HomeAction.AbrirFotoSourceDialog) },
            onRemoverFoto = { onAction(HomeAction.RemoverFotoEdicaoModal) },
            onReprocessarOcr = { onAction(HomeAction.ReprocessarOcrEdicaoModal) },
            onConfirmar = { h, n, m, d, o ->
                onAction(
                    HomeAction.SalvarEdicaoModal(
                        modal.ponto.id, h, n, m, d, o
                    )
                )
            },
            onDismiss = { onAction(HomeAction.FecharEdicaoModal) }
        )
    }

    uiState.exclusaoModal?.let { modal ->
        ExclusaoModal(
            ponto = modal.ponto, tipoDescricao = modal.tipoDescricao, isDeleting = modal.isDeleting,
            onConfirmar = {
                onAction(
                    HomeAction.ConfirmarExclusaoModal(
                        modal.ponto.id,
                        "Removido pelo usuário"
                    )
                )
            },
            onDismiss = { onAction(HomeAction.FecharExclusaoModal) }
        )
    }

    uiState.localizacaoModal?.let { modal ->
        LocalizacaoModal(
            ponto = modal.ponto,
            tipoDescricao = modal.tipoDescricao,
            onDismiss = { onAction(HomeAction.FecharLocalizacaoModal) })
    }

    uiState.fotoModal?.let { modal ->
        FotoPontoModal(
            ponto = modal.ponto,
            tipoDescricao = modal.tipoDescricao,
            fotoPath = modal.fotoPath,
            onDismiss = { onAction(HomeAction.FecharFotoModal) },
            onSalvarFoto = { id, path -> onAction(HomeAction.SalvarFotoModal(id, path)) })
    }

    uiState.registrarPontoModal?.let { modal ->
        RegistrarPontoModal(
            state = modal,
            proximoTipo = uiState.proximoTipo,
            nsrHabilitado = uiState.nsrHabilitado,
            tipoNsr = uiState.tipoNsr,
            fotoHabilitada = uiState.fotoHabilitada,
            fotoObrigatoria = uiState.fotoObrigatoria,
            configLocalizacaoHabilitada = uiState.localizacaoHabilitada,
            comentarioHabilitado = uiState.configuracaoEmprego?.comentarioHabilitado == true,
            comentarioObrigatorio = modal.isObservacaoObrigatoria,
            onNsrChange = { onAction(HomeAction.AtualizarNsrRegistroModal(it)) },
            onObservacaoChange = { onAction(HomeAction.AtualizarObservacaoRegistroModal(it)) },
            onCapturarFoto = { onAction(HomeAction.AbrirFotoSourceDialog) },
            onRemoverFoto = { onAction(HomeAction.AtualizarFotoRegistroModal(null)) },
            onReprocessarOcr = { onAction(HomeAction.ReprocessarOcrRegistroModal) },
            onCapturarLocalizacao = { onAction(HomeAction.CapturarLocalizacaoRegistroModal) },
            onAbrirTimePicker = { onAction(HomeAction.AbrirTimePickerRegistroModal) },
            onFecharTimePicker = { onAction(HomeAction.FecharTimePickerRegistroModal) },
            onHoraSelecionada = { onAction(HomeAction.AtualizarHoraRegistroModal(it)) },
            onConfirmar = { onAction(HomeAction.ConfirmarRegistroPontoModal) },
            onDismiss = { onAction(HomeAction.FecharRegistrarPontoModal) }
        )
    }
}


@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    onNavigateToFotoVisualizacao: (Long) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    verificarDiaEspecialUseCase: VerificarDiaEspecialUseCase?,
    ausenciaRepository: AusenciaRepository?,
) {
    // Diálogos ficam fora da lista para não dependerem do scroll
    uiState.ausenciaParaVisualizarAnexo?.let { ausencia ->
        Dialog(
            onDismissRequest = {
                onAction(HomeAction.FecharVisualizacaoAnexoAusencia)
            }
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Comprovante da ausência",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    AsyncImage(
                        model = ausencia.imagemUri,
                        contentDescription = "Comprovante da ausência",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 240.dp, max = 520.dp),
                        contentScale = ContentScale.Fit
                    )

                    TextButton(
                        onClick = {
                            onAction(HomeAction.FecharVisualizacaoAnexoAusencia)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Fechar")
                    }
                }
            }
        }
    }

    uiState.ausenciaParaRemoverImagem?.let { ausencia ->
        AlertDialog(
            onDismissRequest = {
                onAction(HomeAction.CancelarRemocaoImagemAusencia)
            },
            title = {
                Text("Remover comprovante")
            },
            text = {
                Text("Deseja remover a imagem anexada desta ausência?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(HomeAction.ConfirmarRemocaoImagemAusencia(ausencia))
                    }
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onAction(HomeAction.CancelarRemocaoImagemAusencia)
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    uiState.ausenciaParaExcluir?.let { ausencia ->
        AlertDialog(
            onDismissRequest = {
                onAction(HomeAction.CancelarExcluirAusencia)
            },
            title = {
                Text("Excluir ausência")
            },
            text = {
                Text("Tem certeza que deseja excluir esta ausência? Esta ação não poderá ser desfeita.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(HomeAction.ConfirmarExcluirAusencia(ausencia))
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onAction(HomeAction.CancelarExcluirAusencia)
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        CicloBanner(
            uiState.estadoCiclo,
            { onAction(HomeAction.AbrirDialogFechamentoCiclo) },
            { onAction(HomeAction.NavegarParaHistoricoCiclos) }
        )

        // Área fixa: data + resumo + próximo ponto
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onAction(HomeAction.DiaAnterior) },
                    enabled = uiState.podeNavegaAnterior
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Anterior",
                        modifier = Modifier.size(20.dp),
                        tint = if (uiState.podeNavegaAnterior) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(0.3f)
                        }
                    )
                }

                TextButton(
                    onClick = { onAction(HomeAction.AbrirDatePicker) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))

                    Text(
                        uiState.dataFormatada,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (uiState.pontosHoje.any { it.dataAutoFilled }) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Extraído do comprovante",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onAction(HomeAction.ProximoDia) },
                    enabled = uiState.podeNavegarProximo
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Próximo",
                        modifier = Modifier.size(20.dp),
                        tint = if (uiState.podeNavegarProximo) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(0.3f)
                        }
                    )
                }
            }

            ResumoCard(
                resumoDia = uiState.resumoDia,
                bancoHoras = uiState.bancoHoras,
                horaAtual = uiState.horaAtual,
                versaoJornada = uiState.versaoJornadaAtual,
                onEditarJornada = { onAction(HomeAction.NavegarParaEditarJornada) },
                horasTrabalhadasCalculadasMinutos = uiState.resumoDiaCompleto?.horasTrabalhadasMinutos,
                tempoAbonadoMinutos = uiState.resumoDiaCompleto?.tempoAbonadoMinutos ?: 0,
                saldoDiaCalculadoMinutos = uiState.resumoDiaCompleto?.saldoDiaMinutos,
                modifier = Modifier.fillMaxWidth()
            )

            ProximoPontoCard(
                uiState.proximoTipo,
                uiState.horaAtual,
                { if (!uiState.isFuturo) onAction(HomeAction.RegistrarPontoAgora) },
                modifier = Modifier.fillMaxWidth(),
                habilitado = !uiState.isFuturo && uiState.empregoAtivo != null
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Área rolável: feriados, ausências e pontos
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.feriadosDoDia.isNotEmpty()) {
                item(key = "feriados") {
                    FeriadoBanner(uiState.feriadosDoDia)
                }
            }

            items(
                items = uiState.ausenciasDoDia,
                key = { ausencia -> "ausencia-${ausencia.id}" }
            ) { ausencia ->
                AusenciaBanner(
                    ausencia = ausencia,
                    metadataFerias = if (ausencia.tipo == TipoAusencia.Ferias) {
                        uiState.metadataFerias
                    } else {
                        null
                    },
                    verificarDiaEspecialUseCase = verificarDiaEspecialUseCase,
                    ausenciaRepository = ausenciaRepository,
                    modifier = Modifier.fillMaxWidth(),
                    onVerAnexo = {
                        onAction(HomeAction.VerAnexoAusencia(ausencia))
                    },
                    onAdicionarImagemCamera = {
                        onAction(HomeAction.AdicionarImagemAusenciaCamera(ausencia))
                    },
                    onAdicionarImagemGaleria = {
                        onAction(HomeAction.AdicionarImagemAusenciaGaleria(ausencia))
                    },
                    onRemoverImagem = {
                        onAction(HomeAction.SolicitarRemocaoImagemAusencia(ausencia))
                    },
                    onExcluir = {
                        onAction(HomeAction.SolicitarExcluirAusencia(ausencia))
                    }
                )
            }

            // Alerta de erro inline (dismissável — vem de MostrarErro)
            uiState.erro?.let { erro ->
                item(key = "erro_inline") {
                    AlertaInlineCard(
                        tipo = TipoAlertaInline.BLOQUEANTE,
                        mensagem = erro,
                        onDismiss = { onAction(HomeAction.LimparErro) }
                    )
                }
            }

            // Inconsistências do dia (persistentes — derivadas do resumoDia)
            val inconsistencias = uiState.resumoDia.listaInconsistencias
            if (inconsistencias.isNotEmpty() && !uiState.isFuturo) {
                val tipoAlerta = when (uiState.resumoDia.status) {
                    StatusDiaResumo.FALTA -> TipoAlertaInline.BLOQUEANTE
                    StatusDiaResumo.PENDENTE_JUSTIFICATIVA -> TipoAlertaInline.PENDENTE
                    StatusDiaResumo.NEGATIVO -> TipoAlertaInline.ATENCAO
                    else -> TipoAlertaInline.ATENCAO
                }
                items(
                    items = inconsistencias,
                    key = { msg -> "inconsistencia_$msg" }
                ) { msg ->
                    AlertaInlineCard(
                        tipo = tipoAlerta,
                        mensagem = msg
                    )
                }
            }

            if (
                uiState.pontosHoje.isEmpty() &&
                uiState.ausenciasDoDia.isEmpty() &&
                uiState.feriadosDoDia.isEmpty() &&
                !uiState.isLoading
            ) {
                item(key = "empty") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(0.3f)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Nenhum registro para esta data",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            items(
                items = uiState.intervalos,
                key = { intervalo -> "intervalo-${intervalo.entrada.id}" }
            ) { intervalo ->
                IntervaloCard(
                    intervalo = intervalo,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    mostrarContadorTempoReal = uiState.isHoje,
                    mostrarNsr = uiState.nsrHabilitado,
                    onEditar = { ponto -> onAction(HomeAction.AbrirEdicaoModal(ponto)) },
                    onExcluir = { ponto -> onAction(HomeAction.AbrirExclusaoModal(ponto)) },
                    onVerFoto = { ponto -> onNavigateToFotoVisualizacao(ponto.id) },
                    onVerLocalizacao = { ponto ->
                        onAction(HomeAction.AbrirLocalizacaoModal(ponto))
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val hoje = LocalDate.now()
    val uiState =
        HomeUiState(dataSelecionada = hoje, resumoDia = ResumoDia(data = hoje), isLoading = false)
    MeuPontoTheme {
        HomeContent(
            uiState = uiState,
            onAction = {},
            onNavigateToFotoVisualizacao = {},
            verificarDiaEspecialUseCase = null,
            ausenciaRepository = null
        )
    }
}
