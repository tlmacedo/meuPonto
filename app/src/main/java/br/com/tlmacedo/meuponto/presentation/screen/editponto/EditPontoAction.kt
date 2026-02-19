// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import java.time.LocalDate
import java.time.LocalTime

/**
 * Ações possíveis na tela de edição de ponto.
 *
 * @author Thiago
 * @since 3.5.0
 */
sealed interface EditPontoAction {

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE CAMPOS
    // ══════════════════════════════════════════════════════════════════════

    /** Atualiza a data do ponto */
    data class AtualizarData(val data: LocalDate) : EditPontoAction

    /** Atualiza a hora do ponto */
    data class AtualizarHora(val hora: LocalTime) : EditPontoAction

    /** Atualiza o NSR do ponto */
    data class AtualizarNsr(val nsr: String) : EditPontoAction

    /** Atualiza a localização do ponto */
    data class AtualizarLocalizacao(
        val latitude: Double,
        val longitude: Double,
        val endereco: String? = null
    ) : EditPontoAction

    /** Atualiza a observação do ponto */
    data class AtualizarObservacao(val observacao: String) : EditPontoAction

    /** Atualiza o motivo da edição */
    data class AtualizarMotivo(val motivo: String) : EditPontoAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE DIALOGS
    // ══════════════════════════════════════════════════════════════════════

    /** Abre o TimePicker */
    data object AbrirTimePicker : EditPontoAction

    /** Fecha o TimePicker */
    data object FecharTimePicker : EditPontoAction

    /** Abre o DatePicker */
    data object AbrirDatePicker : EditPontoAction

    /** Fecha o DatePicker */
    data object FecharDatePicker : EditPontoAction

    /** Abre o seletor de localização */
    data object AbrirLocationPicker : EditPontoAction

    /** Fecha o seletor de localização */
    data object FecharLocationPicker : EditPontoAction

    /** Solicita captura automática de localização */
    data object CapturarLocalizacao : EditPontoAction

    /** Limpa a localização */
    data object LimparLocalizacao : EditPontoAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES PRINCIPAIS
    // ══════════════════════════════════════════════════════════════════════

    /** Salva as alterações do ponto */
    data object Salvar : EditPontoAction

    /** Solicita exclusão do ponto */
    data object SolicitarExclusao : EditPontoAction

    /** Confirma exclusão do ponto */
    data object ConfirmarExclusao : EditPontoAction

    /** Cancela exclusão do ponto */
    data object CancelarExclusao : EditPontoAction

    /** Cancela a edição e volta */
    data object Cancelar : EditPontoAction

    /** Limpa erro */
    data object LimparErro : EditPontoAction
}
