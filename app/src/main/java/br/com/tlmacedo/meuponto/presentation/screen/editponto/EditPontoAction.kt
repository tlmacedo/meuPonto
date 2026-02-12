// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.LocalDate
import java.time.LocalTime

/**
 * Sealed class que representa as ações possíveis na tela de edição de ponto.
 *
 * Define todas as intenções do usuário que podem modificar
 * o estado da tela ou disparar operações.
 *
 * @author Thiago
 * @since 1.0.0
 */
sealed class EditPontoAction {

    /**
     * Ação para alterar a data do ponto.
     *
     * @property data Nova data selecionada
     */
    data class AlterarData(val data: LocalDate) : EditPontoAction()

    /**
     * Ação para alterar a hora do ponto.
     *
     * @property hora Nova hora selecionada
     */
    data class AlterarHora(val hora: LocalTime) : EditPontoAction()

    /**
     * Ação para alterar o tipo do ponto.
     *
     * @property tipo Novo tipo selecionado
     */
    data class AlterarTipo(val tipo: TipoPonto) : EditPontoAction()

    /**
     * Ação para alterar a observação do ponto.
     *
     * @property observacao Nova observação
     */
    data class AlterarObservacao(val observacao: String) : EditPontoAction()

    /**
     * Ação para salvar o ponto.
     */
    data object Salvar : EditPontoAction()

    /**
     * Ação para cancelar a edição.
     */
    data object Cancelar : EditPontoAction()

    /**
     * Ação para limpar mensagem de erro.
     */
    data object LimparErro : EditPontoAction()
}
