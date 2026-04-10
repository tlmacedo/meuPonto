// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/LiveCounter.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tlmacedo.meuponto.presentation.theme.Success
import br.com.tlmacedo.meuponto.presentation.theme.SuccessLight
import br.com.tlmacedo.meuponto.presentation.theme.Warning
import br.com.tlmacedo.meuponto.presentation.theme.WarningLight
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

/**
 * Contador em tempo real que exibe a duração desde um horário inicial.
 *
 * Atualiza a cada segundo e exibe no formato HH:mm:ss com animações
 * suaves de transição entre os valores.
 *
 * @param dataHoraInicio Data e hora de início da contagem
 * @param modifier Modificador opcional
 * @param fontSize Tamanho da fonte do contador
 * @param showIcon Se deve exibir o ícone de play
 * @param color Cor do texto (se null, usa cor baseada no tempo)
 * @param showBackground Se deve exibir fundo colorido
 *
 * @author Thiago
 * @since 2.0.0
 */
@Composable
fun LiveCounter(
    dataHoraInicio: LocalDateTime,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    showIcon: Boolean = true,
    color: Color? = null,
    showBackground: Boolean = false
) {
    // Estado para armazenar os segundos decorridos
    var elapsedSeconds by remember(dataHoraInicio) { 
        mutableLongStateOf(calcularSegundosDecorridos(dataHoraInicio)) 
    }

    // Atualiza o contador a cada segundo
    LaunchedEffect(dataHoraInicio) {
        while (true) {
            elapsedSeconds = calcularSegundosDecorridos(dataHoraInicio)
            delay(1000L)
        }
    }

    // Animação de pulsação para o ícone
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Formata o tempo
    val (horas, minutos, segundos) = formatarTempoDecorrido(elapsedSeconds)
    
    // Determina a cor baseada no tempo (se não foi especificada)
    val corTexto = color ?: determinarCorPorTempo(elapsedSeconds)
    val corFundo = determinarCorFundoPorTempo(elapsedSeconds)

    val content = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = if (showBackground) 12.dp else 0.dp, vertical = if (showBackground) 6.dp else 0.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Contador ativo",
                    tint = corTexto,
                    modifier = Modifier
                        .size(if (fontSize > 18.sp) 24.dp else 16.dp)
                        .alpha(pulseAlpha)
                )
            }

            // Horas com animação
            AnimatedDigit(
                value = horas,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = corTexto
            )

            // Separador pulsante
            Text(
                text = ":",
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = corTexto,
                modifier = Modifier.alpha(pulseAlpha)
            )

            // Minutos com animação
            AnimatedDigit(
                value = minutos,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = corTexto
            )

            // Separador pulsante
            Text(
                text = ":",
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = corTexto,
                modifier = Modifier.alpha(pulseAlpha)
            )

            // Segundos com animação
            AnimatedDigit(
                value = segundos,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = corTexto
            )
        }
    }

    if (showBackground) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .background(corFundo)
        ) {
            content()
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}

/**
 * Contador compacto para exibição em cards e listas.
 *
 * Versão simplificada do LiveCounter com formato HH:mm:ss
 * e indicador visual de status.
 *
 * @param dataHoraInicio Data e hora de início da contagem
 * @param modifier Modificador opcional
 *
 * @author Thiago
 * @since 2.0.0
 */
@Composable
fun LiveCounterCompact(
    dataHoraInicio: LocalDateTime,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember(dataHoraInicio) { 
        mutableLongStateOf(calcularSegundosDecorridos(dataHoraInicio)) 
    }

    LaunchedEffect(dataHoraInicio) {
        while (true) {
            elapsedSeconds = calcularSegundosDecorridos(dataHoraInicio)
            delay(1000L)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    val (horas, minutos, segundos) = formatarTempoDecorrido(elapsedSeconds)
    val textoFormatado = "$horas:$minutos:$segundos"
    val corTexto = determinarCorPorTempo(elapsedSeconds)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        // Indicador pulsante
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(blinkAlpha)
                .clip(CircleShape)
                .background(corTexto)
        )

        Text(
            text = textoFormatado,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = corTexto
        )
    }
}

/**
 * Dígito animado para transição suave entre valores.
 */
@Composable
private fun AnimatedDigit(
    value: String,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    color: Color
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (fadeIn() + slideInVertically { -it / 2 }) togetherWith 
            (fadeOut() + slideOutVertically { it / 2 })
        },
        label = "digitAnimation"
    ) { targetValue ->
        Text(
            text = targetValue,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

/**
 * Calcula os segundos decorridos desde a data/hora inicial.
 */
private fun calcularSegundosDecorridos(inicio: LocalDateTime): Long {
    val agora = LocalDateTime.now()
    return if (agora.isAfter(inicio)) {
        Duration.between(inicio, agora).seconds
    } else {
        0L
    }
}

/**
 * Formata o tempo decorrido em horas, minutos e segundos.
 *
 * @return Triple com (horas, minutos, segundos) formatados com 2 dígitos
 */
private fun formatarTempoDecorrido(totalSegundos: Long): Triple<String, String, String> {
    val horas = (totalSegundos / 3600).toString().padStart(2, '0')
    val minutos = ((totalSegundos % 3600) / 60).toString().padStart(2, '0')
    val segundos = (totalSegundos % 60).toString().padStart(2, '0')
    return Triple(horas, minutos, segundos)
}

/**
 * Determina a cor do texto baseada no tempo decorrido.
 *
 * - Verde: até 8 horas (jornada normal)
 * - Amarelo/Warning: mais de 8 horas (hora extra)
 */
private fun determinarCorPorTempo(totalSegundos: Long): Color {
    val horasDecorridas = totalSegundos / 3600
    return when {
        horasDecorridas >= 10 -> Warning // Mais de 10h - alerta forte
        horasDecorridas >= 8 -> Warning  // Hora extra
        else -> Success                   // Jornada normal
    }
}

/**
 * Determina a cor de fundo baseada no tempo decorrido.
 */
private fun determinarCorFundoPorTempo(totalSegundos: Long): Color {
    val horasDecorridas = totalSegundos / 3600
    return when {
        horasDecorridas >= 8 -> WarningLight
        else -> SuccessLight
    }
}
