// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/banco/GerenciarCicloBancoHorasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.banco

import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.saldo.CalcularSaldoPeriodoUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UseCase responsável pelo gerenciamento automático dos ciclos do banco de horas.
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 8.0.0 - Migrado para usar VersaoJornadaRepository
 */
class GerenciarCicloBancoHorasUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
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
        val versaoJornada = versaoJornadaRepository.buscarVigente(empregoId)
            ?: return Resultado.Erro("Versão de jornada não encontrada")

        if (!versaoJornada.temBancoHoras) {
            return Resultado.BancoNaoHabilitado
        }

        val dataInicioCiclo = versaoJornada.dataInicioCicloBancoAtual
            ?: return Resultado.Erro("Data de início do ciclo não configurada")

        val dataInicioProximoCiclo = versaoJornada.calcularDataInicioProximoCiclo()
            ?: return Resultado.Erro("Não foi possível calcular próximo ciclo")

        return if (!dataAtual.isBefore(dataInicioProximoCiclo)) {
            processarTransicaoCiclo(
                empregoId = empregoId,
                versaoJornada = versaoJornada,
                dataInicioCicloAnterior = dataInicioCiclo,
                dataInicioNovoCiclo = dataInicioProximoCiclo,
                dataAtual = dataAtual
            )
        } else {
            val cicloAtual = obterCicloAtual(empregoId, versaoJornada, dataAtual)
            Resultado.CicloAtualValido(cicloAtual)
        }
    }

    private suspend fun processarTransicaoCiclo(
        empregoId: Long,
        versaoJornada: VersaoJornada,
        dataInicioCicloAnterior: LocalDate,
        dataInicioNovoCiclo: LocalDate,
        dataAtual: LocalDate
    ): Resultado {
        var cicloInicio = dataInicioCicloAnterior
        var proximoCicloInicio = dataInicioNovoCiclo
        val ciclosFechados = mutableListOf<FechamentoPeriodo>()
        var versaoAtual = versaoJornada

        while (!dataAtual.isBefore(proximoCicloInicio)) {
            val dataFimCiclo = proximoCicloInicio.minusDays(1)

            val saldoCiclo = calcularSaldoPeriodoUseCase(
                empregoId = empregoId,
                dataInicio = cicloInicio,
                dataFim = dataFimCiclo
            )

            val ultimoFechamento = fechamentoRepository.buscarUltimoFechamentoBancoAteData(empregoId, cicloInicio.minusDays(1))
            val saldoAnterior = ultimoFechamento?.saldoAnteriorMinutos ?: 0
            val saldoAcumulado = saldoCiclo.saldoTotalMinutos + saldoAnterior

            val fechamento = FechamentoPeriodo(
                empregoId = empregoId,
                dataFechamento = proximoCicloInicio,
                dataInicioPeriodo = cicloInicio,
                dataFimPeriodo = dataFimCiclo,
                saldoAnteriorMinutos = saldoAcumulado,
                tipo = TipoFechamento.CICLO_BANCO_AUTOMATICO,
                observacao = "Fechamento automático de ciclo do banco de horas"
            )

            fechamentoRepository.inserir(fechamento)
            ciclosFechados.add(fechamento)

            cicloInicio = proximoCicloInicio
            proximoCicloInicio = calcularProximoCiclo(cicloInicio, versaoAtual)
        }

        // Atualizar VersaoJornada com a nova data de início do ciclo
        val novaVersaoJornada = versaoAtual.copy(
            dataInicioCicloBancoAtual = cicloInicio,
            atualizadoEm = LocalDateTime.now()
        )
        versaoJornadaRepository.atualizar(novaVersaoJornada)

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
        versaoJornada: VersaoJornada,
        dataAtual: LocalDate
    ): CicloBancoHoras {
        val dataInicio = versaoJornada.dataInicioCicloBancoAtual!!
        val dataFim = versaoJornada.calcularDataFimCicloAtual()!!

        val saldoPeriodo = calcularSaldoPeriodoUseCase(
            empregoId = empregoId,
            dataInicio = dataInicio,
            dataFim = minOf(dataAtual, dataFim)
        )

        val ultimoFechamento = fechamentoRepository.buscarUltimoFechamentoBancoAteData(empregoId, dataInicio.minusDays(1))
        val saldoInicial = ultimoFechamento?.saldoAnteriorMinutos ?: 0

        return CicloBancoHoras(
            dataInicio = dataInicio,
            dataFim = dataFim,
            saldoInicialMinutos = saldoInicial,
            saldoAtualMinutos = saldoInicial + saldoPeriodo.saldoTotalMinutos,
            isCicloAtual = true
        )
    }

    private fun calcularProximoCiclo(
        dataInicio: LocalDate,
        versaoJornada: VersaoJornada
    ): LocalDate = when {
        versaoJornada.periodoBancoSemanas > 0 ->
            dataInicio.plusWeeks(versaoJornada.periodoBancoSemanas.toLong())
        versaoJornada.periodoBancoMeses > 0 ->
            dataInicio.plusMonths(versaoJornada.periodoBancoMeses.toLong())
        else -> dataInicio
    }

    suspend fun obterCicloParaData(
        empregoId: Long,
        data: LocalDate
    ): CicloBancoHoras? {
        val versaoJornada = versaoJornadaRepository.buscarVigente(empregoId)
            ?: return null

        if (!versaoJornada.temBancoHoras) return null

        val dataInicioCicloAtual = versaoJornada.dataInicioCicloBancoAtual
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

        return obterCicloAtual(empregoId, versaoJornada, data)
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
