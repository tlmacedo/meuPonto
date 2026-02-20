// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/TipoAusenciaSelector.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia

/**
 * Bottom sheet para seleção do tipo de ausência.
 *
 * @author Thiago
 * @since 4.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoAusenciaSelector(
    tipoSelecionado: TipoAusencia,
    onTipoSelecionado: (TipoAusencia) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Tipo de Ausência",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(TipoAusencia.entries.toList()) { tipo ->
                    TipoAusenciaItem(
                        tipo = tipo,
                        selecionado = tipo == tipoSelecionado,
                        onClick = { onTipoSelecionado(tipo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TipoAusenciaItem(
    tipo: TipoAusencia,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selecionado) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tipo.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Nome e descrição
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tipo.descricao,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal
                )

                Text(
                    text = obterDescricaoTipo(tipo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Check se selecionado
            if (selecionado) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Retorna a descrição detalhada do tipo de ausência.
 */
private fun obterDescricaoTipo(tipo: TipoAusencia): String {
    return when (tipo) {
        TipoAusencia.FERIAS -> "Período de férias remuneradas"
        TipoAusencia.ATESTADO -> "Licença médica com atestado"
        TipoAusencia.DECLARACAO -> "Declaração de comparecimento"
        TipoAusencia.FALTA_JUSTIFICADA -> "Ausência com justificativa aceita"
        TipoAusencia.FOLGA -> "Day-off ou compensação de banco"
        TipoAusencia.FALTA_INJUSTIFICADA -> "Ausência sem justificativa"
    }
}

/**
 * Chip para exibição compacta do tipo de ausência.
 */
@Composable
fun TipoAusenciaChip(
    tipo: TipoAusencia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = tipo.emoji,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = tipo.descricao,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
