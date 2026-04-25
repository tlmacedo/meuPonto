// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/VersaoJornada.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Modelo de domínio que representa uma versão de jornada de trabalho.
 *
 * Agora contém configurações completas de jornada, banco de horas e período RH,
 * permitindo versionamento temporal de todas essas configurações.
 *
 * @author Thiago
 * @since 2.7.0
 * @updated 8.0.0 - Migração de campos de ConfiguracaoEmprego para versionamento temporal
 */
data class VersaoJornada(
    val id: Long = 0,
    val empregoId: Long,
    val dataInicio: LocalDate,
    val dataFim: LocalDate? = null,
    val descricao: String? = null,
    val numeroVersao: Int = 1,
    val vigente: Boolean = true,

    // ════════════════════════════════════════════════════════════════════════
    // JORNADA (campos existentes)
    // ════════════════════════════════════════════════════════════════════════
    /** Jornada máxima diária total (soma de todos os turnos). Default: 600min (10h) */
    val jornadaMaximaDiariaMinutos: Int = 600,
    /** Intervalo mínimo entre jornadas (interjornada). Default: 660min (11h) */
    val intervaloMinimoInterjornadaMinutos: Int = 660,
    /** Turno máximo (tempo entre entrada e saída de um turno). Default: 360min (6h) */
    val turnoMaximoMinutos: Int = 360,
    /** Intervalo mínimo de almoço/descanso. Default: 60min */
    val intervaloMinimoAlmocoMinutos: Int = 60,
    /** Intervalo mínimo de descanso (curtos). Default: 15min */
    val intervaloMinimoDescansoMinutos: Int = 15,
    /** Tolerância de intervalo para mais (Global). Default: 0min */
    val toleranciaIntervaloMaisMinutos: Int = 0,
    /** Tolerância de retorno de intervalo. Default: 5min */
    val toleranciaRetornoIntervaloMinutos: Int = 5,

    // ════════════════════════════════════════════════════════════════════════
    // CARGA HORÁRIA (migrados de ConfiguracaoEmprego)
    // ════════════════════════════════════════════════════════════════════════
    /** Carga horária base diária. Default: 480min (8h) */
    val cargaHorariaDiariaMinutos: Int = 480,
    /** Acréscimo diário para compensar dias ponte. Default: 12min */
    val acrescimoMinutosDiasPontes: Int = 12,
    /** Carga horária semanal total. Default: 2460min (41h = 5 × 492) */
    val cargaHorariaSemanalMinutos: Int = 2460,

    // ════════════════════════════════════════════════════════════════════════
    // PERÍODO/SALDO (migrados de ConfiguracaoEmprego)
    // ════════════════════════════════════════════════════════════════════════
    /** Primeiro dia da semana para cálculos. Default: SEGUNDA */
    val primeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
    /** Dia do mês para fechamento RH. Default: 1 */
    val diaInicioFechamentoRH: Int = 1,
    /** Zerar saldo ao fim de cada semana. Default: false */
    val zerarSaldoSemanal: Boolean = false,
    /** Zerar saldo ao fim do período RH. Default: false */
    val zerarSaldoPeriodoRH: Boolean = false,
    /** Ocultar saldo total na interface. Default: false */
    val ocultarSaldoTotal: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // BANCO DE HORAS (migrados de ConfiguracaoEmprego)
    // ════════════════════════════════════════════════════════════════════════
    /** Flag que indica se banco de horas está habilitado. Default: false */
    val bancoHorasHabilitado: Boolean = false,
    /** Período do ciclo em dias. Default: 0 */
    val periodoBancoDias: Int = 0,
    /** Período do ciclo em semanas. Default: 0 */
    val periodoBancoSemanas: Int = 0,
    /** Período do ciclo em meses. Default: 0 */
    val periodoBancoMeses: Int = 0,
    /** Período do ciclo em anos. Default: 0 */
    val periodoBancoAnos: Int = 0,
    /** Data de início do ciclo atual. Null se não configurado */
    val dataInicioCicloBancoAtual: LocalDate? = null,
    /** Dias úteis antes do fim para lembrete. Default: 3 */
    val diasUteisLembreteFechamento: Int = 3,
    /** Habilitar sugestão de ajuste automático. Default: false */
    val habilitarSugestaoAjuste: Boolean = false,
    /** Ignorar registros antes do início do banco. Default: false */
    val zerarBancoAntesPeriodo: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // VALIDAÇÃO (migrado de ConfiguracaoEmprego)
    // ════════════════════════════════════════════════════════════════════════
    /** Exige justificativa para inconsistências. Default: false */
    val exigeJustificativaInconsistencia: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // AUDITORIA
    // ════════════════════════════════════════════════════════════════════════
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        private val FORMATTER_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        /** Turno máximo padrão: 6 horas */
        const val TURNO_MAXIMO_PADRAO_MINUTOS = 360

        /** Carga horária diária padrão: 8 horas */
        const val CARGA_HORARIA_DIARIA_PADRAO_MINUTOS = 480

        /** Acréscimo padrão dias ponte: 12 minutos */
        const val ACRESCIMO_DIAS_PONTES_PADRAO_MINUTOS = 12
    }

    // ════════════════════════════════════════════════════════════════════════
    // PROPRIEDADES COMPUTADAS - VERSÃO
    // ════════════════════════════════════════════════════════════════════════

    fun contemData(data: LocalDate): Boolean {
        val aposInicio = !data.isBefore(dataInicio)
        val antesFim = dataFim == null || !data.isAfter(dataFim)
        return aposInicio && antesFim
    }

    val periodoFormatado: String
        get() {
            val inicio = dataInicio.format(FORMATTER_DATA)
            return if (dataFim != null) {
                "$inicio até ${dataFim.format(FORMATTER_DATA)}"
            } else {
                "$inicio em diante"
            }
        }

    val titulo: String
        get() = if (descricao != null) "Versão $numeroVersao - $descricao" else "Versão $numeroVersao"

    // ════════════════════════════════════════════════════════════════════════
    // PROPRIEDADES COMPUTADAS - CARGA HORÁRIA
    // ════════════════════════════════════════════════════════════════════════

    /** Carga horária efetiva diária (base + acréscimo dias ponte) */
    val cargaHorariaEfetivaDiariaMinutos: Int
        get() = cargaHorariaDiariaMinutos + acrescimoMinutosDiasPontes

    /** Carga horária de trabalho diária (Total a ser cumprido no dia) */
    val jornadaTrabalhoDiariaMinutos: Int
        get() = cargaHorariaEfetivaDiariaMinutos

    val cargaHorariaDiariaFormatada: String
        get() = formatarMinutosComoHora(cargaHorariaDiariaMinutos)

    val cargaHorariaEfetivaDiariaFormatada: String
        get() = formatarMinutosComoHora(cargaHorariaEfetivaDiariaMinutos)

    val jornadaTrabalhoDiariaFormatada: String
        get() = formatarMinutosComoHora(jornadaTrabalhoDiariaMinutos)

    val cargaHorariaSemanalFormatada: String
        get() = formatarMinutosComoHora(cargaHorariaSemanalMinutos)

    val acrescimoMinutosDiasPontesFormatado: String
        get() = formatarMinutosComoHora(acrescimoMinutosDiasPontes)

    // ════════════════════════════════════════════════════════════════════════
    // PROPRIEDADES COMPUTADAS - JORNADA
    // ════════════════════════════════════════════════════════════════════════

    val jornadaMaximaFormatada: String
        get() = formatarMinutosComoHora(jornadaMaximaDiariaMinutos)

    val intervaloInterjornadaFormatado: String
        get() = formatarMinutosComoHora(intervaloMinimoInterjornadaMinutos)

    val turnoMaximoFormatado: String
        get() = formatarMinutosComoHora(turnoMaximoMinutos)

    val intervaloAlmocoFormatado: String
        get() = formatarMinutosComoHora(intervaloMinimoAlmocoMinutos, usarFormatoReduzido = true)

    val intervaloDescansoFormatado: String
        get() = formatarMinutosComoHora(intervaloMinimoDescansoMinutos, usarFormatoReduzido = true)

    val toleranciaRetornoIntervaloFormatada: String
        get() = formatarMinutosComoHora(
            toleranciaRetornoIntervaloMinutos,
            usarFormatoReduzido = true
        )

    // ════════════════════════════════════════════════════════════════════════
    // PROPRIEDADES COMPUTADAS - BANCO DE HORAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Verifica se o banco de horas está configurado e ativo.
     */
    val temBancoHoras: Boolean
        get() = bancoHorasHabilitado && (periodoBancoDias > 0 || periodoBancoSemanas > 0 || periodoBancoMeses > 0 || periodoBancoAnos > 0)

    /**
     * Calcula o período total do banco em dias para facilitar cálculos.
     */
    val periodoBancoEmDias: Int
        get() = periodoBancoDias + (periodoBancoSemanas * 7) + (periodoBancoMeses * 30) + (periodoBancoAnos * 365)

    /**
     * Retorna uma descrição legível do período do banco.
     */
    val periodoBancoDescricao: String
        get() = when {
            !temBancoHoras -> "Desabilitado"
            periodoBancoDias > 0 -> if (periodoBancoDias == 1) "1 dia" else "$periodoBancoDias dias"
            periodoBancoSemanas > 0 -> if (periodoBancoSemanas == 1) "1 semana" else "$periodoBancoSemanas semanas"
            periodoBancoMeses > 0 -> if (periodoBancoMeses == 1) "1 mês" else "$periodoBancoMeses meses"
            periodoBancoAnos > 0 -> if (periodoBancoAnos == 1) "1 ano" else "$periodoBancoAnos anos"
            else -> "Não configurado"
        }

    /**
     * Calcula a data de fim do ciclo atual do banco de horas.
     * Retorna null se não houver ciclo configurado.
     */
    fun calcularDataFimCicloAtual(): LocalDate? {
        val dataInicioCiclo = dataInicioCicloBancoAtual ?: return null
        if (!temBancoHoras) return null

        return when {
            periodoBancoDias > 0 -> dataInicioCiclo.plusDays(periodoBancoDias.toLong()).minusDays(1)
            periodoBancoSemanas > 0 -> dataInicioCiclo.plusWeeks(periodoBancoSemanas.toLong())
                .minusDays(1)

            periodoBancoMeses > 0 -> dataInicioCiclo.plusMonths(periodoBancoMeses.toLong())
                .minusDays(1)

            periodoBancoAnos > 0 -> dataInicioCiclo.plusYears(periodoBancoAnos.toLong())
                .minusDays(1)

            else -> null
        }
    }

    /**
     * Calcula a data de início do próximo ciclo do banco de horas.
     */
    fun calcularDataInicioProximoCiclo(): LocalDate? {
        val dataInicioCiclo = dataInicioCicloBancoAtual ?: return null
        if (!temBancoHoras) return null

        return when {
            periodoBancoDias > 0 -> dataInicioCiclo.plusDays(periodoBancoDias.toLong())
            periodoBancoSemanas > 0 -> dataInicioCiclo.plusWeeks(periodoBancoSemanas.toLong())
            periodoBancoMeses > 0 -> dataInicioCiclo.plusMonths(periodoBancoMeses.toLong())
            periodoBancoAnos > 0 -> dataInicioCiclo.plusYears(periodoBancoAnos.toLong())
            else -> null
        }
    }

    /**
     * Verifica se uma data está dentro do ciclo atual do banco.
     */
    fun dataEstaNoCicloAtual(data: LocalDate): Boolean {
        val inicio = dataInicioCicloBancoAtual ?: return false
        val fim = calcularDataFimCicloAtual() ?: return false
        return !data.isBefore(inicio) && !data.isAfter(fim)
    }

    // ════════════════════════════════════════════════════════════════════════
    // PROPRIEDADES COMPUTADAS - PERÍODO RH
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Calcula o período RH (fechamento mensal) para uma data específica.
     * Retorna o par (dataInicio, dataFim) do período.
     */
    fun calcularPeriodoRH(dataReferencia: LocalDate): Pair<LocalDate, LocalDate> {
        val diaFechamento = diaInicioFechamentoRH.coerceIn(1, 28)

        val dataInicioPeriodo = if (dataReferencia.dayOfMonth >= diaFechamento) {
            dataReferencia.withDayOfMonth(diaFechamento)
        } else {
            dataReferencia.minusMonths(1).withDayOfMonth(diaFechamento)
        }

        val dataFimPeriodo = dataInicioPeriodo.plusMonths(1).minusDays(1)

        return dataInicioPeriodo to dataFimPeriodo
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILITÁRIOS
    // ════════════════════════════════════════════════════════════════════════

    private fun formatarMinutosComoHora(
        minutos: Int,
        usarFormatoReduzido: Boolean = false
    ): String {
        if (usarFormatoReduzido && minutos < 60) {
            return "${minutos}min"
        }
        val horas = minutos / 60
        val mins = minutos % 60
        return String.format("%02d:%02d", horas, mins)
    }
}
