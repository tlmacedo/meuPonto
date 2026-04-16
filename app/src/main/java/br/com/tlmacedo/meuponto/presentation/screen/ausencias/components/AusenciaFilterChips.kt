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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import br.com.tlmacedo.meuponto.R
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.presentation.screen.ausencias.OrdemData

/**
 * Chips de filtro para tipos de ausência com suporte a múltipla seleção.
 *
 * @author Thiago
 * @since 5.6.0
 */
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Linha 1: Ordenação e tipos de ausência
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip de ordenação
            AssistChip(
                onClick = onToggleOrdem,
                label = {
                    Text(
                        text = when (ordemData) {
                            OrdemData.CRESCENTE -> stringResource(R.string.historico_ordenacao_mais_recentes)
                            OrdemData.DECRESCENTE -> stringResource(R.string.historico_ordenacao_mais_antigas)
                        },
                        style = MaterialTheme.typography.labelMedium
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

            // Separador visual
            Text(
                text = "|",
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Chips de tipo (múltipla seleção)
            TipoAusencia.entries.forEach { tipo ->
                FilterChip(
                    selected = tipo in tiposSelecionados,
                    onClick = { onToggleTipo(tipo) },
                    label = { Text("${tipo.emoji} ${tipo.descricao}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Linha 2: Anos e limpar filtros
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip para limpar filtros
            if (temFiltrosAtivos) {
                FilterChip(
                    selected = false,
                    onClick = onLimparFiltros,
                    label = { Text(stringResource(R.string.ausencia_filtros_limpar)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.ausencia_filtros_limpar),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )

                // Separador visual
                Text(
                    text = "|",
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Chips de ano
            anosDisponiveis.forEach { ano ->
                FilterChip(
                    selected = anoSelecionado == ano,
                    onClick = {
                        onAnoChange(if (anoSelecionado == ano) null else ano)
                    },
                    label = { Text(ano.toString()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
        }

        // Indicador de filtros ativos
        if (tiposSelecionados.isNotEmpty()) {
            Text(
                text = "${tiposSelecionados.size} tipo(s) selecionado(s)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
