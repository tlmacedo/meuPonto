package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class CalcularSaldoMensalUseCase @Inject constructor(
    private val calcularSaldoDiaUseCase: CalcularSaldoDiaUseCase,
    private val configuracaoRepository: ConfiguracaoEmpregoRepository
) {
    data class SaldoMensal(
        val mes: YearMonth,
        val trabalhadoMinutos: Long,
        val esperadoMinutos: Long,
        val saldoMinutos: Long,
        val diasTrabalhados: Int,
        val diasUteis: Int,
        val saldosDiarios: List<CalcularSaldoDiaUseCase.SaldoDia>
    ) {
        val trabalhadoFormatado: String get() = formatarMinutos(trabalhadoMinutos)
        val esperadoFormatado: String get() = formatarMinutos(esperadoMinutos)
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

    suspend operator fun invoke(empregoId: Long, mes: YearMonth): SaldoMensal {
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
        val primeiroDiaMes = configuracao?.primeiroDiaMes ?: 1

        val dataInicio = calcularDataInicio(mes, primeiroDiaMes)
        val dataFim = calcularDataFim(mes, primeiroDiaMes)

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
        val diasUteis = saldosDiarios.count { it.isDiaUtil }

        return SaldoMensal(
            mes = mes,
            trabalhadoMinutos = trabalhadoMinutos,
            esperadoMinutos = esperadoMinutos,
            saldoMinutos = trabalhadoMinutos - esperadoMinutos,
            diasTrabalhados = diasTrabalhados,
            diasUteis = diasUteis,
            saldosDiarios = saldosDiarios
        )
    }

    private fun calcularDataInicio(mes: YearMonth, primeiroDiaMes: Int): LocalDate {
        return if (primeiroDiaMes == 1) {
            mes.atDay(1)
        } else {
            mes.minusMonths(1).atDay(primeiroDiaMes)
        }
    }

    private fun calcularDataFim(mes: YearMonth, primeiroDiaMes: Int): LocalDate {
        return if (primeiroDiaMes == 1) {
            mes.atEndOfMonth()
        } else {
            mes.atDay(primeiroDiaMes - 1)
        }
    }
}
