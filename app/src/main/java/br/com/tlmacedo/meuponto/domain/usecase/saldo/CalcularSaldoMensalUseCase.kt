// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/saldo/CalcularSaldoMensalUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.util.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.minutosParaSaldoFormatado
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * @author Thiago
 * @since 1.0.0
 * @updated 8.0.0 - Migrado para usar CalcularBancoHorasUseCase para consistência
 */
class CalcularSaldoMensalUseCase @Inject constructor(
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase,
    private val versaoJornadaRepository: VersaoJornadaRepository
) {
    data class SaldoMensal(
        val mes: YearMonth,
        val trabalhadoMinutos: Long,
        val esperadoMinutos: Long,
        val saldoMinutos: Long,
        val diasTrabalhados: Int,
        val diasUteis: Int
    ) {
        val trabalhadoFormatado: String get() = trabalhadoMinutos.minutosParaHoraMinuto()
        val esperadoFormatado: String get() = esperadoMinutos.minutosParaHoraMinuto()
        val saldoFormatado: String get() = saldoMinutos.minutosParaSaldoFormatado()
    }

    suspend operator fun invoke(empregoId: Long, mes: YearMonth): SaldoMensal {
        val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId)
        val diaInicio = versaoVigente?.diaInicioFechamentoRH ?: 1

        val hoje = LocalDate.now()
        val dataReferencia = if (mes == YearMonth.now()) hoje else mes.atDay(1)
        val periodo = br.com.tlmacedo.meuponto.domain.model.PeriodoRH.criarPara(dataReferencia, diaInicio)
        
        val dataInicio = periodo.dataInicio
        val dataFim = periodo.dataFim

        val resultado = calcularBancoHorasUseCase.calcularParaPeriodo(empregoId, dataInicio, dataFim)

        return SaldoMensal(
            mes = mes,
            trabalhadoMinutos = 0, // No momento o dashboard não exige o total trabalhado separado
            esperadoMinutos = 0,   // No momento o dashboard não exige o esperado separado
            saldoMinutos = resultado.saldoTotal.toMinutes(),
            diasTrabalhados = resultado.diasTrabalhados,
            diasUteis = 0
        )
    }
}
