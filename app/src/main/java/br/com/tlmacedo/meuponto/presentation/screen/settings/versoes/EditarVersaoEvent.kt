package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

/**
 * Eventos emitidos pelo EditarVersaoViewModel para a UI.
 */
sealed interface EditarVersaoEvent {
    data class MostrarMensagem(val mensagem: String) : EditarVersaoEvent
    data object SalvoComSucesso : EditarVersaoEvent
    data object Voltar : EditarVersaoEvent
    data class NavegarParaHorarios(val versaoId: Long) : EditarVersaoEvent
}
