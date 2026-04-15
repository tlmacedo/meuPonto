// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/horarios/HorariosScreen.kt

package br.com.tlmacedo.meuponto.presentation.screen.settings.horarios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HorariosScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HorariosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is HorariosEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is HorariosEvent.Voltar -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Horários",
                subtitle = "${uiState.empregoApelido?.uppercase()} - ${uiState.versaoDescricao}",
                logo = uiState.empregoLogo,
                showBackButton = true,
                onBackClick = onNavigateBack
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

            uiState.errorMessage != null -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Erro desconhecido",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                HorariosContent(
                    uiState = uiState,
                    onDiaClick = { viewModel.onAction(HorariosAction.SelecionarDia(it)) },
                    onToggleAtivo = { viewModel.onAction(HorariosAction.ToggleAtivo(it.diaSemana)) },
                    onCopiarClick = { viewModel.onAction(HorariosAction.AbrirDialogCopiar(it.diaSemana)) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        // Dialog de edição
        if (uiState.mostrarDialogEdicao && uiState.horarioEmEdicao != null) {
            EditarHorarioDialog(
                horario = uiState.horarioEmEdicao!!,
                isSaving = uiState.isSaving,
                mostrarTimePicker = uiState.mostrarTimePicker,
                campoTimePicker = uiState.campoTimePicker,
                onAlterarCargaHoraria = { viewModel.onAction(HorariosAction.AlterarCargaHoraria(it)) },
                onAlterarIntervaloMinimo = { viewModel.onAction(HorariosAction.AlterarIntervaloMinimo(it)) },
                onAbrirTimePicker = { viewModel.onAction(HorariosAction.AbrirTimePicker(it)) },
                onSelecionarHorario = { viewModel.onAction(HorariosAction.SelecionarHorario(it)) },
                onFecharTimePicker = { viewModel.onAction(HorariosAction.FecharTimePicker) },
                onLimparHorariosIdeais = { viewModel.onAction(HorariosAction.LimparHorariosIdeais) },
                onSalvar = { viewModel.onAction(HorariosAction.SalvarHorario) },
                onDismiss = { viewModel.onAction(HorariosAction.FecharDialogEdicao) },
                avisoJornadaExcedida = uiState.avisoJornadaExcedida,
                avisoTurnoMaximo = uiState.avisoTurnoMaximo,
                avisoIntervaloMinimo = uiState.avisoIntervaloMinimo,
                canSave = uiState.canSaveHorario
            )
        }

        // Dialog de copiar
        if (uiState.mostrarDialogCopiar && uiState.diaSelecionadoParaCopiar != null) {
            CopiarHorarioDialog(
                diaOrigem = uiState.diaSelecionadoParaCopiar!!,
                diasDisponiveis = uiState.horarios.map { it.diaSemana },
                isSaving = uiState.isSaving,
                onConfirmar = { viewModel.onAction(HorariosAction.CopiarParaDias(it)) },
                onDismiss = { viewModel.onAction(HorariosAction.FecharDialogCopiar) }
            )
        }
    }
}

@Composable
private fun HorariosContent(
    uiState: HorariosUiState,
    onDiaClick: (HorarioDiaSemana) -> Unit,
    onToggleAtivo: (HorarioDiaSemana) -> Unit,
    onCopiarClick: (HorarioDiaSemana) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Resumo semanal
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ResumoSemanalCard(
                totalCargaHoraria = uiState.totalCargaHorariaSemanalFormatada,
                diasAtivos = uiState.diasAtivos
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Lista de dias
        items(
            items = uiState.horarios,
            key = { it.id }
        ) { horario ->
            HorarioDiaCard(
                horario = horario,
                onClick = { onDiaClick(horario) },
                onToggleAtivo = { onToggleAtivo(horario) },
                onCopiarClick = { onCopiarClick(horario) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ResumoSemanalCard(
    totalCargaHoraria: String,
    diasAtivos: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Carga Horária Semanal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = totalCargaHoraria,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Dias de Trabalho",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$diasAtivos dias",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun HorarioDiaCard(
    horario: HorarioDiaSemana,
    onClick: () -> Unit,
    onToggleAtivo: () -> Unit,
    onCopiarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = horario.ativo) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (horario.ativo) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabeçalho: Nome do dia + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = horario.diaSemana.descricao,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (horario.ativo) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (horario.ativo) {
                        IconButton(
                            onClick = onCopiarClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar para outros dias",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Switch(
                        checked = horario.ativo,
                        onCheckedChange = { onToggleAtivo() }
                    )
                }
            }

            // Conteúdo quando ativo
            if (horario.ativo) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Carga horária
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Carga: ${horario.cargaHorariaFormatada}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Intervalo mín: ${horario.intervaloMinimoFormatado}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Horários ideais (se configurados)
                if (horario.temHorariosIdeais) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = horario.resumoHorariosIdeais,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Folga",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
