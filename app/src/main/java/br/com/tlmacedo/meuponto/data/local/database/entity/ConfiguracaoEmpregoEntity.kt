// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/entity/ConfiguracaoEmpregoEntity.kt
package br.com.tlmacedo.meuponto.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.FotoFormato
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entidade Room para configurações de emprego.
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

    @ColumnInfo(defaultValue = "0")
    val habilitarNsr: Boolean = false,
    @ColumnInfo(defaultValue = "NUMERICO")
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,

    // ════════════════════════════════════════════════════════════════════════
    // LOCALIZAÇÃO
    // ════════════════════════════════════════════════════════════════════════

    @ColumnInfo(defaultValue = "0")
    val habilitarLocalizacao: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val localizacaoAutomatica: Boolean = false,
    @ColumnInfo(defaultValue = "1")
    val exibirLocalizacaoDetalhes: Boolean = true,

    // ════════════════════════════════════════════════════════════════════════
    // FOTO DE COMPROVANTE
    // ════════════════════════════════════════════════════════════════════════

    @ColumnInfo(defaultValue = "0")
    val fotoHabilitada: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val fotoObrigatoria: Boolean = false,
    @ColumnInfo(defaultValue = "JPEG")
    val fotoFormato: FotoFormato = FotoFormato.JPEG,
    @ColumnInfo(defaultValue = "85")
    val fotoQualidade: Int = 85,
    @ColumnInfo(defaultValue = "1920")
    val fotoResolucaoMaxima: Int = 1920,
    @ColumnInfo(defaultValue = "1024")
    val fotoTamanhoMaximoKb: Int = 1024,
    @ColumnInfo(defaultValue = "1")
    val fotoCorrecaoOrientacao: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val fotoApenasCamera: Boolean = false,
    @ColumnInfo(defaultValue = "1")
    val fotoIncluirLocalizacaoExif: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val fotoBackupNuvemHabilitado: Boolean = false,
    @ColumnInfo(defaultValue = "1")
    val fotoBackupApenasWifi: Boolean = true,
    val fotoLocalArmazenamento: String? = null,
    @ColumnInfo(defaultValue = "0")
    val fotoRegistrarPontoOcr: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // CONFIGURAÇÃO RH E BANCO DE HORAS
    // ════════════════════════════════════════════════════════════════════════

    @ColumnInfo(defaultValue = "11")
    val diaInicioFechamentoRH: Int = 11,
    @ColumnInfo(defaultValue = "0")
    val bancoHorasHabilitado: Boolean = false,
    @ColumnInfo(defaultValue = "6")
    val bancoHorasCicloMeses: Int = 6,
    val bancoHorasDataInicioCiclo: LocalDate? = null,
    @ColumnInfo(defaultValue = "0")
    val bancoHorasZerarAoFinalCiclo: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // VALIDAÇÃO
    // ════════════════════════════════════════════════════════════════════════

    @ColumnInfo(defaultValue = "0")
    val exigeJustificativaInconsistencia: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // EXIBIÇÃO
    // ════════════════════════════════════════════════════════════════════════

    @ColumnInfo(defaultValue = "1")
    val exibirDuracaoTurno: Boolean = true,
    @ColumnInfo(defaultValue = "1")
    val exibirDuracaoIntervalo: Boolean = true,

    // ════════════════════════════════════════════════════════════════════════
    // AUDITORIA
    // ════════════════════════════════════════════════════════════════════════

    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

fun ConfiguracaoEmpregoEntity.toDomain(): ConfiguracaoEmprego =
    ConfiguracaoEmprego(
        id = id,
        empregoId = empregoId,
        habilitarNsr = habilitarNsr,
        tipoNsr = tipoNsr,
        habilitarLocalizacao = habilitarLocalizacao,
        localizacaoAutomatica = localizacaoAutomatica,
        exibirLocalizacaoDetalhes = exibirLocalizacaoDetalhes,
        fotoHabilitada = fotoHabilitada,
        fotoObrigatoria = fotoObrigatoria,
        fotoFormato = fotoFormato,
        fotoQualidade = fotoQualidade,
        fotoResolucaoMaxima = fotoResolucaoMaxima,
        fotoTamanhoMaximoKb = fotoTamanhoMaximoKb,
        fotoCorrecaoOrientacao = fotoCorrecaoOrientacao,
        fotoApenasCamera = fotoApenasCamera,
        fotoIncluirLocalizacaoExif = fotoIncluirLocalizacaoExif,
        fotoBackupNuvemHabilitado = fotoBackupNuvemHabilitado,
        fotoBackupApenasWifi = fotoBackupApenasWifi,
        fotoLocalArmazenamento = fotoLocalArmazenamento,
        fotoRegistrarPontoOcr = fotoRegistrarPontoOcr,
        diaInicioFechamentoRH = diaInicioFechamentoRH,
        bancoHorasHabilitado = bancoHorasHabilitado,
        bancoHorasCicloMeses = bancoHorasCicloMeses,
        bancoHorasDataInicioCiclo = bancoHorasDataInicioCiclo,
        bancoHorasZerarAoFinalCiclo = bancoHorasZerarAoFinalCiclo,
        exigeJustificativaInconsistencia = exigeJustificativaInconsistencia,
        exibirDuracaoTurno = exibirDuracaoTurno,
        exibirDuracaoIntervalo = exibirDuracaoIntervalo,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )

fun ConfiguracaoEmprego.toEntity(): ConfiguracaoEmpregoEntity =
    ConfiguracaoEmpregoEntity(
        id = id,
        empregoId = empregoId,
        habilitarNsr = habilitarNsr,
        tipoNsr = tipoNsr,
        habilitarLocalizacao = habilitarLocalizacao,
        localizacaoAutomatica = localizacaoAutomatica,
        exibirLocalizacaoDetalhes = exibirLocalizacaoDetalhes,
        fotoHabilitada = fotoHabilitada,
        fotoObrigatoria = fotoObrigatoria,
        fotoFormato = fotoFormato,
        fotoQualidade = fotoQualidade,
        fotoResolucaoMaxima = fotoResolucaoMaxima,
        fotoTamanhoMaximoKb = fotoTamanhoMaximoKb,
        fotoCorrecaoOrientacao = fotoCorrecaoOrientacao,
        fotoApenasCamera = fotoApenasCamera,
        fotoIncluirLocalizacaoExif = fotoIncluirLocalizacaoExif,
        fotoBackupNuvemHabilitado = fotoBackupNuvemHabilitado,
        fotoBackupApenasWifi = fotoBackupApenasWifi,
        fotoLocalArmazenamento = fotoLocalArmazenamento,
        fotoRegistrarPontoOcr = fotoRegistrarPontoOcr,
        diaInicioFechamentoRH = diaInicioFechamentoRH,
        bancoHorasHabilitado = bancoHorasHabilitado,
        bancoHorasCicloMeses = bancoHorasCicloMeses,
        bancoHorasDataInicioCiclo = bancoHorasDataInicioCiclo,
        bancoHorasZerarAoFinalCiclo = bancoHorasZerarAoFinalCiclo,
        exigeJustificativaInconsistencia = exigeJustificativaInconsistencia,
        exibirDuracaoTurno = exibirDuracaoTurno,
        exibirDuracaoIntervalo = exibirDuracaoIntervalo,
        criadoEm = criadoEm,
        atualizadoEm = atualizadoEm
    )
