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
import kotlin.math.abs

/**
 * Status simplificado do dia para exibição no histórico.
 *
 * @author Thiago
 * @since 3.0.0
 */
enum class StatusDiaResumo(val descricao: String, val isConsistente: Boolean) {
    DESCANSO("Descanso", true),
    COMPLETO("Completo", true),
    EM_ANDAMENTO("Em andamento", true),
    INCOMPLETO("Incompleto", false),
    COM_PROBLEMAS("Com problemas", false),
    SEM_REGISTRO("Sem registro", true),
    FERIADO("Feriado", true),
    FERIADO_TRABALHADO("Feriado trabalhado", true),
    FUTURO("Futuro", true)
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
    /** Dia de descanso semanal */
    DESCANSO("Descanso", "😴"),

    /** Dia normal de trabalho */
    NORMAL("Dia normal", "📅"),

    /** Feriado oficial (nacional/estadual/municipal) - jornada zerada */
    FERIADO("Feriado", "🎉"),

    /** Ponte (dia entre feriado e fim de semana) - jornada zerada */
    PONTE("Ponte", "⛱️"),

    /** Ponto facultativo - jornada zerada */
    FACULTATIVO("Ponto Facultativo", "📋"),

    /** Férias - jornada zerada */
    FERIAS("Férias", "🏖️"),

    /** Atestado/Declaração (ausência justificada) - jornada zerada */
    ATESTADO("Atestado", "🏥"),

    /** Falta justificada - jornada zerada */
    FALTA_JUSTIFICADA("Falta Justificada", "📝"),

    /** Folga - jornada normal (gera débito) */
    FOLGA("Folga", "🏴‍☠️"),

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
            DESCANSO,
            FERIADO,
            PONTE,
            FACULTATIVO,
            FERIAS,
            ATESTADO,
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

    /**
     * Verifica se este tipo representa uma ausência (afastamento, folga ou falta).
     */
    val isAusencia: Boolean
        get() = this in listOf(
            FERIAS,
            ATESTADO,
            FALTA_JUSTIFICADA,
            FOLGA,
            FALTA_INJUSTIFICADA
        )
}

/**
 * Modelo que representa o resumo de um dia de trabalho.
 *
 * ARQUITETURA:
 * - Os intervalos (turnos) são a fonte única de verdade para cálculos
 * - `horasTrabalhadas` é calculado a partir da soma das durações dos intervalos
 * - `tipoDiaEspecial` define o comportamento do cálculo
 * - Suporte a tempo em andamento (turno aberto) para cálculos em tempo real
 * - Suporte a tempo abonado (declaração/atestado parcial)
 *
 * REGRAS DE CÁLCULO:
 * - Dias futuros: saldo = 0 (não calculado)
 * - Jornada zerada: saldo = trabalhado (hora extra)
 * - Jornada normal: saldo = trabalhado + abonado - jornada (pode ser negativo)
 *
 * TOLERÂNCIA DE INTERVALO:
 * - A tolerância é aplicada APENAS UMA VEZ por dia
 * - É aplicada na pausa cujo horário de saída (início da pausa) seja mais próximo
 *   do `saidaIntervaloIdeal` configurado
 * - Se não houver `saidaIntervaloIdeal`, aplica na primeira pausa elegível
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 4.0.0 - Adicionado suporte a dias especiais
 * @updated 4.1.0 - Adicionado cálculo com tempo em andamento
 * @updated 4.2.0 - Tolerância de intervalo aplicada apenas uma vez (na pausa mais próxima do horário padrão)
 * @updated 5.5.0 - Adicionado tempoAbonadoMinutos para declarações/atestados parciais
 * @updated 6.5.0 - Dias futuros não têm saldo calculado (saldo = 0)
 */
data class ResumoDia(
    val data: LocalDate,
    val pontos: List<Ponto> = emptyList(),
    val cargaHorariaDiaria: Duration = Duration.ofHours(8),
    val intervaloMinimoMinutos: Int = 60,
    val toleranciaIntervaloMinutos: Int = 15,
    val tipoDiaEspecial: TipoDiaEspecial = TipoDiaEspecial.NORMAL,
    /** Horário ideal de saída para intervalo (almoço) - usado para determinar qual pausa recebe tolerância */
    val saidaIntervaloIdeal: LocalTime? = null,
    /** Tempo abonado por declaração/atestado parcial (em minutos) - somado ao saldo */
    val tempoAbonadoMinutos: Int = 0
) {

    /** Lista de intervalos entre pontos de entrada e saída (FONTE ÚNICA DE VERDADE) */
    val intervalos: List<IntervaloPonto> by lazy {
        calcularIntervalos()
    }

    // ========================================================================
    // VERIFICAÇÃO DE DATA FUTURA
    // ========================================================================

    /**
     * Verifica se este dia é futuro (após hoje).
     * Dias futuros não têm saldo calculado.
     */
    val isFuturo: Boolean
        get() = data.isAfter(LocalDate.now())

    /**
     * Verifica se este dia é hoje.
     */
    val isHoje: Boolean
        get() = data == LocalDate.now()

    /**
     * Verifica se este dia é passado (antes de hoje).
     */
    val isPassado: Boolean
        get() = data.isBefore(LocalDate.now())

    /**
     * Verifica se existe algum registro no dia (ponto, ausência, declaração ou feriado)
     * que justifique a contabilização de horas e saldo.
     */
    val temRegistro: Boolean
        get() = pontos.isNotEmpty() || tipoDiaEspecial.isAusencia || tipoDiaEspecial.isTipoFeriado || tempoAbonadoMinutos > 0

    // ========================================================================
    // TURNO ABERTO E TEMPO EM ANDAMENTO
    // ========================================================================

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
            pontos.sortedBy { it.dataHoraEfetiva }.lastOrNull()?.dataHoraEfetiva
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

    // ========================================================================
    // HORAS TRABALHADAS
    // ========================================================================

    /**
     * Total de horas trabalhadas (CALCULADO A PARTIR DOS INTERVALOS FECHADOS).
     * NÃO inclui o tempo em andamento de turnos abertos.
     * NÃO inclui o tempo abonado (que é somado apenas no saldo).
     *
     * Regra: Só começa a contabilizar se tiver no mínimo um registro no dia.
     */
    val horasTrabalhadas: Duration by lazy {
        if (!temRegistro) {
            Duration.ZERO
        } else {
            intervalos
                .mapNotNull { it.duracao }
                .fold(Duration.ZERO) { acc, duracao -> acc.plus(duracao) }
        }
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

    // ========================================================================
    // CARGA HORÁRIA
    // ========================================================================

    /** Carga horária diária em minutos (configurada na versão de jornada) */
    val cargaHorariaDiariaMinutos: Int
        get() = cargaHorariaDiaria.toMinutes().toInt()

    /**
     * Carga horária efetiva do dia (usada no cálculo de saldo).
     *
     * - Dias futuros: 0h (não há cálculo)
     * - Jornada zerada (FERIADO, PONTE, FACULTATIVO, FERIAS, ATESTADO, FALTA_JUSTIFICADA): 0h
     * - Jornada normal (NORMAL, FOLGA, FALTA_INJUSTIFICADA): carga configurada
     */
    val cargaHorariaEfetiva: Duration
        get() = when {
            isFuturo -> Duration.ZERO
            tipoDiaEspecial.zeraJornada -> Duration.ZERO
            else -> cargaHorariaDiaria
        }

    /** Carga horária efetiva em minutos */
    val cargaHorariaEfetivaMinutos: Int
        get() = cargaHorariaEfetiva.toMinutes().toInt()

    /** Tempo abonado como Duration */
    val tempoAbonado: Duration
        get() = Duration.ofMinutes(tempoAbonadoMinutos.toLong())

    /** Verifica se há tempo abonado */
    val temTempoAbonado: Boolean
        get() = tempoAbonadoMinutos > 0

    // ========================================================================
    // SALDO DO DIA
    // ========================================================================

    /**
     * Saldo do dia (positivo = hora extra, negativo = deve horas).
     * NÃO inclui tempo em andamento.
     *
     * REGRAS:
     * - Dias futuros: saldo = 0 (não calculado)
     * - Sem registros: saldo = 0 (só começa a contabilizar se tiver registro de ponto, ausência ou feriado)
     * - Jornada zerada: saldo = trabalhado + abonado - 0 = trabalhado + abonado
     * - Jornada normal: saldo = trabalhado + abonado - jornada (pode ser negativo)
     */
    val saldoDia: Duration
        get() {
            // Dias futuros e sem registros não têm saldo calculado
            if (isFuturo || !temRegistro) return Duration.ZERO

            return horasTrabalhadas.plus(tempoAbonado).minus(cargaHorariaEfetiva)
        }

    /**
     * Saldo do dia INCLUINDO tempo em andamento.
     * Use esta propriedade para exibição em tempo real na UI.
     *
     * Para dias futuros ou sem registros, retorna sempre ZERO.
     */
    fun saldoDiaComAndamento(horaAtual: LocalTime = LocalTime.now()): Duration {
        // Dias futuros e sem registros não têm saldo calculado
        if (isFuturo || !temRegistro) return Duration.ZERO

        return horasTrabalhadasComAndamento(horaAtual).plus(tempoAbonado).minus(cargaHorariaEfetiva)
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
        get() = !isFuturo && !saldoDia.isNegative && !saldoDia.isZero

    /** Verifica se o dia tem saldo negativo */
    val temSaldoNegativo: Boolean
        get() = !isFuturo && saldoDia.isNegative

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
        get() = isFuturo || tipoDiaEspecial.zeraJornada

    val isDescanso: Boolean
        get() = tipoDiaEspecial == TipoDiaEspecial.DESCANSO

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
     * Lista de problemas/inconsistências encontrados no dia.
     */
    val listaInconsistencias: List<String>
        get() {
            if (isFuturo) return emptyList()
            val inconsistencias = mutableListOf<String>()

            if (temInconsistenciaPontoAberto) {
                inconsistencias.add("Ponto em aberto em dia passado")
            }

            if (!jornadaCompleta && pontos.size > 1) {
                inconsistencias.add("Jornada incompleta (${pontos.size} pontos registrados)")
            }

            if (pontos.size >= 4 && !tipoDiaEspecial.zeraJornada) {
                val pausaPrincipal = intervalos.find { it.isPausaPrincipal }
                val pausaPrincipalMinutos = pausaPrincipal?.pausaAntesMinutos ?: 0

                if (pausaPrincipalMinutos < (intervaloMinimoMinutos - 10)) {
                    inconsistencias.add("Intervalo principal não atingiu o mínimo de ${intervaloMinimoMinutos.minutosParaIntervalo()} (pausa: ${pausaPrincipalMinutos.minutosParaIntervalo()})")
                }
            }

            return inconsistencias
        }

    /**
     * Verifica se o dia tem problemas.
     */
    val temProblemas: Boolean
        get() = listaInconsistencias.isNotEmpty()

    /**
     * Status do dia para exibição no histórico.
     */
    val statusDia: StatusDiaResumo
        get() = when {
            // Dias futuros
            isFuturo -> StatusDiaResumo.FUTURO
            // Dias com jornada zerada (feriado, férias, atestado, etc.)
            tipoDiaEspecial.zeraJornada && pontos.isNotEmpty() -> StatusDiaResumo.FERIADO_TRABALHADO
            tipoDiaEspecial.zeraJornada -> StatusDiaResumo.FERIADO
            // Dia de descanso
            cargaHorariaDiaria == Duration.ZERO -> StatusDiaResumo.DESCANSO
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

    /** Tempo abonado formatado (ex: "2h 30min") */
    val tempoAbonadoFormatado: String
        get() = tempoAbonado.formatarDuracao()

    // ========================================================================
    // CÁLCULO DOS INTERVALOS
    // ========================================================================

    /**
     * Representa uma pausa candidata a receber tolerância.
     */
    private data class PausaCandidata(
        val indice: Int,
        val horaSaidaParaIntervalo: LocalDateTime,
        val pausaRealMinutos: Int
    )

    /**
     * Calcula os intervalos aplicando tolerância APENAS UMA VEZ.
     *
     * REGRA:
     * - Não existe tolerância no primeiro registro do dia (entrada).
     * - A tolerância é aplicada APENAS na volta do intervalo do almoço.
     * - O intervalo do almoço é identificado como aquele que tem duração >= intervaloMinimoMinutos
     *   e cujo horário de saída seja mais próximo do saidaIntervaloIdeal.
     * - Apenas essa pausa recebe a tolerância e é marcada como "pausa principal" (almoço).
     */
    private fun calcularIntervalos(): List<IntervaloPonto> {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }

        // Primeiro passo: coletar informações de todas as pausas
        data class InfoPausa(
            val indice: Int,
            val horaSaidaParaIntervalo: LocalDateTime,
            val pausaRealMinutos: Int,
            val elegivelTolerancia: Boolean
        )

        val infoPausas = mutableListOf<InfoPausa>()
        val limiteInferior = intervaloMinimoMinutos
        val limiteSuperior = intervaloMinimoMinutos + toleranciaIntervaloMinutos

        var i = 0
        var indicePausa = 0
        while (i < pontosOrdenados.size) {
            val entrada = pontosOrdenados.getOrNull(i)
            val saidaAnterior = if (i >= 2) pontosOrdenados.getOrNull(i - 1) else null

            if (entrada != null && saidaAnterior != null) {
                val pausaMinutos =
                    Duration.between(saidaAnterior.dataHora, entrada.dataHora).toMinutes().toInt()

                infoPausas.add(
                    InfoPausa(
                        indice = indicePausa,
                        horaSaidaParaIntervalo = saidaAnterior.dataHora,
                        pausaRealMinutos = pausaMinutos,
                        elegivelTolerancia = pausaMinutos in limiteInferior..limiteSuperior
                    )
                )
                indicePausa++
            }
            i += 2
        }

        // Segundo passo: determinar qual é a pausa principal (almoço)
        // Critérios em ordem de prioridade:
        // 1. Se houver saidaIntervaloIdeal: a pausa mais próxima desse horário (que tenha >= intervaloMinimoMinutos)
        // 2. Se não houver: a pausa com duração mais próxima do intervaloMinimoMinutos (global)
        val indicePausaPrincipal: Int? = if (infoPausas.isNotEmpty()) {
            val pausasLongas =
                infoPausas.filter { it.pausaRealMinutos >= (intervaloMinimoMinutos - toleranciaIntervaloMinutos) }

            if (saidaIntervaloIdeal != null && pausasLongas.isNotEmpty()) {
                // Seleciona a pausa mais próxima do horário ideal (comportamento para dias úteis)
                pausasLongas.minByOrNull { pausa ->
                    val horaSaida = pausa.horaSaidaParaIntervalo.toLocalTime()
                    abs(Duration.between(horaSaida, saidaIntervaloIdeal).toMinutes())
                }?.indice
            } else if (pausasLongas.isNotEmpty()) {
                // Sem horário ideal (ex: Sábado): seleciona a pausa que MAIS SE APROXIMA do intervalo mínimo configurado
                // Isso garante que a tolerância global seja aplicada na pausa que parece ser o almoço
                pausasLongas.minByOrNull { pausa ->
                    abs(pausa.pausaRealMinutos - intervaloMinimoMinutos)
                }?.indice
            } else {
                // Nenhuma pausa na faixa de tolerância: seleciona a maior pausa do dia
                infoPausas.maxByOrNull { it.pausaRealMinutos }?.indice
            }
        } else {
            null
        }

        // A tolerância só é aplicada na pausa principal (se elegivel)
        val indicePausaComTolerancia: Int? = indicePausaPrincipal?.let { idx ->
            val info = infoPausas.find { it.indice == idx }
            if (info?.elegivelTolerancia == true) idx else null
        }

        // Terceiro passo: construir os intervalos
        val lista = mutableListOf<IntervaloPonto>()
        i = 0
        indicePausa = 0

        while (i < pontosOrdenados.size) {
            val entrada = pontosOrdenados.getOrNull(i)
            val saida = pontosOrdenados.getOrNull(i + 1)

            if (entrada != null) {
                val saidaAnterior = if (i >= 2) pontosOrdenados.getOrNull(i - 1) else null

                val pausaAntesMinutos = saidaAnterior?.let {
                    Duration.between(it.dataHora, entrada.dataHora).toMinutes().toInt()
                }

                // Aplica tolerância APENAS se esta for a pausa selecionada
                val deveAplicarTolerancia =
                    saidaAnterior != null && indicePausa == indicePausaComTolerancia

                val pausaConsideradaMinutos = pausaAntesMinutos?.let { pausa ->
                    if (deveAplicarTolerancia && pausa in limiteInferior..limiteSuperior) {
                        intervaloMinimoMinutos
                    } else {
                        pausa
                    }
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

                // Marca se esta é a pausa principal (almoço)
                val isPausaPrincipal = saidaAnterior != null && indicePausa == indicePausaPrincipal

                lista.add(
                    IntervaloPonto(
                        entrada = entrada,
                        saida = saida,
                        duracao = duracaoTurno,
                        pausaAntesMinutos = pausaAntesMinutos,
                        pausaConsideradaMinutos = pausaConsideradaMinutos,
                        intervaloMinimoMinutos = intervaloMinimoMinutos,
                        toleranciaMinutos = toleranciaIntervaloMinutos,
                        horaEntradaConsiderada = horaEntradaConsiderada,
                        isPausaPrincipal = isPausaPrincipal
                    )
                )

                if (saidaAnterior != null) {
                    indicePausa++
                }
            }
            i += 2
        }
        return lista
    }
}

/**
 * Tipo de pausa entre turnos de trabalho.
 *
 * @author Thiago
 * @since 4.2.0
 */
enum class TipoPausa(val descricao: String, val emoji: String) {
    /** Pausa para café (≤ 30 minutos) */
    CAFE("Café", "☕"),

    /** Saída rápida (> 30 minutos, mas não é almoço) */
    SAIDA_RAPIDA("Saída Rápida", "🚶"),

    /** Intervalo de almoço (pausa principal do dia, próxima ao horário configurado) */
    ALMOCO("Almoço", "🍽️")
}

/**
 * Representa um intervalo entre entrada e saída (turno de trabalho).
 *
 * @updated 4.2.0 - Adicionado tipoPausa para classificação correta das pausas
 */
data class IntervaloPonto(
    val entrada: Ponto,
    val saida: Ponto?,
    val duracao: Duration?,
    val pausaAntesMinutos: Int? = null,
    val pausaConsideradaMinutos: Int? = null,
    val intervaloMinimoMinutos: Int? = null,
    val toleranciaMinutos: Int? = null,
    val horaEntradaConsiderada: LocalDateTime? = null,
    /** Indica se esta é a pausa principal (almoço) do dia */
    val isPausaPrincipal: Boolean = false
) {
    companion object {
        /** Limite em minutos para considerar uma pausa como "café" */
        private const val LIMITE_CAFE_MINUTOS = 30
    }

    val aberto: Boolean get() = saida == null

    val duracaoMinutos: Int?
        get() = duracao?.toMinutes()?.toInt()

    val temPausaAntes: Boolean
        get() = pausaAntesMinutos != null && pausaAntesMinutos > 0

    val toleranciaAplicada: Boolean
        get() = pausaAntesMinutos != null &&
                pausaConsideradaMinutos != null &&
                pausaAntesMinutos != pausaConsideradaMinutos

    /**
     * Tipo da pausa baseado na duração e se é a pausa principal.
     *
     * Regras:
     * - ALMOCO: é a pausa principal do dia (próxima ao horário de almoço configurado)
     * - CAFE: ≤ 30 minutos
     * - SAIDA_RAPIDA: > 30 minutos, mas não é a pausa principal
     */
    val tipoPausa: TipoPausa?
        get() {
            val minutos = pausaAntesMinutos ?: return null
            return when {
                isPausaPrincipal -> TipoPausa.ALMOCO
                minutos <= LIMITE_CAFE_MINUTOS -> TipoPausa.CAFE
                else -> TipoPausa.SAIDA_RAPIDA
            }
        }

    /**
     * @deprecated Use tipoPausa em vez disso
     */
    @Deprecated("Use tipoPausa == TipoPausa.ALMOCO", ReplaceWith("tipoPausa == TipoPausa.ALMOCO"))
    val isIntervaloAlmoco: Boolean
        get() = tipoPausa == TipoPausa.ALMOCO

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
