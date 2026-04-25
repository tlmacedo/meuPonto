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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamadoListScreen(
    onNavigateToChamadoDetail: (Long) -> Unit,
    onNavigateToChamadoCreate: () -> Unit,
    viewModel: ChamadoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Chamados") }
                // Futuramente, aqui podemos adicionar ícones de filtro/pesquisa
            )
        },
        floatingActionButton = {
            // O FAB para o usuário final será para criar um NOVO chamado
            FloatingActionButton(onClick = onNavigateToChamadoCreate) {
                Icon(Icons.Filled.Add, "Criar novo chamado")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Erro desconhecido",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.chamados.isEmpty() && !uiState.isLoading && uiState.errorMessage == null -> {
                    Text(
                        text = "Você ainda não tem nenhum chamado. Crie um novo!",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.chamados, key = { it.id }) { chamado ->
                            ChamadoListItem(
                                chamado = chamado,
                                onChamadoClick = onNavigateToChamadoDetail
                            )
                        }
                    }
                }
            }
        }
    }
}