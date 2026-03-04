// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/banco/VerificarCicloPendenteUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.banco

import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * UseCase para verificar se há ciclo de banco de horas pendente de fechamento.
 *
 * Um ciclo está pendente quando:
 * - dataAtual > dataFimCiclo (o ciclo já terminou)
 * - Não foi criado um FechamentoPeriodo para este ciclo ainda
 *
 * @author Thiago
 * @since 6.2.0
 * @updated 8.0.0 - Migrado para usar VersaoJornadaRepository
 */
class VerificarCicloPendenteUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase
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
        val versaoJornada = versaoJornadaRepository.buscarVigente(empregoId)
            ?: return Resultado.SemVersaoJornada

        if (!versaoJornada.temBancoHoras) {
            return Resultado.BancoNaoHabilitado
        }

        val dataInicioCiclo = versaoJornada.dataInicioCicloBancoAtual
            ?: return Resultado.CicloNaoConfigurado

        val dataFimCiclo = versaoJornada.calcularDataFimCicloAtual()
            ?: return Resultado.CicloNaoConfigurado

        val diasParaFim = ChronoUnit.DAYS.between(dataAtual, dataFimCiclo).toInt()

        return when {
            // Ciclo já encerrou - precisa fechar
            dataAtual.isAfter(dataFimCiclo) -> {
                val resultado = calcularBancoHorasUseCase.calcularParaPeriodo(
                    empregoId = empregoId,
                    dataInicio = dataInicioCiclo,
                    dataFim = dataFimCiclo
                )

                Resultado.CicloPendente(
                    ciclo = CicloBancoHoras(
                        dataInicio = dataInicioCiclo,
                        dataFim = dataFimCiclo,
                        saldoAtualMinutos = resultado.bancoHoras.saldoTotalMinutos,
                        isCicloAtual = false
                    ),
                    diasAposVencimento = -diasParaFim
                )
            }

            // Ciclo próximo do fim - aviso
            diasParaFim <= versaoJornada.diasUteisLembreteFechamento -> {
                val resultado = calcularBancoHorasUseCase.calcularParaPeriodo(
                    empregoId = empregoId,
                    dataInicio = dataInicioCiclo,
                    dataFim = dataAtual
                )

                Resultado.CicloProximoDoFim(
                    ciclo = CicloBancoHoras(
                        dataInicio = dataInicioCiclo,
                        dataFim = dataFimCiclo,
                        saldoAtualMinutos = resultado.bancoHoras.saldoTotalMinutos,
                        isCicloAtual = true
                    ),
                    diasRestantes = diasParaFim
                )
            }

            // Ciclo normal - sem pendências
            else -> {
                val resultado = calcularBancoHorasUseCase.calcularParaPeriodo(
                    empregoId = empregoId,
                    dataInicio = dataInicioCiclo,
                    dataFim = dataAtual
                )

                Resultado.CicloEmAndamento(
                    ciclo = CicloBancoHoras(
                        dataInicio = dataInicioCiclo,
                        dataFim = dataFimCiclo,
                        saldoAtualMinutos = resultado.bancoHoras.saldoTotalMinutos,
                        isCicloAtual = true
                    ),
                    diasRestantes = diasParaFim
                )
            }
        }
    }

    sealed class Resultado {
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

        data class CicloEmAndamento(
            val ciclo: CicloBancoHoras,
            val diasRestantes: Int
        ) : Resultado()

        data object SemVersaoJornada : Resultado()
        data object BancoNaoHabilitado : Resultado()
        data object CicloNaoConfigurado : Resultado()
    }
}
