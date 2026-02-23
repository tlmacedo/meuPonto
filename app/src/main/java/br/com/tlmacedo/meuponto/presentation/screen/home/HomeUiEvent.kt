// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeUiEvent.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

/**
 * Eventos únicos emitidos pela HomeScreen.
 *
 * @author Thiago
 * @since 2.0.0
 */
sealed interface HomeUiEvent {

    data class MostrarMensagem(val mensagem: String) : HomeUiEvent
    data class MostrarErro(val mensagem: String) : HomeUiEvent

    data object NavegarParaHistorico : HomeUiEvent
    data object NavegarParaConfiguracoes : HomeUiEvent

    /** Navega para a tela de edição de ponto */
    data class NavegarParaEditarPonto(val pontoId: Long) : HomeUiEvent

    data class EmpregoTrocado(val nomeEmprego: String) : HomeUiEvent
    data object NavegarParaNovoEmprego : HomeUiEvent
    data class NavegarParaEditarEmprego(val empregoId: Long) : HomeUiEvent

    data object NavegarParaHistoricoCiclos : HomeUiEvent

}
