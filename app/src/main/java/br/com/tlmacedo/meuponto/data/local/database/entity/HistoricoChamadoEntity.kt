// path: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/HistoricoChamadoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "historico_chamados",
    foreignKeys = [
        ForeignKey(
            entity = ChamadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["chamadoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chamadoId")]
)
data class HistoricoChamadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chamadoId: Long,
    val chamadoIdentificador: String,
    val statusAnterior: String? = null,
    val statusNovo: String,
    val mensagem: String,
    val autor: String,
    val criadoEm: String
)