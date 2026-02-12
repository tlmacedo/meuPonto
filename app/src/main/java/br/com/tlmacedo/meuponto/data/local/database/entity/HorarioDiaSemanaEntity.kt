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
 * Permite configurar horários ideais, carga horária e tolerâncias de intervalo
 * para cada dia da semana de forma independente por emprego.
 *
 * @property id Identificador único auto-gerado
 * @property empregoId FK para o emprego associado
 * @property diaSemana Dia da semana desta configuração
 * @property ativo Se false, o dia é considerado folga
 * @property cargaHorariaMinutos Carga horária esperada em minutos (ex: 492 = 8h12m)
 * @property entradaIdeal Horário ideal de entrada
 * @property saidaIntervaloIdeal Horário ideal de saída para intervalo
 * @property voltaIntervaloIdeal Horário ideal de volta do intervalo
 * @property saidaIdeal Horário ideal de saída final
 * @property intervaloMinimoMinutos Intervalo mínimo obrigatório em minutos
 * @property toleranciaIntervaloMaisMinutos Tolerância para mais no intervalo
 * @property toleranciaIntervaloMenosMinutos Tolerância para menos no intervalo
 * @property criadoEm Timestamp de criação
 * @property atualizadoEm Timestamp da última atualização
 *
 * @author Thiago
 * @since 2.0.0
 */
@Entity(
    tableName = "horarios_dia_semana",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empregoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["empregoId", "diaSemana"], unique = true)
    ]
)
data class HorarioDiaSemanaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long,
    val diaSemana: DiaSemana,
    val ativo: Boolean = true,
    
    // Carga horária
    val cargaHorariaMinutos: Int = 492, // 8h12m padrão
    
    // Horários ideais (nullable = não configurado)
    val entradaIdeal: LocalTime? = null,
    val saidaIntervaloIdeal: LocalTime? = null,
    val voltaIntervaloIdeal: LocalTime? = null,
    val saidaIdeal: LocalTime? = null,
    
    // Intervalo
    val intervaloMinimoMinutos: Int = 60, // 1 hora padrão
    val toleranciaIntervaloMaisMinutos: Int = 0,
    val toleranciaIntervaloMenosMinutos: Int = 0,
    
    // Auditoria
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)
