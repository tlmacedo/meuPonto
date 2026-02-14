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
 * Caso de uso para calcular o saldo de horas de um dia específico.
 */
class CalcularSaldoDiaUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val horarioPadraoRepository: HorarioPadraoRepository
) {
    data class SaldoDia(
        val data: LocalDate,
        val trabalhadoMinutos: Long,
        val esperadoMinutos: Long,
        val saldoMinutos: Long,
        val pontos: List<Ponto>,
        val isDiaUtil: Boolean,
        val isCompleto: Boolean
    ) {
        val trabalhadoFormatado: String
            get() = formatarMinutos(trabalhadoMinutos)

        val esperadoFormatado: String
            get() = formatarMinutos(esperadoMinutos)

        val saldoFormatado: String
            get() {
                val sinal = if (saldoMinutos >= 0) "+" else "-"
                return "$sinal${formatarMinutos(kotlin.math.abs(saldoMinutos))}"
            }

        private fun formatarMinutos(minutos: Long): String {
            val h = minutos / 60
            val m = minutos % 60
            return "${h}h${m}min"
        }
    }

    suspend operator fun invoke(empregoId: Long, data: LocalDate): SaldoDia {
        val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        val isDiaUtil = isDiaUtil(data)
        val trabalhadoMinutos = calcularTempoTrabalhado(pontos)
        val esperadoMinutos = if (isDiaUtil) getJornadaDiaria(empregoId, data) else 0L
        val isCompleto = pontos.size >= 2 && pontos.size % 2 == 0

        return SaldoDia(
            data = data,
            trabalhadoMinutos = trabalhadoMinutos,
            esperadoMinutos = esperadoMinutos,
            saldoMinutos = trabalhadoMinutos - esperadoMinutos,
            pontos = pontos.sortedBy { it.hora },
            isDiaUtil = isDiaUtil,
            isCompleto = isCompleto
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
