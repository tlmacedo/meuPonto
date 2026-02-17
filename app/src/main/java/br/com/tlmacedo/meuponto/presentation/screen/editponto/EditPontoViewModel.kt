// Arquivo: EditPontoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.usecase.ponto.EditarPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.RegistrarPontoUseCase
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel da tela de edição de ponto.
 *
 * O tipo do ponto não pode ser alterado pois é determinado pela posição.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Tipo calculado por posição (não editável)
 */
@HiltViewModel
class EditPontoViewModel @Inject constructor(
    private val obterPontoUseCase: ObterPontoUseCase,
    private val editarPontoUseCase: EditarPontoUseCase,
    private val registrarPontoUseCase: RegistrarPontoUseCase,
    private val pontoRepository: PontoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPontoUiState())
    val uiState: StateFlow<EditPontoUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<EditPontoUiEvent>()
    val uiEvent: SharedFlow<EditPontoUiEvent> = _uiEvent.asSharedFlow()

    private val pontoId: Long = savedStateHandle["pontoId"] ?: -1L
    private val empregoId: Long = 1L

    init {
        if (pontoId > 0) carregarPonto()
    }

    fun onAction(action: EditPontoAction) {
        when (action) {
            is EditPontoAction.AlterarData -> _uiState.update { it.copy(data = action.data) }
            is EditPontoAction.AlterarHora -> _uiState.update { it.copy(hora = action.hora) }
            is EditPontoAction.AlterarObservacao -> _uiState.update { it.copy(observacao = action.observacao) }
            is EditPontoAction.Salvar -> salvar()
            is EditPontoAction.Cancelar -> viewModelScope.launch { _uiEvent.emit(EditPontoUiEvent.NavigateBack) }
            is EditPontoAction.LimparErro -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun carregarPonto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val resultado = obterPontoUseCase(pontoId)) {
                is ObterPontoUseCase.Resultado.Sucesso -> {
                    val ponto = resultado.ponto
                    
                    // Calcula o índice do ponto na lista ordenada
                    val pontosDoDia = pontoRepository.buscarPorEmpregoEData(ponto.empregoId, ponto.data)
                    val pontosOrdenados = pontosDoDia.sortedBy { it.dataHora }
                    val indice = pontosOrdenados.indexOfFirst { it.id == ponto.id }
                    
                    _uiState.update {
                        it.copy(
                            pontoOriginal = ponto,
                            indice = if (indice >= 0) indice else 0,
                            data = ponto.data,
                            hora = ponto.hora,
                            observacao = ponto.observacao ?: "",
                            isLoading = false,
                            isEditMode = true
                        )
                    }
                }
                is ObterPontoUseCase.Resultado.NaoEncontrado -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Ponto não encontrado") }
                }
            }
        }
    }

    private fun salvar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val dataHora = LocalDateTime.of(state.data, state.hora)
            val observacao = state.observacao.ifBlank { null }

            if (state.isEditMode && state.pontoOriginal != null) {
                atualizarPonto(state.pontoOriginal.id, dataHora, observacao)
            } else {
                criarPonto(dataHora, observacao)
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private suspend fun atualizarPonto(id: Long, dataHora: LocalDateTime, obs: String?) {
        val params = EditarPontoUseCase.Parametros(id, dataHora, obs)
        when (val r = editarPontoUseCase(params)) {
            is EditarPontoUseCase.Resultado.Sucesso -> {
                _uiEvent.emit(EditPontoUiEvent.ShowSnackbar("Ponto atualizado"))
                _uiEvent.emit(EditPontoUiEvent.PontoSalvo)
                _uiEvent.emit(EditPontoUiEvent.NavigateBack)
            }
            is EditarPontoUseCase.Resultado.Erro -> _uiState.update { it.copy(errorMessage = r.mensagem) }
            is EditarPontoUseCase.Resultado.NaoEncontrado -> _uiState.update { it.copy(errorMessage = "Ponto não encontrado") }
            is EditarPontoUseCase.Resultado.Validacao -> _uiState.update { it.copy(errorMessage = r.erros.joinToString("\n")) }
        }
    }

    private suspend fun criarPonto(dataHora: LocalDateTime, obs: String?) {
        val params = RegistrarPontoUseCase.Parametros(empregoId, dataHora, obs)
        when (val r = registrarPontoUseCase(params)) {
            is RegistrarPontoUseCase.Resultado.Sucesso -> {
                _uiEvent.emit(EditPontoUiEvent.ShowSnackbar("Ponto registrado"))
                _uiEvent.emit(EditPontoUiEvent.PontoSalvo)
                _uiEvent.emit(EditPontoUiEvent.NavigateBack)
            }
            is RegistrarPontoUseCase.Resultado.Erro -> _uiState.update { it.copy(errorMessage = r.mensagem) }
            is RegistrarPontoUseCase.Resultado.Validacao -> _uiState.update { it.copy(errorMessage = r.erros.joinToString("\n")) }
            is RegistrarPontoUseCase.Resultado.SemEmpregoAtivo -> _uiState.update { it.copy(errorMessage = "Nenhum emprego ativo") }
            is RegistrarPontoUseCase.Resultado.HorarioInvalido -> _uiState.update { it.copy(errorMessage = r.motivo) }
            is RegistrarPontoUseCase.Resultado.LimiteAtingido -> _uiState.update { it.copy(errorMessage = "Limite de ${r.limite} pontos") }
        }
    }
}
