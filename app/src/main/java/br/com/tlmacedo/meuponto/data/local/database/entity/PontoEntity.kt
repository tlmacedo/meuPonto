// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/PontoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.core.security.CryptoHelper
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Entidade Room que representa um registro de ponto no banco de dados.
 *
 * SEGURANÇA: Os campos latitude, longitude e endereco são armazenados
 * CRIPTOGRAFADOS usando AES-256-GCM (via CryptoHelper).
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 6.1.0 - Campos de localização criptografados
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

    // ✅ Campos de localização agora são String (criptografados)
    val latitude: String? = null,
    val longitude: String? = null,
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
 * Descriptografa os campos de localização.
 */
fun PontoEntity.toDomain(): Ponto = Ponto(
    id = id,
    empregoId = empregoId,
    dataHora = dataHora,
    observacao = observacao,
    isEditadoManualmente = isEditadoManualmente,
    nsr = nsr,
    // ✅ Descriptografa ao converter para domínio
    latitude = CryptoHelper.decryptDouble(latitude),
    longitude = CryptoHelper.decryptDouble(longitude),
    endereco = CryptoHelper.decrypt(endereco),
    marcadorId = marcadorId,
    justificativaInconsistencia = justificativaInconsistencia,
    horaConsiderada = horaConsiderada,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)

/**
 * Converte Ponto (domínio) para PontoEntity.
 * Criptografa os campos de localização.
 */
fun Ponto.toEntity(): PontoEntity = PontoEntity(
    id = id,
    empregoId = empregoId,
    dataHora = dataHora,
    observacao = observacao,
    isEditadoManualmente = isEditadoManualmente,
    nsr = nsr,
    // ✅ Criptografa ao converter para entity
    latitude = CryptoHelper.encryptDouble(latitude),
    longitude = CryptoHelper.encryptDouble(longitude),
    endereco = CryptoHelper.encrypt(endereco),
    marcadorId = marcadorId,
    justificativaInconsistencia = justificativaInconsistencia,
    horaConsiderada = horaConsiderada,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)
