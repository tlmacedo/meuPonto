// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/TipoFechamento.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Tipo de fechamento de período para banco de horas.
 */
enum class TipoFechamento {
    /** Fechamento semanal */
    SEMANAL,
    
    /** Fechamento mensal (folha de pagamento) */
    MENSAL,
    
    /** Fechamento do período de banco de horas */
    BANCO_HORAS
}
