// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/components/ImportarFeriadosDialog.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * Dialog para importar feriados da Brasil API.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Composable
fun ImportarFeriadosDialog(
    isLoading: Boolean,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    val anoAtual = LocalDate.now().year

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Text(
                text = if (isLoading) "Importando..." else "Importar Feriados",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    Text(
                        text = "Buscando feriados nacionais...",
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Deseja importar os feriados nacionais da Brasil API?",
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "📅",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = anoAtual.toString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "📅",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = (anoAtual + 1).toString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Serão importados os feriados de $anoAtual e ${anoAtual + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            if (!isLoading) {
                TextButton(onClick = onConfirmar) {
                    Text("Importar")
                }
            }
        },
        dismissButton = {
            if (!isLoading) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
