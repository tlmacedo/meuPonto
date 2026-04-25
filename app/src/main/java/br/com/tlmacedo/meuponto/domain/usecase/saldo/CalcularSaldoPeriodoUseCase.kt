// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/saldo/CalcularSaldoPeriodoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.PeriodoRH
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * @author Thiago
 * @since 3.0.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
class CalcularSaldoPeriodoUseCase @Inject constructor(
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val calcularSaldoDiaUseCase: CalcularSaldoDiaUseCase
) {
    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): BancoHoras {
        var saldoTotalMinutos = 0L

        var dataAtual = dataInicio
        while (!dataAtual.isAfter(dataFim)) {
            val saldoDia = calcularSaldoDiaUseCase(empregoId, dataAtual)
            saldoTotalMinutos += saldoDia.saldoMinutos
            dataAtual = dataAtual.plusDays(1)
        }

        val ajustes = ajusteSaldoRepository.buscarPorPeriodo(empregoId, dataInicio, dataFim)
        saldoTotalMinutos += ajustes.sumOf { it.minutos.toLong() }

        return BancoHoras(saldoTotal = Duration.ofMinutes(saldoTotalMinutos))
    }

    suspend fun calcularSaldoPeriodoRH(
        empregoId: Long,
        dataReferencia: LocalDate = LocalDate.now()
    ): Pair<PeriodoRH, BancoHoras> {
        val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId)
            ?: return PeriodoRH.criarPara(dataReferencia, 1) to BancoHoras()

        val periodo = PeriodoRH.criarPara(dataReferencia, versaoVigente.diaInicioFechamentoRH)
        val dataFimCalculo = minOf(dataReferencia, periodo.dataFim)
        val saldo = invoke(empregoId, periodo.dataInicio, dataFimCalculo)

        return periodo.copy(saldoMinutos = saldo.saldoTotalMinutos) to saldo
    }

    suspend fun calcularSaldoCicloBancoAtual(
        empregoId: Long,
        dataReferencia: LocalDate = LocalDate.now()
    ): BancoHoras {
        val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId) ?: return BancoHoras()

        if (!versaoVigente.temBancoHoras) return BancoHoras()

        val dataInicioCiclo = versaoVigente.dataInicioCicloBancoAtual ?: return BancoHoras()
        val dataFimCiclo = versaoVigente.calcularDataFimCicloAtual() ?: return BancoHoras()
        val dataFimCalculo = minOf(dataReferencia, dataFimCiclo)

        if (dataReferencia.isBefore(dataInicioCiclo)) return BancoHoras()

        return invoke(empregoId, dataInicioCiclo, dataFimCalculo)
    }
}
