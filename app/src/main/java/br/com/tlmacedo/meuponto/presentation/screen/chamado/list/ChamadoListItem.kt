package br.com.tlmacedo.meuponto.presentation.screen.chamado.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.presentation.theme.Error
import br.com.tlmacedo.meuponto.presentation.theme.Info
import br.com.tlmacedo.meuponto.presentation.theme.Success
import br.com.tlmacedo.meuponto.presentation.theme.Warning
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ChamadoListItem(
    chamado: Chamado,
    onItemClick: (Long) -> Unit
) {
    val statusColor = getStatusColor(chamado.status)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onItemClick(chamado.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            // Barra lateral de status
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chamado.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatusChip(status = chamado.status, color = statusColor)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chamado.identificador,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = chamado.categoria.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Criado em: ${chamado.criadoEm.format(formatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: StatusChamado, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = status.label.uppercase(Locale.getDefault()),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 9.sp
        )
    }
}

@Composable
fun getStatusColor(status: StatusChamado): Color {
    return when (status) {
        StatusChamado.ABERTO -> Info
        StatusChamado.EM_PROGRESSO -> Warning
        StatusChamado.EM_ANALISE -> Color(0xFF673AB7) // Roxo (Especial)
        StatusChamado.RESOLVIDO -> Success
        StatusChamado.AGUARDANDO_USUARIO -> Color(0xFFFF9800) // Laranja
        StatusChamado.FECHADO -> MaterialTheme.colorScheme.outline
        StatusChamado.CANCELADO -> Error
    }
}

@Preview(showBackground = true)
@Composable
fun ChamadoListItemPreview() {
    val sampleChamado = Chamado(
        id = 1,
        identificador = "CH-2023-001",
        titulo = "Problema com registro de ponto",
        descricao = "Não consigo registrar meu ponto hoje.",
        categoria = CategoriaChamado.SUPORTE_TECNICO,
        status = StatusChamado.ABERTO,
        prioridade = PrioridadeChamado.ALTA,
        empregoId = 101,
        usuarioEmail = "thiago@example.com",
        usuarioNome = "Thiago Macedo",
        resposta = null,
        anexos = null,
        avaliacaoNota = null,
        avaliacaoComentario = null,
        avaliadoEm = null,
        resolvidoEm = null,
        criadoEm = LocalDateTime.now().minusDays(2),
        atualizadoEm = LocalDateTime.now().minusHours(5)
    )
    MaterialTheme {
        ChamadoListItem(chamado = sampleChamado) {}
    }
}
