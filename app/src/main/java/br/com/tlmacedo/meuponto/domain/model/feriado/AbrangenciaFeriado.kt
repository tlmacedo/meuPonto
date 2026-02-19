// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/feriado/AbrangenciaFeriado.kt
package br.com.tlmacedo.meuponto.domain.model.feriado

/**
 * Define a abrangência/escopo de aplicação de um feriado.
 *
 * @author Thiago
 * @since 3.0.0
 */
enum class AbrangenciaFeriado(
    val descricao: String
) {
    /**
     * Feriado aplicado globalmente a todos os empregos.
     */
    GLOBAL("Global - Todos os empregos"),

    /**
     * Feriado aplicado apenas a empregos específicos.
     */
    EMPREGO_ESPECIFICO("Específico por emprego");
}
