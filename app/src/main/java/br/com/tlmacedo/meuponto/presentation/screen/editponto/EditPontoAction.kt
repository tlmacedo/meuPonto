// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import br.com.tlmacedo.meuponto.domain.model.Emprego
import java.time.LocalDate
import java.time.LocalTime

/**
 * Ações que a UI pode enviar para o [EditPontoViewModel].
 *
 * Modeladas como sealed class para garantir tipo seguro e
 * rastreabilidade completa de todos os eventos da tela.
 *
 * ## Sobre tipoPonto:
 * Não existe ação de AlterarTipoPonto. O tipo é calculado dinamicamente
 * pela posição do ponto no dia (ímpar=entrada, par=saída) e não é editável.
 *
 * @author Thiago
 * @since 12.0.0
 */
sealed class EditPontoAction {

    // ========================================================================
    // ALTERAÇÕES DE CAMPOS EDITÁVEIS
    // ========================================================================

    /** Altera a data do ponto */
    data class AlterarData(val data: LocalDate) : EditPontoAction()

    /** Altera a hora do ponto */
    data class AlterarHora(val hora: LocalTime) : EditPontoAction()

    /** Altera o emprego selecionado */
    data class AlterarEmprego(val emprego: Emprego) : EditPontoAction()

    /** Altera o texto de observação */
    data class AlterarObservacao(val observacao: String) : EditPontoAction()

    /** Altera o NSR */
    data class AlterarNsr(val nsr: String) : EditPontoAction()

    /** Define o caminho da foto após captura bem-sucedida */
    data class AlterarFoto(val relativePath: String?) : EditPontoAction()

    // ========================================================================
    // CONTROLE DE VISIBILIDADE DE SELETORES
    // ========================================================================

    /** Abre o seletor de data */
    object AbrirDatePicker : EditPontoAction()

    /** Fecha o seletor de data */
    object FecharDatePicker : EditPontoAction()

    /** Abre o seletor de hora */
    object AbrirTimePicker : EditPontoAction()

    /** Fecha o seletor de hora */
    object FecharTimePicker : EditPontoAction()

    /** Abre o visualizador de foto */
    object AbrirVisualizadorFoto : EditPontoAction()

    // ========================================================================
    // AÇÕES PRINCIPAIS
    // ========================================================================

    /** Marca a foto atual para remoção */
    object RemoverFoto : EditPontoAction()

    /** Confirma o salvamento do ponto */
    object Salvar : EditPontoAction()

    /** Confirma a exclusão do ponto */
    object Excluir : EditPontoAction()

    /** Limpa a mensagem de erro atual */
    object LimparErro : EditPontoAction()
}