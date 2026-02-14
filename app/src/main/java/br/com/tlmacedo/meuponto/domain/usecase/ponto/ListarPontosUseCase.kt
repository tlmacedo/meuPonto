package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Caso de uso para listar pontos com informações adicionais.
 */
class ListarPontosUseCase @Inject constructor(
    private val pontoRepository: PontoRepository
) {
    data class PontoComDetalhes(
        val ponto: Ponto,
        val duracaoDesdeAnteriorMinutos: Long?,
        val isEntrada: Boolean,
        val isUltimoDoDia: Boolean
    )

    data class DiaComPontos(
        val data: LocalDate,
        val pontos: List<PontoComDetalhes>,
        val totalTrabalhadoMinutos: Long,
        val isCompleto: Boolean
    ) {
        val totalTrabalhadoFormatado: String
            get() {
                val h = totalTrabalhadoMinutos / 60
                val m = totalTrabalhadoMinutos % 60
                return "${h}h${m}min"
            }
    }

    fun observarPorData(empregoId: Long, data: LocalDate): Flow<DiaComPontos> {
        return pontoRepository.observarPorEmpregoEData(empregoId, data)
            .map { pontos -> processarPontosDoDia(data, pontos) }
    }

    fun observarPorPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<DiaComPontos>> {
        return pontoRepository.observarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
            .map { pontos ->
                pontos.groupBy { it.data }
                    .map { (data, pontosDoDia) -> processarPontosDoDia(data, pontosDoDia) }
                    .sortedByDescending { it.data }
            }
    }

    suspend fun buscarPorData(empregoId: Long, data: LocalDate): DiaComPontos {
        val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        return processarPontosDoDia(data, pontos)
    }

    private fun processarPontosDoDia(data: LocalDate, pontos: List<Ponto>): DiaComPontos {
        val pontosOrdenados = pontos.sortedBy { it.hora }
        val pontosComDetalhes = pontosOrdenados.mapIndexed { index, ponto ->
            val duracaoDesdeAnterior = if (index > 0) {
                ChronoUnit.MINUTES.between(pontosOrdenados[index - 1].hora, ponto.hora)
            } else null

            PontoComDetalhes(
                ponto = ponto,
                duracaoDesdeAnteriorMinutos = duracaoDesdeAnterior,
                isEntrada = ponto.tipo == TipoPonto.ENTRADA,
                isUltimoDoDia = index == pontosOrdenados.lastIndex
            )
        }

        val totalTrabalhado = calcularTempoTrabalhado(pontosOrdenados)
        val isCompleto = pontosOrdenados.size >= 2 && pontosOrdenados.size % 2 == 0

        return DiaComPontos(
            data = data,
            pontos = pontosComDetalhes,
            totalTrabalhadoMinutos = totalTrabalhado,
            isCompleto = isCompleto
        )
    }

    private fun calcularTempoTrabalhado(pontos: List<Ponto>): Long {
        if (pontos.isEmpty()) return 0L

        var totalMinutos = 0L
        var i = 0

        while (i < pontos.size - 1) {
            val entrada = pontos[i]
            val saida = pontos[i + 1]

            if (entrada.tipo == TipoPonto.ENTRADA && saida.tipo == TipoPonto.SAIDA) {
                totalMinutos += ChronoUnit.MINUTES.between(entrada.hora, saida.hora)
            }
            i += 2
        }

        return totalMinutos
    }
}
