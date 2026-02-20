// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ResumoDia.kt
package br.com.tlmacedo.meuponto.domain.model

import br.com.tlmacedo.meuponto.util.formatarDuracao
import br.com.tlmacedo.meuponto.util.formatarSaldo
import br.com.tlmacedo.meuponto.util.minutosParaDuracaoCompacta
import br.com.tlmacedo.meuponto.util.minutosParaIntervalo
import br.com.tlmacedo.meuponto.util.minutosParaTurno
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Status simplificado do dia para exibição no histórico.
 *
 * @author Thiago
 * @since 3.0.0
 */
enum class StatusDiaResumo(val descricao: String, val isConsistente: Boolean) {
    COMPLETO("Completo", true),
    EM_ANDAMENTO("Em andamento", true),
    INCOMPLETO("Incompleto", false),
    COM_PROBLEMAS("Com problemas", false),
    SEM_REGISTRO("Sem registro", true),
    FERIADO("Feriado", true),
    FERIADO_TRABALHADO("Feriado trabalhado", true)
}

/**
 * Tipo de dia especial que afeta o cálculo de jornada.
 *
 * REGRAS DE CÁLCULO:
 *
 * Jornada ZERADA (trabalho = hora extra):
 * - FERIADO, PONTE, FACULTATIVO, FERIAS, ATESTADO, FALTA_JUSTIFICADA
 *
 * Jornada NORMAL (débito se não trabalhar):
 * - NORMAL, FOLGA, FALTA_INJUSTIFICADA
 *
 * @author Thiago
 * @since 4.0.0
 */
enum class TipoDiaEspecial(val descricao: String, val emoji: String) {
    /** Dia normal de trabalho */
    NORMAL("Dia normal", "📅"),

    /** Feriado oficial (nacional/estadual/municipal) - jornada zerada */
    FERIADO("Feriado", "🎉"),

    /** Ponte (dia entre feriado e fim de semana) - jornada zerada */
    PONTE("Ponte", "🌉"),

    /** Ponto facultativo - jornada zerada */
    FACULTATIVO("Ponto Facultativo", "📋"),

    /** Férias - jornada zerada */
    FERIAS("Férias", "🏖️"),

    /** Atestado/Declaração (ausência justificada) - jornada zerada */
    ATESTADO("Atestado", "🏥"),

    /** Falta justificada - jornada zerada */
    FALTA_JUSTIFICADA("Falta Justificada", "📝"),

    /** Folga - jornada normal (gera débito) */
    FOLGA("Folga", "😴"),

    /** Falta injustificada - jornada normal (gera débito) */
    FALTA_INJUSTIFICADA("Falta Injustificada", "❌");

    /**
     * Verifica se este tipo zera a jornada (não gera débito).
     *
     * Zeram jornada: FERIADO, PONTE, FACULTATIVO, FERIAS, ATESTADO, FALTA_JUSTIFICADA
     * Mantêm jornada: NORMAL, FOLGA, FALTA_INJUSTIFICADA
     */
    val zeraJornada: Boolean
        get() = this in listOf(
            FERIADO,
            PONTE,
            FACULTATIVO,
            FERIAS,
            ATESTADO,
            FOLGA,
            FALTA_JUSTIFICADA
        )

    /**
     * Verifica se é um tipo de feriado (para exibição do banner).
     */
    val isTipoFeriado: Boolean
        get() = this in listOf(FERIADO, PONTE, FACULTATIVO)

    /**
     * Verifica se é ausência justificada (abonada).
     */
    val isAusenciaJustificada: Boolean
        get() = this in listOf(FERIADO, PONTE, FACULTATIVO, FERIAS, ATESTADO, FALTA_JUSTIFICADA)
}

/**
 * Modelo que representa o resumo de um dia de trabalho.
 *
 * ARQUITETURA:
 * - Os intervalos (turnos) são a fonte única de verdade para cálculos
 * - `horasTrabalhadas` é calculado a partir da soma das durações dos intervalos
 * - `tipoDiaEspecial` define o comportamento do cálculo
 * - Suporte a tempo em andamento (turno aberto) para cálculos em tempo real
 *
 * REGRAS DE CÁLCULO:
 * - Jornada zerada: saldo = trabalhado (hora extra)
 * - Jornada normal: saldo = trabalhado - jornada (pode ser negativo)
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 4.0.0 - Adicionado suporte a dias especiais
 * @updated 4.1.0 - Adicionado cálculo com tempo em andamento
 */
data class ResumoDia(
    val data: LocalDate,
    val pontos: List<Ponto> = emptyList(),
    val cargaHorariaDiaria: Duration = Duration.ofHours(8),
    val intervaloMinimoMinutos: Int = 60,
    val toleranciaIntervaloMinutos: Int = 15,
    val tipoDiaEspecial: TipoDiaEspecial = TipoDiaEspecial.NORMAL
) {

    /** Lista de intervalos entre pontos de entrada e saída (FONTE ÚNICA DE VERDADE) */
    val intervalos: List<IntervaloPonto> by lazy {
        calcularIntervalos()
    }

    /**
     * Verifica se há um turno aberto (entrada sem saída correspondente).
     */
    val temTurnoAberto: Boolean
        get() = pontos.isNotEmpty() && pontos.size % 2 != 0

    /**
     * Obtém o horário de início do turno aberto (última entrada sem saída).
     */
    val horarioInicioTurnoAberto: LocalDateTime?
        get() = if (temTurnoAberto) {
            pontos.sortedBy { it.dataHora }.lastOrNull()?.dataHora
        } else null

    /**
     * Calcula o tempo em andamento do turno aberto (desde a última entrada até agora).
     * Retorna Duration.ZERO se não houver turno aberto ou se a data não for hoje.
     */
    fun calcularTempoEmAndamento(horaAtual: LocalTime = LocalTime.now()): Duration {
        if (!temTurnoAberto) return Duration.ZERO
        if (data != LocalDate.now()) return Duration.ZERO

        val inicioTurno = horarioInicioTurnoAberto ?: return Duration.ZERO
        val agora = LocalDateTime.of(data, horaAtual)

        return if (agora.isAfter(inicioTurno)) {
            Duration.between(inicioTurno, agora)
        } else {
            Duration.ZERO
        }
    }

    /**
     * Total de horas trabalhadas (CALCULADO A PARTIR DOS INTERVALOS FECHADOS).
     * NÃO inclui o tempo em andamento de turnos abertos.
     */
    val horasTrabalhadas: Duration by lazy {
        intervalos
            .mapNotNull { it.duracao }
            .fold(Duration.ZERO) { acc, duracao -> acc.plus(duracao) }
    }

    /**
     * Total de horas trabalhadas INCLUINDO o tempo em andamento.
     * Use esta propriedade para exibição em tempo real na UI.
     */
    fun horasTrabalhadasComAndamento(horaAtual: LocalTime = LocalTime.now()): Duration {
        return horasTrabalhadas.plus(calcularTempoEmAndamento(horaAtual))
    }

    /**
     * Horas trabalhadas em minutos (sem andamento).
     */
    val horasTrabalhadasMinutos: Int
        get() = horasTrabalhadas.toMinutes().toInt()

    /**
     * Horas trabalhadas em minutos INCLUINDO tempo em andamento.
     */
    fun horasTrabalhadasComAndamentoMinutos(horaAtual: LocalTime = LocalTime.now()): Int {
        return horasTrabalhadasComAndamento(horaAtual).toMinutes().toInt()
    }

    /** Carga horária diária em minutos (configurada na versão de jornada) */
    val cargaHorariaDiariaMinutos: Int
        get() = cargaHorariaDiaria.toMinutes().toInt()

    /**
     * Carga horária efetiva do dia (usada no cálculo de saldo).
     *
     * - Jornada zerada (FERIADO, PONTE, FACULTATIVO, FERIAS, ATESTADO, FALTA_JUSTIFICADA): 0h
     * - Jornada normal (NORMAL, FOLGA, FALTA_INJUSTIFICADA): carga configurada
     */
    val cargaHorariaEfetiva: Duration
        get() = if (tipoDiaEspecial.zeraJornada) Duration.ZERO else cargaHorariaDiaria

    /** Carga horária efetiva em minutos */
    val cargaHorariaEfetivaMinutos: Int
        get() = cargaHorariaEfetiva.toMinutes().toInt()

    /**
     * Saldo do dia (positivo = hora extra, negativo = deve horas).
     * NÃO inclui tempo em andamento.
     *
     * Cálculo único: saldo = trabalhado - cargaHorariaEfetiva
     *
     * - Jornada zerada: saldo = trabalhado - 0 = trabalhado (sempre >= 0)
     * - Jornada normal: saldo = trabalhado - jornada (pode ser negativo)
     */
    val saldoDia: Duration
        get() = horasTrabalhadas.minus(cargaHorariaEfetiva)

    /**
     * Saldo do dia INCLUINDO tempo em andamento.
     * Use esta propriedade para exibição em tempo real na UI.
     */
    fun saldoDiaComAndamento(horaAtual: LocalTime = LocalTime.now()): Duration {
        return horasTrabalhadasComAndamento(horaAtual).minus(cargaHorariaEfetiva)
    }

    /** Saldo do dia em minutos (sem andamento) */
    val saldoDiaMinutos: Int
        get() = saldoDia.toMinutes().toInt()

    /**
     * Saldo do dia em minutos INCLUINDO tempo em andamento.
     */
    fun saldoDiaComAndamentoMinutos(horaAtual: LocalTime = LocalTime.now()): Int {
        return saldoDiaComAndamento(horaAtual).toMinutes().toInt()
    }

    /** Verifica se o dia tem saldo positivo */
    val temSaldoPositivo: Boolean
        get() = !saldoDia.isNegative && !saldoDia.isZero

    /** Verifica se o dia tem saldo negativo */
    val temSaldoNegativo: Boolean
        get() = saldoDia.isNegative

    /** Verifica se a jornada está completa (número par de pontos) */
    val jornadaCompleta: Boolean
        get() = pontos.isNotEmpty() && pontos.size % 2 == 0

    /** Próximo tipo de ponto esperado (true = entrada, false = saída) */
    val proximoIsEntrada: Boolean
        get() = proximoPontoIsEntrada(pontos.size)

    /** Descrição do próximo tipo esperado */
    val proximoTipoDescricao: String
        get() = proximoPontoDescricao(pontos.size)

    // ========================================================================
    // PROPRIEDADES DE DIAS ESPECIAIS
    // ========================================================================

    /** Verifica se é um dia com jornada zerada (não gera débito) */
    val isJornadaZerada: Boolean
        get() = tipoDiaEspecial.zeraJornada

    /** Verifica se é um dia de feriado (inclui ponte e facultativo) */
    val isFeriado: Boolean
        get() = tipoDiaEspecial.isTipoFeriado

    /** Verifica se é um dia de férias */
    val isFerias: Boolean
        get() = tipoDiaEspecial == TipoDiaEspecial.FERIAS

    /** Verifica se é um dia de folga */
    val isFolga: Boolean
        get() = tipoDiaEspecial == TipoDiaEspecial.FOLGA

    /** Verifica se é um dia de falta injustificada */
    val isFaltaInjustificada: Boolean
        get() = tipoDiaEspecial == TipoDiaEspecial.FALTA_INJUSTIFICADA

    /** Verifica se é um dia de falta justificada */
    val isFaltaJustificada: Boolean
        get() = tipoDiaEspecial == TipoDiaEspecial.FALTA_JUSTIFICADA

    /** Verifica se é um dia de atestado/declaração */
    val isAtestado: Boolean
        get() = tipoDiaEspecial == TipoDiaEspecial.ATESTADO

    /** Verifica se é um dia especial (não normal) */
    val isDiaEspecial: Boolean
        get() = tipoDiaEspecial != TipoDiaEspecial.NORMAL

    /**
     * Verifica se há inconsistência de ponto aberto em dia passado.
     */
    val temInconsistenciaPontoAberto: Boolean
        get() {
            if (pontos.isEmpty()) return false
            val hoje = LocalDate.now()
            val temPontoAberto = pontos.size % 2 != 0
            return temPontoAberto && data.isBefore(hoje)
        }

    // ========================================================================
    // PROPRIEDADES PARA HISTÓRICO
    // ========================================================================

    /** Quantidade de pontos registrados */
    val quantidadePontos: Int
        get() = pontos.size

    /** Primeiro ponto do dia */
    val primeiroPonto: Ponto?
        get() = pontos.minByOrNull { it.dataHora }

    /** Último ponto do dia */
    val ultimoPonto: Ponto?
        get() = pontos.maxByOrNull { it.dataHora }

    /** Calcula minutos de intervalo total (soma de todas as pausas consideradas) */
    val minutosIntervaloTotal: Int
        get() = intervalos
            .mapNotNull { it.pausaConsideradaMinutos }
            .sum()

    /** Calcula minutos de intervalo real */
    val minutosIntervaloReal: Int
        get() = intervalos
            .mapNotNull { it.pausaAntesMinutos }
            .sum()

    /**
     * Verifica se o dia tem problemas.
     */
    val temProblemas: Boolean
        get() {
            if (temInconsistenciaPontoAberto) return true
            if (!jornadaCompleta && pontos.size > 1) return true
            if (pontos.size >= 4 && !tipoDiaEspecial.zeraJornada) {
                val intervaloReal = intervalos.getOrNull(1)?.pausaAntesMinutos ?: 0
                val toleranciaProblema = 10
                if (intervaloReal < intervaloMinimoMinutos - toleranciaProblema) return true
            }
            return false
        }

    /**
     * Status do dia para exibição no histórico.
     */
    val statusDia: StatusDiaResumo
        get() = when {
            // Dias com jornada zerada (feriado, férias, atestado, etc.)
            tipoDiaEspecial.zeraJornada && pontos.isNotEmpty() -> StatusDiaResumo.FERIADO_TRABALHADO
            tipoDiaEspecial.zeraJornada -> StatusDiaResumo.FERIADO
            // Dias com jornada normal (normal, folga, falta injustificada)
            pontos.isEmpty() -> StatusDiaResumo.SEM_REGISTRO
            !jornadaCompleta && pontos.size == 1 && data == LocalDate.now() -> StatusDiaResumo.EM_ANDAMENTO
            !jornadaCompleta -> StatusDiaResumo.INCOMPLETO
            temProblemas -> StatusDiaResumo.COM_PROBLEMAS
            else -> StatusDiaResumo.COMPLETO
        }

    /** Verifica se o dia tem intervalo registrado */
    val temIntervalo: Boolean
        get() = minutosIntervaloReal > 0

    /** Verifica se a tolerância de intervalo foi aplicada */
    val temToleranciaIntervaloAplicada: Boolean
        get() = minutosIntervaloReal != minutosIntervaloTotal && minutosIntervaloTotal > 0

    // ========================================================================
    // FORMATADORES
    // ========================================================================

    /** Horas trabalhadas formatadas (ex: "09h 03min") */
    val horasTrabalhadasFormatadas: String
        get() = horasTrabalhadas.formatarDuracao()

    /** Horas trabalhadas com andamento formatadas */
    fun horasTrabalhadasComAndamentoFormatadas(horaAtual: LocalTime = LocalTime.now()): String {
        return horasTrabalhadasComAndamento(horaAtual).formatarDuracao()
    }

    /** Saldo do dia formatado (ex: "+00h 51min" ou "-01h 30min") */
    val saldoDiaFormatado: String
        get() = saldoDia.formatarSaldo()

    /** Saldo do dia com andamento formatado */
    fun saldoDiaComAndamentoFormatado(horaAtual: LocalTime = LocalTime.now()): String {
        return saldoDiaComAndamento(horaAtual).formatarSaldo()
    }

    /** Carga horária formatada (ex: "08h 00min" ou "00h 00min" para dia especial) */
    val cargaHorariaDiariaFormatada: String
        get() = cargaHorariaEfetiva.formatarDuracao()

    /** Descrição do tipo de dia especial */
    val tipoDiaEspecialDescricao: String
        get() = tipoDiaEspecial.descricao

    // ========================================================================
    // CÁLCULO DOS INTERVALOS
    // ========================================================================

    private fun calcularIntervalos(): List<IntervaloPonto> {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        val lista = mutableListOf<IntervaloPonto>()

        var i = 0
        while (i < pontosOrdenados.size) {
            val entrada = pontosOrdenados.getOrNull(i)
            val saida = pontosOrdenados.getOrNull(i + 1)

            if (entrada != null) {
                val saidaAnterior = if (i >= 2) pontosOrdenados.getOrNull(i - 1) else null

                val pausaAntesMinutos = saidaAnterior?.let {
                    Duration.between(it.dataHora, entrada.dataHora).toMinutes().toInt()
                }

                val pausaConsideradaMinutos = pausaAntesMinutos?.let { pausa ->
                    calcularPausaConsiderada(pausa)
                }

                val horaEntradaConsiderada: LocalDateTime? = if (
                    saidaAnterior != null &&
                    pausaAntesMinutos != null &&
                    pausaConsideradaMinutos != null &&
                    pausaAntesMinutos != pausaConsideradaMinutos
                ) {
                    saidaAnterior.dataHora.plusMinutes(pausaConsideradaMinutos.toLong())
                } else null

                val horaEntradaEfetiva = horaEntradaConsiderada ?: entrada.dataHora
                val duracaoTurno = saida?.let {
                    Duration.between(horaEntradaEfetiva, it.dataHora)
                }

                lista.add(
                    IntervaloPonto(
                        entrada = entrada,
                        saida = saida,
                        duracao = duracaoTurno,
                        pausaAntesMinutos = pausaAntesMinutos,
                        pausaConsideradaMinutos = pausaConsideradaMinutos,
                        intervaloMinimoMinutos = intervaloMinimoMinutos,
                        toleranciaMinutos = toleranciaIntervaloMinutos,
                        horaEntradaConsiderada = horaEntradaConsiderada
                    )
                )
            }
            i += 2
        }
        return lista
    }

    private fun calcularPausaConsiderada(pausaReal: Int): Int {
        val limiteInferior = intervaloMinimoMinutos
        val limiteSuperior = intervaloMinimoMinutos + toleranciaIntervaloMinutos

        return when {
            pausaReal in limiteInferior..limiteSuperior -> intervaloMinimoMinutos
            else -> pausaReal
        }
    }
}

/**
 * Representa um intervalo entre entrada e saída (turno de trabalho).
 */
data class IntervaloPonto(
    val entrada: Ponto,
    val saida: Ponto?,
    val duracao: Duration?,
    val pausaAntesMinutos: Int? = null,
    val pausaConsideradaMinutos: Int? = null,
    val intervaloMinimoMinutos: Int? = null,
    val toleranciaMinutos: Int? = null,
    val horaEntradaConsiderada: LocalDateTime? = null
) {
    val aberto: Boolean get() = saida == null

    val duracaoMinutos: Int?
        get() = duracao?.toMinutes()?.toInt()

    val temPausaAntes: Boolean
        get() = pausaAntesMinutos != null && pausaAntesMinutos > 0

    val toleranciaAplicada: Boolean
        get() = pausaAntesMinutos != null &&
                pausaConsideradaMinutos != null &&
                pausaAntesMinutos != pausaConsideradaMinutos

    val isIntervaloAlmoco: Boolean
        get() = pausaAntesMinutos != null &&
                intervaloMinimoMinutos != null &&
                pausaAntesMinutos >= intervaloMinimoMinutos

    val temHoraEntradaConsiderada: Boolean
        get() = horaEntradaConsiderada != null

    fun formatarDuracao(): String {
        return duracaoMinutos?.minutosParaTurno() ?: "Em andamento..."
    }

    fun formatarDuracaoCompacta(): String {
        return duracaoMinutos?.minutosParaDuracaoCompacta() ?: "..."
    }

    fun formatarPausaAntes(): String? {
        return pausaAntesMinutos?.minutosParaIntervalo()
    }

    fun formatarPausaConsiderada(): String? {
        return pausaConsideradaMinutos?.minutosParaIntervalo()
    }

    fun formatarPausaAntesCompacta(): String? {
        return pausaAntesMinutos?.minutosParaDuracaoCompacta()
    }

    fun formatarPausaConsideradaCompacta(): String? {
        return pausaConsideradaMinutos?.minutosParaDuracaoCompacta()
    }
}
