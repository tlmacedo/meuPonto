// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/banco/InicializarCiclosRetroativosUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.banco

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.saldo.CalcularSaldoPeriodoUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UseCase responsável por inicializar ciclos retroativos do banco de horas.
 *
 * Baseado na dataInicioCicloBancoAtual informada pelo usuário, calcula ciclos
 * anteriores retroativamente até encontrar o primeiro registro de ponto.
 *
 * Exemplo:
 * - dataInicioCicloBancoAtual = 11/02/2026, período = 6 meses
 * - Primeiro ponto = 15/03/2025
 * - Ciclos a criar:
 *   - 11/02/2025 ~ 10/08/2025 (contém primeiro ponto)
 *   - 11/08/2025 ~ 10/02/2026 (ciclo anterior ao atual)
 *
 * @author Thiago
 * @since 6.2.0
 */
class InicializarCiclosRetroativosUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val configuracaoRepository: ConfiguracaoEmpregoRepository,
    private val fechamentoRepository: FechamentoPeriodoRepository,
    private val calcularSaldoPeriodoUseCase: CalcularSaldoPeriodoUseCase
) {

    /**
     * Inicializa os ciclos retroativos para um emprego.
     *
     * @param empregoId ID do emprego
     * @param forcarRecriacao Se true, recria mesmo se já existirem fechamentos
     * @return Resultado da operação
     */
    suspend operator fun invoke(
        empregoId: Long,
        forcarRecriacao: Boolean = false
    ): Resultado {
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
            ?: return Resultado.Erro("Configuração não encontrada")

        if (!configuracao.temBancoHoras) {
            return Resultado.BancoNaoHabilitado
        }

        val dataInicioCicloAtual = configuracao.dataInicioCicloBancoAtual
            ?: return Resultado.Erro("Data de início do ciclo não configurada")

        // Verificar se já existem fechamentos (evita reprocessar)
        val fechamentosExistentes = fechamentoRepository.buscarPorEmpregoId(empregoId)
            .filter { it.tipo == TipoFechamento.CICLO_BANCO_AUTOMATICO }

        if (fechamentosExistentes.isNotEmpty() && !forcarRecriacao) {
            return Resultado.JaInicializado(fechamentosExistentes.size)
        }

        // Buscar primeiro ponto registrado
        val primeiroPonto = pontoRepository.buscarPrimeiraData(empregoId)
            ?: return Resultado.SemRegistros

        // Calcular ciclos retroativos
        val ciclosRetroativos = calcularCiclosRetroativos(
            configuracao = configuracao,
            dataInicioCicloAtual = dataInicioCicloAtual,
            dataPrimeiroPonto = primeiroPonto
        )

        if (ciclosRetroativos.isEmpty()) {
            return Resultado.Sucesso(
                ciclosCriados = 0,
                mensagem = "Nenhum ciclo retroativo necessário"
            )
        }

        // Criar fechamentos para cada ciclo retroativo
        val fechamentosCriados = mutableListOf<FechamentoPeriodo>()

        for (ciclo in ciclosRetroativos) {
            // Verificar se já existe fechamento para este período
            val jaExiste = fechamentosExistentes.any {
                it.dataInicioPeriodo == ciclo.dataInicio && it.dataFimPeriodo == ciclo.dataFim
            }

            if (jaExiste && !forcarRecriacao) continue

            // Calcular saldo do ciclo
            val saldoCiclo = calcularSaldoPeriodoUseCase(
                empregoId = empregoId,
                dataInicio = ciclo.dataInicio,
                dataFim = ciclo.dataFim
            )

            // Criar fechamento
            val fechamento = FechamentoPeriodo(
                empregoId = empregoId,
                dataFechamento = ciclo.dataFim.plusDays(1),
                dataInicioPeriodo = ciclo.dataInicio,
                dataFimPeriodo = ciclo.dataFim,
                saldoAnteriorMinutos = saldoCiclo.saldoTotalMinutos,
                tipo = TipoFechamento.CICLO_BANCO_AUTOMATICO,
                observacao = "Fechamento retroativo gerado automaticamente",
                criadoEm = LocalDateTime.now()
            )

            fechamentoRepository.inserir(fechamento)
            fechamentosCriados.add(fechamento)
        }

        return Resultado.Sucesso(
            ciclosCriados = fechamentosCriados.size,
            mensagem = "Criados ${fechamentosCriados.size} ciclo(s) retroativo(s)"
        )
    }

    /**
     * Calcula os ciclos retroativos necessários.
     */
    private fun calcularCiclosRetroativos(
        configuracao: ConfiguracaoEmprego,
        dataInicioCicloAtual: LocalDate,
        dataPrimeiroPonto: LocalDate
    ): List<CicloInfo> {
        val ciclos = mutableListOf<CicloInfo>()
        var dataInicioCiclo = dataInicioCicloAtual

        // Retroceder ciclo por ciclo até antes do primeiro ponto
        while (true) {
            val dataInicioCicloAnterior = calcularInicioCicloAnterior(dataInicioCiclo, configuracao)
            val dataFimCicloAnterior = dataInicioCiclo.minusDays(1)

            // Se o início do ciclo anterior é posterior ao primeiro ponto,
            // significa que há registros nesse ciclo
            if (dataInicioCicloAnterior.isAfter(dataPrimeiroPonto)) {
                ciclos.add(
                    CicloInfo(
                        dataInicio = dataInicioCicloAnterior,
                        dataFim = dataFimCicloAnterior
                    )
                )
                dataInicioCiclo = dataInicioCicloAnterior
            } else {
                // Último ciclo que contém o primeiro ponto
                if (dataPrimeiroPonto <= dataFimCicloAnterior) {
                    ciclos.add(
                        CicloInfo(
                            dataInicio = dataInicioCicloAnterior,
                            dataFim = dataFimCicloAnterior
                        )
                    )
                }
                break
            }

            // Limite de segurança (máximo 10 anos de ciclos)
            if (ciclos.size > 20) break
        }

        // Retornar em ordem cronológica (mais antigo primeiro)
        return ciclos.reversed()
    }

    private fun calcularInicioCicloAnterior(
        dataInicioCicloAtual: LocalDate,
        configuracao: ConfiguracaoEmprego
    ): LocalDate = when {
        configuracao.periodoBancoSemanas > 0 ->
            dataInicioCicloAtual.minusWeeks(configuracao.periodoBancoSemanas.toLong())
        configuracao.periodoBancoMeses > 0 ->
            dataInicioCicloAtual.minusMonths(configuracao.periodoBancoMeses.toLong())
        else -> dataInicioCicloAtual
    }

    private data class CicloInfo(
        val dataInicio: LocalDate,
        val dataFim: LocalDate
    )

    sealed class Resultado {
        data class Sucesso(
            val ciclosCriados: Int,
            val mensagem: String
        ) : Resultado()

        data class JaInicializado(val quantidadeCiclos: Int) : Resultado()
        data object SemRegistros : Resultado()
        data object BancoNaoHabilitado : Resultado()
        data class Erro(val mensagem: String) : Resultado()
    }
}
