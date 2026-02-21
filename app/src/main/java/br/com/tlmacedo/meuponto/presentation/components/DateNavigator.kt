// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/components/DateNavigator.kt
package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Componente compacto para navegação entre datas.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 5.0.0 - Layout compactado
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
    onSelecionarData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            // Botão dia anterior
            IconButton(
                onClick = onDiaAnterior,
                enabled = podeNavegarAnterior,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Dia anterior",
                    tint = if (podeNavegarAnterior) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            // Data central - clicável
            AnimatedContent(
                targetState = dataFormatada to dataFormatadaCurta,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                },
                label = "date_animation",
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelecionarData() }
            ) { (dataLonga, dataCurta) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dataLonga,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = dataCurta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Botão próximo dia
            IconButton(
                onClick = onProximoDia,
                enabled = podeNavegarProximo,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Próximo dia",
                    tint = if (podeNavegarProximo) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
