// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/AuditLogEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import java.time.LocalDateTime

/**
 * Entidade Room que representa um log de auditoria no banco de dados.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.5.0 - Adicionado campo motivo
 */
@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["entidade", "entidade_id"]),
        Index(value = ["criado_em"]),
        Index(value = ["acao"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "entidade")
    val entidade: String,

    @ColumnInfo(name = "entidade_id")
    val entidadeId: Long,

    @ColumnInfo(name = "acao")
    val acao: AcaoAuditoria,

    @ColumnInfo(name = "motivo")
    val motivo: String? = null,

    @ColumnInfo(name = "dados_anteriores")
    val dadosAnteriores: String? = null,

    @ColumnInfo(name = "dados_novos")
    val dadosNovos: String? = null,

    @ColumnInfo(name = "criado_em")
    val criadoEm: LocalDateTime = LocalDateTime.now()
)

fun AuditLogEntity.toDomain(): AuditLog = AuditLog(
    id = id,
    entidade = entidade,
    entidadeId = entidadeId,
    acao = acao,
    motivo = motivo,
    dadosAnteriores = dadosAnteriores,
    dadosNovos = dadosNovos,
    criadoEm = criadoEm
)

fun AuditLog.toEntity(): AuditLogEntity = AuditLogEntity(
    id = id,
    entidade = entidade,
    entidadeId = entidadeId,
    acao = acao,
    motivo = motivo,
    dadosAnteriores = dadosAnteriores,
    dadosNovos = dadosNovos,
    criadoEm = criadoEm
)
