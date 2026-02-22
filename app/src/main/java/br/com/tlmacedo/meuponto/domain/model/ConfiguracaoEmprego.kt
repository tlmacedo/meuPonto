// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ConfiguracaoEmprego.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo de domínio que representa as configurações específicas de um emprego.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Refatoração do sistema de ciclos de banco de horas
 */
data class ConfiguracaoEmprego(
    val id: Long = 0,
    val empregoId: Long,

    // JORNADA DE TRABALHO
    val cargaHorariaDiariaMinutos: Int = 492,
    val jornadaMaximaDiariaMinutos: Int = 600,
    val intervaloMinimoInterjornadaMinutos: Int = 660,
    val intervaloMinimoMinutos: Int = 60,

    // TOLERÂNCIAS (apenas intervalo - entrada/saída são por dia)
    val toleranciaIntervaloMaisMinutos: Int = 0,

    // VALIDAÇÕES
    val exigeJustificativaInconsistencia: Boolean = false,

    // NSR
    val habilitarNsr: Boolean = false,
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,

    // LOCALIZAÇÃO
    val habilitarLocalizacao: Boolean = false,
    val localizacaoAutomatica: Boolean = false,
    val exibirLocalizacaoDetalhes: Boolean = true,

    // EXIBIÇÃO
    val exibirDuracaoTurno: Boolean = true,
    val exibirDuracaoIntervalo: Boolean = true,

    // PERÍODO RH (FECHAMENTO MENSAL)
    val primeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
    val diaInicioFechamentoRH: Int = 1, // Renomeado de primeiroDiaMes

    // SALDO
    val zerarSaldoSemanal: Boolean = false,
    val zerarSaldoPeriodoRH: Boolean = false, // Renomeado de zerarSaldoMensal
    val ocultarSaldoTotal: Boolean = false,

    // BANCO DE HORAS - CICLO
    val bancoHorasHabilitado: Boolean = false, // Novo: flag explícita
    val periodoBancoSemanas: Int = 0, // Novo: período em semanas (1-3)
    val periodoBancoMeses: Int = 0, // Renomeado de periodoBancoHorasMeses
    val dataInicioCicloBancoAtual: LocalDate? = null, // Renomeado de ultimoFechamentoBanco
    val diasUteisLembreteFechamento: Int = 3,
    val habilitarSugestaoAjuste: Boolean = false,
    val zerarBancoAntesPeriodo: Boolean = false, // Ignorar registros antes do início

    // AUDITORIA
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Verifica se o banco de horas está configurado e ativo.
     */
    val temBancoHoras: Boolean
        get() = bancoHorasHabilitado && (periodoBancoSemanas > 0 || periodoBancoMeses > 0)

    /**
     * Calcula o período total do banco em dias para facilitar cálculos.
     */
    val periodoBancoEmDias: Int
        get() = (periodoBancoSemanas * 7) + (periodoBancoMeses * 30)

    /**
     * Calcula a data de fim do ciclo atual do banco de horas.
     * Retorna null se não houver ciclo configurado.
     */
    fun calcularDataFimCicloAtual(): LocalDate? {
        val dataInicio = dataInicioCicloBancoAtual ?: return null
        if (!temBancoHoras) return null

        return when {
            periodoBancoSemanas > 0 -> dataInicio.plusWeeks(periodoBancoSemanas.toLong()).minusDays(1)
            periodoBancoMeses > 0 -> dataInicio.plusMonths(periodoBancoMeses.toLong()).minusDays(1)
            else -> null
        }
    }

    /**
     * Calcula a data de início do próximo ciclo do banco de horas.
     */
    fun calcularDataInicioProximoCiclo(): LocalDate? {
        val dataInicio = dataInicioCicloBancoAtual ?: return null
        if (!temBancoHoras) return null

        return when {
            periodoBancoSemanas > 0 -> dataInicio.plusWeeks(periodoBancoSemanas.toLong())
            periodoBancoMeses > 0 -> dataInicio.plusMonths(periodoBancoMeses.toLong())
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

    /**
     * Calcula o período RH (fechamento mensal) para uma data específica.
     * Retorna o par (dataInicio, dataFim) do período.
     */
    fun calcularPeriodoRH(dataReferencia: LocalDate): Pair<LocalDate, LocalDate> {
        val diaFechamento = diaInicioFechamentoRH.coerceIn(1, 28)

        // Se a data de referência é >= dia de fechamento, o período começou neste mês
        // Se a data de referência é < dia de fechamento, o período começou no mês anterior
        val dataInicio = if (dataReferencia.dayOfMonth >= diaFechamento) {
            dataReferencia.withDayOfMonth(diaFechamento)
        } else {
            dataReferencia.minusMonths(1).withDayOfMonth(diaFechamento)
        }

        // O fim é sempre o dia anterior ao início do próximo período
        val dataFim = dataInicio.plusMonths(1).minusDays(1)

        return dataInicio to dataFim
    }

    /**
     * Retorna uma descrição legível do período do banco.
     */
    val periodoBancoDescricao: String
        get() = when {
            !temBancoHoras -> "Desabilitado"
            periodoBancoSemanas == 1 -> "1 semana"
            periodoBancoSemanas in 2..3 -> "$periodoBancoSemanas semanas"
            periodoBancoMeses == 1 -> "1 mês"
            periodoBancoMeses > 1 -> "$periodoBancoMeses meses"
            else -> "Não configurado"
        }

    // Formatadores existentes...
    val cargaHorariaDiariaFormatada: String
        get() = formatarMinutosComoHoras(cargaHorariaDiariaMinutos)

    val jornadaMaximaDiariaFormatada: String
        get() = formatarMinutosComoHoras(jornadaMaximaDiariaMinutos)

    val intervaloMinimoInterjornadaFormatada: String
        get() = formatarMinutosComoHoras(intervaloMinimoInterjornadaMinutos)

    val intervaloMinimoFormatado: String
        get() = formatarMinutosComoHoras(intervaloMinimoMinutos)

    private fun formatarMinutosComoHoras(minutos: Int): String {
        val horas = minutos / 60
        val mins = minutos % 60
        return String.format("%02d:%02d", horas, mins)
    }
}
