// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/foto/FotoSourceDialog.kt
package br.com.tlmacedo.meuponto.presentation.components.foto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Diálogo para seleção de fonte da foto (câmera ou galeria).
 *
 * IMPORTANTE: Este diálogo NÃO fecha automaticamente ao selecionar uma opção.
 * O fechamento é controlado pelo componente pai após receber o resultado
 * da câmera/galeria, evitando que o ComprovanteImagePicker seja removido
 * da composição antes de processar o callback.
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 10.2.0 - Removido onDismiss automático nas opções para evitar perda de callbacks
 */
@Composable
fun FotoSourceDialog(
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onCustomCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Adicionar Comprovante",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Escolha como deseja adicionar a foto do comprovante",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SourceOption(
//                        icon = Icons.Default.DocumentScanner,
                        icon = Icons.Default.CameraAlt,
                        label = "Câmera",
                        onClick = onCustomCameraSelected
                    )

//                    SourceOption(
//                        icon = Icons.Default.CameraAlt,
//                        label = "Câmera",
//                        onClick = onCameraSelected
//                    )

                    SourceOption(
                        icon = Icons.Default.Photo,
                        label = "Galeria",
                        onClick = onGallerySelected
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun SourceOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
