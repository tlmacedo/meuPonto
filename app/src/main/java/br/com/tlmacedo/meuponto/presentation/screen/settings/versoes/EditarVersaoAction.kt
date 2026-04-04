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
    data class AlterarTurnoMaximo(val minutos: Int) : EditarVersaoAction

    // Carga horária
    data class AlterarCargaHorariaDiaria(val minutos: Int) : EditarVersaoAction
    data class AlterarAcrescimoDiasPontes(val minutos: Int) : EditarVersaoAction
    data class AlterarCargaHorariaSemanal(val minutos: Int) : EditarVersaoAction

    // Período/Saldo
    data class AlterarPrimeiroDiaSemana(val dia: br.com.tlmacedo.meuponto.domain.model.DiaSemana) : EditarVersaoAction
    data class AlterarDiaInicioFechamentoRH(val dia: Int) : EditarVersaoAction
    data class AlterarZerarSaldoSemanal(val zerar: Boolean) : EditarVersaoAction
    data class AlterarZerarSaldoPeriodoRH(val zerar: Boolean) : EditarVersaoAction
    data class AlterarOcultarSaldoTotal(val ocultar: Boolean) : EditarVersaoAction

    // Banco de Horas
    data class AlterarBancoHorasHabilitado(val habilitar: Boolean) : EditarVersaoAction
    data class AlterarPeriodoBancoSemanas(val semanas: Int) : EditarVersaoAction
    data class AlterarPeriodoBancoMeses(val meses: Int) : EditarVersaoAction
    data class AlterarDataInicioCicloBancoAtual(val data: LocalDate?) : EditarVersaoAction

    data class AlterarZerarBancoAntesPeriodo(val zerar: Boolean) : EditarVersaoAction
    data class AlterarHabilitarSugestaoAjuste(val habilitar: Boolean) : EditarVersaoAction
    data class AlterarDiasUteisLembreteFechamento(val dias: Int) : EditarVersaoAction
    data class AlterarExigeJustificativaInconsistencia(val exige: Boolean) : EditarVersaoAction

    data class MostrarDataInicioPicker(val mostrar: Boolean) : EditarVersaoAction
    data class MostrarDataFimPicker(val mostrar: Boolean) : EditarVersaoAction
    data class MostrarDataInicioCicloBancoPicker(val mostrar: Boolean) : EditarVersaoAction
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
    CARGA_HORARIA,
    PERIODO_SALDO,
    BANCO_HORAS,
    VALIDACAO,
    HORARIOS
}
