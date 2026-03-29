package br.com.tlmacedo.meuponto.presentation.screen.settings.feriados.editar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.feriado.AbrangenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.RecorrenciaFeriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.MonthDay
import javax.inject.Inject

@HiltViewModel
class EditarFeriadoViewModel @Inject constructor(
    private val feriadoRepository: FeriadoRepository,
    private val empregoRepository: EmpregoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarFeriadoUiState())
    val uiState: StateFlow<EditarFeriadoUiState> = _uiState.asStateFlow()

    private val feriadoId: Long? = savedStateHandle.get<Long>(MeuPontoDestinations.ARG_FERIADO_ID)?.takeIf { it > 0 }
    private var feriadoOriginal: Feriado? = null

    init {
        carregarEmpregos()
        feriadoId?.let { carregarFeriado(it) }
    }

    private fun carregarEmpregos() {
        viewModelScope.launch {
            empregoRepository.observarAtivos().collect { empregos ->
                _uiState.update { it.copy(empregosDisponiveis = empregos) }
            }
        }
    }

    private fun carregarFeriado(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                feriadoRepository.buscarPorId(id)?.let { feriado ->
                    feriadoOriginal = feriado
                    val emprego = feriado.empregoId?.let { empregoId -> empregoRepository.buscarPorId(empregoId) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            feriadoId = feriado.id,
                            nome = feriado.nome,
                            tipo = feriado.tipo,
                            recorrencia = feriado.recorrencia,
                            abrangencia = feriado.abrangencia,
                            diaMes = feriado.diaMes,
                            dataEspecifica = feriado.dataEspecifica,
                            uf = feriado.uf,
                            municipio = feriado.municipio,
                            empregoSelecionado = emprego,
                            observacao = feriado.observacao ?: "",
                            ativo = feriado.ativo,
                            hasChanges = false
                        )
                    }
                } ?: run { _uiState.update { it.copy(isLoading = false, errorMessage = "Feriado não encontrado") } }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Erro ao carregar feriado: ${e.message}") }
            }
        }
    }

    fun onNomeChange(nome: String) {
        _uiState.update { it.copy(nome = nome, nomeError = if (nome.isBlank()) "Nome é obrigatório" else null, hasChanges = true) }
    }

    fun onTipoChange(tipo: TipoFeriado) {
        _uiState.update {
            it.copy(
                tipo = tipo,
                uf = if (tipo == TipoFeriado.ESTADUAL || tipo == TipoFeriado.MUNICIPAL) it.uf else null,
                municipio = if (tipo == TipoFeriado.MUNICIPAL) it.municipio else null,
                ufError = null, municipioError = null, hasChanges = true
            )
        }
    }

    fun onRecorrenciaChange(recorrencia: RecorrenciaFeriado) {
        _uiState.update {
            it.copy(
                recorrencia = recorrencia,
                diaMes = if (recorrencia == RecorrenciaFeriado.ANUAL) it.diaMes else null,
                dataEspecifica = if (recorrencia == RecorrenciaFeriado.UNICO) it.dataEspecifica else null,
                dataError = null, hasChanges = true
            )
        }
    }

    fun onAbrangenciaChange(abrangencia: AbrangenciaFeriado) {
        _uiState.update {
            it.copy(
                abrangencia = abrangencia,
                empregoSelecionado = if (abrangencia == AbrangenciaFeriado.EMPREGO_ESPECIFICO) it.empregoSelecionado else null,
                empregoError = null, hasChanges = true
            )
        }
    }

    fun onDiaMesChange(diaMes: MonthDay) { _uiState.update { it.copy(diaMes = diaMes, dataError = null, hasChanges = true) } }
    fun onDataEspecificaChange(data: LocalDate) { _uiState.update { it.copy(dataEspecifica = data, dataError = null, hasChanges = true) } }
    fun onUfChange(uf: String) { _uiState.update { it.copy(uf = uf, ufError = null, municipio = if (uf != it.uf) null else it.municipio, hasChanges = true) } }
    fun onMunicipioChange(municipio: String) { _uiState.update { it.copy(municipio = municipio, municipioError = if (municipio.isBlank()) "Município é obrigatório" else null, hasChanges = true) } }
    fun onEmpregoSelecionado(emprego: Emprego) { _uiState.update { it.copy(empregoSelecionado = emprego, empregoError = null, showEmpregoSelector = false, hasChanges = true) } }
    fun onObservacaoChange(observacao: String) { _uiState.update { it.copy(observacao = observacao, hasChanges = true) } }
    fun onAtivoChange(ativo: Boolean) { _uiState.update { it.copy(ativo = ativo, hasChanges = true) } }

    fun showDatePicker() { _uiState.update { it.copy(showDatePicker = true) } }
    fun hideDatePicker() { _uiState.update { it.copy(showDatePicker = false) } }
    fun showEmpregoSelector() { _uiState.update { it.copy(showEmpregoSelector = true) } }
    fun hideEmpregoSelector() { _uiState.update { it.copy(showEmpregoSelector = false) } }
    fun showUfSelector() { _uiState.update { it.copy(showUfSelector = true) } }
    fun hideUfSelector() { _uiState.update { it.copy(showUfSelector = false) } }
    fun showDeleteConfirmation() { _uiState.update { it.copy(showDeleteConfirmation = true) } }
    fun hideDeleteConfirmation() { _uiState.update { it.copy(showDeleteConfirmation = false) } }
    fun showDiscardConfirmation() { _uiState.update { it.copy(showDiscardConfirmation = true) } }
    fun hideDiscardConfirmation() { _uiState.update { it.copy(showDiscardConfirmation = false) } }

    fun salvar() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val feriado = Feriado(
                    id = state.feriadoId ?: 0,
                    nome = state.nome.trim(),
                    tipo = state.tipo,
                    recorrencia = state.recorrencia,
                    abrangencia = state.abrangencia,
                    diaMes = if (state.recorrencia == RecorrenciaFeriado.ANUAL) state.diaMes else null,
                    dataEspecifica = if (state.recorrencia == RecorrenciaFeriado.UNICO) state.dataEspecifica else null,
                    anoReferencia = if (state.recorrencia == RecorrenciaFeriado.UNICO) state.dataEspecifica?.year else null,
                    uf = state.uf,
                    municipio = state.municipio,
                    empregoId = if (state.abrangencia == AbrangenciaFeriado.EMPREGO_ESPECIFICO) state.empregoSelecionado?.id else null,
                    ativo = state.ativo,
                    observacao = state.observacao.trim().ifBlank { null },
                    criadoEm = feriadoOriginal?.criadoEm ?: LocalDateTime.now(),
                    atualizadoEm = LocalDateTime.now()
                )

                if (state.isEditing) feriadoRepository.atualizar(feriado) else feriadoRepository.inserir(feriado)

                _uiState.update { it.copy(isSaving = false, successMessage = if (state.isEditing) "Feriado atualizado" else "Feriado criado", shouldNavigateBack = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Erro ao salvar: ${e.message}") }
            }
        }
    }

    fun excluir() {
        val feriadoId = _uiState.value.feriadoId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, showDeleteConfirmation = false) }
            try {
                feriadoRepository.excluirPorId(feriadoId)
                _uiState.update { it.copy(isDeleting = false, successMessage = "Feriado excluído", shouldNavigateBack = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeleting = false, errorMessage = "Erro ao excluir: ${e.message}") }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearSuccess() { _uiState.update { it.copy(successMessage = null) } }
    fun onNavigateBackHandled() { _uiState.update { it.copy(shouldNavigateBack = false) } }
}
