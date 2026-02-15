// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeUiEvent.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

/**
 * Eventos únicos emitidos pela HomeScreen.
 *
 * Representa ações que devem ser executadas uma única vez,
 * como navegação ou exibição de mensagens.
 *
 * @author Thiago
 * @since 2.0.0
 */
sealed interface HomeUiEvent {

    /**
     * Exibe uma mensagem de sucesso ao usuário.
     *
     * @property mensagem Texto da mensagem a ser exibida
     */
    data class MostrarMensagem(val mensagem: String) : HomeUiEvent

    /**
     * Exibe uma mensagem de erro ao usuário.
     *
     * @property mensagem Texto do erro a ser exibido
     */
    data class MostrarErro(val mensagem: String) : HomeUiEvent

    /**
     * Navega para a tela de histórico de pontos.
     */
    data object NavegarParaHistorico : HomeUiEvent

    /**
     * Navega para a tela de configurações.
     */
    data object NavegarParaConfiguracoes : HomeUiEvent

    /**
     * Navega para a tela de edição de ponto.
     *
     * @property pontoId ID do ponto a ser editado
     */
    data class NavegarParaEdicao(val pontoId: Long) : HomeUiEvent

    /**
     * Emprego foi trocado com sucesso.
     *
     * @property nomeEmprego Nome do novo emprego ativo
     */
    data class EmpregoTrocado(val nomeEmprego: String) : HomeUiEvent
}
