// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/IntervaloCard.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPausa
import br.com.tlmacedo.meuponto.presentation.components.swipe.SwipeablePontoRow
import br.com.tlmacedo.meuponto.presentation.theme.EntradaBg
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaBg
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import br.com.tlmacedo.meuponto.presentation.theme.Warning
import br.com.tlmacedo.meuponto.presentation.theme.WarningLight
import java.time.format.DateTimeFormatter

/**
 * Card que exibe um intervalo de trabalho (entrada -> saída) com suporte a swipe.
 *
 * Cada registro de ponto (entrada e saída) possui swipe individual:
 * - Swipe para ESQUERDA: revela botão Excluir
 * - Swipe para DIREITA: revela Editar, Ver Foto, Ver Localização
 *
 * @param intervalo Intervalo a ser exibido
 * @param mostrarContadorTempoReal Se deve exibir contador em tempo real
 * @param mostrarNsr Se deve exibir o NSR (quando habilitado no emprego)
 * @param onEditar Callback para editar um ponto
 * @param onExcluir Callback para excluir um ponto
 * @param onVerFoto Callback para ver foto de um ponto
 * @param onVerLocalizacao Callback para ver localização de um ponto
 * @param modifier Modificador opcional
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 7.2.0 - Swipe individual para cada registro de ponto
 */
@Composable
fun IntervaloCard(
    intervalo: IntervaloPonto,
    mostrarContadorTempoReal: Boolean = true,
    mostrarNsr: Boolean = false,
    onEditar: (Ponto) -> Unit = {},
    onExcluir: (Ponto) -> Unit = {},
    onVerFoto: (Ponto) -> Unit = {},
    onVerLocalizacao: (Ponto) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val formatadorHora = DateTimeFormatter.ofPattern("HH:mm")

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
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ══════════════════════════════════════════════════════════
                // COLUNA ESQUERDA - ENTRADA (com swipe)
                // ══════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    SwipeablePontoRow(
                        ponto = intervalo.entrada,
                        onEditar = onEditar,
                        onExcluir = onExcluir,
                        onVerFoto = onVerFoto,
                        onVerLocalizacao = onVerLocalizacao
                    ) {
                        PontoContent(
                            tipo = TipoRegistro.ENTRADA,
                            ponto = intervalo.entrada,
                            horaReal = intervalo.entrada.hora.format(formatadorHora),
                            horaConsiderada = if (intervalo.temHoraEntradaConsiderada) {
                                intervalo.horaEntradaConsiderada!!.toLocalTime().format(formatadorHora)
                            } else if (intervalo.entrada.temAjusteTolerancia) {
                                intervalo.entrada.horaConsiderada.format(formatadorHora)
                            } else null,
                            nsr = if (mostrarNsr) intervalo.entrada.nsr else null,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
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
                        .padding(horizontal = 4.dp)
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
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (intervalo.aberto) Warning else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(12.dp)
                            )

                            if (intervalo.aberto && mostrarContadorTempoReal) {
                                LiveCounterCompact(
                                    dataHoraInicio = intervalo.entrada.dataHoraEfetiva
                                )
                            } else {
                                Text(
                                    text = intervalo.formatarDuracaoCompacta(),
                                    style = MaterialTheme.typography.labelSmall,
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
                // COLUNA DIREITA - SAÍDA (com swipe)
                // ══════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (intervalo.saida != null) {
                        SwipeablePontoRow(
                            ponto = intervalo.saida,
                            onEditar = onEditar,
                            onExcluir = onExcluir,
                            onVerFoto = onVerFoto,
                            onVerLocalizacao = onVerLocalizacao
                        ) {
                            PontoContent(
                                tipo = TipoRegistro.SAIDA,
                                ponto = intervalo.saida,
                                horaReal = intervalo.saida.hora.format(formatadorHora),
                                horaConsiderada = null, // Saída não tem tolerância
                                nsr = if (mostrarNsr) intervalo.saida.nsr else null,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        // Aguardando saída (sem swipe)
                        PontoAguardando(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tipo de registro de ponto.
 */
private enum class TipoRegistro {
    ENTRADA, SAIDA
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun PontoContent(
    tipo: TipoRegistro,
    ponto: Ponto,
    horaReal: String,
    horaConsiderada: String?,
    nsr: String?,
    modifier: Modifier = Modifier
) {
    val isEntrada = tipo == TipoRegistro.ENTRADA
    val corPrimaria = if (isEntrada) EntradaColor else SaidaColor
    val corFundo = if (isEntrada) EntradaBg else SaidaBg
    val icone = if (isEntrada) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout
    val label = if (isEntrada) "Entrada" else "Saída"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Ícone
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(corFundo)
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = label,
                    tint = corPrimaria,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Identificador de foto (se houver)
            if (ponto.temFotoComprovante) {
                val isCamera = ponto.fotoComprovantePath?.contains("camera", ignoreCase = true) == true
                val iconeFoto = if (isCamera) Icons.Default.CameraAlt else Icons.Default.Image

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(1.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                ) {
                    Icon(
                        imageVector = iconeFoto,
                        contentDescription = "Tem foto",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = corPrimaria
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Hora (com tolerância se aplicável)
        if (horaConsiderada != null) {
            Text(
                text = horaReal,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.LineThrough
            )
            Text(
                text = horaConsiderada,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = if (isEntrada) Color(0xFF34D399) else corPrimaria
            )
        } else {
            Text(
                text = horaReal,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = if (ponto.temAjusteTolerancia) corPrimaria else MaterialTheme.colorScheme.onSurface
            )
        }

        // NSR (se disponível)
        if (!nsr.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "# $nsr",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Placeholder visual para saída ainda não registrada.
 */
@Composable
private fun PontoAguardando(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(WarningLight)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Aguardando saída",
                tint = Warning,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Saída",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = Warning
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "--:--",
            style = MaterialTheme.typography.titleLarge,
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

/**
 * Componente que exibe o tempo de pausa/intervalo entre turnos.
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
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Tempo Real (Tachado) - Aparece apenas se houver tolerância aplicada
            if (textoConsiderado != null) {
                Text(
                    text = textoReal,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textDecoration = TextDecoration.LineThrough
                )
            }

            // Linha Principal: Ícone + Descrição + Tempo Considerado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = buildString {
                        append(tipoPausa.descricao)
                        append(" ")
                        append(textoConsiderado ?: textoReal)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Intervalo com Tolerância Aplicada")
@Composable
private fun IntervaloComToleranciaPreview() {
    val hoje = java.time.LocalDate.now()
    val empregoId = 1L
    
    // Simula uma pausa de 68 minutos (mínimo 60, tolerância 15)
    // A tolerância deve "puxar" para 60min e marcar a entrada seguinte como considerada
    val ponto1 = br.com.tlmacedo.meuponto.domain.model.Ponto.criar(empregoId, hoje.atTime(8, 0)).copy(id = 1L)
    val ponto2 = br.com.tlmacedo.meuponto.domain.model.Ponto.criar(empregoId, hoje.atTime(12, 0)).copy(id = 2L)
    val ponto3 = br.com.tlmacedo.meuponto.domain.model.Ponto.criar(empregoId, hoje.atTime(13, 8)).copy(id = 3L) // 68min de pausa
    val ponto4 = br.com.tlmacedo.meuponto.domain.model.Ponto.criar(empregoId, hoje.atTime(17, 0)).copy(id = 4L)

    val resumo = br.com.tlmacedo.meuponto.domain.model.ResumoDia(
        data = hoje,
        pontos = listOf(ponto1, ponto2, ponto3, ponto4),
        intervaloMinimoMinutos = 60,
        toleranciaIntervaloMinutos = 15,
        saidaIntervaloIdeal = null // Força a lógica de proximidade (global)
    )

    br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(16.dp), 
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            Text("Exemplo de Tolerância Global (Sábado/Sem Horário Fixo)", style = MaterialTheme.typography.titleSmall)
            // Exibimos o segundo intervalo, que contém a pausa de almoço antes dele
            IntervaloCard(
                intervalo = resumo.intervalos[1],
                mostrarNsr = true
            )
        }
    }
}
