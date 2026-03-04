// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/HorarioDiaSemanaEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Entidade Room que armazena a configuração de horários para cada dia da semana.
 *
 * @since 2.0.0
 * @updated 2.7.0 - Adicionado versaoJornadaId para versionamento
 * @updated 7.2.0 - Removidos toleranciaEntradaMinutos e toleranciaSaidaMinutos
 */
@Entity(
    tableName = "horarios_dia_semana",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empregoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VersaoJornadaEntity::class,
            parentColumns = ["id"],
            childColumns = ["versaoJornadaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["empregoId"]),
        Index(value = ["versaoJornadaId"]),
        Index(value = ["versaoJornadaId", "diaSemana"], unique = true)
    ]
)
data class HorarioDiaSemanaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long,
    val versaoJornadaId: Long? = null,
    val diaSemana: DiaSemana,
    val ativo: Boolean = true,
    val cargaHorariaMinutos: Int = 492,
    val entradaIdeal: LocalTime? = null,
    val saidaIntervaloIdeal: LocalTime? = null,
    val voltaIntervaloIdeal: LocalTime? = null,
    val saidaIdeal: LocalTime? = null,
    val intervaloMinimoMinutos: Int = 60,
    val toleranciaIntervaloMaisMinutos: Int = 0,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

fun HorarioDiaSemanaEntity.toDomain(): br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana =
    br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana(
        id = id,
        empregoId = empregoId,
        versaoJornadaId = versaoJornadaId,
        diaSemana = diaSemana,
        ativo = ativo,
        cargaHorariaMinutos = cargaHorariaMinutos,
        entradaIdeal = entradaIdeal,
        saidaIntervaloIdeal = saidaIntervaloIdeal,
        voltaIntervaloIdeal = voltaIntervaloIdeal,
        saidaIdeal = saidaIdeal,
        intervaloMinimoMinutos = intervaloMinimoMinutos,
        toleranciaIntervaloMaisMinutos = toleranciaIntervaloMaisMinutos,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )

fun br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana.toEntity(): HorarioDiaSemanaEntity =
    HorarioDiaSemanaEntity(
        id = id,
        empregoId = empregoId,
        versaoJornadaId = versaoJornadaId,
        diaSemana = diaSemana,
        ativo = ativo,
        cargaHorariaMinutos = cargaHorariaMinutos,
        entradaIdeal = entradaIdeal,
        saidaIntervaloIdeal = saidaIntervaloIdeal,
        voltaIntervaloIdeal = voltaIntervaloIdeal,
        saidaIdeal = saidaIdeal,
        intervaloMinimoMinutos = intervaloMinimoMinutos,
        toleranciaIntervaloMaisMinutos = toleranciaIntervaloMaisMinutos,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )
