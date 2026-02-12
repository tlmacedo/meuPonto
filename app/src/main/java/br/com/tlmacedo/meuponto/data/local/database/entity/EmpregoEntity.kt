// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/EmpregoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entidade Room que representa um emprego/trabalho do usuário.
 * 
 * Cada emprego é um workspace independente com seus próprios registros de ponto,
 * configurações de horário e banco de horas.
 *
 * @property id Identificador único auto-gerado
 * @property nome Nome do emprego (ex: "Empresa ABC", "Freelance")
 * @property descricao Descrição opcional do emprego
 * @property ativo Indica se o emprego está ativo (visível na lista principal)
 * @property arquivado Indica se o emprego foi arquivado permanentemente (mantém dados)
 * @property ordem Ordem de exibição na lista de empregos
 * @property criadoEm Timestamp de criação
 * @property atualizadoEm Timestamp da última atualização
 *
 * @author Thiago
 * @since 2.0.0
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
    val descricao: String? = null,
    val ativo: Boolean = true,
    val arquivado: Boolean = false,
    val ordem: Int = 0,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)
