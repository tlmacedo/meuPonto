// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/saldo/CalcularSaldoPeriodoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.PeriodoRH
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * UseCase unificado para cálculo de saldo em qualquer período.
 *
 * @author Thiago
 * @since 3.0.0
 */
class CalcularSaldoPeriodoUseCase @Inject constructor(
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val configuracaoRepository: ConfiguracaoEmpregoRepository,
    private val calcularSaldoDiaUseCase: CalcularSaldoDiaUseCase
) {
    /**
     * Calcula o saldo total de um período.
     */
    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): BancoHoras {
        var saldoTotalMinutos = 0L

        // Itera por cada dia do período
        var dataAtual = dataInicio
        while (!dataAtual.isAfter(dataFim)) {
            val saldoDia = calcularSaldoDiaUseCase(empregoId, dataAtual)
            saldoTotalMinutos += saldoDia.saldoMinutos
            dataAtual = dataAtual.plusDays(1)
        }

        // Adiciona ajustes manuais do período
        val ajustes = ajusteSaldoRepository.buscarPorPeriodo(empregoId, dataInicio, dataFim)
        saldoTotalMinutos += ajustes.sumOf { it.minutos.toLong() }

        return BancoHoras(saldoTotal = Duration.ofMinutes(saldoTotalMinutos))
    }

    /**
     * Calcula o saldo do período RH atual.
     */
    suspend fun calcularSaldoPeriodoRH(
        empregoId: Long,
        dataReferencia: LocalDate = LocalDate.now()
    ): Pair<PeriodoRH, BancoHoras> {
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
            ?: return PeriodoRH.criarPara(dataReferencia, 1) to BancoHoras()

        val periodo = PeriodoRH.criarPara(
            dataReferencia = dataReferencia,
            diaInicioFechamento = configuracao.diaInicioFechamentoRH
        )

        val dataFimCalculo = minOf(dataReferencia, periodo.dataFim)
        val saldo = invoke(empregoId, periodo.dataInicio, dataFimCalculo)

        return periodo.copy(saldoMinutos = saldo.saldoTotalMinutos) to saldo
    }

    /**
     * Calcula o saldo do ciclo atual do banco de horas.
     */
    suspend fun calcularSaldoCicloBancoAtual(
        empregoId: Long,
        dataReferencia: LocalDate = LocalDate.now()
    ): BancoHoras {
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
            ?: return BancoHoras()

        if (!configuracao.temBancoHoras) {
            return BancoHoras()
        }

        val dataInicioCiclo = configuracao.dataInicioCicloBancoAtual
            ?: return BancoHoras()

        val dataFimCiclo = configuracao.calcularDataFimCicloAtual()
            ?: return BancoHoras()

        val dataFimCalculo = minOf(dataReferencia, dataFimCiclo)

        if (dataReferencia.isBefore(dataInicioCiclo)) {
            return BancoHoras()
        }

        return invoke(empregoId, dataInicioCiclo, dataFimCalculo)
    }
}
