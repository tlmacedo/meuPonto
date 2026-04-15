// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/auditoria/AuditoriaViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.auditoria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.AuditLog
import br.com.tlmacedo.meuponto.domain.repository.AuditLogRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel da tela de Auditoria.
 *
 * @author Thiago
 * @since 11.0.0
 * @updated 12.1.1 - Adicionado suporte ao job metadata
 */
@HiltViewModel
class AuditoriaViewModel @Inject constructor(
    private val auditLogRepository: AuditLogRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditoriaUiState())
    val uiState: StateFlow<AuditoriaUiState> = _uiState.asStateFlow()

    init {
        carregarLogs()
        carregarEmpregoAtivo()
    }

    fun onEvent(event: AuditoriaEvent) {
        when (event) {
            is AuditoriaEvent.CarregarLogs -> carregarLogs()
            is AuditoriaEvent.ToggleFiltros -> toggleFiltros()
            is AuditoriaEvent.AtualizarFiltro -> atualizarFiltro(event.filtro)
            is AuditoriaEvent.LimparFiltros -> limparFiltros()
            is AuditoriaEvent.SelecionarLog -> selecionarLog(event.log)
            is AuditoriaEvent.FecharDetalhes -> fecharDetalhes()
            is AuditoriaEvent.LimparMensagem -> limparMensagem()
        }
    }

    private fun carregarEmpregoAtivo() {
        viewModelScope.launch {
            obterEmpregoAtivoUseCase.observar().collectLatest { emprego ->
                _uiState.update {
                    it.copy(
                        empregoApelido = emprego?.apelido,
                        empregoLogo = emprego?.logo
                    )
                }
            }
        }
    }

    private fun carregarLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                auditLogRepository.observarTodos().collect { logs ->
                    val logsFiltrados = aplicarFiltros(logs, _uiState.value.filtroAtivo)
                    val logsAgrupados = agruparPorData(logsFiltrados)

                    _uiState.update {
                        it.copy(
                            logs = logsFiltrados,
                            logsAgrupados = logsAgrupados,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar logs de auditoria")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensagemErro = "Erro ao carregar logs: ${e.message}"
                    )
                }
            }
        }
    }

    private fun aplicarFiltros(logs: List<AuditLog>, filtro: FiltroAuditoria): List<AuditLog> {
        return logs.filter { log ->
            val logDate = log.data

            // Filtro por data início
            val passaDataInicio = filtro.dataInicio?.let { logDate >= it } ?: true

            // Filtro por data fim
            val passaDataFim = filtro.dataFim?.let { logDate <= it } ?: true

            // Filtro por ações
            val passaAcao = filtro.acoes.isEmpty() || log.acao in filtro.acoes

            // Filtro por tipo de entidade
            val passaEntityType = filtro.entityTypes.isEmpty() || log.entidade in filtro.entityTypes

            // Filtro por termo de busca
            val passaBusca = filtro.termoBusca.isBlank() ||
                    log.description.contains(filtro.termoBusca, ignoreCase = true) ||
                    log.entidade.contains(filtro.termoBusca, ignoreCase = true) ||
                    log.acao.name.contains(filtro.termoBusca, ignoreCase = true)

            passaDataInicio && passaDataFim && passaAcao && passaEntityType && passaBusca
        }
    }

    private fun agruparPorData(logs: List<AuditLog>): Map<java.time.LocalDate, List<AuditLog>> {
        return logs.groupBy { it.data }
            .toSortedMap(compareByDescending { it })
    }

    private fun toggleFiltros() {
        _uiState.update { it.copy(showFiltros = !it.showFiltros) }
    }

    private fun atualizarFiltro(filtro: FiltroAuditoria) {
        _uiState.update { it.copy(filtroAtivo = filtro) }
        carregarLogs()
    }

    private fun limparFiltros() {
        _uiState.update { it.copy(filtroAtivo = FiltroAuditoria()) }
        carregarLogs()
    }

    private fun selecionarLog(log: AuditLog) {
        _uiState.update { it.copy(logSelecionado = log) }
    }

    private fun fecharDetalhes() {
        _uiState.update { it.copy(logSelecionado = null) }
    }

    private fun limparMensagem() {
        _uiState.update { it.copy(mensagemErro = null) }
    }
}
