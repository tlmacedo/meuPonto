package br.com.tlmacedo.meuponto.presentation.screen.settings.comprovantes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.repository.FotoComprovanteRepository
import br.com.tlmacedo.meuponto.domain.usecase.foto.DeleteComprovanteImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComprovantesViewModel @Inject constructor(
    private val repository: FotoComprovanteRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val deleteUseCase: DeleteComprovanteImageUseCase
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
                _uiState.update { it.copy(dataInicio = action.inicio, dataFim = action.fim) }
                carregarComprovantes()
            }
            is ComprovantesAction.SelecionarComprovante -> {
                _uiState.update { it.copy(selectedItem = action.comprovante) }
            }
            is ComprovantesAction.ExcluirComprovante -> excluirComprovante(action.id)
            ComprovantesAction.LimparCache -> { /* TODO: Implementar limpeza de órfãos */ }
            ComprovantesAction.Refresh -> carregarComprovantes()
        }
    }

    private fun carregarComprovantes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val empregoId = preferenciasRepository.obterEmpregoAtivoId() ?: return@launch
            
            repository.listarPorEmpregoEPeriodo(
                empregoId,
                _uiState.value.dataInicio,
                _uiState.value.dataFim
            ).collectLatest { lista ->
                _uiState.update { it.copy(items = lista, isLoading = false) }
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
