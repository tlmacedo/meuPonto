// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/AusenciaEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Entidade de ausência para persistência no banco de dados.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.5.0 - Removido SubTipoFolga
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

    val empregoId: Long,
    val tipo: TipoAusencia,
    val tipoFolga: TipoFolga? = null,
    val dataInicio: LocalDate,
    val dataFim: LocalDate,

    // Campos básicos
    val descricao: String? = null,
    val observacao: String? = null,

    // ========================================================================
    // CAMPOS ESPECÍFICOS POR TIPO (v5.4.0)
    // ========================================================================

    // Para DECLARACAO
    val horaInicio: LocalTime? = null,
    val duracaoDeclaracaoMinutos: Int? = null,
    val duracaoAbonoMinutos: Int? = null,

    // Para FOLGA (mantido para compatibilidade de migração, mas não mais usado)
    @Deprecated("SubTipoFolga removido na v5.5.0")
    val subTipoFolga: String? = null,

    // Para FERIAS
    val periodoAquisitivo: String? = null,

    // Para anexos (ATESTADO, DECLARACAO, FALTA_JUSTIFICADA)
    val imagemUri: String? = null,

    // ========================================================================
    // CAMPOS DE CONTROLE
    // ========================================================================
    val ativo: Boolean = true,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Converte a Entity para o modelo de domínio.
     */
    fun toAusencia(): Ausencia {
        return Ausencia(
            id = id,
            empregoId = empregoId,
            tipo = tipo,
            tipoFolga = tipoFolga,
            dataInicio = dataInicio,
            dataFim = dataFim,
            descricao = descricao,
            observacao = observacao,
            horaInicio = horaInicio,
            duracaoDeclaracaoMinutos = duracaoDeclaracaoMinutos,
            duracaoAbonoMinutos = duracaoAbonoMinutos,
            periodoAquisitivo = periodoAquisitivo,
            imagemUri = imagemUri,
            ativo = ativo,
            criadoEm = criadoEm,
            atualizadoEm = atualizadoEm
        )
    }

    companion object {
        /**
         * Cria uma Entity a partir do modelo de domínio.
         */
        fun fromAusencia(ausencia: Ausencia): AusenciaEntity {
            return AusenciaEntity(
                id = ausencia.id,
                empregoId = ausencia.empregoId,
                tipo = ausencia.tipo,
                tipoFolga = ausencia.tipoFolga,
                dataInicio = ausencia.dataInicio,
                dataFim = ausencia.dataFim,
                descricao = ausencia.descricao,
                observacao = ausencia.observacao,
                horaInicio = ausencia.horaInicio,
                duracaoDeclaracaoMinutos = ausencia.duracaoDeclaracaoMinutos,
                duracaoAbonoMinutos = ausencia.duracaoAbonoMinutos,
                subTipoFolga = null, // Deprecated
                periodoAquisitivo = ausencia.periodoAquisitivo,
                imagemUri = ausencia.imagemUri,
                ativo = ausencia.ativo,
                criadoEm = ausencia.criadoEm,
                atualizadoEm = ausencia.atualizadoEm
            )
        }
    }
}
