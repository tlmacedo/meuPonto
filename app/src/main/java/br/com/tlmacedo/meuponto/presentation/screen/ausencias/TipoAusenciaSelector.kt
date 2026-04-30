// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/TipoAusenciaSelector.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.R
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusenciaCor

/**
 * Bottom sheet para seleção do tipo de ausência com explicações detalhadas.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.3.0 - Adicionado explicações de impacto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoAusenciaSelector(
    tipoSelecionado: TipoAusencia,
    onTipoSelecionado: (TipoAusencia) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Título
            Text(
                text = stringResource(R.string.ausencia_tipo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Legenda
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendaItem(
                    cor = MaterialTheme.colorScheme.primaryContainer,
                    texto = stringResource(R.string.ausencia_legenda_abonado)
                )
                LegendaItem(
                    cor = MaterialTheme.colorScheme.tertiaryContainer,
                    texto = stringResource(R.string.ausencia_legenda_debito)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Lista de tipos
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TipoAusencia.todos) { tipo ->
                    TipoAusenciaItem(
                        tipo = tipo,
                        isSelected = tipo == tipoSelecionado,
                        onClick = { onTipoSelecionado(tipo) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LegendaItem(
    cor: androidx.compose.ui.graphics.Color,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(cor)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TipoAusenciaItem(
    tipo: TipoAusencia,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isSelected -> {
            if (tipo.corIndicativa == TipoAusenciaCor.VERDE) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        }

        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        tipo.corIndicativa == TipoAusenciaCor.VERDE -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val iconeDescricaoPrefix = stringResource(R.string.ausencia_icone_de)
    val estadoSelecionado = stringResource(R.string.estado_selecionado)
    val clickLabel = stringResource(R.string.ausencia_selecionar_tipo, tipo.descricao)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .semantics(mergeDescendants = true) {
                role = Role.RadioButton
            }
            .clickable(
                onClick = onClick,
                onClickLabel = clickLabel
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabeçalho: Emoji + Nome + Badge + Check
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Emoji grande
                    Text(
                        text = tipo.emoji,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics {
                            contentDescription = "$iconeDescricaoPrefix ${tipo.descricao}"
                        }
                    )

                    Column {
                        // Nome do tipo
                        Text(
                            text = tipo.descricao,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Badge de impacto
                        Text(
                            text = tipo.impactoResumido,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (tipo.zeraJornada) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            }
                        )
                    }
                }

                // Indicador de seleção
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = estadoSelecionado,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Explicação do impacto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = tipo.explicacaoImpacto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Exemplo de uso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = tipo.exemploUso,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Indicador de documento necessário
            if (tipo.requerDocumento) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📎",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = stringResource(R.string.ausencia_requer_documento_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Chip compacto para exibir o tipo de ausência selecionado.
 */
@Composable
fun TipoAusenciaChip(
    tipo: TipoAusencia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (tipo.zeraJornada) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }

    val contentColor = if (tipo.zeraJornada) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
            }
            .clickable(
                onClick = onClick,
                onClickLabel = stringResource(R.string.ausencia_alterar_tipo)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = tipo.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    Text(
                        text = tipo.descricao,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                    Text(
                        text = tipo.impactoResumido,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = stringResource(R.string.btn_alterar),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}
