// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/banco/VerificarCicloPendenteUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.banco

import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.usecase.saldo.CalcularSaldoPeriodoUseCase
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * UseCase para verificar se há ciclo de banco de horas pendente de fechamento.
 *
 * Um ciclo está pendente quando:
 * - dataAtual >= dataFimCiclo (o ciclo já terminou)
 * - Não foi criado um FechamentoPeriodo para este ciclo ainda
 *
 * @author Thiago
 * @since 6.2.0
 */
class VerificarCicloPendenteUseCase @Inject constructor(
    private val configuracaoRepository: ConfiguracaoEmpregoRepository,
    private val calcularSaldoPeriodoUseCase: CalcularSaldoPeriodoUseCase
) {

    /**
     * Verifica se há ciclo pendente de fechamento.
     *
     * @param empregoId ID do emprego
     * @param dataAtual Data de referência (default: hoje)
     * @return Resultado da verificação
     */
    suspend operator fun invoke(
        empregoId: Long,
        dataAtual: LocalDate = LocalDate.now()
    ): Resultado {
        val configuracao = configuracaoRepository.buscarPorEmpregoId(empregoId)
            ?: return Resultado.SemConfiguracao

        if (!configuracao.temBancoHoras) {
            return Resultado.BancoNaoHabilitado
        }

        val dataInicioCiclo = configuracao.dataInicioCicloBancoAtual
            ?: return Resultado.CicloNaoConfigurado

        val dataFimCiclo = configuracao.calcularDataFimCicloAtual()
            ?: return Resultado.CicloNaoConfigurado

        // Calcular dias restantes ou passados
        val diasParaFim = ChronoUnit.DAYS.between(dataAtual, dataFimCiclo).toInt()

        return when {
            // Ciclo já encerrou - precisa fechar
            dataAtual.isAfter(dataFimCiclo) -> {
                val saldo = calcularSaldoPeriodoUseCase(
                    empregoId = empregoId,
                    dataInicio = dataInicioCiclo,
                    dataFim = dataFimCiclo
                )

                Resultado.CicloPendente(
                    ciclo = CicloBancoHoras(
                        dataInicio = dataInicioCiclo,
                        dataFim = dataFimCiclo,
                        saldoAtualMinutos = saldo.saldoTotalMinutos,
                        isCicloAtual = false
                    ),
                    diasAposVencimento = -diasParaFim
                )
            }

            // Ciclo próximo do fim - aviso
            diasParaFim <= configuracao.diasUteisLembreteFechamento -> {
                val saldo = calcularSaldoPeriodoUseCase(
                    empregoId = empregoId,
                    dataInicio = dataInicioCiclo,
                    dataFim = dataAtual
                )

                Resultado.CicloProximoDoFim(
                    ciclo = CicloBancoHoras(
                        dataInicio = dataInicioCiclo,
                        dataFim = dataFimCiclo,
                        saldoAtualMinutos = saldo.saldoTotalMinutos,
                        isCicloAtual = true
                    ),
                    diasRestantes = diasParaFim
                )
            }

            // Ciclo normal - sem pendências
            else -> {
                val saldo = calcularSaldoPeriodoUseCase(
                    empregoId = empregoId,
                    dataInicio = dataInicioCiclo,
                    dataFim = dataAtual
                )

                Resultado.CicloEmAndamento(
                    ciclo = CicloBancoHoras(
                        dataInicio = dataInicioCiclo,
                        dataFim = dataFimCiclo,
                        saldoAtualMinutos = saldo.saldoTotalMinutos,
                        isCicloAtual = true
                    ),
                    diasRestantes = diasParaFim
                )
            }
        }
    }

    sealed class Resultado {
        /**
         * Ciclo encerrado, pendente de fechamento.
         */
        data class CicloPendente(
            val ciclo: CicloBancoHoras,
            val diasAposVencimento: Int
        ) : Resultado() {
            val mensagem: String
                get() = if (diasAposVencimento == 1) {
                    "Ciclo encerrou ontem"
                } else {
                    "Ciclo encerrou há $diasAposVencimento dias"
                }
        }

        /**
         * Ciclo próximo do fim, aviso preventivo.
         */
        data class CicloProximoDoFim(
            val ciclo: CicloBancoHoras,
            val diasRestantes: Int
        ) : Resultado() {
            val mensagem: String
                get() = when (diasRestantes) {
                    0 -> "Ciclo encerra hoje"
                    1 -> "Ciclo encerra amanhã"
                    else -> "Ciclo encerra em $diasRestantes dias"
                }
        }

        /**
         * Ciclo em andamento normal.
         */
        data class CicloEmAndamento(
            val ciclo: CicloBancoHoras,
            val diasRestantes: Int
        ) : Resultado()

        data object SemConfiguracao : Resultado()
        data object BancoNaoHabilitado : Resultado()
        data object CicloNaoConfigurado : Resultado()
    }
}
