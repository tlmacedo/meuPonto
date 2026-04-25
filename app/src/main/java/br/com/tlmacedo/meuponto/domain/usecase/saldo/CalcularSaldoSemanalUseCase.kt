// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/saldo/CalcularSaldoSemanalUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.util.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.minutosParaSaldoFormatado
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * @author Thiago
 * @since 1.0.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
class CalcularSaldoSemanalUseCase @Inject constructor(
    private val calcularSaldoDiaUseCase: CalcularSaldoDiaUseCase,
    private val versaoJornadaRepository: VersaoJornadaRepository
) {
    data class SaldoSemanal(
        val dataInicio: LocalDate,
        val dataFim: LocalDate,
        val trabalhadoMinutos: Long,
        val esperadoMinutos: Long,
        val saldoMinutos: Long,
        val diasTrabalhados: Int,
        val saldosDiarios: List<CalcularSaldoDiaUseCase.SaldoDia>
    ) {
        val trabalhadoFormatado: String get() = trabalhadoMinutos.minutosParaHoraMinuto()
        val esperadoFormatado: String get() = esperadoMinutos.minutosParaHoraMinuto()
        val saldoFormatado: String get() = saldoMinutos.minutosParaSaldoFormatado()
    }

    suspend operator fun invoke(empregoId: Long, dataReferencia: LocalDate): SaldoSemanal {
        val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId)
        val primeiroDiaSemana = versaoVigente?.primeiroDiaSemana ?: DiaSemana.SEGUNDA

        val dayOfWeek = primeiroDiaSemana.toDayOfWeek()
        val dataInicio = dataReferencia.with(TemporalAdjusters.previousOrSame(dayOfWeek))
        val dataFim = dataInicio.plusDays(6)

        val saldosDiarios = mutableListOf<CalcularSaldoDiaUseCase.SaldoDia>()
        var dataAtual = dataInicio
        val hoje = LocalDate.now()

        while (!dataAtual.isAfter(dataFim) && !dataAtual.isAfter(hoje)) {
            val saldoDia = calcularSaldoDiaUseCase(empregoId, dataAtual)
            saldosDiarios.add(saldoDia)
            dataAtual = dataAtual.plusDays(1)
        }

        val trabalhadoMinutos = saldosDiarios.sumOf { it.trabalhadoMinutos }
        val esperadoMinutos = saldosDiarios.sumOf { it.esperadoMinutos }
        val diasTrabalhados = saldosDiarios.count { it.trabalhadoMinutos > 0 }

        return SaldoSemanal(
            dataInicio,
            dataFim,
            trabalhadoMinutos,
            esperadoMinutos,
            trabalhadoMinutos - esperadoMinutos,
            diasTrabalhados,
            saldosDiarios
        )
    }

    private fun DiaSemana.toDayOfWeek(): DayOfWeek = when (this) {
        DiaSemana.SEGUNDA -> DayOfWeek.MONDAY
        DiaSemana.TERCA -> DayOfWeek.TUESDAY
        DiaSemana.QUARTA -> DayOfWeek.WEDNESDAY
        DiaSemana.QUINTA -> DayOfWeek.THURSDAY
        DiaSemana.SEXTA -> DayOfWeek.FRIDAY
        DiaSemana.SABADO -> DayOfWeek.SATURDAY
        DiaSemana.DOMINGO -> DayOfWeek.SUNDAY
    }
}
