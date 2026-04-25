package br.com.tlmacedo.meuponto.presentation.screen.chamado.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import java.time.LocalDateTime

@Composable
fun ChamadoListItem(
    chamado: Chamado,
    onItemClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onItemClick(chamado.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = getStatusColor(chamado.status))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = chamado.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Identificador: ${chamado.identificador}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Status: ${chamado.status}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Prioridade: ${chamado.prioridade}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Criado em: ${chamado.criadoEm.toLocalDate()} ${chamado.criadoEm.toLocalTime()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun getStatusColor(status: StatusChamado): Color {
    return when (status) {
        StatusChamado.ABERTO -> MaterialTheme.colorScheme.surfaceVariant
        StatusChamado.EM_PROGRESSO -> Color(0xFFFFF3E0) // Laranja claro
        StatusChamado.EM_ANALISE -> Color(0xFFE3F2FD) // Azul claro
        StatusChamado.RESOLVIDO -> Color(0xFFE8F5E9) // Verde claro
        StatusChamado.AGUARDANDO_USUARIO -> Color(0xFFFFFDE7) // Amarelo claro
        StatusChamado.FECHADO -> Color(0xFFF5F5F5) // Cinza claro
        StatusChamado.CANCELADO -> Color(0xFFFFEBEE) // Vermelho claro
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