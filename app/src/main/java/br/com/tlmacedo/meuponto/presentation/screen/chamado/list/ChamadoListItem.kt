// path: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/chamado/list/ChamadoListItem.kt
package br.com.tlmacedo.meuponto.presentation.screen.chamado.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.CategoriaChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.PrioridadeChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.presentation.theme.MeuPontoTheme
import java.time.LocalDateTime

@Composable
fun ChamadoListItem(
    chamado: Chamado,
    onChamadoClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onChamadoClick(chamado.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = chamado.identificador,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chamado.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = chamado.descricao,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChamadoStatusChip(status = chamado.status)
                ChamadoPrioridadeChip(prioridade = chamado.prioridade)
            }
        }
    }
}

@Composable
fun ChamadoStatusChip(status: StatusChamado) {
    AssistChip(
        onClick = { /* Não clicável */ },
        label = { Text(status.label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (status) {
                StatusChamado.ABERTO -> MaterialTheme.colorScheme.primaryContainer
                StatusChamado.EM_ANALISE -> MaterialTheme.colorScheme.secondaryContainer
                StatusChamado.EM_PROGRESSO -> MaterialTheme.colorScheme.tertiaryContainer
                StatusChamado.AGUARDANDO_USUARIO -> MaterialTheme.colorScheme.errorContainer
                StatusChamado.RESOLVIDO, StatusChamado.FECHADO -> MaterialTheme.colorScheme.surfaceVariant
                StatusChamado.CANCELADO -> MaterialTheme.colorScheme.outlineVariant
            },
            labelColor = when (status) {
                StatusChamado.ABERTO -> MaterialTheme.colorScheme.onPrimaryContainer
                StatusChamado.EM_ANALISE -> MaterialTheme.colorScheme.onSecondaryContainer
                StatusChamado.EM_PROGRESSO -> MaterialTheme.colorScheme.onTertiaryContainer
                StatusChamado.AGUARDANDO_USUARIO -> MaterialTheme.colorScheme.onErrorContainer
                StatusChamado.RESOLVIDO, StatusChamado.FECHADO -> MaterialTheme.colorScheme.onSurfaceVariant
                // CORREÇÃO AQUI: onOutlineVariant não existe, usando onSurfaceVariant
                StatusChamado.CANCELADO -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    )
}

@Composable
fun ChamadoPrioridadeChip(prioridade: PrioridadeChamado) {
    AssistChip(
        onClick = { /* Não clicável */ },
        label = { Text(prioridade.label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (prioridade) {
                PrioridadeChamado.BAIXA -> MaterialTheme.colorScheme.surfaceVariant
                PrioridadeChamado.MEDIA -> MaterialTheme.colorScheme.secondaryContainer
                PrioridadeChamado.ALTA -> MaterialTheme.colorScheme.tertiaryContainer
                PrioridadeChamado.CRITICA -> MaterialTheme.colorScheme.errorContainer
            },
            labelColor = when (prioridade) {
                PrioridadeChamado.BAIXA -> MaterialTheme.colorScheme.onSurfaceVariant
                PrioridadeChamado.MEDIA -> MaterialTheme.colorScheme.onSecondaryContainer
                PrioridadeChamado.ALTA -> MaterialTheme.colorScheme.onTertiaryContainer
                PrioridadeChamado.CRITICA -> MaterialTheme.colorScheme.onErrorContainer
            }
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ChamadoListItemPreview() {
    MeuPontoTheme {
        ChamadoListItem(
            chamado = Chamado(
                id = 1,
                identificador = "MP-2026-00001",
                titulo = "Problema ao registrar ponto",
                descricao = "O aplicativo fecha sozinho ao tentar registrar o ponto de entrada.",
                categoria = CategoriaChamado.BUG,
                prioridade = PrioridadeChamado.ALTA,
                status = StatusChamado.ABERTO,
                empregoId = 1,
                usuarioEmail = "teste@example.com",
                usuarioNome = "Usuário Teste",
                resposta = null,
                anexos = null,
                criadoEm = LocalDateTime.now().minusDays(2),
                atualizadoEm = LocalDateTime.now(),
                resolvidoEm = null,
                avaliacaoNota = null,
                avaliacaoComentario = null,
                avaliadoEm = null
            ),
            onChamadoClick = {}
        )
    }
}