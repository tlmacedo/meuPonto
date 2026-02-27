// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/PontoEntity.kt
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
 * Entidade Room para a tabela de pontos.
 *
 * @property id Identificador único (autoincrement)
 * @property empregoId FK para o emprego associado
 * @property dataHora Data e hora REAL do registro (hora batida pelo usuário)
 * @property data Data do ponto (apenas data, sem hora) - para consultas eficientes
 * @property hora Hora do ponto (apenas hora, sem data) - para consultas eficientes
 * @property horaConsiderada hora CONSIDERADA (com tolerância aplicada, se houver)
 * @property nsr Número Sequencial de Registro (opcional, para compliance)
 * @property observacao Observação livre do usuário
 * @property isEditadoManualmente Indica se o ponto foi editado após registro
 * @property latitude Coordenada de latitude (se localização habilitada)
 * @property longitude Coordenada de longitude (se localização habilitada)
 * @property endereco Endereço reverso da localização (opcional)
 * @property marcadorId FK para marcador/tag (opcional)
 * @property justificativaInconsistencia Justificativa para pontos inconsistentes
 * @property criadoEm Timestamp de criação do registro
 * @property atualizadoEm Timestamp da última atualização
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 7.0.0 - Adicionado campo horaConsiderada, data e hora separados
 */
@Entity(
    tableName = "pontos",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empregoId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = MarcadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["marcadorId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["empregoId"]),
        Index(value = ["marcadorId"]),
        Index(value = ["dataHora"]),
        Index(value = ["data"]),
        Index(value = ["empregoId", "data"]),
        Index(value = ["empregoId", "dataHora"])
    ]
)
data class PontoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long,
    val dataHora: LocalDateTime,
    val data: LocalDate,
    val hora: LocalTime,
    val horaConsiderada: LocalTime,
    val nsr: String? = null,
    val observacao: String? = null,
    val isEditadoManualmente: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val endereco: String? = null,
    val marcadorId: Long? = null,
    val justificativaInconsistencia: String? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

// ============================================================================
// MAPPERS
// ============================================================================

/**
 * Converte PontoEntity para Ponto (domain).
 */
fun PontoEntity.toDomain(): Ponto = Ponto(
    id = id,
    empregoId = empregoId,
    dataHora = dataHora,
    horaConsiderada = horaConsiderada,
    nsr = nsr,
    observacao = observacao,
    isEditadoManualmente = isEditadoManualmente,
    latitude = latitude,
    longitude = longitude,
    endereco = endereco,
    marcadorId = marcadorId,
    justificativaInconsistencia = justificativaInconsistencia,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)

/**
 * Converte Ponto (domain) para PontoEntity.
 */
fun Ponto.toEntity(): PontoEntity = PontoEntity(
    id = id,
    empregoId = empregoId,
    data = dataHora.toLocalDate(),
    hora = dataHora.toLocalTime(),
    dataHora = dataHora,
    horaConsiderada = horaConsiderada,
    nsr = nsr,
    observacao = observacao,
    isEditadoManualmente = isEditadoManualmente,
    latitude = latitude,
    longitude = longitude,
    endereco = endereco,
    marcadorId = marcadorId,
    justificativaInconsistencia = justificativaInconsistencia,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)
