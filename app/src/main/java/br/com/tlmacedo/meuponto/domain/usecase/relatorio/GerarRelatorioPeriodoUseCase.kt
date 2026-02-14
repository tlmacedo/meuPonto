package br.com.tlmacedo.meuponto.domain.usecase.relatorio

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.HorarioPadraoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Caso de uso para gerar relatório de um período personalizado.
 */
class GerarRelatorioPeriodoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val horarioPadraoRepository: HorarioPadraoRepository
) {
    data class RelatorioPeriodo(
        val empregoId: Long,
        val dataInicio: LocalDate,
        val dataFim: LocalDate,
        val dias: List<DiaRelatorio>,
        val totalTrabalhadoMinutos: Long,
        val totalEsperadoMinutos: Long,
        val saldoMinutos: Long,
        val diasTrabalhados: Int,
        val diasUteis: Int,
        val mediaHorasDia: Long
    )

    data class DiaRelatorio(
        val data: LocalDate,
        val pontos: List<Ponto>,
        val trabalhadoMinutos: Long,
        val esperadoMinutos: Long,
        val saldoMinutos: Long,
        val isDiaUtil: Boolean
    )

    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): RelatorioPeriodo {
        val pontos = pontoRepository.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
        val pontosPorDia = pontos.groupBy { it.data }

        val dias = mutableListOf<DiaRelatorio>()
        var dataAtual = dataInicio

        while (!dataAtual.isAfter(dataFim)) {
            val pontosDoDia = pontosPorDia[dataAtual] ?: emptyList()
            val isDiaUtil = isDiaUtil(dataAtual)
            val trabalhadoMinutos = calcularTempoTrabalhado(pontosDoDia)
            val esperadoMinutos = if (isDiaUtil) getJornadaDiaria(empregoId, dataAtual) else 0L

            dias.add(
                DiaRelatorio(
                    data = dataAtual,
                    pontos = pontosDoDia.sortedBy { it.hora },
                    trabalhadoMinutos = trabalhadoMinutos,
                    esperadoMinutos = esperadoMinutos,
                    saldoMinutos = trabalhadoMinutos - esperadoMinutos,
                    isDiaUtil = isDiaUtil
                )
            )

            dataAtual = dataAtual.plusDays(1)
        }

        val totalTrabalhado = dias.sumOf { it.trabalhadoMinutos }
        val totalEsperado = dias.sumOf { it.esperadoMinutos }
        val diasTrabalhados = dias.count { it.pontos.isNotEmpty() }
        val diasUteis = dias.count { it.isDiaUtil }
        val mediaHorasDia = if (diasTrabalhados > 0) totalTrabalhado / diasTrabalhados else 0L

        return RelatorioPeriodo(
            empregoId = empregoId,
            dataInicio = dataInicio,
            dataFim = dataFim,
            dias = dias,
            totalTrabalhadoMinutos = totalTrabalhado,
            totalEsperadoMinutos = totalEsperado,
            saldoMinutos = totalTrabalhado - totalEsperado,
            diasTrabalhados = diasTrabalhados,
            diasUteis = diasUteis,
            mediaHorasDia = mediaHorasDia
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
