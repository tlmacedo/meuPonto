// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import kotlinx.coroutines.flow.collectLatest
import java.time.Duration

/**
 * Tela de edição/criação de emprego.
 */
@Composable
fun EditarEmpregoScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVersoes: (() -> Unit)? = null,
    onNavigateToCargos: (() -> Unit)? = null, // ← Novo
    modifier: Modifier = Modifier,
    viewModel: EditarEmpregoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is EditarEmpregoEvent.SalvoComSucesso -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                    onNavigateBack()
                }
                is EditarEmpregoEvent.MostrarErro -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is EditarEmpregoEvent.Voltar -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = uiState.tituloTela,
                showBackButton = true,
                onBackClick = { viewModel.onAction(EditarEmpregoAction.Cancelar) }
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
            EditarEmpregoContent(
                uiState = uiState,
                onAction = viewModel::onAction,
                onSetShowInicioTrabalhoPicker = viewModel::setShowInicioTrabalhoPicker,
                onSetShowTerminoTrabalhoPicker = viewModel::setShowTerminoTrabalhoPicker,
                onSetShowDataInicioCicloPicker = viewModel::setShowDataInicioCicloPicker,
                onNavigateToVersoes = onNavigateToVersoes,
                onNavigateToCargos = onNavigateToCargos,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditarEmpregoContent(
    uiState: EditarEmpregoUiState,
    onAction: (EditarEmpregoAction) -> Unit,
    onSetShowInicioTrabalhoPicker: (Boolean) -> Unit,
    onSetShowTerminoTrabalhoPicker: (Boolean) -> Unit,
    onSetShowDataInicioCicloPicker: (Boolean) -> Unit,
    onNavigateToVersoes: (() -> Unit)? = null,
    onNavigateToCargos: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // DATE PICKER - Data Início Trabalho
    if (uiState.showInicioTrabalhoPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataInicioTrabalho?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onSetShowInicioTrabalhoPicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let { date ->
                        onAction(EditarEmpregoAction.AlterarDataInicioTrabalho(date))
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { onSetShowInicioTrabalhoPicker(false) }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DATE PICKER - Data Término Trabalho
    if (uiState.showTerminoTrabalhoPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataTerminoTrabalho?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onSetShowTerminoTrabalhoPicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let { date ->
                        onAction(EditarEmpregoAction.AlterarDataTerminoTrabalho(date))
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { onSetShowTerminoTrabalhoPicker(false) }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DATE PICKER - Data Início Ciclo Banco
    if (uiState.showDataInicioCicloPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataInicioCicloBanco?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onSetShowDataInicioCicloPicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let { date ->
                        onAction(EditarEmpregoAction.AlterarDataInicioCicloBanco(date))
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { onSetShowDataInicioCicloPicker(false) }) { Text("Cancelar") }
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
        // 📋 INFORMAÇÕES BÁSICAS
        item {
            FormSection(
                title = "Informações Básicas",
                icon = Icons.Default.Business,
                isExpanded = uiState.secaoExpandida == SecaoFormulario.DADOS_BASICOS,
                onToggle = { onAction(EditarEmpregoAction.ToggleSecao(SecaoFormulario.DADOS_BASICOS)) }
            ) {
                OutlinedTextField(
                    value = uiState.nome,
                    onValueChange = { onAction(EditarEmpregoAction.AlterarNome(it)) },
                    label = { Text("Nome da Empresa") },
                    placeholder = { Text("Ex: Empresa ABC") },
                    isError = uiState.nomeErro != null,
                    supportingText = uiState.nomeErro?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.apelido,
                    onValueChange = { onAction(EditarEmpregoAction.AlterarApelido(it)) },
                    label = { Text("Apelido") },
                    placeholder = { Text("Ex: Meu Trabalho Principal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.dataInicioTrabalhoFormatada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data de Início") },
                        trailingIcon = {
                            IconButton(onClick = { onSetShowInicioTrabalhoPicker(true) }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetShowInicioTrabalhoPicker(true) }
                    )

                    OutlinedTextField(
                        value = uiState.dataTerminoTrabalhoFormatada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data de Término") },
                        trailingIcon = {
                            IconButton(onClick = { onSetShowTerminoTrabalhoPicker(true) }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetShowTerminoTrabalhoPicker(true) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.endereco,
                    onValueChange = { onAction(EditarEmpregoAction.AlterarEndereco(it)) },
                    label = { Text("Endereço") },
                    placeholder = { Text("Ex: Av. Paulista, 1000 - São Paulo/SP") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 💼 HISTÓRICO DE CARGOS
        item {
            FormSection(
                title = "Histórico de Cargos",
                icon = Icons.Default.Badge,
                isExpanded = uiState.secaoExpandida == SecaoFormulario.HISTORICO_CARGOS,
                onToggle = { onAction(EditarEmpregoAction.ToggleSecao(SecaoFormulario.HISTORICO_CARGOS)) }
            ) {
                if (!uiState.isNovoEmprego && onNavigateToCargos != null) {
                    Card(
                        onClick = onNavigateToCargos,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gerenciar Cargos e Salários", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Acesse o histórico completo de funções e ajustes salariais.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                } else if (uiState.isNovoEmprego) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Dados do cargo inicial:",
                            style = MaterialTheme.typography.titleSmall,
                        )

                        OutlinedTextField(
                            value = uiState.funcaoInicial,
                            onValueChange = { onAction(EditarEmpregoAction.AlterarFuncaoInicial(it)) },
                            label = { Text("Função/Cargo") },
                            placeholder = { Text("Ex: Desenvolvedor Android") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.salarioInicial?.toString() ?: "",
                            onValueChange = { onAction(EditarEmpregoAction.AlterarSalarioInicial(it.toDoubleOrNull())) },
                            label = { Text("Salário Inicial (R$)") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            "O histórico completo de cargos e ajustes salariais poderá ser gerenciado após a criação.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ⚙️ CONFIGURAÇÕES GERAIS
        item {
            FormSection(
                title = "Configurações Gerais",
                icon = Icons.Default.Settings,
                isExpanded = uiState.secaoExpandida == SecaoFormulario.CONFIGURACOES_GERAIS,
                onToggle = { onAction(EditarEmpregoAction.ToggleSecao(SecaoFormulario.CONFIGURACOES_GERAIS)) }
            ) {
                // 📊 DADOS RH
                Text(
                    text = "📊 DADOS RH",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                MinutesSliderWithSteppers(
                    label = "Início do Fechamento (Dia)",
                    value = uiState.diaInicioFechamentoRH,
                    onValueChange = { onAction(EditarEmpregoAction.AlterarDiaInicioFechamentoRH(it)) },
                    valueRange = 1..28,
                    sliderStep = 1,
                    formatAsHours = false,
                    helperText = uiState.exemploPeriodoRH
                )

                SwitchOption(
                    title = "Banco de Horas Habilitado",
                    description = "Ativar controle de compensação de horas",
                    checked = uiState.bancoHorasHabilitado,
                    onCheckedChange = { onAction(EditarEmpregoAction.AlterarBancoHorasHabilitado(it)) }
                )

                AnimatedVisibility(visible = uiState.bancoHorasHabilitado) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        MinutesSliderWithSteppers(
                            label = "Tempo de Ciclo",
                            value = uiState.periodoBancoValor,
                            onValueChange = { onAction(EditarEmpregoAction.AlterarPeriodoBancoHoras(it)) },
                            valueRange = 1..15,
                            sliderStep = 1,
                            formatAsHours = false,
                            displayFormatter = { uiState.periodoBancoDescricao }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.dataInicioCicloFormatada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Início do Ciclo Atual") },
                            trailingIcon = {
                                IconButton(onClick = { onSetShowDataInicioCicloPicker(true) }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetShowDataInicioCicloPicker(true) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SwitchOption(
                            title = "Zerar no Fechamento RH",
                            description = "Zerar saldo de horas ao final de cada período do RH",
                            checked = uiState.zerarSaldoPeriodoRH,
                            onCheckedChange = { onAction(EditarEmpregoAction.AlterarZerarSaldoPeriodoRH(it)) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 🎛️ OPÇÕES EXTRAS
                Text(
                    text = "🎛️ OPÇÕES EXTRAS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                SwitchOption(
                    title = "Habilitar NSR",
                    description = "Registro Sequencial de Registro (Portaria 671)",
                    checked = uiState.habilitarNsr,
                    onCheckedChange = { onAction(EditarEmpregoAction.AlterarHabilitarNsr(it)) }
                )

                AnimatedVisibility(visible = uiState.habilitarNsr) {
                    Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                        Text("Tipo de NSR", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TipoNsr.entries.forEach { tipo ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = uiState.tipoNsr == tipo,
                                        onClick = { onAction(EditarEmpregoAction.AlterarTipoNsr(tipo)) }
                                    )
                                    Text(tipo.descricao, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SwitchOption(
                    title = "Registrar Localização",
                    description = "Capturar GPS ao bater o ponto",
                    checked = uiState.habilitarLocalizacao,
                    onCheckedChange = { onAction(EditarEmpregoAction.AlterarHabilitarLocalizacao(it)) }
                )

                AnimatedVisibility(visible = uiState.habilitarLocalizacao) {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        SwitchOption(
                            title = "Captura Automática",
                            description = "Obter localização automaticamente",
                            checked = uiState.localizacaoAutomatica,
                            onCheckedChange = { onAction(EditarEmpregoAction.AlterarLocalizacaoAutomatica(it)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SwitchOption(
                    title = "Foto de Comprovante",
                    description = "Permitir anexar foto do recibo",
                    checked = uiState.habilitarFotoComprovante,
                    onCheckedChange = { onAction(EditarEmpregoAction.AlterarHabilitarFotoComprovante(it)) }
                )

                AnimatedVisibility(visible = uiState.habilitarFotoComprovante) {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        SwitchOption(
                            title = "Foto Obrigatória",
                            description = "Impedir registro sem foto",
                            checked = uiState.fotoObrigatoria,
                            onCheckedChange = { onAction(EditarEmpregoAction.AlterarFotoObrigatoria(it)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SwitchOption(
                    title = "Exigir Justificativa",
                    description = "Exigir motivo para inconsistências na jornada",
                    checked = uiState.exigeJustificativaInconsistencia,
                    onCheckedChange = { onAction(EditarEmpregoAction.AlterarExigeJustificativa(it)) }
                )
            }
        }

        // 🕒 JORNADAS VERSIONADAS
        item {
            FormSection(
                title = "Jornadas Versionadas",
                icon = Icons.Default.Schedule,
                isExpanded = uiState.secaoExpandida == SecaoFormulario.JORNADAS_VERSIONADAS,
                onToggle = { onAction(EditarEmpregoAction.ToggleSecao(SecaoFormulario.JORNADAS_VERSIONADAS)) }
            ) {
                if (!uiState.isNovoEmprego && onNavigateToVersoes != null) {
                    Column {
                        Text(
                            "Acesse o histórico de horários e regras aplicadas a este emprego.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedCard(
                            onClick = onNavigateToVersoes,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Gerenciar Versões de Jornada", style = MaterialTheme.typography.titleSmall)
                                    Text("Regras vigentes, turnos e feriados.", style = MaterialTheme.typography.bodySmall)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                } else if (uiState.isNovoEmprego) {
                    Text(
                        "Configure a primeira versão da sua jornada:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    MinutesSliderWithSteppers(
                        label = "Carga Horária Diária",
                        value = uiState.cargaHorariaDiaria.toMinutes().toInt(),
                        onValueChange = { onAction(EditarEmpregoAction.AlterarCargaHorariaDiaria(Duration.ofMinutes(it.toLong()))) },
                        valueRange = 240..600,
                        sliderStep = 30,
                        formatAsHours = true
                    )

                    uiState.avisoJornadaExcedida?.let { ValidationWarning(it, isError = uiState.cargaHorariaTotalMinutos > uiState.jornadaMaximaDiariaMinutos) }

                    Spacer(modifier = Modifier.height(16.dp))

                    MinutesSliderWithSteppers(
                        label = "Jornada Máxima Diária",
                        value = uiState.jornadaMaximaDiariaMinutos,
                        onValueChange = { onAction(EditarEmpregoAction.AlterarJornadaMaximaDiaria(it)) },
                        valueRange = 360..720,
                        sliderStep = 30,
                        formatAsHours = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MinutesSliderWithSteppers(
                        label = "Intervalo Mínimo",
                        value = uiState.intervaloMinimoMinutos,
                        onValueChange = { onAction(EditarEmpregoAction.AlterarIntervaloMinimo(it)) },
                        valueRange = 0..120,
                        sliderStep = 15,
                        formatAsHours = true
                    )
                }
            }
        }

        // SALVAR
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onAction(EditarEmpregoAction.Salvar) },
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
private fun ValidationWarning(
    message: String,
    isError: Boolean = false
) {
    val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    val icon = if (isError) Icons.Default.Info else Icons.Default.Info

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 8.dp, end = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
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
    suffix: String = "min",
    helperText: String? = null,
    displayFormatter: ((Int) -> String)? = null
) {
    val displayValue = when {
        displayFormatter != null -> displayFormatter(value)
        formatAsHours -> {
            val h = value / 60
            val m = value % 60
            String.format("%02d:%02d", h, m)
        }
        else -> if (suffix.isEmpty()) "$value" else "$value $suffix"
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
                onClick = { onValueChange((value - 1).coerceIn(valueRange)) },
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
                onClick = { onValueChange((value + 1).coerceIn(valueRange)) },
                enabled = value < valueRange.last
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar")
            }
        }

        helperText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
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

@Composable
private fun SwitchOption(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun EditarEmpregoContentPreview() {
    MeuPontoTheme {
        EditarEmpregoContent(
            uiState = EditarEmpregoUiState(
                isNovoEmprego = true,
                isLoading = false
            ),
            onAction = {},
            onSetShowInicioTrabalhoPicker = {},
            onSetShowTerminoTrabalhoPicker = {},
            onSetShowDataInicioCicloPicker = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditarEmpregoContentEditingPreview() {
    MeuPontoTheme {
        EditarEmpregoContent(
            uiState = EditarEmpregoUiState(
                isNovoEmprego = false,
                nome = "Empresa de Teste",
                isLoading = false,
                bancoHorasHabilitado = true
            ),
            onAction = {},
            onSetShowInicioTrabalhoPicker = {},
            onSetShowTerminoTrabalhoPicker = {},
            onSetShowDataInicioCicloPicker = {}
        )
    }
}
