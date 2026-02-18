// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/saldo/CalcularSaldoMensalUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.util.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.minutosParaSaldoFormatado
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Caso de uso para calcular saldo mensal.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.11.0 - Usa formatadores padronizados de MinutosExtensions
 */
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
        /** Trabalhado: "00h 00min" */
        val trabalhadoFormatado: String
            get() = trabalhadoMinutos.minutosParaHoraMinuto()

        /** Esperado: "00h 00min" */
        val esperadoFormatado: String
            get() = esperadoMinutos.minutosParaHoraMinuto()

        /** Saldo: "+00h 00min" ou "-00h 00min" */
        val saldoFormatado: String
            get() = saldoMinutos.minutosParaSaldoFormatado()
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
