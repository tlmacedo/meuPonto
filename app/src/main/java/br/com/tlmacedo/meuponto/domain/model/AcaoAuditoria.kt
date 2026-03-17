// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/AcaoAuditoria.kt
package br.com.tlmacedo.meuponto.domain.model

/**
 * Enum representando as ações de auditoria.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Adicionadas ações de soft delete
 */
enum class AcaoAuditoria(val descricao: String) {
    INSERT("Criação"),
    UPDATE("Atualização"),
    DELETE("Exclusão"),
    SOFT_DELETE("Movido para lixeira"),
    RESTORE("Restaurado"),
    PERMANENT_DELETE("Exclusão permanente");

    companion object {
        fun fromString(value: String): AcaoAuditoria {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: INSERT
        }
    }
}
