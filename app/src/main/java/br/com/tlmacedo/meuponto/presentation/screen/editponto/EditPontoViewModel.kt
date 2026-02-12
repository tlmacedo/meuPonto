// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.usecase.ponto.AtualizarPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterPontoPorIdUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.RegistrarPontoUseCase
import br.com.tlmacedo.meuponto.presentation.navigation.Route
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
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel da tela de edição de ponto.
 *
 * Gerencia o estado do formulário de edição/criação de ponto,
 * incluindo carregamento de dados existentes e persistência.
 *
 * @property obterPontoPorIdUseCase Caso de uso para buscar ponto existente
 * @property atualizarPontoUseCase Caso de uso para atualizar ponto
 * @property registrarPontoUseCase Caso de uso para registrar novo ponto
 * @property savedStateHandle Handle para argumentos de navegação
 *
 * @author Thiago
 * @since 1.0.0
 */
@HiltViewModel
class EditPontoViewModel @Inject constructor(
    private val obterPontoPorIdUseCase: ObterPontoPorIdUseCase,
    private val atualizarPontoUseCase: AtualizarPontoUseCase,
    private val registrarPontoUseCase: RegistrarPontoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Estado da UI
    private val _uiState = MutableStateFlow(EditPontoUiState())
    val uiState: StateFlow<EditPontoUiState> = _uiState.asStateFlow()

    // Eventos únicos
    private val _uiEvent = MutableSharedFlow<EditPontoUiEvent>()
    val uiEvent: SharedFlow<EditPontoUiEvent> = _uiEvent.asSharedFlow()

    // ID do ponto sendo editado (0 = novo ponto)
    private val pontoId: Long = savedStateHandle[Route.ARG_PONTO_ID] ?: 0L

    init {
        if (pontoId > 0) {
            carregarPonto()
        }
    }

    /**
     * Processa as ações da tela.
     *
     * @param action Ação disparada pelo usuário
     */
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

    /**
     * Carrega o ponto existente para edição.
     */
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

    /**
     * Altera a data do ponto.
     */
    private fun alterarData(data: java.time.LocalDate) {
        _uiState.update { it.copy(data = data) }
    }

    /**
     * Altera a hora do ponto.
     */
    private fun alterarHora(hora: java.time.LocalTime) {
        _uiState.update { it.copy(hora = hora) }
    }

    /**
     * Altera o tipo do ponto.
     */
    private fun alterarTipo(tipo: br.com.tlmacedo.meuponto.domain.model.TipoPonto) {
        _uiState.update { it.copy(tipo = tipo) }
    }

    /**
     * Altera a observação do ponto.
     */
    private fun alterarObservacao(observacao: String) {
        _uiState.update { it.copy(observacao = observacao) }
    }

    /**
     * Salva o ponto (atualiza ou cria novo).
     */
    private fun salvar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val state = _uiState.value
            val dataHora = LocalDateTime.of(state.data, state.hora)
            val observacao = state.observacao.ifBlank { null }

            val resultado = if (state.isEditMode && state.pontoOriginal != null) {
                // Atualiza ponto existente
                val pontoAtualizado = state.pontoOriginal.copy(
                    dataHora = dataHora,
                    tipo = state.tipo,
                    observacao = observacao
                )
                atualizarPontoUseCase(pontoAtualizado)
            } else {
                // Cria novo ponto
                registrarPontoUseCase(
                    dataHora = dataHora,
                    tipo = state.tipo,
                    observacao = observacao
                )
            }

            resultado.fold(
                onSuccess = {
                    Timber.d("Ponto salvo com sucesso")
                    _uiEvent.emit(EditPontoUiEvent.ShowSnackbar("Ponto salvo com sucesso"))
                    _uiEvent.emit(EditPontoUiEvent.PontoSalvo)
                    _uiEvent.emit(EditPontoUiEvent.NavigateBack)
                },
                onFailure = { erro ->
                    Timber.e(erro, "Erro ao salvar ponto")
                    _uiState.update { it.copy(errorMessage = erro.message) }
                }
            )

            _uiState.update { it.copy(isSaving = false) }
        }
    }

    /**
     * Cancela a edição e volta à tela anterior.
     */
    private fun cancelar() {
        viewModelScope.launch {
            _uiEvent.emit(EditPontoUiEvent.NavigateBack)
        }
    }

    /**
     * Limpa a mensagem de erro.
     */
    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
