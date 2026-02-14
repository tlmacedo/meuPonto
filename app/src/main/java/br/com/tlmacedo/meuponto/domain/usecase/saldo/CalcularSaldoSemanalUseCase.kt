package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class CalcularSaldoSemanalUseCase @Inject constructor(
    private val calcularSaldoDiaUseCase: CalcularSaldoDiaUseCase,
    private val configuracaoRepository: ConfiguracaoEmpregoRepository
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

    suspend operator fun invoke(empregoId: Long, dataReferencia: LocalDate): SaldoSemanal {
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
        val primeiroDiaSemana = configuracao?.primeiroDiaSemana ?: DiaSemana.SEGUNDA

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
            dataInicio = dataInicio,
            dataFim = dataFim,
            trabalhadoMinutos = trabalhadoMinutos,
            esperadoMinutos = esperadoMinutos,
            saldoMinutos = trabalhadoMinutos - esperadoMinutos,
            diasTrabalhados = diasTrabalhados,
            saldosDiarios = saldosDiarios
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
