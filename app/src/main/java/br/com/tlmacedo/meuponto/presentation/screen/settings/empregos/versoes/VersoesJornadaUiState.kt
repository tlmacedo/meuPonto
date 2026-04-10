package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.versoes

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada

/**
 * Estado da tela de listagem de versões de jornada.
 */
data class VersoesJornadaUiState(
    val empregoId: Long = -1L,
    val nomeEmprego: String = "",
    val versoes: List<VersaoJornada> = emptyList(),
    val isLoading: Boolean = true,
    val erro: String? = null
)
