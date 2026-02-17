// Arquivo: CalcularSaldoPeriodoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular saldo de um período.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Tipo calculado por posição
 */
class CalcularSaldoPeriodoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository
) {
    data class SaldoPeriodo(
        val dataInicio: LocalDate,
        val dataFim: LocalDate,
        val totalTrabalhadoMinutos: Long,
        val totalEsperadoMinutos: Long,
        val saldoMinutos: Long,
        val diasTrabalhados: Int
    )

    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        cargaHorariaDiariaMinutos: Long = 480L
    ): SaldoPeriodo {
        val pontos = pontoRepository.buscarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
        val pontosPorDia = pontos.groupBy { it.data }

        var totalTrabalhado = 0L
        var diasTrabalhados = 0

        pontosPorDia.forEach { (_, pontosDoDia) ->
            if (pontosDoDia.isNotEmpty()) {
                diasTrabalhados++
                totalTrabalhado += calcularTempoTrabalhado(pontosDoDia)
            }
        }

        val totalEsperado = diasTrabalhados * cargaHorariaDiariaMinutos

        return SaldoPeriodo(
            dataInicio = dataInicio,
            dataFim = dataFim,
            totalTrabalhadoMinutos = totalTrabalhado,
            totalEsperadoMinutos = totalEsperado,
            saldoMinutos = totalTrabalhado - totalEsperado,
            diasTrabalhados = diasTrabalhados
        )
    }

    private fun calcularTempoTrabalhado(pontos: List<Ponto>): Long {
        if (pontos.size < 2) return 0L

        val ordenados = pontos.sortedBy { it.hora }
        var totalMinutos = 0L
        var i = 0

        while (i < ordenados.size - 1) {
            val entrada = ordenados[i]
            val saida = ordenados[i + 1]
            totalMinutos += Duration.between(entrada.hora, saida.hora).toMinutes()
            i += 2
        }

        return totalMinutos
    }
}
