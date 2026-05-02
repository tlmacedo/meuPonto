// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/components/AusenciaFilterChips.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.R
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.list.OrdemData

@Composable
fun AusenciaFilterChips(
    tiposSelecionados: Set<TipoAusencia>,
    anoSelecionado: Int?,
    anosDisponiveis: List<Int>,
    ordemData: OrdemData,
    onToggleTipo: (TipoAusencia) -> Unit,
    onAnoChange: (Int?) -> Unit,
    onToggleOrdem: () -> Unit,
    onLimparFiltros: () -> Unit,
    modifier: Modifier = Modifier
) {
    val temFiltrosAtivos = tiposSelecionados.isNotEmpty() || anoSelecionado != null

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = onToggleOrdem,
                label = {
                    Text(
                        text = when (ordemData) {
                            OrdemData.CRESCENTE -> stringResource(R.string.historico_ordenacao_mais_recentes)
                            OrdemData.DECRESCENTE -> stringResource(R.string.historico_ordenacao_mais_antigas)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = when (ordemData) {
                            OrdemData.CRESCENTE -> Icons.Default.ArrowUpward
                            OrdemData.DECRESCENTE -> Icons.Default.ArrowDownward
                        },
                        contentDescription = stringResource(R.string.historico_filtrar)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )

            Text(
                text = "|",
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            TipoAusencia.todos.forEach { tipo ->
                FilterChip(
                    selected = tipo in tiposSelecionados,
                    onClick = { onToggleTipo(tipo) },
                    label = {
                        Text(
                            text = "${tipo.emoji} ${tipo.descricao}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (tipo in tiposSelecionados) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = tipo in tiposSelecionados,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (temFiltrosAtivos) {
                FilterChip(
                    selected = false,
                    onClick = onLimparFiltros,
                    label = {
                        Text(
                            text = stringResource(R.string.ausencia_filtros_limpar),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.ausencia_filtros_limpar),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f),
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )

                Text(
                    text = "|",
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            anosDisponiveis.forEach { ano ->
                FilterChip(
                    selected = anoSelecionado == ano,
                    onClick = {
                        onAnoChange(if (anoSelecionado == ano) null else ano)
                    },
                    label = {
                        Text(
                            text = ano.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (anoSelecionado == ano) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = anoSelecionado == ano,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = MaterialTheme.colorScheme.tertiary
                    )
                )
            }
        }

        if (tiposSelecionados.isNotEmpty()) {
            Text(
                text = "${tiposSelecionados.size} tipo(s) selecionado(s)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}