// Arquivo: RegistroDiario.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.Duration
import java.time.LocalDate

/**
 * Modelo que representa o resumo de um dia de trabalho.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Removida dependência de TipoPonto enum
 */
data class RegistroDiario(
    val data: LocalDate,
    val pontos: List<Ponto> = emptyList(),
    val cargaHorariaDiariaMinutos: Int = 480,
    val intervaloMinimoMinutos: Int = 60,
    val toleranciaMinutos: Int = 10,
    val jornadaMaximaMinutos: Int = 600
) {
    /** Retorna os pontos ordenados por data/hora */
    val pontosOrdenados: List<Ponto>
        get() = pontos.sortedBy { it.dataHora }

    /** Quantidade de pontos registrados no dia */
    val quantidadePontos: Int
        get() = pontos.size

    /** Primeiro ponto do dia (primeira entrada - índice 0) */
    val primeiraEntrada: Ponto?
        get() = pontosOrdenados.firstOrNull()

    /** Último ponto do dia */
    val ultimoPonto: Ponto?
        get() = pontosOrdenados.lastOrNull()

    /** Última saída registrada (último índice ímpar) */
    val ultimaSaida: Ponto?
        get() {
            val ordenados = pontosOrdenados
            return if (ordenados.size >= 2 && ordenados.size % 2 == 0) {
                ordenados.lastOrNull()
            } else if (ordenados.size > 2) {
                ordenados.getOrNull(ordenados.size - 2)
            } else null
        }

    /** Verifica se o dia possui o cenário ideal de 4 pontos */
    val isCenarioIdeal: Boolean
        get() = quantidadePontos == PontoConstants.PONTOS_IDEAL

    /** Verifica se a quantidade de pontos é par (consistente) */
    val isQuantidadePar: Boolean
        get() = quantidadePontos % 2 == 0

    /** Verifica se próximo ponto é entrada */
    val proximoIsEntrada: Boolean
        get() = proximoPontoIsEntrada(quantidadePontos)

    /** Descrição do próximo ponto esperado */
    val proximoPontoDescricao: String
        get() = proximoPontoDescricao(quantidadePontos)

    /** Verifica se ainda pode registrar mais pontos */
    val podeRegistrarMaisPontos: Boolean
        get() = quantidadePontos < PontoConstants.MAX_PONTOS

    /**
     * Calcula o total de minutos trabalhados no dia.
     * Soma os períodos entre cada par (índice par = entrada, ímpar = saída).
     */
    fun calcularMinutosTrabalhados(): Int? {
        if (quantidadePontos < PontoConstants.MIN_PONTOS) return null
        if (!isQuantidadePar) return null

        val ordenados = pontosOrdenados
        var totalMinutos = 0L

        var i = 0
        while (i < ordenados.size - 1) {
            val entrada = ordenados[i]      // índice par = entrada
            val saida = ordenados[i + 1]    // índice ímpar = saída
            val duracao = Duration.between(entrada.dataHora, saida.dataHora)
            totalMinutos += duracao.toMinutes()
            i += 2
        }

        return totalMinutos.toInt()
    }

    /**
     * Calcula o total de minutos de intervalo no dia.
     * Soma os períodos entre cada saída (ímpar) e próxima entrada (par).
     */
    fun calcularMinutosIntervalo(): Int? {
        if (quantidadePontos < PontoConstants.PONTOS_IDEAL) return null

        val ordenados = pontosOrdenados
        var totalIntervalo = 0L

        var i = 1
        while (i < ordenados.size - 1) {
            val saida = ordenados[i]        // índice ímpar = saída
            val entrada = ordenados[i + 1]  // próximo índice par = entrada
            val duracao = Duration.between(saida.dataHora, entrada.dataHora)
            totalIntervalo += duracao.toMinutes()
            i += 2
        }

        return totalIntervalo.toInt()
    }

    /**
     * Calcula o saldo do dia em minutos.
     */
    fun calcularSaldoMinutos(): Int? {
        val trabalhado = calcularMinutosTrabalhados() ?: return null
        return trabalhado - cargaHorariaDiariaMinutos
    }

    /**
     * Determina o status de consistência do dia.
     */
    fun determinarStatus(): StatusDia {
        if (pontos.isEmpty()) return StatusDia.SEM_REGISTRO
        if (quantidadePontos > PontoConstants.MAX_PONTOS) return StatusDia.EXCESSO_PONTOS

        if (!isQuantidadePar) {
            return if (quantidadePontos == 1) StatusDia.EM_ANDAMENTO else StatusDia.INCOMPLETO
        }

        val minutosTrabalhados = calcularMinutosTrabalhados() ?: return StatusDia.INCOMPLETO
        if (minutosTrabalhados > jornadaMaximaMinutos) return StatusDia.JORNADA_EXCEDIDA

        if (quantidadePontos >= PontoConstants.PONTOS_IDEAL) {
            val minutosIntervalo = calcularMinutosIntervalo() ?: 0
            val intervaloMinimoComTolerancia = intervaloMinimoMinutos - toleranciaMinutos
            if (minutosIntervalo < intervaloMinimoComTolerancia) {
                return StatusDia.INTERVALO_INSUFICIENTE
            }
        }

        return if (quantidadePontos == 2) StatusDia.COMPLETO_SEM_INTERVALO else StatusDia.COMPLETO
    }
}
