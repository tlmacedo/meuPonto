package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "historico_cargos",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empregoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["empregoId"])]
)
data class HistoricoCargoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long,
    val funcao: String,
    val salarioInicial: Double,
    val dataInicio: LocalDate,
    val dataFim: LocalDate? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

fun HistoricoCargoEntity.toDomain(): br.com.tlmacedo.meuponto.domain.model.HistoricoCargo =
    br.com.tlmacedo.meuponto.domain.model.HistoricoCargo(
        id = id,
        empregoId = empregoId,
        funcao = funcao,
        salarioInicial = salarioInicial,
        dataInicio = dataInicio,
        dataFim = dataFim,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )

fun br.com.tlmacedo.meuponto.domain.model.HistoricoCargo.toEntity(): HistoricoCargoEntity =
    HistoricoCargoEntity(
        id = id,
        empregoId = empregoId,
        funcao = funcao,
        salarioInicial = salarioInicial,
        dataInicio = dataInicio,
        dataFim = dataFim,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )

@Entity(
    tableName = "ajustes_salariais",
    foreignKeys = [
        ForeignKey(
            entity = HistoricoCargoEntity::class,
            parentColumns = ["id"],
            childColumns = ["historicoCargoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["historicoCargoId"])]
)
data class AjusteSalarialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val historicoCargoId: Long,
    val dataAjuste: LocalDate,
    val novoSalario: Double,
    val observacao: String? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now()
)

fun AjusteSalarialEntity.toDomain(): br.com.tlmacedo.meuponto.domain.model.AjusteSalarial =
    br.com.tlmacedo.meuponto.domain.model.AjusteSalarial(
        id = id,
        historicoCargoId = historicoCargoId,
        dataAjuste = dataAjuste,
        novoSalario = novoSalario,
        observacao = observacao
    )

fun br.com.tlmacedo.meuponto.domain.model.AjusteSalarial.toEntity(): AjusteSalarialEntity =
    AjusteSalarialEntity(
        id = id,
        historicoCargoId = historicoCargoId,
        dataAjuste = dataAjuste,
        novoSalario = novoSalario,
        observacao = observacao
    )
