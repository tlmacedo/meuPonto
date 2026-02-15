// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/DateNavigator.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * Componente para navegação entre datas.
 *
 * Exibe a data atual com setas para navegar entre dias
 * e um botão para voltar rapidamente para hoje.
 *
 * @param dataFormatada Data formatada para exibição principal
 * @param dataFormatadaCurta Data em formato curto (dd/MM/yyyy)
 * @param isHoje Indica se a data selecionada é hoje
 * @param podeNavegarAnterior Se pode navegar para dia anterior
 * @param podeNavegarProximo Se pode navegar para próximo dia
 * @param onDiaAnterior Callback para navegar ao dia anterior
 * @param onProximoDia Callback para navegar ao próximo dia
 * @param onIrParaHoje Callback para ir para hoje
 * @param onSelecionarData Callback para abrir seletor de data (opcional)
 * @param modifier Modificador opcional
 *
 * @author Thiago
 * @since 2.0.0
 */
@Composable
fun DateNavigator(
    dataFormatada: String,
    dataFormatadaCurta: String,
    isHoje: Boolean,
    podeNavegarAnterior: Boolean,
    podeNavegarProximo: Boolean,
    onDiaAnterior: () -> Unit,
    onProximoDia: () -> Unit,
    onIrParaHoje: () -> Unit,
    onSelecionarData: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Botão dia anterior
            IconButton(
                onClick = onDiaAnterior,
                enabled = podeNavegarAnterior
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Dia anterior",
                    tint = if (podeNavegarAnterior) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(32.dp)
                )
            }

            // Data central com animação
            AnimatedContent(
                targetState = dataFormatada,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                },
                label = "date_animation",
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onSelecionarData != null) {
                            Modifier.clickable { onSelecionarData() }
                        } else {
                            Modifier
                        }
                    )
            ) { data ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = data,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    if (!isHoje) {
                        Text(
                            text = dataFormatadaCurta,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Botão Hoje / Próximo dia
            if (!isHoje) {
                // Botão para voltar para hoje
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onIrParaHoje() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Ir para hoje",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                // Botão próximo dia
                IconButton(
                    onClick = onProximoDia,
                    enabled = podeNavegarProximo
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Próximo dia",
                        tint = if (podeNavegarProximo) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
