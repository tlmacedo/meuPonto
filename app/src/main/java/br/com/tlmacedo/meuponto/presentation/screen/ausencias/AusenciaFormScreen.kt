// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciaFormScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import br.com.tlmacedo.meuponto.presentation.components.DurationInputField
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.LocalTime

/**
 * Tela de formulário para criar/editar ausência.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.5.0 - Removido SubTipoFolga
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AusenciaFormScreen(
    onVoltar: () -> Unit,
    onSalvo: () -> Unit,
    viewModel: AusenciaFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Launchers para câmera e galeria
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // Implementar lógica de captura de foto
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onAction(AusenciaFormAction.SelecionarImagem(it.toString(), null))
        }
    }

    // Eventos
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AusenciaFormUiEvent.Voltar -> onVoltar()
                is AusenciaFormUiEvent.SalvoComSucesso -> onSalvo()
                is AusenciaFormUiEvent.MostrarMensagem -> snackbarHostState.showSnackbar(event.mensagem)
                is AusenciaFormUiEvent.MostrarErro -> snackbarHostState.showSnackbar(event.mensagem)
                is AusenciaFormUiEvent.AbrirCamera -> {
                    // TODO: Implementar captura de foto com FileProvider
                }
                is AusenciaFormUiEvent.AbrirGaleria -> {
                    galeriaLauncher.launch("image/*")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.tituloTela) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onAction(AusenciaFormAction.Cancelar) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ============================================================
                // TIPO DE AUSÊNCIA
                // ============================================================
                SectionTitle("Tipo de Ausência")
                TipoAusenciaChip(
                    tipo = uiState.tipo,
                    onClick = { viewModel.onAction(AusenciaFormAction.AbrirTipoSelector) }
                )

                // Card informativo sobre o impacto do tipo selecionado
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = uiState.tipo.impactoResumido,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = uiState.tipo.explicacaoImpacto,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ============================================================
                // TIPO DE FOLGA (para FOLGA)
                // ============================================================
                AnimatedVisibility(
                    visible = uiState.mostrarTipoFolga,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("Tipo de Folga")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TipoFolga.entries.forEach { tipoFolga ->
                                FilterChip(
                                    selected = uiState.tipoFolga == tipoFolga,
                                    onClick = {
                                        viewModel.onAction(AusenciaFormAction.SelecionarTipoFolga(tipoFolga))
                                    },
                                    label = { Text(tipoFolga.descricao) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Card informativo sobre o tipo de folga
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = uiState.tipoFolga.explicacao,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // ============================================================
                // PERÍODO (para tipos que usam período)
                // ============================================================
                AnimatedVisibility(
                    visible = uiState.usaPeriodo,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("Período")

                        // Seletor de modo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.modoPeriodo == ModoPeriodo.DATA_FINAL,
                                onClick = {
                                    viewModel.onAction(AusenciaFormAction.SelecionarModoPeriodo(ModoPeriodo.DATA_FINAL))
                                },
                                label = { Text("Data final") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.modoPeriodo == ModoPeriodo.QUANTIDADE_DIAS,
                                onClick = {
                                    viewModel.onAction(AusenciaFormAction.SelecionarModoPeriodo(ModoPeriodo.QUANTIDADE_DIAS))
                                },
                                label = { Text("Qtd. dias") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Data início
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Data início",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { viewModel.onAction(AusenciaFormAction.AbrirDatePickerInicio) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(uiState.dataInicioFormatada)
                                }
                            }

                            // Data fim ou quantidade de dias
                            Column(modifier = Modifier.weight(1f)) {
                                if (uiState.modoPeriodo == ModoPeriodo.DATA_FINAL) {
                                    Text(
                                        text = "Data fim",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.onAction(AusenciaFormAction.AbrirDatePickerFim) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(uiState.dataFimFormatada)
                                    }
                                } else {
                                    Text(
                                        text = "Quantidade de dias",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    QuantidadeDiasSelector(
                                        quantidade = uiState.quantidadeDias,
                                        onQuantidadeChange = {
                                            viewModel.onAction(AusenciaFormAction.AtualizarQuantidadeDias(it))
                                        }
                                    )
                                }
                            }
                        }

                        // Resumo do período
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${uiState.totalDias} ${if (uiState.totalDias == 1) "dia" else "dias"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // ============================================================
                // INTERVALO DE HORAS (para DECLARACAO)
                // ============================================================
                AnimatedVisibility(
                    visible = uiState.usaIntervaloHoras,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionTitle("Data e Horário")

                        // Data
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Data:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(80.dp)
                            )
                            OutlinedButton(
                                onClick = { viewModel.onAction(AusenciaFormAction.AbrirDatePickerInicio) }
                            ) {
                                Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(uiState.dataInicioFormatada)
                            }
                        }

                        // Hora início
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hora início:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(80.dp)
                            )
                            OutlinedButton(
                                onClick = { viewModel.onAction(AusenciaFormAction.AbrirTimePickerInicio) }
                            ) {
                                Icon(Icons.Default.AccessTime, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(uiState.horaInicioFormatada)
                            }
                        }

                        HorizontalDivider()

                        // Duração da declaração
                        DurationInputField(
                            totalMinutos = uiState.duracaoDeclaracaoHoras * 60 + uiState.duracaoDeclaracaoMinutos,
                            onValueChange = { totalMinutos ->
                                val horas = totalMinutos / 60
                                val minutos = totalMinutos % 60
                                viewModel.onAction(AusenciaFormAction.AtualizarDuracaoDeclaracao(horas, minutos))
                            },
                            label = "Tempo da declaração",
                            minValue = 1, // Mínimo 1 minuto
                            maxValue = 720, // Máximo 12 horas
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Duração do abono
                        val maxAbono = uiState.duracaoDeclaracaoHoras * 60 + uiState.duracaoDeclaracaoMinutos
                        DurationInputField(
                            totalMinutos = uiState.duracaoAbonoHoras * 60 + uiState.duracaoAbonoMinutos,
                            onValueChange = { totalMinutos ->
                                val horas = totalMinutos / 60
                                val minutos = totalMinutos % 60
                                viewModel.onAction(AusenciaFormAction.AtualizarDuracaoAbono(horas, minutos))
                            },
                            label = "Tempo que será abonado",
                            minValue = 0,
                            maxValue = maxAbono, // Não pode ser maior que a declaração
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Info card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Intervalo: ${uiState.horaInicioFormatada} - ${uiState.horaFimFormatada}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Duração: ${uiState.duracaoDeclaracaoFormatada} | Abono: ${uiState.duracaoAbonoFormatada}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // ============================================================
                // PERÍODO AQUISITIVO (para FERIAS)
                // ============================================================
                AnimatedVisibility(
                    visible = uiState.tipo == TipoAusencia.FERIAS,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        SectionTitle("Período Aquisitivo *")
                        OutlinedTextField(
                            value = uiState.periodoAquisitivo,
                            onValueChange = {
                                viewModel.onAction(AusenciaFormAction.AtualizarPeriodoAquisitivo(it))
                            },
                            placeholder = { Text(uiState.placeholderObservacao) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // ============================================================
                // OBSERVAÇÃO / MOTIVO
                // ============================================================
                Column {
                    SectionTitle(uiState.labelObservacao)
                    OutlinedTextField(
                        value = uiState.observacao,
                        onValueChange = {
                            viewModel.onAction(AusenciaFormAction.AtualizarObservacao(it))
                        },
                        placeholder = { Text(uiState.placeholderObservacao) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                // ============================================================
                // ANEXO DE IMAGEM (se permitido)
                // ============================================================
                AnimatedVisibility(
                    visible = uiState.permiteAnexo,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("Anexo (opcional)")

                        if (uiState.imagemUri != null) {
                            // Preview da imagem
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box {
                                    AsyncImage(
                                        model = uiState.imagemUri,
                                        contentDescription = "Anexo",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.onAction(AusenciaFormAction.RemoverImagem) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            "Remover anexo",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        } else {
                            // Botões para adicionar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.onAction(AusenciaFormAction.AbrirCamera) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Câmera")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.onAction(AusenciaFormAction.AbrirGaleria) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Image, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Galeria")
                                }
                            }
                        }
                    }
                }

                // ============================================================
                // ERRO
                // ============================================================
                uiState.erro?.let { erro ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = erro,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ============================================================
                // BOTÃO SALVAR
                // ============================================================
                Button(
                    onClick = { viewModel.onAction(AusenciaFormAction.Salvar) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isFormValido && !uiState.isSalvando
                ) {
                    if (uiState.isSalvando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(uiState.textoBotaoSalvar)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ========================================================================
    // DIALOGS E BOTTOM SHEETS
    // ========================================================================

    // Selector de tipo
    if (uiState.showTipoSelector) {
        TipoAusenciaSelector(
            tipoSelecionado = uiState.tipo,
            onTipoSelecionado = { viewModel.onAction(AusenciaFormAction.SelecionarTipo(it)) },
            onDismiss = { viewModel.onAction(AusenciaFormAction.FecharTipoSelector) }
        )
    }

    // Date picker início
    if (uiState.showDatePickerInicio) {
        DatePickerDialogWrapper(
            initialDate = uiState.dataInicio,
            onDateSelected = { viewModel.onAction(AusenciaFormAction.SelecionarDataInicio(it)) },
            onDismiss = { viewModel.onAction(AusenciaFormAction.FecharDatePickerInicio) }
        )
    }

    // Date picker fim
    if (uiState.showDatePickerFim) {
        DatePickerDialogWrapper(
            initialDate = uiState.dataFim,
            onDateSelected = { viewModel.onAction(AusenciaFormAction.SelecionarDataFim(it)) },
            onDismiss = { viewModel.onAction(AusenciaFormAction.FecharDatePickerFim) }
        )
    }

    // Time picker
    if (uiState.showTimePickerInicio) {
        TimePickerDialogWrapper(
            initialTime = uiState.horaInicio,
            onTimeSelected = { viewModel.onAction(AusenciaFormAction.SelecionarHoraInicio(it)) },
            onDismiss = { viewModel.onAction(AusenciaFormAction.FecharTimePickerInicio) }
        )
    }
}

// ============================================================================
// COMPONENTES AUXILIARES
// ============================================================================

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun QuantidadeDiasSelector(
    quantidade: Int,
    onQuantidadeChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { if (quantidade > 1) onQuantidadeChange(quantidade - 1) },
            enabled = quantidade > 1
        ) {
            Icon(Icons.Default.Remove, "Diminuir")
        }

        Text(
            text = quantidade.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = { if (quantidade < 365) onQuantidadeChange(quantidade + 1) },
            enabled = quantidade < 365
        ) {
            Icon(Icons.Default.Add, "Aumentar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogWrapper(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toDatePickerMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millis.toLocalDateFromDatePicker())
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogWrapper(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selecione o horário",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            TimePicker(state = timePickerState)

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("OK")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
