// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.data.service.LocationService
import br.com.tlmacedo.meuponto.domain.model.Localizacao
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.presentation.components.LocationPickerDialog
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.TimePickerDialog
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import java.time.ZoneOffset

/**
 * Tela de edição de ponto.
 *
 * @author Thiago
 * @since 3.5.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPontoScreen(
    viewModel: EditPontoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Coleta eventos
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is EditPontoUiEvent.Salvo -> snackbarHostState.showSnackbar(event.mensagem)
                is EditPontoUiEvent.Excluido -> snackbarHostState.showSnackbar(event.mensagem)
                is EditPontoUiEvent.Erro -> snackbarHostState.showSnackbar(event.mensagem)
                EditPontoUiEvent.Voltar -> onNavigateBack()
            }
        }
    }

    // Dialogs
    if (uiState.showLocationPicker) {
        val context = LocalContext.current.applicationContext
        val locationService = remember(context) {
            LocationService(context)
        }

        LocationPickerDialog(
            localizacaoInicial = if (uiState.temLocalizacao) {
                Localizacao(
                    latitude = uiState.latitude!!,
                    longitude = uiState.longitude!!,
                    endereco = uiState.endereco
                )
            } else null,
            onConfirm = { localizacao ->
                viewModel.onAction(
                    EditPontoAction.AtualizarLocalizacao(
                        latitude = localizacao.latitude,
                        longitude = localizacao.longitude,
                        endereco = localizacao.endereco
                    )
                )
            },
            onDismiss = { viewModel.onAction(EditPontoAction.FecharLocationPicker) },
            locationService = locationService
        )
    }
    if (uiState.showTimePicker) {
        TimePickerDialog(
            titulo = "Selecionar Horário",
            horaInicial = uiState.hora,
            onConfirm = { hora -> viewModel.onAction(EditPontoAction.AtualizarHora(hora)) },
            onDismiss = { viewModel.onAction(EditPontoAction.FecharTimePicker) }
        )
    }

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.data
                .atStartOfDay()
                .atZone(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { viewModel.onAction(EditPontoAction.FecharDatePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.onAction(EditPontoAction.AtualizarData(selectedDate))
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(EditPontoAction.FecharDatePicker) }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            tipoPonto = uiState.tipoPonto,
            horaFormatada = uiState.horaFormatada,
            motivo = uiState.motivo,
            onMotivoChange = { viewModel.onAction(EditPontoAction.AtualizarMotivo(it)) },
            onConfirm = { viewModel.onAction(EditPontoAction.ConfirmarExclusao) },
            onDismiss = { viewModel.onAction(EditPontoAction.CancelarExclusao) }
        )
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Editar ${uiState.tipoPonto.descricao}",
                showBackButton = true,
                onBackClick = { viewModel.onAction(EditPontoAction.Cancelar) },
                actions = {
                    IconButton(
                        onClick = { viewModel.onAction(EditPontoAction.SolicitarExclusao) },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
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
            EditPontoContent(
                uiState = uiState,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EditPontoContent(
    uiState: EditPontoUiState,
    onAction: (EditPontoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isEntrada = uiState.tipoPonto == TipoPonto.ENTRADA
    val corTipo = if (isEntrada) EntradaColor else SaidaColor
    val iconeTipo = if (isEntrada) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header com tipo do ponto
        Card(
            colors = CardDefaults.cardColors(
                containerColor = corTipo.copy(alpha = 0.1f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = iconeTipo,
                    contentDescription = null,
                    tint = corTipo,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = uiState.tipoPonto.descricao,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = corTipo
                    )
                    Text(
                        text = uiState.dataCompletaFormatada,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Seção: Horário
        SectionTitle("Horário")

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Data
            OutlinedCard(
                onClick = { onAction(EditPontoAction.AbrirDatePicker) },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Data",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.dataFormatada,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Hora
            OutlinedCard(
                onClick = { onAction(EditPontoAction.AbrirTimePicker) },
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Hora",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.horaFormatada,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Seção: NSR (se habilitado)
        if (uiState.habilitarNsr) {
            SectionTitle("NSR - Número Sequencial de Registro")

            OutlinedTextField(
                value = uiState.nsr,
                onValueChange = { onAction(EditPontoAction.AtualizarNsr(it)) },
                label = { Text("NSR") },
                placeholder = {
                    Text(
                        when (uiState.tipoNsr) {
                            TipoNsr.NUMERICO -> "Ex: 000123"
                            TipoNsr.ALFANUMERICO -> "Ex: ABC123"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (uiState.tipoNsr) {
                        TipoNsr.NUMERICO -> KeyboardType.Number
                        TipoNsr.ALFANUMERICO -> KeyboardType.Text
                    }
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Numbers,
                        contentDescription = null
                    )
                },
                isError = uiState.erroNsr != null,
                supportingText = uiState.erroNsr?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Seção: Localização (se habilitado)
        if (uiState.habilitarLocalizacao) {
            SectionTitle("Localização")

            OutlinedCard(
                onClick = { onAction(EditPontoAction.AbrirLocationPicker) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.temLocalizacao) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = if (uiState.temLocalizacao) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Localização",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.localizacaoFormatada,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (uiState.temLocalizacao) {
                        IconButton(onClick = { onAction(EditPontoAction.LimparLocalizacao) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar localização"
                            )
                        }
                    }
                }
            }

            if (uiState.erroLocalizacao != null) {
                Text(
                    text = uiState.erroLocalizacao!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Seção: Observação
        SectionTitle("Observação")

        OutlinedTextField(
            value = uiState.observacao,
            onValueChange = { onAction(EditPontoAction.AtualizarObservacao(it)) },
            label = { Text("Observação (opcional)") },
            placeholder = { Text("Adicione uma observação...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Notes,
                    contentDescription = null
                )
            },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        // Seção: Motivo da Edição (obrigatório)
        SectionTitle("Motivo da Edição *", required = true)

        OutlinedTextField(
            value = uiState.motivo,
            onValueChange = { onAction(EditPontoAction.AtualizarMotivo(it)) },
            label = { Text("Motivo") },
            placeholder = { Text("Informe o motivo da alteração (mín. 5 caracteres)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null
                )
            },
            isError = uiState.motivo.isNotEmpty() && !uiState.motivoValido,
            supportingText = {
                if (uiState.motivo.isNotEmpty() && !uiState.motivoValido) {
                    Text(uiState.erroMotivo ?: "")
                } else {
                    Text("${uiState.motivo.length}/5 caracteres mínimos")
                }
            },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        // Resumo das alterações
        if (uiState.temAlteracoes) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alterações a serem salvas:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.alteracoesResumo,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão Salvar
        Button(
            onClick = { onAction(EditPontoAction.Salvar) },
            enabled = uiState.podeSalvar && uiState.temAlteracoes,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salvar Alterações")
        }

        // Erro geral
        uiState.erro?.let { erro ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = erro,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onAction(EditPontoAction.LimparErro) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    required: Boolean = false
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = if (required) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DeleteConfirmDialog(
    tipoPonto: TipoPonto,
    horaFormatada: String,
    motivo: String,
    onMotivoChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Excluir Ponto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Deseja realmente excluir o registro de ${tipoPonto.descricao} às $horaFormatada?"
                )
                Text(
                    "Esta ação não pode ser desfeita, mas ficará registrada no histórico de auditoria.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = motivo,
                    onValueChange = onMotivoChange,
                    label = { Text("Motivo da exclusão *") },
                    placeholder = { Text("Informe o motivo (mín. 5 caracteres)") },
                    isError = motivo.isNotEmpty() && motivo.length < 5,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = motivo.length >= 5
            ) {
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
