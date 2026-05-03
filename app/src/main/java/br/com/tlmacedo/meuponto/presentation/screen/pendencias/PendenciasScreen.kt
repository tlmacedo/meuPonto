// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/pendencias/PendenciasScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.pendencias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import br.com.tlmacedo.meuponto.domain.model.StatusDiaPonto
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.theme.LocalAppThemeController
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))

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

    LaunchedEffect(uiState.mensagemSucesso) {
        uiState.mensagemSucesso?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(PendenciasEvent.LimparSucesso)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MeuPontoTopBar(
                title = "Pendências",
                subtitle = if (uiState.resultado != null) {
                    "${uiState.resultado!!.total} pendência(s) no período"
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
            uiState.saude?.let { saude ->
                SaudeCard(
                    saude = saude,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            PeriodoSelector(
                mesReferencia = uiState.mesReferencia,
                onMesAlterado = { viewModel.onEvent(PendenciasEvent.AlterarMes(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            PendenciasTabRow(
                uiState = uiState,
                onTabSelected = { viewModel.onEvent(PendenciasEvent.SelecionarTab(it)) }
            )

            when {
                uiState.isLoading -> PendenciasLoading()
                !uiState.temPendencias && uiState.resultado != null -> PendenciasVazia(uiState.tabSelecionada)
                else -> PendenciasLista(
                    dias = uiState.diasExibidos,
                    onDiaClick = onNavigateToDia,
                    onJustificar = { viewModel.onEvent(PendenciasEvent.AbrirDialogoJustificativa(it)) }
                )
            }
        }
    }

    uiState.dialogoJustificativa?.let { dialogo ->
        JustificativaBottomSheet(
            dialogo = dialogo,
            isSalvando = uiState.isSalvandoJustificativa,
            onDismiss = { viewModel.onEvent(PendenciasEvent.FecharDialogoJustificativa) },
            onTextoAlterado = { viewModel.onEvent(PendenciasEvent.AlterarTextoJustificativa(it)) },
            onSugestaoSelecionada = { viewModel.onEvent(PendenciasEvent.SelecionarSugestao(it)) },
            onConfirmar = {
                viewModel.onEvent(
                    PendenciasEvent.ConfirmarJustificativa(
                        data = dialogo.pendencia.data,
                        justificativa = dialogo.textoAtual
                    )
                )
            }
        )
    }
}

@Composable
private fun PeriodoSelector(
    mesReferencia: YearMonth,
    onMesAlterado: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMesAlterado(-1L) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Mês anterior",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = mesReferencia.format(MONTH_YEAR_FORMATTER).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(onClick = { onMesAlterado(1L) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Próximo mês",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SaudeCard(
    saude: br.com.tlmacedo.meuponto.domain.usecase.pendencias.CalcularSaudeDoEmpregoUseCase.SaudeEmprego,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppThemeController.current
    val corSaude = when {
        saude.percentualSaude >= 90 -> MaterialTheme.colorScheme.tertiary
        saude.percentualSaude >= 70 -> Color(0xFFE67E22)
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (theme.isPremium) 20.dp else 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp)
            ) {
                CircularProgressIndicator(
                    progress = { saude.percentualSaude / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = corSaude,
                    strokeWidth = 6.dp,
                    trackColor = corSaude.copy(alpha = 0.1f),
                )
                Text(
                    text = "${saude.percentualSaude}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = corSaude
                )
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text(
                    text = "Saúde do seu Ponto",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                val mensagemSaude = when {
                    saude.percentualSaude >= 90 -> "Excelente! Tudo em dia."
                    saude.percentualSaude >= 70 -> "Atenção: Algumas pendências."
                    else -> if (saude.totalPendenciasMes > 0) "Crítico: Regularize seus registros." else "Atenção: Sincronize seu backup na nuvem."
                }
                Text(
                    text = mensagemSaude,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                when (val backup = saude.backupStatus) {
                    is br.com.tlmacedo.meuponto.domain.usecase.backup.VerificarBackupSaudavelUseCase.StatusBackup.Atrasado -> {
                        Text(
                            text = "⚠️ Backup atrasado (${backup.diasDesdeUltimo} dias)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    is br.com.tlmacedo.meuponto.domain.usecase.backup.VerificarBackupSaudavelUseCase.StatusBackup.NuncaRealizado -> {
                        Text(
                            text = "❌ Backup nunca realizado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JustificativaBottomSheet(
    dialogo: DialogoJustificativaState,
    isSalvando: Boolean,
    onDismiss: () -> Unit,
    onTextoAlterado: (String) -> Unit,
    onSugestaoSelecionada: (String) -> Unit,
    onConfirmar: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Justificar pendências",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Dia ${dialogo.pendencia.data.format(DATE_FORMATTER)} · ${dialogo.pendencia.inconsistencias.size} inconsistência(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Text(
                text = "Sugestões",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SugestoesChips(
                sugestoes = dialogo.sugestoes,
                textoAtual = dialogo.textoAtual,
                onSugestaoSelecionada = onSugestaoSelecionada
            )

            OutlinedTextField(
                value = dialogo.textoAtual,
                onValueChange = onTextoAlterado,
                label = { Text("Justificativa") },
                placeholder = { Text("Descreva o motivo da pendência...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss, enabled = !isSalvando) {
                    Text("Cancelar")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onConfirmar,
                    enabled = dialogo.textoAtual.isNotBlank() && !isSalvando
                ) {
                    if (isSalvando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SugestoesChips(
    sugestoes: List<String>,
    textoAtual: String,
    onSugestaoSelecionada: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        sugestoes.forEach { sugestao ->
            FilterChip(
                selected = textoAtual == sugestao,
                onClick = { onSugestaoSelecionada(sugestao) },
                label = {
                    Text(
                        text = sugestao,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
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
    onDiaClick: (LocalDate) -> Unit,
    onJustificar: (PendenciaDia) -> Unit
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
                onDiaClick = { onDiaClick(dia.data) },
                onJustificar = { onJustificar(dia) }
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
    onDiaClick: () -> Unit,
    onJustificar: () -> Unit
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
                        temJustificativa = dia.temJustificativa,
                        onJustificar = onJustificar
                    )
                }
            }
        }
    }
}

@Composable
private fun InconsistenciasLista(
    inconsistencias: List<InconsistenciaDetectada>,
    temJustificativa: Boolean,
    onJustificar: () -> Unit
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
                val cor = corDaSeveridade(item)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (temJustificativa) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
            } else {
                Spacer(Modifier.width(1.dp))
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onJustificar)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (temJustificativa) "Editar" else "Justificar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: StatusDiaPonto, cor: Color) {
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
                    TabPendencias.BLOQUEADOS -> "Nenhum item bloqueado"
                    TabPendencias.PENDENTES -> "Nenhum item pendente"
                    TabPendencias.EM_ANDAMENTO -> "Nenhuma jornada em aberto"
                    TabPendencias.INFORMATIVOS -> "Nenhum informativo"
                    TabPendencias.TODOS -> "Tudo em ordem"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Nenhuma pendência encontrada no período.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun corDaSeveridade(inconsistencia: InconsistenciaDetectada): Color {
    return when {
        inconsistencia.isBloqueante -> MaterialTheme.colorScheme.error
        inconsistencia.isPendente -> Color(0xFFE67E22)
        else -> MaterialTheme.colorScheme.tertiary
    }
}

@Composable
private fun corDoTab(tab: TabPendencias): Color {
    return when (tab) {
        TabPendencias.BLOQUEADOS -> MaterialTheme.colorScheme.error
        TabPendencias.PENDENTES -> Color(0xFFE67E22)
        TabPendencias.EM_ANDAMENTO -> MaterialTheme.colorScheme.primary
        TabPendencias.INFORMATIVOS -> MaterialTheme.colorScheme.tertiary
        TabPendencias.TODOS -> MaterialTheme.colorScheme.outline
    }
}

@Composable
private fun corDoStatus(status: StatusDiaPonto): Color {
    return when (status) {
        StatusDiaPonto.BLOQUEADO -> MaterialTheme.colorScheme.error
        StatusDiaPonto.PENDENTE_JUSTIFICATIVA -> Color(0xFFE67E22)
        StatusDiaPonto.EM_ANDAMENTO -> MaterialTheme.colorScheme.primary
        StatusDiaPonto.INFO -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
}

private fun iconeDoStatus(status: StatusDiaPonto): ImageVector {
    return when (status) {
        StatusDiaPonto.BLOQUEADO -> Icons.Outlined.Error
        StatusDiaPonto.PENDENTE_JUSTIFICATIVA -> Icons.Outlined.Warning
        StatusDiaPonto.EM_ANDAMENTO -> Icons.Outlined.Schedule
        StatusDiaPonto.INFO -> Icons.Outlined.Info
        else -> Icons.Outlined.CheckCircle
    }
}

private fun diaSemanaFormatado(data: LocalDate): String {
    val diaSemana = data.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
    return diaSemana.replaceFirstChar { it.uppercase() }
}
