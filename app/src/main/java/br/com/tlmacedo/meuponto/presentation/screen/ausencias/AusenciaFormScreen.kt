// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciaFormScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Tela de formulário para criar/editar ausência.
 *
 * @author Thiago
 * @since 4.0.0
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

    // Eventos
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AusenciaFormUiEvent.Voltar -> onVoltar()
                is AusenciaFormUiEvent.SalvoComSucesso -> onSalvo()
                is AusenciaFormUiEvent.MostrarMensagem -> snackbarHostState.showSnackbar(event.mensagem)
                is AusenciaFormUiEvent.MostrarErro -> snackbarHostState.showSnackbar(event.mensagem)
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
                // Tipo de ausência
                Column {
                    Text(
                        text = "Tipo",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TipoAusenciaChip(
                        tipo = uiState.tipo,
                        onClick = { viewModel.onAction(AusenciaFormAction.AbrirTipoSelector) }
                    )
                }

                // Período
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Data início
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Data início",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.onAction(AusenciaFormAction.AbrirDatePickerInicio) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null
                            )
                            Text(
                                text = uiState.dataInicioFormatada,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    // Data fim
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Data fim",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.onAction(AusenciaFormAction.AbrirDatePickerFim) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null
                            )
                            Text(
                                text = uiState.dataFimFormatada,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Total de dias
                Text(
                    text = "Total: ${uiState.totalDias} ${if (uiState.totalDias == 1) "dia" else "dias"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                // Descrição
                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = { viewModel.onAction(AusenciaFormAction.AtualizarDescricao(it)) },
                    label = { Text("Descrição (opcional)") },
                    placeholder = { Text("Ex: Férias de verão") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Observação
                OutlinedTextField(
                    value = uiState.observacao,
                    onValueChange = { viewModel.onAction(AusenciaFormAction.AtualizarObservacao(it)) },
                    label = { Text("Observação (opcional)") },
                    placeholder = { Text("Informações adicionais") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Erro
                uiState.erro?.let { erro ->
                    Text(
                        text = erro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botão salvar
                Button(
                    onClick = { viewModel.onAction(AusenciaFormAction.Salvar) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isFormValido && !uiState.isSalvando
                ) {
                    if (uiState.isSalvando) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(uiState.textoBotaoSalvar)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogWrapper(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
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
