package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes.comparar

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar

@Composable
fun CompararVersoesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompararVersoesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompararVersoesContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun CompararVersoesContent(
    uiState: CompararVersoesUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Comparar Versões",
                subtitle = uiState.empregoApelido?.uppercase(),
                logo = uiState.empregoLogo,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            uiState.versao1 != null && uiState.versao2 != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ComparacaoHeader(v1 = uiState.versao1, v2 = uiState.versao2)
                    }

                    item {
                        ComparacaoSecaoItem(
                            titulo = "Dados Básicos",
                            itens = listOf(
                                ComparacaoLinhaData(
                                    label = "Título",
                                    v1 = uiState.versao1.titulo,
                                    v2 = uiState.versao2.titulo
                                ),
                                ComparacaoLinhaData(
                                    label = "Período",
                                    v1 = uiState.versao1.periodoFormatado,
                                    v2 = uiState.versao2.periodoFormatado
                                ),
                                ComparacaoLinhaData(
                                    label = "Vigente",
                                    v1 = if (uiState.versao1.vigente) "Sim" else "Não",
                                    v2 = if (uiState.versao2.vigente) "Sim" else "Não"
                                )
                            )
                        )
                    }

                    item {
                        ComparacaoSecaoItem(
                            titulo = "Regras de Jornada",
                            itens = listOf(
                                ComparacaoLinhaData(
                                    label = "Jornada Máxima",
                                    v1 = uiState.versao1.jornadaMaximaFormatada,
                                    v2 = uiState.versao2.jornadaMaximaFormatada
                                ),
                                ComparacaoLinhaData(
                                    label = "Interjornada",
                                    v1 = uiState.versao1.intervaloInterjornadaFormatado,
                                    v2 = uiState.versao2.intervaloInterjornadaFormatado
                                ),
                                ComparacaoLinhaData(
                                    label = "Carga Horária",
                                    v1 = uiState.versao1.cargaHorariaDiariaFormatada,
                                    v2 = uiState.versao2.cargaHorariaDiariaFormatada
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparacaoHeader(v1: VersaoJornada, v2: VersaoJornada) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VersaoHeaderResumo(versao = v1, modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 8.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            VersaoHeaderResumo(versao = v2, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun VersaoHeaderResumo(versao: VersaoJornada, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Versão ${versao.numeroVersao}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = versao.titulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComparacaoSecaoItem(
    titulo: String,
    itens: List<ComparacaoLinhaData>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                itens.forEachIndexed { index, item ->
                    ComparacaoLinha(item)
                    if (index < itens.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

data class ComparacaoLinhaData(
    val label: String,
    val v1: String,
    val v2: String
)

@Composable
private fun ComparacaoLinha(item: ComparacaoLinhaData) {
    val alterado = item.v1 != item.v2
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.v1,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = if (alterado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 8.dp).size(16.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = item.v2,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (alterado) FontWeight.Bold else FontWeight.Normal,
                color = if (alterado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
