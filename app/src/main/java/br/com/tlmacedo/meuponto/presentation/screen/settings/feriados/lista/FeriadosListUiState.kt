// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/lista/FeriadosListUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.lista

import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado

/**
 * Estado da UI da tela de listagem de feriados.
 *
 * @author Thiago
 * @since 3.0.0
 */
data class FeriadosListUiState(
    val isLoading: Boolean = true,
    val feriados: List<Feriado> = emptyList(),
    val feriadosFiltrados: List<Feriado> = emptyList(),
    val filtroTipo: TipoFeriado? = null,
    val filtroAno: Int? = null,
    val anosDisponiveis: List<Int> = emptyList(),
    val searchQuery: String = "",
    val showImportDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val feriadoParaExcluir: Feriado? = null,
    val importacaoEmAndamento: Boolean = false,
    val mensagemSucesso: String? = null,
    val mensagemErro: String? = null
) {
    val feriadosAgrupados: Map<TipoFeriado, List<Feriado>>
        get() = feriadosFiltrados.groupBy { it.tipo }

    val totalFeriados: Int
        get() = feriados.size

    val totalFeriadosFiltrados: Int
        get() = feriadosFiltrados.size

    val temFiltrosAtivos: Boolean
        get() = filtroTipo != null || filtroAno != null || searchQuery.isNotBlank()
}

/**
 * Eventos da tela de listagem de feriados.
 */
sealed class FeriadosListEvent {
    data class OnSearchQueryChange(val query: String) : FeriadosListEvent()
    data class OnFiltroTipoChange(val tipo: TipoFeriado?) : FeriadosListEvent()
    data class OnFiltroAnoChange(val ano: Int?) : FeriadosListEvent()
    data object OnLimparFiltros : FeriadosListEvent()
    data object OnShowImportDialog : FeriadosListEvent()
    data object OnDismissImportDialog : FeriadosListEvent()
    data object OnImportarFeriados : FeriadosListEvent()
    data class OnShowDeleteDialog(val feriado: Feriado) : FeriadosListEvent()
    data object OnDismissDeleteDialog : FeriadosListEvent()
    data object OnConfirmarExclusao : FeriadosListEvent()
    data class OnToggleAtivo(val feriado: Feriado) : FeriadosListEvent()
    data object OnDismissMessage : FeriadosListEvent()
}
