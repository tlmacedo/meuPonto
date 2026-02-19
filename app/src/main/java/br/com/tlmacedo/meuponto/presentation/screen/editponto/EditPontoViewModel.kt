// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.EditarPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ExcluirPontoUseCase
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
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
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel da tela de edição de ponto.
 *
 * @author Thiago
 * @since 3.5.0
 */
@HiltViewModel
class EditPontoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pontoRepository: PontoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val editarPontoUseCase: EditarPontoUseCase,
    private val excluirPontoUseCase: ExcluirPontoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPontoUiState())
    val uiState: StateFlow<EditPontoUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<EditPontoUiEvent>()
    val uiEvent: SharedFlow<EditPontoUiEvent> = _uiEvent.asSharedFlow()

    private val pontoId: Long = savedStateHandle.get<Long>(MeuPontoDestinations.ARG_PONTO_ID) ?: -1L

    init {
        if (pontoId > 0) {
            carregarPonto()
        }
    }

    fun onAction(action: EditPontoAction) {
        when (action) {
            // Campos
            is EditPontoAction.AtualizarData -> atualizarData(action.data)
            is EditPontoAction.AtualizarHora -> atualizarHora(action.hora)
            is EditPontoAction.AtualizarNsr -> atualizarNsr(action.nsr)
            is EditPontoAction.AtualizarLocalizacao -> atualizarLocalizacao(
                action.latitude, action.longitude, action.endereco
            )
            is EditPontoAction.AtualizarObservacao -> atualizarObservacao(action.observacao)
            is EditPontoAction.AtualizarMotivo -> atualizarMotivo(action.motivo)

            // Dialogs
            EditPontoAction.AbrirTimePicker -> _uiState.update { it.copy(showTimePicker = true) }
            EditPontoAction.FecharTimePicker -> _uiState.update { it.copy(showTimePicker = false) }
            EditPontoAction.AbrirDatePicker -> _uiState.update { it.copy(showDatePicker = true) }
            EditPontoAction.FecharDatePicker -> _uiState.update { it.copy(showDatePicker = false) }
            EditPontoAction.AbrirLocationPicker -> _uiState.update { it.copy(showLocationPicker = true) }
            EditPontoAction.FecharLocationPicker -> _uiState.update { it.copy(showLocationPicker = false) }
            EditPontoAction.CapturarLocalizacao -> capturarLocalizacao()
            EditPontoAction.LimparLocalizacao -> limparLocalizacao()

            // Ações principais
            EditPontoAction.Salvar -> salvar()
            EditPontoAction.SolicitarExclusao -> _uiState.update { it.copy(showDeleteConfirmDialog = true) }
            EditPontoAction.ConfirmarExclusao -> confirmarExclusao()
            EditPontoAction.CancelarExclusao -> _uiState.update { it.copy(showDeleteConfirmDialog = false) }
            EditPontoAction.Cancelar -> cancelar()
            EditPontoAction.LimparErro -> _uiState.update { it.copy(erro = null) }
        }
    }

    // ========================================================================
    // Carregamento
    // ========================================================================

    private fun carregarPonto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val ponto = pontoRepository.buscarPorId(pontoId)
                if (ponto == null) {
                    _uiState.update { it.copy(isLoading = false, erro = "Ponto não encontrado") }
                    return@launch
                }

                // Buscar índice do ponto para determinar tipo
                val pontosDoDia = pontoRepository.buscarPorEmpregoEData(
                    ponto.empregoId,
                    ponto.data
                ).sortedBy { it.dataHora }
                val indice = pontosDoDia.indexOfFirst { it.id == ponto.id }
                val tipoPonto = TipoPonto.getTipoPorIndice(indice)

                // Buscar configuração do emprego
                val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(ponto.empregoId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pontoId = ponto.id,
                        pontoOriginal = ponto,
                        empregoId = ponto.empregoId,
                        tipoPonto = tipoPonto,
                        indice = indice,
                        data = ponto.data,
                        hora = ponto.hora,
                        nsr = ponto.nsr ?: "",
                        latitude = ponto.latitude,
                        longitude = ponto.longitude,
                        endereco = ponto.endereco ?: "",
                        observacao = ponto.observacao ?: "",
                        configuracao = configuracao
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, erro = "Erro ao carregar ponto: ${e.message}")
                }
            }
        }
    }

    // ========================================================================
    // Atualizações de campos
    // ========================================================================

    private fun atualizarData(data: LocalDate) {
        _uiState.update { it.copy(data = data, showDatePicker = false) }
    }

    private fun atualizarHora(hora: LocalTime) {
        _uiState.update { it.copy(hora = hora, showTimePicker = false) }
    }

    private fun atualizarNsr(nsr: String) {
        val state = _uiState.value
        val nsrFiltrado = when (state.tipoNsr) {
            br.com.tlmacedo.meuponto.domain.model.TipoNsr.NUMERICO -> nsr.filter { it.isDigit() }
            br.com.tlmacedo.meuponto.domain.model.TipoNsr.ALFANUMERICO -> nsr.filter { it.isLetterOrDigit() }.uppercase()
        }
        _uiState.update { it.copy(nsr = nsrFiltrado) }
    }

    private fun atualizarLocalizacao(latitude: Double, longitude: Double, endereco: String?) {
        _uiState.update {
            it.copy(
                latitude = latitude,
                longitude = longitude,
                endereco = endereco ?: "",
                showLocationPicker = false
            )
        }
    }

    private fun atualizarObservacao(observacao: String) {
        _uiState.update { it.copy(observacao = observacao) }
    }

    private fun atualizarMotivo(motivo: String) {
        _uiState.update { it.copy(motivo = motivo) }
    }

    private fun capturarLocalizacao() {
        // TODO: Implementar captura de localização via GPS com LocationService
        _uiState.update {
            it.copy(
                showLocationPicker = false,
                erro = "Captura de localização será implementada em breve"
            )
        }
    }

    private fun limparLocalizacao() {
        _uiState.update {
            it.copy(latitude = null, longitude = null, endereco = "")
        }
    }

    // ========================================================================
    // Ações principais
    // ========================================================================

    private fun salvar() {
        val state = _uiState.value

        if (!state.podeSalvar) {
            val erros = listOfNotNull(state.erroMotivo, state.erroNsr, state.erroLocalizacao)
            _uiState.update { it.copy(erro = erros.joinToString("\n")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val parametros = EditarPontoUseCase.Parametros(
                pontoId = state.pontoId,
                dataHora = state.dataHora,
                nsr = state.nsr.ifBlank { null },
                latitude = state.latitude,
                longitude = state.longitude,
                endereco = state.endereco.ifBlank { null },
                observacao = state.observacao.ifBlank { null },
                motivo = state.motivo
            )

            when (val resultado = editarPontoUseCase(parametros)) {
                is EditarPontoUseCase.Resultado.Sucesso -> {
                    _uiEvent.emit(EditPontoUiEvent.Salvo("Ponto atualizado com sucesso"))
                    _uiEvent.emit(EditPontoUiEvent.Voltar)
                }
                is EditarPontoUseCase.Resultado.Erro -> {
                    _uiState.update { it.copy(isSaving = false, erro = resultado.mensagem) }
                }
                is EditarPontoUseCase.Resultado.NaoEncontrado -> {
                    _uiState.update { it.copy(isSaving = false, erro = "Ponto não encontrado") }
                }
                is EditarPontoUseCase.Resultado.Validacao -> {
                    _uiState.update { it.copy(isSaving = false, erro = resultado.erros.joinToString("\n")) }
                }
            }
        }
    }

    private fun confirmarExclusao() {
        val state = _uiState.value

        if (state.motivo.isBlank()) {
            _uiState.update {
                it.copy(
                    showDeleteConfirmDialog = false,
                    erro = "Informe o motivo da exclusão"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showDeleteConfirmDialog = false) }

            val parametros = ExcluirPontoUseCase.Parametros(
                pontoId = state.pontoId,
                motivo = state.motivo
            )

            when (val resultado = excluirPontoUseCase(parametros)) {
                is ExcluirPontoUseCase.Resultado.Sucesso -> {
                    _uiEvent.emit(EditPontoUiEvent.Excluido("Ponto excluído com sucesso"))
                    _uiEvent.emit(EditPontoUiEvent.Voltar)
                }
                is ExcluirPontoUseCase.Resultado.Erro -> {
                    _uiState.update { it.copy(isSaving = false, erro = resultado.mensagem) }
                }
                is ExcluirPontoUseCase.Resultado.NaoEncontrado -> {
                    _uiState.update { it.copy(isSaving = false, erro = "Ponto não encontrado") }
                }
                is ExcluirPontoUseCase.Resultado.Validacao -> {
                    _uiState.update { it.copy(isSaving = false, erro = resultado.erros.joinToString("\n")) }
                }
            }
        }
    }

    private fun cancelar() {
        viewModelScope.launch {
            _uiEvent.emit(EditPontoUiEvent.Voltar)
        }
    }
}
