package br.com.tlmacedo.meuponto.presentation.screen.settings.comprovantes

import br.com.tlmacedo.meuponto.domain.model.FotoComprovante
import java.time.LocalDate

data class ComprovantesUiState(
    val isLoading: Boolean = false,
    val items: List<FotoComprovante> = emptyList(),
    val dataInicio: LocalDate = LocalDate.now().minusMonths(1).withDayOfMonth(1),
    val dataFim: LocalDate = LocalDate.now(),
    val selectedItem: FotoComprovante? = null,
    val storageUsageBytes: Long = 0,
    val error: String? = null
) {
    val totalCount: Int get() = items.size
    val totalSizeMb: Double get() = storageUsageBytes / (1024.0 * 1024.0)
}

sealed interface ComprovantesAction {
    data class AlterarPeriodo(val inicio: LocalDate, val fim: LocalDate) : ComprovantesAction
    data class SelecionarComprovante(val comprovante: FotoComprovante?) : ComprovantesAction
    data class ExcluirComprovante(val id: Long) : ComprovantesAction
    object LimparCache : ComprovantesAction
    object Refresh : ComprovantesAction
}

sealed interface ComprovantesEvent {
    data class ShowError(val message: String) : ComprovantesEvent
    object ComprovanteExcluido : ComprovantesEvent
}
