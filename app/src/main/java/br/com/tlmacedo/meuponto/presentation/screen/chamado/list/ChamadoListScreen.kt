// path: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/chamado/list/ChamadoListScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.chamado.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    viewModel: ChamadoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ChamadoListContent(
        uiState = uiState,
        onBackClick = onNavigateBack,
        onItemClick = onNavigateToDetail,
        onAddClick = onNavigateToCreate
    )
}

@Composable
fun ChamadoListContent(
    uiState: ChamadoListUiState,
    onBackClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = "Meus Chamados",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Chamado")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                ChamadoListUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Carregando chamados...", modifier = Modifier.padding(top = 8.dp))
                    }
                }
                ChamadoListUiState.Empty -> {
                    Text(
                        text = "Nenhum chamado encontrado.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is ChamadoListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.chamados) { chamado ->
                            ChamadoListItem(chamado = chamado, onItemClick = onItemClick)
                        }
                    }
                }
                is ChamadoListUiState.Error -> {
                    Text(
                        text = uiState.message,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
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
        ),
        Chamado(
            id = 2,
            identificador = "MP-2024-002",
            titulo = "Dúvida sobre jornada",
            descricao = "Como configurar jornada flexível?",
            categoria = CategoriaChamado.OUTRO,
            prioridade = PrioridadeChamado.BAIXA,
            status = StatusChamado.RESOLVIDO,
            usuarioEmail = "user@test.com",
            usuarioNome = "User Test",
            resposta = "Resposta de teste",
            criadoEm = LocalDateTime.now().minusDays(1),
            atualizadoEm = LocalDateTime.now(),
            empregoId = null
        )
    )
    MeuPontoTheme {
        ChamadoListContent(
            uiState = ChamadoListUiState.Success(chamados),
            onBackClick = {},
            onItemClick = {},
            onAddClick = {}
        )
    }
}
