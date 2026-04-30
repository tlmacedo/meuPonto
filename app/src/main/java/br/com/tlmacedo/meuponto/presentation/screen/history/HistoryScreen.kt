// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.StatusResumoDia
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.presentation.components.CalendarLegend
import br.com.tlmacedo.meuponto.presentation.components.CalendarView
import br.com.tlmacedo.meuponto.presentation.components.EmptyState
import br.com.tlmacedo.meuponto.presentation.components.HistoryShimmerItem
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.theme.Error
import br.com.tlmacedo.meuponto.presentation.theme.Info
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import br.com.tlmacedo.meuponto.presentation.theme.SidiaMediumGray
import br.com.tlmacedo.meuponto.presentation.theme.Success
import br.com.tlmacedo.meuponto.presentation.theme.Warning
import br.com.tlmacedo.meuponto.util.minutosParaDuracaoCompacta
import br.com.tlmacedo.meuponto.util.minutosParaSaldoFormatado
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDay: (LocalDate) -> Unit = {},
    onNovaAusenciaComData: (String) -> Unit = {},
    onNovoFeriadoComData: (String) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparErro()
        }
    }

    LaunchedEffect(uiState.csvParaExportar) {
        uiState.csvParaExportar?.let { csv ->
            val fileName = "meuponto_relatorio_${
                uiState.periodoSelecionado.descricaoCurta.replace("/", "_")
            }.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csv)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Relatório de Ponto - ${uiState.periodoSelecionado.descricaoFormatada}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório"))
            viewModel.limparCsvExportado()
        }
    }

    HistoryContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToDay = onNavigateToDay,
        onNovaAusencia = { onNovaAusenciaComData(it.toString()) },
        onNovoFeriado = { onNovoFeriadoComData(it.toString()) },
        onPeriodoAnterior = viewModel::periodoAnterior,
        onProximoPeriodo = viewModel::proximoPeriodo,
        onIrParaAtual = viewModel::irParaPeriodoAtual,
        onFiltroSelecionado = viewModel::alterarFiltro,
        onToggleDiaExpandido = viewModel::toggleDiaExpandido,
        onToggleResumoExpandido = viewModel::toggleResumoExpandido,
        onToggleVisualizacao = viewModel::toggleVisualizacao,
        onLimparFiltros = viewModel::limparFiltros,
        onExportar = viewModel::exportarParaCsv,
        onTogglePeriodoSelection = viewModel::togglePeriodoSelection,
        onShowPeriodoSelector = viewModel::setShowPeriodoSelector,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onNavigateBack: () -> Unit,
    onNavigateToDay: (LocalDate) -> Unit,
    onNovaAusencia: (LocalDate) -> Unit,
    onNovoFeriado: (LocalDate) -> Unit,
    onPeriodoAnterior: () -> Unit,
    onProximoPeriodo: () -> Unit,
    onIrParaAtual: () -> Unit,
    onFiltroSelecionado: (FiltroHistorico) -> Unit,
    onToggleDiaExpandido: (LocalDate) -> Unit,
    onToggleResumoExpandido: () -> Unit,
    onToggleVisualizacao: () -> Unit,
    onLimparFiltros: () -> Unit,
    onExportar: () -> Unit,
    onTogglePeriodoSelection: (PeriodoHistorico) -> Unit,
    onShowPeriodoSelector: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Histórico",
                subtitle = (uiState.apelidoEmprego ?: uiState.nomeEmprego)?.uppercase(),
                logo = uiState.logoEmprego,
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onToggleVisualizacao) {
                        Icon(
                            imageVector = if (uiState.visualizacaoCalendario) Icons.AutoMirrored.Filled.List else Icons.Default.ViewModule,
                            contentDescription = if (uiState.visualizacaoCalendario) "Ver Lista" else "Ver Calendário"
                        )
                    }
                    if (uiState.hasRegistros) {
                        IconButton(onClick = onExportar, enabled = !uiState.isExporting) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Exportar CSV"
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showFabMenu = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar")
                }
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nova Ausência") },
                        onClick = {
                            showFabMenu = false
                            onNovaAusencia(LocalDate.now())
                        },
                        leadingIcon = { Icon(Icons.Default.History, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Novo Feriado") },
                        onClick = {
                            showFabMenu = false
                            onNovoFeriado(LocalDate.now())
                        },
                        leadingIcon = { Icon(Icons.Default.Today, null) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MonthNavigator(
                periodoDescricao = uiState.periodoDescricao,
                podeIrProximo = uiState.podeIrProximoPeriodo,
                onPeriodoAnterior = onPeriodoAnterior,
                onProximoPeriodo = onProximoPeriodo,
                onIrParaAtual = onIrParaAtual,
                onShowSelector = { onShowPeriodoSelector(true) }
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (uiState.hasRegistros && !uiState.isLoading) {
                    item {
                        ResumoMes(
                            resumoPeriodo = uiState.resumoPeriodo,
                            saldoInicialPeriodo = uiState.saldoDiaMaisAntigo,
                            saldoAcumuladoTotal = uiState.saldoDiaMaisRecente,
                            isPeriodoFuturo = uiState.periodosSelecionados.any { it.isFuturo },
                            isExpandido = uiState.resumoExpandido,
                            onToggle = onToggleResumoExpandido
                        )
                    }

                    item {
                        SecaoFiltros(
                            resumoPeriodo = uiState.resumoPeriodo,
                            filtrosAtivos = uiState.filtrosAtivos,
                            onFiltroClick = onFiltroSelecionado,
                            onLimparFiltros = onLimparFiltros
                        )
                    }
                }

                if (uiState.filtrosAtivos.isNotEmpty()) {
                    item {
                        FiltroAtivoIndicator(
                            filtros = uiState.filtrosAtivos,
                            quantidadeResultados = uiState.registrosFiltrados.size,
                            onLimparFiltro = onLimparFiltros
                        )
                    }
                }

                when {
                    uiState.isLoading -> {
                        items(5) {
                            HistoryShimmerItem()
                        }
                    }

                    uiState.visualizacaoCalendario -> {
                        val uniqueMonths = uiState.periodosSelecionados.flatMap { periodo ->
                            val start = YearMonth.from(periodo.dataInicio)
                            val end = YearMonth.from(periodo.dataFim)
                            var current = start
                            val months = mutableListOf<YearMonth>()
                            while (!current.isAfter(end)) {
                                months.add(current)
                                current = current.plusMonths(1)
                            }
                            months
                        }.distinct().sorted()

                        items(uniqueMonths) { yearMonth ->
                            val locale = Locale.forLanguageTag("pt-BR")
                            val formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", locale)
                            val title = yearMonth.atDay(1).format(formatter)
                                .replaceFirstChar { it.uppercase() }

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )

                            CalendarView(
                                yearMonth = yearMonth,
                                diasHistorico = uiState.diasHistorico,
                                periodosAtivos = uiState.periodosSelecionados,
                                filtrosAtivos = uiState.filtrosAtivos,
                                showLegend = false,
                                onDateClick = { date -> onNavigateToDay(date) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )

                            Spacer(Modifier.height(24.dp))
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            CalendarLegend()
                        }
                    }

                    uiState.registrosFiltrados.isEmpty() -> {
                        item {
                            EmptyState(
                                title = if (uiState.filtrosAtivos.isNotEmpty()) "Nenhum resultado" else "Sem registros",
                                message = if (uiState.filtrosAtivos.isNotEmpty()) "Não há dias com os filtros selecionados" else "Nenhum ponto registrado neste período",
                                icon = Icons.Outlined.CalendarMonth
                            )
                        }
                    }

                    else -> {
                        items(
                            items = uiState.registrosFiltrados,
                            key = { it.data.toString() }
                        ) { infoDia ->
                            DiaCard(
                                infoDia = infoDia,
                                isExpandido = uiState.diasExpandidos.contains(infoDia.data),
                                saldoBancoAcumulado = uiState.saldoAcumuladoAte(infoDia.data),
                                onToggleExpansao = { onToggleDiaExpandido(infoDia.data) },
                                onNavigateToDay = { onNavigateToDay(infoDia.data) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showPeriodoSelector) {
        PeriodoSelectorDialog(
            periodos = uiState.periodosDisponiveis,
            selecionados = uiState.periodosSelecionados,
            onToggle = onTogglePeriodoSelection,
            onDismiss = { onShowPeriodoSelector(false) }
        )
    }
}

@Composable
private fun PeriodoSelectorDialog(
    periodos: List<PeriodoHistorico>,
    selecionados: List<PeriodoHistorico>,
    onToggle: (PeriodoHistorico) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Períodos") },
        text = {
            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(periodos) { periodo ->
                    val isSelected = selecionados.contains(periodo)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(periodo) }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(checked = isSelected, onCheckedChange = { onToggle(periodo) })
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = periodo.descricaoFormatada,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Concluído") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecaoFiltros(
    resumoPeriodo: ResumoPeriodo,
    filtrosAtivos: Set<FiltroHistorico>,
    onFiltroClick: (FiltroHistorico) -> Unit,
    onLimparFiltros: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Detalhes (toque para filtrar)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (filtrosAtivos.isNotEmpty()) {
                Text(
                    text = "Limpar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onLimparFiltros() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (resumoPeriodo.diasUteis > 0) {
                ResumoSecundarioChip(
                    emoji = "📅", label = "Úteis", valor = resumoPeriodo.diasUteis,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.UTEIS),
                    onClick = { onFiltroClick(FiltroHistorico.UTEIS) }
                )
            }
            if (resumoPeriodo.diasCompletos > 0) {
                ResumoSecundarioChip(
                    emoji = "✅", label = "Completos", valor = resumoPeriodo.diasCompletos,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.COMPLETOS),
                    onClick = { onFiltroClick(FiltroHistorico.COMPLETOS) }
                )
            }
            if (resumoPeriodo.temDiasFuturos) {
                ResumoSecundarioChip(
                    emoji = "🔮", label = "Futuros", valor = resumoPeriodo.diasFuturos,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.FUTUROS),
                    corFundo = Color(0xFF9C27B0).copy(alpha = 0.1f),
                    onClick = { onFiltroClick(FiltroHistorico.FUTUROS) }
                )
            }
            if (resumoPeriodo.diasIncompletos > 0) {
                ResumoSecundarioChip(
                    emoji = "🔄", label = "Incompletos", valor = resumoPeriodo.diasIncompletos,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.INCOMPLETOS),
                    onClick = { onFiltroClick(FiltroHistorico.INCOMPLETOS) }
                )
            }
            if (resumoPeriodo.diasDescanso > 0) {
                ResumoSecundarioChip(
                    emoji = "🛋️", label = "Descanso", valor = resumoPeriodo.diasDescanso,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.DESCANSO),
                    onClick = { onFiltroClick(FiltroHistorico.DESCANSO) }
                )
            }
            if (resumoPeriodo.diasFeriado > 0) {
                ResumoSecundarioChip(
                    emoji = "🎉", label = "Feriados", valor = resumoPeriodo.diasFeriado,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.FERIADOS),
                    onClick = { onFiltroClick(FiltroHistorico.FERIADOS) }
                )
            }
            if (resumoPeriodo.diasFerias > 0) {
                ResumoSecundarioChip(
                    emoji = "🏖️", label = "Férias", valor = resumoPeriodo.diasFerias,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.FERIAS),
                    onClick = { onFiltroClick(FiltroHistorico.FERIAS) }
                )
            }
            if (resumoPeriodo.temDeclaracoes) {
                ResumoSecundarioChip(
                    emoji = "📄", label = "Declarações", valor = resumoPeriodo.quantidadeDeclaracoes,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.DECLARACOES),
                    onClick = { onFiltroClick(FiltroHistorico.DECLARACOES) }
                )
            }
            if (resumoPeriodo.temAtestados) {
                ResumoSecundarioChip(
                    emoji = "🏥", label = "Atestados", valor = resumoPeriodo.quantidadeAtestados,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.ATESTADOS),
                    onClick = { onFiltroClick(FiltroHistorico.ATESTADOS) }
                )
            }
            if (resumoPeriodo.diasFaltas > 0) {
                ResumoSecundarioChip(
                    emoji = "❌", label = "Faltas", valor = resumoPeriodo.diasFaltas,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.FALTAS),
                    corFundo = Color(0xFFF44336).copy(alpha = 0.1f),
                    onClick = { onFiltroClick(FiltroHistorico.FALTAS) }
                )
            }
            if (resumoPeriodo.diasComProblemas > 0) {
                ResumoSecundarioChip(
                    emoji = "⚠️", label = "Problemas", valor = resumoPeriodo.diasComProblemas,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.COM_PROBLEMAS),
                    corFundo = Color(0xFFFF9800).copy(alpha = 0.1f),
                    onClick = { onFiltroClick(FiltroHistorico.COM_PROBLEMAS) }
                )
            }
        }
    }
}

@Composable
private fun FiltroAtivoIndicator(
    filtros: Set<FiltroHistorico>,
    quantidadeResultados: Int,
    onLimparFiltro: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onLimparFiltro() },
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (filtros.size == 1) "Filtro: ${filtros.first().descricao}" else "${filtros.size} filtros ativos",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = quantidadeResultados.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthNavigator(
    periodoDescricao: String,
    podeIrProximo: Boolean,
    onPeriodoAnterior: () -> Unit,
    onProximoPeriodo: () -> Unit,
    onIrParaAtual: () -> Unit,
    onShowSelector: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onPeriodoAnterior,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .size(40.dp)
            ) {
                Icon(Icons.Default.ChevronLeft, "Anterior")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onShowSelector() }
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = periodoDescricao,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.ArrowDropDown,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onProximoPeriodo,
                enabled = podeIrProximo,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    "Próximo",
                    tint = if (podeIrProximo) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResumoMes(
    resumoPeriodo: ResumoPeriodo,
    saldoInicialPeriodo: Int,
    saldoAcumuladoTotal: Int,
    isPeriodoFuturo: Boolean,
    isExpandido: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isPeriodoFuturo) "Previsão do Período" else "Resumo do Período",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPeriodoFuturo) Color(0xFF9C27B0) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${resumoPeriodo.totalDiasPeriodo} dias",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isExpandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpandido) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpandido,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Row with 5 columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ResumoPrincipalItemV2(
                            label = "Dias úteis",
                            valor = "${resumoPeriodo.diasUteis}d",
                            sublabel = "(${resumoPeriodo.totalHorasUteisMinutos.minutosParaDuracaoCompacta()})",
                            icon = Icons.Default.CalendarMonth,
                            iconColor = Color(0xFF4285F4),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        ResumoPrincipalItemV2(
                            label = "Trabalhado",
                            valor = "${resumoPeriodo.diasTrabalhados}d",
                            sublabel = "(${resumoPeriodo.totalMinutosTrabalhados.minutosParaDuracaoCompacta()})",
                            icon = Icons.Default.Check,
                            iconColor = Color(0xFF34A853),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        ProgressoMeta(
                            progresso = resumoPeriodo.progressoMeta,
                            percentualTexto = resumoPeriodo.porcentagemMeta,
                            descricao = "Progresso",
                            realizado = resumoPeriodo.totalMinutosTrabalhados.minutosParaDuracaoCompacta(),
                            meta = resumoPeriodo.totalHorasUteisMinutos.minutosParaDuracaoCompacta(),
                            tipoInicial = TipoProgresso.CIRCULAR,
                            modifier = Modifier.weight(1f)
                        )

//                        val faltaAbonada =
//                            if (resumoPeriodo.totalMinutosAusenciaAbonada != 0) resumoPeriodo.totalMinutosAusenciaAbonada.toLong()
//                                .minutosParaSaldoFormatado() else ""
//                        val faltaNaoAbonada =
//                            if (resumoPeriodo.totalMinutosAusenciaNaoAbonada != 0) resumoPeriodo.totalMinutosAusenciaNaoAbonada.toLong()
//                                .minutosParaSaldoFormatado() else ""
//                        val temBarraParaFaltas =
//                            if (faltaAbonada != "" && faltaNaoAbonada != "") " / " else ""
//                        ResumoPrincipalItemV2(
//                            label = "Ausências",
//                            valor = "${resumoPeriodo.diasAusenciaTotal}d",
//                            sublabel = "(${resumoPeriodo.ausenciasDescricao})",
//                            sublabel2 = if (resumoPeriodo.diasAusenciaTotal != 0)
//                                "(${faltaAbonada}${temBarraParaFaltas}${faltaNaoAbonada})"
//                            else null,
//                            icon = Icons.Default.PersonRemove, // Assuming this exists or similar
//                            iconColor = Color(0xFFEA4335),
//                            modifier = Modifier.weight(1f)
//                        )
//                        ResumoPrincipalItemV2(
//                            label = "Descanso",
//                            valor = "${resumoPeriodo.diasDescansoTotal}d",
//                            sublabel = "(${resumoPeriodo.descansoDescricao})",
//                            icon = Icons.Default.Luggage,
//                            iconColor = Color(0xFF9C27B0),
//                            modifier = Modifier.weight(1f)
//                        )
//                        ResumoPrincipalItemV2(
//                            label = "Folgas",
//                            valor = "${resumoPeriodo.diasFolgasSemanaisTotal}d",
//                            sublabel = "(Descanso)",
//                            icon = Icons.Default.BeachAccess,
//                            iconColor = Color(0xFFFBBC05),
//                            modifier = Modifier.weight(1f)
//                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Meta and Saldo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(16.dp))

                        Row(
                            modifier = Modifier.weight(3f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ResumoSecundarioItem(
                                label = "Saldo do período",
                                valor = resumoPeriodo.saldoPeriodoMinutos.toLong()
                                    .minutosParaSaldoFormatado(),
                                icon = Icons.AutoMirrored.Filled.CompareArrows,
                                color = if (resumoPeriodo.saldoPeriodoMinutos >= 0) Success else Error
                            )
                            ResumoSecundarioItem(
                                label = "Declaração",
                                valor = resumoPeriodo.totalMinutosDeclaracoes.minutosParaDuracaoCompacta(),
                                icon = Icons.Default.Description,
                                color = Color(0xFF2196F3)
                            )
                            ResumoSecundarioItem(
                                label = "Tolerância",
                                valor = resumoPeriodo.totalMinutosTolerancia.minutosParaDuracaoCompacta(),
                                icon = Icons.Default.HistoryToggleOff,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }

            if (!isPeriodoFuturo) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Navigate to Initial Balance Adjustment? */ }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF34A853).copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                null,
                                modifier = Modifier.padding(8.dp),
                                tint = Color(0xFF34A853)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Saldo inicial",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            saldoInicialPeriodo.toLong().minutosParaSaldoFormatado(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (saldoInicialPeriodo >= 0) Success else Error
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Navigate to History Cycles? */ }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFEA4335).copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                null,
                                modifier = Modifier.padding(8.dp),
                                tint = Color(0xFFEA4335)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Banco de horas",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            saldoAcumuladoTotal.toLong().minutosParaSaldoFormatado(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (saldoAcumuladoTotal >= 0) Success else Error
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏦", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Saldo atual",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        saldoInicialPeriodo.toLong().minutosParaSaldoFormatado(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (saldoInicialPeriodo >= 0) Success else Error
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumoPrincipalItemV2(
    label: String,
    valor: String,
    sublabel: String,
    sublabel2: String? = null,
    sublabel3: String? = null,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .defaultMinSize(0.dp, DEFAULT_MIN_SIZE),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {

            // 🔵 Ícone premium
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .shadow(6.dp, CircleShape, clip = false)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(
                                iconColor.copy(alpha = 0.25f),
                                iconColor.copy(alpha = 0.05f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        0.5.dp,
                        iconColor.copy(alpha = 0.3f),
                        CircleShape
                    )
            ) {
                Icon(
                    icon,
                    null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 🔥 Badge principal
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                iconColor.copy(alpha = 0.25f),
                                iconColor.copy(alpha = 0.10f)
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        0.5.dp,
                        iconColor.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    sublabel,
                    color = iconColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            sublabel2?.let {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        it,
                        color = iconColor,
                        fontSize = 8.sp
                    )
                }
            }

            sublabel3?.let {
                Text(
                    it,
                    color = iconColor,
                    fontSize = 8.sp
                )
            }
        }
    }
}

enum class TipoProgresso {
    LINEAR,
    CIRCULAR
}

private val DEFAULT_MIN_SIZE = 140.dp

@Composable
fun ProgressoMeta(
    progresso: Float,
    percentualTexto: String,
    descricao: String,
    realizado: String,
    meta: String,
    tipoInicial: TipoProgresso = TipoProgresso.LINEAR,
    modifier: Modifier = Modifier,
) {

    var tipo by remember { mutableStateOf(tipoInicial) }
    var larguraPx by remember { mutableStateOf(0f) }

    val density = LocalDensity.current

    val haptic = LocalHapticFeedback.current

    val animatedProgress by animateFloatAsState(
        targetValue = progresso.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "progressAnim"
    )

    // 🎨 Cor dinâmica
    val cor = when {
        progresso >= 1f -> Color(0xFF4CAF50) // verde (meta batida)
        progresso < 0.5f -> Color(0xFFF44336) // vermelho (baixo)
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = DEFAULT_MIN_SIZE),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {

            // 🔥 ANIMAÇÃO ENTRE MODOS
            AnimatedContent(
                targetState = tipo,
                transitionSpec = {
                    fadeIn(tween(300)) + scaleIn(initialScale = 0.92f) togetherWith
                            fadeOut(tween(200)) + scaleOut(targetScale = 1.05f)
                },
                label = "tipoAnim"
            ) { currentTipo ->

                when (currentTipo) {

                    TipoProgresso.LINEAR -> {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Spacer(Modifier.height(15.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(35.dp)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        tipo = TipoProgresso.CIRCULAR
                                    }
                                    .onGloballyPositioned {
                                        larguraPx = it.size.width.toFloat()
                                    }
                            ) {

                                // 🔵 Barra
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(cor.copy(alpha = 0.08f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(animatedProgress)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        cor.copy(alpha = 0.9f),
                                                        cor.copy(alpha = 0.5f)
                                                    )
                                                )
                                            )
                                    )
                                }

                                // 🔥 Tooltip
                                if (larguraPx > 0f) {

                                    val posX = larguraPx * animatedProgress
                                    val tooltipWidthPx = with(density) { 60.dp.toPx() }

                                    val safeOffsetPx = when {
                                        animatedProgress < 0.1f -> 0f
                                        animatedProgress > 0.9f -> larguraPx - tooltipWidthPx
                                        else -> posX - (tooltipWidthPx / 2)
                                    }

                                    val safeOffset = with(density) { safeOffsetPx.toDp() }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .offset(x = safeOffset, y = (-18).dp)
                                    ) {

                                        Box(
                                            modifier = Modifier
                                                .shadow(4.dp, RoundedCornerShape(6.dp))
                                                .background(
                                                    color = cor,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                percentualTexto,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    color = cor,
                                                    shape = RoundedCornerShape(1.dp)
                                                )
                                                .rotate(45f)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                        }
                    }

                    TipoProgresso.CIRCULAR -> {

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(55.dp)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    tipo = TipoProgresso.LINEAR
                                }
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                strokeWidth = 6.dp,
                                color = cor,
                                trackColor = cor.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxSize(),
                            )

                            Text(
                                percentualTexto,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = cor
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                descricao,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(meta)
                Spacer(Modifier.width(4.dp))
                Text("Meta", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(realizado, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text("Feito", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ResumoSecundarioItem(
    label: String,
    valor: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(4.dp))
            }
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 8.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ResumoSecundarioChip(
    emoji: String,
    label: String,
    valor: Int,
    isSelected: Boolean,
    temFuturo: Boolean = false,
    corFundo: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else corFundo,
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                "$valor",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (temFuturo) {
                Text("🔮", fontSize = 10.sp, modifier = Modifier.padding(start = 2.dp))
            }
            Spacer(Modifier.width(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ResumoPrincipalItem(
    label: String,
    valor: String,
    sublabel: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    cor: Color = MaterialTheme.colorScheme.onSurface
) {
    @Suppress("DEPRECATION")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (icon != null) {
            Icon(icon, null, tint = cor, modifier = Modifier.size(20.dp))
        } else if (emoji != null) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = cor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
        sublabel?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DiaCard(
    infoDia: InfoDiaHistorico,
    isExpandido: Boolean,
    saldoBancoAcumulado: Int?,
    onToggleExpansao: () -> Unit,
    onNavigateToDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resumo = infoDia.resumoDia
    val statusColor = getStatusColor(resumo.status)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleExpansao() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                    Text(infoDia.emoji, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            resumo.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            buildString {
                                append(
                                    resumo.data.dayOfWeek.getDisplayName(
                                        TextStyle.FULL,
                                        Locale.forLanguageTag("pt-BR")
                                    ).replaceFirstChar { it.uppercase() })
                                if (infoDia.pontos.isNotEmpty()) {
                                    append(" (${resumo.jornadaPrevistaMinutos.minutosParaDuracaoCompacta()})")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        infoDia.descricaoCurta?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = statusColor
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (infoDia.pontos.isNotEmpty()) {
                            Text(
                                resumo.horasTrabalhadasFormatadas,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                "${resumo.quantidadePontos} ponto(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "Sem registro",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (isExpandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpandido) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpandido,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (resumo.listaInconsistencias.isNotEmpty()) {
                        InconsistenciasSection(resumo.listaInconsistencias)
                        Spacer(Modifier.height(8.dp))
                    }
                    infoDia.feriado?.let { FeriadoInfoSection(it); Spacer(Modifier.height(8.dp)) }
                    infoDia.ausenciaPrincipal?.let {
                        AusenciaInfoSection(it); Spacer(
                        Modifier.height(
                            8.dp
                        )
                    )
                    }
                    if (infoDia.declaracoes.isNotEmpty()) {
                        DeclaracoesSection(infoDia.declaracoes); Spacer(Modifier.height(8.dp))
                    }
                    if (infoDia.intervalos.isNotEmpty()) TurnosSection(infoDia.intervalos)
                    if (infoDia.temIntervalo) {
                        Spacer(Modifier.height(8.dp))
                        IntervaloSection(resumo)
                    }
                    if (infoDia.jornadaCompleta || resumo.isJornadaZerada || infoDia.isSemJornada) {
                        Spacer(Modifier.height(8.dp)); SaldosSection(
                            resumo,
                            saldoBancoAcumulado
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        onClick = onNavigateToDay,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Ver detalhes do dia",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InconsistenciasSection(inconsistencias: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Atenção: Inconsistências",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        inconsistencias.forEach { erro ->
            Text(
                "• $erro",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun FeriadoInfoSection(feriado: Feriado) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF9C27B0).copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            Text("🎉", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    feriado.nome,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9C27B0)
                )
                Text(
                    feriado.tipo.descricao,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AusenciaInfoSection(ausencia: Ausencia) {
    val corFundo = when (ausencia.tipo) {
        TipoAusencia.Ferias -> Color(0xFF00BCD4).copy(alpha = 0.1f)
        TipoAusencia.Atestado -> Color(0xFFE91E63).copy(alpha = 0.1f)
        TipoAusencia.Falta.Justificada -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        TipoAusencia.DayOff -> Color(0xFFFF9800).copy(alpha = 0.1f)
        TipoAusencia.Falta.Injustificada -> Color(0xFFF44336).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val emoji = when (ausencia.tipo) {
        TipoAusencia.Ferias -> "🏖️"
        TipoAusencia.Atestado -> "🏥"
        TipoAusencia.Falta.Justificada -> "📝"
        TipoAusencia.DayOff -> if (ausencia.tipoFolga == TipoFolga.DAY_OFF) "🎁" else "😴"
        TipoAusencia.Falta.Injustificada -> "❌"
        else -> "📄"
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = corFundo,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ausencia.tipoDescricaoCompleta,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                ausencia.observacao?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(ausencia.tipo.impactoResumido, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DeclaracoesSection(declaracoes: List<Ausencia>) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📄", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(4.dp))
                Text(
                    "Declarações (${declaracoes.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(4.dp))
            declaracoes.forEachIndexed { index, decl ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        decl.observacao?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        decl.horaInicio?.let { inicio ->
                            Text(
                                "⏰ ${inicio.format(timeFormatter)} - ${
                                    decl.horaFimDeclaracao?.format(
                                        timeFormatter
                                    ) ?: ""
                                }",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "+${decl.duracaoAbonoMinutos?.minutosParaDuracaoCompacta() ?: "0min"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            "abono",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index < declaracoes.lastIndex) HorizontalDivider(
                    modifier = Modifier.padding(
                        vertical = 4.dp
                    ), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun TurnosSection(intervalos: List<IntervaloPonto>) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            intervalos.forEachIndexed { index, intervalo ->
                val horaEntrada = intervalo.horaEntradaConsiderada?.toLocalTime()
                    ?: intervalo.entrada.horaConsiderada
                val hourStr = "${horaEntrada.format(timeFormatter)} - ${
                    intervalo.saida?.horaConsiderada?.format(timeFormatter) ?: "..."
                }"
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Turno ${index + 1}: $hourStr", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "→ ${intervalo.formatarDuracaoCompacta()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (intervalo.aberto) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                    )
                }
                if (index < intervalos.lastIndex) Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun IntervaloSection(resumo: ResumoDia) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Intervalo real", style = MaterialTheme.typography.bodySmall)
                Text(
                    resumo.minutosIntervaloReal.minutosParaDuracaoCompacta(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (resumo.minutosIntervaloReal != resumo.minutosIntervaloConsiderado) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Intervalo considerado", style = MaterialTheme.typography.bodySmall)
                    Text(
                        resumo.minutosIntervaloConsiderado.minutosParaDuracaoCompacta(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (resumo.temToleranciaIntervaloAplicada) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tolerância aplicada", style = MaterialTheme.typography.bodySmall)
                    Text(
                        (resumo.minutosIntervaloReal - resumo.minutosIntervaloConsiderado)
                            .coerceAtLeast(0)
                            .minutosParaDuracaoCompacta(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Warning
                    )
                }
            }
        }
    }
}

@Composable
private fun SaldosSection(
    resumo: ResumoDia,
    saldoBancoAcumulado: Int? = null
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Saldo do dia", style = MaterialTheme.typography.bodySmall)
                Text(
                    resumo.saldoDiaFormatado,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (resumo.saldoDiaMinutos >= 0) Success else Error
                )
            }
            if (saldoBancoAcumulado != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Banco de horas", style = MaterialTheme.typography.bodySmall)
                    Text(
                        saldoBancoAcumulado.toLong().minutosParaSaldoFormatado(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (saldoBancoAcumulado >= 0) Success else Error
                    )
                }
            }
        }
    }
}

private fun getStatusColor(status: StatusResumoDia): Color {
    return when (status) {
        StatusResumoDia.SEM_REGISTRO -> SidiaMediumGray

        StatusResumoDia.FOLGA,
        StatusResumoDia.DESCANSO -> Color(0xFF9C27B0)

        StatusResumoDia.ABONADO -> Info

        StatusResumoDia.FALTA -> Error

        StatusResumoDia.POSITIVO -> Success

        StatusResumoDia.NEGATIVO -> Warning

        StatusResumoDia.NEUTRO -> SidiaMediumGray
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryContentPreview() {
    MeuPontoTheme {
        HistoryContent(
            uiState = HistoryUiState(),
            onNavigateBack = {},
            onNavigateToDay = {},
            onNovaAusencia = {},
            onNovoFeriado = {},
            onPeriodoAnterior = {},
            onProximoPeriodo = {},
            onIrParaAtual = {},
            onFiltroSelecionado = {},
            onToggleDiaExpandido = {},
            onToggleResumoExpandido = {},
            onToggleVisualizacao = {},
            onLimparFiltros = {},
            onExportar = {},
            onTogglePeriodoSelection = {},
            onShowPeriodoSelector = {})
    }
}
