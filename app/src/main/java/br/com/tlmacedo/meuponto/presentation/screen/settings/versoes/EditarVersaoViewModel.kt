// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/versoes/EditarVersaoViewModel.kt
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
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel da tela de edição de versão de jornada.
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

    private val versaoId: Long = savedStateHandle.get<Long>(MeuPontoDestinations.ARG_VERSAO_ID) ?: -1L

    private val _uiState = MutableStateFlow(EditarVersaoUiState())
    val uiState: StateFlow<EditarVersaoUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<EditarVersaoEvent>()
    val eventos: SharedFlow<EditarVersaoEvent> = _eventos.asSharedFlow()

    init {
        if (versaoId > 0) {
            carregarVersao(versaoId)
        } else {
            _uiState.update { it.copy(isLoading = false, isNovaVersao = true) }
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
                    val horarios = horarioDiaSemanaRepository.buscarPorVersaoJornada(id)
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNovaVersao = false,
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
                Timber.e(e, "Erro ao carregar versão")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun alterarDescricao(descricao: String) {
        _uiState.update { it.copy(descricao = descricao) }
    }

    private fun alterarDataInicio(data: LocalDate) {
        _uiState.update { it.copy(dataInicio = data, showDataInicioPicker = false) }
    }

    private fun alterarDataFim(data: LocalDate?) {
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

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val versaoAtualizada = VersaoJornada(
                    id = state.versaoId ?: 0,
                    empregoId = 0, // Será preenchido pelo repositório
                    dataInicio = state.dataInicio,
                    dataFim = state.dataFim,
                    descricao = state.descricao.ifBlank { null },
                    numeroVersao = state.numeroVersao,
                    vigente = state.vigente,
                    jornadaMaximaDiariaMinutos = state.jornadaMaximaDiariaMinutos,
                    intervaloMinimoInterjornadaMinutos = state.intervaloMinimoInterjornadaMinutos,
                    toleranciaIntervaloMaisMinutos = state.toleranciaIntervaloMaisMinutos,
                    atualizadoEm = LocalDateTime.now()
                )

                if (state.isNovaVersao) {
                    // Nova versão - não deveria chegar aqui pela tela de edição
                    _eventos.emit(EditarVersaoEvent.MostrarMensagem("Use a lista para criar nova versão"))
                } else {
                    // Buscar versão original para manter empregoId
                    val versaoOriginal = versaoJornadaRepository.buscarPorId(state.versaoId!!)
                    if (versaoOriginal != null) {
                        val versaoFinal = versaoAtualizada.copy(
                            empregoId = versaoOriginal.empregoId,
                            criadoEm = versaoOriginal.criadoEm
                        )
                        versaoJornadaRepository.atualizar(versaoFinal)
                        _eventos.emit(EditarVersaoEvent.MostrarMensagem("Versão atualizada com sucesso"))
                        _eventos.emit(EditarVersaoEvent.SalvoComSucesso)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao salvar versão")
                _eventos.emit(EditarVersaoEvent.MostrarMensagem("Erro ao salvar: ${e.message}"))
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
