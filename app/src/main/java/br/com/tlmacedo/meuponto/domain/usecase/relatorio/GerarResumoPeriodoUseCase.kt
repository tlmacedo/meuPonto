package br.com.tlmacedo.meuponto.domain.usecase.relatorio

import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ResumoDiaCompleto
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para gerar o resumo completo de um período de datas.
 * Serve como base para a geração de relatórios (PDF/CSV/Tela).
 *
 * @author Thiago
 * @since 10.0.0
 */
class GerarResumoPeriodoUseCase @Inject constructor(
    private val obterResumoDiaCompletoUseCase: ObterResumoDiaCompletoUseCase
) {

    data class ResumoPeriodo(
        val dataInicio: LocalDate,
        val dataFim: LocalDate,
        val resumos: List<ResumoDiaCompleto>,
        val totalTrabalhadoMinutos: Int,
        val totalEsperadoMinutos: Int,
        val totalAbonadoMinutos: Int,
        val saldoPeriodoMinutos: Int,
        val diasTrabalhados: Int,
        val diasUteis: Int
    )

    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): ResumoPeriodo {
        val resumos = mutableListOf<ResumoDiaCompleto>()
        var dataAtual = dataInicio

        while (!dataAtual.isAfter(dataFim)) {
            resumos.add(obterResumoDiaCompletoUseCase(empregoId, dataAtual))
            dataAtual = dataAtual.plusDays(1)
        }

        val totalTrabalhado = resumos.sumOf { it.horasTrabalhadasMinutos }
        val totalEsperado = resumos.sumOf { it.cargaHorariaEfetivaMinutos }
        val totalAbonado = resumos.sumOf { it.tempoAbonadoMinutos }
        val diasTrabalhados = resumos.count { it.temPontos }
        val diasUteis = resumos.count { !it.zeraJornada }

        return ResumoPeriodo(
            dataInicio = dataInicio,
            dataFim = dataFim,
            resumos = resumos,
            totalTrabalhadoMinutos = totalTrabalhado,
            totalEsperadoMinutos = totalEsperado,
            totalAbonadoMinutos = totalAbonado,
            saldoPeriodoMinutos = totalTrabalhado + totalAbonado - totalEsperado,
            diasTrabalhados = diasTrabalhados,
            diasUteis = diasUteis
        )
    }
}
