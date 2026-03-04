// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/configuracao/ObterToleranciasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.configuracao

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.ToleranciasDia
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para obter as tolerâncias efetivas para uma data específica.
 *
 * Aplica a lógica híbrida: usa tolerâncias específicas do dia se configuradas,
 * caso contrário usa as tolerâncias globais da versão de jornada.
 *
 * @author Thiago
 * @since 2.1.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
class ObterToleranciasUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) {
    /**
     * Obtém as tolerâncias efetivas para uma data específica.
     *
     * @param empregoId ID do emprego
     * @param data Data para obter as tolerâncias
     * @return Tolerâncias efetivas para o dia
     */
    suspend operator fun invoke(empregoId: Long, data: LocalDate): ToleranciasDia {
        // Determina o dia da semana
        val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)

        // Busca versão de jornada vigente para a data
        val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)

        // Busca configuração específica do dia
        val horarioDia = versaoJornada?.let {
            horarioDiaSemanaRepository.buscarPorVersaoEDia(it.id, diaSemana)
        } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        // Cria as tolerâncias efetivas combinando versão + específico do dia
        return ToleranciasDia.criar(versaoJornada, horarioDia)
    }

    /**
     * Obtém as tolerâncias efetivas para um dia da semana específico.
     *
     * @param empregoId ID do emprego
     * @param diaSemana Dia da semana
     * @return Tolerâncias efetivas para o dia
     */
    suspend fun porDiaSemana(empregoId: Long, diaSemana: DiaSemana): ToleranciasDia {
        val versaoJornada = versaoJornadaRepository.buscarVigente(empregoId)
        val horarioDia = versaoJornada?.let {
            horarioDiaSemanaRepository.buscarPorVersaoEDia(it.id, diaSemana)
        } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        return ToleranciasDia.criar(versaoJornada, horarioDia)
    }

    /**
     * Obtém as tolerâncias efetivas para hoje.
     *
     * @param empregoId ID do emprego
     * @return Tolerâncias efetivas para hoje
     */
    suspend fun paraHoje(empregoId: Long): ToleranciasDia {
        return invoke(empregoId, LocalDate.now())
    }
}
