// Arquivo: TipoPonto.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Enum que representa o tipo de ponto (ENTRADA ou SAÍDA).
 *
 * **IMPORTANTE:** Este enum é usado APENAS para apresentação na UI.
 * O tipo real do ponto é determinado pela sua posição na lista ordenada:
 * - Índice par (0, 2, 4...) = ENTRADA
 * - Índice ímpar (1, 3, 5...) = SAÍDA
 *
 * @property descricao Descrição para exibição na UI
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Agora é apenas enum de apresentação (não armazenado no banco)
 */
enum class TipoPonto(val descricao: String) {
    ENTRADA("Entrada"),
    SAIDA("Saída");

    companion object {
        /** Quantidade mínima de pontos para calcular jornada */
        const val MIN_PONTOS = 2

        /** Quantidade ideal de pontos (entrada, intervalo, retorno, saída) */
        const val PONTOS_IDEAL = 4

        /** Quantidade máxima de pontos permitidos por dia */
        const val MAX_PONTOS = 10

        /**
         * Determina o tipo baseado na quantidade atual de pontos.
         * Se a quantidade é par, o próximo é ENTRADA. Se ímpar, é SAÍDA.
         */
        fun getProximoTipo(quantidadePontos: Int): TipoPonto {
            return if (quantidadePontos % 2 == 0) ENTRADA else SAIDA
        }

        /**
         * Determina o tipo de um ponto baseado no seu índice na lista ordenada.
         * Índice par = ENTRADA, índice ímpar = SAÍDA.
         */
        fun getTipoPorIndice(indice: Int): TipoPonto {
            return if (indice % 2 == 0) ENTRADA else SAIDA
        }
    }
}

/**
 * Objeto com constantes de ponto (para uso onde não se quer importar o enum).
 */
object PontoConstants {
    const val MIN_PONTOS = 2
    const val PONTOS_IDEAL = 4
    const val MAX_PONTOS = 10
}

/** Verifica se o próximo ponto será uma entrada */
fun proximoPontoIsEntrada(quantidadePontos: Int): Boolean = quantidadePontos % 2 == 0

/** Retorna descrição do próximo tipo esperado */
fun proximoPontoDescricao(quantidadePontos: Int): String =
    if (proximoPontoIsEntrada(quantidadePontos)) "Entrada" else "Saída"
