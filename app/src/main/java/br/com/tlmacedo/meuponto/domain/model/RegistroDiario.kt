package br.com.tlmacedo.meuponto.domain.model

import java.time.Duration
import java.time.LocalDate

/**
 * Modelo que representa o resumo de um dia de trabalho.
 *
 * Agrupa todos os pontos de um dia e calcula as métricas relacionadas
 * como horas trabalhadas, saldo do dia, etc.
 *
 * @property data Data do registro
 * @property pontos Lista de pontos do dia ordenados por hora
 * @property cargaHorariaEsperada Carga horária esperada em minutos
 *
 * @author Thiago
 * @since 1.0.0
 */
data class RegistroDiario(
    val data: LocalDate,
    val pontos: List<Ponto> = emptyList(),
    val cargaHorariaEsperada: Int = 480
) {
    /**
     * Retorna os pontos ordenados por hora.
     */
    val pontosOrdenados: List<Ponto>
        get() = pontos.sortedBy { it.dataHora }

    /**
     * Entrada do dia (primeiro ponto do tipo ENTRADA).
     */
    val entrada: Ponto?
        get() = pontosOrdenados.firstOrNull { it.tipo == TipoPonto.ENTRADA }

    /**
     * Saída para almoço.
     */
    val saidaAlmoco: Ponto?
        get() = pontosOrdenados.firstOrNull { it.tipo == TipoPonto.SAIDA_ALMOCO }

    /**
     * Retorno do almoço.
     */
    val retornoAlmoco: Ponto?
        get() = pontosOrdenados.firstOrNull { it.tipo == TipoPonto.RETORNO_ALMOCO }

    /**
     * Saída do dia (último ponto do tipo SAIDA).
     */
    val saida: Ponto?
        get() = pontosOrdenados.lastOrNull { it.tipo == TipoPonto.SAIDA }

    /**
     * Calcula o total de minutos trabalhados no dia.
     *
     * @return Total de minutos trabalhados, ou null se não houver pontos suficientes
     */
    fun calcularMinutosTrabalhados(): Int? {
        if (entrada != null && saida != null && saidaAlmoco == null && retornoAlmoco == null) {
            return Duration.between(entrada!!.dataHora, saida!!.dataHora).toMinutes().toInt()
        }

        if (entrada != null && saidaAlmoco != null && retornoAlmoco != null && saida != null) {
            val primeiroTurno = Duration.between(entrada!!.dataHora, saidaAlmoco!!.dataHora).toMinutes()
            val segundoTurno = Duration.between(retornoAlmoco!!.dataHora, saida!!.dataHora).toMinutes()
            return (primeiroTurno + segundoTurno).toInt()
        }

        if (entrada != null && saidaAlmoco != null && retornoAlmoco == null) {
            return Duration.between(entrada!!.dataHora, saidaAlmoco!!.dataHora).toMinutes().toInt()
        }

        if (entrada != null && saidaAlmoco != null && retornoAlmoco != null && saida == null) {
            val primeiroTurno = Duration.between(entrada!!.dataHora, saidaAlmoco!!.dataHora).toMinutes()
            return primeiroTurno.toInt()
        }

        return null
    }

    /**
     * Calcula o saldo do dia em minutos.
     *
     * @return Saldo em minutos (positivo = crédito, negativo = débito), ou null se não calculável
     */
    fun calcularSaldoMinutos(): Int? {
        val trabalhado = calcularMinutosTrabalhados() ?: return null
        return trabalhado - cargaHorariaEsperada
    }

    /**
     * Verifica se o dia está completo (todos os 4 pontos registrados).
     */
    val diaCompleto: Boolean
        get() = entrada != null && saidaAlmoco != null && retornoAlmoco != null && saida != null

    /**
     * Retorna o próximo tipo de ponto esperado.
     */
    val proximoPontoEsperado: TipoPonto
        get() = TipoPonto.proximoTipo(pontosOrdenados.lastOrNull()?.tipo)
}
