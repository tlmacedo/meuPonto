// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/TipoPausa.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Enum que representa os tipos de pausa permitidos durante a jornada.
 *
 * @property descricao Descrição amigável do tipo de pausa
 *
 * @author Thiago
 * @since 1.0.0
 */
enum class TipoPausa(
    val descricao: String
) {
    /** Pausa curta para café ou lanche */
    CAFE("Café"),

    /** Pausa principal para almoço ou jantar */
    ALMOCO("Almoço"),

    /** Outras saídas rápidas (ex: banco, farmácia) */
    SAIDA_RAPIDA("Saída rápida")
}
