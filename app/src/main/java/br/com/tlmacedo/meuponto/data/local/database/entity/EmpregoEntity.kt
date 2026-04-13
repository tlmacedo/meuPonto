// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/EmpregoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade Room que representa um emprego/trabalho do usuário.
 */
@Entity(
    tableName = "empregos",
    indices = [
        Index(value = ["ativo"]),
        Index(value = ["arquivado"])
    ]
)
data class EmpregoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val razaoSocial: String? = null,
    val cnpj: String? = null,
    val apelido: String? = null,
    val endereco: String? = null,
    val dataInicioTrabalho: LocalDate? = null,
    val dataTerminoTrabalho: LocalDate? = null,
    val descricao: String? = null,
    val ativo: Boolean = true,
    val arquivado: Boolean = false,
    val ordem: Int = 0,
    val logo: String? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

fun EmpregoEntity.toDomain(): br.com.tlmacedo.meuponto.domain.model.Emprego =
    br.com.tlmacedo.meuponto.domain.model.Emprego(
        id = id,
        nome = nome,
        razaoSocial = razaoSocial,
        cnpj = cnpj,
        apelido = apelido,
        endereco = endereco,
        dataInicioTrabalho = dataInicioTrabalho,
        dataTerminoTrabalho = dataTerminoTrabalho,
        descricao = descricao,
        ativo = ativo,
        arquivado = arquivado,
        ordem = ordem,
        logo = logo,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )

fun br.com.tlmacedo.meuponto.domain.model.Emprego.toEntity(): EmpregoEntity =
    EmpregoEntity(
        id = id,
        nome = nome,
        razaoSocial = razaoSocial,
        cnpj = cnpj,
        apelido = apelido,
        endereco = endereco,
        dataInicioTrabalho = dataInicioTrabalho,
        dataTerminoTrabalho = dataTerminoTrabalho,
        descricao = descricao,
        ativo = ativo,
        arquivado = arquivado,
        ordem = ordem,
        logo = logo,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )
