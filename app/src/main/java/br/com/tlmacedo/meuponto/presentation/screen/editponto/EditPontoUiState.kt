// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.LocalDate
import java.time.LocalTime

/**
 * Estado da interface da tela de edição de ponto.
 *
 * Contém os dados necessários para renderizar e controlar
 * o formulário de edição de um registro de ponto.
 *
 * @property pontoOriginal Ponto original sendo editado (null se novo)
 * @property data Data selecionada para o ponto
 * @property hora Hora selecionada para o ponto
 * @property tipo Tipo do ponto (ENTRADA ou SAIDA)
 * @property observacao Observação opcional do usuário
 * @property isLoading Indica se está carregando dados
 * @property isSaving Indica se está salvando alterações
 * @property errorMessage Mensagem de erro para exibição
 * @property isEditMode Se true, está editando; se false, está criando
 *
 * @author Thiago
 * @since 1.0.0
 */
data class EditPontoUiState(
    val pontoOriginal: Ponto? = null,
    val data: LocalDate = LocalDate.now(),
    val hora: LocalTime = LocalTime.now(),
    val tipo: TipoPonto = TipoPonto.ENTRADA,
    val observacao: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false
) {
    /**
     * Verifica se há alterações pendentes em relação ao ponto original.
     */
    val hasChanges: Boolean
        get() = pontoOriginal?.let { original ->
            original.data != data ||
            original.hora != hora ||
            original.tipo != tipo ||
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
