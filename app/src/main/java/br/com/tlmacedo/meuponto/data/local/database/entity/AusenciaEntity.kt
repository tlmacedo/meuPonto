// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/AusenciaEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade do Room para persistência de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
@Entity(
    tableName = "ausencias",
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
        Index(value = ["tipo"]),
        Index(value = ["dataInicio"]),
        Index(value = ["dataFim"]),
        Index(value = ["ativo"]),
        Index(value = ["empregoId", "dataInicio", "dataFim"])
    ]
)
data class AusenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "empregoId")
    val empregoId: Long,

    @ColumnInfo(name = "tipo")
    val tipo: TipoAusencia,

    @ColumnInfo(name = "dataInicio")
    val dataInicio: LocalDate,

    @ColumnInfo(name = "dataFim")
    val dataFim: LocalDate,

    @ColumnInfo(name = "descricao")
    val descricao: String? = null,

    @ColumnInfo(name = "observacao")
    val observacao: String? = null,

    @ColumnInfo(name = "ativo")
    val ativo: Boolean = true,

    @ColumnInfo(name = "criadoEm")
    val criadoEm: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "atualizadoEm")
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

/**
 * Converte Entity para Domain.
 */
fun AusenciaEntity.toDomain(): Ausencia = Ausencia(
    id = id,
    empregoId = empregoId,
    tipo = tipo,
    dataInicio = dataInicio,
    dataFim = dataFim,
    descricao = descricao,
    observacao = observacao,
    ativo = ativo,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)

/**
 * Converte Domain para Entity.
 */
fun Ausencia.toEntity(): AusenciaEntity = AusenciaEntity(
    id = id,
    empregoId = empregoId,
    tipo = tipo,
    dataInicio = dataInicio,
    dataFim = dataFim,
    descricao = descricao,
    observacao = observacao,
    ativo = ativo,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)
