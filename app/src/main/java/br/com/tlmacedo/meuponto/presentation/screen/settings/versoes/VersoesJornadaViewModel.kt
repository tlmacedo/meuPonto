package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel da tela de listagem de versões de jornada.
 *
 * Agora a tela trabalha com um emprego específico via argumento de navegação,
 * removendo dependência do conceito de emprego ativo.
 *
 * @author Thiago
 * @since 4.0.0
 */
@HiltViewModel
class VersoesJornadaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val versaoJornadaRepository: VersaoJornadaRepository
) : ViewModel() {

    private val empregoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val _uiState = MutableStateFlow(VersoesJornadaUiState())
    val uiState: StateFlow<VersoesJornadaUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<VersoesJornadaEvent>()
    val eventos: SharedFlow<VersoesJornadaEvent> = _eventos.asSharedFlow()

    init {
        observarVersoes()
    }

    fun onAction(action: VersoesJornadaAction) {
        when (action) {
            is VersoesJornadaAction.Recarregar -> observarVersoes()
            is VersoesJornadaAction.CriarNovaVersao -> criarNovaVersao()
            is VersoesJornadaAction.AbrirDialogNovaVersao -> abrirDialogNovaVersao()
            is VersoesJornadaAction.FecharDialogNovaVersao -> fecharDialogNovaVersao()
            is VersoesJornadaAction.AlterarDataInicioNovaVersao -> alterarDataInicioNovaVersao(action.data)
            is VersoesJornadaAction.AlterarDescricaoNovaVersao -> alterarDescricaoNovaVersao(action.descricao)
            is VersoesJornadaAction.ToggleCopiarHorariosNovaVersao -> toggleCopiarHorarios(action.copiar)
            is VersoesJornadaAction.ConfirmarNovaVersao -> confirmarNovaVersao()
            is VersoesJornadaAction.EditarVersao -> editarVersao(action.versaoId)
            is VersoesJornadaAction.AbrirDialogExcluir -> abrirDialogExcluir(action.versao)
            is VersoesJornadaAction.FecharDialogExcluir -> fecharDialogExcluir()
            is VersoesJornadaAction.ConfirmarExclusao -> confirmarExclusao()
            is VersoesJornadaAction.DefinirComoVigente -> definirComoVigente(action.versaoId)
            is VersoesJornadaAction.AlternarSelecaoVersao -> alternarSelecao(action.versaoId)
            is VersoesJornadaAction.CompararSelecionadas -> compararSelecionadas()
            is VersoesJornadaAction.LimparSelecao -> limparSelecao()
            is VersoesJornadaAction.LimparErro -> limparErro()
        }
    }

    private fun observarVersoes() {
        if (empregoId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Emprego inválido"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    empregoId = empregoId
                )
            }

            combine(
                versaoJornadaRepository.observarPorEmprego(empregoId),
                versaoJornadaRepository.observarVigente(empregoId)
            ) { versoes, vigente ->
                versoes to vigente
            }
                .catch { e ->
                    Timber.e(e, "Erro ao observar versões do emprego %d", empregoId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Erro ao carregar versões"
                        )
                    }
                }
                .collect { (versoes, vigente) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            empregoId = empregoId,
                            versoes = versoes,
                            versaoVigente = vigente
                        )
                    }
                }
        }
    }

    private fun criarNovaVersao() {
        abrirDialogNovaVersao()
    }

    private fun abrirDialogNovaVersao() {
        _uiState.update {
            it.copy(
                mostrarDialogNovaVersao = true,
                dataInicioNovaVersao = it.versaoVigente?.dataFim?.plusDays(1) ?: LocalDate.now(),
                descricaoNovaVersao = "",
                copiarHorariosNovaVersao = true
            )
        }
    }

    private fun fecharDialogNovaVersao() {
        _uiState.update {
            it.copy(mostrarDialogNovaVersao = false)
        }
    }

    private fun alterarDataInicioNovaVersao(data: LocalDate) {
        _uiState.update { it.copy(dataInicioNovaVersao = data) }
    }

    private fun alterarDescricaoNovaVersao(descricao: String) {
        _uiState.update { it.copy(descricaoNovaVersao = descricao) }
    }

    private fun toggleCopiarHorarios(copiar: Boolean) {
        _uiState.update { it.copy(copiarHorariosNovaVersao = copiar) }
    }

    private fun confirmarNovaVersao() {
        val state = _uiState.value
        if (state.empregoId <= 0L) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCriando = true) }
            try {
                val novaVersaoId = versaoJornadaRepository.criarNovaVersao(
                    empregoId = state.empregoId,
                    dataInicio = state.dataInicioNovaVersao,
                    descricao = state.descricaoNovaVersao.ifBlank { null },
                    copiarDaVersaoAnterior = state.copiarHorariosNovaVersao
                )
                
                _uiState.update { 
                    it.copy(
                        isCriando = false, 
                        mostrarDialogNovaVersao = false
                    ) 
                }
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Nova versão criada"))
                _eventos.emit(VersoesJornadaEvent.NavegarParaEditar(novaVersaoId))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao criar nova versão")
                _uiState.update { it.copy(isCriando = false) }
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Erro: ${e.message}"))
            }
        }
    }

    private fun editarVersao(versaoId: Long) {
        viewModelScope.launch {
            _eventos.emit(VersoesJornadaEvent.NavegarParaEditar(versaoId))
        }
    }

    private fun abrirDialogExcluir(versao: VersaoJornada) {
        if (_uiState.value.versoes.size <= 1) {
            viewModelScope.launch {
                _eventos.emit(
                    VersoesJornadaEvent.MostrarMensagem(
                        "Não é possível excluir a única versão"
                    )
                )
            }
            return
        }

        if (versao.vigente) {
            viewModelScope.launch {
                _eventos.emit(
                    VersoesJornadaEvent.MostrarMensagem(
                        "Não é possível excluir a versão vigente"
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                mostrarDialogExcluir = true,
                versaoParaExcluir = versao
            )
        }
    }

    private fun fecharDialogExcluir() {
        _uiState.update {
            it.copy(
                mostrarDialogExcluir = false,
                versaoParaExcluir = null
            )
        }
    }

    private fun confirmarExclusao() {
        val versao = _uiState.value.versaoParaExcluir ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isExcluindo = true) }

            try {
                versaoJornadaRepository.excluirPorId(versao.id)
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Versão excluída"))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao excluir versão %d", versao.id)
                _eventos.emit(
                    VersoesJornadaEvent.MostrarMensagem(
                        "Erro ao excluir: ${e.message}"
                    )
                )
            } finally {
                _uiState.update {
                    it.copy(
                        isExcluindo = false,
                        mostrarDialogExcluir = false,
                        versaoParaExcluir = null
                    )
                }
            }
        }
    }

    private fun definirComoVigente(versaoId: Long) {
        if (empregoId <= 0L) {
            viewModelScope.launch {
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Emprego inválido"))
            }
            return
        }

        viewModelScope.launch {
            try {
                // Remove vigente da versão atual
                val versaoAtual = versaoJornadaRepository.buscarVigente(empregoId)
                versaoAtual?.let {
                    versaoJornadaRepository.atualizar(it.copy(vigente = false))
                }

                // Define nova versão como vigente
                val novaVigente = versaoJornadaRepository.buscarPorId(versaoId)
                novaVigente?.let {
                    versaoJornadaRepository.atualizar(it.copy(vigente = true))
                    _eventos.emit(
                        VersoesJornadaEvent.MostrarMensagem("Versão definida como vigente")
                    )
                } ?: run {
                    _eventos.emit(
                        VersoesJornadaEvent.MostrarMensagem("Versão não encontrada")
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao definir versão %d como vigente", versaoId)
                _eventos.emit(
                    VersoesJornadaEvent.MostrarMensagem("Erro: ${e.message}")
                )
            }
        }
    }

    private fun alternarSelecao(versaoId: Long) {
        _uiState.update { state ->
            val novas = if (state.versoesSelecionadas.contains(versaoId)) {
                state.versoesSelecionadas - versaoId
            } else {
                if (state.versoesSelecionadas.size < 2) {
                    state.versoesSelecionadas + versaoId
                } else {
                    state.versoesSelecionadas
                }
            }
            state.copy(versoesSelecionadas = novas)
        }
    }

    private fun compararSelecionadas() {
        val selecionadas = _uiState.value.versoesSelecionadas.toList()
        if (selecionadas.size == 2) {
            viewModelScope.launch {
                val v1 = selecionadas[0]
                val v2 = selecionadas[1]
                _eventos.emit(VersoesJornadaEvent.NavegarParaComparar(empregoId, v1, v2))
                limparSelecao()
            }
        }
    }

    private fun limparSelecao() {
        _uiState.update { it.copy(versoesSelecionadas = emptySet()) }
    }

    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
