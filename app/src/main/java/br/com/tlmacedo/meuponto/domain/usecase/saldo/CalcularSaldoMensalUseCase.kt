// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/saldo/CalcularSaldoMensalUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.util.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.minutosParaSaldoFormatado
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * @author Thiago
 * @since 1.0.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
class CalcularSaldoMensalUseCase @Inject constructor(
    private val calcularSaldoDiaUseCase: CalcularSaldoDiaUseCase,
    private val versaoJornadaRepository: VersaoJornadaRepository
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
        val trabalhadoFormatado: String get() = trabalhadoMinutos.minutosParaHoraMinuto()
        val esperadoFormatado: String get() = esperadoMinutos.minutosParaHoraMinuto()
        val saldoFormatado: String get() = saldoMinutos.minutosParaSaldoFormatado()
    }

    suspend operator fun invoke(empregoId: Long, mes: YearMonth): SaldoMensal {
        val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId)
        val diaInicio = versaoVigente?.diaInicioFechamentoRH ?: 1

        val dataInicio = calcularDataInicio(mes, diaInicio)
        val dataFim = calcularDataFim(mes, diaInicio)

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

        return SaldoMensal(mes, trabalhadoMinutos, esperadoMinutos, trabalhadoMinutos - esperadoMinutos, diasTrabalhados, diasUteis, saldosDiarios)
    }

    private fun calcularDataInicio(mes: YearMonth, diaInicio: Int): LocalDate =
        if (diaInicio == 1) mes.atDay(1) else mes.minusMonths(1).atDay(diaInicio)

    private fun calcularDataFim(mes: YearMonth, diaInicio: Int): LocalDate =
        if (diaInicio == 1) mes.atEndOfMonth() else mes.atDay(diaInicio - 1)
}
