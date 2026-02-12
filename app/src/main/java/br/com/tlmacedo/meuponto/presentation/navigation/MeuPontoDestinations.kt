package br.com.tlmacedo.meuponto.presentation.navigation

object MeuPontoDestinations {
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val EDIT_PONTO = "edit_ponto/{pontoId}"

    fun editPonto(pontoId: Long): String = "edit_ponto/$pontoId"
}
