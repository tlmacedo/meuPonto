// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ResumoDia.kt
package br.com.tlmacedo.meuponto.domain.model

import br.com.tlmacedo.meuponto.util.formatarDuracao
import br.com.tlmacedo.meuponto.util.formatarSaldo
import br.com.tlmacedo.meuponto.util.minutosParaDuracaoCompacta
import br.com.tlmacedo.meuponto.util.minutosParaIntervalo
import br.com.tlmacedo.meuponto.util.minutosParaSaldoFormatado
import br.com.tlmacedo.meuponto.util.minutosParaTurno
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo que representa o resumo de um dia de trabalho.
 *
 * ARQUITETURA:
 * - Os intervalos (turnos) são a fonte única de verdade para cálculos
 * - `horasTrabalhadas` é calculado a partir da soma das durações dos intervalos
 * - Isso garante consistência entre o que é exibido nos cards e o resumo
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Removida dependência de TipoPonto
 * @updated 2.4.0 - Adicionado cálculo de pausas entre turnos
 * @updated 2.8.0 - Adicionada configuração de intervalo para tolerância
 * @updated 2.9.0 - Adicionado cálculo de hora de entrada considerada com tolerância
 * @updated 2.10.0 - Corrigido cálculo de duração do turno usando hora considerada
 * @updated 2.11.0 - Refatorado: horasTrabalhadas calculado a partir dos intervalos (single source of truth)
 */
data class ResumoDia(
    val data: LocalDate,
    val pontos: List<Ponto> = emptyList(),
    val cargaHorariaDiaria: Duration = Duration.ofHours(8),
    val intervaloMinimoMinutos: Int = 60,
    val toleranciaIntervaloMinutos: Int = 15
) {

    /** Lista de intervalos entre pontos de entrada e saída (FONTE ÚNICA DE VERDADE) */
    val intervalos: List<IntervaloPonto> by lazy {
        calcularIntervalos()
    }

    /**
     * Total de horas trabalhadas (CALCULADO A PARTIR DOS INTERVALOS).
     * Isso garante consistência com o que é exibido nos cards de turno.
     */
    val horasTrabalhadas: Duration by lazy {
        intervalos
            .mapNotNull { it.duracao }
            .fold(Duration.ZERO) { acc, duracao -> acc.plus(duracao) }
    }

    /** Horas trabalhadas em minutos */
    val horasTrabalhadasMinutos: Int
        get() = horasTrabalhadas.toMinutes().toInt()

    /** Carga horária diária em minutos */
    val cargaHorariaDiariaMinutos: Int
        get() = cargaHorariaDiaria.toMinutes().toInt()

    /** Saldo do dia (positivo = hora extra, negativo = deve horas) */
    val saldoDia: Duration
        get() = horasTrabalhadas.minus(cargaHorariaDiaria)

    /** Saldo do dia em minutos */
    val saldoDiaMinutos: Int
        get() = saldoDia.toMinutes().toInt()

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
    // FORMATADORES
    // ========================================================================

    /** Horas trabalhadas formatadas (ex: "09h 03min") */
    val horasTrabalhadasFormatadas: String
        get() = horasTrabalhadas.formatarDuracao()

    /** Saldo do dia formatado (ex: "+00h 51min" ou "-01h 30min") */
    val saldoDiaFormatado: String
        get() = saldoDia.formatarSaldo()

    /** Carga horária formatada (ex: "08h 00min") */
    val cargaHorariaDiariaFormatada: String
        get() = cargaHorariaDiaria.formatarDuracao()

    // ========================================================================
    // CÁLCULO DOS INTERVALOS (LÓGICA CENTRALIZADA)
    // ========================================================================

    /**
     * Calcula os intervalos (turnos) do dia com todas as tolerâncias aplicadas.
     * Esta é a FONTE ÚNICA DE VERDADE para todos os cálculos de tempo.
     */
    private fun calcularIntervalos(): List<IntervaloPonto> {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        val lista = mutableListOf<IntervaloPonto>()

        var i = 0
        while (i < pontosOrdenados.size) {
            val entrada = pontosOrdenados.getOrNull(i)
            val saida = pontosOrdenados.getOrNull(i + 1)

            if (entrada != null) {
                // Calcular pausa antes deste turno (tempo desde a saída anterior)
                val saidaAnterior = if (i >= 2) pontosOrdenados.getOrNull(i - 1) else null

                val pausaAntesMinutos = saidaAnterior?.let {
                    Duration.between(it.dataHora, entrada.dataHora).toMinutes().toInt()
                }

                // Calcular pausa considerada (com tolerância)
                val pausaConsideradaMinutos = pausaAntesMinutos?.let { pausa ->
                    calcularPausaConsiderada(pausa)
                }

                // Calcular hora de entrada considerada quando tolerância é aplicada
                val horaEntradaConsiderada: LocalDateTime? = if (
                    saidaAnterior != null &&
                    pausaAntesMinutos != null &&
                    pausaConsideradaMinutos != null &&
                    pausaAntesMinutos != pausaConsideradaMinutos
                ) {
                    // Entrada considerada = saída anterior + intervalo considerado
                    saidaAnterior.dataHora.plusMinutes(pausaConsideradaMinutos.toLong())
                } else null

                // Calcular duração do turno usando a hora de entrada EFETIVA
                // (hora considerada se existir, senão hora real)
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

    /**
     * Calcula a pausa considerada aplicando a tolerância.
     *
     * Regra: Se a pausa real estiver dentro do intervalo
     * [intervaloMinimo, intervaloMinimo + tolerancia], considera como intervaloMinimo.
     *
     * Exemplos (com intervalo=60min e tolerância=15min):
     * - Pausa de 55min → considera 55min (abaixo do mínimo)
     * - Pausa de 60min → considera 60min (exato)
     * - Pausa de 70min → considera 60min (dentro da tolerância)
     * - Pausa de 75min → considera 60min (exato na tolerância)
     * - Pausa de 80min → considera 80min (fora da tolerância)
     */
    private fun calcularPausaConsiderada(pausaReal: Int): Int {
        val limiteInferior = intervaloMinimoMinutos
        val limiteSuperior = intervaloMinimoMinutos + toleranciaIntervaloMinutos

        return when {
            // Pausa dentro da tolerância → considera como intervalo mínimo
            pausaReal in limiteInferior..limiteSuperior -> intervaloMinimoMinutos
            // Fora da tolerância → usa valor real
            else -> pausaReal
        }
    }
}

/**
 * Representa um intervalo entre entrada e saída (turno de trabalho).
 *
 * @property entrada Ponto de entrada do turno
 * @property saida Ponto de saída do turno (null se ainda aberto)
 * @property duracao Duração do turno (calculada com hora efetiva)
 * @property pausaAntesMinutos Tempo de pausa real antes deste turno
 * @property pausaConsideradaMinutos Tempo de pausa considerado (após tolerância)
 * @property intervaloMinimoMinutos Intervalo mínimo configurado
 * @property toleranciaMinutos Tolerância de intervalo configurada
 * @property horaEntradaConsiderada Hora de entrada ajustada pela tolerância do intervalo
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.4.0 - Adicionada pausaAntesMinutos e novos formatadores
 * @updated 2.8.0 - Adicionada tolerância de intervalo
 * @updated 2.9.0 - Adicionada horaEntradaConsiderada
 * @updated 2.10.0 - Duração calculada com hora efetiva
 * @updated 2.11.0 - Padronizado formato "00h 00min"
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
    /** Verifica se o intervalo está aberto (sem saída) */
    val aberto: Boolean get() = saida == null

    /** Duração em minutos */
    val duracaoMinutos: Int?
        get() = duracao?.toMinutes()?.toInt()

    /** Verifica se tem pausa antes (intervalo entre turnos) */
    val temPausaAntes: Boolean
        get() = pausaAntesMinutos != null && pausaAntesMinutos > 0

    /**
     * Verifica se a tolerância foi aplicada (pausa real ≠ pausa considerada).
     */
    val toleranciaAplicada: Boolean
        get() = pausaAntesMinutos != null &&
                pausaConsideradaMinutos != null &&
                pausaAntesMinutos != pausaConsideradaMinutos

    /**
     * Verifica se é intervalo de almoço (>= intervalo mínimo).
     * Usado para escolher o ícone: almoço (🍽️) ou café (☕).
     */
    val isIntervaloAlmoco: Boolean
        get() = pausaAntesMinutos != null &&
                intervaloMinimoMinutos != null &&
                pausaAntesMinutos >= intervaloMinimoMinutos

    /**
     * Verifica se a entrada tem hora considerada diferente da hora real.
     */
    val temHoraEntradaConsiderada: Boolean
        get() = horaEntradaConsiderada != null

    // ========================================================================
    // FORMATADORES (padrão "00h 00min")
    // ========================================================================

    /**
     * Formata a duração do turno.
     * @return String formatada (ex: "Turno de 05h 04min")
     */
    fun formatarDuracao(): String {
        return duracaoMinutos?.minutosParaTurno() ?: "Em andamento..."
    }

    /**
     * Formata a duração do turno de forma compacta.
     * @return String formatada (ex: "05h 04min")
     */
    fun formatarDuracaoCompacta(): String {
        return duracaoMinutos?.minutosParaDuracaoCompacta() ?: "..."
    }

    /**
     * Formata a pausa real antes do turno.
     * @return String formatada (ex: "Intervalo de 01h 14min") ou null se não houver pausa
     */
    fun formatarPausaAntes(): String? {
        return pausaAntesMinutos?.minutosParaIntervalo()
    }

    /**
     * Formata a pausa considerada (após tolerância).
     * @return String formatada (ex: "Intervalo de 01h 00min") ou null se não houver pausa
     */
    fun formatarPausaConsiderada(): String? {
        return pausaConsideradaMinutos?.minutosParaIntervalo()
    }

    /**
     * Formata a pausa antes de forma compacta.
     * @return String formatada (ex: "01h 14min") ou null se não houver pausa
     */
    fun formatarPausaAntesCompacta(): String? {
        return pausaAntesMinutos?.minutosParaDuracaoCompacta()
    }

    /**
     * Formata a pausa considerada de forma compacta.
     * @return String formatada (ex: "01h 00min") ou null se não houver pausa
     */
    fun formatarPausaConsideradaCompacta(): String? {
        return pausaConsideradaMinutos?.minutosParaDuracaoCompacta()
    }
}
