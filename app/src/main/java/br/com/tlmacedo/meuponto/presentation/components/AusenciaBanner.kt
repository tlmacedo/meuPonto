// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/AusenciaBanner.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import java.time.format.DateTimeFormatter

private val horaFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Banner que exibe informações sobre ausências do dia (férias, atestado, folga, etc.).
 * Layout otimizado para Declarações com informações compactas.
 *
 * @param ausencia Ausência do dia
 * @param modifier Modifier opcional
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.5.0 - Layout melhorado para Declarações
 */
@Composable
fun AusenciaBanner(
    ausencia: Ausencia,
    modifier: Modifier = Modifier
) {
    val backgroundColor = ausencia.tipo.getBackgroundColor()
    val contentColor = ausencia.tipo.getContentColor()

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
                .padding(12.dp)
        ) {
            // Header com ícone, tipo e badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ícone do tipo de ausência
                Icon(
                    imageVector = ausencia.tipo.getIcon(),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Tipo da ausência
                val textoTipo = buildString {
                    append(ausencia.tipo.descricao)
                    if (ausencia.tipo == TipoAusencia.FERIAS)
                        append(" (${ausencia.periodoAquisitivo})")

                    ausencia.tipoFolga?.let { append(" (${it.descricao})") }
                }

                Text(
                    text = textoTipo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )


                // Ícone de anexo (se houver imagem)
                if (ausencia.imagemUri != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = contentColor.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Anexo",
                            tint = contentColor,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }

//                // Badge de status
//                AusenciaStatusBadge(
//                    isJustificada = ausencia.isJustificada,
//                    contentColor = contentColor
//                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Conteúdo específico por tipo
            when (ausencia.tipo) {
                TipoAusencia.DECLARACAO -> DeclaracaoContent(
                    ausencia = ausencia,
                    contentColor = contentColor
                )
                else -> DefaultAusenciaContent(
                    ausencia = ausencia,
                    contentColor = contentColor
                )
            }
        }
    }
}

/**
 * Conteúdo específico para Declaração - layout compacto com todas as informações.
 */
@Composable
private fun DeclaracaoContent(
    ausencia: Ausencia,
    contentColor: Color
) {
    // Linha 1: Horário e Duração
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Chip: Horário (início - fim)
        ausencia.horaInicio?.let { inicio ->
            val horaFim = ausencia.horaFimDeclaracao
            InfoChip(
                icon = Icons.Default.Schedule,
                text = if (horaFim != null) {
                    "${inicio.format(horaFormatter)} - ${horaFim.format(horaFormatter)}"
                } else {
                    inicio.format(horaFormatter)
                },
                contentColor = contentColor
            )
        }

        // Chip: Duração total
        ausencia.duracaoDeclaracaoMinutos?.let { duracao ->
            InfoChip(
                icon = Icons.Default.Timer,
                text = formatarMinutos(duracao),
                contentColor = contentColor
            )
        }

        // Chip: Tempo abonado (destaque)
        ausencia.duracaoAbonoMinutos?.let { abono ->
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = contentColor.copy(alpha = 0.2f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatarMinutos(abono)} abonado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }

//    // Linha 2: Motivo/Descrição (se houver)
//    ausencia.descricao?.let { motivo ->
//        Spacer(modifier = Modifier.height(6.dp))
//        Text(
//            text = motivo,
//            style = MaterialTheme.typography.bodySmall,
//            color = contentColor.copy(alpha = 0.85f),
//            maxLines = 2,
//            overflow = TextOverflow.Ellipsis,
//            lineHeight = 16.sp
//        )
//    }

    // Linha 3: Observação adicional (se houver e diferente do motivo)
    ausencia.observacao?.takeIf { it != ausencia.descricao }?.let { obs ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = obs,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Conteúdo padrão para outros tipos de ausência.
 */
@Composable
private fun DefaultAusenciaContent(
    ausencia: Ausencia,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Chip com período
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = contentColor.copy(alpha = 0.12f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventBusy,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ausencia.formatarPeriodo(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
        }

        // Quantidade de dias (se for período)
        if (ausencia.isPeriodo) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = contentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "${ausencia.quantidadeDias} dias",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Descrição (se houver e diferente do tipo)
    ausencia.descricao?.takeIf { it != ausencia.tipo.descricao }?.let { desc ->
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    // Observação (se houver)
    ausencia.observacao?.let { obs ->
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = obs,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Chip de informação reutilizável.
 */
@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = contentColor.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Badge de status (justificada ou não).
 */
@Composable
private fun AusenciaStatusBadge(
    isJustificada: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val icon = if (isJustificada) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel
    val texto = if (isJustificada) "Justificada" else "Injustificada"

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = contentColor.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Formata minutos para exibição (ex: 90 -> "1h30")
 */
private fun formatarMinutos(minutos: Int): String {
    val horas = minutos / 60
    val mins = minutos % 60
    return when {
        horas == 0 -> "${mins}min"
        mins == 0 -> "${horas}h"
        else -> "${horas}h${mins.toString().padStart(2, '0')}"
    }
}

// ============================================================================
// Extensões para TipoAusencia
// ============================================================================

/**
 * Retorna a cor de fundo apropriada para cada tipo de ausência.
 */
private fun TipoAusencia.getBackgroundColor(): Color = when (this) {
    TipoAusencia.FERIAS -> Color(0xFFE0F7FA)           // Ciano claro
    TipoAusencia.ATESTADO -> Color(0xFFFFEBEE)         // Vermelho claro
    TipoAusencia.DECLARACAO -> Color(0xFFFFF8E1)       // Âmbar claro
    TipoAusencia.FOLGA -> Color(0xFFE8F5E9)            // Verde claro
    TipoAusencia.FALTA_JUSTIFICADA -> Color(0xFFE3F2FD) // Azul claro
    TipoAusencia.FALTA_INJUSTIFICADA -> Color(0xFFFFCDD2) // Vermelho mais forte
}

/**
 * Retorna a cor de conteúdo apropriada para cada tipo de ausência.
 */
private fun TipoAusencia.getContentColor(): Color = when (this) {
    TipoAusencia.FERIAS -> Color(0xFF00695C)           // Ciano escuro
    TipoAusencia.ATESTADO -> Color(0xFFC62828)         // Vermelho escuro
    TipoAusencia.DECLARACAO -> Color(0xFFFF8F00)       // Âmbar escuro
    TipoAusencia.FOLGA -> Color(0xFF2E7D32)            // Verde escuro
    TipoAusencia.FALTA_JUSTIFICADA -> Color(0xFF1565C0) // Azul escuro
    TipoAusencia.FALTA_INJUSTIFICADA -> Color(0xFFB71C1C) // Vermelho muito escuro
}

/**
 * Retorna o ícone apropriado para cada tipo de ausência.
 */
private fun TipoAusencia.getIcon(): ImageVector = when (this) {
    TipoAusencia.FERIAS -> Icons.Default.BeachAccess
    TipoAusencia.ATESTADO -> Icons.Default.LocalHospital
    TipoAusencia.DECLARACAO -> Icons.Default.Receipt
    TipoAusencia.FOLGA -> Icons.Default.Home
    TipoAusencia.FALTA_JUSTIFICADA -> Icons.Default.EventBusy
    TipoAusencia.FALTA_INJUSTIFICADA -> Icons.Default.EventBusy
}
