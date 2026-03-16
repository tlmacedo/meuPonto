// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/lixeira/LixeiraViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.lixeira

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ExcluirPontoPermanenteUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.RestaurarPontoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * ViewModel da tela de Lixeira.
 *
 * Gerencia os pontos excluídos (soft delete) e permite restauração
 * ou exclusão permanente.
 *
 * @author Thiago
 * @since 9.2.0
 * @updated 11.0.0 - Refatorado para usar UseCases com soft delete
 */
@HiltViewModel
class LixeiraViewModel @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val empregoRepository: EmpregoRepository,
    private val restaurarPontoUseCase: RestaurarPontoUseCase,
    private val excluirPontoPermanenteUseCase: ExcluirPontoPermanenteUseCase
) : ViewModel() {

    companion object {
        /** Dias para exclusão permanente automática */
        const val DIAS_RETENCAO = 30
    }

    private val _uiState = MutableStateFlow(LixeiraUiState())
    val uiState: StateFlow<LixeiraUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<LixeiraUiEvent>()
    val eventos: SharedFlow<LixeiraUiEvent> = _eventos.asSharedFlow()

    // Cache de nomes de empregos
    private var empregosCache: Map<Long, String> = emptyMap()

    init {
        carregarEmpregos()
        observarPontosExcluidos()
    }

    /**
     * Processa ações da UI.
     */
    fun onAction(action: LixeiraAction) {
        when (action) {
            // Seleção
            is LixeiraAction.ToggleSelecao -> toggleSelecao(action.pontoId)
            is LixeiraAction.SelecionarTodos -> selecionarTodos()
            is LixeiraAction.LimparSelecao -> limparSelecao()
            is LixeiraAction.AtivarModoSelecao -> ativarModoSelecao()
            is LixeiraAction.DesativarModoSelecao -> desativarModoSelecao()

            // Restaurar
            is LixeiraAction.SolicitarRestaurar -> solicitarRestaurar(action.ponto)
            is LixeiraAction.ConfirmarRestaurar -> confirmarRestaurar()
            is LixeiraAction.RestaurarSelecionados -> restaurarSelecionados()
            is LixeiraAction.CancelarRestaurar -> cancelarRestaurar()

            // Excluir permanente
            is LixeiraAction.SolicitarExcluir -> solicitarExcluir(action.ponto)
            is LixeiraAction.ConfirmarExcluir -> confirmarExcluir()
            is LixeiraAction.ExcluirSelecionados -> excluirSelecionados()
            is LixeiraAction.CancelarExcluir -> cancelarExcluir()

            // Esvaziar lixeira
            is LixeiraAction.SolicitarEsvaziarLixeira -> solicitarEsvaziarLixeira()
            is LixeiraAction.ConfirmarEsvaziarLixeira -> confirmarEsvaziarLixeira()
            is LixeiraAction.CancelarEsvaziarLixeira -> cancelarEsvaziarLixeira()

            // Filtros e ordenação
            is LixeiraAction.FiltrarPorEmprego -> filtrarPorEmprego(action.empregoId)
            is LixeiraAction.AlterarOrdenacao -> alterarOrdenacao(action.ordenacao)

            // Outros
            is LixeiraAction.Recarregar -> recarregar()
            is LixeiraAction.LimparErro -> limparErro()
            is LixeiraAction.Voltar -> emitirEvento(LixeiraUiEvent.Voltar)
        }
    }

    // ========================================================================
    // CARREGAMENTO DE DADOS
    // ========================================================================

    private fun carregarEmpregos() {
        viewModelScope.launch {
            try {
                empregoRepository.listarTodos().collect { empregos ->
                    empregosCache = empregos.associate { it.id to it.nome }
                }
            } catch (e: Exception) {
                // Cache vazio, usará "Emprego desconhecido"
            }
        }
    }

    private fun observarPontosExcluidos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                pontoRepository.observarDeletados().collect { pontos ->
                    val agora = Instant.now()
                    val itens = pontos.map { ponto ->
                        val diasRestantes = calcularDiasRestantes(ponto.deletedAt, agora)
                        PontoLixeiraItem(
                            id = ponto.id,
                            ponto = ponto,
                            nomeEmprego = empregosCache[ponto.empregoId] ?: "Emprego desconhecido",
                            diasRestantes = diasRestantes,
                            expirandoEmBreve = diasRestantes <= 7
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pontosNaLixeira = itens,
                            mensagemErro = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensagemErro = "Erro ao carregar lixeira: ${e.message}"
                    )
                }
            }
        }
    }

    private fun calcularDiasRestantes(deletedAtMillis: Long?, agora: Instant): Int {
        if (deletedAtMillis == null) return DIAS_RETENCAO
        val deletedAt = Instant.ofEpochMilli(deletedAtMillis)
        val diasPassados = Duration.between(deletedAt, agora).toDays().toInt()
        return maxOf(0, DIAS_RETENCAO - diasPassados)
    }

    // ========================================================================
    // SELEÇÃO
    // ========================================================================

    private fun toggleSelecao(pontoId: Long) {
        _uiState.update { state ->
            val novaSelecao = if (pontoId in state.pontosSelecionados) {
                state.pontosSelecionados - pontoId
            } else {
                state.pontosSelecionados + pontoId
            }
            state.copy(
                pontosSelecionados = novaSelecao,
                modoSelecao = novaSelecao.isNotEmpty()
            )
        }
    }

    private fun selecionarTodos() {
        _uiState.update { state ->
            val todosIds = state.pontosFiltrados.map { it.id }.toSet()
            state.copy(pontosSelecionados = todosIds)
        }
    }

    private fun limparSelecao() {
        _uiState.update { it.copy(pontosSelecionados = emptySet()) }
    }

    private fun ativarModoSelecao() {
        _uiState.update { it.copy(modoSelecao = true) }
    }

    private fun desativarModoSelecao() {
        _uiState.update {
            it.copy(
                modoSelecao = false,
                pontosSelecionados = emptySet()
            )
        }
    }

    // ========================================================================
    // RESTAURAR
    // ========================================================================

    private fun solicitarRestaurar(ponto: br.com.tlmacedo.meuponto.domain.model.Ponto) {
        _uiState.update {
            it.copy(
                showConfirmacaoRestaurar = true,
                pontoParaAcao = ponto
            )
        }
    }

    private fun confirmarRestaurar() {
        val ponto = _uiState.value.pontoParaAcao ?: return

        viewModelScope.launch {
            restaurarPontoUseCase(ponto.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showConfirmacaoRestaurar = false,
                            pontoParaAcao = null
                        )
                    }
                    emitirEvento(LixeiraUiEvent.ItemRestaurado(ponto.dataFormatada))
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            showConfirmacaoRestaurar = false,
                            pontoParaAcao = null,
                            mensagemErro = "Erro ao restaurar: ${e.message}"
                        )
                    }
                }
        }
    }

    private fun restaurarSelecionados() {
        viewModelScope.launch {
            val selecionados = _uiState.value.pontosSelecionados.toList()

            restaurarPontoUseCase.restaurarMultiplos(selecionados)
                .onSuccess { quantidade ->
                    desativarModoSelecao()
                    emitirEvento(LixeiraUiEvent.ItensRestaurados(quantidade))
                }
                .onFailure { e ->
                    emitirEvento(LixeiraUiEvent.MostrarMensagem("Erro ao restaurar: ${e.message}"))
                }
        }
    }

    private fun cancelarRestaurar() {
        _uiState.update {
            it.copy(
                showConfirmacaoRestaurar = false,
                pontoParaAcao = null
            )
        }
    }

    // ========================================================================
    // EXCLUIR PERMANENTE
    // ========================================================================

    private fun solicitarExcluir(ponto: br.com.tlmacedo.meuponto.domain.model.Ponto) {
        _uiState.update {
            it.copy(
                showConfirmacaoExcluir = true,
                pontoParaAcao = ponto
            )
        }
    }

    private fun confirmarExcluir() {
        val ponto = _uiState.value.pontoParaAcao ?: return

        viewModelScope.launch {
            excluirPontoPermanenteUseCase(ponto.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showConfirmacaoExcluir = false,
                            pontoParaAcao = null
                        )
                    }
                    emitirEvento(LixeiraUiEvent.ItemExcluido(ponto.dataFormatada))
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            showConfirmacaoExcluir = false,
                            pontoParaAcao = null,
                            mensagemErro = "Erro ao excluir: ${e.message}"
                        )
                    }
                }
        }
    }

    private fun excluirSelecionados() {
        viewModelScope.launch {
            val selecionados = _uiState.value.pontosSelecionados.toList()

            excluirPontoPermanenteUseCase.excluirMultiplos(selecionados)
                .onSuccess { quantidade ->
                    desativarModoSelecao()
                    emitirEvento(LixeiraUiEvent.ItensExcluidos(quantidade))
                }
                .onFailure { e ->
                    emitirEvento(LixeiraUiEvent.MostrarMensagem("Erro ao excluir: ${e.message}"))
                }
        }
    }

    private fun cancelarExcluir() {
        _uiState.update {
            it.copy(
                showConfirmacaoExcluir = false,
                pontoParaAcao = null
            )
        }
    }

    // ========================================================================
    // ESVAZIAR LIXEIRA
    // ========================================================================

    private fun solicitarEsvaziarLixeira() {
        _uiState.update { it.copy(showConfirmacaoEsvaziar = true) }
    }

    private fun confirmarEsvaziarLixeira() {
        viewModelScope.launch {
            excluirPontoPermanenteUseCase.esvaziarLixeira()
                .onSuccess { quantidade ->
                    _uiState.update { it.copy(showConfirmacaoEsvaziar = false) }
                    emitirEvento(LixeiraUiEvent.LixeiraEsvaziada)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            showConfirmacaoEsvaziar = false,
                            mensagemErro = "Erro ao esvaziar lixeira: ${e.message}"
                        )
                    }
                }
        }
    }

    private fun cancelarEsvaziarLixeira() {
        _uiState.update { it.copy(showConfirmacaoEsvaziar = false) }
    }

    // ========================================================================
    // FILTROS E ORDENAÇÃO
    // ========================================================================

    private fun filtrarPorEmprego(empregoId: Long?) {
        _uiState.update {
            it.copy(
                filtroEmpregoId = empregoId,
                pontosSelecionados = emptySet()
            )
        }
    }

    private fun alterarOrdenacao(ordenacao: OrdenacaoLixeira) {
        _uiState.update { it.copy(ordenacao = ordenacao) }
    }

    // ========================================================================
    // UTILITÁRIOS
    // ========================================================================

    private fun recarregar() {
        observarPontosExcluidos()
    }

    private fun limparErro() {
        _uiState.update { it.copy(mensagemErro = null) }
    }

    private fun emitirEvento(evento: LixeiraUiEvent) {
        viewModelScope.launch {
            _eventos.emit(evento)
        }
    }
}
