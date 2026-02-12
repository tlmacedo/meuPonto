package br.com.tlmacedo.meuponto.domain.model

/**
 * Enum que representa os tipos de batida de ponto.
 *
 * @property descricao Descrição em português para exibição na UI
 * @property ordem Ordem de exibição/sequência esperada no dia
 *
 * @author Thiago
 * @since 1.0.0
 */
enum class TipoPonto(
    val descricao: String,
    val ordem: Int
) {
    ENTRADA("Entrada", 1),
    SAIDA_ALMOCO("Saída Almoço", 2),
    RETORNO_ALMOCO("Retorno Almoço", 3),
    SAIDA("Saída", 4);

    companion object {
        /**
         * Retorna o próximo tipo de ponto esperado baseado no último registrado.
         *
         * @param ultimoTipo Último tipo de ponto registrado no dia (null se nenhum)
         * @return Próximo tipo esperado na sequência
         */
        fun proximoTipo(ultimoTipo: TipoPonto?): TipoPonto {
            return when (ultimoTipo) {
                null -> ENTRADA
                ENTRADA -> SAIDA_ALMOCO
                SAIDA_ALMOCO -> RETORNO_ALMOCO
                RETORNO_ALMOCO -> SAIDA
                SAIDA -> ENTRADA
            }
        }
    }
}
