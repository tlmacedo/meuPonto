// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciasAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import java.time.YearMonth

/**
 * Ações da tela de listagem de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.6.0 - Filtros múltiplos
 * @updated 12.2.0 - Adicionada visualização de calendário
 */
sealed interface AusenciasAction {
    // Filtros
    data class ToggleTipo(val tipo: TipoAusencia) : AusenciasAction
    data class FiltroAnoChange(val ano: Int?) : AusenciasAction
    data object ToggleOrdem : AusenciasAction
    data object LimparFiltros : AusenciasAction
    data object ToggleVisualizacao : AusenciasAction
    data class MesChange(val mes: YearMonth) : AusenciasAction

    // CRUD
    data object NovaAusencia : AusenciasAction
    data class EditarAusencia(val ausencia: Ausencia) : AusenciasAction
    data class SolicitarExclusao(val ausencia: Ausencia) : AusenciasAction
    data object ConfirmarExclusao : AusenciasAction
    data object CancelarExclusao : AusenciasAction
    data class ToggleAtivo(val ausencia: Ausencia) : AusenciasAction

    // Geral
    data object LimparErro : AusenciasAction
    data object Voltar : AusenciasAction
}
