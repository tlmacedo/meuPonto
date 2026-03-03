// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/versoes/EditarVersaoScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import kotlinx.coroutines.flow.collectLatest

/**
 * Tela de edição de versão de jornada.
 *
 * @author Thiago
 * @since 4.0.0
 */
@Composable
fun EditarVersaoScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHorarios: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditarVersaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is EditarVersaoEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is EditarVersaoEvent.SalvoComSucesso -> {
                    onNavigateBack()
                }
                is EditarVersaoEvent.Voltar -> {
                    onNavigateBack()
                }
                is EditarVersaoEvent.NavegarParaHorarios -> {
                    onNavigateToHorarios(evento.versaoId)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = uiState.tituloTela,
                showBackButton = true,
                onBackClick = { viewModel.onAction(EditarVersaoAction.Cancelar) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
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
            EditarVersaoContent(
                uiState = uiState,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarVersaoContent(
    uiState: EditarVersaoUiState,
    onAction: (EditarVersaoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Date Picker - Data Início
    if (uiState.showDataInicioPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataInicio.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onAction(EditarVersaoAction.MostrarDataInicioPicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()
                    if (date != null) {
                        onAction(EditarVersaoAction.AlterarDataInicio(date))
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditarVersaoAction.MostrarDataInicioPicker(false)) }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Date Picker - Data Fim
    if (uiState.showDataFimPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataFim?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onAction(EditarVersaoAction.MostrarDataFimPicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()
                    onAction(EditarVersaoAction.AlterarDataFim(date))
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditarVersaoAction.MostrarDataFimPicker(false)) }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // DADOS BÁSICOS
        item {
            FormSection(
                title = "Dados da Versão",
                icon = Icons.Default.Info,
                isExpanded = uiState.secaoExpandida == SecaoVersao.DADOS_BASICOS,
                onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.DADOS_BASICOS)) }
            ) {
                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = { onAction(EditarVersaoAction.AlterarDescricao(it)) },
                    label = { Text("Descrição (opcional)") },
                    placeholder = { Text("Ex: Horário de verão, Escala nova...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Data Início
                OutlinedTextField(
                    value = uiState.dataInicioFormatada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data de Início") },
                    trailingIcon = {
                        IconButton(onClick = { onAction(EditarVersaoAction.MostrarDataInicioPicker(true)) }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(EditarVersaoAction.MostrarDataInicioPicker(true)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Data Fim
                OutlinedTextField(
                    value = uiState.dataFimFormatada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data de Fim") },
                    supportingText = { Text("Deixe vazio para versão vigente") },
                    trailingIcon = {
                        IconButton(onClick = { onAction(EditarVersaoAction.MostrarDataFimPicker(true)) }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(EditarVersaoAction.MostrarDataFimPicker(true)) }
                )
            }
        }

        // CONFIGURAÇÕES DE JORNADA
        item {
            FormSection(
                title = "Configurações de Jornada",
                icon = Icons.Default.Schedule,
                isExpanded = uiState.secaoExpandida == SecaoVersao.JORNADA,
                onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.JORNADA)) }
            ) {
                MinutesSliderWithSteppers(
                    label = "Jornada Máxima Diária",
                    value = uiState.jornadaMaximaDiariaMinutos,
                    onValueChange = { onAction(EditarVersaoAction.AlterarJornadaMaxima(it)) },
                    valueRange = 360..720,
                    sliderStep = 30,
                    formatAsHours = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                MinutesSliderWithSteppers(
                    label = "Intervalo Mínimo Interjornada",
                    value = uiState.intervaloMinimoInterjornadaMinutos,
                    onValueChange = { onAction(EditarVersaoAction.AlterarIntervaloInterjornada(it)) },
                    valueRange = 540..780,
                    sliderStep = 30,
                    formatAsHours = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                MinutesSliderWithSteppers(
                    label = "Tolerância Intervalo (+)",
                    value = uiState.toleranciaIntervaloMaisMinutos,
                    onValueChange = { onAction(EditarVersaoAction.AlterarToleranciaIntervalo(it)) },
                    valueRange = 0..30,
                    sliderStep = 5,
                    formatAsHours = false,
                    suffix = "min"
                )
            }
        }

        // HORÁRIOS POR DIA
        item {
            FormSection(
                title = "Horários por Dia da Semana",
                icon = Icons.Default.Timer,
                isExpanded = uiState.secaoExpandida == SecaoVersao.HORARIOS,
                onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.HORARIOS)) }
            ) {
                // Resumo dos horários
                if (uiState.horarios.isNotEmpty()) {
                    uiState.horarios.take(3).forEach { horario ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = horario.diaSemana.descricao,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (horario.ativo) horario.cargaHorariaFormatada else "Folga",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (horario.ativo) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    
                    if (uiState.horarios.size > 3) {
                        Text(
                            text = "... e mais ${uiState.horarios.size - 3} dias",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { onAction(EditarVersaoAction.ConfigurarHorarios) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurar Horários por Dia")
                }
            }
        }

        // BOTÃO SALVAR
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onAction(EditarVersaoAction.Salvar) },
                enabled = uiState.formularioValido && !uiState.isSaving,
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
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(uiState.textoBotaoSalvar)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MinutesSliderWithSteppers(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    sliderStep: Int,
    formatAsHours: Boolean,
    suffix: String = ""
) {
    val displayValue = if (formatAsHours) {
        val h = value / 60
        val m = value % 60
        String.format("%02d:%02d", h, m)
    } else {
        if (suffix.isEmpty()) "$value" else "$value $suffix"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { onValueChange((value - sliderStep).coerceIn(valueRange)) },
                enabled = value > valueRange.first
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Diminuir")
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                steps = if (sliderStep > 0 && (valueRange.last - valueRange.first) > sliderStep)
                    ((valueRange.last - valueRange.first) / sliderStep) - 1
                else 0,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { onValueChange((value + sliderStep).coerceIn(valueRange)) },
                enabled = value < valueRange.last
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar")
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir"
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                    content()
                }
            }
        }
    }
}
