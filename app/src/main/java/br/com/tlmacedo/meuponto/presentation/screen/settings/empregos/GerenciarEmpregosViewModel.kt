// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/GerenciarEmpregosViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ArquivarEmpregoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ExcluirEmpregoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ListarEmpregosUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.TrocarEmpregoAtivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado da UI da tela de gerenciamento de empregos.
 *
 * @property empregos Lista de empregos ativos (não arquivados)
 * @property empregosArquivados Lista de empregos arquivados
 * @property empregoAtivoId ID do emprego atualmente selecionado
 * @property isLoading Indica se está carregando dados
 * @property mostrarArquivados Indica se a seção de arquivados está expandida
 * @property dialogConfirmacaoExclusao Emprego para confirmar exclusão (null se não há dialog)
 *
 * @author Thiago
 * @since 2.0.0
 */
data class GerenciarEmpregosUiState(
    val empregos: List<Emprego> = emptyList(),
    val empregosArquivados: List<Emprego> = emptyList(),
    val empregoAtivoId: Long? = null,
    val isLoading: Boolean = true,
    val mostrarArquivados: Boolean = false,
    val dialogConfirmacaoExclusao: Emprego? = null
)

/**
 * Eventos únicos da tela de gerenciamento de empregos.
 *
 * Eventos que devem ser consumidos apenas uma vez pela UI,
 * como exibição de Snackbar ou navegação.
 *
 * @author Thiago
 * @since 2.0.0
 */
sealed interface GerenciarEmpregosEvent {
    /**
     * Exibe uma mensagem de feedback ao usuário.
     *
     * @property mensagem Texto a ser exibido no Snackbar
     */
    data class MostrarMensagem(val mensagem: String) : GerenciarEmpregosEvent
}

/**
 * Ações que podem ser disparadas pela UI.
 *
 * @author Thiago
 * @since 2.0.0
 */
sealed interface GerenciarEmpregosAction {
    /** Alterna a visibilidade da seção de empregos arquivados */
    data object ToggleMostrarArquivados : GerenciarEmpregosAction

    /** Define um emprego como ativo */
    data class DefinirAtivo(val emprego: Emprego) : GerenciarEmpregosAction

    /** Arquiva um emprego */
    data class Arquivar(val emprego: Emprego) : GerenciarEmpregosAction

    /** Desarquiva um emprego */
    data class Desarquivar(val emprego: Emprego) : GerenciarEmpregosAction

    /** Solicita confirmação para excluir um emprego */
    data class SolicitarExclusao(val emprego: Emprego) : GerenciarEmpregosAction

    /** Cancela o diálogo de confirmação de exclusão */
    data object CancelarExclusao : GerenciarEmpregosAction

    /** Confirma a exclusão do emprego */
    data object ConfirmarExclusao : GerenciarEmpregosAction
}

/**
 * ViewModel para gerenciamento de empregos.
 *
 * Gerencia a listagem, ativação, arquivamento e exclusão de empregos.
 * Implementa o padrão UDF (Unidirectional Data Flow) com StateFlow e SharedFlow.
 *
 * @property listarEmpregosUseCase Caso de uso para listar empregos
 * @property obterEmpregoAtivoUseCase Caso de uso para obter emprego ativo
 * @property trocarEmpregoAtivoUseCase Caso de uso para trocar emprego ativo
 * @property arquivarEmpregoUseCase Caso de uso para arquivar/desarquivar emprego
 * @property excluirEmpregoUseCase Caso de uso para excluir emprego
 *
 * @author Thiago
 * @since 2.0.0
 */
@HiltViewModel
class GerenciarEmpregosViewModel @Inject constructor(
    private val listarEmpregosUseCase: ListarEmpregosUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val trocarEmpregoAtivoUseCase: TrocarEmpregoAtivoUseCase,
    private val arquivarEmpregoUseCase: ArquivarEmpregoUseCase,
    private val excluirEmpregoUseCase: ExcluirEmpregoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GerenciarEmpregosUiState())
    val uiState: StateFlow<GerenciarEmpregosUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<GerenciarEmpregosEvent>()
    val eventos: SharedFlow<GerenciarEmpregosEvent> = _eventos.asSharedFlow()

    init {
        carregarEmpregos()
        observarEmpregoAtivo()
    }

    /**
     * Processa as ações da UI.
     *
     * @param action Ação a ser processada
     */
    fun onAction(action: GerenciarEmpregosAction) {
        when (action) {
            is GerenciarEmpregosAction.ToggleMostrarArquivados -> toggleMostrarArquivados()
            is GerenciarEmpregosAction.DefinirAtivo -> definirAtivo(action.emprego)
            is GerenciarEmpregosAction.Arquivar -> arquivar(action.emprego)
            is GerenciarEmpregosAction.Desarquivar -> desarquivar(action.emprego)
            is GerenciarEmpregosAction.SolicitarExclusao -> solicitarExclusao(action.emprego)
            is GerenciarEmpregosAction.CancelarExclusao -> cancelarExclusao()
            is GerenciarEmpregosAction.ConfirmarExclusao -> confirmarExclusao()
        }
    }

    /**
     * Carrega a lista de empregos de forma reativa.
     */
    private fun carregarEmpregos() {
        viewModelScope.launch {
            listarEmpregosUseCase.observarTodos()
                .collect { empregosComResumo ->
                    val empregos = empregosComResumo.map { it.emprego }
                    _uiState.update { state ->
                        state.copy(
                            empregos = empregos.filter { !it.arquivado },
                            empregosArquivados = empregos.filter { it.arquivado },
                            isLoading = false
                        )
                    }
                }
        }
    }

    /**
     * Observa mudanças no emprego ativo.
     */
    private fun observarEmpregoAtivo() {
        viewModelScope.launch {
            obterEmpregoAtivoUseCase.observar()
                .collect { emprego ->
                    _uiState.update { it.copy(empregoAtivoId = emprego?.id) }
                }
        }
    }

    /**
     * Alterna a visibilidade da seção de arquivados.
     */
    private fun toggleMostrarArquivados() {
        _uiState.update { it.copy(mostrarArquivados = !it.mostrarArquivados) }
    }

    /**
     * Define um emprego como ativo.
     *
     * @param emprego Emprego a ser definido como ativo
     */
    private fun definirAtivo(emprego: Emprego) {
        viewModelScope.launch {
            when (val resultado = trocarEmpregoAtivoUseCase(emprego.id)) {
                is TrocarEmpregoAtivoUseCase.Resultado.Sucesso -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("${emprego.nome} definido como ativo"))
                }

                is TrocarEmpregoAtivoUseCase.Resultado.NaoEncontrado -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("Emprego não encontrado"))
                }

                is TrocarEmpregoAtivoUseCase.Resultado.EmpregoIndisponivel -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("Emprego indisponível"))
                }

                is TrocarEmpregoAtivoUseCase.Resultado.Erro -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem(resultado.mensagem))
                }
            }
        }
    }

    /**
     * Arquiva um emprego.
     *
     * @param emprego Emprego a ser arquivado
     */
    private fun arquivar(emprego: Emprego) {
        viewModelScope.launch {
            when (val resultado = arquivarEmpregoUseCase(emprego.id)) {
                is ArquivarEmpregoUseCase.Resultado.Sucesso -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("${emprego.nome} arquivado"))
                }

                is ArquivarEmpregoUseCase.Resultado.NaoEncontrado -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("Emprego não encontrado"))
                }

                is ArquivarEmpregoUseCase.Resultado.UltimoEmprego -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("Não é possível arquivar o único emprego"))
                }

                is ArquivarEmpregoUseCase.Resultado.Erro -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem(resultado.mensagem))
                }
            }
        }
    }

    /**
     * Desarquiva um emprego.
     *
     * @param emprego Emprego a ser desarquivado
     */
    private fun desarquivar(emprego: Emprego) {
        viewModelScope.launch {
            when (val resultado = arquivarEmpregoUseCase.desarquivar(emprego.id)) {
                is ArquivarEmpregoUseCase.Resultado.Sucesso -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("${emprego.nome} restaurado"))
                }

                is ArquivarEmpregoUseCase.Resultado.NaoEncontrado -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("Emprego não encontrado"))
                }

                is ArquivarEmpregoUseCase.Resultado.UltimoEmprego -> {
                    // Não deve ocorrer no desarquivar
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("Erro inesperado"))
                }

                is ArquivarEmpregoUseCase.Resultado.Erro -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem(resultado.mensagem))
                }
            }
        }
    }

    /**
     * Solicita confirmação para excluir um emprego.
     *
     * @param emprego Emprego a ser excluído
     */
    private fun solicitarExclusao(emprego: Emprego) {
        _uiState.update { it.copy(dialogConfirmacaoExclusao = emprego) }
    }

    /**
     * Cancela o diálogo de confirmação de exclusão.
     */
    private fun cancelarExclusao() {
        _uiState.update { it.copy(dialogConfirmacaoExclusao = null) }
    }

    /**
     * Confirma a exclusão do emprego.
     */
    private fun confirmarExclusao() {
        val emprego = _uiState.value.dialogConfirmacaoExclusao ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(dialogConfirmacaoExclusao = null) }

            when (val resultado = excluirEmpregoUseCase(emprego.id)) {
                is ExcluirEmpregoUseCase.Resultado.Sucesso -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("${emprego.nome} excluído"))
                }

                is ExcluirEmpregoUseCase.Resultado.NaoEncontrado -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem("Emprego não encontrado"))
                }

                is ExcluirEmpregoUseCase.Resultado.Erro -> {
                    _eventos.emit(GerenciarEmpregosEvent.MostrarMensagem(resultado.mensagem))
                }
            }
        }
    }
}
