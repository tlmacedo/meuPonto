// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ConfiguracaoEmprego.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo de domínio para configurações de exibição e comportamento do emprego.
 *
 * Configurações de jornada, banco de horas e período RH foram migradas para
 * VersaoJornada para permitir versionamento temporal.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 8.0.0 - Simplificado: campos de jornada/banco migrados para VersaoJornada
 * @updated 9.0.0 - Adicionado fotoObrigatoria
 * @updated 10.0.0 - Adicionados campos completos de configuração de foto
 */
data class ConfiguracaoEmprego(
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
    // FOTO DE COMPROVANTE
    // ════════════════════════════════════════════════════════════════════════

    /** Habilita a funcionalidade de foto de comprovante */
    val fotoHabilitada: Boolean = false,

    /** Torna a foto obrigatória para concluir o registro */
    val fotoObrigatoria: Boolean = false,

    /** Formato de salvamento: JPEG ou PNG */
    val fotoFormato: FotoFormato = FotoFormato.JPEG,

    /** Qualidade de compressão (1-100, apenas JPEG) */
    val fotoQualidade: Int = 85,

    /** Resolução máxima em pixels (largura). 0 = sem limite */
    val fotoResolucaoMaxima: Int = 1920,

    /** Tamanho máximo em KB. 0 = sem limite */
    val fotoTamanhoMaximoKb: Int = 1024,

    /** Corrigir orientação automaticamente */
    val fotoCorrecaoOrientacao: Boolean = true,

    /** Permitir apenas câmera (desabilitar galeria) */
    val fotoApenasCamera: Boolean = false,

    /** Incluir localização GPS no EXIF da foto */
    val fotoIncluirLocalizacaoExif: Boolean = true,

    /** Habilitar backup automático na nuvem */
    val fotoBackupNuvemHabilitado: Boolean = false,

    /** Sincronizar apenas em Wi-Fi */
    val fotoBackupApenasWifi: Boolean = true,

    /** Local de salvamento da foto */
    val fotoLocalArmazenamento: String? = null,

    /** Registrar ponto automaticamente através de OCR da foto */
    val fotoRegistrarPontoOcr: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // CONFIGURAÇÃO RH E BANCO DE HORAS (Geral do Emprego)
    // ════════════════════════════════════════════════════════════════════════

    val diaInicioFechamentoRH: Int = 11,
    val bancoHorasHabilitado: Boolean = false,
    val bancoHorasCicloMeses: Int = 6,
    val bancoHorasDataInicioCiclo: LocalDate? = null,
    val bancoHorasZerarAoFinalCiclo: Boolean = false,

    // ════════════════════════════════════════════════════════════════════════
    // VALIDAÇÃO
    // ════════════════════════════════════════════════════════════════════════

    val exigeJustificativaInconsistencia: Boolean = false,

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
) {
    // ════════════════════════════════════════════════════════════════════════
    // PROPRIEDADES DERIVADAS
    // ════════════════════════════════════════════════════════════════════════

    /** Verifica se a funcionalidade de foto está ativa */
    val fotoAtiva: Boolean
        get() = fotoHabilitada

    /** Verifica se permite seleção da galeria */
    val fotoPermiteGaleria: Boolean
        get() = fotoHabilitada && !fotoApenasCamera

    /** Verifica se o backup na nuvem está configurado */
    val backupConfigurado: Boolean
        get() = fotoHabilitada && fotoBackupNuvemHabilitado

    companion object {
        /**
         * Cria uma configuração padrão para um emprego.
         */
        fun criarPadrao(empregoId: Long): ConfiguracaoEmprego =
            ConfiguracaoEmprego(empregoId = empregoId)

        /**
         * Valores padrão de configuração de foto.
         */
        object FotoPadroes {
            const val QUALIDADE_MINIMA = 60
            const val QUALIDADE_PADRAO = 85
            const val QUALIDADE_MAXIMA = 100

            const val RESOLUCAO_720P = 1280
            const val RESOLUCAO_1080P = 1920
            const val RESOLUCAO_ORIGINAL = 0

            const val TAMANHO_512KB = 512
            const val TAMANHO_1MB = 1024
            const val TAMANHO_2MB = 2048
            const val TAMANHO_ILIMITADO = 0
        }
    }
}
