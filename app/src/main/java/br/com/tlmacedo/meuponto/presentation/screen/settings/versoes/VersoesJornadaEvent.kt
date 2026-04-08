package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes
/**
 * Eventos emitidos pelo VersoesJornadaViewModel para a UI.
 */
sealed interface VersoesJornadaEvent {
    data class MostrarMensagem(val mensagem: String) : VersoesJornadaEvent
    data class NavegarParaEditar(val versaoId: Long) : VersoesJornadaEvent
    data class NavegarParaNova(val empregoId: Long) : VersoesJornadaEvent
    data class NavegarParaComparar(val empregoId: Long, val v1: Long, val v2: Long) : VersoesJornadaEvent
    data object Voltar : VersoesJornadaEvent
}
