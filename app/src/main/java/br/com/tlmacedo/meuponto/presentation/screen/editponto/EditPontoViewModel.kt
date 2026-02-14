// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.usecase.ponto.EditarPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterPontoUseCase
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
 * ViewModel da tela de edição/criação de ponto.
 *
 * Gerencia o estado do formulário de edição de ponto, incluindo carregamento
 * de dados existentes para edição e persistência de novos pontos ou alterações.
 * Suporta dois modos de operação: criação de novo ponto e edição de ponto existente.
 *
 * @property obterPontoUseCase Caso de uso para obter detalhes de um ponto
 * @property editarPontoUseCase Caso de uso para editar ponto existente (com validação)
 * @property registrarPontoUseCase Caso de uso para registrar novo ponto
 * @property savedStateHandle Handle para recuperar argumentos da navegação
 *
 * @author Thiago
 * @since 1.0.0
 */
@HiltViewModel
class EditPontoViewModel @Inject constructor(
    private val obterPontoUseCase: ObterPontoUseCase,
    private val editarPontoUseCase: EditarPontoUseCase,
    private val registrarPontoUseCase: RegistrarPontoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Estado da UI exposto como StateFlow imutável
    private val _uiState = MutableStateFlow(EditPontoUiState())
    val uiState: StateFlow<EditPontoUiState> = _uiState.asStateFlow()

    // Eventos únicos (navegação, snackbar, etc.) como SharedFlow
    private val _uiEvent = MutableSharedFlow<EditPontoUiEvent>()
    val uiEvent: SharedFlow<EditPontoUiEvent> = _uiEvent.asSharedFlow()

    // ID do ponto recuperado da navegação (-1 indica criação de novo ponto)
    private val pontoId: Long = savedStateHandle["pontoId"] ?: -1L
    
    // ID do emprego ativo (futuramente será obtido das configurações)
    private val empregoId: Long = 1L

    init {
        // Se foi passado um ID válido, carrega o ponto para edição
        if (pontoId > 0) {
            carregarPonto()
        }
    }

    /**
     * Processa as ações enviadas pela UI.
     *
     * @param action Ação a ser processada
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
     * Carrega os dados do ponto para edição.
     *
     * Utiliza o ObterPontoUseCase que retorna um sealed class com os
     * possíveis resultados (Sucesso ou NaoEncontrado).
     */
    private fun carregarPonto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Processa o resultado da busca do ponto
            when (val resultado = obterPontoUseCase(pontoId)) {
                is ObterPontoUseCase.Resultado.Sucesso -> {
                    val ponto = resultado.ponto
                    // Preenche o formulário com os dados do ponto
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
                }
                is ObterPontoUseCase.Resultado.NaoEncontrado -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Ponto não encontrado")
                    }
                    Timber.e("Ponto não encontrado: ${resultado.pontoId}")
                }
            }
        }
    }

    /**
     * Atualiza a data selecionada no formulário.
     *
     * @param data Nova data selecionada
     */
    private fun alterarData(data: LocalDate) {
        _uiState.update { it.copy(data = data) }
    }

    /**
     * Atualiza a hora selecionada no formulário.
     *
     * @param hora Nova hora selecionada
     */
    private fun alterarHora(hora: LocalTime) {
        _uiState.update { it.copy(hora = hora) }
    }

    /**
     * Atualiza o tipo de ponto selecionado no formulário.
     *
     * @param tipo Novo tipo de ponto (ENTRADA, SAIDA_ALMOCO, etc.)
     */
    private fun alterarTipo(tipo: TipoPonto) {
        _uiState.update { it.copy(tipo = tipo) }
    }

    /**
     * Atualiza a observação do ponto.
     *
     * @param observacao Texto da observação
     */
    private fun alterarObservacao(observacao: String) {
        _uiState.update { it.copy(observacao = observacao) }
    }

    /**
     * Salva o ponto (cria novo ou atualiza existente).
     *
     * Verifica se está em modo de edição para decidir qual operação executar.
     * Trata erros e emite eventos apropriados para a UI.
     */
    private fun salvar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val state = _uiState.value
            // Combina data e hora em um LocalDateTime
            val dataHora = LocalDateTime.of(state.data, state.hora)
            // Converte string vazia para null
            val observacao = state.observacao.ifBlank { null }

            try {
                if (state.isEditMode && state.pontoOriginal != null) {
                    // Modo edição: atualiza ponto existente
                    atualizarPontoExistente(
                        id = state.pontoOriginal.id,
                        dataHora = dataHora,
                        tipo = state.tipo,
                        observacao = observacao
                    )
                } else {
                    // Modo criação: registra novo ponto
                    criarNovoPonto(
                        dataHora = dataHora,
                        tipo = state.tipo,
                        observacao = observacao
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro inesperado ao salvar ponto")
                _uiState.update { it.copy(errorMessage = "Erro inesperado: ${e.message}") }
            }

            _uiState.update { it.copy(isSaving = false) }
        }
    }

    /**
     * Atualiza um ponto existente utilizando o EditarPontoUseCase.
     *
     * Este caso de uso realiza validações completas antes de persistir,
     * incluindo validação de sequência de pontos.
     *
     * @param id ID do ponto a ser atualizado
     * @param dataHora Nova data/hora do ponto
     * @param tipo Novo tipo do ponto
     * @param observacao Nova observação (pode ser null)
     */
    private suspend fun atualizarPontoExistente(
        id: Long,
        dataHora: LocalDateTime,
        tipo: TipoPonto,
        observacao: String?
    ) {
        // Monta os parâmetros para o caso de uso
        val parametros = EditarPontoUseCase.Parametros(
            pontoId = id,
            dataHora = dataHora,
            tipo = tipo,
            observacao = observacao
        )

        // Processa o resultado da edição
        when (val resultado = editarPontoUseCase(parametros)) {
            is EditarPontoUseCase.Resultado.Sucesso -> {
                Timber.d("Ponto atualizado com sucesso")
                _uiEvent.emit(EditPontoUiEvent.ShowSnackbar("Ponto atualizado com sucesso"))
                _uiEvent.emit(EditPontoUiEvent.PontoSalvo)
                _uiEvent.emit(EditPontoUiEvent.NavigateBack)
            }
            is EditarPontoUseCase.Resultado.Erro -> {
                Timber.e("Erro ao atualizar ponto: ${resultado.mensagem}")
                _uiState.update { it.copy(errorMessage = resultado.mensagem) }
            }
            is EditarPontoUseCase.Resultado.NaoEncontrado -> {
                Timber.e("Ponto não encontrado: ${resultado.pontoId}")
                _uiState.update { it.copy(errorMessage = "Ponto não encontrado") }
            }
            is EditarPontoUseCase.Resultado.Validacao -> {
                // Junta todas as mensagens de validação em uma única string
                val mensagem = resultado.erros.joinToString("\n")
                Timber.w("Validação falhou: $mensagem")
                _uiState.update { it.copy(errorMessage = mensagem) }
            }
        }
    }

    /**
     * Cria um novo ponto utilizando o RegistrarPontoUseCase.
     *
     * @param dataHora Data/hora do novo ponto
     * @param tipo Tipo do ponto (ENTRADA, SAIDA_ALMOCO, etc.)
     * @param observacao Observação opcional
     */
    private suspend fun criarNovoPonto(
        dataHora: LocalDateTime,
        tipo: TipoPonto,
        observacao: String?
    ) {
        // Monta os parâmetros para o caso de uso
        val parametros = RegistrarPontoUseCase.Parametros(
            empregoId = empregoId,
            dataHora = dataHora,
            tipo = tipo,
            observacao = observacao
        )

        // Processa o resultado do registro
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
                // Junta todas as mensagens de validação em uma única string
                val mensagem = resultado.erros.joinToString("\n")
                Timber.w("Validação falhou: $mensagem")
                _uiState.update { it.copy(errorMessage = mensagem) }
            }
        }
    }

    /**
     * Cancela a operação e navega de volta.
     */
    private fun cancelar() {
        viewModelScope.launch {
            _uiEvent.emit(EditPontoUiEvent.NavigateBack)
        }
    }

    /**
     * Limpa a mensagem de erro do estado.
     */
    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
