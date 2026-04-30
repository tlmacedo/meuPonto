// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/jornada/ObterContextoJornadaDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.jornada

import br.com.tlmacedo.meuponto.domain.model.ContextoJornadaDia
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * UseCase responsável por montar o contexto completo da jornada de um dia.
 *
 * Este é o ponto único e oficial para buscar:
 * - emprego carregado
 * - configuração do emprego
 * - versão vigente da jornada
 * - horário do dia da semana dentro da versão
 *
 * Isso impede misturar regras de empregos ou versões diferentes.
 */
class ObterContextoJornadaDiaUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) {

    sealed class Resultado {
        data class Sucesso(
            val contexto: ContextoJornadaDia
        ) : Resultado()

        data class EmpregoNaoEncontrado(
            val empregoId: Long
        ) : Resultado()

        data class ConfiguracaoNaoEncontrada(
            val empregoId: Long
        ) : Resultado()

        data class VersaoNaoEncontrada(
            val empregoId: Long,
            val data: LocalDate
        ) : Resultado()

        data class Erro(
            val mensagem: String
        ) : Resultado()
    }

    suspend operator fun invoke(
        empregoId: Long,
        data: LocalDate
    ): Resultado {
        return try {
            val emprego = empregoRepository.buscarPorId(empregoId)
                ?: return Resultado.EmpregoNaoEncontrado(empregoId)

            val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
                ?: return Resultado.ConfiguracaoNaoEncontrada(empregoId)

            val versao = versaoJornadaRepository.buscarPorEmpregoEData(
                empregoId = empregoId,
                data = data
            ) ?: return Resultado.VersaoNaoEncontrada(
                empregoId = empregoId,
                data = data
            )

            if (versao.empregoId != empregoId) {
                return Resultado.Erro(
                    mensagem = "A versão de jornada encontrada pertence a outro emprego. esperado=$empregoId, atual=${versao.empregoId}"
                )
            }

            val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)

            val horarioDia = horarioDiaSemanaRepository.buscarPorVersaoEDia(
                versaoId = versao.id,
                diaSemana = diaSemana
            )

            if (horarioDia != null && horarioDia.versaoJornadaId != versao.id) {
                return Resultado.Erro(
                    mensagem = "O horário do dia pertence a outra versão. esperado=${versao.id}, atual=${horarioDia.versaoJornadaId}"
                )
            }

            val cargaHorarioDia = if (horarioDia?.ativo == true) {
                horarioDia.cargaHorariaMinutos
            } else {
                0
            }

            val jornadaDoDia = if (cargaHorarioDia > 0) {
                cargaHorarioDia + versao.acrescimoMinutosDiasPontes
            } else {
                0
            }

            Resultado.Sucesso(
                ContextoJornadaDia(
                    empregoId = empregoId,
                    data = data,
                    diaSemana = diaSemana,

                    emprego = emprego,
                    configuracaoEmprego = configuracao,
                    versaoJornada = versao,
                    horarioDiaSemana = horarioDia,

                    jornadaDoDiaMinutos = jornadaDoDia,
                    acrescimoDiasPontesMinutos = versao.acrescimoMinutosDiasPontes,

                    intervaloMinimoMinutos =
                        horarioDia?.intervaloMinimoMinutos
                            ?: versao.intervaloMinimoAlmocoMinutos,

                    toleranciaVoltaIntervaloMinutos =
                        horarioDia?.toleranciaIntervaloMaisMinutos
                            ?: versao.toleranciaIntervaloMaisMinutos,

                    turnoMaximoMinutos = versao.turnoMaximoMinutos,
                    jornadaMaximaDiariaMinutos = versao.jornadaMaximaDiariaMinutos,
                    intervaloMinimoInterjornadaMinutos = versao.intervaloMinimoInterjornadaMinutos,

                    entradaIdeal = horarioDia?.entradaIdeal,
                    saidaIntervaloIdeal = horarioDia?.saidaIntervaloIdeal,
                    voltaIntervaloIdeal = horarioDia?.voltaIntervaloIdeal,
                    saidaIdeal = horarioDia?.saidaIdeal
                )
            )
        } catch (e: Exception) {
            Resultado.Erro(
                mensagem = e.message ?: "Erro ao obter contexto da jornada do dia."
            )
        }
    }
}