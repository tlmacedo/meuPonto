// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/components/FeriadoCard.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components

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
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Card que exibe informações de um feriado na lista.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Composable
fun FeriadoCard(
    feriado: Feriado,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    onToggleAtivo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val cardAlpha = if (feriado.ativo) 1f else 0.6f
    val backgroundColor by animateColorAsState(
        targetValue = when (feriado.tipo) {
            TipoFeriado.PONTE -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            TipoFeriado.NACIONAL -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surface
        },
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
                    .background(feriado.tipo.cor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = feriado.tipo.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informações do feriado
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = feriado.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Data com dia da semana
                    Text(
                        text = formatarData(feriado),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Badge do tipo
                    TipoBadge(tipo = feriado.tipo)
                }

                // Observação (se houver)
                feriado.observacao?.let { obs ->
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
                            Text(if (feriado.ativo) "Desativar" else "Ativar")
                        },
                        onClick = {
                            showMenu = false
                            onToggleAtivo()
                        },
                        leadingIcon = {
                            Switch(
                                checked = feriado.ativo,
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

@Composable
private fun TipoBadge(tipo: TipoFeriado) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(tipo.cor.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = tipo.descricao,
            style = MaterialTheme.typography.labelSmall,
            color = tipo.cor
        )
    }
}

/**
 * Formata a data do feriado incluindo o dia da semana.
 * - Feriados anuais: "25/12 (Quinta)" ou com ano atual para referência
 * - Feriados únicos: "15/11/2026 (Domingo)"
 */
private fun formatarData(feriado: Feriado): String {
    val locale = Locale("pt", "BR")

    return when (feriado.recorrencia) {
        RecorrenciaFeriado.ANUAL -> {
            feriado.diaMes?.let { diaMes ->
                // Para feriados anuais, usa o ano atual para calcular o dia da semana
                val anoAtual = LocalDate.now().year
                val dataNoAnoAtual = LocalDate.of(anoAtual, diaMes.month, diaMes.dayOfMonth)
                val diaSemana = dataNoAnoAtual.dayOfWeek
                    .getDisplayName(TextStyle.SHORT, locale)
                    .replaceFirstChar { it.uppercase() }
                    .removeSuffix(".")

                val dia = diaMes.dayOfMonth.toString().padStart(2, '0')
                val mes = diaMes.monthValue.toString().padStart(2, '0')

                "$dia/$mes ($diaSemana) • anual"
            } ?: "Data não definida"
        }
        RecorrenciaFeriado.UNICO -> {
            feriado.dataEspecifica?.let { data ->
                val diaSemana = data.dayOfWeek
                    .getDisplayName(TextStyle.SHORT, locale)
                    .replaceFirstChar { it.uppercase() }
                    .removeSuffix(".")

                val dia = data.dayOfMonth.toString().padStart(2, '0')
                val mes = data.monthValue.toString().padStart(2, '0')
                val ano = data.year

                "$dia/$mes/$ano ($diaSemana)"
            } ?: "Data não definida"
        }
    }
}

/**
 * Cor associada a cada tipo de feriado.
 */
private val TipoFeriado.cor: Color
    get() = when (this) {
        TipoFeriado.NACIONAL -> Color(0xFF1976D2)      // Azul
        TipoFeriado.ESTADUAL -> Color(0xFF388E3C)      // Verde
        TipoFeriado.MUNICIPAL -> Color(0xFF7B1FA2)     // Roxo
        TipoFeriado.FACULTATIVO -> Color(0xFFFFA000)   // Âmbar
        TipoFeriado.PONTE -> Color(0xFFD32F2F)         // Vermelho
    }
