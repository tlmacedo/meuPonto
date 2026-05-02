// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/VersaoJornadaEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade Room que representa uma versão de jornada de trabalho.
 *
 * Esta entidade armazena o "snapshot" das regras de um emprego em um determinado período.
 * Isso permite que alterações no contrato de trabalho (mudança de carga horária,
 * ativação de banco de horas, etc) não afetem o cálculo de registros passados.
 *
 * @property id Identificador único
 * @property empregoId FK para o emprego dono desta versão
 * @property dataInicio Data a partir da qual estas regras valem
 * @property dataFim Data limite de vigência (null se for a versão atual)
 * @property descricao Nome amigável para a versão (ex: "Contrato 2024")
 * @property numeroVersao Sequencial para controle de histórico
 * @property vigente Flag indicando se é a configuração ativa no momento
 *
 * @author Thiago
 * @since 2.7.0
 * @updated 8.0.0 - Unificação de regras de banco, jornada e RH em uma única entidade temporal
 */
@Entity(
    tableName = "versoes_jornada",
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
        Index(value = ["empregoId", "dataInicio"]),
        Index(value = ["empregoId", "dataFim"])
    ]
)
data class VersaoJornadaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val empregoId: Long,
    val dataInicio: LocalDate,
    val dataFim: LocalDate? = null,
    val descricao: String? = null,
    @ColumnInfo(defaultValue = "1")
    val numeroVersao: Int = 1,
    @ColumnInfo(defaultValue = "1")
    val vigente: Boolean = true,

    // ════════════════════════════════════════════════════════════════════════
    // JORNADA (campos existentes)
    // ════════════════════════════════════════════════════════════════════════
    /** Jornada máxima diária total (soma de todos os turnos). Default: 600min (10h) */
    @ColumnInfo(defaultValue = "600")
    val jornadaMaximaDiariaMinutos: Int = 600,
    /** Intervalo mínimo entre jornadas (interjornada). Default: 660min (11h) */
    @ColumnInfo(defaultValue = "660")
    val intervaloMinimoInterjornadaMinutos: Int = 660,
    /** Turno máximo (tempo entre entrada e saída de um turno). Default: 360min (6h) */
    @ColumnInfo(defaultValue = "360")
    val turnoMaximoMinutos: Int = 360,
    /** Intervalo mínimo de almoço/descanso. Default: 60min */
    @ColumnInfo(defaultValue = "60")
    val intervaloMinimoAlmocoMinutos: Int = 60,
    /** Intervalo mínimo de descanso (curtos). Default: 15min */
    @ColumnInfo(defaultValue = "15")
    val intervaloMinimoDescansoMinutos: Int = 15,
    /** Tolerância para mais no intervalo de almoço. Default: 0min */
    @ColumnInfo(defaultValue = "0")
    val toleranciaIntervaloMaisMinutos: Int = 0,
    /** Tolerância de retorno de intervalo. Default: 5min */
    @ColumnInfo(defaultValue = "5")
    val toleranciaRetornoIntervaloMinutos: Int = 5,

    // ════════════════════════════════════════════════════════════════════════
    // CARGA HORÁRIA (migrados de ConfiguracaoEmprego)
    // ════════════════════════════════════════════════════════════════════════
    /** Carga horária base diária. Default: 480min (8h) */
    @ColumnInfo(defaultValue = "480")
    val cargaHorariaDiariaMinutos: Int = 480,
    /** Acréscimo diário para compensar dias ponte. Default: 12min (2026) */
    @ColumnInfo(defaultValue = "12")
    val acrescimoMinutosDiasPontes: Int = 12,
    /** Carga horária semanal total. Default: 2460min (41h = 5 × 492) */
    @ColumnInfo(defaultValue = "2460")
    val cargaHorariaSemanalMinutos: Int = 2460,

    // ════════════════════════════════════════════════════════════════════════
    // PERÍODO/SALDO (migrados de ConfiguracaoEmprego)
    // ════════════════════════════════════════════════════════════════════════
    /** Primeiro dia da semana para cálculos. Default: SEGUNDA */
    @ColumnInfo(defaultValue = "SEGUNDA")
    val primeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
    /** Dia do mês para fechamento RH. Default: 1 */
    @ColumnInfo(defaultValue = "1")
    val diaInicioFechamentoRH: Int = 1,
    /** Zerar saldo ao fim de cada semana. Default: false */
    @ColumnInfo(defaultValue = "0")
    val zerarSaldoSemanal: Boolean = false,
    /** Zerar saldo ao fim do período RH. Default: false */
    @ColumnInfo(defaultValue = "0")
    val zerarSaldoPeriodoRH: Boolean = false,
    /** Ocultar saldo total na interface. Default: false */
    @ColumnInfo(defaultValue = "0")
    val ocultarSaldoTotal: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // BANCO DE HORAS (migrados de ConfiguracaoEmprego)
    // ════════════════════════════════════════════════════════════════════════
    /** Flag que indica se banco de horas está habilitado. Default: false */
    @ColumnInfo(defaultValue = "0")
    val bancoHorasHabilitado: Boolean = false,
    /** Período do ciclo em dias. Default: 0 */
    @ColumnInfo(defaultValue = "0")
    val periodoBancoDias: Int = 0,
    /** Período do ciclo em semanas. Default: 0 */
    @ColumnInfo(defaultValue = "0")
    val periodoBancoSemanas: Int = 0,
    /** Período do ciclo em meses. Default: 0 */
    @ColumnInfo(defaultValue = "0")
    val periodoBancoMeses: Int = 0,
    /** Período do ciclo em anos. Default: 0 */
    @ColumnInfo(defaultValue = "0")
    val periodoBancoAnos: Int = 0,
    /** Data de início do ciclo atual. Null se não configurado */
    val dataInicioCicloBancoAtual: LocalDate? = null,
    /** Dias úteis antes do fim para lembrete. Default: 3 */
    @ColumnInfo(defaultValue = "3")
    val diasUteisLembreteFechamento: Int = 3,
    /** Habilitar sugestão de ajuste automático. Default: false */
    @ColumnInfo(defaultValue = "0")
    val habilitarSugestaoAjuste: Boolean = false,
    /** Ignorar registros antes do início do banco. Default: false */
    @ColumnInfo(defaultValue = "0")
    val zerarBancoAntesPeriodo: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // VALIDAÇÃO (migrado de ConfiguracaoEmprego)
    // ════════════════════════════════════════════════════════════════════════
    /** Exige justificativa para inconsistências. Default: false */
    @ColumnInfo(defaultValue = "0")
    val exigeJustificativaInconsistencia: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // AUDITORIA
    // ════════════════════════════════════════════════════════════════════════
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

fun VersaoJornadaEntity.toDomain(): br.com.tlmacedo.meuponto.domain.model.VersaoJornada =
    br.com.tlmacedo.meuponto.domain.model.VersaoJornada(
        id = id,
        empregoId = empregoId,
        dataInicio = dataInicio,
        dataFim = dataFim,
        descricao = descricao,
        numeroVersao = numeroVersao,
        vigente = vigente,
        // Jornada
        jornadaMaximaDiariaMinutos = jornadaMaximaDiariaMinutos,
        intervaloMinimoInterjornadaMinutos = intervaloMinimoInterjornadaMinutos,
        intervaloMinimoAlmocoMinutos = intervaloMinimoAlmocoMinutos,
        intervaloMinimoDescansoMinutos = intervaloMinimoDescansoMinutos,
        toleranciaIntervaloMaisMinutos = toleranciaIntervaloMaisMinutos,
        toleranciaRetornoIntervaloMinutos = toleranciaRetornoIntervaloMinutos,
        turnoMaximoMinutos = turnoMaximoMinutos,
        // Carga Horária
        cargaHorariaDiariaMinutos = cargaHorariaDiariaMinutos,
        acrescimoMinutosDiasPontes = acrescimoMinutosDiasPontes,
        cargaHorariaSemanalMinutos = cargaHorariaSemanalMinutos,
        // Período/Saldo
        primeiroDiaSemana = primeiroDiaSemana,
        diaInicioFechamentoRH = diaInicioFechamentoRH,
        zerarSaldoSemanal = zerarSaldoSemanal,
        zerarSaldoPeriodoRH = zerarSaldoPeriodoRH,
        ocultarSaldoTotal = ocultarSaldoTotal,
        // Banco de Horas
        bancoHorasHabilitado = bancoHorasHabilitado,
        periodoBancoDias = periodoBancoDias,
        periodoBancoSemanas = periodoBancoSemanas,
        periodoBancoMeses = periodoBancoMeses,
        periodoBancoAnos = periodoBancoAnos,
        dataInicioCicloBancoAtual = dataInicioCicloBancoAtual,
        diasUteisLembreteFechamento = diasUteisLembreteFechamento,
        habilitarSugestaoAjuste = habilitarSugestaoAjuste,
        zerarBancoAntesPeriodo = zerarBancoAntesPeriodo,
        // Validação
        exigeJustificativaInconsistencia = exigeJustificativaInconsistencia,
        // Auditoria
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )

fun br.com.tlmacedo.meuponto.domain.model.VersaoJornada.toEntity(): VersaoJornadaEntity =
    VersaoJornadaEntity(
        id = id,
        empregoId = empregoId,
        dataInicio = dataInicio,
        dataFim = dataFim,
        descricao = descricao,
        numeroVersao = numeroVersao,
        vigente = vigente,
        // Jornada
        jornadaMaximaDiariaMinutos = jornadaMaximaDiariaMinutos,
        intervaloMinimoInterjornadaMinutos = intervaloMinimoInterjornadaMinutos,
        intervaloMinimoAlmocoMinutos = intervaloMinimoAlmocoMinutos,
        intervaloMinimoDescansoMinutos = intervaloMinimoDescansoMinutos,
        toleranciaIntervaloMaisMinutos = toleranciaIntervaloMaisMinutos,
        toleranciaRetornoIntervaloMinutos = toleranciaRetornoIntervaloMinutos,
        turnoMaximoMinutos = turnoMaximoMinutos,
        // Carga Horária
        cargaHorariaDiariaMinutos = cargaHorariaDiariaMinutos,
        acrescimoMinutosDiasPontes = acrescimoMinutosDiasPontes,
        cargaHorariaSemanalMinutos = cargaHorariaSemanalMinutos,
        // Período/Saldo
        primeiroDiaSemana = primeiroDiaSemana,
        diaInicioFechamentoRH = diaInicioFechamentoRH,
        zerarSaldoSemanal = zerarSaldoSemanal,
        zerarSaldoPeriodoRH = zerarSaldoPeriodoRH,
        ocultarSaldoTotal = ocultarSaldoTotal,
        // Banco de Horas
        bancoHorasHabilitado = bancoHorasHabilitado,
        periodoBancoDias = periodoBancoDias,
        periodoBancoSemanas = periodoBancoSemanas,
        periodoBancoMeses = periodoBancoMeses,
        periodoBancoAnos = periodoBancoAnos,
        dataInicioCicloBancoAtual = dataInicioCicloBancoAtual,
        diasUteisLembreteFechamento = diasUteisLembreteFechamento,
        habilitarSugestaoAjuste = habilitarSugestaoAjuste,
        zerarBancoAntesPeriodo = zerarBancoAntesPeriodo,
        // Validação
        exigeJustificativaInconsistencia = exigeJustificativaInconsistencia,
        // Auditoria
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )