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
 * @updated 11.0.0 - Centralizada lógica de agregação para paridade com Histórico
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
        val totalAjustesMinutos: Int = 0,
        val diasTrabalhados: Int,
        val diasCompletos: Int = 0,
        val diasComProblemas: Int = 0,
        val diasUteis: Int,
        val diasUteisSemRegistro: Int = 0,
        val diasDescanso: Int = 0,
        val diasFeriado: Int = 0,
        val diasFerias: Int = 0,
        val diasFolga: Int = 0,
        val diasFolgaDayOff: Int = 0,
        val diasFolgaCompensacao: Int = 0,
        val diasAtestado: Int = 0,
        val diasFaltaJustificada: Int = 0,
        val diasFaltaInjustificada: Int = 0,
        val totalMinutosTolerancia: Int = 0,
        val quantidadeDeclaracoes: Int = 0,
        val totalMinutosDeclaracoes: Int = 0,
        val quantidadeAtestados: Int = 0,
        val nomesFeriados: List<String> = emptyList(),
        // Previsão de dias futuros
        val diasDescansoFuturo: Int = 0,
        val diasFeriadoFuturo: Int = 0,
        val diasFeriasFuturo: Int = 0,
        val diasFolgaFuturo: Int = 0,
        val diasFuturos: Int = 0
    )

    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        ajustes: Map<LocalDate, Int> = emptyMap()
    ): ResumoPeriodo {
        val resumos = mutableListOf<ResumoDiaCompleto>()
        var dataAtual = dataInicio

        while (!dataAtual.isAfter(dataFim)) {
            resumos.add(obterResumoDiaCompletoUseCase(empregoId, dataAtual))
            dataAtual = dataAtual.plusDays(1)
        }

        return calcular(dataInicio, dataFim, resumos, ajustes)
    }

    companion object {
        /**
         * Centraliza a lógica de agregação de dias em um resumo de período.
         * Garante que o Histórico e os Relatórios usem os mesmos totais.
         */
        fun calcular(
            dataInicio: LocalDate,
            dataFim: LocalDate,
            resumos: List<ResumoDiaCompleto>,
            ajustes: Map<LocalDate, Int> = emptyMap()
        ): ResumoPeriodo {
            val resumosPassados = resumos.filter { !it.isFuturo }
            val resumosFuturos = resumos.filter { it.isFuturo }

            // Totais de minutos (apenas passado)
            val totalTrabalhado = resumosPassados.sumOf { it.horasTrabalhadasMinutos }
            val totalEsperado = resumosPassados.sumOf { it.cargaHorariaEfetivaMinutos }
            val totalAbonado = resumosPassados.sumOf { it.tempoAbonadoMinutos }
            val totalTolerancia = resumosPassados.sumOf { it.minutosToleranciaIntervalo }
            val totalDeclaracoes = resumosPassados.sumOf { it.totalMinutosDeclaracoes }
            val totalAjustes = ajustes.filter { it.key in dataInicio..dataFim }.values.sum()

            // Contagem de dias (passado)
            val diasTrabalhados = resumosPassados.count { it.temPontos }
            val diasCompletos = resumosPassados.count { it.jornadaCompleta }
            val diasComProblemas = resumosPassados.count { it.temProblemas }
            val diasUteis = resumosPassados.count { !it.zeraJornada }
            val diasUteisSemRegistro = resumosPassados.count { 
                !it.zeraJornada && !it.temPontos && it.data.dayOfWeek.value < 6 
            }

            val diasDescanso = resumosPassados.count { it.isDescanso && !it.temPontos && !it.temFeriado && it.ausencias.isEmpty() }
            val diasFeriado = resumosPassados.count { it.temFeriado }
            val diasFerias = resumosPassados.count { it.isFerias }
            val diasAtestado = resumosPassados.count { it.isAtestado }
            val diasFaltaJustificada = resumosPassados.count { it.isFaltaJustificada }
            val diasFaltaInjustificada = resumosPassados.count { it.isFaltaInjustificada }
            val diasFolga = resumosPassados.count { it.isFolga }
            val diasFolgaDayOff = resumosPassados.count { it.isDayOff }
            val diasFolgaCompensacao = resumosPassados.count { it.isFolga && !it.isDayOff }

            val quantidadeDeclaracoes = resumosPassados.count { it.declaracoes.isNotEmpty() }
            val quantidadeAtestados = resumosPassados.count { it.isAtestado }
            val nomesFeriados = resumos.flatMap { it.feriadosDoDia }.map { it.nome }.distinct()

            // Contagem de dias (futuro)
            val diasFuturos = resumosFuturos.size
            val diasDescansoFuturo = resumosFuturos.count { it.isDescanso }
            val diasFeriadoFuturo = resumosFuturos.count { it.temFeriado }
            val diasFeriasFuturo = resumosFuturos.count { it.isFerias }
            val diasFolgaFuturo = resumosFuturos.count { it.isFolga }

            return ResumoPeriodo(
                dataInicio = dataInicio,
                dataFim = dataFim,
                resumos = resumos,
                totalTrabalhadoMinutos = totalTrabalhado,
                totalEsperadoMinutos = totalEsperado,
                totalAbonadoMinutos = totalAbonado,
                saldoPeriodoMinutos = totalTrabalhado + totalAbonado - totalEsperado,
                totalAjustesMinutos = totalAjustes,
                diasTrabalhados = diasTrabalhados,
                diasCompletos = diasCompletos,
                diasComProblemas = diasComProblemas,
                diasUteis = diasUteis,
                diasUteisSemRegistro = diasUteisSemRegistro,
                diasDescanso = diasDescanso,
                diasFeriado = diasFeriado,
                diasFerias = diasFerias,
                diasFolga = diasFolga,
                diasFolgaDayOff = diasFolgaDayOff,
                diasFolgaCompensacao = diasFolgaCompensacao,
                diasAtestado = diasAtestado,
                diasFaltaJustificada = diasFaltaJustificada,
                diasFaltaInjustificada = diasFaltaInjustificada,
                totalMinutosTolerancia = totalTolerancia,
                quantidadeDeclaracoes = quantidadeDeclaracoes,
                totalMinutosDeclaracoes = totalDeclaracoes,
                quantidadeAtestados = quantidadeAtestados,
                nomesFeriados = nomesFeriados,
                diasDescansoFuturo = diasDescansoFuturo,
                diasFeriadoFuturo = diasFeriadoFuturo,
                diasFeriasFuturo = diasFeriasFuturo,
                diasFolgaFuturo = diasFolgaFuturo,
                diasFuturos = diasFuturos
            )
        }
    }
}
