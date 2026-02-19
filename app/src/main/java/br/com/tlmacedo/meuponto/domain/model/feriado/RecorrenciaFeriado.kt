// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/feriado/RecorrenciaFeriado.kt
package br.com.tlmacedo.meuponto.domain.model.feriado

/**
 * Define se um feriado se repete anualmente ou é único.
 *
 * @author Thiago
 * @since 3.0.0
 */
enum class RecorrenciaFeriado(
    val descricao: String
) {
    /**
     * Feriado que ocorre todo ano na mesma data.
     * Ex: Natal (25/12), Ano Novo (01/01)
     */
    ANUAL("Anual - Repete todo ano"),

    /**
     * Feriado que ocorre apenas em um ano específico.
     * Ex: Feriado municipal especial, ponte específica
     */
    UNICO("Único - Apenas este ano");
}
