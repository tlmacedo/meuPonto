package br.com.tlmacedo.meuponto.presentation.screen.settings.jornada

/**
 * Estado da UI da tela de configurações de jornada.
 */
data class JornadaUiState(
    val isLoading: Boolean = false,
    val cargaHorariaMinutos: Int = 480,
    val intervaloMinimoMinutos: Int = 60,
    val toleranciaGeralMinutos: Int = 10,
    val mensagemErro: String? = null
)

/**
 * Ações disparadas pela UI.
 */
sealed interface JornadaAction {
    data class AlterarCargaHoraria(val minutos: Int) : JornadaAction
    data class AlterarIntervaloMinimo(val minutos: Int) : JornadaAction
    data class AlterarToleranciaGeral(val minutos: Int) : JornadaAction
    object LimparErro : JornadaAction
}

/**
 * Eventos disparados pela ViewModel para a UI.
 */
sealed interface JornadaEvent {
    data class MostrarMensagem(val mensagem: String) : JornadaEvent
}
