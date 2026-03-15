// app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/LocalizacaoModal.kt

package br.com.tlmacedo.meuponto.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.format.DateTimeFormatter

/**
 * Modal para visualização da localização de um registro de ponto.
 *
 * Exibe as coordenadas e permite abrir no Google Maps.
 *
 * @param ponto Ponto com localização a ser exibida
 * @param tipoDescricao Descrição do tipo (Entrada/Saída) - calculada dinamicamente pelo índice
 * @param onDismiss Callback ao fechar o modal
 *
 * @author Thiago
 * @since 7.2.0
 */
@Composable
fun LocalizacaoModal(
    ponto: Ponto,
    tipoDescricao: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    val latitude = ponto.latitude
    val longitude = ponto.longitude

    val temLocalizacao = latitude != null && longitude != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // ══════════════════════════════════════════════════════════
                // HEADER
                // ══════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Localização - $tipoDescricao",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${ponto.hora.format(timeFormatter)} • ${ponto.dataHora.format(dateFormatter)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(24.dp))

                // ══════════════════════════════════════════════════════════
                // CONTEÚDO
                // ══════════════════════════════════════════════════════════
                if (temLocalizacao) {
                    // Card com coordenadas
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Coordenadas",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Lat: %.6f".format(latitude),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Lng: %.6f".format(longitude),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botão para abrir no Google Maps
                    Button(
                        onClick = {
                            val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Ponto)")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")

                            // Se não tiver Google Maps, abre no navegador
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps?q=$latitude,$longitude")
                                )
                                context.startActivity(browserIntent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir no Google Maps")
                    }
                } else {
                    // Sem localização
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Localização não disponível",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Este registro não possui dados de localização.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ══════════════════════════════════════════════════════════
                // FOOTER
                // ══════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}
