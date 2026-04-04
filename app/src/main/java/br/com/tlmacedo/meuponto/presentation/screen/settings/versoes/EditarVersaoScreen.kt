package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
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

    if (uiState.showDataInicioPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataInicio.atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onAction(EditarVersaoAction.MostrarDataInicioPicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onAction(EditarVersaoAction.AlterarDataInicio(selectedDate))
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(EditarVersaoAction.MostrarDataInicioPicker(false)) }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.showDataFimPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (uiState.dataFim ?: LocalDate.now()).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onAction(EditarVersaoAction.MostrarDataFimPicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onAction(EditarVersaoAction.AlterarDataFim(selectedDate))
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(EditarVersaoAction.AlterarDataFim(null)) }) {
                    Text("Limpar")
                }
                TextButton(onClick = { viewModel.onAction(EditarVersaoAction.MostrarDataFimPicker(false)) }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.showDataInicioCicloBancoPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (uiState.dataInicioCicloBancoAtual ?: LocalDate.now()).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onAction(EditarVersaoAction.MostrarDataInicioCicloBancoPicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onAction(EditarVersaoAction.AlterarDataInicioCicloBancoAtual(selectedDate))
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(EditarVersaoAction.AlterarDataInicioCicloBancoAtual(null)) }) {
                    Text("Limpar")
                }
                TextButton(onClick = { viewModel.onAction(EditarVersaoAction.MostrarDataInicioCicloBancoPicker(false)) }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = if (uiState.isNovaVersao) "Nova Versão" else "Editar Versão",
                showBackButton = true,
                onBackClick = { viewModel.onAction(EditarVersaoAction.Cancelar) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Erro desconhecido",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                EditarVersaoContent(
                    uiState = uiState,
                    onDescricaoChange = { viewModel.onAction(EditarVersaoAction.AlterarDescricao(it)) },
                    onDataInicioClick = { viewModel.onAction(EditarVersaoAction.MostrarDataInicioPicker(true)) },
                    onDataFimClick = { viewModel.onAction(EditarVersaoAction.MostrarDataFimPicker(true)) },
                    onJornadaMaximaChange = { viewModel.onAction(EditarVersaoAction.AlterarJornadaMaxima(it)) },
                    onIntervaloInterjornadaChange = { viewModel.onAction(EditarVersaoAction.AlterarIntervaloInterjornada(it)) },
                    onToleranciaIntervaloChange = { viewModel.onAction(EditarVersaoAction.AlterarToleranciaIntervalo(it)) },
                    onTurnoMaximoChange = { viewModel.onAction(EditarVersaoAction.AlterarTurnoMaximo(it)) },
                    onCargaHorariaDiariaChange = { viewModel.onAction(EditarVersaoAction.AlterarCargaHorariaDiaria(it)) },
                    onAcrescimoDiasPontesChange = { viewModel.onAction(EditarVersaoAction.AlterarAcrescimoDiasPontes(it)) },
                    onCargaHorariaSemanalChange = { viewModel.onAction(EditarVersaoAction.AlterarCargaHorariaSemanal(it)) },
                    onPrimeiroDiaSemanaChange = { viewModel.onAction(EditarVersaoAction.AlterarPrimeiroDiaSemana(it)) },
                    onDiaInicioFechamentoRHChange = { viewModel.onAction(EditarVersaoAction.AlterarDiaInicioFechamentoRH(it)) },
                    onZerarSaldoSemanalChange = { viewModel.onAction(EditarVersaoAction.AlterarZerarSaldoSemanal(it)) },
                    onZerarSaldoPeriodoRHChange = { viewModel.onAction(EditarVersaoAction.AlterarZerarSaldoPeriodoRH(it)) },
                    onOcultarSaldoTotalChange = { viewModel.onAction(EditarVersaoAction.AlterarOcultarSaldoTotal(it)) },
                    onBancoHorasHabilitadoChange = { viewModel.onAction(EditarVersaoAction.AlterarBancoHorasHabilitado(it)) },
                    onPeriodoBancoSemanasChange = { viewModel.onAction(EditarVersaoAction.AlterarPeriodoBancoSemanas(it)) },
                    onPeriodoBancoMesesChange = { viewModel.onAction(EditarVersaoAction.AlterarPeriodoBancoMeses(it)) },
                    onDataInicioCicloBancoAtualClick = { viewModel.onAction(EditarVersaoAction.MostrarDataInicioCicloBancoPicker(true)) },
                    onZerarBancoAntesPeriodoChange = { viewModel.onAction(EditarVersaoAction.AlterarZerarBancoAntesPeriodo(it)) },
                    onHabilitarSugestaoAjusteChange = { viewModel.onAction(EditarVersaoAction.AlterarHabilitarSugestaoAjuste(it)) },
                    onDiasUteisLembreteFechamentoChange = { viewModel.onAction(EditarVersaoAction.AlterarDiasUteisLembreteFechamento(it)) },
                    onExigeJustificativaInconsistenciaChange = { viewModel.onAction(EditarVersaoAction.AlterarExigeJustificativaInconsistencia(it)) },
                    onSalvar = { viewModel.onAction(EditarVersaoAction.Salvar) },
                    onGerenciarHorarios = { viewModel.onAction(EditarVersaoAction.ConfigurarHorarios) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarVersaoContent(
    uiState: EditarVersaoUiState,
    onDescricaoChange: (String) -> Unit,
    onDataInicioClick: () -> Unit,
    onDataFimClick: () -> Unit,
    onJornadaMaximaChange: (Int) -> Unit,
    onIntervaloInterjornadaChange: (Int) -> Unit,
    onToleranciaIntervaloChange: (Int) -> Unit,
    onTurnoMaximoChange: (Int) -> Unit,
    onCargaHorariaDiariaChange: (Int) -> Unit,
    onAcrescimoDiasPontesChange: (Int) -> Unit,
    onCargaHorariaSemanalChange: (Int) -> Unit,
    onPrimeiroDiaSemanaChange: (DiaSemana) -> Unit,
    onDiaInicioFechamentoRHChange: (Int) -> Unit,
    onZerarSaldoSemanalChange: (Boolean) -> Unit,
    onZerarSaldoPeriodoRHChange: (Boolean) -> Unit,
    onOcultarSaldoTotalChange: (Boolean) -> Unit,
    onBancoHorasHabilitadoChange: (Boolean) -> Unit,
    onPeriodoBancoSemanasChange: (Int) -> Unit,
    onPeriodoBancoMesesChange: (Int) -> Unit,
    onDataInicioCicloBancoAtualClick: () -> Unit,
    onZerarBancoAntesPeriodoChange: (Boolean) -> Unit,
    onHabilitarSugestaoAjusteChange: (Boolean) -> Unit,
    onDiasUteisLembreteFechamentoChange: (Int) -> Unit,
    onExigeJustificativaInconsistenciaChange: (Boolean) -> Unit,
    onSalvar: () -> Unit,
    onGerenciarHorarios: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Informações básicas
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Informações Básicas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = onDescricaoChange,
                    label = { Text("Descrição da versão") },
                    placeholder = { Text("Ex: Jornada 2024") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Versão: ${uiState.numeroVersao}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.vigente) {
                    Text(
                        text = "✓ Versão vigente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Período de vigência
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Período de Vigência",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.dataInicioFormatada,
                        onValueChange = { },
                        label = { Text("Data início") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = onDataInicioClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar data início")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.dataFimFormatada ?: "Indefinido",
                        onValueChange = { },
                        label = { Text("Data fim") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = onDataFimClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar data fim")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Configurações de jornada
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Configurações de Jornada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.jornadaMaximaDiariaMinutos.toString(),
                    onValueChange = { val value = it.toIntOrNull(); if (value != null) onJornadaMaximaChange(value) },
                    label = { Text("Jornada máx. diária (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Atual: ${uiState.jornadaMaximaFormatada}") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.intervaloMinimoInterjornadaMinutos.toString(),
                    onValueChange = { val value = it.toIntOrNull(); if (value != null) onIntervaloInterjornadaChange(value) },
                    label = { Text("Intervalo interjornada (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Atual: ${uiState.intervaloInterjornadaFormatado}") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.toleranciaIntervaloMaisMinutos.toString(),
                    onValueChange = { val value = it.toIntOrNull(); if (value != null) onToleranciaIntervaloChange(value) },
                    label = { Text("Tolerância intervalo (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.turnoMaximoMinutos.toString(),
                    onValueChange = { val value = it.toIntOrNull(); if (value != null) onTurnoMaximoChange(value) },
                    label = { Text("Turno máximo (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Carga Horária
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Carga Horária",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.cargaHorariaDiariaMinutos.toString(),
                    onValueChange = { val value = it.toIntOrNull(); if (value != null) onCargaHorariaDiariaChange(value) },
                    label = { Text("Carga horária diária (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.acrescimoMinutosDiasPontes.toString(),
                    onValueChange = { val value = it.toIntOrNull(); if (value != null) onAcrescimoDiasPontesChange(value) },
                    label = { Text("Acréscimo dias ponte (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.cargaHorariaSemanalMinutos.toString(),
                    onValueChange = { val value = it.toIntOrNull(); if (value != null) onCargaHorariaSemanalChange(value) },
                    label = { Text("Carga horária semanal (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Período e Saldo
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Período e Saldo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.primeiroDiaSemana.descricao,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Primeiro dia da semana") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DiaSemana.entries.forEach { dia ->
                            DropdownMenuItem(
                                text = { Text(dia.descricao) },
                                onClick = {
                                    onPrimeiroDiaSemanaChange(dia)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.diaInicioFechamentoRH.toString(),
                    onValueChange = { val value = it.toIntOrNull(); if (value != null) onDiaInicioFechamentoRHChange(value) },
                    label = { Text("Dia início fechamento RH") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.zerarSaldoSemanal, onCheckedChange = onZerarSaldoSemanalChange)
                    Text("Zerar saldo semanalmente")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.zerarSaldoPeriodoRH, onCheckedChange = onZerarSaldoPeriodoRHChange)
                    Text("Zerar saldo no período RH")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.ocultarSaldoTotal, onCheckedChange = onOcultarSaldoTotalChange)
                    Text("Ocultar saldo total")
                }
            }
        }

        // Banco de Horas
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Banco de Horas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = uiState.bancoHorasHabilitado, onCheckedChange = onBancoHorasHabilitadoChange)
                }

                if (uiState.bancoHorasHabilitado) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.periodoBancoSemanas.toString(),
                        onValueChange = { val value = it.toIntOrNull(); if (value != null) onPeriodoBancoSemanasChange(value) },
                        label = { Text("Período (semanas)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.periodoBancoMeses.toString(),
                        onValueChange = { val value = it.toIntOrNull(); if (value != null) onPeriodoBancoMesesChange(value) },
                        label = { Text("Período (meses)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.dataInicioCicloBancoAtual?.toString() ?: "Não definida",
                        onValueChange = { },
                        label = { Text("Data início ciclo atual") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = onDataInicioCicloBancoAtualClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar data")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = uiState.zerarBancoAntesPeriodo, onCheckedChange = onZerarBancoAntesPeriodoChange)
                        Text("Ignorar registros antes do início do banco")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = uiState.habilitarSugestaoAjuste, onCheckedChange = onHabilitarSugestaoAjusteChange)
                        Text("Habilitar sugestão de ajuste automático")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.diasUteisLembreteFechamento.toString(),
                        onValueChange = { val value = it.toIntOrNull(); if (value != null) onDiasUteisLembreteFechamentoChange(value) },
                        label = { Text("Dias úteis para lembrete de fechamento") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Validação
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Validação",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.exigeJustificativaInconsistencia,
                        onCheckedChange = onExigeJustificativaInconsistenciaChange
                    )
                    Text("Exigir justificativa para inconsistências")
                }
            }
        }

        // Botão para gerenciar horários (se não for nova versão)
        if (!uiState.isNovaVersao && uiState.versaoId != null) {
            OutlinedButton(
                onClick = onGerenciarHorarios,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gerenciar Horários (${uiState.horarios.size})")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão salvar
        Button(
            onClick = onSalvar,
            enabled = uiState.podeSalvar,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (uiState.isNovaVersao) "Criar Versão" else "Salvar Alterações")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
