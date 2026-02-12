// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.presentation.components.LoadingIndicator
import br.com.tlmacedo.meuponto.presentation.components.MeuPontoTopBar
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tela de edição/criação de ponto.
 *
 * Permite ao usuário editar um ponto existente ou criar um novo,
 * com campos para data, hora, tipo e observação.
 *
 * @param onNavigateBack Callback para voltar à tela anterior
 * @param modifier Modificador opcional para customização do layout
 * @param viewModel ViewModel da tela (injetado pelo Hilt)
 *
 * @author Thiago
 * @since 1.0.0
 */
@Composable
fun EditPontoScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPontoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observa eventos únicos
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is EditPontoUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EditPontoUiEvent.PontoSalvo -> {
                    // Tratado junto com NavigateBack
                }
                is EditPontoUiEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MeuPontoTopBar(
                title = uiState.screenTitle,
                showBackButton = true,
                onBackClick = { viewModel.onAction(EditPontoAction.Cancelar) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            EditPontoContent(
                uiState = uiState,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Conteúdo do formulário de edição de ponto.
 */
@Composable
private fun EditPontoContent(
    uiState: EditPontoUiState,
    onAction: (EditPontoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
    val formatadorHora = DateTimeFormatter.ofPattern("HH:mm", Locale("pt", "BR"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Card de Data e Hora
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Data e Hora",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Data
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Data:")
                    Text(
                        text = uiState.data.format(formatadorData),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hora
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Hora:")
                    Text(
                        text = uiState.hora.format(formatadorHora),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card de Tipo
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .selectableGroup()
            ) {
                Text(
                    text = "Tipo de Ponto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                TipoPonto.entries.forEach { tipo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = uiState.tipo == tipo,
                                onClick = { onAction(EditPontoAction.AlterarTipo(tipo)) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = uiState.tipo == tipo,
                            onClick = null
                        )
                        Text(
                            text = tipo.descricao,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Observação
        OutlinedTextField(
            value = uiState.observacao,
            onValueChange = { onAction(EditPontoAction.AlterarObservacao(it)) },
            label = { Text("Observação (opcional)") },
            placeholder = { Text("Digite uma observação...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botões de ação
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { onAction(EditPontoAction.Cancelar) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = { onAction(EditPontoAction.Salvar) },
                enabled = uiState.canSave,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (uiState.isSaving) "Salvando..." else "Salvar")
            }
        }

        // Mensagem de erro
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
