package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.components.settings.StatusChip
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VersoesJornadaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditar: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VersoesJornadaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collectLatest { evento ->
            when (evento) {
                is VersoesJornadaEvent.MostrarMensagem -> {
                    snackbarHostState.showSnackbar(evento.mensagem)
                }
                is VersoesJornadaEvent.NavegarParaEditar -> {
                    onNavigateToEditar(uiState.empregoId, evento.versaoId)
                }
                is VersoesJornadaEvent.Voltar -> {
                    onNavigateBack()
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Versões de Jornada",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.onAction(VersoesJornadaAction.CriarNovaVersao) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nova Versão") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Erro desconhecido",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            !uiState.temVersoes -> {
                EmptyStateVersoesModern(
                    onCriarVersao = {
                        viewModel.onAction(VersoesJornadaAction.CriarNovaVersao)
                    },
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
                        VersoesResumoHeader(
                            nomeEmprego = uiState.nomeEmprego,
                            totalVersoes = uiState.totalVersoes,
                            vigente = uiState.versaoVigente
                        )
                    }

                    uiState.versaoVigente?.let { vigente ->
                        item {
                            Text(
                                text = "Versão vigente",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        item {
                            VersaoJornadaModernCard(
                                versao = vigente,
                                isVigente = true,
                                onEditar = {
                                    viewModel.onAction(VersoesJornadaAction.EditarVersao(vigente.id))
                                },
                                onDefinirVigente = {},
                                onExcluir = {}
                            )
                        }
                    }

                    val historicas = uiState.versoes.filterNot { it.vigente }
                    if (historicas.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Histórico",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(historicas, key = { it.id }) { versao ->
                            VersaoJornadaModernCard(
                                versao = versao,
                                isVigente = false,
                                onEditar = {
                                    viewModel.onAction(VersoesJornadaAction.EditarVersao(versao.id))
                                },
                                onDefinirVigente = {
                                    viewModel.onAction(VersoesJornadaAction.DefinirComoVigente(versao.id))
                                },
                                onExcluir = {
                                    viewModel.onAction(VersoesJornadaAction.AbrirDialogExcluir(versao))
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(88.dp)) }
                }
            }
        }
    }
}

@Composable
private fun VersoesResumoHeader(
    nomeEmprego: String,
    totalVersoes: Int,
    vigente: VersaoJornada?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = nomeEmprego,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$totalVersoes versão(ões) cadastradas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            vigente?.let {
                Spacer(modifier = Modifier.height(12.dp))
                StatusChip(text = "Vigente: ${it.titulo}")
            }
        }
    }
}

@Composable
private fun VersaoJornadaModernCard(
    versao: VersaoJornada,
    isVigente: Boolean,
    onEditar: () -> Unit,
    onDefinirVigente: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isVigente)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = versao.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = versao.periodoFormatado,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isVigente) {
                    StatusChip(text = "Vigente")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = versao.jornadaMaximaFormatada,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Jornada Máx.",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = versao.intervaloInterjornadaFormatado,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Interjornada",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${versao.numeroVersao}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Versão",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }

                if (!isVigente) {
                    Button(
                        onClick = onDefinirVigente,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Definir Vigente")
                    }
                }
            }

            if (!isVigente) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onExcluir) {
                    Text(
                        text = "Excluir versão",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateVersoesModern(
    onCriarVersao: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
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
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Nenhuma versão cadastrada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crie a primeira versão de jornada para começar a configurar horários e regras específicas deste emprego.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onCriarVersao) {
                    Text("Criar primeira versão")
                }
            }
        }
    }
}
