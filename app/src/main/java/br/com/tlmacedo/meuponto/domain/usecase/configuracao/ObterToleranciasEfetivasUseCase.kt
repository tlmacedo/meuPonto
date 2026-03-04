// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/configuracao/ObterToleranciasEfetivasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.configuracao

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para obter as tolerâncias efetivas para um dia específico.
 *
 * SIMPLIFICAÇÃO (v7.2.0):
 * - Tolerância de entrada: fixa em 10 minutos
 * - Tolerância de saída: não aplicada
 * - Tolerância de intervalo: configurável
 *
 * @author Thiago
 * @since 2.1.0
 * @updated 8.0.0 - Migrado para usar VersaoJornadaRepository
 */
class ObterToleranciasEfetivasUseCase @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) {

    /** Resultado contendo as tolerâncias efetivas calculadas */
    data class ToleranciasEfetivas(
        val intervaloMaisMinutos: Int,
        val fonte: String
    ) {
        companion object {
            /** Tolerância de entrada padrão (fixa) */
            const val TOLERANCIA_ENTRADA_PADRAO = 10

            fun padrao() = ToleranciasEfetivas(
                intervaloMaisMinutos = 0,
                fonte = "Valores padrão do sistema"
            )
        }

        val descricao: String
            get() = buildString {
                append("Entrada: ${TOLERANCIA_ENTRADA_PADRAO}min")
                if (intervaloMaisMinutos > 0) {
                    append(" | Intervalo (+): ${intervaloMaisMinutos}min")
                }
            }
    }

    /** Obtém as tolerâncias efetivas para um emprego em uma data específica */
    suspend operator fun invoke(empregoId: Long, data: LocalDate): ToleranciasEfetivas {
        val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)
        return invoke(empregoId, diaSemana)
    }

    /** Obtém as tolerâncias efetivas para um emprego em um dia da semana específico */
    suspend operator fun invoke(empregoId: Long, diaSemana: DiaSemana): ToleranciasEfetivas {
        val versaoJornada = versaoJornadaRepository.buscarVigente(empregoId)
        val configDia = horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        return calcularToleranciasEfetivas(versaoJornada, configDia, diaSemana)
    }

    /** Calcula tolerâncias usando objetos já carregados */
    fun calcularToleranciasEfetivas(
        versaoJornada: VersaoJornada?,
        configDia: HorarioDiaSemana?,
        diaSemana: DiaSemana
    ): ToleranciasEfetivas {
        // Tolerância de intervalo: dia específico > versão jornada > padrão
        val intervaloMaisEfetivo = configDia?.toleranciaIntervaloMaisMinutos
            ?: versaoJornada?.toleranciaIntervaloMaisMinutos
            ?: 0

        val fonte = when {
            configDia != null -> "Configuração de ${diaSemana.descricao}"
            else -> "Valores padrão"
        }

        return ToleranciasEfetivas(
            intervaloMaisMinutos = intervaloMaisEfetivo,
            fonte = fonte
        )
    }
}
