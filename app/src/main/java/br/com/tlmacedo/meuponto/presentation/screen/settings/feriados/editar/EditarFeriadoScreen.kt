// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/editar/EditarFeriadoScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.editar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.feriado.AbrangenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components.EmpregoSelectorDialog
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Tela de edição/criação de feriados.
 *
 * @author Thiago
 * @since 3.4.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarFeriadoScreen(
    feriadoId: Long?,
    onNavigateBack: () -> Unit,
    onSalvoComSucesso: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditarFeriadoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navegação após salvar
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            viewModel.onNavigateBackHandled()
            onSalvoComSucesso()
        }
    }

    // Mostrar erros
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Mostrar sucesso
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.screenTitle) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.hasChanges) {
                            viewModel.showDiscardConfirmation()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = { viewModel.showDeleteConfirmation() },
                            enabled = !uiState.isDeleting
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nome do feriado
                OutlinedTextField(
                    value = uiState.nome,
                    onValueChange = viewModel::onNomeChange,
                    label = { Text("Nome do feriado") },
                    placeholder = { Text("Ex: Natal, Carnaval...") },
                    isError = uiState.nomeError != null,
                    supportingText = uiState.nomeError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tipo do feriado
                SectionTitle("Tipo")
                TipoFeriadoSelector(
                    tipoSelecionado = uiState.tipo,
                    onTipoChange = viewModel::onTipoChange
                )

                Text(
                    text = uiState.tipoDescricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Recorrência
                SectionTitle("Recorrência")
                RecorrenciaSelector(
                    recorrenciaSelecionada = uiState.recorrencia,
                    onRecorrenciaChange = viewModel::onRecorrenciaChange
                )

                // Data
                when (uiState.recorrencia) {
                    RecorrenciaFeriado.ANUAL -> {
                        DiaMesSelector(
                            diaMes = uiState.diaMes,
                            onDiaMesChange = viewModel::onDiaMesChange,
                            error = uiState.dataError
                        )
                    }
                    RecorrenciaFeriado.UNICO -> {
                        DataEspecificaSelector(
                            data = uiState.dataEspecifica,
                            onShowDatePicker = viewModel::showDatePicker,
                            error = uiState.dataError
                        )
                    }
                }

                // Campos geográficos (se aplicável)
                if (uiState.showUfField) {
                    HorizontalDivider()
                    SectionTitle("Localização")

                    UfSelector(
                        ufSelecionada = uiState.uf,
                        ufList = uiState.ufList,
                        onUfChange = viewModel::onUfChange,
                        error = uiState.ufError
                    )

                    if (uiState.showMunicipioField) {
                        OutlinedTextField(
                            value = uiState.municipio ?: "",
                            onValueChange = viewModel::onMunicipioChange,
                            label = { Text("Município") },
                            placeholder = { Text("Nome da cidade") },
                            isError = uiState.municipioError != null,
                            supportingText = uiState.municipioError?.let { { Text(it) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Abrangência
                HorizontalDivider()
                SectionTitle("Abrangência")
                AbrangenciaSelector(
                    abrangenciaSelecionada = uiState.abrangencia,
                    onAbrangenciaChange = viewModel::onAbrangenciaChange
                )

                // Seletor de emprego (se específico)
                if (uiState.showEmpregoField) {
                    EmpregoSelector(
                        empregoSelecionado = uiState.empregoSelecionado,
                        empregosDisponiveis = uiState.empregosDisponiveis,
                        onShowSelector = viewModel::showEmpregoSelector,
                        error = uiState.empregoError
                    )
                }

                // Observação
                HorizontalDivider()
                OutlinedTextField(
                    value = uiState.observacao,
                    onValueChange = viewModel::onObservacaoChange,
                    label = { Text("Observação (opcional)") },
                    placeholder = { Text("Anotações adicionais...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // Ativo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Feriado ativo",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (uiState.ativo) "Será considerado nos cálculos" else "Ignorado nos cálculos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.ativo,
                        onCheckedChange = viewModel::onAtivoChange
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão salvar
                androidx.compose.material3.Button(
                    onClick = viewModel::salvar,
                    enabled = uiState.isFormValid && !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isEditing) "Salvar Alterações" else "Criar Feriado")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Diálogos
    if (uiState.showDatePicker) {
        DatePickerDialogWrapper(
            initialDate = uiState.dataEspecifica,
            onDateSelected = { date ->
                viewModel.onDataEspecificaChange(date)
                viewModel.hideDatePicker()
            },
            onDismiss = viewModel::hideDatePicker
        )
    }

    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteConfirmation,
            title = { Text("Excluir feriado?") },
            text = { Text("Esta ação não pode ser desfeita. O feriado \"${uiState.nome}\" será removido permanentemente.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::excluir,
                    enabled = !uiState.isDeleting
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteConfirmation) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::hideDiscardConfirmation,
            title = { Text("Descartar alterações?") },
            text = { Text("Você tem alterações não salvas. Deseja descartá-las?") },
            confirmButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Descartar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDiscardConfirmation) {
                    Text("Continuar editando")
                }
            }
        )
    }

    if (uiState.showEmpregoSelector) {
        EmpregoSelectorDialog(
            empregos = uiState.empregosDisponiveis,
            empregoSelecionadoId = uiState.empregoSelecionado?.id,
            onEmpregoSelected = viewModel::onEmpregoSelecionado,
            onDismiss = viewModel::hideEmpregoSelector
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun TipoFeriadoSelector(
    tipoSelecionado: TipoFeriado,
    onTipoChange: (TipoFeriado) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TipoFeriado.entries.take(3).forEach { tipo ->
                FilterChip(
                    selected = tipoSelecionado == tipo,
                    onClick = { onTipoChange(tipo) },
                    label = { Text("${tipo.emoji} ${tipo.descricao}") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TipoFeriado.entries.drop(3).forEach { tipo ->
                FilterChip(
                    selected = tipoSelecionado == tipo,
                    onClick = { onTipoChange(tipo) },
                    label = { Text("${tipo.emoji} ${tipo.descricao}") },
                    modifier = Modifier.weight(1f)
                )
            }
            // Spacer para balancear se necessário
            if (TipoFeriado.entries.size % 3 != 0) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecorrenciaSelector(
    recorrenciaSelecionada: RecorrenciaFeriado,
    onRecorrenciaChange: (RecorrenciaFeriado) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        RecorrenciaFeriado.entries.forEach { recorrencia ->
            FilterChip(
                selected = recorrenciaSelecionada == recorrencia,
                onClick = { onRecorrenciaChange(recorrencia) },
                label = {
                    Text(
                        when (recorrencia) {
                            RecorrenciaFeriado.ANUAL -> "📅 Anual (todo ano)"
                            RecorrenciaFeriado.UNICO -> "📌 Data única"
                        }
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AbrangenciaSelector(
    abrangenciaSelecionada: AbrangenciaFeriado,
    onAbrangenciaChange: (AbrangenciaFeriado) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AbrangenciaFeriado.entries.forEach { abrangencia ->
            FilterChip(
                selected = abrangenciaSelecionada == abrangencia,
                onClick = { onAbrangenciaChange(abrangencia) },
                label = {
                    Text(
                        when (abrangencia) {
                            AbrangenciaFeriado.GLOBAL -> "🌐 Todos os empregos"
                            AbrangenciaFeriado.EMPREGO_ESPECIFICO -> "🏢 Emprego específico"
                        }
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DiaMesSelector(
    diaMes: MonthDay?,
    onDiaMesChange: (MonthDay) -> Unit,
    error: String?
) {
    var expandedDia by remember { mutableStateOf(false) }
    var expandedMes by remember { mutableStateOf(false) }

    val diaSelecionado = diaMes?.dayOfMonth
    val mesSelecionado = diaMes?.month

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Seletor de Mês
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = mesSelecionado?.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))?.replaceFirstChar { it.uppercase() } ?: "",
                    onValueChange = {},
                    label = { Text("Mês") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedMes = true }
                )
                DropdownMenu(
                    expanded = expandedMes,
                    onDismissRequest = { expandedMes = false }
                ) {
                    Month.entries.forEach { mes ->
                        DropdownMenuItem(
                            text = {
                                Text(mes.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() })
                            },
                            onClick = {
                                val novoDia = diaSelecionado?.coerceAtMost(mes.length(false)) ?: 1
                                onDiaMesChange(MonthDay.of(mes, novoDia))
                                expandedMes = false
                            }
                        )
                    }
                }
            }

            // Seletor de Dia
            Box(modifier = Modifier.weight(0.5f)) {
                OutlinedTextField(
                    value = diaSelecionado?.toString() ?: "",
                    onValueChange = {},
                    label = { Text("Dia") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDia = true }
                )
                DropdownMenu(
                    expanded = expandedDia,
                    onDismissRequest = { expandedDia = false }
                ) {
                    val maxDia = mesSelecionado?.length(false) ?: 31
                    (1..maxDia).forEach { dia ->
                        DropdownMenuItem(
                            text = { Text(dia.toString()) },
                            onClick = {
                                mesSelecionado?.let { mes ->
                                    onDiaMesChange(MonthDay.of(mes, dia))
                                }
                                expandedDia = false
                            }
                        )
                    }
                }
            }
        }

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun DataEspecificaSelector(
    data: LocalDate?,
    onShowDatePicker: () -> Unit,
    error: String?
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    OutlinedTextField(
        value = data?.format(formatter) ?: "",
        onValueChange = {},
        label = { Text("Data") },
        placeholder = { Text("Selecione a data") },
        readOnly = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        trailingIcon = {
            IconButton(onClick = onShowDatePicker) {
                Icon(Icons.Default.Event, contentDescription = "Selecionar data")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onShowDatePicker)
    )
}

@Composable
private fun UfSelector(
    ufSelecionada: String?,
    ufList: List<String>,
    onUfChange: (String) -> Unit,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = ufSelecionada ?: "",
            onValueChange = {},
            label = { Text("Estado (UF)") },
            readOnly = true,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ufList.forEach { uf ->
                DropdownMenuItem(
                    text = { Text(uf) },
                    onClick = {
                        onUfChange(uf)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmpregoSelector(
    empregoSelecionado: br.com.tlmacedo.meuponto.domain.model.Emprego?,
    empregosDisponiveis: List<br.com.tlmacedo.meuponto.domain.model.Emprego>,
    onShowSelector: () -> Unit,
    error: String?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (error != null)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onShowSelector)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Work,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = empregoSelecionado?.nome ?: "Selecionar emprego",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (empregoSelecionado != null) FontWeight.Medium else FontWeight.Normal,
                    color = if (empregoSelecionado != null)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun EmpregoSelectorDialog(
    empregos: List<br.com.tlmacedo.meuponto.domain.model.Emprego>,
    onEmpregoSelected: (br.com.tlmacedo.meuponto.domain.model.Emprego) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Emprego") },
        text = {
            Column {
                if (empregos.isEmpty()) {
                    Text("Nenhum emprego cadastrado")
                } else {
                    empregos.forEach { emprego ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onEmpregoSelected(emprego) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = emprego.nome,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * DatePicker corrigido para evitar problema de timezone.
 *
 * O DatePicker do Material3 trabalha internamente com milissegundos em UTC.
 * Para evitar o problema de "um dia a menos", precisamos:
 * 1. Converter LocalDate para millis usando UTC (não timezone local)
 * 2. Converter millis de volta para LocalDate usando UTC
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogWrapper(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // Converte LocalDate para millis em UTC ao meio-dia para evitar problemas de timezone
    val initialMillis = initialDate?.let {
        it.atTime(LocalTime.NOON)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // O DatePicker retorna millis à meia-noite UTC do dia selecionado
                        // Convertemos diretamente para LocalDate usando UTC
                        val instant = java.time.Instant.ofEpochMilli(millis)
                        val date = instant.atZone(ZoneOffset.UTC).toLocalDate()
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
