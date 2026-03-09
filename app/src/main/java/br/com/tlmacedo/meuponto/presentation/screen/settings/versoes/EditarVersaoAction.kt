package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import java.time.LocalDate

/**
 * Ações disparadas pela UI para o EditarVersaoViewModel.
 */
sealed interface EditarVersaoAction {
    data class AlterarDescricao(val descricao: String) : EditarVersaoAction
    data class AlterarDataInicio(val data: LocalDate) : EditarVersaoAction
    data class AlterarDataFim(val data: LocalDate?) : EditarVersaoAction
    data class AlterarJornadaMaxima(val minutos: Int) : EditarVersaoAction
    data class AlterarIntervaloInterjornada(val minutos: Int) : EditarVersaoAction
    data class AlterarToleranciaIntervalo(val minutos: Int) : EditarVersaoAction
    data class MostrarDataInicioPicker(val mostrar: Boolean) : EditarVersaoAction
    data class MostrarDataFimPicker(val mostrar: Boolean) : EditarVersaoAction
    data class ToggleSecao(val secao: SecaoVersao) : EditarVersaoAction
    data object Salvar : EditarVersaoAction
    data object Cancelar : EditarVersaoAction
    data object LimparErro : EditarVersaoAction
    data object ConfigurarHorarios : EditarVersaoAction
}

/**
 * Seções expansíveis na tela de edição de versão.
 */
enum class SecaoVersao {
    INFORMACOES_BASICAS,
    PERIODO_VIGENCIA,
    CONFIGURACOES_JORNADA,
    HORARIOS
}
