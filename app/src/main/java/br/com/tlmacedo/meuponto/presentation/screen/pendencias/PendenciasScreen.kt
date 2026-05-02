// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/pendencias/PendenciasScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.pendencias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.InconsistenciaDetectada
import br.com.tlmacedo.meuponto.domain.model.PendenciaDia
import br.com.tlmacedo.meuponto.domain.model.StatusPendencia
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendenciasScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDia: (LocalDate) -> Unit = {},
    viewModel: PendenciasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(uiState.mensagemErro) {
        uiState.mensagemErro?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(PendenciasEvent.LimparErro)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MeuPontoTopBar(
                title = "Pendências",
                subtitle = if (uiState.resultado != null) {
                    "${uiState.resultado!!.total} pendência(s) nos últimos 30 dias"
                } else null,
                showBackButton = true,
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PendenciasTabRow(
                uiState = uiState,
                onTabSelected = { viewModel.onEvent(PendenciasEvent.SelecionarTab(it)) }
            )

            when {
                uiState.isLoading -> PendenciasLoading()
                !uiState.temPendencias && uiState.resultado != null -> PendenciasVazia(uiState.tabSelecionada)
                else -> PendenciasLista(
                    dias = uiState.diasExibidos,
                    onDiaClick = onNavigateToDia
                )
            }
        }
    }
}

@Composable
private fun PendenciasTabRow(
    uiState: PendenciasUiState,
    onTabSelected: (TabPendencias) -> Unit
) {
    val tabs = TabPendencias.entries
    val selectedIndex = tabs.indexOf(uiState.tabSelecionada)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        tabs.forEachIndexed { index, tab ->
            val contador = uiState.contadorPorTab[tab] ?: 0
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabSelected(tab) },
                text = {
                    BadgedBox(
                        badge = {
                            if (contador > 0) {
                                Badge(
                                    containerColor = corDoTab(tab),
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = contador.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = if (contador > 0) 8.dp else 0.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun PendenciasLista(
    dias: List<PendenciaDia>,
    onDiaClick: (LocalDate) -> Unit
) {
    val expandidos = remember { mutableStateMapOf<LocalDate, Boolean>() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dias, key = { it.data }) { dia ->
            PendenciaCard(
                dia = dia,
                expandido = expandidos[dia.data] ?: false,
                onToggleExpand = { expandidos[dia.data] = !(expandidos[dia.data] ?: false) },
                onDiaClick = { onDiaClick(dia.data) }
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun PendenciaCard(
    dia: PendenciaDia,
    expandido: Boolean,
    onToggleExpand: () -> Unit,
    onDiaClick: () -> Unit
) {
    val cor = corDoStatus(dia.status)
    val icone = iconeDoStatus(dia.status)
    val animatedBorderColor by animateColorAsState(
        targetValue = cor,
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDiaClick)
                    .padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(animatedBorderColor)
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(cor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icone,
                        contentDescription = null,
                        tint = cor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dia.data.format(DATE_FORMATTER),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = diaSemanaFormatado(dia.data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = dia.status, cor = cor)
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (expandido) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expandido) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onToggleExpand)
                        .padding(4.dp)
                )
                Spacer(Modifier.width(8.dp))
            }

            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    InconsistenciasLista(
                        inconsistencias = dia.inconsistencias,
                        temJustificativa = dia.temJustificativa
                    )
                }
            }
        }
    }
}

@Composable
private fun InconsistenciasLista(
    inconsistencias: List<InconsistenciaDetectada>,
    temJustificativa: Boolean
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        inconsistencias.forEach { item ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val cor = corDaSeveridade(item.isBloqueante)
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(cor)
                        .align(Alignment.CenterVertically)
                )
                Column {
                    Text(
                        text = item.inconsistencia.descricao,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    item.detalhes?.let { detalhe ->
                        Text(
                            text = detalhe,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if (temJustificativa) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Justificativa registrada",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: StatusPendencia, cor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(cor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = cor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PendenciasLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Verificando pendências...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PendenciasVazia(tab: TabPendencias) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = when (tab) {
                    TabPendencias.BLOQUEANTES -> "Nenhum item bloqueante"
                    TabPendencias.PENDENTES -> "Nenhum item pendente"
                    TabPendencias.INFORMATIVOS -> "Nenhum informativo"
                    TabPendencias.TODOS -> "Tudo em ordem"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Nenhuma pendência encontrada nos últimos 30 dias.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun corDoStatus(status: StatusPendencia): Color {
    return when (status) {
        StatusPendencia.BLOQUEANTE -> MaterialTheme.colorScheme.error
        StatusPendencia.PENDENTE -> Color(0xFFE67E22)
        StatusPendencia.INFORMATIVO -> MaterialTheme.colorScheme.tertiary
    }
}

@Composable
private fun corDaSeveridade(isBloqueante: Boolean): Color {
    return if (isBloqueante) MaterialTheme.colorScheme.error
    else Color(0xFFE67E22)
}

@Composable
private fun corDoTab(tab: TabPendencias): Color {
    return when (tab) {
        TabPendencias.BLOQUEANTES -> MaterialTheme.colorScheme.error
        TabPendencias.PENDENTES -> Color(0xFFE67E22)
        TabPendencias.INFORMATIVOS -> MaterialTheme.colorScheme.tertiary
        TabPendencias.TODOS -> MaterialTheme.colorScheme.primary
    }
}

private fun iconeDoStatus(status: StatusPendencia): ImageVector {
    return when (status) {
        StatusPendencia.BLOQUEANTE -> Icons.Outlined.Error
        StatusPendencia.PENDENTE -> Icons.Outlined.Warning
        StatusPendencia.INFORMATIVO -> Icons.Outlined.Info
    }
}

private fun diaSemanaFormatado(data: LocalDate): String {
    val diaSemana = data.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
    return diaSemana.replaceFirstChar { it.uppercase() }
}
