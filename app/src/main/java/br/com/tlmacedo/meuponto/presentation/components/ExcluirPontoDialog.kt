// app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/ExcluirPontoDialog.kt

package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.format.DateTimeFormatter

/**
 * Dialog de confirmação para exclusão de registro de ponto.
 *
 * Exige um motivo obrigatório para auditoria.
 *
 * @param ponto Ponto a ser excluído
 * @param tipoDescricao Descrição do tipo (Entrada/Saída) - calculada dinamicamente pelo índice
 * @param onDismiss Callback ao fechar/cancelar
 * @param onConfirmar Callback ao confirmar exclusão (recebe o motivo)
 * @param isLoading Se está processando a exclusão
 *
 * @author Thiago
 * @since 7.2.0
 */
@Composable
fun ExcluirPontoDialog(
    ponto: Ponto,
    tipoDescricao: String,
    onDismiss: () -> Unit,
    onConfirmar: (motivo: String) -> Unit,
    isLoading: Boolean = false
) {
    var motivo by remember { mutableStateOf("") }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    val motivoValido = motivo.trim().length >= 5
    val podeConfirmar = motivoValido && !isLoading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Excluir Registro",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Informações do ponto
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tipoDescricao,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ponto.hora.format(timeFormatter),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = ponto.dataHora.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Esta ação não pode ser desfeita. O registro será removido permanentemente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Campo de motivo obrigatório
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo da exclusão *") },
                    placeholder = { Text("Descreva o motivo (mín. 5 caracteres)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    isError = motivo.isNotEmpty() && !motivoValido,
                    supportingText = {
                        Text("${motivo.length}/5 caracteres mínimos")
                    },
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(motivo.trim()) },
                enabled = podeConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}
