// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/IntervaloCard.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.TipoPausa
import br.com.tlmacedo.meuponto.presentation.theme.EntradaBg
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaBg
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import br.com.tlmacedo.meuponto.presentation.theme.Warning
import br.com.tlmacedo.meuponto.presentation.theme.WarningLight
import java.time.format.DateTimeFormatter

/**
 * Card que exibe um intervalo de trabalho (entrada -> saída).
 *
 * @param intervalo Intervalo a ser exibido
 * @param mostrarContadorTempoReal Se deve exibir contador em tempo real
 * @param mostrarNsr Se deve exibir o NSR (quando habilitado no emprego)
 * @param onEditarEntrada Callback para editar entrada (long press)
 * @param onEditarSaida Callback para editar saída (long press)
 * @param modifier Modificador opcional
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 3.6.0 - Novo layout diagonal com suporte a edição via long press
 * @updated 3.7.0 - Adicionada exibição do NSR
 * @updated 3.8.0 - Melhorias visuais: alinhamento centralizado e maior contraste
 * @updated 4.2.0 - Classificação correta de pausas (Café, Saída Rápida, Almoço)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntervaloCard(
    intervalo: IntervaloPonto,
    mostrarContadorTempoReal: Boolean = true,
    mostrarNsr: Boolean = false,
    onEditarEntrada: ((Long) -> Unit)? = null,
    onEditarSaida: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val formatadorHora = DateTimeFormatter.ofPattern("HH:mm")
    val haptic = LocalHapticFeedback.current

    var showMenuEntrada by remember { mutableStateOf(false) }
    var showMenuSaida by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Pausa antes do turno (se houver)
        if (intervalo.temPausaAntes) {
            PausaEntreIntervalos(
                textoReal = intervalo.formatarPausaAntesCompacta() ?: "",
                textoConsiderado = if (intervalo.toleranciaAplicada) {
                    intervalo.formatarPausaConsideradaCompacta()
                } else null,
                tipoPausa = intervalo.tipoPausa ?: TipoPausa.CAFE
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Card principal
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ══════════════════════════════════════════════════════════
                // COLUNA ESQUERDA - ENTRADA (centralizada verticalmente)
                // ══════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = { },
                                onLongClick = {
                                    if (onEditarEntrada != null) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showMenuEntrada = true
                                    }
                                }
                            )
                            .padding(8.dp)
                    ) {
                        // Ícone de entrada
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EntradaBg)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = "Entrada",
                                tint = EntradaColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Entrada",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = EntradaColor
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Hora (com tolerância se aplicável)
                        if (intervalo.temHoraEntradaConsiderada) {
                            Text(
                                text = intervalo.entrada.hora.format(formatadorHora),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textDecoration = TextDecoration.LineThrough
                            )
                            Text(
                                text = intervalo.horaEntradaConsiderada!!.toLocalTime().format(formatadorHora),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = EntradaColor
                            )
                        } else {
                            Text(
                                text = intervalo.entrada.hora.format(formatadorHora),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // NSR da entrada (se habilitado e tiver valor)
                        if (mostrarNsr && !intervalo.entrada.nsr.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "# ${intervalo.entrada.nsr}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        // Menu de contexto
                        DropdownMenu(
                            expanded = showMenuEntrada,
                            onDismissRequest = { showMenuEntrada = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar entrada") },
                                onClick = {
                                    showMenuEntrada = false
                                    onEditarEntrada?.invoke(intervalo.entrada.id)
                                }
                            )
                        }
                    }
                }

                // ══════════════════════════════════════════════════════════
                // COLUNA CENTRAL - DURAÇÃO DO TURNO
                // ══════════════════════════════════════════════════════════
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp)
                ) {
                    // Linha vertical superior
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Badge de duração
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(
                                color = if (intervalo.aberto) WarningLight else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (intervalo.aberto) Warning else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )

                            if (intervalo.aberto && mostrarContadorTempoReal) {
                                LiveCounterCompact(
                                    dataHoraInicio = intervalo.entrada.dataHora
                                )
                            } else {
                                Text(
                                    text = intervalo.formatarDuracao(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (intervalo.aberto) Warning else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Linha vertical inferior
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                // ══════════════════════════════════════════════════════════
                // COLUNA DIREITA - SAÍDA (centralizada verticalmente)
                // ══════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (intervalo.saida != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = {
                                        if (onEditarSaida != null) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showMenuSaida = true
                                        }
                                    }
                                )
                                .padding(8.dp)
                        ) {
                            // Ícone de saída
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SaidaBg)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Saída",
                                    tint = SaidaColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Saída",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = SaidaColor
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = intervalo.saida.hora.format(formatadorHora),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // NSR da saída (se habilitado e tiver valor)
                            if (mostrarNsr && !intervalo.saida.nsr.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "# ${intervalo.saida.nsr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }

                            // Menu de contexto
                            DropdownMenu(
                                expanded = showMenuSaida,
                                onDismissRequest = { showMenuSaida = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar saída") },
                                    onClick = {
                                        showMenuSaida = false
                                        onEditarSaida?.invoke(intervalo.saida.id)
                                    }
                                )
                            }
                        }
                    } else {
                        // Aguardando saída
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(WarningLight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Aguardando saída",
                                    tint = Warning,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Saída",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Warning
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "--:--",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Warning
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Aguardando",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Warning.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que exibe o tempo de pausa/intervalo entre turnos.
 *
 * @param textoReal Tempo real da pausa formatado
 * @param textoConsiderado Tempo considerado (com tolerância) formatado, ou null se não aplicável
 * @param tipoPausa Tipo da pausa (Café, Saída Rápida ou Almoço)
 *
 * @updated 4.2.0 - Agora usa TipoPausa para classificação correta
 */
@Composable
private fun PausaEntreIntervalos(
    textoReal: String,
    textoConsiderado: String? = null,
    tipoPausa: TipoPausa,
    modifier: Modifier = Modifier
) {
    val icone: ImageVector = when (tipoPausa) {
        TipoPausa.CAFE -> Icons.Default.Coffee
        TipoPausa.SAIDA_RAPIDA -> Icons.AutoMirrored.Filled.DirectionsWalk
        TipoPausa.ALMOCO -> Icons.Default.Restaurant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Linha do intervalo real (se houver tolerância, mostra cortado)
            if (textoConsiderado != null) {
                Text(
                    text = textoReal,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textDecoration = TextDecoration.LineThrough
                )
            }

            // Linha principal com ícone, tipo e tempo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = "Intervalo de ${tipoPausa.descricao}",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${tipoPausa.descricao} ${textoConsiderado ?: textoReal}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}
