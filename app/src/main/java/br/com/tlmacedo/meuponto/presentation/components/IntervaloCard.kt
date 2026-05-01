// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/IntervaloCard.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPausa
import br.com.tlmacedo.meuponto.presentation.components.swipe.SwipeablePontoRow
import br.com.tlmacedo.meuponto.presentation.components.theme.ThemedCard
import br.com.tlmacedo.meuponto.presentation.theme.EntradaBg
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaBg
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import br.com.tlmacedo.meuponto.presentation.theme.Warning
import br.com.tlmacedo.meuponto.presentation.theme.WarningLight
import java.time.format.DateTimeFormatter

@Composable
fun IntervaloCard(
    intervalo: IntervaloPonto,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
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
        if (intervalo.temPausaAntes) {
            PausaEntreTurnos(
                intervalo = intervalo
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        ThemedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            horaReal = intervalo.formatarHoraEntradaReal(),
                            horaConsiderada = if (
                                intervalo.toleranciaAplicada &&
                                intervalo.temHoraEntradaConsideradaDiferenteDaReal
                            ) {
                                intervalo.formatarHoraEntradaConsiderada()
                            } else {
                                null
                            },
                            nsr = if (mostrarNsr) intervalo.entrada.nsr else null,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }

                DuracaoTurnoCentral(
                    intervalo = intervalo,
                    mostrarContadorTempoReal = mostrarContadorTempoReal
                )

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
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                horaReal = intervalo.formatarHoraSaida() ?: "--:--",
                                horaConsiderada = null,
                                nsr = if (mostrarNsr) intervalo.saida.nsr else null,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        PontoAguardando(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private enum class TipoRegistro {
    ENTRADA,
    SAIDA
}

@Composable
private fun DuracaoTurnoCentral(
    intervalo: IntervaloPonto,
    mostrarContadorTempoReal: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .weight(1f)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    color = if (intervalo.aberto) {
                        WarningLight
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
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
                    tint = if (intervalo.aberto) {
                        Warning
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.size(12.dp)
                )

                if (intervalo.aberto && mostrarContadorTempoReal) {
                    LiveCounterCompact(
                        dataHoraInicio = intervalo.entradaParaCalculo
                    )
                } else {
                    Text(
                        text = intervalo.formatarDuracaoCompacta(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (intervalo.aberto) {
                            Warning
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .width(2.dp)
                .weight(1f)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
private fun PontoContent(
    tipo: TipoRegistro,
    ponto: Ponto,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    horaReal: String,
    horaConsiderada: String?,
    nsr: String?,
    modifier: Modifier = Modifier
) {
    val isEntrada = tipo == TipoRegistro.ENTRADA
    val corPrimaria = if (isEntrada) EntradaColor else SaidaColor
    val corFundo = if (isEntrada) EntradaBg else SaidaBg
    val icone = if (isEntrada) {
        Icons.AutoMirrored.Filled.Login
    } else {
        Icons.AutoMirrored.Filled.Logout
    }
    val label = if (isEntrada) "Entrada" else "Saída"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp)
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

            if (ponto.temFotoComprovante) {
                val iconeFoto = when (ponto.fotoOrigem) {
                    FotoOrigem.CAMERA -> Icons.Default.CameraAlt
                    FotoOrigem.GALERIA -> Icons.Default.Image
                    FotoOrigem.EDITADA -> Icons.Default.AutoAwesome
                    else -> Icons.Default.Image
                }

                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
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
                                .sharedElement(
                                    rememberSharedContentState(key = "foto_${ponto.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                        ) {
                            Icon(
                                imageVector = iconeFoto,
                                contentDescription = "Tem foto",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                } else {
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
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = corPrimaria
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (horaConsiderada != null && horaConsiderada != horaReal) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    color = corPrimaria
                )
            }
        } else {
            Text(
                text = horaReal,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = if (ponto.temAjusteTolerancia) {
                    corPrimaria
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        if (ponto.horaAutoFilled) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Extraído do comprovante",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        if (!nsr.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "# $nsr",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (ponto.nsrAutoFilled) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Extraído do comprovante",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

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

@Composable
private fun PausaEntreTurnos(
    intervalo: IntervaloPonto,
    modifier: Modifier = Modifier
) {
    val tipoPausa = intervalo.tipoPausa ?: TipoPausa.CAFE

    val icone: ImageVector = when (tipoPausa) {
        TipoPausa.CAFE -> Icons.Default.Coffee
        TipoPausa.SAIDA_RAPIDA -> Icons.AutoMirrored.Filled.DirectionsWalk
        TipoPausa.ALMOCO -> Icons.Default.Restaurant
    }

    val textoReal = intervalo.formatarPausaAntesCompacta().orEmpty()
    val textoConsiderado = intervalo.formatarPausaConsideradaCompacta().orEmpty()

    val deveMostrarAjuste =
        intervalo.temPausaConsideradaDiferenteDaReal &&
                textoReal.isNotBlank() &&
                textoConsiderado.isNotBlank() &&
                textoReal != textoConsiderado

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
            tonalElevation = 1.dp,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                ) {
                    Icon(
                        imageVector = icone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (deveMostrarAjuste) {
                        Text(
                            text = textoReal,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                            textDecoration = TextDecoration.LineThrough,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tipoPausa.descricao,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (deveMostrarAjuste) textoConsiderado else textoReal,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = if (deveMostrarAjuste) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

//                    if (deveMostrarAjuste) {
//                        Text(
//                            text = "considerado para cálculo",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
//                            textAlign = TextAlign.Center
//                        )
//                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    }
}