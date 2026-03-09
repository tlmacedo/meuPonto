package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EditarVersaoScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHorarios: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditarVersaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is EditarVersaoEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is EditarVersaoEvent.SalvoComSucesso -> {
                    snackbarHostState.showSnackbar("Versão salva com sucesso!")
                    onNavigateBack()
                }
                is EditarVersaoEvent.Voltar -> {
                    onNavigateBack()
                }
                is EditarVersaoEvent.NavegarParaHorarios -> {
                    onNavigateToHorarios(evento.versaoId)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = if (uiState.isNovaVersao) "Nova Versão" else "Editar Versão",
                showBackButton = true,
                onBackClick = { viewModel.onAction(EditarVersaoAction.Cancelar) }
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
                EditarVersaoContent(
                    uiState = uiState,
                    onDescricaoChange = { viewModel.onAction(EditarVersaoAction.AlterarDescricao(it)) },
                    onSalvar = { viewModel.onAction(EditarVersaoAction.Salvar) },
                    onGerenciarHorarios = { viewModel.onAction(EditarVersaoAction.ConfigurarHorarios) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EditarVersaoContent(
    uiState: EditarVersaoUiState,
    onDescricaoChange: (String) -> Unit,
    onSalvar: () -> Unit,
    onGerenciarHorarios: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Informações básicas
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Informações Básicas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = onDescricaoChange,
                    label = { Text("Descrição da versão") },
                    placeholder = { Text("Ex: Jornada 2024") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Versão: ${uiState.numeroVersao}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.vigente) {
                    Text(
                        text = "✓ Versão vigente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Período de vigência
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Período de Vigência",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.dataInicioFormatada,
                        onValueChange = { },
                        label = { Text("Data início") },
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.dataFimFormatada ?: "Indefinido",
                        onValueChange = { },
                        label = { Text("Data fim") },
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Configurações de jornada
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Configurações de Jornada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Jornada máxima diária: ${uiState.jornadaMaximaFormatada}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Intervalo interjornada: ${uiState.intervaloInterjornadaFormatado}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tolerância intervalo: ${uiState.toleranciaIntervaloFormatada}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Botão para gerenciar horários (se não for nova versão)
        if (!uiState.isNovaVersao && uiState.versaoId != null) {
            OutlinedButton(
                onClick = onGerenciarHorarios,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gerenciar Horários (${uiState.horarios.size})")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão salvar
        Button(
            onClick = onSalvar,
            enabled = uiState.podeSalvar,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (uiState.isNovaVersao) "Criar Versão" else "Salvar Alterações")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
