package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import kotlinx.coroutines.flow.collectLatest
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import java.time.Duration
import java.time.LocalDate

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

    EditarVersaoContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Conteúdo da tela de edição de versão, desacoplado do ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarVersaoContent(
    uiState: EditarVersaoUiState,
    onAction: (EditarVersaoAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    if (uiState.showDataInicioPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataInicio.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onAction(EditarVersaoAction.MostrarDataInicioPicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let {
                        onAction(EditarVersaoAction.AlterarDataInicio(it))
                    }
                }) { Text("OK") }
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

    if (uiState.showDataFimPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataFim?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onAction(EditarVersaoAction.MostrarDataFimPicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let {
                        onAction(EditarVersaoAction.AlterarDataFim(it))
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditarVersaoAction.AlterarDataFim(null)) }) {
                    Text("Limpar")
                }
                TextButton(onClick = { onAction(EditarVersaoAction.MostrarDataFimPicker(false)) }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.showDataInicioCicloBancoPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataInicioCicloBancoAtual?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onAction(EditarVersaoAction.MostrarDataInicioCicloBancoPicker(false)) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let {
                        onAction(EditarVersaoAction.AlterarDataInicioCicloBancoAtual(it))
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditarVersaoAction.AlterarDataInicioCicloBancoAtual(null)) }) {
                    Text("Limpar")
                }
                TextButton(onClick = { onAction(EditarVersaoAction.MostrarDataInicioCicloBancoPicker(false)) }) {
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
                onBackClick = onNavigateBack
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
                EditarVersaoForm(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarVersaoForm(
    uiState: EditarVersaoUiState,
    onAction: (EditarVersaoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Informações Básicas
        ConfiguracaoSection(
            title = "Informações Básicas",
            icon = Icons.Default.Info,
            subtitle = uiState.descricao.ifBlank { "Versão ${uiState.numeroVersao}" },
            expanded = uiState.secaoExpandida == SecaoVersao.INFORMACOES_BASICAS,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.INFORMACOES_BASICAS)) }
        ) {
            OutlinedTextField(
                value = uiState.descricao,
                onValueChange = { onAction(EditarVersaoAction.AlterarDescricao(it)) },
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

        // Período de Vigência
        ConfiguracaoSection(
            title = "Período de Vigência",
            icon = Icons.Default.CalendarMonth,
            subtitle = "${uiState.dataInicioFormatada} até ${uiState.dataFimFormatada ?: "Indefinido"}",
            expanded = uiState.secaoExpandida == SecaoVersao.PERIODO_VIGENCIA,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.PERIODO_VIGENCIA)) }
        ) {
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
                        IconButton(onClick = { onAction(EditarVersaoAction.MostrarDataInicioPicker(true)) }) {
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
                        IconButton(onClick = { onAction(EditarVersaoAction.MostrarDataFimPicker(true)) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar data fim")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Configurações de Jornada
        ConfiguracaoSection(
            title = "Configurações de Jornada",
            icon = Icons.Default.Schedule,
            subtitle = "Máx: ${uiState.jornadaMaximaFormatada} | Int: ${uiState.intervaloInterjornadaFormatado}",
            expanded = uiState.secaoExpandida == SecaoVersao.CONFIGURACOES_JORNADA,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.CONFIGURACOES_JORNADA)) }
        ) {
            OutlinedTextField(
                value = uiState.jornadaMaximaDiariaMinutos.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarJornadaMaxima(it)) }
                },
                label = { Text("Jornada máx. diária (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Atual: ${uiState.jornadaMaximaFormatada}") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.intervaloMinimoInterjornadaMinutos.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarIntervaloInterjornada(it)) }
                },
                label = { Text("Intervalo interjornada (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Atual: ${uiState.intervaloInterjornadaFormatado}") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.toleranciaIntervaloMaisMinutos.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarToleranciaIntervalo(it)) }
                },
                label = { Text("Tolerância intervalo (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Tolerância além do intervalo mínimo") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.turnoMaximoMinutos.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarTurnoMaximo(it)) }
                },
                label = { Text("Turno máximo (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Tempo máximo de trabalho contínuo") }
            )
        }

        // Carga Horária
        ConfiguracaoSection(
            title = "Carga Horária",
            icon = Icons.Default.Schedule,
            subtitle = "${uiState.cargaHorariaDiariaMinutos} min/dia | ${uiState.cargaHorariaSemanalMinutos} min/sem",
            expanded = uiState.secaoExpandida == SecaoVersao.CARGA_HORARIA,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.CARGA_HORARIA)) }
        ) {
            OutlinedTextField(
                value = uiState.cargaHorariaDiariaMinutos.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarCargaHorariaDiaria(it)) }
                },
                label = { Text("Carga horária diária (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.acrescimoMinutosDiasPontes.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarAcrescimoDiasPontes(it)) }
                },
                label = { Text("Acréscimo dias ponte (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Compensação para feriados 'ponte'") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.cargaHorariaSemanalMinutos.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarCargaHorariaSemanal(it)) }
                },
                label = { Text("Carga horária semanal (minutos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Período e Saldo
        ConfiguracaoSection(
            title = "Período e Saldo",
            icon = Icons.Default.CalendarMonth,
            subtitle = "Início RH: Dia ${uiState.diaInicioFechamentoRH} | ${uiState.primeiroDiaSemana.descricaoCurta}",
            expanded = uiState.secaoExpandida == SecaoVersao.PERIODO_SALDO,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.PERIODO_SALDO)) }
        ) {
            Text(
                text = "Primeiro dia da semana",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiaSemana.entries.forEach { dia ->
                    val selecionado = uiState.primeiroDiaSemana == dia
                    FilterChip(
                        selected = selecionado,
                        onClick = { onAction(EditarVersaoAction.AlterarPrimeiroDiaSemana(dia)) },
                        label = { Text(dia.descricaoCurta) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.diaInicioFechamentoRH.toString(),
                onValueChange = {
                    it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarDiaInicioFechamentoRH(it)) }
                },
                label = { Text("Dia início fechamento RH") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Dia do mês em que inicia o fechamento") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SwitchListItem(
                text = "Zerar saldo semanalmente",
                checked = uiState.zerarSaldoSemanal,
                onCheckedChange = { onAction(EditarVersaoAction.AlterarZerarSaldoSemanal(it)) }
            )

            SwitchListItem(
                text = "Zerar saldo no período RH",
                checked = uiState.zerarSaldoPeriodoRH,
                onCheckedChange = { onAction(EditarVersaoAction.AlterarZerarSaldoPeriodoRH(it)) }
            )

            SwitchListItem(
                text = "Ocultar saldo total",
                checked = uiState.ocultarSaldoTotal,
                onCheckedChange = { onAction(EditarVersaoAction.AlterarOcultarSaldoTotal(it)) }
            )
        }

        // Banco de Horas
        ConfiguracaoSection(
            title = "Banco de Horas",
            icon = Icons.Default.Schedule,
            subtitle = if (uiState.bancoHorasHabilitado) "Ativo" else "Desativado",
            expanded = uiState.secaoExpandida == SecaoVersao.BANCO_HORAS,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.BANCO_HORAS)) }
        ) {
            SwitchListItem(
                text = "Habilitar Banco de Horas",
                checked = uiState.bancoHorasHabilitado,
                onCheckedChange = { onAction(EditarVersaoAction.AlterarBancoHorasHabilitado(it)) }
            )

            AnimatedVisibility(
                visible = uiState.bancoHorasHabilitado,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.periodoBancoSemanas.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarPeriodoBancoSemanas(it)) }
                            },
                            label = { Text("Semanas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = uiState.periodoBancoMeses.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarPeriodoBancoMeses(it)) }
                            },
                            label = { Text("Meses") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = uiState.dataInicioCicloBancoAtual?.toString() ?: "Não definida",
                        onValueChange = { },
                        label = { Text("Data início ciclo atual") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { onAction(EditarVersaoAction.MostrarDataInicioCicloBancoPicker(true)) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar data")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    SwitchListItem(
                        text = "Ignorar registros anteriores",
                        checked = uiState.zerarBancoAntesPeriodo,
                        onCheckedChange = { onAction(EditarVersaoAction.AlterarZerarBancoAntesPeriodo(it)) }
                    )

                    SwitchListItem(
                        text = "Sugestão de ajuste automático",
                        checked = uiState.habilitarSugestaoAjuste,
                        onCheckedChange = { onAction(EditarVersaoAction.AlterarHabilitarSugestaoAjuste(it)) }
                    )

                    OutlinedTextField(
                        value = uiState.diasUteisLembreteFechamento.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { onAction(EditarVersaoAction.AlterarDiasUteisLembreteFechamento(it)) }
                        },
                        label = { Text("Dias úteis para lembrete") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Lembrete antes do fechamento") }
                    )
                }
            }
        }

        // Validação
        ConfiguracaoSection(
            title = "Validação",
            icon = Icons.Default.Info,
            subtitle = if (uiState.exigeJustificativaInconsistencia) "Exige justificativa" else "Flexível",
            expanded = uiState.secaoExpandida == SecaoVersao.VALIDACAO,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.VALIDACAO)) }
        ) {
            SwitchListItem(
                text = "Exigir justificativa para inconsistências",
                checked = uiState.exigeJustificativaInconsistencia,
                onCheckedChange = { onAction(EditarVersaoAction.AlterarExigeJustificativaInconsistencia(it)) }
            )
        }

        // Horários
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            ListItem(
                headlineContent = { Text("Configurar Horários", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Defina as entradas e saídas padrão") },
                leadingContent = {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                modifier = Modifier.clickable { onAction(EditarVersaoAction.ConfigurarHorarios) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão salvar
        Button(
            onClick = { onAction(EditarVersaoAction.Salvar) },
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

@Composable
private fun ConfiguracaoSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!expanded && !subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Recolher" else "Expandir"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun SwitchListItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ════════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true)
@Composable
private fun EditarVersaoContentPreview() {
    val uiState = EditarVersaoUiState(
        isLoading = false,
        isNovaVersao = false,
        descricao = "Jornada Padrão 2024",
        numeroVersao = 1,
        vigente = true,
        empregoId = 1L,
        bancoHorasHabilitado = true,
        secaoExpandida = SecaoVersao.BANCO_HORAS
    )

    MeuPontoTheme {
        Surface {
            EditarVersaoContent(
                uiState = uiState,
                onAction = {},
                onNavigateBack = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditarVersaoNovaPreview() {
    val uiState = EditarVersaoUiState(
        isLoading = false,
        isNovaVersao = true,
        descricao = "",
        numeroVersao = 2,
        empregoId = 1L,
        secaoExpandida = SecaoVersao.INFORMACOES_BASICAS
    )

    MeuPontoTheme {
        Surface {
            EditarVersaoContent(
                uiState = uiState,
                onAction = {},
                onNavigateBack = {}
            )
        }
    }
}
