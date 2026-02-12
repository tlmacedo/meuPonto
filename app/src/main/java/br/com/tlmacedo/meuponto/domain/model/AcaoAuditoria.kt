// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/AcaoAuditoria.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Tipos de ação registradas no log de auditoria.
 */
enum class AcaoAuditoria {
    /** Inserção de novo registro */
    INSERT,
    
    /** Atualização de registro existente */
    UPDATE,
    
    /** Exclusão de registro */
    DELETE
}
