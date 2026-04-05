package br.com.tlmacedo.meuponto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.presentation.components.swipe.SwipeablePontoRow
import br.com.tlmacedo.meuponto.presentation.theme.EntradaBg
import br.com.tlmacedo.meuponto.presentation.theme.EntradaColor
import br.com.tlmacedo.meuponto.presentation.theme.SaidaBg
import br.com.tlmacedo.meuponto.presentation.theme.SaidaColor
import java.time.format.DateTimeFormatter

/**
 * Item individual de ponto para exibição em listas.
 *
 * @param ponto O objeto Ponto a ser exibido.
 * @param tipoPonto O tipo de ponto (Entrada ou Saída).
 * @param onEditar Callback para ação de editar.
 * @param onExcluir Callback para ação de excluir.
 * @param onVerLocalizacao Callback para ver a localização do ponto.
 * @param onVerFoto Callback para ver a foto associada ao ponto.
 * @param modifier Modificador opcional.
 */
@Composable
fun PontoItem(
    ponto: Ponto,
    tipoPonto: TipoPonto,
    onEditar: (Ponto) -> Unit,
    onExcluir: (Ponto) -> Unit,
    onVerLocalizacao: (Ponto) -> Unit,
    onVerFoto: (Ponto) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatadorHora = DateTimeFormatter.ofPattern("HH:mm")
    val isEntrada = tipoPonto == TipoPonto.ENTRADA
    val corPrimaria = if (isEntrada) EntradaColor else SaidaColor
    val corFundo = if (isEntrada) EntradaBg else SaidaBg
    val icone = if (isEntrada) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        SwipeablePontoRow(
            ponto = ponto,
            onEditar = onEditar,
            onExcluir = onExcluir,
            onVerFoto = onVerFoto,
            onVerLocalizacao = onVerLocalizacao
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone e Categoria
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(corFundo)
                ) {
                    Icon(
                        imageVector = icone,
                        contentDescription = tipoPonto.descricao,
                        tint = corPrimaria,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                // Informações do Ponto
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tipoPonto.descricao,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = corPrimaria
                    )

                    if (!ponto.observacao.isNullOrBlank()) {
                        Text(
                            text = ponto.observacao,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Horários (Real e Considerado)
                Column(horizontalAlignment = Alignment.End) {
                    val horaReal = ponto.dataHora.toLocalTime().format(formatadorHora)
                    val horaConsiderada = ponto.horaConsiderada.format(formatadorHora)

                    if (horaConsiderada != horaReal) {
                        Text(
                            text = horaReal,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = horaConsiderada,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = corPrimaria
                        )
                    } else {
                        Text(
                            text = horaReal,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
