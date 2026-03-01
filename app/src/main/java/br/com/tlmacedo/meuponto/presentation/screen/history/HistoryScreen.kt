// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.StatusDiaResumo
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.presentation.components.EmptyState
import br.com.tlmacedo.meuponto.presentation.components.LoadingIndicator
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.util.minutosParaDuracaoCompacta
import br.com.tlmacedo.meuponto.util.minutosParaSaldoFormatado
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Tela de histórico de registros de ponto.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 7.8.0 - Adicionado filtro de dias futuros no resumo secundário
 */
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDay: (LocalDate) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparErro()
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Histórico",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                periodoSubtitulo = uiState.periodoSubtitulo,
                onPeriodoAnterior = viewModel::periodoAnterior,
                onProximoPeriodo = viewModel::proximoPeriodo,
                onIrParaAtual = viewModel::irParaPeriodoAtual
            )

            if (uiState.hasRegistros && !uiState.isLoading) {
                ResumoMes(
                    resumoPeriodo = uiState.resumoPeriodo,
                    totalDiasPeriodo = uiState.periodoSelecionado.totalDias,
                    saldoInicialPeriodo = uiState.saldoInicialPeriodo,
                    saldoAcumuladoTotal = uiState.saldoAcumuladoTotal,
                    filtroAtivo = uiState.filtroAtivo,
                    isPeriodoFuturo = uiState.periodoSelecionado.isFuturo,
                    onFiltroClick = viewModel::alterarFiltro
                )
            }

            if (uiState.filtroAtivo != FiltroHistorico.TODOS) {
                FiltroAtivoIndicator(
                    filtro = uiState.filtroAtivo,
                    quantidadeResultados = uiState.registrosFiltrados.size,
                    onLimparFiltro = { viewModel.alterarFiltro(FiltroHistorico.TODOS) }
                )
            }

            FiltrosChips(
                filtroAtivo = uiState.filtroAtivo,
                onFiltroSelecionado = viewModel::alterarFiltro
            )

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.registrosFiltrados.isEmpty() -> {
                    EmptyState(
                        title = if (uiState.filtroAtivo != FiltroHistorico.TODOS)
                            "Nenhum resultado"
                        else if (uiState.hasRegistros)
                            "Nenhum registro encontrado"
                        else
                            "Sem registros",
                        message = when {
                            uiState.filtroAtivo != FiltroHistorico.TODOS ->
                                "Não há dias com \"${uiState.filtroAtivo.descricao}\" neste período"

                            uiState.hasRegistros ->
                                "Não há registros com o filtro selecionado"

                            else ->
                                "Nenhum ponto registrado neste período"
                        },
                        icon = Icons.Outlined.CalendarMonth
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.registrosFiltrados,
                            key = { it.data.toString() }
                        ) { infoDia ->
                            DiaCard(
                                infoDia = infoDia,
                                isExpandido = uiState.diaExpandido == infoDia.data,
                                saldoBancoAcumulado = uiState.saldoAcumuladoAte(infoDia.data),
                                onToggleExpansao = { viewModel.toggleDiaExpandido(infoDia.data) },
                                onNavigateToDay = { onNavigateToDay(infoDia.data) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroAtivoIndicator(
    filtro: FiltroHistorico,
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
                filtro.emoji?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = "Filtrando: ${filtro.descricao}",
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
                        text = "$quantidadeResultados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            IconButton(onClick = onLimparFiltro, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Limpar filtro",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MonthNavigator(
    periodoSelecionado: PeriodoHistorico,
    podeIrProximo: Boolean,
    isPeriodoAtual: Boolean,
    periodoSubtitulo: String?,
    onPeriodoAnterior: () -> Unit,
    onProximoPeriodo: () -> Unit,
    onIrParaAtual: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onPeriodoAnterior) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Período anterior")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .then(if (!isPeriodoAtual) Modifier.clickable { onIrParaAtual() } else Modifier)
            ) {
                Text(
                    text = periodoSelecionado.descricaoFormatada,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                periodoSubtitulo?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isPeriodoAtual) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Today, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Toque para ir ao atual",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(onClick = onProximoPeriodo, enabled = podeIrProximo) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo período",
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
    filtroAtivo: FiltroHistorico,
    isPeriodoFuturo: Boolean,
    onFiltroClick: (FiltroHistorico) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    cor = MaterialTheme.colorScheme.tertiary
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
                        cor = if (resumoPeriodo.saldoPeriodoMinutos >= 0) Color(0xFF4CAF50) else Color(
                            0xFFF44336
                        )
                    )
                    ResumoPrincipalItem(
                        label = "Completos",
                        valor = "${resumoPeriodo.diasCompletos}",
                        sublabel = "dias",
                        icon = Icons.Default.CheckCircle,
                        cor = Color(0xFF4CAF50)
                    )
                }
            }

            // Resumo secundário - Filtros clicáveis
            val temItensSecundarios = resumoPeriodo.temDiasFuturos ||
                    resumoPeriodo.totalDescanso > 0 ||
                    resumoPeriodo.totalFeriados > 0 ||
                    resumoPeriodo.totalFerias > 0 ||
                    resumoPeriodo.diasFolgaDayOff > 0 ||
                    resumoPeriodo.diasFolgaCompensacao > 0 ||
                    resumoPeriodo.temAtestados ||
                    resumoPeriodo.totalDiasFaltas > 0 ||
                    resumoPeriodo.diasFolgaFuturo > 0

            if (temItensSecundarios) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Detalhes (toque para filtrar)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Chip de Futuros - primeiro item
                    if (resumoPeriodo.temDiasFuturos) {
                        ResumoSecundarioChip(
                            emoji = "🔮",
                            label = "Futuros",
                            valor = resumoPeriodo.diasFuturos,
                            isSelected = filtroAtivo == FiltroHistorico.FUTUROS,
                            corFundo = Color(0xFF9C27B0).copy(alpha = 0.1f),
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.FUTUROS)
                                        FiltroHistorico.TODOS else FiltroHistorico.FUTUROS
                                )
                            }
                        )
                    }

                    if (resumoPeriodo.totalDescanso > 0) {
                        ResumoSecundarioChip(
                            emoji = "🛋️",
                            label = "Descanso",
                            valor = resumoPeriodo.totalDescanso,
                            temFuturo = resumoPeriodo.diasDescansoFuturo > 0,
                            isSelected = filtroAtivo == FiltroHistorico.DESCANSO,
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.DESCANSO)
                                        FiltroHistorico.TODOS else FiltroHistorico.DESCANSO
                                )
                            }
                        )
                    }
                    if (resumoPeriodo.totalFeriados > 0) {
                        ResumoSecundarioChip(
                            emoji = "🎉",
                            label = "Feriados",
                            valor = resumoPeriodo.totalFeriados,
                            temFuturo = resumoPeriodo.diasFeriadoFuturo > 0,
                            isSelected = filtroAtivo == FiltroHistorico.FERIADOS,
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.FERIADOS)
                                        FiltroHistorico.TODOS else FiltroHistorico.FERIADOS
                                )
                            }
                        )
                    }
                    if (resumoPeriodo.totalFerias > 0) {
                        ResumoSecundarioChip(
                            emoji = "🏖️",
                            label = "Férias",
                            valor = resumoPeriodo.totalFerias,
                            temFuturo = resumoPeriodo.diasFeriasFuturo > 0,
                            isSelected = filtroAtivo == FiltroHistorico.FERIAS,
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.FERIAS)
                                        FiltroHistorico.TODOS else FiltroHistorico.FERIAS
                                )
                            }
                        )
                    }
                    if (resumoPeriodo.diasFolgaDayOff > 0) {
                        ResumoSecundarioChip(
                            emoji = "🎁",
                            label = "Day-off",
                            valor = resumoPeriodo.diasFolgaDayOff,
                            isSelected = filtroAtivo == FiltroHistorico.DAY_OFF,
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.DAY_OFF)
                                        FiltroHistorico.TODOS else FiltroHistorico.DAY_OFF
                                )
                            }
                        )
                    }
                    if (resumoPeriodo.diasFolgaCompensacao > 0 || resumoPeriodo.diasFolgaFuturo > 0) {
                        ResumoSecundarioChip(
                            emoji = "😴",
                            label = "Folgas",
                            valor = resumoPeriodo.diasFolgaCompensacao + resumoPeriodo.diasFolgaFuturo,
                            temFuturo = resumoPeriodo.diasFolgaFuturo > 0,
                            isSelected = filtroAtivo == FiltroHistorico.FOLGAS,
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.FOLGAS)
                                        FiltroHistorico.TODOS else FiltroHistorico.FOLGAS
                                )
                            }
                        )
                    }
                    if (resumoPeriodo.temAtestados) {
                        ResumoSecundarioChip(
                            emoji = "🏥",
                            label = "Atestados",
                            valor = resumoPeriodo.quantidadeAtestados,
                            isSelected = filtroAtivo == FiltroHistorico.ATESTADOS,
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.ATESTADOS)
                                        FiltroHistorico.TODOS else FiltroHistorico.ATESTADOS
                                )
                            }
                        )
                    }
                    if (resumoPeriodo.temDeclaracoes) {
                        ResumoSecundarioChip(
                            emoji = "📄",
                            label = "Declarações",
                            valor = resumoPeriodo.quantidadeDeclaracoes,
                            isSelected = filtroAtivo == FiltroHistorico.DECLARACOES,
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.DECLARACOES)
                                        FiltroHistorico.TODOS else FiltroHistorico.DECLARACOES
                                )
                            }
                        )
                    }
                    if (resumoPeriodo.totalDiasFaltas > 0) {
                        ResumoSecundarioChip(
                            emoji = "❌",
                            label = "Faltas",
                            valor = resumoPeriodo.totalDiasFaltas,
                            isSelected = filtroAtivo == FiltroHistorico.FALTAS,
                            corFundo = Color(0xFFF44336).copy(alpha = 0.1f),
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.FALTAS)
                                        FiltroHistorico.TODOS else FiltroHistorico.FALTAS
                                )
                            }
                        )
                    }
                    if (resumoPeriodo.diasComProblemas > 0) {
                        ResumoSecundarioChip(
                            emoji = "⚠️",
                            label = "Problemas",
                            valor = resumoPeriodo.diasComProblemas,
                            isSelected = filtroAtivo == FiltroHistorico.COM_PROBLEMAS,
                            corFundo = Color(0xFFFF9800).copy(alpha = 0.1f),
                            onClick = {
                                onFiltroClick(
                                    if (filtroAtivo == FiltroHistorico.COM_PROBLEMAS)
                                        FiltroHistorico.TODOS else FiltroHistorico.COM_PROBLEMAS
                                )
                            }
                        )
                    }
                }
            }

            // Saldo acumulado
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
                    Text(
                        saldoInicialPeriodo.toLong().minutosParaSaldoFormatado(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (saldoInicialPeriodo == 0) Color(0xFFDFE1DF)
                        else if (saldoInicialPeriodo >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
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
                        color = if (saldoAcumuladoTotal >= 0) Color(0xFF4CAF50) else Color(
                            0xFFF44336
                        )
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
                        color = if (saldoInicialPeriodo >= 0) Color(0xFF4CAF50) else Color(
                            0xFFF44336
                        )
                    )
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
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (temFuturo) {
                Text(
                    "🔮",
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            Spacer(Modifier.width(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun FiltrosChips(
    filtroAtivo: FiltroHistorico,
    onFiltroSelecionado: (FiltroHistorico) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(FiltroHistorico.principais) { filtro ->
            FilterChip(
                selected = filtro == filtroAtivo,
                onClick = { onFiltroSelecionado(filtro) },
                label = { Text(filtro.descricao) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
    onNavigateToDay: () -> Unit
) {
    val resumo = infoDia.resumoDia
    val statusColor = getStatusColor(resumo.statusDia)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
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
                                        Locale("pt", "BR")
                                    ).replaceFirstChar { it.uppercase() }
                                )
                                if (infoDia.pontos.size > 0) append(" (${resumo.cargaHorariaDiariaFormatada})")
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
                        if (infoDia.declaracoes.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📄", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${infoDia.declaracoes.size} declaração(ões) - ${infoDia.totalMinutosDeclaracoes.minutosParaDuracaoCompacta()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.End) {
                        when {
                            resumo.jornadaCompleta -> {
                                Text(
                                    resumo.horasTrabalhadasFormatadas,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Text(
                                    "${resumo.quantidadePontos} pontos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            resumo.pontos.isNotEmpty() -> {
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
                            }

                            else -> Text(
                                "Sem registro",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (isExpandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpandido) "Colapsar" else "Expandir",
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
                    infoDia.feriado?.let {
                        FeriadoInfoSection(it)
                        Spacer(Modifier.height(8.dp))
                    }
                    infoDia.ausenciaPrincipal?.let {
                        AusenciaInfoSection(it)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (infoDia.declaracoes.isNotEmpty()) {
                        DeclaracoesSection(infoDia.declaracoes)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (resumo.intervalos.isNotEmpty()) TurnosSection(resumo.intervalos)
                    if (resumo.temIntervalo) {
                        Spacer(Modifier.height(8.dp))
                        IntervaloSection(resumo)
                    }
                    if (resumo.jornadaCompleta || resumo.isJornadaZerada || infoDia.isSemJornada) {
                        Spacer(Modifier.height(8.dp))
                        SaldosSection(resumo, saldoBancoAcumulado, infoDia.isSemJornada)
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
                if (ausencia.tipo == TipoAusencia.FOLGA && ausencia.tipoFolga == TipoFolga.COMPENSACAO) {
                    Text(
                        "⚠️ Desconta do banco de horas",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9800)
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
            declaracoes.forEachIndexed { index, declaracao ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        declaracao.observacao?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                        declaracao.horaInicio?.let { inicio ->
                            Text(
                                "⏰ ${inicio.format(timeFormatter)} - ${
                                    declaracao.horaFimDeclaracao?.format(
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
                            "+${declaracao.duracaoAbonoMinutos?.minutosParaDuracaoCompacta() ?: "0min"}",
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
                if (index < declaracoes.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
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
                val turnoNum = index + 1
                val horaEntradaReal = intervalo.entrada.dataHora.toLocalTime().format(timeFormatter)
                val horaSaidaReal =
                    intervalo.saida?.dataHora?.toLocalTime()?.format(timeFormatter) ?: "..."
                val horaEntradaConsiderada = intervalo.entrada.horaConsiderada.format(timeFormatter)
                val horaSaidaConsiderada =
                    intervalo.saida?.horaConsiderada?.format(timeFormatter) ?: "..."
                val temToleranciaEntrada = intervalo.entrada.temAjusteTolerancia
                val temToleranciaSaida = intervalo.saida?.temAjusteTolerancia == true
                val temAlgumaTolerancia = temToleranciaEntrada || temToleranciaSaida
                val duracao = intervalo.formatarDuracaoCompacta()

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Turno $turnoNum:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            if (temAlgumaTolerancia) {
                                Text(
                                    "$horaEntradaConsiderada - $horaSaidaConsiderada",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Schedule, "Tolerância",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Text(
                                    "$horaEntradaReal - $horaSaidaReal",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Text(
                            "→ $duracao",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (intervalo.aberto) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                    if (temAlgumaTolerancia) {
                        Spacer(Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 48.dp)
                        ) {
                            Text("⏱️", style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Real: $horaEntradaReal - $horaSaidaReal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (index < intervalos.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun IntervaloSection(resumo: ResumoDia) {
    val temTolerancia = resumo.temToleranciaIntervaloAplicada
    val diferencaMinutos = resumo.minutosIntervaloReal - resumo.minutosIntervaloTotal

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                "Intervalo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱️", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Real",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    resumo.minutosIntervaloReal.minutosParaDuracaoCompacta(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (temTolerancia) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (temTolerancia) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            if (temTolerancia) {
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Considerado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        resumo.minutosIntervaloTotal.minutosParaDuracaoCompacta(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Tolerância: ${diferencaMinutos}min desconsiderados",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaldosSection(
    resumo: ResumoDia,
    saldoBancoAcumulado: Int? = null,
    isSemJornada: Boolean = false
) {
    val saldoDiaColor = when {
        isSemJornada -> MaterialTheme.colorScheme.onSurfaceVariant
        resumo.temSaldoPositivo || !resumo.temSaldoNegativo -> Color(0xFF4CAF50)
        else -> Color(0xFFF44336)
    }
    val saldoBancoColor =
        saldoBancoAcumulado?.let { if (it >= 0) Color(0xFF4CAF50) else Color(0xFFF44336) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            if (!isSemJornada || resumo.pontos.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (resumo.isJornadaZerada && resumo.pontos.isNotEmpty()) "Horas extras" else "Saldo do dia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        resumo.saldoDiaFormatado,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = saldoDiaColor
                    )
                }
            }
            if (saldoBancoAcumulado != null) {
                if (!isSemJornada || resumo.pontos.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏦", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Banco de horas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        saldoBancoAcumulado.minutosParaSaldoFormatado(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = saldoBancoColor ?: MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun Int.minutosParaSaldoFormatado(): String = this.toLong().minutosParaSaldoFormatado()

private fun getStatusColor(status: StatusDiaResumo): Color {
    return when (status) {
        StatusDiaResumo.COMPLETO -> Color(0xFF4CAF50)
        StatusDiaResumo.EM_ANDAMENTO -> Color(0xFF2196F3)
        StatusDiaResumo.INCOMPLETO -> Color(0xFFFF9800)
        StatusDiaResumo.COM_PROBLEMAS -> Color(0xFFF44336)
        StatusDiaResumo.SEM_REGISTRO -> Color(0xFF9E9E9E)
        StatusDiaResumo.FERIADO -> Color(0xFF9C27B0)
        StatusDiaResumo.FERIADO_TRABALHADO -> Color(0xFFFF9800)
        StatusDiaResumo.FUTURO -> Color(0xFF78909C)
    }
}
