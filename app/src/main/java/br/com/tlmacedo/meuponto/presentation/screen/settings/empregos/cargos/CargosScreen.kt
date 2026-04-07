package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.cargos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.AjusteSalarial
import br.com.tlmacedo.meuponto.domain.model.HistoricoCargo
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tela de listagem de cargos e salários de um emprego.
 *
 * Exibe o histórico completo de cargos com:
 * - Função/cargo
 * - Salário inicial
 * - Ajustes e dissídios (expansível)
 * - Período (início e fim)
 *
 * @author Thiago
 * @since 29.0.0
 */
@Composable
fun CargosScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditarCargo: (Long, Long) -> Unit,
    onNavigateToNovoCargo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CargosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is CargosEvent.MostrarMensagem -> snackbarHostState.showSnackbar(evento.mensagem)
                is CargosEvent.NavegarParaEditar -> onNavigateToEditarCargo(evento.empregoId, evento.cargoId)
                is CargosEvent.NavegarParaNovo -> onNavigateToNovoCargo(evento.empregoId)
            }
        }
    }

    CargosContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
internal fun CargosContent(
    uiState: CargosUiState,
    onAction: (CargosAction) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Cargos e Salários",
                subtitle = uiState.nomeEmprego,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(CargosAction.NovoCargo) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo Cargo") }
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

            uiState.cargos.isEmpty() -> {
                EmptyStateCargos(
                    onNovoCargo = { onAction(CargosAction.NovoCargo) },
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
                        ResumoCargoHeader(
                            totalCargos = uiState.cargos.size,
                            cargoAtual = uiState.cargoAtual?.funcao
                        )
                    }

                    items(
                        items = uiState.cargos,
                        key = { it.id }
                    ) { cargo ->
                        CargoCard(
                            cargo = cargo,
                            ajustes = uiState.ajustesPorCargo[cargo.id] ?: emptyList(),
                            isAtual = cargo.id == uiState.cargoAtual?.id,
                            onEditar = { onAction(CargosAction.EditarCargo(cargo)) },
                            onExcluir = { onAction(CargosAction.SolicitarExclusao(cargo)) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(88.dp)) }
                }
            }
        }
    }

    // Dialog de confirmação de exclusão
    uiState.cargoParaExcluir?.let { cargo ->
        AlertDialog(
            onDismissRequest = { onAction(CargosAction.CancelarExclusao) },
            title = { Text("Excluir Cargo") },
            text = {
                Text(
                    "Tem certeza que deseja excluir o cargo \"${cargo.funcao}\"?\n\n" +
                            "Todos os ajustes salariais associados também serão removidos."
                )
            },
            confirmButton = {
                Button(
                    onClick = { onAction(CargosAction.ConfirmarExclusao) },
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
                TextButton(onClick = { onAction(CargosAction.CancelarExclusao) }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// COMPONENTES
// ════════════════════════════════════════════════════════════════════════════════

@Composable
private fun ResumoCargoHeader(
    totalCargos: Int,
    cargoAtual: String?,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Badge,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "$totalCargos cargo(s) registrado(s)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (cargoAtual != null) {
                    Text(
                        text = "Cargo atual: $cargoAtual",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CargoCard(
    cargo: HistoricoCargo,
    ajustes: List<AjusteSalarial>,
    isAtual: Boolean,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    var expandido by remember { mutableStateOf(isAtual) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isAtual)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Cabeçalho do cargo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = if (isAtual)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cargo.funcao,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAtual)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isAtual) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Atual",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                // Ações
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar cargo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onExcluir) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir cargo",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Período
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buildString {
                        append(cargo.dataInicio.format(dateFormatter))
                        append(" → ")
                        append(cargo.dataFim?.format(dateFormatter) ?: "atual")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Salário inicial
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Salário inicial: ${currencyFormat.format(cargo.salarioInicial)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Ajustes e dissídios (só mostra se houver)
            if (ajustes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Botão para expandir/recolher ajustes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${ajustes.size} ajuste(s)/dissídio(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { expandido = !expandido },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expandido) "Recolher" else "Expandir",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expandido,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        ajustes.sortedBy { it.dataAjuste }.forEach { ajuste ->
                            AjusteRow(
                                ajuste = ajuste,
                                dateFormatter = dateFormatter,
                                currencyFormat = currencyFormat
                            )
                        }
                        // Salário atual (último ajuste)
                        val ultimoAjuste = ajustes.maxByOrNull { it.dataAjuste }
                        if (ultimoAjuste != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Salário atual:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = currencyFormat.format(ultimoAjuste.novoSalario),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AjusteRow(
    ajuste: AjusteSalarial,
    dateFormatter: DateTimeFormatter,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.size(6.dp)
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = ajuste.dataAjuste.format(dateFormatter),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = currencyFormat.format(ajuste.novoSalario),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyStateCargos(
    onNovoCargo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nenhum cargo registrado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Adicione o histórico de cargos e salários para acompanhar sua evolução profissional.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onNovoCargo) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Cargo")
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ════════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true)
@Composable
private fun CargosScreenPreview() {
    val sampleCargos = listOf(
        HistoricoCargo(
            id = 1,
            empregoId = 1,
            funcao = "Desenvolvedor Android Senior",
            salarioInicial = 8000.0,
            dataInicio = LocalDate.now().minusYears(1)
        ),
        HistoricoCargo(
            id = 2,
            empregoId = 1,
            funcao = "Desenvolvedor Android Pleno",
            salarioInicial = 6000.0,
            dataInicio = LocalDate.now().minusYears(2),
            dataFim = LocalDate.now().minusYears(1).minusDays(1)
        )
    )

    val sampleAjustes = mapOf(
        1L to listOf(
            AjusteSalarial(
                id = 1,
                historicoCargoId = 1,
                dataAjuste = LocalDate.now().minusMonths(6),
                novoSalario = 8500.0
            ),
            AjusteSalarial(
                id = 2,
                historicoCargoId = 1,
                dataAjuste = LocalDate.now().minusMonths(2),
                novoSalario = 9000.0
            )
        )
    )

    val uiState = CargosUiState(
        isLoading = false,
        nomeEmprego = "Empresa de Tecnologia LTDA",
        cargos = sampleCargos,
        cargoAtual = sampleCargos[0],
        ajustesPorCargo = sampleAjustes
    )

    MeuPontoTheme {
        CargosContent(
            uiState = uiState,
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CargosEmptyPreview() {
    val uiState = CargosUiState(
        isLoading = false,
        nomeEmprego = "Empresa de Tecnologia LTDA",
        cargos = emptyList()
    )

    MeuPontoTheme {
        CargosContent(
            uiState = uiState,
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
