package br.com.tlmacedo.meuponto.presentation.screen.settings.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado da tela principal de configurações.
 *
 * @author Thiago
 * @since 9.0.0
 */
data class SettingsMainUiState(
    val isLoading: Boolean = true,
    val empregoAtual: Emprego? = null,
    val empregosDisponiveis: List<Emprego> = emptyList(),
    val versaoVigenteDescricao: String? = null,
    val totalVersoes: Int = 0
) {
    val empregoAtualId: Long?
        get() = empregoAtual?.id

    val empregoAtualNome: String?
        get() = empregoAtual?.nome

    val temEmpregoConfigurado: Boolean
        get() = empregoAtual != null
}

/**
 * Ações da tela de configurações.
 */
sealed interface SettingsMainAction {
    data class TrocarEmprego(val emprego: Emprego) : SettingsMainAction
    data object Recarregar : SettingsMainAction
}

/**
 * Eventos únicos emitidos pelo ViewModel.
 */
sealed interface SettingsMainEvent {
    data class MostrarMensagem(val mensagem: String) : SettingsMainEvent
    data class EmpregoTrocado(val nomeEmprego: String) : SettingsMainEvent
}

/**
 * ViewModel da tela principal de configurações.
 *
 * Gerencia o estado do emprego atual, versão vigente e lista
 * de empregos disponíveis para troca rápida.
 *
 * @author Thiago
 * @since 9.0.0
 */
@HiltViewModel
class SettingsMainViewModel @Inject constructor(
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val listarEmpregosUseCase: ListarEmpregosUseCase,
    private val trocarEmpregoAtivoUseCase: TrocarEmpregoAtivoUseCase,
    private val versaoJornadaRepository: VersaoJornadaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsMainUiState())
    val uiState: StateFlow<SettingsMainUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<SettingsMainEvent>()
    val eventos: SharedFlow<SettingsMainEvent> = _eventos.asSharedFlow()

    init {
        observarDados()
    }

    /**
     * Processa ações da UI.
     */
    fun onAction(action: SettingsMainAction) {
        when (action) {
            is SettingsMainAction.TrocarEmprego -> trocarEmprego(action.emprego)
            is SettingsMainAction.Recarregar -> recarregar()
        }
    }

    /**
     * Observa mudanças no emprego ativo e lista de empregos.
     */
    private fun observarDados() {
        viewModelScope.launch {
            combine(
                obterEmpregoAtivoUseCase.observar(),
                listarEmpregosUseCase.observarTodos()
            ) { empregoAtivo, empregosComResumo ->
                Pair(empregoAtivo, empregosComResumo.map { it.emprego }.filter { !it.arquivado })
            }.collectLatest { (empregoAtivo, empregosDisponiveis) ->
                if (empregoAtivo != null) {
                    carregarDadosEmprego(empregoAtivo, empregosDisponiveis)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            empregoAtual = null,
                            empregosDisponiveis = empregosDisponiveis,
                            versaoVigenteDescricao = null,
                            totalVersoes = 0
                        )
                    }
                }
            }
        }
    }

    /**
     * Carrega dados complementares do emprego (versão vigente, totais).
     */
    private suspend fun carregarDadosEmprego(
        emprego: Emprego,
        empregosDisponiveis: List<Emprego>
    ) {
        try {
            val versaoVigente = versaoJornadaRepository.buscarVigente(emprego.id)
            val totalVersoes = versaoJornadaRepository.contarPorEmprego(emprego.id)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    empregoAtual = emprego,
                    empregosDisponiveis = empregosDisponiveis,
                    versaoVigenteDescricao = versaoVigente?.let { v ->
                        "Versão ${v.numeroVersao}" + (v.descricao?.let { d -> " - $d" } ?: "")
                    },
                    totalVersoes = totalVersoes
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao carregar dados do emprego %d", emprego.id)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    empregoAtual = emprego,
                    empregosDisponiveis = empregosDisponiveis,
                    versaoVigenteDescricao = null,
                    totalVersoes = 0
                )
            }
        }
    }

    /**
     * Troca o emprego ativo.
     */
    private fun trocarEmprego(emprego: Emprego) {
        viewModelScope.launch {
            when (val resultado = trocarEmpregoAtivoUseCase(emprego.id)) {
                is TrocarEmpregoAtivoUseCase.Resultado.Sucesso -> {
                    _eventos.emit(SettingsMainEvent.EmpregoTrocado(emprego.nome))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.NaoEncontrado -> {
                    _eventos.emit(SettingsMainEvent.MostrarMensagem("Emprego não encontrado"))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.EmpregoIndisponivel -> {
                    _eventos.emit(SettingsMainEvent.MostrarMensagem("Emprego indisponível"))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.Erro -> {
                    _eventos.emit(SettingsMainEvent.MostrarMensagem(resultado.mensagem))
                }
            }
        }
    }

    /**
     * Recarrega os dados.
     */
    private fun recarregar() {
        _uiState.update { it.copy(isLoading = true) }
        observarDados()
    }
}
