package br.com.tlmacedo.meuponto.presentation.screen.relatorios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.relatorio.GerarRelatorioMensalUseCase
import br.com.tlmacedo.meuponto.domain.usecase.relatorio.GerarEspelhoPontoPdfUseCase
import br.com.tlmacedo.meuponto.domain.usecase.relatorio.ExportarRelatorioCsvUseCase
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
import java.io.OutputStream
import java.time.YearMonth
import javax.inject.Inject

data class RelatoriosUiState(
    val isLoading: Boolean = false,
    val mesSelecionado: YearMonth = YearMonth.now(),
    val empregoNome: String = "",
    val isExportando: Boolean = false
)

sealed interface RelatoriosAction {
    data class AlterarMes(val delta: Long) : RelatoriosAction
    data class ExportarPDF(val outputStream: OutputStream) : RelatoriosAction
    data class ExportarCSV(val outputStream: OutputStream) : RelatoriosAction
}

sealed interface RelatoriosEvent {
    data class MostrarMensagem(val mensagem: String) : RelatoriosEvent
    data object ExportacaoConcluida : RelatoriosEvent
}

@HiltViewModel
class RelatoriosViewModel @Inject constructor(
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val gerarRelatorioMensalUseCase: GerarRelatorioMensalUseCase,
    private val gerarEspelhoPontoPdfUseCase: GerarEspelhoPontoPdfUseCase,
    private val exportarRelatorioCsvUseCase: ExportarRelatorioCsvUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RelatoriosUiState())
    val uiState: StateFlow<RelatoriosUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<RelatoriosEvent>()
    val eventos: SharedFlow<RelatoriosEvent> = _eventos.asSharedFlow()

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch {
            val emprego = obterEmpregoAtivoUseCase.observar().first()
            _uiState.update { it.copy(empregoNome = emprego?.nomeExibicao ?: "Nenhum emprego") }
        }
    }

    fun onAction(action: RelatoriosAction) {
        when (action) {
            is RelatoriosAction.AlterarMes -> {
                _uiState.update { it.copy(mesSelecionado = it.mesSelecionado.plusMonths(action.delta)) }
            }
            is RelatoriosAction.ExportarPDF -> exportarPDF(action.outputStream)
            is RelatoriosAction.ExportarCSV -> exportarCSV(action.outputStream)
        }
    }

    private fun exportarPDF(outputStream: OutputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExportando = true) }
            
            try {
                val emprego = obterEmpregoAtivoUseCase.observar().first() ?: return@launch
                val relatorio = gerarRelatorioMensalUseCase(emprego.id, _uiState.value.mesSelecionado)
                
                val resultado = gerarEspelhoPontoPdfUseCase.execute(relatorio, emprego, outputStream)
                
                if (resultado.isSuccess) {
                    _eventos.emit(RelatoriosEvent.MostrarMensagem("PDF gerado com sucesso!"))
                    _eventos.emit(RelatoriosEvent.ExportacaoConcluida)
                } else {
                    _eventos.emit(RelatoriosEvent.MostrarMensagem("Erro ao gerar PDF: ${resultado.exceptionOrNull()?.message}"))
                }
            } catch (e: Exception) {
                _eventos.emit(RelatoriosEvent.MostrarMensagem("Erro inesperado: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isExportando = false) }
            }
        }
    }

    private fun exportarCSV(outputStream: OutputStream) {
        // TODO: Implementar exportação CSV similar ao PDF
        viewModelScope.launch {
            _eventos.emit(RelatoriosEvent.MostrarMensagem("Exportação CSV será implementada em breve."))
        }
    }
}
