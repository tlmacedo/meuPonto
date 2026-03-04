// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ToleranciasDia.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Modelo que representa as tolerâncias efetivas para um dia específico.
 *
 * SIMPLIFICAÇÃO (v7.2.0):
 * - Tolerância de entrada: fixa em 10 minutos (aplicada quando chega antes do horário ideal)
 * - Tolerância de saída: não aplicada (saídas são registradas no horário real)
 * - Tolerância de intervalo: configurável por dia ou via VersaoJornada
 *
 * @property intervaloMaisMinutos Tolerância para mais no intervalo
 *
 * @author Thiago
 * @since 2.1.0
 * @updated 8.0.0 - Atualizado para usar VersaoJornada em vez de ConfiguracaoEmprego
 */
data class ToleranciasDia(
    val intervaloMaisMinutos: Int
) {
    companion object {
        /** Tolerância de entrada padrão (fixa) */
        const val TOLERANCIA_ENTRADA_PADRAO = 10

        /**
         * Cria ToleranciasDia a partir das configurações do dia específico.
         *
         * @param versaoJornada Versão de jornada (para tolerância de intervalo global)
         * @param horarioDia Configurações específicas do dia
         * @return Tolerâncias efetivas para o dia
         */
        fun criar(
            versaoJornada: VersaoJornada?,
            horarioDia: HorarioDiaSemana?
        ): ToleranciasDia {
            return ToleranciasDia(
                intervaloMaisMinutos = horarioDia?.toleranciaIntervaloMaisMinutos
                    ?: versaoJornada?.toleranciaIntervaloMaisMinutos
                    ?: 0
            )
        }

        /**
         * Cria ToleranciasDia a partir de HorarioDiaSemana apenas.
         */
        fun criar(horarioDia: HorarioDiaSemana?): ToleranciasDia {
            return ToleranciasDia(
                intervaloMaisMinutos = horarioDia?.toleranciaIntervaloMaisMinutos ?: 0
            )
        }

        /** Tolerâncias padrão quando não há configuração */
        val PADRAO = ToleranciasDia(intervaloMaisMinutos = 0)
    }

    /** Verifica se há tolerância de intervalo configurada */
    val temToleranciaIntervalo: Boolean
        get() = intervaloMaisMinutos > 0

    /** Descrição resumida das tolerâncias */
    val descricao: String
        get() = buildString {
            append("Entrada: ${TOLERANCIA_ENTRADA_PADRAO}min (padrão)")
            if (intervaloMaisMinutos > 0) {
                append(" | Intervalo (+): ${intervaloMaisMinutos}min")
            }
        }
}
