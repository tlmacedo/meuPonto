package br.com.tlmacedo.meuponto.domain.usecase.relatorio

import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ResumoDiaCompleto
import br.com.tlmacedo.meuponto.util.helper.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.helper.minutosParaSaldoFormatado
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Caso de uso para gerar relatório mensal de pontos.
 * Refatorado para utilizar GerarResumoPeriodoUseCase e garantir paridade de cálculos.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 10.0.0 - Refatorado para usar GerarResumoPeriodoUseCase
 */
class GerarRelatorioMensalUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val gerarResumoPeriodoUseCase: GerarResumoPeriodoUseCase
) {
    data class RelatorioMensal(
        val empregoId: Long,
        val mesRef: YearMonth,
        val dataInicio: LocalDate,
        val dataFim: LocalDate,
        val dias: List<ResumoDiaCompleto>,
        val totalTrabalhadoMinutos: Int,
        val totalEsperadoMinutos: Int,
        val totalAbonadoMinutos: Int,
        val saldoMinutos: Int,
        val diasTrabalhados: Int,
        val diasUteis: Int
    ) {
        val saldoFormatado: String
            get() = saldoMinutos.minutosParaSaldoFormatado()

        val totalTrabalhadoFormatado: String
            get() = totalTrabalhadoMinutos.minutosParaHoraMinuto()

        val totalEsperadoFormatado: String
            get() = totalEsperadoMinutos.minutosParaHoraMinuto()

        val totalAbonadoFormatado: String
            get() = totalAbonadoMinutos.minutosParaHoraMinuto()
    }

    suspend operator fun invoke(empregoId: Long, mes: YearMonth): RelatorioMensal {
        val versaoJornada = versaoJornadaRepository.buscarVigente(empregoId)
        val diaInicio = versaoJornada?.diaInicioFechamentoRH ?: 1

        val dataInicio = if (diaInicio <= 1) {
            mes.atDay(1)
        } else {
            // Se o fechamento é no dia 21, o mês de referência "Maio" 
            // costuma ser de 21/Abril a 20/Maio.
            mes.minusMonths(1).atDay(diaInicio)
        }

        val dataFim = if (diaInicio <= 1) {
            mes.atEndOfMonth()
        } else {
            mes.atDay(diaInicio - 1)
        }

        val resumoPeriodo = gerarResumoPeriodoUseCase(empregoId, dataInicio, dataFim)

        return RelatorioMensal(
            empregoId = empregoId,
            mesRef = mes,
            dataInicio = dataInicio,
            dataFim = dataFim,
            dias = resumoPeriodo.resumos,
            totalTrabalhadoMinutos = resumoPeriodo.totalTrabalhadoMinutos,
            totalEsperadoMinutos = resumoPeriodo.totalEsperadoMinutos,
            totalAbonadoMinutos = resumoPeriodo.totalAbonadoMinutos,
            saldoMinutos = resumoPeriodo.saldoPeriodoMinutos,
            diasTrabalhados = resumoPeriodo.diasTrabalhados,
            diasUteis = resumoPeriodo.diasUteis
        )
    }
}
