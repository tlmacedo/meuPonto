// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/lixeira/LixeiraUiEvent.kt
package br.com.tlmacedo.meuponto.presentation.screen.lixeira

/**
 * Eventos de UI da tela de Lixeira (one-shot events).
 *
 * @author Thiago
 * @since 11.0.0
 */
sealed interface LixeiraUiEvent {
    data object Voltar : LixeiraUiEvent

    data class ItemRestaurado(val dataFormatada: String) : LixeiraUiEvent
    data class ItensRestaurados(val quantidade: Int) : LixeiraUiEvent

    data class ItemExcluido(val dataFormatada: String) : LixeiraUiEvent
    data class ItensExcluidos(val quantidade: Int) : LixeiraUiEvent

    data class LixeiraEsvaziada(val quantidade: Int) : LixeiraUiEvent

    data class MostrarMensagem(val mensagem: String) : LixeiraUiEvent

    data class Erro(val mensagem: String) : LixeiraUiEvent
}
