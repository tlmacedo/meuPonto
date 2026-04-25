package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.NumberPicker
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import kotlinx.coroutines.flow.collectLatest

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
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let {
                            onAction(EditarVersaoAction.AlterarDataInicio(it))
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) { Text("OK") }
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
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let {
                            onAction(EditarVersaoAction.AlterarDataFim(it))
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) { Text("OK") }
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
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let {
                            onAction(EditarVersaoAction.AlterarDataInicioCicloBancoAtual(it))
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onAction(
                        EditarVersaoAction.AlterarDataInicioCicloBancoAtual(
                            null
                        )
                    )
                }) {
                    Text("Limpar")
                }
                TextButton(onClick = {
                    onAction(
                        EditarVersaoAction.MostrarDataInicioCicloBancoPicker(
                            false
                        )
                    )
                }) {
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
                subtitle = uiState.empregoApelido?.uppercase(),
                logo = uiState.empregoLogo,
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
        // 1. Vigência e Identificação
        ConfiguracaoSection(
            title = "Vigência e Identificação",
            icon = Icons.Default.CalendarMonth,
            subtitle = "${uiState.dataInicioFormatada} ~ ${uiState.dataFimFormatada ?: "Indefinido"}",
            expanded = uiState.secaoExpandida == SecaoVersao.VIGENCIA_IDENTIFICACAO,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.VIGENCIA_IDENTIFICACAO)) },
            showSaveButton = uiState.temMudancasVigencia,
            onSave = { onAction(EditarVersaoAction.SalvarVigencia) },
            isSaving = uiState.isSaving
        ) {
            OutlinedTextField(
                value = uiState.descricao,
                onValueChange = { onAction(EditarVersaoAction.AlterarDescricao(it)) },
                label = { Text("Descrição da versão") },
                placeholder = { Text("Ex: Jornada 2024") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                        IconButton(onClick = {
                            onAction(
                                EditarVersaoAction.MostrarDataInicioPicker(
                                    true
                                )
                            )
                        }) {
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

        // 2. Jornada e Carga Horária
        ConfiguracaoSection(
            title = "Jornada e Carga Horária",
            icon = Icons.Default.Schedule,
            subtitle = "Carga: ${uiState.cargaHorariaDiariaMinutos} min | Int: ${uiState.intervaloAlmocoFormatado}",
            expanded = uiState.secaoExpandida == SecaoVersao.JORNADA_CARGA,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.JORNADA_CARGA)) },
            showSaveButton = uiState.temMudancasJornada,
            onSave = { onAction(EditarVersaoAction.SalvarJornada) },
            isSaving = uiState.isSaving
        ) {
            SliderComLabel(
                label = "Carga Horária Diária (tempo de trab.)",
                valor = uiState.cargaHorariaDiariaMinutos,
                range = 15f..720f,
                onValorChange = { onAction(EditarVersaoAction.AlterarCargaHorariaDiaria(it.toInt())) },
                formatar = { uiState.cargaHorariaDiariaFormatada }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SliderComLabel(
                label = "Acréscimo Diário (Dias Ponte)",
                valor = uiState.acrescimoMinutosDiasPontes,
                range = 0f..60f,
                onValorChange = { onAction(EditarVersaoAction.AlterarAcrescimoDiasPontes(it.toInt())) },
                formatar = { uiState.acrescimoMinutosDiasPontesFormatado }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SliderComLabel(
                label = "Jornada Máxima Diária",
                valor = uiState.jornadaMaximaDiariaMinutos,
                range = 15f..720f,
                onValorChange = { onAction(EditarVersaoAction.AlterarJornadaMaxima(it.toInt())) },
                formatar = { uiState.jornadaMaximaFormatada }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SliderComLabel(
                label = "Intervalo Mínimo Almoço",
                valor = uiState.intervaloMinimoAlmocoMinutos,
                range = 15f..120f,
                onValorChange = { onAction(EditarVersaoAction.AlterarIntervaloAlmoco(it.toInt())) },
                formatar = { uiState.intervaloAlmocoFormatado }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SliderComLabel(
                label = "Intervalo Mínimo de Descanso",
                valor = uiState.intervaloMinimoDescansoMinutos,
                range = 0f..30f,
                onValorChange = { onAction(EditarVersaoAction.AlterarIntervaloDescanso(it.toInt())) },
                formatar = { uiState.intervaloDescansoFormatado }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SliderComLabel(
                label = "Tolerância de Retorno do Almoço",
                valor = uiState.toleranciaRetornoIntervaloMinutos,
                range = 0f..40f,
                onValorChange = { onAction(EditarVersaoAction.AlterarToleranciaRetornoIntervalo(it.toInt())) },
                formatar = { uiState.toleranciaRetornoIntervaloFormatada }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SliderComLabel(
                label = "Interjornada Mínima (tempo da Saída até Entrada)",
                valor = uiState.intervaloMinimoInterjornadaMinutos,
                range = 0f..720f,
                onValorChange = { onAction(EditarVersaoAction.AlterarIntervaloInterjornada(it.toInt())) },
                formatar = { uiState.intervaloInterjornadaFormatado }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SliderComLabel(
                label = "Turno Máximo (Entrada e Saída)",
                valor = uiState.turnoMaximoMinutos,
                range = 0f..uiState.cargaHorariaDiariaMinutos.toFloat(),
                onValorChange = { onAction(EditarVersaoAction.AlterarTurnoMaximo(it.toInt())) },
                formatar = { uiState.turnoMaximoFormatado }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.3f
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            "Escala e Horários Padrão",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    supportingContent = { Text("Configure os horários para cada dia da semana") },
                    leadingContent = { Icon(Icons.Default.Timer, contentDescription = null) },
                    trailingContent = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    modifier = Modifier.clickable { onAction(EditarVersaoAction.ConfigurarHorarios) }
                )
            }
        }

        // 3. Fechamento e Saldo
        ConfiguracaoSection(
            title = "Fechamento e Saldo",
            icon = Icons.Default.AccountBalance,
            subtitle = "RH: Dia ${uiState.diaInicioFechamentoRH} | ${uiState.primeiroDiaSemana.descricaoCurta}",
            expanded = uiState.secaoExpandida == SecaoVersao.FECHAMENTO_SALDO,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.FECHAMENTO_SALDO)) },
            showSaveButton = uiState.temMudancasFechamento,
            onSave = { onAction(EditarVersaoAction.SalvarFechamento) },
            isSaving = uiState.isSaving
        ) {

            SliderComLabel(
                label = "Dia de Início do Mês RH",
                valor = uiState.diaInicioFechamentoRH,
                range = 0f..31f,
                onValorChange = { onAction(EditarVersaoAction.AlterarDiaInicioFechamentoRH(it.toInt())) },
                formatar = { uiState.diaInicioFechamentoRH.toString() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Primeiro Dia da Semana", style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiaSemana.entries.forEach { dia ->
                    FilterChip(
                        selected = uiState.primeiroDiaSemana == dia,
                        onClick = { onAction(EditarVersaoAction.AlterarPrimeiroDiaSemana(dia)) },
                        label = { Text(dia.descricaoCurta) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SwitchListItem(
                text = "Zerar saldo no fechamento RH",
                checked = uiState.zerarSaldoPeriodoRH,
                onCheckedChange = { onAction(EditarVersaoAction.AlterarZerarSaldoPeriodoRH(it)) }
            )

            SwitchListItem(
                text = "Ocultar saldo total na home",
                checked = uiState.ocultarSaldoTotal,
                onCheckedChange = { onAction(EditarVersaoAction.AlterarOcultarSaldoTotal(it)) }
            )
        }

        // 4. Banco de Horas
        ConfiguracaoSection(
            title = "Banco de Horas",
            icon = Icons.Default.History,
            subtitle = if (uiState.bancoHorasHabilitado) "Ativo" else "Desativado",
            expanded = uiState.secaoExpandida == SecaoVersao.BANCO_HORAS,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.BANCO_HORAS)) },
            showSaveButton = uiState.temMudancasBancoHoras,
            onSave = { onAction(EditarVersaoAction.SalvarBancoHoras) },
            isSaving = uiState.isSaving
        ) {
            SwitchListItem(
                text = "Habilitar Banco de Horas",
                checked = uiState.bancoHorasHabilitado,
                onCheckedChange = { onAction(EditarVersaoAction.AlterarBancoHorasHabilitado(it)) }
            )

            AnimatedVisibility(visible = uiState.bancoHorasHabilitado) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CicloBancoSlider(
                        valorProgresso = uiState.progressoCicloBanco,
                        onProgressoChange = {
                            onAction(
                                EditarVersaoAction.AlterarProgressoCicloBanco(
                                    it
                                )
                            )
                        },
                        labelCiclo = uiState.labelCicloBanco
                    )

                    OutlinedTextField(
                        value = uiState.dataInicioCicloBancoAtual?.toString() ?: "Não definida",
                        onValueChange = { },
                        label = { Text("Data Início Ciclo Atual") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                onAction(
                                    EditarVersaoAction.MostrarDataInicioCicloBancoPicker(
                                        true
                                    )
                                )
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar data")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    SwitchListItem(
                        text = "Sugestão de ajuste automático",
                        checked = uiState.habilitarSugestaoAjuste,
                        onCheckedChange = {
                            onAction(
                                EditarVersaoAction.AlterarHabilitarSugestaoAjuste(
                                    it
                                )
                            )
                        }
                    )
                }
            }
        }

        // 5. Validação e Inconsistências
        ConfiguracaoSection(
            title = "Validação e Regras",
            icon = Icons.Default.GppGood,
            subtitle = if (uiState.exigeJustificativaInconsistencia) "Rigorosa" else "Flexível",
            expanded = uiState.secaoExpandida == SecaoVersao.VALIDACAO,
            onToggle = { onAction(EditarVersaoAction.ToggleSecao(SecaoVersao.VALIDACAO)) },
            showSaveButton = uiState.temMudancasValidacao,
            onSave = { onAction(EditarVersaoAction.SalvarValidacao) },
            isSaving = uiState.isSaving
        ) {
            SwitchListItem(
                text = "Exigir justificativa em inconsistências",
                checked = uiState.exigeJustificativaInconsistencia,
                onCheckedChange = {
                    onAction(
                        EditarVersaoAction.AlterarExigeJustificativaInconsistencia(
                            it
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SliderComLabel(
                label = "Tolerância Atraso/Extra (Geral)",
                valor = uiState.toleranciaIntervaloMaisMinutos,
                range = 0f..15f,
                onValorChange = { onAction(EditarVersaoAction.AlterarToleranciaIntervalo(it.toInt())) },
                formatar = { uiState.toleranciaIntervaloFormatada }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isNovaVersao) {
            Button(
                onClick = { onAction(EditarVersaoAction.Salvar) },
                enabled = uiState.podeSalvar,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (uiState.isNovaVersao) "Criar Versão" else "Salvar Alterações")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TimeSelectionRow(
    label: String,
    minutos: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = "${minutos / 60}h ${minutos % 60}min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberPicker(
                value = minutos / 60,
                onValueChange = { onValueChange(it * 60 + (minutos % 60)) },
                range = 0..23,
                suffix = "h"
            )
            Spacer(modifier = Modifier.width(8.dp))
            NumberPicker(
                value = minutos % 60,
                onValueChange = { onValueChange((minutos / 60) * 60 + it) },
                range = 0..59,
                suffix = "m"
            )
        }
    }
}

@Composable
private fun ConfiguracaoSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    subtitle: String? = null,
    showSaveButton: Boolean = false,
    onSave: () -> Unit = {},
    isSaving: Boolean = false,
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

                if (showSaveButton && expanded) {
                    IconButton(
                        onClick = onSave,
                        enabled = !isSaving,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Salvar seção",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
private fun SliderComLabel(
    label: String,
    valor: Int,
    range: ClosedFloatingPointRange<Float>,
    onValorChange: (Float) -> Unit,
    formatar: (Int) -> String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = formatar(valor),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = valor.toFloat(),
            onValueChange = onValorChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CicloBancoSlider(
    valorProgresso: Float,
    onProgressoChange: (Float) -> Unit,
    labelCiclo: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "Ciclo do Banco",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = labelCiclo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = valorProgresso,
            onValueChange = onProgressoChange,
            valueRange = 0f..20f,
            steps = 19,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1 dia", style = MaterialTheme.typography.labelSmall)
            Text("1 ano", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SwitchListItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun EditarVersaoContentPreview() {
    val uiState = EditarVersaoUiState(
        isLoading = false,
        isNovaVersao = false,
        descricao = "Jornada Padrão 2024",
        empregoId = 1L,
        secaoExpandida = SecaoVersao.JORNADA_CARGA
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
