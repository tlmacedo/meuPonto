// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/ConfiguracaoEmpregoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.LocalDateTime

/**
 * Entidade Room que armazena configurações de EXIBIÇÃO e COMPORTAMENTO do emprego.
 *
 * Configurações de jornada, banco de horas e período RH foram migradas para
 * VersaoJornadaEntity para permitir versionamento temporal.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 8.0.0 - Simplificado: campos de jornada/banco migrados para VersaoJornada
 */
@Entity(
    tableName = "configuracoes_emprego",
    foreignKeys = [
        ForeignKey(
            entity = EmpregoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empregoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["empregoId"], unique = true)]
)
data class ConfiguracaoEmpregoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long,

    // ════════════════════════════════════════════════════════════════════════
    // NSR (Número Sequencial de Registro)
    // ════════════════════════════════════════════════════════════════════════
    val habilitarNsr: Boolean = false,
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,

    // ════════════════════════════════════════════════════════════════════════
    // LOCALIZAÇÃO
    // ════════════════════════════════════════════════════════════════════════
    val habilitarLocalizacao: Boolean = false,
    val localizacaoAutomatica: Boolean = false,
    val exibirLocalizacaoDetalhes: Boolean = true,

    // ════════════════════════════════════════════════════════════════════════
    // EXIBIÇÃO
    // ════════════════════════════════════════════════════════════════════════
    val exibirDuracaoTurno: Boolean = true,
    val exibirDuracaoIntervalo: Boolean = true,

    // ════════════════════════════════════════════════════════════════════════
    // AUDITORIA
    // ════════════════════════════════════════════════════════════════════════
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

/**
 * Converte Entity para Domain Model.
 */
fun ConfiguracaoEmpregoEntity.toDomain(): ConfiguracaoEmprego =
    ConfiguracaoEmprego(
        id = id,
        empregoId = empregoId,
        habilitarNsr = habilitarNsr,
        tipoNsr = tipoNsr,
        habilitarLocalizacao = habilitarLocalizacao,
        localizacaoAutomatica = localizacaoAutomatica,
        exibirLocalizacaoDetalhes = exibirLocalizacaoDetalhes,
        exibirDuracaoTurno = exibirDuracaoTurno,
        exibirDuracaoIntervalo = exibirDuracaoIntervalo,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )

/**
 * Converte Domain Model para Entity.
 */
fun ConfiguracaoEmprego.toEntity(): ConfiguracaoEmpregoEntity =
    ConfiguracaoEmpregoEntity(
        id = id,
        empregoId = empregoId,
        habilitarNsr = habilitarNsr,
        tipoNsr = tipoNsr,
        habilitarLocalizacao = habilitarLocalizacao,
        localizacaoAutomatica = localizacaoAutomatica,
        exibirLocalizacaoDetalhes = exibirLocalizacaoDetalhes,
        exibirDuracaoTurno = exibirDuracaoTurno,
        exibirDuracaoIntervalo = exibirDuracaoIntervalo,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )
