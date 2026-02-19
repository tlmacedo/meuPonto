// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/components/FeriadoFilterChips.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado

/**
 * Chips de filtro para tipos de feriado.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Composable
fun FeriadoFilterChips(
    tipoSelecionado: TipoFeriado?,
    anoSelecionado: Int?,
    anosDisponiveis: List<Int>,
    onTipoChange: (TipoFeriado?) -> Unit,
    onAnoChange: (Int?) -> Unit,
    onLimparFiltros: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val temFiltrosAtivos = tipoSelecionado != null || anoSelecionado != null

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip para limpar filtros
        if (temFiltrosAtivos) {
            FilterChip(
                selected = false,
                onClick = onLimparFiltros,
                label = { Text("Limpar") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Limpar filtros",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                )
            )
        }

        // Chips de tipo
        TipoFeriado.entries.forEach { tipo ->
            FilterChip(
                selected = tipoSelecionado == tipo,
                onClick = {
                    onTipoChange(if (tipoSelecionado == tipo) null else tipo)
                },
                label = { Text("${tipo.emoji} ${tipo.descricao}") }
            )
        }

        // Separador visual
        if (anosDisponiveis.isNotEmpty()) {
            Text(
                text = "|",
                modifier = Modifier.padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Chips de ano
        anosDisponiveis.forEach { ano ->
            FilterChip(
                selected = anoSelecionado == ano,
                onClick = {
                    onAnoChange(if (anoSelecionado == ano) null else ano)
                },
                label = { Text(ano.toString()) }
            )
        }
    }
}
