// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/AuditAction.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Tipos de ações de auditoria.
 *
 * @author Thiago
 * @since 11.0.0
 */
enum class AuditAction(val descricao: String) {
    CREATE("Criação"),
    UPDATE("Alteração"),
    DELETE("Exclusão"),
    RESTORE("Restauração"),
    PERMANENT_DELETE("Exclusão Permanente")
}
