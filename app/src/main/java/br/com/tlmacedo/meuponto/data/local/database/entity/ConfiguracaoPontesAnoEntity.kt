// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/ConfiguracaoPontesAnoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.feriado.ConfiguracaoPontesAno
import java.time.LocalDateTime

/**
 * Entidade do Room para persistência da configuração de pontes por ano.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Entity(
    tableName = "configuracao_pontes_ano",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empregoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["empregoId", "ano"], unique = true),
        Index(value = ["ano"])
    ]
)
data class ConfiguracaoPontesAnoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "empregoId")
    val empregoId: Long,

    @ColumnInfo(name = "ano")
    val ano: Int,

    @ColumnInfo(name = "diasPonte")
    val diasPonte: Int,

    @ColumnInfo(name = "cargaHorariaPonteMinutos")
    val cargaHorariaPonteMinutos: Int,

    @ColumnInfo(name = "diasUteisAno")
    val diasUteisAno: Int,

    @ColumnInfo(name = "adicionalDiarioMinutos")
    val adicionalDiarioMinutos: Int,

    @ColumnInfo(name = "observacao")
    val observacao: String? = null,

    @ColumnInfo(name = "calculadoEm")
    val calculadoEm: LocalDateTime = LocalDateTime.now()
)

/**
 * Converte Entity para Domain.
 */
fun ConfiguracaoPontesAnoEntity.toDomain(): ConfiguracaoPontesAno = ConfiguracaoPontesAno(
    id = id,
    empregoId = empregoId,
    ano = ano,
    diasPonte = diasPonte,
    cargaHorariaPonteMinutos = cargaHorariaPonteMinutos,
    diasUteisAno = diasUteisAno,
    adicionalDiarioMinutos = adicionalDiarioMinutos,
    observacao = observacao,
    calculadoEm = calculadoEm
)

/**
 * Converte Domain para Entity.
 */
fun ConfiguracaoPontesAno.toEntity(): ConfiguracaoPontesAnoEntity = ConfiguracaoPontesAnoEntity(
    id = id,
    empregoId = empregoId,
    ano = ano,
    diasPonte = diasPonte,
    cargaHorariaPonteMinutos = cargaHorariaPonteMinutos,
    diasUteisAno = diasUteisAno,
    adicionalDiarioMinutos = adicionalDiarioMinutos,
    observacao = observacao,
    calculadoEm = calculadoEm
)
