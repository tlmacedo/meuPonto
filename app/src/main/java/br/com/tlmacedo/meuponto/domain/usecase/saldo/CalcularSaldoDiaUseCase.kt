// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/saldo/CalcularSaldoDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.util.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.minutosParaSaldoFormatado
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular saldo de um dia.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.11.0 - Usa formatadores padronizados de MinutosExtensions
 */
class CalcularSaldoDiaUseCase @Inject constructor(
    private val pontoRepository: PontoRepository
) {
    data class SaldoDia(
        val data: LocalDate,
        val trabalhadoMinutos: Long,
        val esperadoMinutos: Long,
        val saldoMinutos: Long,
        val intervaloRealMinutos: Long,
        val intervaloConsideradoMinutos: Long,
        val isDiaUtil: Boolean,
        val temAjusteTolerancia: Boolean
    ) {
        /** Saldo: "+00h 00min" ou "-00h 00min" */
        val saldoFormatado: String
            get() = saldoMinutos.minutosParaSaldoFormatado()

        /** Trabalhado: "00h 00min" */
        val trabalhadoFormatado: String
            get() = trabalhadoMinutos.minutosParaHoraMinuto()

        /** Intervalo real: "00h 00min" */
        val intervaloRealFormatado: String
            get() = intervaloRealMinutos.minutosParaHoraMinuto()

        /** Intervalo considerado: "00h 00min" */
        val intervaloConsideradoFormatado: String
            get() = intervaloConsideradoMinutos.minutosParaHoraMinuto()

        // Retrocompatibilidade
        val intervaloMinutos: Long get() = intervaloRealMinutos
    }

    suspend operator fun invoke(
        empregoId: Long,
        data: LocalDate,
        cargaHorariaDiariaMinutos: Long = 480L
    ): SaldoDia {
        val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        return calcular(pontos, data, cargaHorariaDiariaMinutos)
    }

    fun calcularComPontos(
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
        val intervaloReal = calcularIntervaloReal(pontos)
        val intervaloConsiderado = calcularIntervaloConsiderado(pontos)
        val isDiaUtil = isDiaUtil(data)
        val esperado = if (isDiaUtil) cargaHorariaDiariaMinutos else 0L
        val temAjuste = pontos.any { it.temAjusteTolerancia }

        return SaldoDia(
            data = data,
            trabalhadoMinutos = trabalhado,
            esperadoMinutos = esperado,
            saldoMinutos = trabalhado - esperado,
            intervaloRealMinutos = intervaloReal,
            intervaloConsideradoMinutos = intervaloConsiderado,
            isDiaUtil = isDiaUtil,
            temAjusteTolerancia = temAjuste
        )
    }

    private fun isDiaUtil(data: LocalDate): Boolean {
        return data.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }

    private fun calcularTempoTrabalhado(pontos: List<Ponto>): Long {
        if (pontos.size < 2) return 0L

        val ordenados = pontos.sortedBy { it.dataHora }
        var totalMinutos = 0L
        var i = 0

        while (i < ordenados.size - 1) {
            val entrada = ordenados[i]
            val saida = ordenados[i + 1]
            totalMinutos += Duration.between(entrada.horaEfetiva, saida.horaEfetiva).toMinutes()
            i += 2
        }

        return totalMinutos
    }

    private fun calcularIntervaloReal(pontos: List<Ponto>): Long {
        if (pontos.size < 4) return 0L

        val ordenados = pontos.sortedBy { it.dataHora }
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

    private fun calcularIntervaloConsiderado(pontos: List<Ponto>): Long {
        if (pontos.size < 4) return 0L

        val ordenados = pontos.sortedBy { it.dataHora }
        var totalIntervalo = 0L
        var i = 1

        while (i < ordenados.size - 1) {
            val saida = ordenados[i]
            val entrada = ordenados[i + 1]
            totalIntervalo += Duration.between(saida.horaEfetiva, entrada.horaEfetiva).toMinutes()
            i += 2
        }

        return totalIntervalo
    }
}
