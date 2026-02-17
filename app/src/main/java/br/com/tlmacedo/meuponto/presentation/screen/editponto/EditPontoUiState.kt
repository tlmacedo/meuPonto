// Arquivo: EditPontoUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.LocalDate
import java.time.LocalTime

/**
 * Estado da interface da tela de edição de ponto.
 *
 * O tipo do ponto (entrada/saída) é determinado pela posição na lista ordenada,
 * não podendo ser alterado pelo usuário. Apenas data, hora e observação são editáveis.
 *
 * @property pontoOriginal Ponto original sendo editado (null se novo)
 * @property indice Índice do ponto na lista ordenada (para determinar tipo visual)
 * @property data Data selecionada para o ponto
 * @property hora Hora selecionada para o ponto
 * @property observacao Observação opcional do usuário
 * @property isLoading Indica se está carregando dados
 * @property isSaving Indica se está salvando alterações
 * @property errorMessage Mensagem de erro para exibição
 * @property isEditMode Se true, está editando; se false, está criando
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Tipo calculado por índice (não editável)
 */
data class EditPontoUiState(
    val pontoOriginal: Ponto? = null,
    val indice: Int = 0,
    val data: LocalDate = LocalDate.now(),
    val hora: LocalTime = LocalTime.now(),
    val observacao: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false
) {
    /**
     * Tipo calculado pelo índice (apenas para exibição visual).
     */
    val tipo: TipoPonto
        get() = TipoPonto.getTipoPorIndice(indice)

    /**
     * Verifica se há alterações pendentes em relação ao ponto original.
     */
    val hasChanges: Boolean
        get() = pontoOriginal?.let { original ->
            original.data != data ||
            original.hora != hora ||
            (original.observacao ?: "") != observacao
        } ?: true

    /**
     * Verifica se o formulário pode ser salvo.
     */
    val canSave: Boolean
        get() = !isSaving && !isLoading

    /**
     * Título da tela baseado no modo (edição ou criação).
     */
    val screenTitle: String
        get() = if (isEditMode) "Editar Ponto" else "Novo Ponto"
}
