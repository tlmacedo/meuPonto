// path: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/ChamadoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chamados")
data class ChamadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val identificador: String,
    val titulo: String,
    val descricao: String,
    val passosParaReproduzir: String? = null,
    val deviceInfo: String? = null,
    val categoria: String,
    val status: String = "ABERTO",
    val prioridade: String = "NORMAL",
    val empregoId: Long? = null,
    val usuarioId: Long? = null,
    val usuarioNome: String,
    val usuarioEmail: String,
    val resposta: String? = null,
    val anexos: String? = null,
    val avaliacaoNota: String? = null,
    val avaliacaoComentario: String? = null,
    val avaliadoEm: String? = null,
    val resolvidoEm: String? = null,
    val criadoEm: String,
    val atualizadoEm: String
)