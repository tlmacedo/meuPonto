// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoUiEvent.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

/**
 * Eventos emitidos pela tela de edição de ponto.
 *
 * @author Thiago
 * @since 3.5.0
 */
sealed interface EditPontoUiEvent {

    /** Ponto salvo com sucesso */
    data class Salvo(val mensagem: String) : EditPontoUiEvent

    /** Ponto excluído com sucesso */
    data class Excluido(val mensagem: String) : EditPontoUiEvent

    /** Erro ao processar ação */
    data class Erro(val mensagem: String) : EditPontoUiEvent

    /** Navegar para tela anterior */
    data object Voltar : EditPontoUiEvent
}
