// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/EmpregoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade Room que representa um emprego/trabalho do usuário no banco de dados.
 *
 * @property id Identificador único (autoincrement)
 * @property nome Nome oficial ou principal do emprego
 * @property razaoSocial Razão social da empresa
 * @property cnpj CNPJ da empresa (apenas números)
 * @property apelido Nome curto para exibição simplificada
 * @property endereco Endereço físico do local de trabalho
 * @property dataInicioTrabalho Data de admissão
 * @property dataTerminoTrabalho Data de rescisão (null se ativo)
 * @property descricao Notas adicionais do usuário
 * @property ativo Flag de uso (false = desativado logicamente)
 * @property arquivado Flag para ocultar da tela principal mantendo histórico
 * @property ordem Posição na lista de exibição
 * @property logo Path ou URI para a imagem da logo
 * @property criadoEm Timestamp de criação do registro
 * @property atualizadoEm Timestamp da última alteração
 *
 * @author Thiago
 * @since 1.0.0
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
