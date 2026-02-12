// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoUiEvent.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

/**
 * Sealed class que representa eventos únicos da tela de edição de ponto.
 *
 * Eventos que devem ser consumidos uma única vez pela UI,
 * como navegação e mensagens de feedback.
 *
 * @author Thiago
 * @since 1.0.0
 */
sealed class EditPontoUiEvent {

    /**
     * Evento para exibir mensagem em Snackbar.
     *
     * @property message Mensagem a ser exibida
     */
    data class ShowSnackbar(val message: String) : EditPontoUiEvent()

    /**
     * Evento indicando que o ponto foi salvo com sucesso.
     */
    data object PontoSalvo : EditPontoUiEvent()

    /**
     * Evento para navegar de volta à tela anterior.
     */
    data object NavigateBack : EditPontoUiEvent()
}
