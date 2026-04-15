// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.presentation.components.LocalImage
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.OutlinedNumberField
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import kotlinx.coroutines.flow.collectLatest

/**
 * Tela de edição/criação de emprego.
 */
@Composable
fun EditarEmpregoScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVersoes: (() -> Unit)? = null,
    onNavigateToCargos: (() -> Unit)? = null,
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
                subtitle = uiState.apelido.ifBlank { uiState.nome }.uppercase(),
                logo = uiState.logo,
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
                onSetShowInicioCicloBHPicker = viewModel::setShowInicioCicloBHPicker,
                onNavigateToVersoes = onNavigateToVersoes,
                onNavigateToCargos = onNavigateToCargos,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun EditarEmpregoContent(
    uiState: EditarEmpregoUiState,
    onAction: (EditarEmpregoAction) -> Unit,
    onSetShowInicioTrabalhoPicker: (Boolean) -> Unit,
    onSetShowTerminoTrabalhoPicker: (Boolean) -> Unit,
    onSetShowInicioCicloBHPicker: (Boolean) -> Unit,
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
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let { date ->
                            onAction(EditarEmpregoAction.AlterarDataInicioTrabalho(date))
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Confirmar") }
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
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let { date ->
                            onAction(EditarEmpregoAction.AlterarDataTerminoTrabalho(date))
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { onSetShowTerminoTrabalhoPicker(false) }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DATE PICKER - Início Ciclo BH
    if (uiState.showInicioCicloBHPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.bancoHorasDataInicioCiclo?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onSetShowInicioCicloBHPicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDateFromDatePicker()?.let { date ->
                            onAction(EditarEmpregoAction.AlterarBancoHorasDataInicioCiclo(date))
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { onSetShowInicioCicloBHPicker(false) }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            onAction(EditarEmpregoAction.AlterarLogo(uri?.toString()))
        }
    )

    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 📋 INFORMAÇÕES BÁSICAS
        item {
            FormSection(
                title = "Informações Básicas",
                icon = Icons.Default.Business,
                isExpanded = uiState.secaoExpandida == SecaoFormulario.DADOS_BASICOS,
                onToggle = { onAction(EditarEmpregoAction.ToggleSecao(SecaoFormulario.DADOS_BASICOS)) },
                showSaveButton = uiState.temMudancasDadosBasicos,
                onSave = { onAction(EditarEmpregoAction.SalvarDadosBasicos) },
                isSaving = uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo Picker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                logoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.logo != null) {
                            LocalImage(
                                imagePath = uiState.logo,
                                contentDescription = "Logo da empresa",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Botão de editar sobreposto
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Trocar logo",
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Logotipo da Empresa",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Toque para selecionar uma imagem da galeria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.logo != null) {
                            TextButton(
                                onClick = { onAction(EditarEmpregoAction.AlterarLogo(null)) },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Remover logo", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.dataInicioTrabalhoFormatada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Data de Início") },
                            trailingIcon = {
                                IconButton(onClick = { onSetShowInicioTrabalhoPicker(true) }) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "Selecionar data"
                                    )
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
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "Selecionar data"
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSetShowTerminoTrabalhoPicker(true) }
                        )
                    }

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
        }

        // 🏦 RH E BANCO DE HORAS
        if (!uiState.isNovoEmprego) {
            item {
                FormSection(
                    title = "RH e Banco de Horas",
                    icon = Icons.Default.Save,
                    isExpanded = uiState.secaoExpandida == SecaoFormulario.RH_E_BANCO_DE_HORAS,
                    onToggle = { onAction(EditarEmpregoAction.ToggleSecao(SecaoFormulario.RH_E_BANCO_DE_HORAS)) },
                    showSaveButton = uiState.temMudancasRHBank,
                    onSave = { onAction(EditarEmpregoAction.SalvarRHBank) },
                    isSaving = uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedNumberField(
                            value = uiState.diaInicioFechamentoRH.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { dia ->
                                    if (dia in 1..31) onAction(EditarEmpregoAction.AlterarDiaInicioFechamentoRH(dia))
                                }
                            },
                            label = "Dia de Início do Fechamento RH",
                            modifier = Modifier.fillMaxWidth()
                        )

                        SwitchOption(
                            title = "Habilitar Banco de Horas",
                            description = "Controlar saldo de horas extras e atrasos",
                            checked = uiState.bancoHorasHabilitado,
                            onCheckedChange = { onAction(EditarEmpregoAction.AlterarBancoHorasHabilitado(it)) }
                        )

                        AnimatedVisibility(visible = uiState.bancoHorasHabilitado) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                OutlinedNumberField(
                                    value = uiState.bancoHorasCicloMeses.toString(),
                                    onValueChange = {
                                        it.toIntOrNull()?.let { meses ->
                                            if (meses > 0) onAction(EditarEmpregoAction.AlterarBancoHorasCicloMeses(meses))
                                        }
                                    },
                                    label = "Ciclo do Banco (Meses)",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = uiState.dataInicioCicloBHFormatada,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Data de Início do Ciclo") },
                                    trailingIcon = {
                                        IconButton(onClick = { onSetShowInicioCicloBHPicker(true) }) {
                                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().clickable { onSetShowInicioCicloBHPicker(true) }
                                )

                                SwitchOption(
                                    title = "Zerar ao Final do Ciclo",
                                    description = "Limpar saldo automaticamente após o período",
                                    checked = uiState.bancoHorasZerarAoFinalCiclo,
                                    onCheckedChange = { onAction(EditarEmpregoAction.AlterarBancoHorasZerarAoFinalCiclo(it)) }
                                )

                                SwitchOption(
                                    title = "Exigir Justificativa",
                                    description = "Obrigar comentário em inconsistências",
                                    checked = uiState.exigeJustificativaInconsistencia,
                                    onCheckedChange = { onAction(EditarEmpregoAction.AlterarExigeJustificativaInconsistencia(it)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🕒 JORNADAS VERSIONADAS
        item {
            FormSection(
                title = "Jornadas Versionadas",
                icon = Icons.Default.Schedule,
                isExpanded = uiState.secaoExpandida == SecaoFormulario.JORNADAS_VERSIONADAS,
                onToggle = { onAction(EditarEmpregoAction.ToggleSecao(SecaoFormulario.JORNADAS_VERSIONADAS)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!uiState.isNovoEmprego && onNavigateToVersoes != null) {
                    Column {
                        Text(
                            "Gerencie o histórico de horários, carga horária e regras específicas.",
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
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Configurar Jornadas",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        "Regras que mudam ao longo do tempo.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                } else if (uiState.isNovoEmprego) {
                    Text(
                        "As configurações detalhadas de jornada estarão disponíveis logo após a criação do emprego.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 💼 HISTÓRICO DE CARGOS
        item {
            FormSection(
                title = "Histórico de Cargos",
                icon = Icons.Default.Badge,
                isExpanded = uiState.secaoExpandida == SecaoFormulario.HISTORICO_CARGOS,
                onToggle = { onAction(EditarEmpregoAction.ToggleSecao(SecaoFormulario.HISTORICO_CARGOS)) },
                modifier = Modifier.fillMaxWidth()
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
                                Text(
                                    "Gerenciar Cargos e Salários",
                                    style = MaterialTheme.typography.titleSmall
                                )
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
                            onValueChange = {
                                onAction(
                                    EditarEmpregoAction.AlterarSalarioInicial(
                                        it.toDoubleOrNull()
                                    )
                                )
                            },
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

        // SALVAR (Apenas para novos empregos, os existentes usam salvamento granular por seção)
        if (uiState.isNovoEmprego) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onAction(EditarEmpregoAction.Salvar) },
                        enabled = uiState.formularioValido && !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
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

    }
}

@Composable
private fun FormSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showSaveButton: Boolean = false,
    onSave: () -> Unit = {},
    isSaving: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
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

                if (showSaveButton && isExpanded) {
                    IconButton(
                        onClick = onSave,
                        enabled = !isSaving,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
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
            onSetShowInicioCicloBHPicker = {}
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
                isLoading = false
            ),
            onAction = {},
            onSetShowInicioTrabalhoPicker = {},
            onSetShowTerminoTrabalhoPicker = {},
            onSetShowInicioCicloBHPicker = {}
        )
    }
}
