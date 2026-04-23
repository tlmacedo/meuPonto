package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Estado da tela de edição de versão de jornada.
 */
data class EditarVersaoUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isNovaVersao: Boolean = true,
    val empregoId: Long = 0L,
    val empregoApelido: String? = null,
    val empregoLogo: String? = null,
    val versaoId: Long? = null,
    val descricao: String = "",
    val dataInicio: LocalDate = LocalDate.now(),
    val dataFim: LocalDate? = null,
    val numeroVersao: Int = 1,
    val vigente: Boolean = false,
    val jornadaMaximaDiariaMinutos: Int = 600,
    val intervaloMinimoInterjornadaMinutos: Int = 660,
    val intervaloMinimoAlmocoMinutos: Int = 60,
    val intervaloMinimoDescansoMinutos: Int = 15,
    val toleranciaIntervaloMaisMinutos: Int = 0,
    val toleranciaRetornoIntervaloMinutos: Int = 5,
    val turnoMaximoMinutos: Int = 360,

    // Carga horária
    val cargaHorariaDiariaMinutos: Int = 480,
    val acrescimoMinutosDiasPontes: Int = 12,
    val cargaHorariaSemanalMinutos: Int = 2460,

    // Período/Saldo
    val primeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
    val diaInicioFechamentoRH: Int = 1,
    val zerarSaldoSemanal: Boolean = false,
    val zerarSaldoPeriodoRH: Boolean = false,
    val ocultarSaldoTotal: Boolean = false,

    // Banco de Horas
    val bancoHorasHabilitado: Boolean = false,
    val periodoBancoDias: Int = 0,
    val periodoBancoSemanas: Int = 0,
    val periodoBancoMeses: Int = 0,
    val periodoBancoAnos: Int = 0,
    val dataInicioCicloBancoAtual: LocalDate? = null,
    val diasUteisLembreteFechamento: Int = 3,
    val habilitarSugestaoAjuste: Boolean = false,
    val zerarBancoAntesPeriodo: Boolean = false,

    // Validação
    val exigeJustificativaInconsistencia: Boolean = false,

    val horarios: List<HorarioDiaSemana> = emptyList(),
    val showDataInicioPicker: Boolean = false,
    val showDataFimPicker: Boolean = false,
    val showDataInicioCicloBancoPicker: Boolean = false,
    val secaoExpandida: SecaoVersao? = SecaoVersao.VIGENCIA_IDENTIFICACAO,
    val errorMessage: String? = null,

    // DADOS ORIGINAIS PARA COMPARACAO
    val originalDescricao: String = "",
    val originalDataInicio: LocalDate = LocalDate.now(),
    val originalDataFim: LocalDate? = null,
    val originalJornadaMaximaDiariaMinutos: Int = 600,
    val originalIntervaloMinimoInterjornadaMinutos: Int = 660,
    val originalIntervaloMinimoAlmocoMinutos: Int = 60,
    val originalIntervaloMinimoDescansoMinutos: Int = 15,
    val originalToleranciaIntervaloMaisMinutos: Int = 0,
    val originalToleranciaRetornoIntervaloMinutos: Int = 5,
    val originalTurnoMaximoMinutos: Int = 360,
    val originalCargaHorariaDiariaMinutos: Int = 480,
    val originalAcrescimoMinutosDiasPontes: Int = 12,
    val originalCargaHorariaSemanalMinutos: Int = 2460,
    val originalPrimeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
    val originalDiaInicioFechamentoRH: Int = 1,
    val originalZerarSaldoSemanal: Boolean = false,
    val originalZerarSaldoPeriodoRH: Boolean = false,
    val originalOcultarSaldoTotal: Boolean = false,
    val originalBancoHorasHabilitado: Boolean = false,
    val originalPeriodoBancoDias: Int = 0,
    val originalPeriodoBancoSemanas: Int = 0,
    val originalPeriodoBancoMeses: Int = 0,
    val originalPeriodoBancoAnos: Int = 0,
    val originalDataInicioCicloBancoAtual: LocalDate? = null,
    val originalDiasUteisLembreteFechamento: Int = 3,
    val originalHabilitarSugestaoAjuste: Boolean = false,
    val originalZerarBancoAntesPeriodo: Boolean = false,
    val originalExigeJustificativaInconsistencia: Boolean = false
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val dataInicioFormatada: String
        get() = dataInicio.format(dateFormatter)

    val dataFimFormatada: String?
        get() = dataFim?.format(dateFormatter)

    val cargaHorariaDiariaFormatada: String
        get() = formatarMinutosEmHoras(cargaHorariaDiariaMinutos)

    val acrescimoMinutosDiasPontesFormatado: String
        get() = formatarMinutosEmHoras(acrescimoMinutosDiasPontes, usarFormatoReduzido = true)

    val turnoMaximoFormatado: String
        get() = formatarMinutosEmHoras(turnoMaximoMinutos)

    val jornadaMaximaFormatada: String
        get() = formatarMinutosEmHoras(jornadaMaximaDiariaMinutos)

    val intervaloInterjornadaFormatado: String
        get() = formatarMinutosEmHoras(intervaloMinimoInterjornadaMinutos)

    val intervaloAlmocoFormatado: String
        get() = formatarMinutosEmHoras(intervaloMinimoAlmocoMinutos, usarFormatoReduzido = true)

    val intervaloDescansoFormatado: String
        get() = formatarMinutosEmHoras(intervaloMinimoDescansoMinutos, usarFormatoReduzido = true)

    val toleranciaRetornoIntervaloFormatada: String
        get() = formatarMinutosEmHoras(toleranciaRetornoIntervaloMinutos, usarFormatoReduzido = true)

    val toleranciaIntervaloFormatada: String
        get() = formatarMinutosEmHoras(toleranciaIntervaloMaisMinutos, usarFormatoReduzido = true)

    val podeSalvar: Boolean
        get() = !isSaving && empregoId > 0L

    val titulo: String
        get() = if (descricao.isNotBlank()) descricao else "Versão $numeroVersao"

    // MUDANCAS GRANULARES
    val temMudancasVigencia: Boolean = !isNovaVersao && (
        descricao != originalDescricao ||
            dataInicio != originalDataInicio ||
            dataFim != originalDataFim
    )

    val temMudancasJornada: Boolean = !isNovaVersao && (
        cargaHorariaDiariaMinutos != originalCargaHorariaDiariaMinutos ||
            acrescimoMinutosDiasPontes != originalAcrescimoMinutosDiasPontes ||
            jornadaMaximaDiariaMinutos != originalJornadaMaximaDiariaMinutos ||
            intervaloMinimoAlmocoMinutos != originalIntervaloMinimoAlmocoMinutos ||
            intervaloMinimoDescansoMinutos != originalIntervaloMinimoDescansoMinutos ||
            toleranciaRetornoIntervaloMinutos != originalToleranciaRetornoIntervaloMinutos ||
            intervaloMinimoInterjornadaMinutos != originalIntervaloMinimoInterjornadaMinutos ||
            turnoMaximoMinutos != originalTurnoMaximoMinutos
    )

    val temMudancasFechamento: Boolean = !isNovaVersao && (
        diaInicioFechamentoRH != originalDiaInicioFechamentoRH ||
            primeiroDiaSemana != originalPrimeiroDiaSemana ||
            zerarSaldoPeriodoRH != originalZerarSaldoPeriodoRH ||
            ocultarSaldoTotal != originalOcultarSaldoTotal
    )

    val temMudancasBancoHoras: Boolean = !isNovaVersao && (
        bancoHorasHabilitado != originalBancoHorasHabilitado ||
            periodoBancoDias != originalPeriodoBancoDias ||
            periodoBancoSemanas != originalPeriodoBancoSemanas ||
            periodoBancoMeses != originalPeriodoBancoMeses ||
            periodoBancoAnos != originalPeriodoBancoAnos ||
            dataInicioCicloBancoAtual != originalDataInicioCicloBancoAtual ||
            habilitarSugestaoAjuste != originalHabilitarSugestaoAjuste
    )

    val temMudancasValidacao: Boolean = !isNovaVersao && (
        exigeJustificativaInconsistencia != originalExigeJustificativaInconsistencia ||
            toleranciaIntervaloMaisMinutos != originalToleranciaIntervaloMaisMinutos
    )

    // BANCO DE HORAS - COMPUTADOS
    val temBancoHoras: Boolean
        get() = bancoHorasHabilitado && (periodoBancoDias > 0 || periodoBancoSemanas > 0 || periodoBancoMeses > 0 || periodoBancoAnos > 0)

    val labelCicloBanco: String
        get() {
            return when {
                periodoBancoAnos > 0 -> if (periodoBancoAnos == 1) "1 Ano" else "$periodoBancoAnos Anos"
                periodoBancoMeses > 0 -> if (periodoBancoMeses == 1) "1 Mês" else "$periodoBancoMeses Meses"
                periodoBancoSemanas > 0 -> if (periodoBancoSemanas == 1) "1 Semana" else "$periodoBancoSemanas Semanas"
                periodoBancoDias > 0 -> if (periodoBancoDias == 1) "1 Dia" else "$periodoBancoDias Dias"
                else -> "Não definido"
            }
        }

    val progressoCicloBanco: Float
        get() {
            return when {
                periodoBancoAnos > 0 -> 20f
                periodoBancoMeses > 0 -> 8f + periodoBancoMeses
                periodoBancoSemanas > 0 -> 5f + periodoBancoSemanas
                periodoBancoDias > 0 -> (periodoBancoDias - 1).toFloat()
                else -> 0f
            }
        }

    val dataFimCicloCalculada: String
        get() {
            if (!temBancoHoras || dataInicioCicloBancoAtual == null) return "—"
            val dataFim = when {
                periodoBancoDias > 0 -> dataInicioCicloBancoAtual.plusDays(periodoBancoDias.toLong()).minusDays(1)
                periodoBancoSemanas > 0 -> dataInicioCicloBancoAtual.plusWeeks(periodoBancoSemanas.toLong()).minusDays(1)
                periodoBancoMeses > 0 -> dataInicioCicloBancoAtual.plusMonths(periodoBancoMeses.toLong()).minusDays(1)
                periodoBancoAnos > 0 -> dataInicioCicloBancoAtual.plusYears(periodoBancoAnos.toLong()).minusDays(1)
                else -> return "—"
            }
            return dataFim.format(dateFormatter)
        }

    val cicloDescricao: String
        get() {
            if (!temBancoHoras || dataInicioCicloBancoAtual == null) return "Não configurado"
            return "${dataInicioCicloBancoAtual.format(dateFormatter)} ~ $dataFimCicloCalculada"
        }

    private fun formatarMinutosEmHoras(minutos: Int, usarFormatoReduzido: Boolean = false): String {
        if (usarFormatoReduzido && minutos < 60) {
            return "${minutos}min"
        }
        val horas = minutos / 60
        val mins = minutos % 60
        return String.format("%02d:%02d", horas, mins)
    }
}
