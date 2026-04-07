package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.settings.StatusChip
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun VersoesJornadaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditar: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VersoesJornadaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is VersoesJornadaEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is VersoesJornadaEvent.NavegarParaEditar -> {
                    onNavigateToEditar(uiState.empregoId, evento.versaoId)
                }
                is VersoesJornadaEvent.Voltar -> {
                    onNavigateBack()
                }
                else -> Unit
            }
        }
    }

    VersoesJornadaContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Conteúdo da tela de listagem de versões de jornada, desacoplado do ViewModel.
 */
@Composable
fun VersoesJornadaContent(
    uiState: VersoesJornadaUiState,
    onAction: (VersoesJornadaAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Versões de Jornada",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = { onAction(VersoesJornadaAction.CriarNovaVersao) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nova Versão") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Erro desconhecido",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            !uiState.temVersoes -> {
                EmptyStateVersoesModern(
                    onCriarVersao = {
                        onAction(VersoesJornadaAction.CriarNovaVersao)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        VersoesResumoHeader(
                            nomeEmprego = uiState.nomeEmprego,
                            totalVersoes = uiState.totalVersoes,
                            vigente = uiState.versaoVigente
                        )
                    }

                    uiState.versaoVigente?.let { vigente ->
                        item {
                            Text(
                                text = "Versão vigente",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        item {
                            VersaoJornadaModernCard(
                                versao = vigente,
                                isVigente = true,
                                onEditar = {
                                    onAction(VersoesJornadaAction.EditarVersao(vigente.id))
                                },
                                onDefinirVigente = {},
                                onExcluir = {}
                            )
                        }
                    }

                    val historicas = uiState.versoes.filterNot { it.vigente }
                    if (historicas.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Histórico",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(historicas, key = { it.id }) { versao ->
                            VersaoJornadaModernCard(
                                versao = versao,
                                isVigente = false,
                                onEditar = {
                                    onAction(VersoesJornadaAction.EditarVersao(versao.id))
                                },
                                onDefinirVigente = {
                                    onAction(VersoesJornadaAction.DefinirComoVigente(versao.id))
                                },
                                onExcluir = {
                                    onAction(VersoesJornadaAction.AbrirDialogExcluir(versao))
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(88.dp)) }
                }
            }
        }

        if (uiState.mostrarDialogNovaVersao) {
            NovaVersaoDialog(
                dataInicio = uiState.dataInicioNovaVersao,
                descricao = uiState.descricaoNovaVersao,
                copiarHorarios = uiState.copiarHorariosNovaVersao,
                isSaving = uiState.isCriando,
                onDataInicioChange = { onAction(VersoesJornadaAction.AlterarDataInicioNovaVersao(it)) },
                onDescricaoChange = { onAction(VersoesJornadaAction.AlterarDescricaoNovaVersao(it)) },
                onCopiarHorariosToggle = { onAction(VersoesJornadaAction.ToggleCopiarHorariosNovaVersao(it)) },
                onConfirmar = { onAction(VersoesJornadaAction.ConfirmarNovaVersao) },
                onDismiss = { onAction(VersoesJornadaAction.FecharDialogNovaVersao) }
            )
        }

        uiState.versaoParaExcluir?.let { versao ->
            AlertDialog(
                onDismissRequest = { onAction(VersoesJornadaAction.FecharDialogExcluir) },
                title = { Text("Excluir Versão") },
                text = {
                    Text("Tem certeza que deseja excluir a versão \"${versao.titulo}\"? Esta ação não pode ser desfeita.")
                },
                confirmButton = {
                    Button(
                        onClick = { onAction(VersoesJornadaAction.ConfirmarExclusao) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (uiState.isExcluindo) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Excluir")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(VersoesJornadaAction.FecharDialogExcluir) }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaVersaoDialog(
    dataInicio: LocalDate,
    descricao: String,
    copiarHorarios: Boolean,
    isSaving: Boolean,
    onDataInicioChange: (LocalDate) -> Unit,
    onDescricaoChange: (String) -> Unit,
    onCopiarHorariosToggle: (Boolean) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dataInicio.atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDataInicioChange(
                            java.time.Instant.ofEpochMilli(it)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Versão de Jornada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "A nova versão passará a valer a partir da data de início selecionada. A versão anterior será encerrada automaticamente um dia antes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = dataInicio.format(dateFormatter),
                    onValueChange = { },
                    label = { Text("Data de Início") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Selecionar data")
                        }
                    }
                )

                OutlinedTextField(
                    value = descricao,
                    onValueChange = onDescricaoChange,
                    label = { Text("Descrição (opcional)") },
                    placeholder = { Text("Ex: Horário de Verão, Promoção...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = copiarHorarios,
                        onCheckedChange = onCopiarHorariosToggle
                    )
                    Text(
                        text = "Copiar horários da versão atual",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Criar Versão")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun VersoesResumoHeader(
    nomeEmprego: String,
    totalVersoes: Int,
    vigente: VersaoJornada?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = nomeEmprego,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$totalVersoes versão(ões) cadastradas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            vigente?.let {
                Spacer(modifier = Modifier.height(12.dp))
                StatusChip(text = "Vigente: ${it.titulo}")
            }
        }
    }
}

@Composable
private fun VersaoJornadaModernCard(
    versao: VersaoJornada,
    isVigente: Boolean,
    onEditar: () -> Unit,
    onDefinirVigente: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isVigente)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = versao.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = versao.periodoFormatado,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isVigente) {
                    StatusChip(text = "Vigente")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = versao.jornadaMaximaFormatada,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Jornada Máx.",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = versao.intervaloInterjornadaFormatado,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Interjornada",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${versao.numeroVersao}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Versão",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }

                if (!isVigente) {
                    Button(
                        onClick = onDefinirVigente,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Definir Vigente")
                    }
                }
            }

            if (!isVigente) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onExcluir) {
                    Text(
                        text = "Excluir versão",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateVersoesModern(
    onCriarVersao: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Nenhuma versão cadastrada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crie a primeira versão de jornada para começar a configurar horários e regras específicas deste emprego.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onCriarVersao) {
                    Text("Criar primeira versão")
                }
            }
        }
    }
}
