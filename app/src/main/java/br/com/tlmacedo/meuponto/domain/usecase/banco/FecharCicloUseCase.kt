// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/banco/FecharCicloUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.banco

import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.usecase.saldo.CalcularSaldoPeriodoUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.abs

/**
 * UseCase responsável por executar o fechamento de um ciclo do banco de horas.
 *
 * O fechamento:
 * 1. Calcula o saldo final do ciclo
 * 2. Cria um AjusteSaldo inverso para zerar o banco
 * 3. Cria um FechamentoPeriodo com o registro histórico
 * 4. Atualiza a dataInicioCicloBancoAtual para o próximo ciclo
 *
 * @author Thiago
 * @since 6.2.0
 */
class FecharCicloUseCase @Inject constructor(
    private val configuracaoRepository: ConfiguracaoEmpregoRepository,
    private val fechamentoRepository: FechamentoPeriodoRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val calcularSaldoPeriodoUseCase: CalcularSaldoPeriodoUseCase
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
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
            ?: return Resultado.Erro("Configuração não encontrada")

        if (!configuracao.temBancoHoras) {
            return Resultado.Erro("Banco de horas não habilitado")
        }

        val dataInicioCiclo = configuracao.dataInicioCicloBancoAtual
            ?: return Resultado.Erro("Data de início do ciclo não configurada")

        val dataFimCiclo = configuracao.calcularDataFimCicloAtual()
            ?: return Resultado.Erro("Não foi possível calcular fim do ciclo")

        val dataInicioProximoCiclo = configuracao.calcularDataInicioProximoCiclo()
            ?: return Resultado.Erro("Não foi possível calcular próximo ciclo")

        // Calcular saldo do ciclo até a data de fim
        val saldoCiclo = calcularSaldoPeriodoUseCase(
            empregoId = empregoId,
            dataInicio = dataInicioCiclo,
            dataFim = dataFimCiclo
        )

        val saldoMinutos = saldoCiclo.saldoTotalMinutos
        val agora = LocalDateTime.now()

        // 1. Criar ajuste inverso para zerar o saldo (se houver saldo)
        if (saldoMinutos != 0) {
            val ajusteZeramento = AjusteSaldo(
                empregoId = empregoId,
                data = dataFimCiclo,
                minutos = -saldoMinutos,
                justificativa = buildString {
                    append("Zeramento automático de ciclo")
                    append(" (${dataInicioCiclo.format(formatadorData)} ~ ${dataFimCiclo.format(formatadorData)})")
                    if (saldoMinutos > 0) {
                        append(" - Saldo positivo creditado: ${formatarMinutos(saldoMinutos)}")
                    } else {
                        append(" - Saldo negativo debitado: ${formatarMinutos(saldoMinutos)}")
                    }
                },
                criadoEm = agora
            )
            ajusteSaldoRepository.inserir(ajusteZeramento)
        }

        // 2. Criar registro de fechamento
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
        fechamentoRepository.inserir(fechamento)

        // 3. Atualizar configuração para próximo ciclo
        val novaConfiguracao = configuracao.copy(
            dataInicioCicloBancoAtual = dataInicioProximoCiclo,
            atualizadoEm = agora
        )
        configuracaoRepository.atualizar(novaConfiguracao)

        // Montar ciclo para retorno
        val cicloFechado = CicloBancoHoras(
            dataInicio = dataInicioCiclo,
            dataFim = dataFimCiclo,
            saldoAtualMinutos = saldoMinutos,
            fechamento = fechamento,
            isCicloAtual = false
        )

        val novoCiclo = CicloBancoHoras(
            dataInicio = dataInicioProximoCiclo,
            dataFim = calcularDataFimCiclo(dataInicioProximoCiclo, configuracao),
            saldoInicialMinutos = 0,
            saldoAtualMinutos = 0,
            isCicloAtual = true
        )

        return Resultado.Sucesso(
            cicloFechado = cicloFechado,
            novoCiclo = novoCiclo,
            saldoZerado = saldoMinutos
        )
    }

    /**
     * Fecha múltiplos ciclos pendentes de uma vez.
     * Útil quando o usuário ficou muito tempo sem abrir o app.
     */
    suspend fun fecharCiclosPendentes(
        empregoId: Long,
        dataAtual: LocalDate = LocalDate.now()
    ): ResultadoMultiplo {
        val ciclosFechados = mutableListOf<CicloBancoHoras>()
        var ultimoResultado: Resultado? = null

        // Loop para fechar todos os ciclos pendentes
        repeat(20) {
            val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
                ?: return ResultadoMultiplo.Erro("Configuração não encontrada")

            val dataFimCiclo = configuracao.calcularDataFimCicloAtual()
                ?: return ResultadoMultiplo.Erro("Ciclo não configurado")

            // Se o ciclo atual ainda não terminou, paramos
            if (!dataAtual.isAfter(dataFimCiclo)) {
                return@repeat
            }

            // Fechar ciclo
            when (val resultado = invoke(empregoId)) {
                is Resultado.Sucesso -> {
                    ciclosFechados.add(resultado.cicloFechado)
                    ultimoResultado = resultado
                }
                is Resultado.Erro -> {
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
        configuracao: ConfiguracaoEmprego
    ): LocalDate = when {
        configuracao.periodoBancoSemanas > 0 ->
            dataInicio.plusWeeks(configuracao.periodoBancoSemanas.toLong()).minusDays(1)
        configuracao.periodoBancoMeses > 0 ->
            dataInicio.plusMonths(configuracao.periodoBancoMeses.toLong()).minusDays(1)
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
