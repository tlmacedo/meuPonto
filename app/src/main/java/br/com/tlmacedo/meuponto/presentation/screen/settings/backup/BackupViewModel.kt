package br.com.tlmacedo.meuponto.presentation.screen.settings.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado da tela de backup.
 */
data class BackupUiState(
    val isLoading: Boolean = true,
    val isProcessando: Boolean = false,
    val operacaoAtual: String? = null,
    val totalEmpregos: Int = 0,
    val totalPontos: Int = 0,
    val totalFeriados: Int = 0,
    val tamanhoEstimado: String = "..."
)

/**
 * Ações da tela de backup.
 */
sealed interface BackupAction {
    data object ExportarBackup : BackupAction
    data object ImportarBackup : BackupAction
    data object LimparDadosAntigos : BackupAction
    data object Recarregar : BackupAction
}

/**
 * Eventos da tela de backup.
 */
sealed interface BackupEvent {
    data class MostrarMensagem(val mensagem: String) : BackupEvent
    data object ExportacaoConcluida : BackupEvent
    data object ImportacaoConcluida : BackupEvent
    data class LimpezaConcluida(val registrosRemovidos: Int) : BackupEvent
}

/**
 * ViewModel da tela de backup e dados.
 *
 * @author Thiago
 * @since 9.0.0
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val pontoRepository: PontoRepository,
    private val feriadoRepository: FeriadoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<BackupEvent>()
    val eventos: SharedFlow<BackupEvent> = _eventos.asSharedFlow()

    init {
        carregarEstatisticas()
    }

    fun onAction(action: BackupAction) {
        when (action) {
            BackupAction.ExportarBackup -> exportarBackup()
            BackupAction.ImportarBackup -> importarBackup()
            BackupAction.LimparDadosAntigos -> limparDadosAntigos()
            BackupAction.Recarregar -> carregarEstatisticas()
        }
    }

    private fun carregarEstatisticas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val totalEmpregos = empregoRepository.contarTodos()

                // Conta pontos usando o flow (primeiro valor)
                val pontos = pontoRepository.observarTodos().first()
                val totalPontos = pontos.size

                // Conta feriados usando a lista
                val feriados = feriadoRepository.buscarTodos()
                val totalFeriados = feriados.size

                // Estimativa simplificada de tamanho
                val tamanhoBytes = (totalPontos * 200L) + (totalEmpregos * 500L) + (totalFeriados * 100L)
                val tamanhoEstimado = formatarTamanho(tamanhoBytes)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalEmpregos = totalEmpregos,
                        totalPontos = totalPontos,
                        totalFeriados = totalFeriados,
                        tamanhoEstimado = tamanhoEstimado
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar estatísticas")
                _uiState.update { it.copy(isLoading = false) }
                _eventos.emit(BackupEvent.MostrarMensagem("Erro ao carregar estatísticas"))
            }
        }
    }

    private fun formatarTamanho(bytes: Long): String {
        return when {
            bytes < 1024L -> "${bytes}B"
            bytes < 1024L * 1024L -> "${bytes / 1024L}KB"
            else -> "${bytes / (1024L * 1024L)}MB"
        }
    }

    private fun exportarBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "exportar") }

            try {
                // TODO: Implementar exportação real usando FileProvider/SAF
                kotlinx.coroutines.delay(2000) // Simula processamento

                _eventos.emit(BackupEvent.ExportacaoConcluida)
            } catch (e: Exception) {
                Timber.e(e, "Erro ao exportar backup")
                _eventos.emit(BackupEvent.MostrarMensagem("Erro ao exportar: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
            }
        }
    }

    private fun importarBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "importar") }

            try {
                // TODO: Implementar importação real usando SAF
                kotlinx.coroutines.delay(2000) // Simula processamento

                _eventos.emit(BackupEvent.ImportacaoConcluida)
                carregarEstatisticas() // Atualiza estatísticas após importação
            } catch (e: Exception) {
                Timber.e(e, "Erro ao importar backup")
                _eventos.emit(BackupEvent.MostrarMensagem("Erro ao importar: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
            }
        }
    }

    private fun limparDadosAntigos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "limpar") }

            try {
                // TODO: Implementar limpeza real no repositório
                val registrosRemovidos = 0

                _eventos.emit(BackupEvent.LimpezaConcluida(registrosRemovidos))
                carregarEstatisticas() // Atualiza estatísticas após limpeza
            } catch (e: Exception) {
                Timber.e(e, "Erro ao limpar dados")
                _eventos.emit(BackupEvent.MostrarMensagem("Erro ao limpar: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
            }
        }
    }
}
