// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/FeriadoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.feriado.AbrangenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.MonthDay

/**
 * Entidade do Room para persistência de feriados.
 *
 * @author Thiago
 * @since 3.0.0
 */
@Entity(
    tableName = "feriados",
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
        Index(value = ["anoReferencia"]),
        Index(value = ["diaMes"]),
        Index(value = ["dataEspecifica"]),
        Index(value = ["ativo"])
    ]
)
data class FeriadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "nome")
    val nome: String,

    @ColumnInfo(name = "tipo")
    val tipo: TipoFeriado,

    @ColumnInfo(name = "recorrencia")
    val recorrencia: RecorrenciaFeriado,

    @ColumnInfo(name = "abrangencia")
    val abrangencia: AbrangenciaFeriado,

    @ColumnInfo(name = "diaMes")
    val diaMes: String? = null, // Formato: "MM-dd" (ex: "12-25" para Natal)

    @ColumnInfo(name = "dataEspecifica")
    val dataEspecifica: LocalDate? = null,

    @ColumnInfo(name = "anoReferencia")
    val anoReferencia: Int? = null,

    @ColumnInfo(name = "uf")
    val uf: String? = null,

    @ColumnInfo(name = "municipio")
    val municipio: String? = null,

    @ColumnInfo(name = "empregoId")
    val empregoId: Long? = null,

    @ColumnInfo(name = "ativo")
    val ativo: Boolean = true,

    @ColumnInfo(name = "observacao")
    val observacao: String? = null,

    @ColumnInfo(name = "criadoEm")
    val criadoEm: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "atualizadoEm")
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

/**
 * Converte Entity para Domain.
 */
fun FeriadoEntity.toDomain(): Feriado = Feriado(
    id = id,
    nome = nome,
    tipo = tipo,
    recorrencia = recorrencia,
    abrangencia = abrangencia,
    diaMes = diaMes?.let { MonthDay.parse("--$it") },
    dataEspecifica = dataEspecifica,
    anoReferencia = anoReferencia,
    uf = uf,
    municipio = municipio,
    empregoId = empregoId,
    ativo = ativo,
    observacao = observacao,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)

/**
 * Converte Domain para Entity.
 */
fun Feriado.toEntity(): FeriadoEntity = FeriadoEntity(
    id = id,
    nome = nome,
    tipo = tipo,
    recorrencia = recorrencia,
    abrangencia = abrangencia,
    diaMes = diaMes?.let { String.format("%02d-%02d", it.monthValue, it.dayOfMonth) },
    dataEspecifica = dataEspecifica,
    anoReferencia = anoReferencia,
    uf = uf,
    municipio = municipio,
    empregoId = empregoId,
    ativo = ativo,
    observacao = observacao,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)
