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
        val totalDiasCorridos: Int,
        val diasUteis: Int,
        val totalHorasUteisMinutos: Int = 0,
        val diasTrabalhados: Int,
        val diasCompletos: Int = 0,
        val diasIncompletos: Int = 0,
        val diasComProblemas: Int = 0,
        val diasAusenciaTotal: Int = 0,
        val quantidadeFaltas: Int = 0,
        val totalMinutosAusenciaAbonada: Int = 0,
        val totalMinutosAusenciaNaoAbonada: Int = 0,
        val diasDescansoTotal: Int = 0,
        val quantidadeFerias: Int = 0,
        val quantidadeFeriados: Int = 0,
        val diasFolgasSemanaisTotal: Int = 0,
        val diasDescanso: Int = 0,
        val diasFeriado: Int = 0,
        val diasFerias: Int = 0,
        val diasFolga: Int = 0,
        val diasFolgaDayOff: Int = 0,
        val diasFolgaCompensacao: Int = 0,
        val diasAtestado: Int = 0,
        val diasFaltaJustificada: Int = 0,
        val diasFaltaInjustificada: Int = 0,
        val diasFaltas: Int = 0,
        val diasFeriasFeriadosDescanso: Int = 0,
        val totalMinutosTolerancia: Int = 0,
        val totalMinutosDeclaracoes: Int = 0,
        val quantidadeDeclaracoes: Int = 0,
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
            val resumoGeral = resumos
            val resumosPassados = resumos.filter { !it.isFuturo }
            val resumosFuturos = resumos.filter { it.isFuturo }

            // Totais de minutos (apenas passado)
            val totalTrabalhado = resumoGeral.sumOf { it.horasTrabalhadasMinutos }
            val totalEsperado = resumoGeral.sumOf { it.cargaHorariaEfetivaMinutos }
            val totalAbonado = resumosPassados.sumOf { it.tempoAbonadoMinutos }
            val totalTolerancia = resumosPassados.sumOf { it.minutosToleranciaIntervalo }
            val totalDeclaracoes = resumosPassados.sumOf { it.totalMinutosDeclaracoes }

            val totalAjustes = ajustes.filter { it.key in dataInicio..dataFim }.values.sum()

            // 1. Dias Corridos
            val totalDiasCorridos = resumosPassados.size + resumosFuturos.size

            // Regra de Prioridade: Falta > Férias > Feriado > Descanso Semanal

            val resumosFaltas =
                resumoGeral.filter { it.isFaltaJustificada || it.isFaltaInjustificada }
            val resumosAtestados =
                resumoGeral.filter { it.isAtestado }
            val diasAtestados = resumosAtestados.size
            val diasFaltas = resumosFaltas.size
            val diasFaltasAtestados = diasFaltas + diasAtestados

            val resumosFerias = resumosPassados.filter {
                it.isFerias && !(it.isFaltaJustificada || it.isFaltaInjustificada || it.isAtestado)
            }
            val diasFeriasCount = resumosFerias.size

            val resumosFeriado = resumosPassados.filter {
                it.temFeriado && !it.isFerias && !(it.isFaltaJustificada || it.isFaltaInjustificada || it.isAtestado)
            }
            val diasFeriadoCount = resumosFeriado.size

            val resumosFolgaSemanal = resumosPassados.filter { resumo ->
                val isDescansoSemanal = (resumo.horarioDiaSemana?.cargaHorariaMinutos ?: 0) == 0
                isDescansoSemanal && !resumo.temFeriado && !resumo.isFerias && !(resumo.isFaltaJustificada || resumo.isFaltaInjustificada || resumo.isAtestado)
            }
            val diasFolgaSemanalCount = resumosFolgaSemanal.size

            // 2. Dias úteis (Quantidade de dias do período não pode contar com férias, feriados, e descanso (sáb e dom))
            val diasUteisCount = resumoGeral.count { resumo ->
                val temJornadaSemanal = (resumo.horarioDiaSemana?.cargaHorariaMinutos ?: 0) > 0
                temJornadaSemanal && !resumo.temFeriado && !resumo.isFerias && !(resumo.isFaltaJustificada || resumo.isFaltaInjustificada || resumo.isAtestado)
            }

            val totalHorasUteisMinutos = resumoGeral.sumOf { resumo ->
                val temJornadaSemanal = (resumo.horarioDiaSemana?.cargaHorariaMinutos ?: 0) > 0
                if (temJornadaSemanal && !resumo.temFeriado && !resumo.isFerias && !(resumo.isFaltaJustificada || resumo.isFaltaInjustificada || resumo.isAtestado)) {
                    resumo.resumoDia.cargaHorariaDiariaMinutos
                } else 0
            }

            // Trabalhado
            val diasTrabalhados = resumosPassados.count { it.temPontos }

            // Ausências (Atestado + Faltas)
            val totalMinutosAusenciaAbonada = resumosPassados.sumOf { resumo ->
                if (resumo.isFaltaJustificada || resumo.isAtestado) {
                    resumo.resumoDia.cargaHorariaDiariaMinutos
                } else 0
            }

            val totalMinutosAusenciaNaoAbonada = resumosPassados.sumOf { resumo ->
                if (resumo.isFaltaInjustificada) {
                    resumo.resumoDia.cargaHorariaDiariaMinutos * -1
                } else 0
            }

            // 8. Dias Completos (2 turnos ou >= 4h)
            val diasCompletosCount = resumosPassados.count {
                it.pontos.size >= 4 || it.horasTrabalhadasMinutos >= 240
            }

            // Incompletos: Dia útil E (sem registro OU jornada < 4h)
            val diasIncompletosCount = resumosPassados.count { resumo ->
                val temJornadaSemanal = (resumo.horarioDiaSemana?.cargaHorariaMinutos ?: 0) > 0
                val isDiaUtil =
                    temJornadaSemanal && !resumo.temFeriado && !resumo.isFerias && !(resumo.isFaltaJustificada || resumo.isFaltaInjustificada || resumo.isAtestado)
                isDiaUtil && (resumo.pontos.isEmpty() || resumo.horasTrabalhadasMinutos < 240)
            }

            val diasComProblemas = resumosPassados.count { it.temProblemas }

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
                saldoPeriodoMinutos = resumosPassados.sumOf { it.saldoDiaMinutos },
                totalAjustesMinutos = totalAjustes,
                totalDiasCorridos = totalDiasCorridos,
                diasUteis = diasUteisCount,
                totalHorasUteisMinutos = totalHorasUteisMinutos,
                diasTrabalhados = diasTrabalhados,
                diasCompletos = diasCompletosCount,
                diasIncompletos = diasIncompletosCount,
                diasComProblemas = diasComProblemas,
                diasAusenciaTotal = diasFaltasAtestados,
                quantidadeFaltas = diasFaltas,
                quantidadeAtestados = diasAtestados,
                totalMinutosAusenciaAbonada = totalMinutosAusenciaAbonada,
                totalMinutosAusenciaNaoAbonada = totalMinutosAusenciaNaoAbonada,
                diasDescansoTotal = diasFeriasCount + diasFeriadoCount,
                quantidadeFerias = diasFeriasCount,
                quantidadeFeriados = diasFeriadoCount,
                diasFolgasSemanaisTotal = diasFolgaSemanalCount,
                diasDescanso = resumosPassados.count { it.isDescanso },
                diasFeriado = resumosPassados.count { it.temFeriado },
                diasFerias = resumosPassados.count { it.isFerias },
                diasFolga = resumosPassados.count { it.isFolga },
                diasFolgaDayOff = resumosPassados.count { it.isDayOff },
                diasFolgaCompensacao = resumosPassados.count { it.isFolga && !it.isDayOff },
                diasAtestado = resumosPassados.count { it.isAtestado },
                diasFaltaJustificada = resumosPassados.count { it.isFaltaJustificada },
                diasFaltaInjustificada = resumosPassados.count { it.isFaltaInjustificada },
                diasFaltas = resumosPassados.count { it.isFaltaJustificada || it.isFaltaInjustificada },
                diasFeriasFeriadosDescanso = diasFeriasCount + diasFeriadoCount + diasFolgaSemanalCount,
                totalMinutosTolerancia = totalTolerancia,
                totalMinutosDeclaracoes = totalDeclaracoes,
                quantidadeDeclaracoes = resumosPassados.count { it.declaracoes.isNotEmpty() },
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
