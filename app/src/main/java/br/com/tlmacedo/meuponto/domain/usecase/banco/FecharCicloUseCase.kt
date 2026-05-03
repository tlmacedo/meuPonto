// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/banco/FecharCicloUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.banco

import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.abs
import timber.log.Timber

/**
 * UseCase responsável por executar o fechamento de um ciclo do banco de horas.
 *
 * O fechamento:
 * 1. Busca o saldo acumulado DO PERÍODO do ciclo (dataInicio ~ dataFim)
 * 2. Cria um FechamentoPeriodo com o registro histórico
 * 3. Se houver saldo != 0, cria um AjusteSaldo para zerar A PARTIR do próximo ciclo
 * 4. Atualiza a dataInicioCicloBancoAtual na VersaoJornada para o próximo ciclo
 *
 * IMPORTANTE: O fechamento NÃO altera saldos de dias anteriores.
 * O ajuste de zeramento é aplicado na data de INÍCIO do novo ciclo.
 *
 * @author Thiago
 * @since 6.2.0
 * @updated 8.0.0 - Migrado para usar VersaoJornadaRepository
 */
class FecharCicloUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val fechamentoRepository: FechamentoPeriodoRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase,
    private val notificarTransicaoCiclo: NotificarTransicaoCicloUseCase
) {

    private val formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /**
     * Executa o fechamento do ciclo atual.
     *
     * @param empregoId ID do emprego
     * @param observacao Observação opcional do usuário
     * @return Resultado do fechamento
     */
    suspend operator fun invoke(
        empregoId: Long,
        observacao: String? = null
    ): Resultado {
        val versaoJornada = versaoJornadaRepository.buscarVigente(empregoId)
            ?: return Resultado.Erro("Versão de jornada não encontrada")

        if (!versaoJornada.temBancoHoras) {
            return Resultado.Erro("Banco de horas não habilitado")
        }

        val dataInicioCiclo = versaoJornada.dataInicioCicloBancoAtual
            ?: return Resultado.Erro("Data de início do ciclo não configurada")

        val dataFimCiclo = versaoJornada.calcularDataFimCicloAtual()
            ?: return Resultado.Erro("Não foi possível calcular fim do ciclo")

        val dataInicioProximoCiclo = versaoJornada.calcularDataInicioProximoCiclo()
            ?: return Resultado.Erro("Não foi possível calcular próximo ciclo")

        Timber.d("═══════════════════════════════════════════════")
        Timber.d("Fechando ciclo: $dataInicioCiclo ~ $dataFimCiclo")
        Timber.d("Próximo ciclo inicia em: $dataInicioProximoCiclo")

        // ═══════════════════════════════════════════════════════════════════
        // IMPORTANTE: Calcular o saldo EXATAMENTE do período do ciclo!
        // ═══════════════════════════════════════════════════════════════════
        val resultadoBanco = calcularBancoHorasUseCase.calcularParaPeriodo(
            empregoId = empregoId,
            dataInicio = dataInicioCiclo,
            dataFim = dataFimCiclo
        )

        val saldoMinutos = resultadoBanco.bancoHoras.saldoTotalMinutos
        val agora = LocalDateTime.now()

        Timber.d("Saldo do ciclo: $saldoMinutos min (${formatarMinutos(saldoMinutos)})")
        Timber.d("  - Dias trabalhados: ${resultadoBanco.diasTrabalhados}")
        Timber.d("  - Dias com ausência: ${resultadoBanco.diasComAusencia}")
        Timber.d("  - Dias úteis sem registro: ${resultadoBanco.diasUteisSemRegistro}")
        Timber.d("  - Ajustes no período: ${resultadoBanco.totalAjustesMinutos}")

        // 1. Criar registro de fechamento (histórico)
        val fechamento = FechamentoPeriodo(
            empregoId = empregoId,
            dataFechamento = LocalDate.now(),
            dataInicioPeriodo = dataInicioCiclo,
            dataFimPeriodo = dataFimCiclo,
            saldoAnteriorMinutos = saldoMinutos,
            tipo = TipoFechamento.CICLO_BANCO_AUTOMATICO,
            observacao = observacao ?: "Fechamento de ciclo do banco de horas",
            criadoEm = agora
        )
        val fechamentoId = fechamentoRepository.inserir(fechamento)
        Timber.d("Fechamento criado com ID: $fechamentoId")

        notificarTransicaoCiclo(listOf(fechamento.copy(id = fechamentoId)))

        // 2. Criar ajuste de zeramento (SEMPRE, mesmo que saldo seja zero)
        val ajusteZeramento = AjusteSaldo(
            empregoId = empregoId,
            data = dataInicioProximoCiclo,
            minutos = -saldoMinutos,
            justificativa = buildString {
                append("Zeramento de ciclo anterior")
                append(
                    " (${dataInicioCiclo.format(formatadorData)} ~ ${
                        dataFimCiclo.format(
                            formatadorData
                        )
                    })"
                )
                if (saldoMinutos != 0) {
                    append(" - Saldo zerado: ${formatarMinutos(saldoMinutos)}")
                } else {
                    append(" - Ciclo encerrado zerado")
                }
            },
            criadoEm = agora
        )
        val ajusteId = ajusteSaldoRepository.inserir(ajusteZeramento)
        Timber.d("Ajuste criado com ID: $ajusteId, valor: ${-saldoMinutos} min")

        // 3. Atualizar VersaoJornada para próximo ciclo
        val novaVersaoJornada = versaoJornada.copy(
            dataInicioCicloBancoAtual = dataInicioProximoCiclo,
            atualizadoEm = agora
        )
        versaoJornadaRepository.atualizar(novaVersaoJornada)
        Timber.d("VersaoJornada atualizada: novo ciclo inicia em $dataInicioProximoCiclo")

        // Montar ciclo para retorno
        val cicloFechado = CicloBancoHoras(
            dataInicio = dataInicioCiclo,
            dataFim = dataFimCiclo,
            saldoAtualMinutos = saldoMinutos,
            fechamento = fechamento.copy(id = fechamentoId),
            isCicloAtual = false
        )

        val novoCiclo = CicloBancoHoras(
            dataInicio = dataInicioProximoCiclo,
            dataFim = calcularDataFimCiclo(dataInicioProximoCiclo, versaoJornada),
            saldoInicialMinutos = 0,
            saldoAtualMinutos = 0,
            isCicloAtual = true
        )

        Timber.d("═══════════════════════════════════════════════")

        return Resultado.Sucesso(
            cicloFechado = cicloFechado,
            novoCiclo = novoCiclo,
            saldoZerado = saldoMinutos
        )
    }

    /**
     * Fecha múltiplos ciclos pendentes de uma vez.
     */
    suspend fun fecharCiclosPendentes(
        empregoId: Long,
        dataAtual: LocalDate = LocalDate.now()
    ): ResultadoMultiplo {
        val ciclosFechados = mutableListOf<CicloBancoHoras>()
        var ultimoResultado: Resultado? = null

        repeat(20) { iteracao ->
            val versaoJornada = versaoJornadaRepository.buscarVigente(empregoId)
                ?: return ResultadoMultiplo.Erro("Versão de jornada não encontrada")

            val dataFimCiclo = versaoJornada.calcularDataFimCicloAtual()
                ?: return ResultadoMultiplo.Erro("Ciclo não configurado")

            if (!dataAtual.isAfter(dataFimCiclo)) {
                Timber.d("Ciclo atual ($dataFimCiclo) ainda não terminou. Parando.")
                return@repeat
            }

            Timber.d("═══════════════════════════════════════════════")
            Timber.d("Fechando ciclo pendente #${iteracao + 1}")

            when (val resultado = invoke(empregoId)) {
                is Resultado.Sucesso -> {
                    ciclosFechados.add(resultado.cicloFechado)
                    ultimoResultado = resultado
                    Timber.d("Ciclo fechado com saldo: ${resultado.saldoZerado} min")
                }

                is Resultado.Erro -> {
                    Timber.e("Erro ao fechar ciclo: ${resultado.mensagem}")
                    return ResultadoMultiplo.Erro(resultado.mensagem)
                }
            }
        }

        return if (ciclosFechados.isEmpty()) {
            ResultadoMultiplo.NenhumPendente
        } else {
            ResultadoMultiplo.Sucesso(
                ciclosFechados = ciclosFechados,
                novoCiclo = (ultimoResultado as? Resultado.Sucesso)?.novoCiclo
            )
        }
    }

    private fun calcularDataFimCiclo(
        dataInicio: LocalDate,
        versaoJornada: VersaoJornada
    ): LocalDate = when {
        versaoJornada.periodoBancoSemanas > 0 ->
            dataInicio.plusWeeks(versaoJornada.periodoBancoSemanas.toLong()).minusDays(1)

        versaoJornada.periodoBancoMeses > 0 ->
            dataInicio.plusMonths(versaoJornada.periodoBancoMeses.toLong()).minusDays(1)

        else -> dataInicio
    }

    private fun formatarMinutos(minutos: Int): String {
        val sinal = if (minutos >= 0) "+" else "-"
        val total = abs(minutos)
        val horas = total / 60
        val mins = total % 60
        return "$sinal${String.format("%02d:%02d", horas, mins)}"
    }

    sealed class Resultado {
        data class Sucesso(
            val cicloFechado: CicloBancoHoras,
            val novoCiclo: CicloBancoHoras,
            val saldoZerado: Int
        ) : Resultado()

        data class Erro(val mensagem: String) : Resultado()
    }

    sealed class ResultadoMultiplo {
        data class Sucesso(
            val ciclosFechados: List<CicloBancoHoras>,
            val novoCiclo: CicloBancoHoras?
        ) : ResultadoMultiplo()

        data object NenhumPendente : ResultadoMultiplo()
        data class Erro(val mensagem: String) : ResultadoMultiplo()
    }
}
