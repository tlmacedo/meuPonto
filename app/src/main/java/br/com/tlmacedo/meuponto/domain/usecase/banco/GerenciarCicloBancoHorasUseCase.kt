// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/banco/GerenciarCicloBancoHorasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.banco

import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.usecase.saldo.CalcularSaldoPeriodoUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UseCase responsável pelo gerenciamento automático dos ciclos do banco de horas.
 *
 * @author Thiago
 * @since 3.0.0
 */
class GerenciarCicloBancoHorasUseCase @Inject constructor(
    private val configuracaoRepository: ConfiguracaoEmpregoRepository,
    private val fechamentoRepository: FechamentoPeriodoRepository,
    private val calcularSaldoPeriodoUseCase: CalcularSaldoPeriodoUseCase
) {
    /**
     * Verifica e processa a transição de ciclo se necessário.
     */
    suspend operator fun invoke(
        empregoId: Long,
        dataAtual: LocalDate = LocalDate.now()
    ): Resultado {
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
            ?: return Resultado.Erro("Configuração não encontrada")

        if (!configuracao.temBancoHoras) {
            return Resultado.BancoNaoHabilitado
        }

        val dataInicioCiclo = configuracao.dataInicioCicloBancoAtual
            ?: return Resultado.Erro("Data de início do ciclo não configurada")

        val dataInicioProximoCiclo = configuracao.calcularDataInicioProximoCiclo()
            ?: return Resultado.Erro("Não foi possível calcular próximo ciclo")

        return if (!dataAtual.isBefore(dataInicioProximoCiclo)) {
            processarTransicaoCiclo(
                empregoId = empregoId,
                configuracao = configuracao,
                dataInicioCicloAnterior = dataInicioCiclo,
                dataInicioNovoCiclo = dataInicioProximoCiclo,
                dataAtual = dataAtual
            )
        } else {
            val cicloAtual = obterCicloAtual(empregoId, configuracao, dataAtual)
            Resultado.CicloAtualValido(cicloAtual)
        }
    }

    private suspend fun processarTransicaoCiclo(
        empregoId: Long,
        configuracao: ConfiguracaoEmprego,
        dataInicioCicloAnterior: LocalDate,
        dataInicioNovoCiclo: LocalDate,
        dataAtual: LocalDate
    ): Resultado {
        var cicloInicio = dataInicioCicloAnterior
        var proximoCicloInicio = dataInicioNovoCiclo
        val ciclosFechados = mutableListOf<FechamentoPeriodo>()

        while (!dataAtual.isBefore(proximoCicloInicio)) {
            val dataFimCiclo = proximoCicloInicio.minusDays(1)

            val saldoCiclo = calcularSaldoPeriodoUseCase(
                empregoId = empregoId,
                dataInicio = cicloInicio,
                dataFim = dataFimCiclo
            )

            val fechamento = FechamentoPeriodo(
                empregoId = empregoId,
                dataFechamento = proximoCicloInicio,
                dataInicioPeriodo = cicloInicio,
                dataFimPeriodo = dataFimCiclo,
                saldoAnteriorMinutos = saldoCiclo.saldoTotalMinutos,
                tipo = TipoFechamento.CICLO_BANCO_AUTOMATICO,
                observacao = "Fechamento automático de ciclo do banco de horas"
            )

            fechamentoRepository.inserir(fechamento)
            ciclosFechados.add(fechamento)

            cicloInicio = proximoCicloInicio
            proximoCicloInicio = calcularProximoCiclo(cicloInicio, configuracao)
        }

        // Atualiza a configuração com a nova data de início do ciclo
        val novaConfiguracao = configuracao.copy(
            dataInicioCicloBancoAtual = cicloInicio,
            atualizadoEm = LocalDateTime.now()
        )
        configuracaoRepository.atualizar(novaConfiguracao)

        val cicloAtual = CicloBancoHoras(
            dataInicio = cicloInicio,
            dataFim = proximoCicloInicio.minusDays(1),
            saldoInicialMinutos = 0,
            saldoAtualMinutos = calcularSaldoPeriodoUseCase(
                empregoId = empregoId,
                dataInicio = cicloInicio,
                dataFim = dataAtual
            ).saldoTotalMinutos,
            isCicloAtual = true
        )

        return Resultado.TransicaoRealizada(
            cicloAtual = cicloAtual,
            ciclosFechados = ciclosFechados
        )
    }

    private suspend fun obterCicloAtual(
        empregoId: Long,
        configuracao: ConfiguracaoEmprego,
        dataAtual: LocalDate
    ): CicloBancoHoras {
        val dataInicio = configuracao.dataInicioCicloBancoAtual!!
        val dataFim = configuracao.calcularDataFimCicloAtual()!!

        val saldoAtual = calcularSaldoPeriodoUseCase(
            empregoId = empregoId,
            dataInicio = dataInicio,
            dataFim = minOf(dataAtual, dataFim)
        )

        return CicloBancoHoras(
            dataInicio = dataInicio,
            dataFim = dataFim,
            saldoInicialMinutos = 0,
            saldoAtualMinutos = saldoAtual.saldoTotalMinutos,
            isCicloAtual = true
        )
    }

    private fun calcularProximoCiclo(
        dataInicio: LocalDate,
        configuracao: ConfiguracaoEmprego
    ): LocalDate = when {
        configuracao.periodoBancoSemanas > 0 ->
            dataInicio.plusWeeks(configuracao.periodoBancoSemanas.toLong())
        configuracao.periodoBancoMeses > 0 ->
            dataInicio.plusMonths(configuracao.periodoBancoMeses.toLong())
        else -> dataInicio
    }

    suspend fun obterCicloParaData(
        empregoId: Long,
        data: LocalDate
    ): CicloBancoHoras? {
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
            ?: return null

        if (!configuracao.temBancoHoras) return null

        val dataInicioCicloAtual = configuracao.dataInicioCicloBancoAtual
            ?: return null

        if (data.isBefore(dataInicioCicloAtual)) {
            val fechamento = fechamentoRepository.buscarPorData(empregoId, data)
            return fechamento?.let {
                CicloBancoHoras(
                    dataInicio = it.dataInicioPeriodo,
                    dataFim = it.dataFimPeriodo,
                    saldoInicialMinutos = 0,
                    saldoAtualMinutos = it.saldoAnteriorMinutos,
                    fechamento = it,
                    isCicloAtual = false
                )
            }
        }

        return obterCicloAtual(empregoId, configuracao, data)
    }

    sealed class Resultado {
        data class CicloAtualValido(val ciclo: CicloBancoHoras) : Resultado()
        data class TransicaoRealizada(
            val cicloAtual: CicloBancoHoras,
            val ciclosFechados: List<FechamentoPeriodo>
        ) : Resultado()
        data object BancoNaoHabilitado : Resultado()
        data class Erro(val mensagem: String) : Resultado()
    }
}
