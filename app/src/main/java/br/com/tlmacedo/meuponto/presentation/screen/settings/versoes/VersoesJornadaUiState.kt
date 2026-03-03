// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/versoes/VersoesJornadaUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada

/**
 * Estado da tela de listagem de versões de jornada.
 *
 * @author Thiago
 * @since 4.0.0
 */
data class VersoesJornadaUiState(
    val isLoading: Boolean = true,
    val emprego: Emprego? = null,
    val versoes: List<VersaoJornada> = emptyList(),
    val versaoVigente: VersaoJornada? = null,
    val errorMessage: String? = null,
    val mostrarDialogExcluir: Boolean = false,
    val versaoParaExcluir: VersaoJornada? = null,
    val isExcluindo: Boolean = false
) {
    val temVersoes: Boolean get() = versoes.isNotEmpty()
    val totalVersoes: Int get() = versoes.size
    val podeExcluirVersao: Boolean get() = versoes.size > 1
    
    val nomeEmprego: String get() = emprego?.nome ?: "Emprego"
}

/**
 * Eventos únicos da tela de versões.
 */
sealed interface VersoesJornadaEvent {
    data class MostrarMensagem(val mensagem: String) : VersoesJornadaEvent
    data class NavegarParaEditar(val versaoId: Long) : VersoesJornadaEvent
    data object NavegarParaNova : VersoesJornadaEvent
    data object Voltar : VersoesJornadaEvent
}

/**
 * Ações da tela de versões.
 */
sealed interface VersoesJornadaAction {
    data object Recarregar : VersoesJornadaAction
    data object CriarNovaVersao : VersoesJornadaAction
    data class EditarVersao(val versaoId: Long) : VersoesJornadaAction
    data class AbrirDialogExcluir(val versao: VersaoJornada) : VersoesJornadaAction
    data object FecharDialogExcluir : VersoesJornadaAction
    data object ConfirmarExclusao : VersoesJornadaAction
    data class DefinirComoVigente(val versaoId: Long) : VersoesJornadaAction
    data object LimparErro : VersoesJornadaAction
}
