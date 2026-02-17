// Arquivo: CalcularSaldoDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular saldo de um dia.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Tipo calculado por posição (índice par = entrada)
 */
class CalcularSaldoDiaUseCase @Inject constructor(
    private val pontoRepository: PontoRepository
) {
    data class SaldoDia(
        val data: LocalDate,
        val trabalhadoMinutos: Long,
        val esperadoMinutos: Long,
        val saldoMinutos: Long,
        val intervaloMinutos: Long,
        val isDiaUtil: Boolean
    ) {
        val saldoFormatado: String
            get() {
                val horas = kotlin.math.abs(saldoMinutos) / 60
                val minutos = kotlin.math.abs(saldoMinutos) % 60
                val sinal = if (saldoMinutos >= 0) "+" else "-"
                return "$sinal${horas}h${minutos}min"
            }
    }

    /**
     * Calcula saldo buscando pontos do repositório.
     */
    suspend operator fun invoke(
        empregoId: Long,
        data: LocalDate,
        cargaHorariaDiariaMinutos: Long = 480L
    ): SaldoDia {
        val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        return calcular(pontos, data, cargaHorariaDiariaMinutos)
    }

    /**
     * Calcula saldo a partir de uma lista de pontos.
     */
    fun invoke(
        pontos: List<Ponto>,
        cargaHorariaDiariaMinutos: Long = 480L
    ): SaldoDia {
        val data = pontos.firstOrNull()?.dataHora?.toLocalDate() ?: LocalDate.now()
        return calcular(pontos, data, cargaHorariaDiariaMinutos)
    }

    private fun calcular(
        pontos: List<Ponto>,
        data: LocalDate,
        cargaHorariaDiariaMinutos: Long
    ): SaldoDia {
        val trabalhado = calcularTempoTrabalhado(pontos)
        val intervalo = calcularIntervalo(pontos)
        val isDiaUtil = isDiaUtil(data)
        val esperado = if (isDiaUtil) cargaHorariaDiariaMinutos else 0L
        
        return SaldoDia(
            data = data,
            trabalhadoMinutos = trabalhado,
            esperadoMinutos = esperado,
            saldoMinutos = trabalhado - esperado,
            intervaloMinutos = intervalo,
            isDiaUtil = isDiaUtil
        )
    }

    private fun isDiaUtil(data: LocalDate): Boolean {
        return data.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
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

    private fun calcularIntervalo(pontos: List<Ponto>): Long {
        if (pontos.size < 4) return 0L

        val ordenados = pontos.sortedBy { it.hora }
        var totalIntervalo = 0L
        var i = 1

        while (i < ordenados.size - 1) {
            val saida = ordenados[i]
            val entrada = ordenados[i + 1]
            totalIntervalo += Duration.between(saida.hora, entrada.hora).toMinutes()
            i += 2
        }

        return totalIntervalo
    }
}
