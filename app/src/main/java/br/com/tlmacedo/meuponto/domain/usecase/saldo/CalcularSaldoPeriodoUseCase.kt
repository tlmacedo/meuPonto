package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.HorarioPadraoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Caso de uso para calcular o saldo de horas de um período.
 */
class CalcularSaldoPeriodoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val horarioPadraoRepository: HorarioPadraoRepository
) {
    data class SaldoPeriodo(
        val dataInicio: LocalDate,
        val dataFim: LocalDate,
        val totalTrabalhadoMinutos: Long,
        val totalEsperadoMinutos: Long,
        val saldoMinutos: Long,
        val diasTrabalhados: Int,
        val diasUteis: Int
    ) {
        val saldoFormatado: String
            get() {
                val horas = kotlin.math.abs(saldoMinutos) / 60
                val minutos = kotlin.math.abs(saldoMinutos) % 60
                val sinal = if (saldoMinutos >= 0) "+" else "-"
                return "$sinal${horas}h${minutos}min"
            }

        val totalTrabalhadoFormatado: String
            get() {
                val horas = totalTrabalhadoMinutos / 60
                val minutos = totalTrabalhadoMinutos % 60
                return "${horas}h${minutos}min"
            }
    }

    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): SaldoPeriodo {
        val pontos = pontoRepository.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
        val pontosPorDia = pontos.groupBy { it.data }

        var totalTrabalhado = 0L
        var totalEsperado = 0L
        var diasTrabalhados = 0
        var diasUteis = 0

        var dataAtual = dataInicio
        while (!dataAtual.isAfter(dataFim)) {
            val isDiaUtil = isDiaUtil(dataAtual)
            if (isDiaUtil) {
                diasUteis++
                totalEsperado += getJornadaDiaria(empregoId, dataAtual)
            }

            val pontosDoDia = pontosPorDia[dataAtual]
            if (!pontosDoDia.isNullOrEmpty()) {
                diasTrabalhados++
                totalTrabalhado += calcularTempoTrabalhado(pontosDoDia)
            }

            dataAtual = dataAtual.plusDays(1)
        }

        return SaldoPeriodo(
            dataInicio = dataInicio,
            dataFim = dataFim,
            totalTrabalhadoMinutos = totalTrabalhado,
            totalEsperadoMinutos = totalEsperado,
            saldoMinutos = totalTrabalhado - totalEsperado,
            diasTrabalhados = diasTrabalhados,
            diasUteis = diasUteis
        )
    }

    private fun calcularTempoTrabalhado(pontos: List<Ponto>): Long {
        if (pontos.isEmpty()) return 0L

        val pontosOrdenados = pontos.sortedBy { it.hora }
        var totalMinutos = 0L
        var i = 0

        while (i < pontosOrdenados.size - 1) {
            val entrada = pontosOrdenados[i]
            val saida = pontosOrdenados[i + 1]

            if (entrada.tipo == TipoPonto.ENTRADA && saida.tipo == TipoPonto.SAIDA) {
                totalMinutos += ChronoUnit.MINUTES.between(entrada.hora, saida.hora)
            }
            i += 2
        }

        return totalMinutos
    }

    private fun isDiaUtil(data: LocalDate): Boolean {
        return data.dayOfWeek != DayOfWeek.SATURDAY && data.dayOfWeek != DayOfWeek.SUNDAY
    }

    private suspend fun getJornadaDiaria(empregoId: Long, data: LocalDate): Long {
        val horarioPadrao = horarioPadraoRepository.buscarPorEmpregoEDiaSemana(
            empregoId,
            data.dayOfWeek.value
        )
        return horarioPadrao?.jornadaMinutos?.toLong() ?: 480L
    }
}
