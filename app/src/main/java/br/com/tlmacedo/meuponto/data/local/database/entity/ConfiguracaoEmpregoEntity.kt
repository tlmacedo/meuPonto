// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/ConfiguracaoEmpregoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade Room que armazena as configurações específicas de cada emprego.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Refatoração do sistema de ciclos de banco de horas
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

    // JORNADA DE TRABALHO
    val cargaHorariaDiariaMinutos: Int = 492,
    val jornadaMaximaDiariaMinutos: Int = 600,
    val intervaloMinimoInterjornadaMinutos: Int = 660,
    val intervaloMinimoMinutos: Int = 60,

    // TOLERÂNCIAS (apenas intervalo - entrada/saída são por dia)
    val toleranciaIntervaloMaisMinutos: Int = 0,

    // VALIDAÇÕES
    val exigeJustificativaInconsistencia: Boolean = false,

    // NSR
    val habilitarNsr: Boolean = false,
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,

    // LOCALIZAÇÃO
    val habilitarLocalizacao: Boolean = false,
    val localizacaoAutomatica: Boolean = false,
    val exibirLocalizacaoDetalhes: Boolean = true,

    // EXIBIÇÃO
    val exibirDuracaoTurno: Boolean = true,
    val exibirDuracaoIntervalo: Boolean = true,

    // PERÍODO RH (FECHAMENTO MENSAL)
    val primeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
    val diaInicioFechamentoRH: Int = 1,

    // SALDO
    val zerarSaldoSemanal: Boolean = false,
    val zerarSaldoPeriodoRH: Boolean = false,
    val ocultarSaldoTotal: Boolean = false,

    // BANCO DE HORAS - CICLO
    val bancoHorasHabilitado: Boolean = false,
    val periodoBancoSemanas: Int = 0,
    val periodoBancoMeses: Int = 0,
    val dataInicioCicloBancoAtual: LocalDate? = null,
    val diasUteisLembreteFechamento: Int = 3,
    val habilitarSugestaoAjuste: Boolean = false,
    val zerarBancoAntesPeriodo: Boolean = false,

    // AUDITORIA
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
        cargaHorariaDiariaMinutos = cargaHorariaDiariaMinutos,
        jornadaMaximaDiariaMinutos = jornadaMaximaDiariaMinutos,
        intervaloMinimoInterjornadaMinutos = intervaloMinimoInterjornadaMinutos,
        intervaloMinimoMinutos = intervaloMinimoMinutos,
        toleranciaIntervaloMaisMinutos = toleranciaIntervaloMaisMinutos,
        exigeJustificativaInconsistencia = exigeJustificativaInconsistencia,
        habilitarNsr = habilitarNsr,
        tipoNsr = tipoNsr,
        habilitarLocalizacao = habilitarLocalizacao,
        localizacaoAutomatica = localizacaoAutomatica,
        exibirLocalizacaoDetalhes = exibirLocalizacaoDetalhes,
        exibirDuracaoTurno = exibirDuracaoTurno,
        exibirDuracaoIntervalo = exibirDuracaoIntervalo,
        primeiroDiaSemana = primeiroDiaSemana,
        diaInicioFechamentoRH = diaInicioFechamentoRH,
        zerarSaldoSemanal = zerarSaldoSemanal,
        zerarSaldoPeriodoRH = zerarSaldoPeriodoRH,
        ocultarSaldoTotal = ocultarSaldoTotal,
        bancoHorasHabilitado = bancoHorasHabilitado,
        periodoBancoSemanas = periodoBancoSemanas,
        periodoBancoMeses = periodoBancoMeses,
        dataInicioCicloBancoAtual = dataInicioCicloBancoAtual,
        diasUteisLembreteFechamento = diasUteisLembreteFechamento,
        habilitarSugestaoAjuste = habilitarSugestaoAjuste,
        zerarBancoAntesPeriodo = zerarBancoAntesPeriodo,
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
        cargaHorariaDiariaMinutos = cargaHorariaDiariaMinutos,
        jornadaMaximaDiariaMinutos = jornadaMaximaDiariaMinutos,
        intervaloMinimoInterjornadaMinutos = intervaloMinimoInterjornadaMinutos,
        intervaloMinimoMinutos = intervaloMinimoMinutos,
        toleranciaIntervaloMaisMinutos = toleranciaIntervaloMaisMinutos,
        exigeJustificativaInconsistencia = exigeJustificativaInconsistencia,
        habilitarNsr = habilitarNsr,
        tipoNsr = tipoNsr,
        habilitarLocalizacao = habilitarLocalizacao,
        localizacaoAutomatica = localizacaoAutomatica,
        exibirLocalizacaoDetalhes = exibirLocalizacaoDetalhes,
        exibirDuracaoTurno = exibirDuracaoTurno,
        exibirDuracaoIntervalo = exibirDuracaoIntervalo,
        primeiroDiaSemana = primeiroDiaSemana,
        diaInicioFechamentoRH = diaInicioFechamentoRH,
        zerarSaldoSemanal = zerarSaldoSemanal,
        zerarSaldoPeriodoRH = zerarSaldoPeriodoRH,
        ocultarSaldoTotal = ocultarSaldoTotal,
        bancoHorasHabilitado = bancoHorasHabilitado,
        periodoBancoSemanas = periodoBancoSemanas,
        periodoBancoMeses = periodoBancoMeses,
        dataInicioCicloBancoAtual = dataInicioCicloBancoAtual,
        diasUteisLembreteFechamento = diasUteisLembreteFechamento,
        habilitarSugestaoAjuste = habilitarSugestaoAjuste,
        zerarBancoAntesPeriodo = zerarBancoAntesPeriodo,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )
