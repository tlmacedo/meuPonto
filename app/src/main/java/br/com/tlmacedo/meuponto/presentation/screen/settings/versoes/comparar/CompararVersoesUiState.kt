package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes.comparar

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada

/**
 * Estado da tela de comparação entre duas versões de jornada.
 */
data class CompararVersoesUiState(
    val isLoading: Boolean = true,
    val versao1: VersaoJornada? = null,
    val versao2: VersaoJornada? = null,
    val errorMessage: String? = null
)
