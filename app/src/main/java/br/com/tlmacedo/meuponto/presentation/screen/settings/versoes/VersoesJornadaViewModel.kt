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
            is VersoesJornadaAction.EditarVersao -> editarVersao(action.versaoId)
            is VersoesJornadaAction.AbrirDialogExcluir -> abrirDialogExcluir(action.versao)
            is VersoesJornadaAction.FecharDialogExcluir -> fecharDialogExcluir()
            is VersoesJornadaAction.ConfirmarExclusao -> confirmarExclusao()
            is VersoesJornadaAction.DefinirComoVigente -> definirComoVigente(action.versaoId)
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
        if (empregoId <= 0L) {
            viewModelScope.launch {
                _eventos.emit(
                    VersoesJornadaEvent.MostrarMensagem("Emprego inválido para criar versão")
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val novaVersaoId = versaoJornadaRepository.criarNovaVersao(
                    empregoId = empregoId,
                    dataInicio = LocalDate.now(),
                    descricao = null,
                    copiarDaVersaoAnterior = true
                )
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Nova versão criada"))
                _eventos.emit(VersoesJornadaEvent.NavegarParaEditar(novaVersaoId))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao criar nova versão para emprego %d", empregoId)
                _eventos.emit(
                    VersoesJornadaEvent.MostrarMensagem(
                        "Erro ao criar versão: ${e.message}"
                    )
                )
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

    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
