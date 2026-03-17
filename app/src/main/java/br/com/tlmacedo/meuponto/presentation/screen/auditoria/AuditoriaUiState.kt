// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/auditoria/AuditoriaUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.auditoria

import br.com.tlmacedo.meuponto.domain.model.AcaoAuditoria
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import java.time.LocalDate

/**
 * Estado da UI da tela de Auditoria.
 *
 * @author Thiago
 * @since 11.0.0
 */
data class AuditoriaUiState(
    val logs: List<AuditLog> = emptyList(),
    val logsAgrupados: Map<LocalDate, List<AuditLog>> = emptyMap(),
    val isLoading: Boolean = false,
    val mensagemErro: String? = null,
    val showFiltros: Boolean = false,
    val filtroAtivo: FiltroAuditoria = FiltroAuditoria(),
    val logSelecionado: AuditLog? = null
) {
    val quantidadeLogs: Int get() = logs.size
    val temLogs: Boolean get() = logs.isNotEmpty()
}

/**
 * Filtros para a tela de auditoria.
 */
data class FiltroAuditoria(
    val termoBusca: String = "",
    val dataInicio: LocalDate? = null,
    val dataFim: LocalDate? = null,
    val acoes: Set<AcaoAuditoria> = emptySet(),
    val entityTypes: Set<String> = emptySet()
) {
    val temFiltrosAtivos: Boolean
        get() = termoBusca.isNotBlank() ||
                dataInicio != null ||
                dataFim != null ||
                acoes.isNotEmpty() ||
                entityTypes.isNotEmpty()
}

/**
 * Eventos da tela de Auditoria.
 */
sealed class AuditoriaEvent {
    data object CarregarLogs : AuditoriaEvent()
    data object ToggleFiltros : AuditoriaEvent()
    data class AtualizarFiltro(val filtro: FiltroAuditoria) : AuditoriaEvent()
    data object LimparFiltros : AuditoriaEvent()
    data class SelecionarLog(val log: AuditLog) : AuditoriaEvent()
    data object FecharDetalhes : AuditoriaEvent()
    data object LimparMensagem : AuditoriaEvent()
}
