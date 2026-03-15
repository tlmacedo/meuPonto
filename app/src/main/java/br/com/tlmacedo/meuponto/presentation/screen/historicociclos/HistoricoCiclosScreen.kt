// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/historicociclos/HistoricoCiclosScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.historicociclos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras
import br.com.tlmacedo.meuponto.presentation.components.EmptyState
import br.com.tlmacedo.meuponto.presentation.components.LoadingIndicator
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.util.minutosParaSaldoFormatado
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Tela de Histórico de Ciclos do Banco de Horas.
 *
 * Exibe todos os ciclos do banco de horas, incluindo o ciclo atual
 * e os ciclos históricos (já fechados).
 *
 * @param onNavigateBack Callback para voltar à tela anterior
 * @param viewModel ViewModel da tela
 *
 * @author Thiago
 * @since 9.0.0
 */
@Composable
fun HistoricoCiclosScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoricoCiclosViewModel = hiltViewModel()
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
                title = "Histórico de Ciclos",
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
            // Header com resumo geral
            if (uiState.hasCiclos && !uiState.isLoading) {
                ResumoGeralCiclos(
                    totalCiclos = uiState.ciclos.size,
                    ciclosFechados = uiState.totalCiclosFechados,
                    empregoNome = uiState.empregoNome
                )
            }

            when {
                uiState.isLoading -> LoadingIndicator()

                !uiState.hasCiclos -> {
                    EmptyState(
                        title = "Nenhum ciclo encontrado",
                        message = "Ainda não há ciclos de banco de horas registrados",
                        icon = Icons.Outlined.History
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.ciclos,
                            key = { index, ciclo -> "${ciclo.dataInicio}_$index" }
                        ) { index, ciclo ->
                            CicloCard(
                                ciclo = ciclo,
                                isExpandido = uiState.cicloExpandido == index,
                                onToggleExpansao = { viewModel.toggleCicloExpandido(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumoGeralCiclos(
    totalCiclos: Int,
    ciclosFechados: Int,
    empregoNome: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏦", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Banco de Horas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = empregoNome,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalCiclos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total de ciclos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$ciclosFechados",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Ciclos fechados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (totalCiclos > ciclosFechados) "1" else "0",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        text = "Em andamento",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CicloCard(
    ciclo: CicloBancoHoras,
    isExpandido: Boolean,
    onToggleExpansao: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val hoje = LocalDate.now()

    val corStatus = when {
        ciclo.isCicloAtual -> Color(0xFF2196F3) // Azul - em andamento
        ciclo.saldoAtualMinutos >= 0 -> Color(0xFF4CAF50) // Verde - positivo
        else -> Color(0xFFF44336) // Vermelho - negativo
    }

    val emoji = when {
        ciclo.isCicloAtual -> "🔄"
        ciclo.saldoAtualMinutos >= 0 -> "✅"
        else -> "⚠️"
    }

    val diasRestantes = if (ciclo.isCicloAtual) {
        ChronoUnit.DAYS.between(hoje, ciclo.dataFim).toInt()
    } else null

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (ciclo.isCicloAtual)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (ciclo.isCicloAtual) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpansao() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header do card
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(emoji, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (ciclo.isCicloAtual) "Ciclo Atual" else "Ciclo Fechado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = corStatus
                            )
                            if (ciclo.isCicloAtual) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF2196F3).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "EM ANDAMENTO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2196F3),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = ciclo.periodoDescricao,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${ciclo.duracaoDias} dias",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (diasRestantes != null && diasRestantes >= 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = when (diasRestantes) {
                                    0 -> "⏰ Último dia do ciclo!"
                                    1 -> "⏰ Falta 1 dia para fechar"
                                    else -> "⏰ Faltam $diasRestantes dias para fechar"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (diasRestantes <= 3) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (diasRestantes <= 3) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (ciclo.saldoAtualMinutos >= 0)
                                Icons.AutoMirrored.Filled.TrendingUp
                            else
                                Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = corStatus,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = ciclo.saldoAtualFormatado,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = corStatus
                        )
                    }
                    Text(
                        text = if (ciclo.isCicloAtual) "saldo atual" else "saldo final",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Icon(
                        imageVector = if (isExpandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpandido) "Colapsar" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Conteúdo expandido
            AnimatedVisibility(
                visible = isExpandido,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))

                    // Detalhes do ciclo
                    DetalhesCiclo(ciclo = ciclo, dateFormatter = dateFormatter)

                    // Informações do fechamento (se existir)
                    ciclo.fechamento?.let { fechamento ->
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))

                        InfoFechamento(
                            dataFechamento = fechamento.dataFechamento,
                            observacao = fechamento.observacao,
                            dateFormatter = dateFormatter
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalhesCiclo(
    ciclo: CicloBancoHoras,
    dateFormatter: DateTimeFormatter
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "📊 Detalhes do Ciclo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Início",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ciclo.dataInicio.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${ciclo.duracaoDias} dias",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Fim",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ciclo.dataFim.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏦", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (ciclo.isCicloAtual) "Saldo atual do ciclo" else "Saldo final do ciclo",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = ciclo.saldoAtualFormatado,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (ciclo.saldoAtualMinutos >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun InfoFechamento(
    dataFechamento: LocalDate,
    observacao: String?,
    dateFormatter: DateTimeFormatter
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Ciclo Fechado",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Data do fechamento",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dataFechamento.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            observacao?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "📝 $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Extensão para formatar minutos em saldo
private fun Int.minutosParaSaldoFormatado(): String = this.toLong().minutosParaSaldoFormatado()
