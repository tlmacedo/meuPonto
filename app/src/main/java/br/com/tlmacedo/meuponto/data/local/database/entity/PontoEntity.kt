// Arquivo: PontoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Entidade Room que representa um registro de ponto no banco de dados.
 *
 * IMPORTANTE: O tipo do ponto (ENTRADA/SAÍDA) NÃO é armazenado no banco.
 * É calculado em runtime baseado na posição na lista ordenada por dataHora:
 * - Índice par (0, 2, 4...) = ENTRADA
 * - Índice ímpar (1, 3, 5...) = SAÍDA
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Removido campo tipo (calculado em runtime)
 */
@Entity(
    tableName = "pontos",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empregoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MarcadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["marcadorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["empregoId"]),
        Index(value = ["data"]),
        Index(value = ["empregoId", "data"]),
        Index(value = ["marcadorId"])
    ]
)
data class PontoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long = 1,
    val dataHora: LocalDateTime,
    val observacao: String? = null,
    val isEditadoManualmente: Boolean = false,
    val nsr: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val endereco: String? = null,
    val marcadorId: Long? = null,
    val justificativaInconsistencia: String? = null,
    val horaConsiderada: LocalDateTime? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now(),
    val data: LocalDate = dataHora.toLocalDate(),
    val hora: LocalTime = dataHora.toLocalTime()
)

/**
 * Converte PontoEntity para Ponto (domínio).
 */
fun PontoEntity.toDomain(): Ponto = Ponto(
    id = id,
    empregoId = empregoId,
    dataHora = dataHora,
    observacao = observacao,
    isEditadoManualmente = isEditadoManualmente,
    nsr = nsr,
    latitude = latitude,
    longitude = longitude,
    endereco = endereco,
    marcadorId = marcadorId,
    justificativaInconsistencia = justificativaInconsistencia,
    horaConsiderada = horaConsiderada,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)

/**
 * Converte Ponto (domínio) para PontoEntity.
 */
fun Ponto.toEntity(): PontoEntity = PontoEntity(
    id = id,
    empregoId = empregoId,
    dataHora = dataHora,
    observacao = observacao,
    isEditadoManualmente = isEditadoManualmente,
    nsr = nsr,
    latitude = latitude,
    longitude = longitude,
    endereco = endereco,
    marcadorId = marcadorId,
    justificativaInconsistencia = justificativaInconsistencia,
    horaConsiderada = horaConsiderada,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)
