// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/relatorio/GerarResumoPeriodoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.relatorio

import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ResumoDiaCompleto
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para gerar o resumo completo de um período de datas.
 * Serve como base para a geração de relatórios (PDF/CSV/Tela).
 *
 * Centraliza a lógica de agregação para manter Histórico e Relatórios consistentes.
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

        return calcular(
            dataInicio = dataInicio,
            dataFim = dataFim,
            resumos = resumos,
            ajustes = ajustes
        )
    }

    companion object {

        fun calcular(
            dataInicio: LocalDate,
            dataFim: LocalDate,
            resumos: List<ResumoDiaCompleto>,
            ajustes: Map<LocalDate, Int> = emptyMap()
        ): ResumoPeriodo {
            val resumosPassados = resumos.filterNot { it.isFuturo }
            val resumosFuturos = resumos.filter { it.isFuturo }

            val totalTrabalhado = resumosPassados.sumOf { resumo ->
                resumo.horasTrabalhadasMinutos
            }

            val totalEsperado = resumosPassados.sumOf { resumo ->
                resumo.cargaHorariaEfetivaMinutos
            }

            val totalAbonado = resumosPassados.sumOf { resumo ->
                resumo.tempoAbonadoMinutos
            }

            val totalTolerancia = resumosPassados.sumOf { resumo ->
                resumo.minutosToleranciaIntervalo
            }

            val totalDeclaracoes = resumosPassados.sumOf { resumo ->
                resumo.totalMinutosDeclaracoes
            }

            val saldoPeriodo = resumosPassados.sumOf { resumo ->
                resumo.saldoDiaMinutos
            }

            val totalAjustes = ajustes
                .filterKeys { data -> data in dataInicio..dataFim }
                .values
                .sum()

            val totalDiasCorridos = resumos.size

            val resumosFaltas = resumosPassados.filter { resumo ->
                resumo.isFaltaJustificada || resumo.isFaltaInjustificada
            }

            val resumosAtestados = resumosPassados.filter { resumo ->
                resumo.isAtestado
            }

            val diasAtestados = resumosAtestados.size
            val diasFaltas = resumosFaltas.size
            val diasFaltasAtestados = diasFaltas + diasAtestados

            val resumosFerias = resumosPassados.filter { resumo ->
                resumo.isFerias && !resumo.temFaltaOuAtestado()
            }

            val diasFeriasCount = resumosFerias.size

            val resumosFeriado = resumosPassados.filter { resumo ->
                resumo.temFeriado &&
                        !resumo.isFerias &&
                        !resumo.temFaltaOuAtestado()
            }

            val diasFeriadoCount = resumosFeriado.size

            val resumosFolgaSemanal = resumosPassados.filter { resumo ->
                resumo.isFolgaSemanal() &&
                        !resumo.temFeriado &&
                        !resumo.isFerias &&
                        !resumo.temFaltaOuAtestado()
            }

            val diasFolgaSemanalCount = resumosFolgaSemanal.size

            val diasUteisCount = resumos.count { resumo ->
                resumo.isDiaUtilParaRelatorio()
            }

            val totalHorasUteisMinutos = resumos.sumOf { resumo ->
                if (resumo.isDiaUtilParaRelatorio()) {
                    resumo.jornadaPlanejadaMinutos()
                } else {
                    0
                }
            }

            val diasTrabalhados = resumosPassados.count { resumo ->
                resumo.temPontos
            }

            val totalMinutosAusenciaAbonada = resumosPassados.sumOf { resumo ->
                if (resumo.isFaltaJustificada || resumo.isAtestado) {
                    resumo.jornadaPlanejadaMinutos()
                } else {
                    0
                }
            }

            val totalMinutosAusenciaNaoAbonada = resumosPassados.sumOf { resumo ->
                if (resumo.isFaltaInjustificada) {
                    -resumo.jornadaPlanejadaMinutos()
                } else {
                    0
                }
            }

            val diasCompletosCount = resumosPassados.count { resumo ->
                resumo.pontos.size >= 4 || resumo.horasTrabalhadasMinutos >= 240
            }

            val diasIncompletosCount = resumosPassados.count { resumo ->
                resumo.isDiaUtilParaRelatorio() &&
                        (resumo.pontos.isEmpty() || resumo.horasTrabalhadasMinutos < 240)
            }

            val diasComProblemas = resumosPassados.count { resumo ->
                resumo.temProblemas
            }

            val nomesFeriados = resumos
                .flatMap { resumo -> resumo.feriadosDoDia }
                .map { feriado -> feriado.nome }
                .distinct()

            val diasFuturos = resumosFuturos.size
            val diasDescansoFuturo = resumosFuturos.count { resumo -> resumo.isDescanso }
            val diasFeriadoFuturo = resumosFuturos.count { resumo -> resumo.temFeriado }
            val diasFeriasFuturo = resumosFuturos.count { resumo -> resumo.isFerias }
            val diasFolgaFuturo = resumosFuturos.count { resumo -> resumo.isFolga }

            return ResumoPeriodo(
                dataInicio = dataInicio,
                dataFim = dataFim,
                resumos = resumos,

                totalTrabalhadoMinutos = totalTrabalhado,
                totalEsperadoMinutos = totalEsperado,
                totalAbonadoMinutos = totalAbonado,
                saldoPeriodoMinutos = saldoPeriodo,
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

                diasDescanso = resumosPassados.count { resumo -> resumo.isDescanso },
                diasFeriado = resumosPassados.count { resumo -> resumo.temFeriado },
                diasFerias = resumosPassados.count { resumo -> resumo.isFerias },
                diasFolga = resumosPassados.count { resumo -> resumo.isFolga },
                diasFolgaDayOff = resumosPassados.count { resumo -> resumo.isDayOff },
                diasFolgaCompensacao = resumosPassados.count { resumo ->
                    resumo.isFolga && !resumo.isDayOff
                },

                diasAtestado = resumosPassados.count { resumo -> resumo.isAtestado },
                diasFaltaJustificada = resumosPassados.count { resumo -> resumo.isFaltaJustificada },
                diasFaltaInjustificada = resumosPassados.count { resumo -> resumo.isFaltaInjustificada },
                diasFaltas = resumosPassados.count { resumo ->
                    resumo.isFaltaJustificada || resumo.isFaltaInjustificada
                },

                diasFeriasFeriadosDescanso = diasFeriasCount + diasFeriadoCount + diasFolgaSemanalCount,

                totalMinutosTolerancia = totalTolerancia,
                totalMinutosDeclaracoes = totalDeclaracoes,
                quantidadeDeclaracoes = resumosPassados.count { resumo ->
                    resumo.declaracoes.isNotEmpty()
                },

                nomesFeriados = nomesFeriados,

                diasDescansoFuturo = diasDescansoFuturo,
                diasFeriadoFuturo = diasFeriadoFuturo,
                diasFeriasFuturo = diasFeriasFuturo,
                diasFolgaFuturo = diasFolgaFuturo,
                diasFuturos = diasFuturos
            )
        }

        private fun ResumoDiaCompleto.temFaltaOuAtestado(): Boolean {
            return isFaltaJustificada || isFaltaInjustificada || isAtestado
        }

        private fun ResumoDiaCompleto.isFolgaSemanal(): Boolean {
            return (horarioDiaSemana?.cargaHorariaMinutos ?: 0) == 0
        }

        private fun ResumoDiaCompleto.isDiaUtilParaRelatorio(): Boolean {
            val temJornadaSemanal = (horarioDiaSemana?.cargaHorariaMinutos ?: 0) > 0

            return temJornadaSemanal &&
                    !temFeriado &&
                    !isFerias &&
                    !temFaltaOuAtestado()
        }

        private fun ResumoDiaCompleto.jornadaPlanejadaMinutos(): Int {
            return horarioDiaSemana?.cargaHorariaMinutos
                ?: resumoDia.jornadaPrevistaMinutos
        }
    }
}