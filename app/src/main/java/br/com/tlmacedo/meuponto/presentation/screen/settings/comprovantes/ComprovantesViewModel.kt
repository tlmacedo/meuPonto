package br.com.tlmacedo.meuponto.presentation.screen.settings.comprovantes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.repository.FotoComprovanteRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.foto.DeleteComprovanteImageUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.presentation.screen.history.PeriodoHistorico
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ComprovantesViewModel @Inject constructor(
    private val repository: FotoComprovanteRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val deleteUseCase: DeleteComprovanteImageUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val storageManager: br.com.tlmacedo.meuponto.util.foto.FotoStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComprovantesUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<ComprovantesEvent>()
    val eventos = _eventos.asSharedFlow()

    init {
        carregarComprovantes()
        atualizarEstatisticas()
    }

    fun onAction(action: ComprovantesAction) {
        when (action) {
            is ComprovantesAction.AlterarPeriodo -> {
                _uiState.update { it.copy(dataInicio = action.inicio, dataFim = action.fim, isFiltroPersonalizado = true) }
                carregarComprovantes()
            }
            is ComprovantesAction.AlterarFiltroAssociacao -> {
                _uiState.update { it.copy(filtroAssociacao = action.filtro) }
                carregarComprovantes()
            }
            is ComprovantesAction.SelecionarComprovante -> {
                _uiState.update { it.copy(selectedItem = action.comprovante) }
            }
            is ComprovantesAction.AlternarSelecao -> {
                _uiState.update { state ->
                    val newSelection = state.selectedIds.toMutableSet()
                    if (newSelection.contains(action.id)) {
                        newSelection.remove(action.id)
                    } else {
                        newSelection.add(action.id)
                    }
                    state.copy(selectedIds = newSelection)
                }
            }
            ComprovantesAction.LimparSelecao -> {
                _uiState.update { it.copy(selectedIds = emptySet()) }
            }
            ComprovantesAction.ExcluirSelecionados -> excluirSelecionados()
            is ComprovantesAction.ExcluirComprovante -> excluirComprovante(action.id)
            ComprovantesAction.LimparCache -> limparArquivosOrfaos()
            ComprovantesAction.Refresh -> carregarComprovantes()
            ComprovantesAction.AnalisarFotosLocais -> analisarFotosLocais()
        }
    }

    private fun analisarFotosLocais() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val empregoId = _uiState.value.empregoAtivo?.id ?: return@launch
                val pathsNoBanco = repository.listarPathsPorEmprego(empregoId).toSet()
                val removidos = storageManager.cleanupOrphanFiles(pathsNoBanco)
                
                carregarComprovantes()
                _eventos.emit(ComprovantesEvent.ShowError("Análise concluída. $removidos arquivos órfãos removidos."))
            } catch (e: Exception) {
                _eventos.emit(ComprovantesEvent.ShowError("Erro ao analisar arquivos: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun limparArquivosOrfaos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Implementação simplificada de limpeza de órfãos
                val todosPathsNoBanco = repository.calcularTamanhoTotal() // Apenas para trigger
                // O deleteUseCase.cleanupOrphans precisaria de uma lista de todos os paths
                // Vamos implementar isso no repositório se necessário
                _eventos.emit(ComprovantesEvent.ShowError("Limpeza de arquivos órfãos não implementada totalmente"))
            } catch (e: Exception) {
                _eventos.emit(ComprovantesEvent.ShowError("Erro na limpeza: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun carregarComprovantes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Timber.d("Iniciando carregarComprovantes")

            obterEmpregoAtivoUseCase.observar().collectLatest { emprego ->
                _uiState.update { it.copy(empregoAtivo = emprego) }
                var empregoId = emprego?.id

                if (empregoId == null) {
                    val fallbackId = repository.listarPorEmprego(0L).firstOrNull()?.firstOrNull()?.empregoId
                    Timber.d("Tentando fallback para empregoId: $fallbackId")
                    empregoId = fallbackId
                }

                if (empregoId == null) {
                    Timber.w("EmpregoId continua nulo mesmo apos fallback, abortando carregamento")
                    _uiState.update { it.copy(items = emptyList(), isLoading = false) }
                    return@collectLatest
                }

                // Ajusta o período inicial baseado no dia de fechamento do RH
                val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId)
                val diaInicioRH = versaoVigente?.diaInicioFechamentoRH ?: 1
                
                _uiState.update { state ->
                    if (state.isFiltroPersonalizado) {
                        state
                    } else {
                        val periodoRH = PeriodoHistorico.periodoAtual(diaInicioRH)
                        state.copy(
                            dataInicio = periodoRH.dataInicio,
                            dataFim = periodoRH.dataFim
                        )
                    }
                }

                val state = _uiState.value
                Timber.d("Buscando fotos para empregoId: $empregoId, periodo: ${state.dataInicio} ate ${state.dataFim}")
                repository.listarPorEmpregoEPeriodo(
                    empregoId,
                    state.dataInicio,
                    state.dataFim
                ).collectLatest { lista ->
                    Timber.d("Fotos retornadas do banco para emprego $empregoId: ${lista.size}")
                    
                    val listaFiltrada = when (_uiState.value.filtroAssociacao) {
                        FiltroAssociacao.TODOS -> lista
                        FiltroAssociacao.COM_PONTO -> lista.filter { it.pontoId != 0L }
                        FiltroAssociacao.SEM_PONTO -> lista.filter { it.pontoId == 0L }
                    }
                    
                    _uiState.update { it.copy(items = listaFiltrada, isLoading = false) }
                }
            }
        }
    }

    private fun excluirSelecionados() {
        viewModelScope.launch {
            val idsParaExcluir = _uiState.value.selectedIds
            if (idsParaExcluir.isEmpty()) return@launch
            
            _uiState.update { it.copy(isLoading = true) }
            try {
                idsParaExcluir.forEach { id ->
                    val foto = repository.buscarPorId(id)
                    foto?.let {
                        deleteUseCase.deleteFileOnly(it.fotoPath)
                        repository.excluir(id)
                    }
                }
                _uiState.update { it.copy(selectedIds = emptySet()) }
                _eventos.emit(ComprovantesEvent.ComprovanteExcluido)
                atualizarEstatisticas()
            } catch (e: Exception) {
                _eventos.emit(ComprovantesEvent.ShowError("Erro ao excluir selecionados: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun atualizarEstatisticas() {
        viewModelScope.launch {
            val total = repository.calcularTamanhoTotal()
            _uiState.update { it.copy(storageUsageBytes = total) }
        }
    }

    private fun excluirComprovante(id: Long) {
        viewModelScope.launch {
            try {
                val foto = repository.buscarPorId(id) ?: return@launch
                deleteUseCase.deleteFileOnly(foto.fotoPath)
                repository.excluir(id)
                _eventos.emit(ComprovantesEvent.ComprovanteExcluido)
                atualizarEstatisticas()
            } catch (e: Exception) {
                _eventos.emit(ComprovantesEvent.ShowError("Erro ao excluir comprovante: ${e.message}"))
            }
        }
    }
}
