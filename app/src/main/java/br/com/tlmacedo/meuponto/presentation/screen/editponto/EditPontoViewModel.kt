package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.usecase.ponto.AtualizarPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterPontoPorIdUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.RegistrarPontoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel da tela de edição de ponto.
 *
 * Gerencia o estado do formulário de edição/criação de ponto,
 * incluindo carregamento de dados existentes e persistência.
 */
@HiltViewModel
class EditPontoViewModel @Inject constructor(
    private val obterPontoPorIdUseCase: ObterPontoPorIdUseCase,
    private val atualizarPontoUseCase: AtualizarPontoUseCase,
    private val registrarPontoUseCase: RegistrarPontoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPontoUiState())
    val uiState: StateFlow<EditPontoUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<EditPontoUiEvent>()
    val uiEvent: SharedFlow<EditPontoUiEvent> = _uiEvent.asSharedFlow()

    // ID do ponto da navegação (-1 significa novo ponto)
    private val pontoId: Long = savedStateHandle["pontoId"] ?: -1L
    
    // ID do emprego padrão (usa 1 como default, pode ser alterado para buscar emprego ativo)
    private val empregoId: Long = 1L

    init {
        if (pontoId > 0) {
            carregarPonto()
        }
    }

    fun onAction(action: EditPontoAction) {
        when (action) {
            is EditPontoAction.AlterarData -> alterarData(action.data)
            is EditPontoAction.AlterarHora -> alterarHora(action.hora)
            is EditPontoAction.AlterarTipo -> alterarTipo(action.tipo)
            is EditPontoAction.AlterarObservacao -> alterarObservacao(action.observacao)
            is EditPontoAction.Salvar -> salvar()
            is EditPontoAction.Cancelar -> cancelar()
            is EditPontoAction.LimparErro -> limparErro()
        }
    }

    private fun carregarPonto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val ponto = obterPontoPorIdUseCase(pontoId)

            if (ponto != null) {
                _uiState.update { state ->
                    state.copy(
                        pontoOriginal = ponto,
                        data = ponto.data,
                        hora = ponto.hora,
                        tipo = ponto.tipo,
                        observacao = ponto.observacao ?: "",
                        isLoading = false,
                        isEditMode = true
                    )
                }
                Timber.d("Ponto carregado para edição: $ponto")
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Ponto não encontrado") }
                Timber.e("Ponto não encontrado: $pontoId")
            }
        }
    }

    private fun alterarData(data: LocalDate) {
        _uiState.update { it.copy(data = data) }
    }

    private fun alterarHora(hora: LocalTime) {
        _uiState.update { it.copy(hora = hora) }
    }

    private fun alterarTipo(tipo: TipoPonto) {
        _uiState.update { it.copy(tipo = tipo) }
    }

    private fun alterarObservacao(observacao: String) {
        _uiState.update { it.copy(observacao = observacao) }
    }

    private fun salvar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val state = _uiState.value
            val dataHora = LocalDateTime.of(state.data, state.hora)
            val observacao = state.observacao.ifBlank { null }

            try {
                if (state.isEditMode && state.pontoOriginal != null) {
                    // Atualiza ponto existente
                    val pontoAtualizado = state.pontoOriginal.copy(
                        dataHora = dataHora,
                        tipo = state.tipo,
                        observacao = observacao
                    )
                    
                    val resultado = atualizarPontoUseCase(pontoAtualizado)
                    
                    resultado.fold(
                        onSuccess = {
                            Timber.d("Ponto atualizado com sucesso")
                            _uiEvent.emit(EditPontoUiEvent.ShowSnackbar("Ponto atualizado com sucesso"))
                            _uiEvent.emit(EditPontoUiEvent.PontoSalvo)
                            _uiEvent.emit(EditPontoUiEvent.NavigateBack)
                        },
                        onFailure = { erro ->
                            Timber.e(erro, "Erro ao atualizar ponto")
                            _uiState.update { it.copy(errorMessage = erro.message) }
                        }
                    )
                } else {
                    // Cria novo ponto usando Parametros
                    val parametros = RegistrarPontoUseCase.Parametros(
                        empregoId = empregoId,
                        dataHora = dataHora,
                        tipo = state.tipo,
                        observacao = observacao
                    )
                    
                    when (val resultado = registrarPontoUseCase(parametros)) {
                        is RegistrarPontoUseCase.Resultado.Sucesso -> {
                            Timber.d("Ponto registrado com sucesso: ${resultado.pontoId}")
                            _uiEvent.emit(EditPontoUiEvent.ShowSnackbar("Ponto registrado com sucesso"))
                            _uiEvent.emit(EditPontoUiEvent.PontoSalvo)
                            _uiEvent.emit(EditPontoUiEvent.NavigateBack)
                        }
                        is RegistrarPontoUseCase.Resultado.Erro -> {
                            Timber.e("Erro ao registrar ponto: ${resultado.mensagem}")
                            _uiState.update { it.copy(errorMessage = resultado.mensagem) }
                        }
                        is RegistrarPontoUseCase.Resultado.Validacao -> {
                            val mensagem = resultado.erros.joinToString("\n")
                            Timber.w("Validação falhou: $mensagem")
                            _uiState.update { it.copy(errorMessage = mensagem) }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro inesperado ao salvar ponto")
                _uiState.update { it.copy(errorMessage = "Erro inesperado: ${e.message}") }
            }

            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun cancelar() {
        viewModelScope.launch {
            _uiEvent.emit(EditPontoUiEvent.NavigateBack)
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
