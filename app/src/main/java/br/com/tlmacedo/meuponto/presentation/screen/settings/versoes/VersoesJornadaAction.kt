package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada

/**
 * Ações disparadas pela UI para o VersoesJornadaViewModel.
 */
sealed interface VersoesJornadaAction {
    data object Recarregar : VersoesJornadaAction
    data object CriarNovaVersao : VersoesJornadaAction
    data class EditarVersao(val versaoId: Long) : VersoesJornadaAction
    data class DefinirComoVigente(val versaoId: Long) : VersoesJornadaAction
    data class AbrirDialogExcluir(val versao: VersaoJornada) : VersoesJornadaAction
    data object FecharDialogExcluir : VersoesJornadaAction
    data object ConfirmarExclusao : VersoesJornadaAction
    data object LimparErro : VersoesJornadaAction
}
