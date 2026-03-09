package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
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
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel da tela de edição de versão de jornada.
 *
 * Recebe tanto empregoId quanto versaoId via navegação para garantir
 * consistência e permitir criação de novas versões diretamente.
 *
 * @author Thiago
 * @since 4.0.0
 */
@HiltViewModel
class EditarVersaoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) : ViewModel() {

    private val empregoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val versaoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_VERSAO_ID) ?: -1L

    private val _uiState = MutableStateFlow(EditarVersaoUiState())
    val uiState: StateFlow<EditarVersaoUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<EditarVersaoEvent>()
    val eventos: SharedFlow<EditarVersaoEvent> = _eventos.asSharedFlow()

    init {
        validarArgumentosECarregar()
    }

    private fun validarArgumentosECarregar() {
        when {
            empregoId <= 0L -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Emprego inválido"
                    )
                }
            }
            versaoId > 0L -> {
                carregarVersao(versaoId)
            }
            else -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNovaVersao = true,
                        empregoId = empregoId
                    )
                }
            }
        }
    }

    fun onAction(action: EditarVersaoAction) {
        when (action) {
            is EditarVersaoAction.AlterarDescricao -> alterarDescricao(action.descricao)
            is EditarVersaoAction.AlterarDataInicio -> alterarDataInicio(action.data)
            is EditarVersaoAction.AlterarDataFim -> alterarDataFim(action.data)
            is EditarVersaoAction.AlterarJornadaMaxima -> alterarJornadaMaxima(action.minutos)
            is EditarVersaoAction.AlterarIntervaloInterjornada -> alterarIntervaloInterjornada(action.minutos)
            is EditarVersaoAction.AlterarToleranciaIntervalo -> alterarToleranciaIntervalo(action.minutos)
            is EditarVersaoAction.MostrarDataInicioPicker -> mostrarDataInicioPicker(action.mostrar)
            is EditarVersaoAction.MostrarDataFimPicker -> mostrarDataFimPicker(action.mostrar)
            is EditarVersaoAction.ToggleSecao -> toggleSecao(action.secao)
            is EditarVersaoAction.Salvar -> salvar()
            is EditarVersaoAction.Cancelar -> cancelar()
            is EditarVersaoAction.LimparErro -> limparErro()
            is EditarVersaoAction.ConfigurarHorarios -> configurarHorarios()
        }
    }

    private fun carregarVersao(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val versao = versaoJornadaRepository.buscarPorId(id)

                if (versao != null) {
                    if (versao.empregoId != empregoId) {
                        Timber.w(
                            "Versão %d pertence ao emprego %d, mas foi solicitada para emprego %d",
                            id, versao.empregoId, empregoId
                        )
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Versão não pertence a este emprego"
                            )
                        }
                        return@launch
                    }

                    val horarios = horarioDiaSemanaRepository.buscarPorVersaoJornada(id)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNovaVersao = false,
                            empregoId = versao.empregoId,
                            versaoId = versao.id,
                            descricao = versao.descricao ?: "",
                            dataInicio = versao.dataInicio,
                            dataFim = versao.dataFim,
                            numeroVersao = versao.numeroVersao,
                            vigente = versao.vigente,
                            jornadaMaximaDiariaMinutos = versao.jornadaMaximaDiariaMinutos,
                            intervaloMinimoInterjornadaMinutos = versao.intervaloMinimoInterjornadaMinutos,
                            toleranciaIntervaloMaisMinutos = versao.toleranciaIntervaloMaisMinutos,
                            horarios = horarios
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _eventos.emit(EditarVersaoEvent.MostrarMensagem("Versão não encontrada"))
                    _eventos.emit(EditarVersaoEvent.Voltar)
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar versão %d", id)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao carregar versão"
                    )
                }
            }
        }
    }

    private fun alterarDescricao(descricao: String) {
        _uiState.update { it.copy(descricao = descricao) }
    }

    private fun alterarDataInicio(data: java.time.LocalDate) {
        _uiState.update { it.copy(dataInicio = data, showDataInicioPicker = false) }
    }

    private fun alterarDataFim(data: java.time.LocalDate?) {
        _uiState.update { it.copy(dataFim = data, showDataFimPicker = false) }
    }

    private fun alterarJornadaMaxima(minutos: Int) {
        _uiState.update { it.copy(jornadaMaximaDiariaMinutos = minutos) }
    }

    private fun alterarIntervaloInterjornada(minutos: Int) {
        _uiState.update { it.copy(intervaloMinimoInterjornadaMinutos = minutos) }
    }

    private fun alterarToleranciaIntervalo(minutos: Int) {
        _uiState.update { it.copy(toleranciaIntervaloMaisMinutos = minutos) }
    }

    private fun mostrarDataInicioPicker(mostrar: Boolean) {
        _uiState.update { it.copy(showDataInicioPicker = mostrar) }
    }

    private fun mostrarDataFimPicker(mostrar: Boolean) {
        _uiState.update { it.copy(showDataFimPicker = mostrar) }
    }

    private fun toggleSecao(secao: SecaoVersao) {
        _uiState.update { state ->
            state.copy(
                secaoExpandida = if (state.secaoExpandida == secao) null else secao
            )
        }
    }

    private fun salvar() {
        val state = _uiState.value

        if (state.empregoId <= 0L) {
            viewModelScope.launch {
                _eventos.emit(EditarVersaoEvent.MostrarMensagem("Emprego inválido"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                if (state.isNovaVersao) {
                    _eventos.emit(
                        EditarVersaoEvent.MostrarMensagem(
                            "Use a lista de versões para criar nova versão"
                        )
                    )
                    return@launch
                }

                val versaoOriginal = versaoJornadaRepository.buscarPorId(state.versaoId!!)
                if (versaoOriginal == null) {
                    _eventos.emit(EditarVersaoEvent.MostrarMensagem("Versão não encontrada"))
                    return@launch
                }

                val versaoAtualizada = versaoOriginal.copy(
                    descricao = state.descricao.ifBlank { null },
                    dataInicio = state.dataInicio,
                    dataFim = state.dataFim,
                    jornadaMaximaDiariaMinutos = state.jornadaMaximaDiariaMinutos,
                    intervaloMinimoInterjornadaMinutos = state.intervaloMinimoInterjornadaMinutos,
                    toleranciaIntervaloMaisMinutos = state.toleranciaIntervaloMaisMinutos,
                    atualizadoEm = LocalDateTime.now()
                )

                versaoJornadaRepository.atualizar(versaoAtualizada)
                _eventos.emit(EditarVersaoEvent.MostrarMensagem("Versão atualizada com sucesso"))
                _eventos.emit(EditarVersaoEvent.SalvoComSucesso)
            } catch (e: Exception) {
                Timber.e(e, "Erro ao salvar versão")
                _eventos.emit(
                    EditarVersaoEvent.MostrarMensagem("Erro ao salvar: ${e.message}")
                )
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun cancelar() {
        viewModelScope.launch {
            _eventos.emit(EditarVersaoEvent.Voltar)
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun configurarHorarios() {
        val versaoId = _uiState.value.versaoId ?: return

        viewModelScope.launch {
            _eventos.emit(EditarVersaoEvent.NavegarParaHorarios(versaoId))
        }
    }
}
