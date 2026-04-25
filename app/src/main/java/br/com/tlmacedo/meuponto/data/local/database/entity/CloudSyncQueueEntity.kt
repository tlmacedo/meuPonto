// path: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/CloudSyncQueueEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "cloud_sync_queue")
data class CloudSyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String,        // ex: "PONTO", "AUSENCIA", "EMPREGO"
    val entityId: Long,
    val operation: String,         // "INSERT", "UPDATE", "DELETE"
    val payload: String? = null,   // JSON serializado da entidade
    val tentativas: Int = 0,
    val ultimoErro: String? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val sincronizadoEm: LocalDateTime? = null
)