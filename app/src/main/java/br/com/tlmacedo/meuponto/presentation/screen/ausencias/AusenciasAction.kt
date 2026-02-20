// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciasAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import java.time.LocalDate
import java.time.YearMonth

/**
 * Ações da tela de listagem de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
sealed interface AusenciasAction {
    // Navegação por mês
    data object MesAnterior : AusenciasAction
    data object ProximoMes : AusenciasAction
    data class SelecionarMes(val mes: YearMonth) : AusenciasAction

    // Filtro
    data class FiltrarPorTipo(val tipo: TipoAusencia?) : AusenciasAction

    // CRUD
    data object NovaAusencia : AusenciasAction
    data class EditarAusencia(val ausencia: Ausencia) : AusenciasAction
    data class SolicitarExclusao(val ausencia: Ausencia) : AusenciasAction
    data object ConfirmarExclusao : AusenciasAction
    data object CancelarExclusao : AusenciasAction

    // Geral
    data object LimparErro : AusenciasAction
    data object Voltar : AusenciasAction
}

/**
 * Ações do formulário de ausência.
 */
sealed interface AusenciaFormAction {
    // Campos
    data class SelecionarTipo(val tipo: TipoAusencia) : AusenciaFormAction
    data class SelecionarDataInicio(val data: LocalDate) : AusenciaFormAction
    data class SelecionarDataFim(val data: LocalDate) : AusenciaFormAction
    data class AtualizarDescricao(val descricao: String) : AusenciaFormAction
    data class AtualizarObservacao(val observacao: String) : AusenciaFormAction

    // Dialogs
    data object AbrirDatePickerInicio : AusenciaFormAction
    data object FecharDatePickerInicio : AusenciaFormAction
    data object AbrirDatePickerFim : AusenciaFormAction
    data object FecharDatePickerFim : AusenciaFormAction
    data object AbrirTipoSelector : AusenciaFormAction
    data object FecharTipoSelector : AusenciaFormAction

    // Ações principais
    data object Salvar : AusenciaFormAction
    data object Cancelar : AusenciaFormAction
    data object LimparErro : AusenciaFormAction
}
