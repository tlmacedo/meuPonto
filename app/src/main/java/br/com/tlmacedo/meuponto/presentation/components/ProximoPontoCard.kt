// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/ProximoPontoCard.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import br.com.tlmacedo.meuponto.presentation.theme.LocalAppThemeController
import br.com.tlmacedo.meuponto.presentation.theme.LocalPremiumTokens
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ProximoPonto
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Card compacto que exibe o próximo ponto a ser registrado e a hora atual.
 *
 * @param proximo Informações do próximo ponto (tipo, descrição, índice)
 * @param horaAtual Hora atual para exibição no relógio
 * @param onClick Ação ao clicar no card
 * @param habilitado Se o card está ativo para clique
 * @param modifier Modificador de layout
 *
 * @author Thiago
 * @since 12.0.0
 */
@Composable
fun ProximoPontoCard(
    proximo: ProximoPonto,
    horaAtual: LocalTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val isEntrada = proximo.isEntrada
    val corPrincipal = if (isEntrada) EntradaColor else SaidaColor
    val theme = LocalAppThemeController.current
    val premium = LocalPremiumTokens.current
    val shape = RoundedCornerShape(if (theme.isPremium) 28.dp else 16.dp)
    val icone = if (isEntrada) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout
    val formatadorHora = DateTimeFormatter.ofPattern("HH:mm:ss")

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && habilitado) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val gradientBrush = when {
        !habilitado -> Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        )

        theme.isPremium -> Brush.linearGradient(
            colors = listOf(
                corPrincipal.copy(alpha = 0.98f),
                corPrincipal.copy(alpha = 0.72f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
            )
        )

        theme.isDarkula -> Brush.linearGradient(
            colors = listOf(
                corPrincipal.copy(alpha = 0.78f),
                MaterialTheme.colorScheme.surfaceVariant
            )
        )

        else -> Brush.linearGradient(
            colors = listOf(
                corPrincipal,
                corPrincipal.copy(alpha = 0.82f)
            )
        )
    }

    val borderColor = when {
        !habilitado -> MaterialTheme.colorScheme.outlineVariant
        theme.isPremium -> corPrincipal.copy(alpha = 0.62f)
        theme.isDarkula -> MaterialTheme.colorScheme.outline
        else -> Color.Transparent
    }

    val shadowElevation = when {
        !habilitado -> 0.dp
        theme.isPremium -> 18.dp
        theme.isDarkula -> 4.dp
        else -> 4.dp
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = if (theme.isPremium) premium.primaryGlow.copy(alpha = 0.25f) else Color.Transparent,
                spotColor = if (theme.isPremium) corPrincipal.copy(alpha = 0.35f) else Color.Transparent
            )
            .clip(shape)
            .border(
                width = if (theme.isPremium || theme.isDarkula || !habilitado) 1.dp else 0.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = habilitado,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(if (theme.isPremium) 18.dp else 14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Título/Tipo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = icone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Registrar ${proximo.descricao}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Relógio
                Text(
                    text = horaAtual.format(formatadorHora),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    letterSpacing = 1.sp
                )

                if (!habilitado) {
                    Text(
                        text = "Indisponível",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
