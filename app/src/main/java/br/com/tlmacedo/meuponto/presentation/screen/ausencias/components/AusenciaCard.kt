// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/components/AusenciaCard.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import java.time.format.TextStyle
import java.util.Locale

/**
 * Card que exibe informações de uma ausência na lista.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.6.0 - Menu dropdown com ações
 */
@Composable
fun AusenciaCard(
    ausencia: Ausencia,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    onToggleAtivo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val cardAlpha = if (ausencia.ativo) 1f else 0.6f
    val backgroundColor by animateColorAsState(
        targetValue = ausencia.tipo.cor.copy(alpha = 0.15f),
        label = "cardBackground"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de tipo (emoji)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ausencia.tipo.cor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ausencia.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informações da ausência
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nome do tipo
                Text(
                    text = ausencia.tipoDescricaoCompleta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Data com dia da semana
                Text(
                    text = formatarDataComDiaSemana(ausencia),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Detalhes específicos por tipo
                when (ausencia.tipo) {
                    TipoAusencia.DECLARACAO -> {
                        ausencia.horaInicio?.let { horaInicio ->
                            Text(
                                text = "🕐 ${horaInicio.toString().substring(0, 5)} • ${formatarMinutos(ausencia.duracaoDeclaracaoMinutos ?: 0)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ausencia.duracaoAbonoMinutos?.let { abono ->
                            Text(
                                text = "✅ Abono: ${formatarMinutos(abono)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    else -> {
                        // Mostrar quantidade de dias para períodos
                        if (ausencia.quantidadeDias > 1 || ausencia.isPeriodo) {
                            Text(
                                text = "${ausencia.quantidadeDias} ${if (ausencia.quantidadeDias == 1) "dia" else "dias"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Observação (se houver)
                ausencia.observacao?.takeIf { it.isNotBlank() }?.let { obs ->
                    Text(
                        text = obs,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Menu de ações
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            showMenu = false
                            onEditar()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(if (ausencia.ativo) "Desativar" else "Ativar")
                        },
                        onClick = {
                            showMenu = false
                            onToggleAtivo()
                        },
                        leadingIcon = {
                            Switch(
                                checked = ausencia.ativo,
                                onCheckedChange = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Excluir",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onExcluir()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Formata a data da ausência incluindo o dia da semana.
 */
private fun formatarDataComDiaSemana(ausencia: Ausencia): String {
    val locale = Locale("pt", "BR")
    val dataInicio = ausencia.dataInicio
    val dataFim = ausencia.dataFim

    val diaSemanaInicio = dataInicio.dayOfWeek
        .getDisplayName(TextStyle.SHORT, locale)
        .replaceFirstChar { it.uppercase() }
        .removeSuffix(".")

    val diaInicio = dataInicio.dayOfMonth.toString().padStart(2, '0')
    val mesInicio = dataInicio.monthValue.toString().padStart(2, '0')
    val anoInicio = dataInicio.year

    return if (ausencia.isDiaUnico) {
        "$diaInicio/$mesInicio/$anoInicio ($diaSemanaInicio)"
    } else {
        val diaFim = dataFim.dayOfMonth.toString().padStart(2, '0')
        val mesFim = dataFim.monthValue.toString().padStart(2, '0')
        val anoFim = dataFim.year

        if (anoInicio == anoFim) {
            "$diaInicio/$mesInicio - $diaFim/$mesFim/$anoInicio"
        } else {
            "$diaInicio/$mesInicio/$anoInicio - $diaFim/$mesFim/$anoFim"
        }
    }
}

private fun formatarMinutos(minutos: Int): String {
    val horas = minutos / 60
    val mins = minutos % 60
    return when {
        horas > 0 && mins > 0 -> "${horas}h ${mins}min"
        horas > 0 -> "${horas}h"
        else -> "${mins}min"
    }
}

/**
 * Cor associada a cada tipo de ausência.
 */
private val TipoAusencia.cor: Color
    get() = when (this) {
        TipoAusencia.FERIAS -> Color(0xFF1976D2)            // Azul
        TipoAusencia.ATESTADO -> Color(0xFFD32F2F)          // Vermelho
        TipoAusencia.DECLARACAO -> Color(0xFF7B1FA2)        // Roxo
        TipoAusencia.FALTA_JUSTIFICADA -> Color(0xFFFFA000) // Âmbar
        TipoAusencia.FOLGA -> Color(0xFF388E3C)             // Verde
        TipoAusencia.FALTA_INJUSTIFICADA -> Color(0xFF616161) // Cinza
    }
