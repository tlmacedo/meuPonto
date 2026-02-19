// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/feriado/TipoFeriado.kt
package br.com.tlmacedo.meuponto.domain.model.feriado

/**
 * Tipos de feriado disponíveis no sistema.
 *
 * @author Thiago
 * @since 3.0.0
 */
enum class TipoFeriado(
    val descricao: String,
    val emoji: String
) {
    /**
     * Feriado nacional - aplicado em todo o país.
     * Ex: Natal, Ano Novo, Independência
     */
    NACIONAL("Nacional", "🇧🇷"),

    /**
     * Feriado estadual - aplicado apenas no estado específico.
     * Ex: Data Magna do estado
     */
    ESTADUAL("Estadual", "🏛️"),

    /**
     * Feriado municipal - aplicado apenas no município específico.
     * Ex: Aniversário da cidade, padroeiro
     */
    MUNICIPAL("Municipal", "🏙️"),

    /**
     * Ponto facultativo - não é feriado oficial, mas pode ser folga.
     * Ex: Carnaval, Quarta-feira de Cinzas
     */
    FACULTATIVO("Ponto Facultativo", "📋"),

    /**
     * Feriado ponte - dia útil entre feriado e fim de semana.
     * A carga horária é distribuída ao longo do ano.
     */
    PONTE("Ponte", "🌉");

    companion object {
        /**
         * Retorna tipos que representam dias de folga efetiva.
         */
        fun tiposFolga(): List<TipoFeriado> = listOf(NACIONAL, ESTADUAL, MUNICIPAL)

        /**
         * Retorna tipos que podem ser opcionais dependendo da empresa.
         */
        fun tiposOpcionais(): List<TipoFeriado> = listOf(FACULTATIVO, PONTE)
    }
}
