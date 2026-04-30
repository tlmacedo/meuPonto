// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/TipoPausa.kt
package br.com.tlmacedo.meuponto.domain.model

enum class TipoPausa(
    val descricao: String
) {
    CAFE("Café"),
    ALMOCO("Almoço"),
    SAIDA_RAPIDA("Saída rápida")
}