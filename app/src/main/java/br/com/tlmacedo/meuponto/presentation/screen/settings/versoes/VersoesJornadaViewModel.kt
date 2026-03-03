// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/versoes/VersoesJornadaViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
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
 * @author Thiago
 * @since 4.0.0
 */
@HiltViewModel
class VersoesJornadaViewModel @Inject constructor(
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VersoesJornadaUiState())
    val uiState: StateFlow<VersoesJornadaUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<VersoesJornadaEvent>()
    val eventos: SharedFlow<VersoesJornadaEvent> = _eventos.asSharedFlow()

    init {
        observarDados()
    }

    fun onAction(action: VersoesJornadaAction) {
        when (action) {
            is VersoesJornadaAction.Recarregar -> observarDados()
            is VersoesJornadaAction.CriarNovaVersao -> criarNovaVersao()
            is VersoesJornadaAction.EditarVersao -> editarVersao(action.versaoId)
            is VersoesJornadaAction.AbrirDialogExcluir -> abrirDialogExcluir(action.versao)
            is VersoesJornadaAction.FecharDialogExcluir -> fecharDialogExcluir()
            is VersoesJornadaAction.ConfirmarExclusao -> confirmarExclusao()
            is VersoesJornadaAction.DefinirComoVigente -> definirComoVigente(action.versaoId)
            is VersoesJornadaAction.LimparErro -> limparErro()
        }
    }

    private fun observarDados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            obterEmpregoAtivoUseCase.observar()
                .catch { e ->
                    Timber.e(e, "Erro ao observar emprego ativo")
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Erro ao carregar: ${e.message}")
                    }
                }
                .collect { emprego ->
                    if (emprego != null) {
                        observarVersoes(emprego.id)
                        _uiState.update { it.copy(emprego = emprego) }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "Nenhum emprego ativo")
                        }
                    }
                }
        }
    }

    private fun observarVersoes(empregoId: Long) {
        viewModelScope.launch {
            combine(
                versaoJornadaRepository.observarPorEmprego(empregoId),
                versaoJornadaRepository.observarVigente(empregoId)
            ) { versoes, vigente ->
                versoes to vigente
            }
                .catch { e ->
                    Timber.e(e, "Erro ao observar versões")
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Erro ao carregar versões")
                    }
                }
                .collect { (versoes, vigente) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            versoes = versoes,
                            versaoVigente = vigente
                        )
                    }
                }
        }
    }

    private fun criarNovaVersao() {
        viewModelScope.launch {
            val emprego = _uiState.value.emprego ?: return@launch

            try {
                val novaVersaoId = versaoJornadaRepository.criarNovaVersao(
                    empregoId = emprego.id,
                    dataInicio = LocalDate.now(),
                    descricao = null,
                    copiarDaVersaoAnterior = true
                )

                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Nova versão criada"))
                _eventos.emit(VersoesJornadaEvent.NavegarParaEditar(novaVersaoId))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao criar nova versão")
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Erro ao criar versão: ${e.message}"))
            }
        }
    }

    private fun editarVersao(versaoId: Long) {
        viewModelScope.launch {
            _eventos.emit(VersoesJornadaEvent.NavegarParaEditar(versaoId))
        }
    }

    private fun abrirDialogExcluir(versao: br.com.tlmacedo.meuponto.domain.model.VersaoJornada) {
        if (_uiState.value.versoes.size <= 1) {
            viewModelScope.launch {
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Não é possível excluir a única versão"))
            }
            return
        }

        if (versao.vigente) {
            viewModelScope.launch {
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Não é possível excluir a versão vigente"))
            }
            return
        }

        _uiState.update {
            it.copy(mostrarDialogExcluir = true, versaoParaExcluir = versao)
        }
    }

    private fun fecharDialogExcluir() {
        _uiState.update {
            it.copy(mostrarDialogExcluir = false, versaoParaExcluir = null)
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
                Timber.e(e, "Erro ao excluir versão")
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Erro ao excluir: ${e.message}"))
            } finally {
                _uiState.update {
                    it.copy(isExcluindo = false, mostrarDialogExcluir = false, versaoParaExcluir = null)
                }
            }
        }
    }

    private fun definirComoVigente(versaoId: Long) {
        val emprego = _uiState.value.emprego ?: return

        viewModelScope.launch {
            try {
                // Remover vigente de todas
                val versaoAtual = versaoJornadaRepository.buscarVigente(emprego.id)
                versaoAtual?.let {
                    versaoJornadaRepository.atualizar(it.copy(vigente = false))
                }

                // Definir nova como vigente
                val novaVigente = versaoJornadaRepository.buscarPorId(versaoId)
                novaVigente?.let {
                    versaoJornadaRepository.atualizar(it.copy(vigente = true))
                }

                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Versão definida como vigente"))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao definir versão vigente")
                _eventos.emit(VersoesJornadaEvent.MostrarMensagem("Erro: ${e.message}"))
            }
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
