// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/MarcadorEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entidade Room que representa um marcador/tag para categorizar registros de ponto.
 * 
 * Permite criar etiquetas personalizadas como "Home Office", "Externo", "Plantão", etc.
 *
 * @property id Identificador único auto-gerado
 * @property empregoId FK para o emprego associado
 * @property nome Nome do marcador
 * @property cor Cor em formato hexadecimal (ex: "#FF5722")
 * @property icone Nome do ícone (opcional, para uso futuro)
 * @property ativo Se false, o marcador não aparece nas opções
 * @property ordem Ordem de exibição na lista
 * @property criadoEm Timestamp de criação
 * @property atualizadoEm Timestamp da última atualização
 *
 * @author Thiago
 * @since 2.0.0
 */
@Entity(
    tableName = "marcadores",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empregoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["empregoId"]),
        Index(value = ["empregoId", "ativo"])
    ]
)
data class MarcadorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long,
    val nome: String,
    val cor: String = "#2196F3", // Azul padrão
    val icone: String? = null,
    val ativo: Boolean = true,
    val ordem: Int = 0,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)
