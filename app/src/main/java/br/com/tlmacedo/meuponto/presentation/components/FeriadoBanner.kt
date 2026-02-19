// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/FeriadoBanner.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.WorkOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado

/**
 * Banner que exibe informações sobre feriados do dia.
 *
 * @param feriados Lista de feriados do dia
 * @param modifier Modifier opcional
 *
 * @author Thiago
 * @since 3.4.0
 */
@Composable
fun FeriadoBanner(
    feriados: List<Feriado>,
    modifier: Modifier = Modifier
) {
    if (feriados.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    val feriadoPrincipal = feriados.first()
    val temMultiplos = feriados.size > 1

    val backgroundColor = feriadoPrincipal.tipo.getBackgroundColor()
    val contentColor = feriadoPrincipal.tipo.getContentColor()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (temMultiplos) {
                        Modifier.clickable { expanded = !expanded }
                    } else {
                        Modifier
                    }
                )
                .padding(16.dp)
        ) {
            // Header com ícone e nome do feriado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ícone do tipo de feriado
                Icon(
                    imageVector = feriadoPrincipal.tipo.getIcon(),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Nome e tipo do feriado
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = feriadoPrincipal.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FeriadoTipoChip(
                            tipo = feriadoPrincipal.tipo,
                            contentColor = contentColor
                        )

                        // Localização para estadual/municipal
                        feriadoPrincipal.uf?.let { uf ->
                            Text(
                                text = "• $uf${feriadoPrincipal.municipio?.let { " - $it" } ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Indicador de múltiplos feriados
                if (temMultiplos) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = contentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "+${feriados.size - 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Recolher" else "Expandir",
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Observação do feriado principal
            feriadoPrincipal.observacao?.let { obs ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = obs,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Lista expandida de feriados adicionais
            if (expanded && temMultiplos) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = contentColor.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                feriados.drop(1).forEach { feriado ->
                    FeriadoItemCompacto(
                        feriado = feriado,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Chip que exibe o tipo do feriado.
 */
@Composable
private fun FeriadoTipoChip(
    tipo: TipoFeriado,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = contentColor.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Text(
            text = tipo.descricao,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Item compacto de feriado para lista expandida.
 */
@Composable
private fun FeriadoItemCompacto(
    feriado: Feriado,
    modifier: Modifier = Modifier
) {
    val contentColor = feriado.tipo.getContentColor()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = feriado.tipo.getIcon(),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = feriado.nome,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier.weight(1f)
        )

        FeriadoTipoChip(
            tipo = feriado.tipo,
            contentColor = contentColor
        )
    }
}

// ============================================================================
// Extensões para TipoFeriado
// ============================================================================

/**
 * Retorna a cor de fundo apropriada para cada tipo de feriado.
 */
private fun TipoFeriado.getBackgroundColor(): Color = when (this) {
    TipoFeriado.NACIONAL -> Color(0xFFE8F5E9)    // Verde claro
    TipoFeriado.ESTADUAL -> Color(0xFFE3F2FD)    // Azul claro
    TipoFeriado.MUNICIPAL -> Color(0xFFFFF3E0)   // Laranja claro
    TipoFeriado.FACULTATIVO -> Color(0xFFFCE4EC) // Rosa claro
    TipoFeriado.PONTE -> Color(0xFFF3E5F5)       // Roxo claro
}

/**
 * Retorna a cor de conteúdo apropriada para cada tipo de feriado.
 */
private fun TipoFeriado.getContentColor(): Color = when (this) {
    TipoFeriado.NACIONAL -> Color(0xFF2E7D32)    // Verde escuro
    TipoFeriado.ESTADUAL -> Color(0xFF1565C0)    // Azul escuro
    TipoFeriado.MUNICIPAL -> Color(0xFFE65100)   // Laranja escuro
    TipoFeriado.FACULTATIVO -> Color(0xFFC2185B) // Rosa escuro
    TipoFeriado.PONTE -> Color(0xFF7B1FA2)       // Roxo escuro
}

/**
 * Retorna o ícone apropriado para cada tipo de feriado.
 */
private fun TipoFeriado.getIcon(): ImageVector = when (this) {
    TipoFeriado.NACIONAL -> Icons.Default.Flag
    TipoFeriado.ESTADUAL -> Icons.Default.Place
    TipoFeriado.MUNICIPAL -> Icons.Default.LocationCity
    TipoFeriado.FACULTATIVO -> Icons.Outlined.Description
    TipoFeriado.PONTE -> Icons.Outlined.WorkOff
}
