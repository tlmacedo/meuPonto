// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/lixeira/components/LixeiraItemCard.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.lixeira.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.presentation.screen.lixeira.TipoItemLixeira
import br.com.tlmacedo.meuponto.presentation.screen.settings.lixeira.ItemLixeira
import br.com.tlmacedo.meuponto.presentation.screen.settings.lixeira.TipoItemLixeira

/**
 * Card de item na lixeira.
 *
 * Exibe informações do item excluído com ações de restaurar ou excluir permanentemente.
 *
 * @param item Item da lixeira a ser exibido
 * @param isSelected Se o item está selecionado
 * @param modoSelecao Se está em modo de seleção múltipla
 * @param onRestaurar Callback ao restaurar o item
 * @param onExcluirPermanente Callback ao excluir permanentemente
 * @param onSelecionar Callback ao selecionar/desselecionar
 * @param onLongPress Callback ao pressionar longamente (ativa seleção)
 *
 * @author Thiago
 * @since 9.2.0
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LixeiraItemCard(
    item: ItemLixeira,
    isSelected: Boolean,
    modoSelecao: Boolean,
    onRestaurar: () -> Unit,
    onExcluirPermanente: () -> Unit,
    onSelecionar: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "containerColor"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (modoSelecao) {
                        onSelecionar()
                    }
                },
                onLongClick = onLongPress
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Ícone de seleção ou tipo
            if (modoSelecao) {
                Icon(
                    imageVector = if (isSelected) {
                        Icons.Outlined.CheckCircle
                    } else {
                        getIconForTipo(item.tipo)
                    },
                    contentDescription = if (isSelected) "Selecionado" else null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    imageVector = getIconForTipo(item.tipo),
                    contentDescription = null,
                    tint = getColorForTipo(item.tipo),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informações do item
            Column(modifier = Modifier.weight(1f)) {
                // Tipo badge
                Text(
                    text = getLabelForTipo(item.tipo),
                    style = MaterialTheme.typography.labelSmall,
                    color = getColorForTipo(item.tipo),
                    fontWeight = FontWeight.Medium
                )

                // Título
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                // Subtítulo
                Text(
                    text = item.subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Dias restantes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (item.diasRestantes <= 7) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                    Text(
                        text = when {
                            item.diasRestantes == 0 -> "Será excluído hoje"
                            item.diasRestantes == 1 -> "1 dia restante"
                            else -> "${item.diasRestantes} dias restantes"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.diasRestantes <= 7) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }

            // Ações (apenas se não estiver em modo seleção)
            if (!modoSelecao) {
                IconButton(onClick = onRestaurar) {
                    Icon(
                        imageVector = Icons.Outlined.RestoreFromTrash,
                        contentDescription = "Restaurar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onExcluirPermanente) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir permanentemente",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Retorna ícone para o tipo de item.
 */
@Composable
private fun getIconForTipo(tipo: TipoItemLixeira): ImageVector {
    return when (tipo) {
        TipoItemLixeira.PONTO -> Icons.Outlined.AccessTime
        TipoItemLixeira.EMPREGO -> Icons.Outlined.Business
        TipoItemLixeira.FERIADO -> Icons.Outlined.Celebration
        TipoItemLixeira.AUSENCIA -> Icons.Outlined.BeachAccess
        TipoItemLixeira.VERSAO_JORNADA -> Icons.Outlined.Schedule
    }
}

/**
 * Retorna cor para o tipo de item.
 */
@Composable
private fun getColorForTipo(tipo: TipoItemLixeira) = when (tipo) {
    TipoItemLixeira.PONTO -> MaterialTheme.colorScheme.primary
    TipoItemLixeira.EMPREGO -> MaterialTheme.colorScheme.secondary
    TipoItemLixeira.FERIADO -> MaterialTheme.colorScheme.tertiary
    TipoItemLixeira.AUSENCIA -> MaterialTheme.colorScheme.error
    TipoItemLixeira.VERSAO_JORNADA -> MaterialTheme.colorScheme.outline
}

/**
 * Retorna label para o tipo de item.
 */
private fun getLabelForTipo(tipo: TipoItemLixeira): String = when (tipo) {
    TipoItemLixeira.PONTO -> "PONTO"
    TipoItemLixeira.EMPREGO -> "EMPREGO"
    TipoItemLixeira.FERIADO -> "FERIADO"
    TipoItemLixeira.AUSENCIA -> "AUSÊNCIA"
    TipoItemLixeira.VERSAO_JORNADA -> "JORNADA"
}
