// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/PontoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Entidade Room que representa um registro de ponto no banco de dados.
 *
 * Esta classe é responsável pela persistência dos dados de ponto,
 * mapeando os campos para colunas no SQLite através do Room.
 *
 * @property id Identificador único auto-gerado
 * @property dataHora Data e hora completa do registro
 * @property tipo Tipo do ponto (ENTRADA/SAIDA)
 * @property observacao Observação opcional
 * @property isEditadoManualmente Indica se foi editado após criação
 * @property criadoEm Timestamp de criação
 * @property atualizadoEm Timestamp da última atualização
 * @property data Data extraída (para queries otimizadas)
 * @property hora Hora extraída (para queries otimizadas)
 *
 * @author Thiago
 * @since 1.0.0
 */
@Entity(tableName = "pontos")
data class PontoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dataHora: LocalDateTime,
    val tipo: TipoPonto,
    val observacao: String? = null,
    val isEditadoManualmente: Boolean = false,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now(),
    // Campos desnormalizados para facilitar queries por data/hora
    val data: LocalDate = dataHora.toLocalDate(),
    val hora: LocalTime = dataHora.toLocalTime()
)

// ============================================================================
// Funções de Mapeamento (Mapper Extensions)
// ============================================================================

/**
 * Converte PontoEntity (camada de dados) para Ponto (camada de domínio).
 *
 * @return Instância de [Ponto] com os dados mapeados
 */
fun PontoEntity.toDomain(): Ponto = Ponto(
    id = id,
    dataHora = dataHora,
    tipo = tipo,
    observacao = observacao,
    isEditadoManualmente = isEditadoManualmente,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
)

/**
 * Converte Ponto (camada de domínio) para PontoEntity (camada de dados).
 *
 * @return Instância de [PontoEntity] pronta para persistência
 */
fun Ponto.toEntity(): PontoEntity = PontoEntity(
    id = id,
    dataHora = dataHora,
    tipo = tipo,
    observacao = observacao,
    isEditadoManualmente = isEditadoManualmente,
    criadoEm = criadoEm,
    atualizadoEm = atualizadoEm
    // data e hora são calculados automaticamente a partir de dataHora
)
