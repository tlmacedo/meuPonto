// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/LocationChip.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Chip compacto para exibição de localização.
 *
 * @author Thiago
 * @since 3.5.0
 */
@Composable
fun LocationChip(
    latitude: Double?,
    longitude: Double?,
    endereco: String?,
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val temLocalizacao = latitude != null && longitude != null

    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = when {
                    endereco?.isNotBlank() == true -> endereco
                    temLocalizacao -> "%.4f, %.4f".format(latitude, longitude)
                    else -> "Adicionar localização"
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = if (temLocalizacao) Icons.Default.LocationOn else Icons.Default.LocationOff,
                contentDescription = null,
                tint = if (temLocalizacao)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (temLocalizacao && onClear != null) {
            {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpar",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        } else null,
        modifier = modifier
    )
}

/**
 * Card expandido para exibição de localização com mais detalhes.
 */
@Composable
fun LocationCard(
    latitude: Double?,
    longitude: Double?,
    endereco: String?,
    precisao: Float? = null,
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val temLocalizacao = latitude != null && longitude != null

    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Ícone
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (temLocalizacao)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = if (temLocalizacao) Icons.Default.LocationOn else Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = if (temLocalizacao)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informações
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Localização",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (temLocalizacao) {
                    // Endereço ou coordenadas
                    Text(
                        text = endereco?.takeIf { it.isNotBlank() }
                            ?: "%.6f, %.6f".format(latitude, longitude),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Precisão
                    precisao?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    it <= 10f -> Icons.Default.GpsFixed
                                    it <= 30f -> Icons.Default.GpsNotFixed
                                    else -> Icons.Default.GpsOff
                                },
                                contentDescription = null,
                                tint = when {
                                    it <= 10f -> MaterialTheme.colorScheme.primary
                                    it <= 30f -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Precisão: ${it.toInt()}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Toque para adicionar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botão limpar
            if (temLocalizacao && onClear != null) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpar localização"
                    )
                }
            }
        }
    }
}
