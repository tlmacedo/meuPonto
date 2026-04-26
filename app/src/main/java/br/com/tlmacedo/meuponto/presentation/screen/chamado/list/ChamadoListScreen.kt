// path: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/chamado/list/ChamadoListScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.chamado.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import java.time.LocalDateTime

@Composable
fun ChamadoListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: ChamadoListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    ChamadoListContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        onBackClick = onNavigateBack,
        onItemClick = onNavigateToDetail,
        onAddClick = onNavigateToCreate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamadoListContent(
    uiState: ChamadoListUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Suporte",
                subtitle = "MEUS CHAMADOS",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Chamado")
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                ChamadoListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                ChamadoListUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum chamado aberto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toque no botão + para iniciar uma solicitação de suporte.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ChamadoListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(uiState.chamados) { chamado ->
                            ChamadoListItem(chamado = chamado, onItemClick = onItemClick)
                        }
                    }
                }

                is ChamadoListUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ops! Algo deu errado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.message,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChamadoListContentPreview() {
    val chamados = listOf(
        Chamado(
            id = 1,
            identificador = "MP-2024-001",
            titulo = "Erro no backup",
            descricao = "O backup em nuvem não está funcionando.",
            categoria = CategoriaChamado.SUPORTE_TECNICO,
            prioridade = PrioridadeChamado.ALTA,
            status = StatusChamado.ABERTO,
            usuarioEmail = "user@test.com",
            usuarioNome = "User Test",
            resposta = null,
            criadoEm = LocalDateTime.now(),
            atualizadoEm = LocalDateTime.now(),
            empregoId = null
        )
    )
    MeuPontoTheme {
        ChamadoListContent(
            uiState = ChamadoListUiState.Success(chamados),
            isRefreshing = false,
            onRefresh = {},
            onBackClick = {},
            onItemClick = {},
            onAddClick = {}
        )
    }
}
