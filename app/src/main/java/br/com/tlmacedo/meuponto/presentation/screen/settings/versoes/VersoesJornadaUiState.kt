package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
/**
 * Estado da tela de listagem de versões de jornada.
 */
data class VersoesJornadaUiState(
    val isLoading: Boolean = true,
    val isExcluindo: Boolean = false,
    val empregoId: Long = 0L,
    val nomeEmprego: String = "",
    val versoes: List<VersaoJornada> = emptyList(),
    val versaoVigente: VersaoJornada? = null,
    val mostrarDialogExcluir: Boolean = false,
    val versaoParaExcluir: VersaoJornada? = null,
    val errorMessage: String? = null
) {
    val temVersoes: Boolean
        get() = versoes.isNotEmpty()
    val totalVersoes: Int
        get() = versoes.size
}
