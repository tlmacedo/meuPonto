package br.com.tlmacedo.meuponto.presentation.screen.settings.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.BackupRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.preferencias.SalvarPreferenciasGlobaisUseCase
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
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.temporal.ChronoUnit
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
    data class ExportarBackup(val outputStream: OutputStream) : BackupAction
    data class ImportarBackup(val inputStream: InputStream) : BackupAction
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
    data object SolicitarDestinoExportacao : BackupEvent
    data object SolicitarOrigemImportacao : BackupEvent
}

/**
 * ViewModel da tela de backup e dados.
 *
 * @author Thiago
 * @since 9.1.0
 */
@HiltViewModel
@Suppress("DEPRECATION") // observarTodos é usado intencionalmente para backup completo
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val empregoRepository: EmpregoRepository,
    private val pontoRepository: PontoRepository,
    private val feriadoRepository: FeriadoRepository,
    private val salvarPreferenciasGlobaisUseCase: SalvarPreferenciasGlobaisUseCase
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
            is BackupAction.ExportarBackup -> exportarBackup(action.outputStream)
            is BackupAction.ImportarBackup -> importarBackup(action.inputStream)
            BackupAction.LimparDadosAntigos -> limparDadosAntigos()
            BackupAction.Recarregar -> carregarEstatisticas()
        }
    }

    fun iniciarExportacao() {
        viewModelScope.launch {
            _eventos.emit(BackupEvent.SolicitarDestinoExportacao)
        }
    }

    fun iniciarImportacao() {
        viewModelScope.launch {
            _eventos.emit(BackupEvent.SolicitarOrigemImportacao)
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

    private fun exportarBackup(outputStream: OutputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "exportar") }

            backupRepository.exportarBanco(outputStream)
                .onSuccess {
                    salvarPreferenciasGlobaisUseCase.registrarBackupRealizado()
                    _eventos.emit(BackupEvent.ExportacaoConcluida)
                }
                .onFailure { e ->
                    Timber.e(e, "Erro ao exportar backup")
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao exportar: ${e.message}"))
                }

            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun importarBackup(inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "importar") }

            backupRepository.importarBanco(inputStream)
                .onSuccess {
                    _eventos.emit(BackupEvent.ImportacaoConcluida)
                    carregarEstatisticas()
                }
                .onFailure { e ->
                    Timber.e(e, "Erro ao importar backup")
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao importar: ${e.message}"))
                }

            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun limparDadosAntigos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "limpar") }

            try {
                // Limpa pontos com mais de 2 anos (padrão)
                val dataLimite = LocalDate.now().minus(2, ChronoUnit.YEARS)
                val registrosRemovidos = pontoRepository.excluirPontosAnterioresA(dataLimite)

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
