package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.cargos

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import kotlinx.coroutines.flow.collectLatest
import br.com.tlmacedo.meuponto.util.toDatePickerMillis
import br.com.tlmacedo.meuponto.util.toLocalDateFromDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Tela de criação/edição de cargo e salário.
 *
 * Permite configurar:
 * - Função/cargo
 * - Salário inicial
 * - Data de início e fim
 * - Ajustes e dissídios salariais
 *
 * @author Thiago
 * @since 29.0.0
 */
@Composable
fun EditarCargoScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditarCargoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is EditarCargoEvent.SalvoComSucesso -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                    onNavigateBack()
                }

                is EditarCargoEvent.MostrarErro -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }

                is EditarCargoEvent.Voltar -> onNavigateBack()
            }
        }
    }

    EditarCargoContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditarCargoContent(
    uiState: EditarCargoUiState,
    onAction: (EditarCargoAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    // Date Pickers
    if (uiState.showDataInicioPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataInicio?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onAction(EditarCargoAction.FecharDataInicioPicker) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onAction(
                            EditarCargoAction.AlterarDataInicio(
                                it.toLocalDateFromDatePicker()
                            )
                        )
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditarCargoAction.FecharDataInicioPicker) }) {
                    Text("Cancelar")
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (uiState.showDataFimPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dataFim?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onAction(EditarCargoAction.FecharDataFimPicker) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onAction(
                            EditarCargoAction.AlterarDataFim(
                                it.toLocalDateFromDatePicker()
                            )
                        )
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditarCargoAction.AlterarDataFim(null)) }) {
                    Text("Limpar")
                }
                TextButton(onClick = { onAction(EditarCargoAction.FecharDataFimPicker) }) {
                    Text("Cancelar")
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Date Pickers para ajustes
    uiState.ajustePickerIndex?.let { idx ->
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.ajustes.getOrNull(idx)?.dataAjuste?.toDatePickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { onAction(EditarCargoAction.FecharAjusteDatePicker) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onAction(
                            EditarCargoAction.AlterarDataAjuste(
                                idx,
                                it.toLocalDateFromDatePicker()
                            )
                        )
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditarCargoAction.FecharAjusteDatePicker) }) {
                    Text("Cancelar")
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = if (uiState.isNovoCargo) "Novo Cargo" else "Editar Cargo",
                subtitle = uiState.nomeEmprego,
                showBackButton = true,
                onBackClick = { onAction(EditarCargoAction.Cancelar) }
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
            ) { CircularProgressIndicator() }
        } else {
            EditarCargoForm(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EditarCargoForm(
    uiState: EditarCargoUiState,
    onAction: (EditarCargoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        // ── DADOS DO CARGO ──────────────────────────────────────────
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Work, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dados do Cargo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.funcao,
                        onValueChange = { onAction(EditarCargoAction.AlterarFuncao(it)) },
                        label = { Text("Função / Cargo") },
                        placeholder = { Text("Ex: Analista Pleno, Gerente...") },
                        isError = uiState.funcaoErro != null,
                        supportingText = uiState.funcaoErro?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.salarioInicialStr,
                        onValueChange = { onAction(EditarCargoAction.AlterarSalarioInicial(it)) },
                        label = { Text("Salário Inicial (R$)") },
                        placeholder = { Text("Ex: 3500.00") },
                        isError = uiState.salarioErro != null,
                        supportingText = uiState.salarioErro?.let { { Text(it) } },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ── PERÍODO ─────────────────────────────────────────────────
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Período",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.dataInicioFormatada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data de Início") },
                        isError = uiState.dataInicioErro != null,
                        supportingText = uiState.dataInicioErro?.let { { Text(it) } },
                        trailingIcon = {
                            IconButton(onClick = {
                                onAction(EditarCargoAction.AbrirDataInicioPicker)
                            }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAction(EditarCargoAction.AbrirDataInicioPicker) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle cargo atual
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cargo atual",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Sem data de término (cargo em exercício)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isCargoAtual,
                            onCheckedChange = { onAction(EditarCargoAction.ToggleCargoAtual(it)) }
                        )
                    }

                    AnimatedVisibility(
                        visible = !uiState.isCargoAtual,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = uiState.dataFimFormatada ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Data de Término") },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        onAction(EditarCargoAction.AbrirDataFimPicker)
                                    }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAction(EditarCargoAction.AbrirDataFimPicker) }
                            )
                        }
                    }
                }
            }
        }

        // ── AJUSTES E DISSÍDIOS ─────────────────────────────────────
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.BarChart, contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ajustes e Dissídios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { onAction(EditarCargoAction.AdicionarAjuste) }
                        ) {
                            Icon(
                                Icons.Default.Add, contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar")
                        }
                    }

                    if (uiState.ajustes.isEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhum ajuste salarial registrado. Adicione dissídios ou reajustes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Ajustes individuais
        itemsIndexed(uiState.ajustes) { index, ajuste ->
            AjusteFormCard(
                index = index,
                ajuste = ajuste,
                onAlterarData = { onAction(EditarCargoAction.AbrirAjusteDatePicker(index)) },
                onAlterarSalario = { onAction(EditarCargoAction.AlterarSalarioAjuste(index, it)) },
                onRemover = { onAction(EditarCargoAction.RemoverAjuste(index)) }
            )
        }

        // ── SALVAR ──────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onAction(EditarCargoAction.Salvar) },
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
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (uiState.isNovoCargo) "Criar Cargo" else "Salvar Alterações")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AjusteFormCard(
    index: Int,
    ajuste: AjusteFormItem,
    onAlterarData: () -> Unit,
    onAlterarSalario: (String) -> Unit,
    onRemover: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ajuste ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onRemover,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover ajuste",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            OutlinedTextField(
                value = ajuste.dataAjusteStr,
                onValueChange = {},
                readOnly = true,
                label = { Text("Data do Ajuste") },
                trailingIcon = {
                    IconButton(onClick = onAlterarData) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlterarData() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ajuste.novoSalarioStr,
                onValueChange = onAlterarSalario,
                label = { Text("Novo Salário (R$)") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditarCargoContentPreview() {
    val uiState = EditarCargoUiState(
        isLoading = false,
        isNovoCargo = false,
        nomeEmprego = "Empresa de Tecnologia",
        funcao = "Desenvolvedor Android",
        salarioInicialStr = "5000.00",
        dataInicio = LocalDate.of(2023, 1, 1),
        dataInicioFormatada = "01/01/2023",
        isCargoAtual = true,
        ajustes = listOf(
            AjusteFormItem(
                id = 1,
                dataAjuste = LocalDate.of(2023, 7, 1),
                dataAjusteStr = "01/07/2023",
                novoSalarioStr = "5500.00"
            )
        ),
        formularioValido = true
    )

    MeuPontoTheme {
        EditarCargoContent(
            uiState = uiState,
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
