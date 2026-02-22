// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/FechamentoPeriodoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.FechamentoPeriodo
import br.com.tlmacedo.meuponto.domain.model.TipoFechamento
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade Room que armazena fechamentos de período do banco de horas.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Suporte a fechamentos automáticos de ciclo
 * @updated 6.0.0 - Corrigido mapeamento de colunas e índices para corresponder ao banco
 */
@Entity(
    tableName = "fechamentos_periodo",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["emprego_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["emprego_id"]),
        Index(value = ["data_fechamento"]),
        Index(value = ["tipo"]),
        Index(value = ["emprego_id", "data_inicio_periodo", "data_fim_periodo"], unique = true)
    ]
)
data class FechamentoPeriodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "emprego_id")
    val empregoId: Long,

    @ColumnInfo(name = "data_fechamento")
    val dataFechamento: LocalDate,

    @ColumnInfo(name = "data_inicio_periodo")
    val dataInicioPeriodo: LocalDate,

    @ColumnInfo(name = "data_fim_periodo")
    val dataFimPeriodo: LocalDate,

    @ColumnInfo(name = "saldo_anterior_minutos")
    val saldoAnteriorMinutos: Int,

    val tipo: TipoFechamento,

    val observacao: String? = null,

    @ColumnInfo(name = "criado_em")
    val criadoEm: LocalDateTime = LocalDateTime.now()
)

fun FechamentoPeriodoEntity.toDomain(): FechamentoPeriodo =
    FechamentoPeriodo(
        id = id,
        empregoId = empregoId,
        dataFechamento = dataFechamento,
        dataInicioPeriodo = dataInicioPeriodo,
        dataFimPeriodo = dataFimPeriodo,
        saldoAnteriorMinutos = saldoAnteriorMinutos,
        tipo = tipo,
        observacao = observacao,
        criadoEm = criadoEm
    )

fun FechamentoPeriodo.toEntity(): FechamentoPeriodoEntity =
    FechamentoPeriodoEntity(
        id = id,
        empregoId = empregoId,
        dataFechamento = dataFechamento,
        dataInicioPeriodo = dataInicioPeriodo,
        dataFimPeriodo = dataFimPeriodo,
        saldoAnteriorMinutos = saldoAnteriorMinutos,
        tipo = tipo,
        observacao = observacao,
        criadoEm = criadoEm
    )
