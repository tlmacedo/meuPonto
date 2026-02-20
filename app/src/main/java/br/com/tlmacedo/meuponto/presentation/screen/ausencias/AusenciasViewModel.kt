// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciasViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ExcluirAusenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ListarAusenciasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ResultadoExcluirAusencia
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

/**
 * ViewModel da tela de listagem de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
@HiltViewModel
class AusenciasViewModel @Inject constructor(
    private val listarAusenciasUseCase: ListarAusenciasUseCase,
    private val excluirAusenciaUseCase: ExcluirAusenciaUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AusenciasUiState())
    val uiState: StateFlow<AusenciasUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AusenciasUiEvent>()
    val uiEvent: SharedFlow<AusenciasUiEvent> = _uiEvent.asSharedFlow()

    private var ausenciasJob: Job? = null

    init {
        carregarEmpregoAtivo()
    }

    fun onAction(action: AusenciasAction) {
        when (action) {
            // Navegação por mês
            is AusenciasAction.MesAnterior -> navegarMesAnterior()
            is AusenciasAction.ProximoMes -> navegarProximoMes()
            is AusenciasAction.SelecionarMes -> selecionarMes(action.mes)

            // Filtro
            is AusenciasAction.FiltrarPorTipo -> filtrarPorTipo(action.tipo)

            // CRUD
            is AusenciasAction.NovaAusencia -> {
                viewModelScope.launch {
                    _uiEvent.emit(AusenciasUiEvent.NavegarParaNovaAusencia)
                }
            }
            is AusenciasAction.EditarAusencia -> {
                viewModelScope.launch {
                    _uiEvent.emit(AusenciasUiEvent.NavegarParaEditarAusencia(action.ausencia.id))
                }
            }
            is AusenciasAction.SolicitarExclusao -> solicitarExclusao(action.ausencia)
            is AusenciasAction.ConfirmarExclusao -> confirmarExclusao()
            is AusenciasAction.CancelarExclusao -> cancelarExclusao()

            // Geral
            is AusenciasAction.LimparErro -> limparErro()
            is AusenciasAction.Voltar -> {
                viewModelScope.launch {
                    _uiEvent.emit(AusenciasUiEvent.Voltar)
                }
            }
        }
    }

    private fun carregarEmpregoAtivo() {
        viewModelScope.launch {
            // Usa o método observar() que retorna Flow<Emprego?>
            obterEmpregoAtivoUseCase.observar().collect { emprego ->
                _uiState.update { it.copy(empregoAtivo = emprego) }
                if (emprego != null) {
                    carregarAusencias()
                }
            }
        }
    }

    private fun carregarAusencias() {
        ausenciasJob?.cancel()

        val empregoId = _uiState.value.empregoAtivo?.id ?: return
        val mes = _uiState.value.mesSelecionado

        ausenciasJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                listarAusenciasUseCase.observarPorMes(empregoId, mes).collect { ausencias ->
                    _uiState.update {
                        it.copy(
                            ausencias = ausencias.sortedBy { a -> a.dataInicio },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar ausências: ${e.message}"
                    )
                }
            }
        }
    }

    private fun navegarMesAnterior() {
        if (_uiState.value.podeNavegaMesAnterior) {
            val novoMes = _uiState.value.mesSelecionado.minusMonths(1)
            selecionarMes(novoMes)
        }
    }

    private fun navegarProximoMes() {
        if (_uiState.value.podeNavegarMesProximo) {
            val novoMes = _uiState.value.mesSelecionado.plusMonths(1)
            selecionarMes(novoMes)
        }
    }

    private fun selecionarMes(mes: YearMonth) {
        _uiState.update { it.copy(mesSelecionado = mes) }
        carregarAusencias()
    }

    private fun filtrarPorTipo(tipo: TipoAusencia?) {
        _uiState.update { it.copy(filtroTipo = tipo) }
    }

    private fun solicitarExclusao(ausencia: Ausencia) {
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                ausenciaParaExcluir = ausencia
            )
        }
    }

    private fun cancelarExclusao() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false,
                ausenciaParaExcluir = null
            )
        }
    }

    private fun confirmarExclusao() {
        val ausencia = _uiState.value.ausenciaParaExcluir ?: return

        viewModelScope.launch {
            when (val resultado = excluirAusenciaUseCase(ausencia)) {
                is ResultadoExcluirAusencia.Sucesso -> {
                    _uiEvent.emit(AusenciasUiEvent.MostrarMensagem("Ausência excluída"))
                }
                is ResultadoExcluirAusencia.Erro -> {
                    _uiEvent.emit(AusenciasUiEvent.MostrarErro(resultado.mensagem))
                }
            }
            cancelarExclusao()
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    fun recarregar() {
        carregarAusencias()
    }
}
