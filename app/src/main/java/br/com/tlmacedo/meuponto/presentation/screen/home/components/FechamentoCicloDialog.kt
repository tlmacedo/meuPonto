// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/components/FechamentoCicloDialog.kt
package br.com.tlmacedo.meuponto.presentation.screen.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.presentation.screen.home.EstadoCiclo

/**
 * Dialog de confirmação para fechamento de ciclo do banco de horas.
 *
 * @author Thiago
 * @since 6.2.0
 */
@Composable
fun FechamentoCicloDialog(
    estadoCiclo: EstadoCiclo.Pendente,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                text = "Fechar Ciclo do Banco",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "O ciclo do banco de horas encerrou e precisa ser fechado.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Período: ${estadoCiclo.ciclo.periodoDescricao}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Saldo a zerar:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = " ${estadoCiclo.ciclo.saldoAtualFormatado}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (estadoCiclo.ciclo.saldoAtualMinutos >= 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }

                Text(
                    text = buildString {
                        if (estadoCiclo.ciclo.saldoAtualMinutos > 0) {
                            append("✅ Você tem horas positivas! ")
                            append("Elas serão registradas no histórico.")
                        } else if (estadoCiclo.ciclo.saldoAtualMinutos < 0) {
                            append("⚠️ Você tem horas negativas. ")
                            append("O débito será registrado no histórico.")
                        } else {
                            append("✅ Seu saldo está zerado. ")
                            append("Nenhum ajuste necessário.")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Fechar Ciclo")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancelar) {
                Text("Depois")
            }
        }
    )
}
