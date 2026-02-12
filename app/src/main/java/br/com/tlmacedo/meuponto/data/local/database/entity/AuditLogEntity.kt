// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/AuditLogEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import java.time.LocalDateTime

/**
 * Entidade Room que registra todas as alterações no banco de dados para auditoria.
 * 
 * Permite rastrear quem alterou o quê e quando, possibilitando reversão de ações.
 * Registros com mais de 1 ano são automaticamente removidos.
 *
 * @property id Identificador único auto-gerado
 * @property entidade Nome da tabela/entidade afetada (ex: "pontos", "empregos")
 * @property entidadeId ID do registro afetado
 * @property acao Tipo de ação (INSERT, UPDATE, DELETE)
 * @property dadosAnteriores JSON com os dados antes da alteração (null para INSERT)
 * @property dadosNovos JSON com os dados após a alteração (null para DELETE)
 * @property criadoEm Timestamp do registro da ação
 *
 * @author Thiago
 * @since 2.0.0
 */
@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["entidade"]),
        Index(value = ["entidadeId"]),
        Index(value = ["acao"]),
        Index(value = ["criadoEm"]), // Para limpeza de registros antigos
        Index(value = ["entidade", "entidadeId"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entidade: String,
    val entidadeId: Long,
    val acao: AcaoAuditoria,
    val dadosAnteriores: String? = null, // JSON
    val dadosNovos: String? = null, // JSON
    val criadoEm: LocalDateTime = LocalDateTime.now()
)
