package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import java.time.LocalDate

/**
 * Estado da tela de listagem de versões de jornada.
 */
data class VersoesJornadaUiState(
    val isLoading: Boolean = true,
    val isExcluindo: Boolean = false,
    val isCriando: Boolean = false,
    val empregoId: Long = 0L,
    val nomeEmprego: String = "",
    val empregoApelido: String? = null,
    val empregoLogo: String? = null,
    val versoes: List<VersaoJornada> = emptyList(),
    val versaoVigente: VersaoJornada? = null,
    val mostrarDialogExcluir: Boolean = false,
    val versaoParaExcluir: VersaoJornada? = null,
    val mostrarDialogNovaVersao: Boolean = false,
    val dataInicioNovaVersao: LocalDate = LocalDate.now(),
    val descricaoNovaVersao: String = "",
    val copiarHorariosNovaVersao: Boolean = true,
    val versoesSelecionadas: Set<Long> = emptySet(),
    val errorMessage: String? = null
) {
    val modoSelecao: Boolean
        get() = versoesSelecionadas.isNotEmpty()
    val temVersoes: Boolean
        get() = versoes.isNotEmpty()
    val totalVersoes: Int
        get() = versoes.size
}
