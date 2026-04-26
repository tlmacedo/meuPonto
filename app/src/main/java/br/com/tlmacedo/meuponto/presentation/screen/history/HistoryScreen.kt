// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.StatusDiaResumo
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
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
                periodoSelecionado = uiState.periodoSelecionado,
                podeIrProximo = uiState.podeIrProximoPeriodo,
                isPeriodoAtual = uiState.isPeriodoAtual,
                onPeriodoAnterior = onPeriodoAnterior,
                onProximoPeriodo = onProximoPeriodo,
                onIrParaAtual = onIrParaAtual
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (uiState.hasRegistros && !uiState.isLoading) {
                    item {
                        ResumoMes(
                            resumoPeriodo = uiState.resumoPeriodo,
                            totalDiasPeriodo = uiState.periodoSelecionado.totalDias,
                            saldoInicialPeriodo = uiState.saldoInicialPeriodo,
                            saldoAcumuladoTotal = uiState.saldoAcumuladoTotal,
                            isPeriodoFuturo = uiState.periodoSelecionado.isFuturo,
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
                        item {
                            CalendarView(
                                yearMonth = YearMonth.from(uiState.periodoSelecionado.dataInicio),
                                diasHistorico = uiState.diasHistorico,
                                filtrosAtivos = uiState.filtrosAtivos,
                                onDateClick = { date -> onNavigateToDay(date) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
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
                                isExpandido = uiState.diaExpandido == infoDia.data,
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
            if (resumoPeriodo.temDiasFuturos) {
                ResumoSecundarioChip(
                    emoji = "🔮", label = "Futuros", valor = resumoPeriodo.diasFuturos,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.FUTUROS),
                    corFundo = Color(0xFF9C27B0).copy(alpha = 0.1f),
                    onClick = { onFiltroClick(FiltroHistorico.FUTUROS) }
                )
            }
            if (resumoPeriodo.totalDescanso > 0) {
                ResumoSecundarioChip(
                    emoji = "🛋️", label = "Descanso", valor = resumoPeriodo.totalDescanso,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.DESCANSO),
                    onClick = { onFiltroClick(FiltroHistorico.DESCANSO) }
                )
            }
            if (resumoPeriodo.totalFeriados > 0) {
                ResumoSecundarioChip(
                    emoji = "🎉", label = "Feriados", valor = resumoPeriodo.totalFeriados,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.FERIADOS),
                    onClick = { onFiltroClick(FiltroHistorico.FERIADOS) }
                )
            }
            if (resumoPeriodo.totalFerias > 0) {
                ResumoSecundarioChip(
                    emoji = "🏖️", label = "Férias", valor = resumoPeriodo.totalFerias,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.FERIAS),
                    onClick = { onFiltroClick(FiltroHistorico.FERIAS) }
                )
            }
            if (resumoPeriodo.diasFolgaDayOff > 0) {
                ResumoSecundarioChip(
                    emoji = "🎁", label = "Day-off", valor = resumoPeriodo.diasFolgaDayOff,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.DAY_OFF),
                    onClick = { onFiltroClick(FiltroHistorico.DAY_OFF) }
                )
            }
            if (resumoPeriodo.diasFolgaCompensacao > 0 || resumoPeriodo.diasFolgaFuturo > 0) {
                ResumoSecundarioChip(
                    emoji = "😴",
                    label = "Folgas",
                    valor = resumoPeriodo.diasFolgaCompensacao + resumoPeriodo.diasFolgaFuturo,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.FOLGAS),
                    onClick = { onFiltroClick(FiltroHistorico.FOLGAS) }
                )
            }
            if (resumoPeriodo.temAtestados) {
                ResumoSecundarioChip(
                    emoji = "🏥", label = "Atestados", valor = resumoPeriodo.quantidadeAtestados,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.ATESTADOS),
                    onClick = { onFiltroClick(FiltroHistorico.ATESTADOS) }
                )
            }
            if (resumoPeriodo.temDeclaracoes) {
                ResumoSecundarioChip(
                    emoji = "📄", label = "Declarações", valor = resumoPeriodo.quantidadeDeclaracoes,
                    isSelected = filtrosAtivos.contains(FiltroHistorico.DECLARACOES),
                    onClick = { onFiltroClick(FiltroHistorico.DECLARACOES) }
                )
            }
            if (resumoPeriodo.totalDiasFaltas > 0) {
                ResumoSecundarioChip(
                    emoji = "❌", label = "Faltas", valor = resumoPeriodo.totalDiasFaltas,
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

            // Filtros que eram principais
            ResumoSecundarioChip(
                emoji = "✅", label = "Completos", valor = resumoPeriodo.diasCompletos,
                isSelected = filtrosAtivos.contains(FiltroHistorico.COMPLETOS),
                onClick = { onFiltroClick(FiltroHistorico.COMPLETOS) }
            )
            ResumoSecundarioChip(
                emoji = "🔄", label = "Incompletos", valor = resumoPeriodo.diasUteisSemRegistro,
                isSelected = filtrosAtivos.contains(FiltroHistorico.INCOMPLETOS),
                onClick = { onFiltroClick(FiltroHistorico.INCOMPLETOS) }
            )
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
    periodoSelecionado: PeriodoHistorico,
    podeIrProximo: Boolean,
    isPeriodoAtual: Boolean,
    onPeriodoAnterior: () -> Unit,
    onProximoPeriodo: () -> Unit,
    onIrParaAtual: () -> Unit
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
                    .clickable(enabled = !isPeriodoAtual) { onIrParaAtual() }
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
                    text = periodoSelecionado.descricaoFormatada,
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
    totalDiasPeriodo: Int,
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
                        if (isPeriodoFuturo) {
                            Spacer(Modifier.width(8.dp))
                            Text("🔮", fontSize = 14.sp)
                        }
                    }
                    if (!isExpandido && !isPeriodoFuturo) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📊", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Saldo anterior",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val anteriorColor =
                                if (saldoInicialPeriodo == 0) MaterialTheme.colorScheme.outline else if (saldoInicialPeriodo >= 0) Success else Error
                            Text(
                                saldoInicialPeriodo.toLong().minutosParaSaldoFormatado(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = anteriorColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏦", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Saldo acumulado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                saldoAcumuladoTotal.toLong().minutosParaSaldoFormatado(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (saldoAcumuladoTotal >= 0) Success else Error
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (isExpandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpandido) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpandido,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ResumoPrincipalItem(
                            label = "Dias úteis",
                            valor = "${resumoPeriodo.diasUteis}",
                            sublabel = if (isPeriodoFuturo) "previstos" else "no período",
                            icon = Icons.Default.CalendarMonth,
                            cor = MaterialTheme.colorScheme.onSurface
                        )

                        if (!isPeriodoFuturo) {
                            ResumoPrincipalItem(
                                label = "Trabalhado",
                                valor = resumoPeriodo.totalMinutosTrabalhados.minutosParaDuracaoCompacta(),
                                sublabel = null,
                                icon = Icons.Default.Schedule,
                                cor = MaterialTheme.colorScheme.primary
                            )
                            ResumoPrincipalItem(
                                label = "Tolerância",
                                valor = if (resumoPeriodo.temToleranciaAplicada)
                                    resumoPeriodo.totalMinutosTolerancia.minutosParaDuracaoCompacta()
                                else "—",
                                sublabel = if (resumoPeriodo.temToleranciaAplicada) "aplicada" else null,
                                icon = Icons.Default.Schedule,
                                cor = if (resumoPeriodo.temToleranciaAplicada)
                                    Color(0xFF9C27B0) else MaterialTheme.colorScheme.outline
                            )
                        } else {
                            ResumoPrincipalItem(
                                label = "Total",
                                valor = "$totalDiasPeriodo",
                                sublabel = "dias",
                                icon = Icons.Default.CalendarMonth,
                                cor = MaterialTheme.colorScheme.outline
                            )
                            ResumoPrincipalItem(
                                label = "Especiais",
                                valor = "${resumoPeriodo.totalFeriados + resumoPeriodo.totalFerias}",
                                sublabel = "feriados/férias",
                                icon = null,
                                emoji = "🎉",
                                cor = Color(0xFF9C27B0)
                            )
                        }
                    }

                    if (!isPeriodoFuturo) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ResumoPrincipalItem(
                                label = "Declarações",
                                valor = if (resumoPeriodo.temDeclaracoes)
                                    resumoPeriodo.totalMinutosDeclaracoes.minutosParaDuracaoCompacta()
                                else "—",
                                sublabel = if (resumoPeriodo.temDeclaracoes)
                                    "${resumoPeriodo.quantidadeDeclaracoes} decl." else null,
                                icon = null,
                                emoji = "📄",
                                cor = if (resumoPeriodo.temDeclaracoes)
                                    Color(0xFF2196F3) else MaterialTheme.colorScheme.outline
                            )
                            ResumoPrincipalItem(
                                label = "Saldo",
                                valor = resumoPeriodo.saldoPeriodoMinutos.toLong()
                                    .minutosParaSaldoFormatado(),
                                sublabel = "do período",
                                icon = if (resumoPeriodo.saldoPeriodoMinutos >= 0)
                                    Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                cor = if (resumoPeriodo.saldoPeriodoMinutos >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                            )
                            ResumoPrincipalItem(
                                label = "Completos",
                                valor = "${resumoPeriodo.diasCompletos}",
                                sublabel = "dias",
                                icon = Icons.Default.CheckCircle,
                                cor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    if (!isPeriodoFuturo) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📊", style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Saldo anterior",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val anteriorColor =
                                if (saldoInicialPeriodo == 0) MaterialTheme.colorScheme.outline else if (saldoInicialPeriodo >= 0) Success else Error
                            Text(
                                saldoInicialPeriodo.toLong().minutosParaSaldoFormatado(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = anteriorColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏦", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Saldo acumulado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                saldoAcumuladoTotal.toLong().minutosParaSaldoFormatado(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (saldoAcumuladoTotal >= 0) Success else Error
                            )
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
    icon: ImageVector?,
    emoji: String? = null,
    cor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
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
            maxLines = 1
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        sublabel?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp,
                maxLines = 1
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
    val statusColor = getStatusColor(resumo.statusDia)

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
                                if (infoDia.pontos.isNotEmpty()) append(" (${resumo.cargaHorariaDiariaFormatada})")
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
                        if (resumo.pontos.isNotEmpty()) {
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
                    if (resumo.intervalos.isNotEmpty()) TurnosSection(resumo.intervalos)
                    if (resumo.temIntervalo) {
                        Spacer(Modifier.height(8.dp)); IntervaloSection(resumo)
                    }
                    if (resumo.jornadaCompleta || resumo.isJornadaZerada || infoDia.isSemJornada) {
                        Spacer(Modifier.height(8.dp)); SaldosSection(
                            resumo,
                            saldoBancoAcumulado,
                            infoDia.isSemJornada
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
        TipoAusencia.FERIAS -> Color(0xFF00BCD4).copy(alpha = 0.1f)
        TipoAusencia.ATESTADO -> Color(0xFFE91E63).copy(alpha = 0.1f)
        TipoAusencia.FALTA_JUSTIFICADA -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        TipoAusencia.FOLGA -> Color(0xFFFF9800).copy(alpha = 0.1f)
        TipoAusencia.FALTA_INJUSTIFICADA -> Color(0xFFF44336).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val emoji = when (ausencia.tipo) {
        TipoAusencia.FERIAS -> "🏖️"
        TipoAusencia.ATESTADO -> "🏥"
        TipoAusencia.FALTA_JUSTIFICADA -> "📝"
        TipoAusencia.FOLGA -> if (ausencia.tipoFolga == TipoFolga.DAY_OFF) "🎁" else "😴"
        TipoAusencia.FALTA_INJUSTIFICADA -> "❌"
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
                val hourStr = "${intervalo.entrada.horaConsiderada.format(timeFormatter)} - ${
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
        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Intervalo Real", style = MaterialTheme.typography.bodySmall)
            Text(
                resumo.minutosIntervaloReal.minutosParaDuracaoCompacta(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SaldosSection(
    resumo: ResumoDia,
    saldoBancoAcumulado: Int? = null,
    isSemJornada: Boolean = false
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
                    Text("Acumulado", style = MaterialTheme.typography.bodySmall)
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

private fun getStatusColor(status: StatusDiaResumo): Color = when (status) {
    StatusDiaResumo.DESCANSO, StatusDiaResumo.FERIADO -> Color(0xFF9C27B0)
    StatusDiaResumo.COMPLETO -> Success
    StatusDiaResumo.EM_ANDAMENTO -> Info
    StatusDiaResumo.INCOMPLETO, StatusDiaResumo.FERIADO_TRABALHADO -> Warning
    StatusDiaResumo.COM_PROBLEMAS -> Error
    else -> SidiaMediumGray
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
            onExportar = {})
    }
}
