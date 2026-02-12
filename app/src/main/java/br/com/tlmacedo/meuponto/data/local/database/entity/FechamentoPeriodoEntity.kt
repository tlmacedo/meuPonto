// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/FechamentoPeriodoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade Room que representa um fechamento de período (zerar saldo).
 * 
 * Quando um período é fechado, o saldo anterior é registrado e zerado,
 * criando um marco para cálculos futuros.
 *
 * @property id Identificador único auto-gerado
 * @property empregoId FK para o emprego associado
 * @property dataFechamento Data em que o fechamento foi realizado
 * @property dataInicioPeriodo Data de início do período fechado
 * @property dataFimPeriodo Data de fim do período fechado
 * @property saldoAnteriorMinutos Saldo em minutos antes do fechamento
 * @property tipo Tipo de fechamento (SEMANAL, MENSAL, BANCO_HORAS)
 * @property observacao Observação opcional
 * @property criadoEm Timestamp de criação
 *
 * @author Thiago
 * @since 2.0.0
 */
@Entity(
    tableName = "fechamentos_periodo",
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
        Index(value = ["dataFechamento"]),
        Index(value = ["tipo"]),
        Index(value = ["empregoId", "tipo", "dataFechamento"])
    ]
)
data class FechamentoPeriodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long,
    val dataFechamento: LocalDate,
    val dataInicioPeriodo: LocalDate,
    val dataFimPeriodo: LocalDate,
    val saldoAnteriorMinutos: Int,
    val tipo: TipoFechamento,
    val observacao: String? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now()
)
