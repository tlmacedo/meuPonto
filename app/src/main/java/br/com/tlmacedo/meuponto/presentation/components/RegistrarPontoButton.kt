// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/RegistrarPontoButton.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ProximoPonto
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Botão compacto para registrar ponto no dia atual.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 5.0.0 - Corrigido VerticalDivider com altura fixa
 */
@Composable
fun RegistrarPontoButton(
    proximoTipo: ProximoPonto,
    horaAtual: LocalTime,
    onRegistrarAgora: () -> Unit,
    onRegistrarManual: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEntrada = proximoTipo.isEntrada
    val corPrincipal = if (isEntrada) EntradaColor else SaidaColor
    val icone = if (isEntrada) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout
    val texto = "Registrar ${proximoTipo.descricao}"
    val formatadorHora = DateTimeFormatter.ofPattern("HH:mm:ss")

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = corPrincipal
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(14.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp) // Altura fixa para o botão
        ) {
            // Área principal clicável - registro automático
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onRegistrarAgora
                    )
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = texto,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = horaAtual.format(formatadorHora),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }

            // Divisor vertical com altura fixa
            VerticalDivider(
                modifier = Modifier
                    .height(32.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            // Botão para registro manual
            Box(
                modifier = Modifier
                    .clickable(onClick = onRegistrarManual)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Informar outro horário",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Botão compacto para registrar ponto manual em dias anteriores.
 *
 * @author Thiago
 * @since 2.6.0
 * @updated 5.0.0 - Layout compactado
 */
@Composable
fun RegistrarPontoManualButton(
    proximoTipo: ProximoPonto,
    dataFormatada: String,
    onRegistrarManual: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEntrada = proximoTipo.isEntrada
    val corPrincipal = if (isEntrada) EntradaColor else SaidaColor
    val icone = if (isEntrada) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = corPrincipal
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(14.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onRegistrarManual
                )
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Registrar ${proximoTipo.descricao}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
//            Spacer(modifier = Modifier.width(4.dp))
//            Text(
//                text = "Informar horário",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
//            )
        }
    }
}
