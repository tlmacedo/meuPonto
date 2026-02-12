// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/TipoNsr.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Tipo de campo NSR (Número Sequencial de Registro).
 * 
 * Define se o campo NSR aceita apenas números ou também letras.
 */
enum class TipoNsr {
    /** Aceita apenas caracteres numéricos (0-9) */
    NUMERICO,
    
    /** Aceita caracteres alfanuméricos (A-Z, 0-9) */
    ALFANUMERICO
}
