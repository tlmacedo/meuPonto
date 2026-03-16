// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/lixeira/LixeiraUiEvent.kt
package br.com.tlmacedo.meuponto.presentation.screen.lixeira

/**
 * Eventos emitidos pela tela de Lixeira.
 *
 * São eventos one-shot que devem ser consumidos uma única vez,
 * como navegação e mensagens de feedback.
 *
 * @author Thiago
 * @since 9.2.0
 * @updated 11.0.0 - Refatorado para soft delete
 */
sealed interface LixeiraUiEvent {

    // === Navegação ===
    data object Voltar : LixeiraUiEvent

    // === Feedback de sucesso ===
    data class ItemRestaurado(val dataFormatada: String) : LixeiraUiEvent
    data class ItensRestaurados(val quantidade: Int) : LixeiraUiEvent
    data class ItemExcluido(val dataFormatada: String) : LixeiraUiEvent
    data class ItensExcluidos(val quantidade: Int) : LixeiraUiEvent
    data object LixeiraEsvaziada : LixeiraUiEvent

    // === Mensagens genéricas ===
    data class MostrarMensagem(val mensagem: String) : LixeiraUiEvent
}
