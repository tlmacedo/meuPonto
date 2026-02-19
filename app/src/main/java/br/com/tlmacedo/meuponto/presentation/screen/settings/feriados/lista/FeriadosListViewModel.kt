// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/lista/FeriadosListViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.usecase.feriado.ImportarFeriadosNacionaisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel da tela de listagem de feriados.
 *
 * @author Thiago
 * @since 3.0.0
 */
@HiltViewModel
class FeriadosListViewModel @Inject constructor(
    private val feriadoRepository: FeriadoRepository,
    private val importarFeriadosUseCase: ImportarFeriadosNacionaisUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeriadosListUiState())
    val uiState: StateFlow<FeriadosListUiState> = _uiState.asStateFlow()

    init {
        carregarFeriados()
    }

    fun onEvent(event: FeriadosListEvent) {
        when (event) {
            is FeriadosListEvent.OnSearchQueryChange -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                aplicarFiltros()
            }
            is FeriadosListEvent.OnFiltroTipoChange -> {
                _uiState.update { it.copy(filtroTipo = event.tipo) }
                aplicarFiltros()
            }
            is FeriadosListEvent.OnFiltroAnoChange -> {
                _uiState.update { it.copy(filtroAno = event.ano) }
                aplicarFiltros()
            }
            FeriadosListEvent.OnLimparFiltros -> {
                _uiState.update {
                    it.copy(
                        filtroTipo = null,
                        filtroAno = null,
                        searchQuery = ""
                    )
                }
                aplicarFiltros()
            }
            FeriadosListEvent.OnShowImportDialog -> {
                _uiState.update { it.copy(showImportDialog = true) }
            }
            FeriadosListEvent.OnDismissImportDialog -> {
                _uiState.update { it.copy(showImportDialog = false) }
            }
            FeriadosListEvent.OnImportarFeriados -> importarFeriados()
            is FeriadosListEvent.OnShowDeleteDialog -> {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = true,
                        feriadoParaExcluir = event.feriado
                    )
                }
            }
            FeriadosListEvent.OnDismissDeleteDialog -> {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        feriadoParaExcluir = null
                    )
                }
            }
            FeriadosListEvent.OnConfirmarExclusao -> confirmarExclusao()
            is FeriadosListEvent.OnToggleAtivo -> toggleAtivo(event.feriado)
            FeriadosListEvent.OnDismissMessage -> {
                _uiState.update {
                    it.copy(
                        mensagemSucesso = null,
                        mensagemErro = null
                    )
                }
            }
        }
    }

    private fun carregarFeriados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Observa TODOS os feriados (ativos e inativos) para a tela de gerenciamento
                feriadoRepository.observarTodos().collect { feriados ->
                    val anos = extrairAnosDisponiveis(feriados)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            feriados = feriados,
                            anosDisponiveis = anos
                        )
                    }
                    aplicarFiltros()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensagemErro = "Erro ao carregar feriados: ${e.message}"
                    )
                }
            }
        }
    }

    private fun extrairAnosDisponiveis(feriados: List<Feriado>): List<Int> {
        val anoAtual = LocalDate.now().year
        val anosDosFeriados = feriados.mapNotNull { it.anoReferencia }.toSet()
        val anosFuturos = setOf(anoAtual, anoAtual + 1)
        return (anosDosFeriados + anosFuturos).sorted()
    }

    private fun aplicarFiltros() {
        val state = _uiState.value
        var filtrados = state.feriados

        // Filtro por tipo
        state.filtroTipo?.let { tipo ->
            filtrados = filtrados.filter { it.tipo == tipo }
        }

        // Filtro por ano
        state.filtroAno?.let { ano ->
            filtrados = filtrados.filter { feriado ->
                feriado.anoReferencia == ano || feriado.anoReferencia == null
            }
        }

        // Filtro por busca
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtrados = filtrados.filter { feriado ->
                feriado.nome.lowercase().contains(query) ||
                        feriado.observacao?.lowercase()?.contains(query) == true
            }
        }

        // Ordenar: Pontes primeiro, depois por data
        filtrados = filtrados.sortedWith(
            compareBy<Feriado> { it.tipo != TipoFeriado.PONTE }
                .thenBy { it.calcularProximaOcorrencia() }
                .thenBy { it.nome }
        )

        _uiState.update { it.copy(feriadosFiltrados = filtrados) }
    }

    /**
     * Calcula a próxima ocorrência de um feriado para fins de ordenação.
     */
    private fun Feriado.calcularProximaOcorrencia(): LocalDate? {
        val hoje = LocalDate.now()
        return when (recorrencia) {
            RecorrenciaFeriado.ANUAL -> {
                diaMes?.let { dm ->
                    val esteAno = dm.atYear(hoje.year)
                    if (esteAno.isBefore(hoje)) dm.atYear(hoje.year + 1) else esteAno
                }
            }
            RecorrenciaFeriado.UNICO -> dataEspecifica
        }
    }

    private fun importarFeriados() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    importacaoEmAndamento = true,
                    showImportDialog = false
                )
            }

            when (val resultado = importarFeriadosUseCase(forcarReimportacao = true)) {
                is ImportarFeriadosNacionaisUseCase.Resultado.Sucesso -> {
                    val mensagem = if (resultado.feriadosImportados > 0) {
                        "✅ ${resultado.feriadosImportados} feriados importados"
                    } else {
                        "ℹ️ Nenhum novo feriado para importar"
                    }
                    _uiState.update {
                        it.copy(
                            importacaoEmAndamento = false,
                            mensagemSucesso = mensagem
                        )
                    }
                }
                is ImportarFeriadosNacionaisUseCase.Resultado.Erro -> {
                    _uiState.update {
                        it.copy(
                            importacaoEmAndamento = false,
                            mensagemErro = "❌ ${resultado.mensagem}"
                        )
                    }
                }
                ImportarFeriadosNacionaisUseCase.Resultado.SemConexao -> {
                    _uiState.update {
                        it.copy(
                            importacaoEmAndamento = false,
                            mensagemErro = "📡 Sem conexão com a internet"
                        )
                    }
                }
            }
        }
    }

    private fun confirmarExclusao() {
        val feriado = _uiState.value.feriadoParaExcluir ?: return

        viewModelScope.launch {
            try {
                feriadoRepository.excluir(feriado)
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        feriadoParaExcluir = null,
                        mensagemSucesso = "✅ ${feriado.nome} excluído"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        feriadoParaExcluir = null,
                        mensagemErro = "❌ Erro ao excluir: ${e.message}"
                    )
                }
            }
        }
    }

    private fun toggleAtivo(feriado: Feriado) {
        viewModelScope.launch {
            try {
                feriadoRepository.atualizar(feriado.copy(ativo = !feriado.ativo))
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(mensagemErro = "❌ Erro ao atualizar: ${e.message}")
                }
            }
        }
    }
}
