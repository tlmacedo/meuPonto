// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ConfiguracaoEmprego.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDateTime

/**
 * Modelo de domínio para configurações de EXIBIÇÃO e COMPORTAMENTO do emprego.
 *
 * Configurações de jornada, banco de horas e período RH foram migradas para
 * VersaoJornada para permitir versionamento temporal.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 8.0.0 - Simplificado: campos de jornada/banco migrados para VersaoJornada
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
    companion object {
        /**
         * Cria uma configuração padrão para um emprego.
         */
        fun criarPadrao(empregoId: Long): ConfiguracaoEmprego =
            ConfiguracaoEmprego(empregoId = empregoId)
    }
}
