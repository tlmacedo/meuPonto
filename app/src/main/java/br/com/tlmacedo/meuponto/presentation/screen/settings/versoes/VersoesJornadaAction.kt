package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import java.time.LocalDate

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
    data object AbrirDialogNovaVersao : VersoesJornadaAction
    data object FecharDialogNovaVersao : VersoesJornadaAction
    data class AlterarDataInicioNovaVersao(val data: LocalDate) : VersoesJornadaAction
    data class AlterarDescricaoNovaVersao(val descricao: String) : VersoesJornadaAction
    data class ToggleCopiarHorariosNovaVersao(val copiar: Boolean) : VersoesJornadaAction
    data object ConfirmarNovaVersao : VersoesJornadaAction
    data object LimparErro : VersoesJornadaAction
}
