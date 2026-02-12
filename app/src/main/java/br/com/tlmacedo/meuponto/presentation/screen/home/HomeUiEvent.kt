package br.com.tlmacedo.meuponto.presentation.screen.home

sealed interface HomeUiEvent {
    data class MostrarMensagem(val mensagem: String) : HomeUiEvent
    data class MostrarErro(val mensagem: String) : HomeUiEvent
    data object NavegarParaHistorico : HomeUiEvent
    data object NavegarParaConfiguracoes : HomeUiEvent
    data class NavegarParaEdicao(val pontoId: Long) : HomeUiEvent
}
