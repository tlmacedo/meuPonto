// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/components/EmpregoSelectorDialog.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.Emprego

/**
 * Dialog para seleção de emprego.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Composable
fun EmpregoSelectorDialog(
    empregos: List<Emprego>,
    empregoSelecionadoId: Long?,
    onEmpregoSelected: (Emprego) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Emprego") },
        text = {
            if (empregos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💼",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum emprego cadastrado",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    itemsIndexed(
                        items = empregos,
                        key = { _, emprego -> emprego.id }
                    ) { index, emprego ->
                        val isSelected = emprego.id == empregoSelecionadoId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEmpregoSelected(emprego) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = emprego.nome,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selecionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (index < empregos.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
