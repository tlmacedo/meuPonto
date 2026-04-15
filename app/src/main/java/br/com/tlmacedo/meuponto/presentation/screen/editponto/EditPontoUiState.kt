// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.Marcador
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.LocalDate
import java.time.LocalTime

/**
 * Estado imutável da tela de edição de ponto.
 *
 * ## Sobre tipoPonto:
 * Não existe campo tipoPonto neste estado. O tipo do ponto (entrada/saída)
 * é dinâmico e calculado pelo domínio com base na posição do registro no dia:
 * - Posição ímpar (1º, 3º, 5º...): Entrada
 * - Posição par (2º, 4º, 6º...): Saída
 *
 * Por isso, tipoPonto não é persistido no banco, não é guardado em memória
 * e não faz parte do UiState de edição.
 *
 * @property isLoading true durante operações assíncronas
 * @property ponto Ponto original sendo editado (null enquanto carregando)
 * @property empregos Lista de empregos disponíveis
 * @property marcadores Lista de marcadores do emprego selecionado
 * @property empregoSelecionado Emprego atualmente selecionado
 * @property data Data do ponto
 * @property hora Hora do ponto
 * @property observacao Observação livre opcional
 * @property nsr Número Sequencial de Registro
 * @property fotoRelativePath Caminho relativo da foto (null = sem foto)
 * @property fotoRemovida true se a foto foi marcada para remoção
 * @property mostrarDatePicker true quando o DatePicker deve ser exibido
 * @property mostrarTimePicker true quando o TimePicker deve ser exibido
 * @property isSalvo true quando salvo com sucesso (sinaliza navegação de volta)
 * @property erro Mensagem de erro para Snackbar (null = sem erro)
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 12.0.0 - Removida Redeclaration de EditPontoAction; removido tipoPonto
 *                   (campo dinâmico calculado pela posição do ponto no dia,
 *                   não persistido no banco); adicionados fotoRelativePath,
 *                   fotoRemovida, mostrarDatePicker, mostrarTimePicker
 */
data class EditPontoUiState(
    val isLoading: Boolean = false,
    val ponto: Ponto? = null,
    val empregos: List<Emprego> = emptyList(),
    val marcadores: List<Marcador> = emptyList(),
    val empregoSelecionado: Emprego? = null,
    val empregoApelido: String? = null,
    val empregoLogo: String? = null,
    val data: LocalDate = LocalDate.now(),
    val hora: LocalTime = LocalTime.now(),
    val observacao: String = "",
    val nsr: String = "",
    val fotoRelativePath: String? = null,
    val fotoRemovida: Boolean = false,
    val mostrarDatePicker: Boolean = false,
    val mostrarTimePicker: Boolean = false,
    val isSalvo: Boolean = false,
    val erro: String? = null
) {
    /**
     * true se o ponto possui foto de comprovante ativa (não marcada para remoção).
     */
    val temFoto: Boolean
        get() = !fotoRelativePath.isNullOrBlank() && !fotoRemovida

    /**
     * true se há alguma alteração em relação ao ponto original.
     * Controla a habilitação do botão de salvar.
     *
     * tipoPonto não é comparado pois é calculado dinamicamente
     * e não é um campo editável nesta tela.
     */
    val temAlteracao: Boolean
        get() = ponto != null && (
                data != ponto.dataHora.toLocalDate() ||
                        hora != ponto.dataHora.toLocalTime().withSecond(0).withNano(0) ||
                        observacao.trim() != (ponto.observacao ?: "").trim() ||
                        nsr.trim() != (ponto.nsr ?: "").trim() ||
                        empregoSelecionado?.id != ponto.empregoId ||
                        fotoRemovida ||
                        fotoRelativePath != ponto.fotoComprovantePath
                )
}