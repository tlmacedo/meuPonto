// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciaFormViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.AtualizarAusenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.CriarAusenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ResultadoAtualizarAusencia
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ResultadoCriarAusencia
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel do formulário de ausência.
 *
 * @author Thiago
 * @since 4.0.0
 */
@HiltViewModel
class AusenciaFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ausenciaRepository: AusenciaRepository,
    private val criarAusenciaUseCase: CriarAusenciaUseCase,
    private val atualizarAusenciaUseCase: AtualizarAusenciaUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val ausenciaId: Long = savedStateHandle.get<Long>("ausenciaId") ?: 0L
    private val tipoInicial: String? = savedStateHandle.get<String>("tipo")
    private val dataInicial: String? = savedStateHandle.get<String>("data")

    private val _uiState = MutableStateFlow(AusenciaFormUiState())
    val uiState: StateFlow<AusenciaFormUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AusenciaFormUiEvent>()
    val uiEvent: SharedFlow<AusenciaFormUiEvent> = _uiEvent.asSharedFlow()

    init {
        inicializar()
    }

    private fun inicializar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Obter emprego ativo usando o método suspend
            val empregoId = when (val resultado = obterEmpregoAtivoUseCase()) {
                is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> resultado.emprego.id
                else -> 0L
            }

            if (ausenciaId > 0) {
                // Modo edição
                carregarAusencia(ausenciaId)
            } else {
                // Modo criação
                val tipo = tipoInicial?.let {
                    runCatching { TipoAusencia.valueOf(it) }.getOrNull()
                } ?: TipoAusencia.FERIAS

                val data = dataInicial?.let {
                    runCatching { LocalDate.parse(it) }.getOrNull()
                } ?: LocalDate.now()

                _uiState.update {
                    it.copy(
                        empregoId = empregoId,
                        tipo = tipo,
                        dataInicio = data,
                        dataFim = data,
                        isEdicao = false,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun carregarAusencia(id: Long) {
        val ausencia = ausenciaRepository.buscarPorId(id)

        if (ausencia != null) {
            _uiState.update {
                it.copy(
                    id = ausencia.id,
                    empregoId = ausencia.empregoId,
                    tipo = ausencia.tipo,
                    dataInicio = ausencia.dataInicio,
                    dataFim = ausencia.dataFim,
                    descricao = ausencia.descricao ?: "",
                    observacao = ausencia.observacao ?: "",
                    isEdicao = true,
                    isLoading = false
                )
            }
        } else {
            _uiState.update { it.copy(isLoading = false, erro = "Ausência não encontrada") }
        }
    }

    fun onAction(action: AusenciaFormAction) {
        when (action) {
            // Campos
            is AusenciaFormAction.SelecionarTipo -> selecionarTipo(action.tipo)
            is AusenciaFormAction.SelecionarDataInicio -> selecionarDataInicio(action.data)
            is AusenciaFormAction.SelecionarDataFim -> selecionarDataFim(action.data)
            is AusenciaFormAction.AtualizarDescricao -> atualizarDescricao(action.descricao)
            is AusenciaFormAction.AtualizarObservacao -> atualizarObservacao(action.observacao)

            // Dialogs
            is AusenciaFormAction.AbrirDatePickerInicio -> {
                _uiState.update { it.copy(showDatePickerInicio = true) }
            }
            is AusenciaFormAction.FecharDatePickerInicio -> {
                _uiState.update { it.copy(showDatePickerInicio = false) }
            }
            is AusenciaFormAction.AbrirDatePickerFim -> {
                _uiState.update { it.copy(showDatePickerFim = true) }
            }
            is AusenciaFormAction.FecharDatePickerFim -> {
                _uiState.update { it.copy(showDatePickerFim = false) }
            }
            is AusenciaFormAction.AbrirTipoSelector -> {
                _uiState.update { it.copy(showTipoSelector = true) }
            }
            is AusenciaFormAction.FecharTipoSelector -> {
                _uiState.update { it.copy(showTipoSelector = false) }
            }

            // Ações principais
            is AusenciaFormAction.Salvar -> salvar()
            is AusenciaFormAction.Cancelar -> {
                viewModelScope.launch {
                    _uiEvent.emit(AusenciaFormUiEvent.Voltar)
                }
            }
            is AusenciaFormAction.LimparErro -> {
                _uiState.update { it.copy(erro = null) }
            }
        }
    }

    private fun selecionarTipo(tipo: TipoAusencia) {
        _uiState.update {
            it.copy(
                tipo = tipo,
                descricao = if (it.descricao.isBlank()) tipo.descricao else it.descricao,
                showTipoSelector = false
            )
        }
    }

    private fun selecionarDataInicio(data: LocalDate) {
        _uiState.update { state ->
            val novaDataFim = if (state.dataFim < data) data else state.dataFim
            state.copy(
                dataInicio = data,
                dataFim = novaDataFim,
                showDatePickerInicio = false
            )
        }
    }

    private fun selecionarDataFim(data: LocalDate) {
        _uiState.update { state ->
            val dataFimValida = if (data < state.dataInicio) state.dataInicio else data
            state.copy(
                dataFim = dataFimValida,
                showDatePickerFim = false
            )
        }
    }

    private fun atualizarDescricao(descricao: String) {
        _uiState.update { it.copy(descricao = descricao) }
    }

    private fun atualizarObservacao(observacao: String) {
        _uiState.update { it.copy(observacao = observacao) }
    }

    private fun salvar() {
        val state = _uiState.value

        if (!state.isFormValido) {
            _uiState.update { it.copy(erro = "Preencha todos os campos obrigatórios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSalvando = true) }

            val ausencia = state.toAusencia()

            val resultado = if (state.isEdicao) {
                atualizarAusenciaUseCase(ausencia)
            } else {
                criarAusenciaUseCase(ausencia)
            }

            when (resultado) {
                is ResultadoCriarAusencia.Sucesso -> {
                    _uiEvent.emit(AusenciaFormUiEvent.MostrarMensagem("Ausência salva com sucesso"))
                    _uiEvent.emit(AusenciaFormUiEvent.SalvoComSucesso)
                }
                is ResultadoCriarAusencia.Erro -> {
                    _uiState.update { it.copy(erro = resultado.mensagem, isSalvando = false) }
                }
                is ResultadoAtualizarAusencia.Sucesso -> {
                    _uiEvent.emit(AusenciaFormUiEvent.MostrarMensagem("Ausência atualizada"))
                    _uiEvent.emit(AusenciaFormUiEvent.SalvoComSucesso)
                }
                is ResultadoAtualizarAusencia.Erro -> {
                    _uiState.update { it.copy(erro = resultado.mensagem, isSalvando = false) }
                }
            }
        }
    }
}
