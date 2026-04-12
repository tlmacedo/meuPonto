// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoScreen.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.tlmacedo.meuponto.R

/**
 * Tela de edição de ponto existente.
 *
 * Permite ao usuário alterar data, hora, emprego, observação, NSR
 * e foto de comprovante de um ponto já registrado.
 *
 * ## Sobre tipoPonto:
 * Esta tela não exibe nem edita o tipo do ponto. O tipo é calculado
 * dinamicamente pelo domínio (ímpar=entrada, par=saída) e não é
 * uma informação editável pelo usuário.
 *
 * ## Correções aplicadas (12.0.0):
 * - uiEvent (inexistente) substituído por uiState (confirmado no ViewModel)
 * - DatePickerField, TimePickerField, DropdownField, ObservacaoField agora
 *   estão definidos em EditPontoComponents.kt no mesmo pacote — sem import
 *   necessário pois estão no mesmo package declaration
 * - Todas as referências a onAction resolvidas via viewModel.onAction(Action)
 * - Removidas chamadas suspend diretas no corpo da composable
 *
 * @param onNavigateBack Callback para navegação de volta
 * @param viewModel ViewModel injetado via Hilt
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 12.0.0 - Todos os erros de compilação resolvidos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPontoScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditPontoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarConfirmacaoExclusao by remember { mutableStateOf(false) }

    // Navega de volta quando o ponto for salvo ou excluído com sucesso
    LaunchedEffect(uiState.isSalvo) {
        if (uiState.isSalvo) {
            onNavigateBack()
        }
    }

    // Exibe erros via Snackbar e limpa após exibição
    LaunchedEffect(uiState.erro) {
        uiState.erro?.let { mensagem ->
            snackbarHostState.showSnackbar(mensagem)
            viewModel.onAction(EditPontoAction.LimparErro)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editar Ponto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_voltar)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { mostrarConfirmacaoExclusao = true },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.btn_excluir),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ================================================================
            // DATA
            // ================================================================
            // DatePickerField está em EditPontoComponents.kt — mesmo pacote
            DatePickerField(
                label = "Data",
                selectedDate = uiState.data,
                onDateSelected = { data ->
                    viewModel.onAction(EditPontoAction.AlterarData(data))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            // ================================================================
            // HORA
            // ================================================================
            // TimePickerField está em EditPontoComponents.kt — mesmo pacote
            TimePickerField(
                label = "Hora",
                selectedTime = uiState.hora,
                onTimeSelected = { hora ->
                    viewModel.onAction(EditPontoAction.AlterarHora(hora))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            // ================================================================
            // EMPREGO
            // ================================================================
            if (uiState.empregos.isNotEmpty()) {
                // DropdownField está em EditPontoComponents.kt — mesmo pacote
                DropdownField(
                    label = "Emprego",
                    options = uiState.empregos.map { it.nome },
                    selectedOption = uiState.empregoSelecionado?.nome ?: "",
                    onOptionSelected = { nome ->
                        val emprego = uiState.empregos.find { it.nome == nome }
                        emprego?.let {
                            viewModel.onAction(EditPontoAction.AlterarEmprego(it))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )
            }

            // ================================================================
            // OBSERVAÇÃO
            // ================================================================
            // ObservacaoField está em EditPontoComponents.kt — mesmo pacote
            ObservacaoField(
                value = uiState.observacao,
                onValueChange = { obs ->
                    viewModel.onAction(EditPontoAction.AlterarObservacao(obs))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            // ================================================================
            // FOTO DE COMPROVANTE
            // ================================================================
            if (uiState.temFoto) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.foto_titulo),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(
                        onClick = {
                            viewModel.onAction(EditPontoAction.RemoverFoto)
                        },
                        enabled = !uiState.isLoading
                    ) {
                        Text(
                            text = stringResource(R.string.foto_excluir),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ================================================================
            // SALVAR
            // ================================================================
            Button(
                onClick = { viewModel.onAction(EditPontoAction.Salvar) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.temAlteracao,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(stringResource(R.string.btn_salvar))
            }

            // ================================================================
            // CANCELAR
            // ================================================================
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(stringResource(R.string.btn_cancelar))
            }
        }
    }

    // ========================================================================
    // DIALOG DE CONFIRMAÇÃO DE EXCLUSÃO
    // ========================================================================
    if (mostrarConfirmacaoExclusao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoExclusao = false },
            title = {
                Text(stringResource(R.string.editar_ponto_confirmar_exclusao))
            },
            text = {
                Text(stringResource(R.string.editar_ponto_confirmar_exclusao_descricao))
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacaoExclusao = false
                        viewModel.onAction(EditPontoAction.Excluir)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_excluir))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoExclusao = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }
}