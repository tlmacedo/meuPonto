package br.com.tlmacedo.meuponto.domain.usecase.relatorio

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioPadraoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Caso de uso para gerar relatório mensal de pontos.
 */
class GerarRelatorioMensalUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val configuracaoRepository: ConfiguracaoEmpregoRepository,
    private val horarioPadraoRepository: HorarioPadraoRepository
) {
    data class RelatorioMensal(
        val empregoId: Long,
        val mes: YearMonth,
        val dias: List<DiaSemana>,
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
    }

    data class DiaSemana(
        val data: LocalDate,
        val pontos: List<Ponto>,
        val trabalhadoMinutos: Long,
        val esperadoMinutos: Long,
        val saldoMinutos: Long,
        val isDiaUtil: Boolean,
        val isFeriado: Boolean = false,
        val observacao: String? = null
    )

    suspend operator fun invoke(empregoId: Long, mes: YearMonth): RelatorioMensal {
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
        val primeiroDia = configuracao?.primeiroDiaMes ?: 1

        val dataInicio = if (primeiroDia == 1) {
            mes.atDay(1)
        } else {
            mes.minusMonths(1).atDay(primeiroDia)
        }

        val dataFim = if (primeiroDia == 1) {
            mes.atEndOfMonth()
        } else {
            mes.atDay(primeiroDia - 1)
        }

        val pontos = pontoRepository.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
        val pontosPorDia = pontos.groupBy { it.data }

        val dias = mutableListOf<DiaSemana>()
        var dataAtual = dataInicio

        while (!dataAtual.isAfter(dataFim)) {
            val pontosDoDia = pontosPorDia[dataAtual] ?: emptyList()
            val isDiaUtil = isDiaUtil(dataAtual)
            val trabalhadoMinutos = calcularTempoTrabalhado(pontosDoDia)
            val esperadoMinutos = if (isDiaUtil) getJornadaDiaria(empregoId, dataAtual) else 0L

            dias.add(
                DiaSemana(
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

        return RelatorioMensal(
            empregoId = empregoId,
            mes = mes,
            dias = dias,
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
