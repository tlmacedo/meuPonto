// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciasViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.AtualizarAusenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ExcluirAusenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ListarAusenciasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ResultadoExcluirAusencia
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.presentation.screen.history.InfoDiaHistorico
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
 * ViewModel da tela de listagem de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.6.0 - Filtros múltiplos e lista unificada
 * @updated 12.2.0 - Adicionada visualização de calendário anual
 */
@HiltViewModel
class AusenciasViewModel @Inject constructor(
    private val listarAusenciasUseCase: ListarAusenciasUseCase,
    private val excluirAusenciaUseCase: ExcluirAusenciaUseCase,
    private val atualizarAusenciaUseCase: AtualizarAusenciaUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AusenciasUiState())
    val uiState: StateFlow<AusenciasUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AusenciasUiEvent>()
    val uiEvent: SharedFlow<AusenciasUiEvent> = _uiEvent.asSharedFlow()

    private var ausenciasJob: Job? = null

    init {
        carregarEmpregoAtivo()
    }

    fun onAction(action: AusenciasAction) {
        when (action) {
            // Filtros
            is AusenciasAction.ToggleTipo -> toggleTipo(action.tipo)
            is AusenciasAction.FiltroAnoChange -> filtrarPorAno(action.ano)
            is AusenciasAction.ToggleOrdem -> toggleOrdem()
            is AusenciasAction.LimparFiltros -> limparFiltros()

            // Visualização
            AusenciasAction.ToggleVisualizacao -> {
                _uiState.update { it.copy(visualizacaoCalendario = !it.visualizacaoCalendario) }
                atualizarDiasCalendario()
            }

            is AusenciasAction.MesChange -> {
                _uiState.update { it.copy(mesVisualizacao = action.mes) }
                // No modo anual, MesChange pode ser usado para scroll, não precisa atualizar tudo
            }

            // CRUD
            is AusenciasAction.NovaAusencia -> {
                viewModelScope.launch {
                    _uiEvent.emit(AusenciasUiEvent.NavegarParaNovaAusencia)
                }
            }

            is AusenciasAction.EditarAusencia -> {
                viewModelScope.launch {
                    _uiEvent.emit(AusenciasUiEvent.NavegarParaEditarAusencia(action.ausencia.id))
                }
            }

            is AusenciasAction.SolicitarExclusao -> solicitarExclusao(action.ausencia)
            is AusenciasAction.ConfirmarExclusao -> confirmarExclusao()
            is AusenciasAction.CancelarExclusao -> cancelarExclusao()
            is AusenciasAction.ToggleAtivo -> toggleAtivo(action.ausencia)

            // Geral
            is AusenciasAction.LimparErro -> limparErro()
            is AusenciasAction.Voltar -> {
                viewModelScope.launch {
                    _uiEvent.emit(AusenciasUiEvent.Voltar)
                }
            }
        }
    }

    private fun carregarEmpregoAtivo() {
        viewModelScope.launch {
            obterEmpregoAtivoUseCase.observar().collect { emprego ->
                _uiState.update { it.copy(empregoAtivo = emprego) }
                if (emprego != null) {
                    carregarTodasAusencias(emprego.id)
                }
            }
        }
    }

    private fun carregarTodasAusencias(empregoId: Long) {
        ausenciasJob?.cancel()

        ausenciasJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                listarAusenciasUseCase.observarTodas(empregoId).collect { ausencias ->
                    _uiState.update {
                        it.copy(
                            ausencias = ausencias,
                            isLoading = false
                        )
                    }
                    atualizarDiasCalendario()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar ausências: ${e.message}"
                    )
                }
            }
        }
    }

    private fun atualizarDiasCalendario() {
        val state = _uiState.value
        val ausencias = state.ausencias.filter { it.ativo }

        // Carregamos o ano inteiro
        val ano = state.filtroAno ?: LocalDate.now().year
        val dataInicioAno = LocalDate.of(ano, 1, 1)
        val dataFimAno = LocalDate.of(ano, 12, 31)

        val diasList = mutableListOf<InfoDiaHistorico>()
        var dataAtual = dataInicioAno
        while (dataAtual <= dataFimAno) {
            val ausenciasDoDia =
                ausencias.filter { it.dataInicio <= dataAtual && it.dataFim >= dataAtual }

            val tipoEspecial = when {
                ausenciasDoDia.any { it.tipo == TipoAusencia.Ferias } ->
                    TipoDiaEspecial.Descanso.Ferias

                ausenciasDoDia.any { it.tipo == TipoAusencia.Atestado } ->
                    TipoDiaEspecial.Ausencia.Atestado

                ausenciasDoDia.any { it.tipo == TipoAusencia.Falta.Justificada } ->
                    TipoDiaEspecial.Ausencia.Falta.Justificada

                ausenciasDoDia.any { it.tipo == TipoAusencia.DayOff } ->
                    TipoDiaEspecial.Ausencia.DayOff

                ausenciasDoDia.any { it.tipo == TipoAusencia.Falta.Injustificada } ->
                    TipoDiaEspecial.Ausencia.Falta.Injustificada

                else -> TipoDiaEspecial.Normal
            }

            diasList.add(
                InfoDiaHistorico(
                    resumoDia = ResumoDia(data = dataAtual, tipoDiaEspecial = tipoEspecial),
                    ausencias = ausenciasDoDia
                )
            )
            dataAtual = dataAtual.plusDays(1)
        }

        _uiState.update { it.copy(diasHistorico = diasList) }
    }

    private fun toggleTipo(tipo: TipoAusencia) {
        _uiState.update { state ->
            val novosFiltros = if (tipo in state.filtroTipos) {
                state.filtroTipos - tipo
            } else {
                state.filtroTipos + tipo
            }
            state.copy(filtroTipos = novosFiltros)
        }
    }

    private fun filtrarPorAno(ano: Int?) {
        _uiState.update { it.copy(filtroAno = ano) }
        atualizarDiasCalendario()
    }

    private fun toggleOrdem() {
        _uiState.update { state ->
            state.copy(
                ordemData = when (state.ordemData) {
                    OrdemData.CRESCENTE -> OrdemData.DECRESCENTE
                    OrdemData.DECRESCENTE -> OrdemData.CRESCENTE
                }
            )
        }
    }

    private fun limparFiltros() {
        _uiState.update {
            it.copy(
                filtroTipos = emptySet(),
                filtroAno = LocalDate.now().year
            )
        }
        atualizarDiasCalendario()
    }

    private fun toggleAtivo(ausencia: Ausencia) {
        viewModelScope.launch {
            try {
                val ausenciaAtualizada = ausencia.copy(ativo = !ausencia.ativo)
                atualizarAusenciaUseCase(ausenciaAtualizada)

                val mensagem = if (ausenciaAtualizada.ativo) {
                    "Ausência ativada"
                } else {
                    "Ausência desativada"
                }
                _uiEvent.emit(AusenciasUiEvent.MostrarMensagem(mensagem))
            } catch (e: Exception) {
                _uiEvent.emit(AusenciasUiEvent.MostrarErro("Erro ao atualizar: ${e.message}"))
            }
        }
    }

    private fun solicitarExclusao(ausencia: Ausencia) {
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                ausenciaParaExcluir = ausencia
            )
        }
    }

    private fun cancelarExclusao() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false,
                ausenciaParaExcluir = null
            )
        }
    }

    private fun confirmarExclusao() {
        val ausencia = _uiState.value.ausenciaParaExcluir ?: return

        viewModelScope.launch {
            when (val resultado = excluirAusenciaUseCase(ausencia)) {
                is ResultadoExcluirAusencia.Sucesso -> {
                    _uiEvent.emit(AusenciasUiEvent.MostrarMensagem("Ausência excluída"))
                }

                is ResultadoExcluirAusencia.Erro -> {
                    _uiEvent.emit(AusenciasUiEvent.MostrarErro(resultado.mensagem))
                }
            }
            cancelarExclusao()
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    fun recarregar() {
        _uiState.value.empregoAtivo?.let { emprego ->
            carregarTodasAusencias(emprego.id)
        }
    }
}
