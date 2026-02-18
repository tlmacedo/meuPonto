// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.StatusDiaResumo
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
 * @updated 3.2.0 - Layout compacto com turnos e intervalos detalhados
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
                mesSelecionado = uiState.mesSelecionado,
                podeIrProximo = uiState.podeIrProximoMes,
                onMesAnterior = viewModel::mesAnterior,
                onProximoMes = viewModel::proximoMes
            )

            if (uiState.hasRegistros && !uiState.isLoading) {
                ResumoMes(
                    totalMinutos = uiState.totalMinutosTrabalhados,
                    saldoMinutos = uiState.saldoTotalMinutos,
                    diasCompletos = uiState.diasCompletos,
                    diasComProblemas = uiState.diasComProblemas
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
                        title = if (uiState.hasRegistros) "Nenhum registro encontrado" else "Sem registros",
                        message = if (uiState.hasRegistros)
                            "Não há registros com o filtro selecionado"
                        else
                            "Nenhum ponto registrado neste mês",
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
                        ) { resumo ->
                            DiaCard(
                                resumo = resumo,
                                isExpandido = uiState.diaExpandido == resumo.data,
                                onToggleExpansao = { viewModel.toggleDiaExpandido(resumo.data) },
                                onNavigateToDay = { onNavigateToDay(resumo.data) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Navegador de mês com setas e nome do mês.
 */
@Composable
private fun MonthNavigator(
    mesSelecionado: LocalDate,
    podeIrProximo: Boolean,
    onMesAnterior: () -> Unit,
    onProximoMes: () -> Unit
) {
    val formatador = remember {
        DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))
    }

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
            IconButton(onClick = onMesAnterior) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Mês anterior"
                )
            }

            Text(
                text = mesSelecionado.format(formatador).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = onProximoMes,
                enabled = podeIrProximo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Próximo mês",
                    tint = if (podeIrProximo)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Card de resumo do mês.
 */
@Composable
private fun ResumoMes(
    totalMinutos: Int,
    saldoMinutos: Int,
    diasCompletos: Int,
    diasComProblemas: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ResumoItem(
                label = "Trabalhado",
                valor = totalMinutos.minutosParaDuracaoCompacta(),
                icon = Icons.Default.Schedule,
                cor = MaterialTheme.colorScheme.primary
            )
            ResumoItem(
                label = "Saldo",
                valor = saldoMinutos.toLong().minutosParaSaldoFormatado(),
                icon = if (saldoMinutos >= 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                cor = if (saldoMinutos >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            ResumoItem(
                label = "Completos",
                valor = "$diasCompletos dias",
                icon = Icons.Default.CheckCircle,
                cor = Color(0xFF4CAF50)
            )
            if (diasComProblemas > 0) {
                ResumoItem(
                    label = "Problemas",
                    valor = "$diasComProblemas dias",
                    icon = Icons.Default.Error,
                    cor = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun ResumoItem(
    label: String,
    valor: String,
    icon: ImageVector,
    cor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = cor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Chips de filtro horizontal.
 */
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
        items(FiltroHistorico.entries) { filtro ->
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

/**
 * Card de um dia - Layout compacto com turnos.
 */
@Composable
private fun DiaCard(
    resumo: ResumoDia,
    isExpandido: Boolean,
    onToggleExpansao: () -> Unit,
    onNavigateToDay: () -> Unit
) {
    val status = resumo.statusDia
    val statusIcon = getStatusIcon(status)
    val statusColor = getStatusColor(status)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpansao() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ================================================================
            // HEADER: Ícone + Data + Horas trabalhadas
            // ================================================================
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Lado esquerdo: Ícone + Data + Dia da semana
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = statusIcon,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        // Linha 1: Data + Horas trabalhadas
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = resumo.data.format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        // Linha 2: Dia da semana + (carga horária) + pontos
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = buildString {
                                    append(
                                        resumo.data.dayOfWeek.getDisplayName(
                                            TextStyle.FULL,
                                            Locale("pt", "BR")
                                        ).replaceFirstChar { it.uppercase() }
                                    )
                                    append(" (${resumo.cargaHorariaDiariaFormatada})")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Lado direito: Horas trabalhadas + Pontos + Seta
                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (resumo.jornadaCompleta || resumo.horasTrabalhadasMinutos > 0) {
                            Text(
                                text = resumo.horasTrabalhadasFormatadas,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                        Text(
                            text = "${resumo.quantidadePontos} ponto${if (resumo.quantidadePontos != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpandido) "Colapsar" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ================================================================
            // CONTEÚDO EXPANDIDO
            // ================================================================
            AnimatedVisibility(
                visible = isExpandido,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // --------------------------------------------------------
                    // SEÇÃO: TURNOS
                    // --------------------------------------------------------
                    if (resumo.intervalos.isNotEmpty()) {
                        TurnosSection(intervalos = resumo.intervalos)
                    }

                    // --------------------------------------------------------
                    // SEÇÃO: INTERVALO (se houver)
                    // --------------------------------------------------------
                    if (resumo.temIntervalo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        IntervaloSection(resumo = resumo)
                    }

                    // --------------------------------------------------------
                    // SEÇÃO: SALDOS
                    // --------------------------------------------------------
                    if (resumo.jornadaCompleta) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SaldosSection(resumo = resumo)
                    }

                    // --------------------------------------------------------
                    // BOTÃO: VER DETALHES
                    // --------------------------------------------------------
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        onClick = onNavigateToDay,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Ver detalhes do dia",
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

/**
 * Seção que exibe os turnos de trabalho.
 */
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
                val horaEntrada = intervalo.entrada.dataHora.format(timeFormatter)
                val horaSaida = intervalo.saida?.dataHora?.format(timeFormatter) ?: "..."
                val duracao = intervalo.formatarDuracaoCompacta()

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Turno X: HH:mm - HH:mm
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Turno $turnoNum:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$horaEntrada - $horaSaida",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Indicador se entrada foi ajustada
                        if (intervalo.temHoraEntradaConsiderada) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Entrada ajustada",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Duração
                    Text(
                        text = "→ $duracao",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (intervalo.aberto)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }

                // Espaço entre turnos (exceto último)
                if (index < intervalos.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * Seção que exibe o intervalo real vs considerado.
 */
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
            // Título
            Text(
                text = "Intervalo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Intervalo Real
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⏱️", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Real",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = resumo.minutosIntervaloReal.minutosParaDuracaoCompacta(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (temTolerancia) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (temTolerancia)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            // Intervalo Considerado (se diferente)
            if (temTolerancia) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✅", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Considerado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = resumo.minutosIntervaloTotal.minutosParaDuracaoCompacta(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Badge de tolerância
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tolerância: ${diferencaMinutos}min desconsiderados",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Seção que exibe os saldos do dia.
 */
@Composable
private fun SaldosSection(resumo: ResumoDia) {
    val saldoColor = if (resumo.temSaldoPositivo || !resumo.temSaldoNegativo)
        Color(0xFF4CAF50) else Color(0xFFF44336)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Saldo do dia
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Saldo do dia",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = resumo.saldoDiaFormatado,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = saldoColor
                )
            }

            // TODO: Saldo do banco (acumulado) - implementar quando tiver o cálculo
            // Spacer(modifier = Modifier.height(2.dp))
            // Row(
            //     horizontalArrangement = Arrangement.SpaceBetween,
            //     modifier = Modifier.fillMaxWidth()
            // ) {
            //     Text(
            //         text = "Saldo do banco",
            //         style = MaterialTheme.typography.bodySmall,
            //         color = MaterialTheme.colorScheme.onSurfaceVariant
            //     )
            //     Text(
            //         text = "+00h 01min", // TODO: calcular acumulado
            //         style = MaterialTheme.typography.bodySmall,
            //         fontWeight = FontWeight.Bold,
            //         color = saldoColor
            //     )
            // }
        }
    }
}

/**
 * Retorna o ícone emoji do status.
 */
private fun getStatusIcon(status: StatusDiaResumo): String {
    return when (status) {
        StatusDiaResumo.COMPLETO -> "✅"
        StatusDiaResumo.EM_ANDAMENTO -> "🔄"
        StatusDiaResumo.INCOMPLETO -> "⚠️"
        StatusDiaResumo.COM_PROBLEMAS -> "❌"
        StatusDiaResumo.SEM_REGISTRO -> "⬜"
    }
}

/**
 * Retorna a cor principal do status.
 */
private fun getStatusColor(status: StatusDiaResumo): Color {
    return when (status) {
        StatusDiaResumo.COMPLETO -> Color(0xFF4CAF50)
        StatusDiaResumo.EM_ANDAMENTO -> Color(0xFF2196F3)
        StatusDiaResumo.INCOMPLETO -> Color(0xFFFF9800)
        StatusDiaResumo.COM_PROBLEMAS -> Color(0xFFF44336)
        StatusDiaResumo.SEM_REGISTRO -> Color(0xFF9E9E9E)
    }
}
