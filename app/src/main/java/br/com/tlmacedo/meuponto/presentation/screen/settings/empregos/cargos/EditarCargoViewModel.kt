package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.cargos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.AjusteSalarial
import br.com.tlmacedo.meuponto.domain.model.HistoricoCargo
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HistoricoCargoRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel para criação/edição de cargo e salário.
 *
 * @author Thiago
 * @since 29.0.0
 */
@HiltViewModel
class EditarCargoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val empregoRepository: EmpregoRepository,
    private val historicoCargoRepository: HistoricoCargoRepository
) : ViewModel() {

    private val empregoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val cargoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_CARGO_ID) ?: -1L

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val _uiState = MutableStateFlow(EditarCargoUiState())
    val uiState: StateFlow<EditarCargoUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<EditarCargoEvent>()
    val eventos: SharedFlow<EditarCargoEvent> = _eventos.asSharedFlow()

    init {
        carregarDados()
    }

    fun onAction(action: EditarCargoAction) {
        when (action) {
            is EditarCargoAction.AlterarFuncao -> alterarFuncao(action.valor)
            is EditarCargoAction.AlterarSalarioInicial -> alterarSalarioInicial(action.valor)
            is EditarCargoAction.AbrirDataInicioPicker -> _uiState.update { it.copy(showDataInicioPicker = true) }
            is EditarCargoAction.FecharDataInicioPicker -> _uiState.update { it.copy(showDataInicioPicker = false) }
            is EditarCargoAction.AlterarDataInicio -> alterarDataInicio(action.data)
            is EditarCargoAction.AbrirDataFimPicker -> _uiState.update { it.copy(showDataFimPicker = true) }
            is EditarCargoAction.FecharDataFimPicker -> _uiState.update { it.copy(showDataFimPicker = false) }
            is EditarCargoAction.AlterarDataFim -> alterarDataFim(action.data)
            is EditarCargoAction.ToggleCargoAtual -> toggleCargoAtual(action.isAtual)
            is EditarCargoAction.AdicionarAjuste -> adicionarAjuste()
            is EditarCargoAction.RemoverAjuste -> removerAjuste(action.index)
            is EditarCargoAction.AbrirAjusteDatePicker -> _uiState.update { it.copy(ajustePickerIndex = action.index) }
            is EditarCargoAction.FecharAjusteDatePicker -> _uiState.update { it.copy(ajustePickerIndex = null) }
            is EditarCargoAction.AlterarDataAjuste -> alterarDataAjuste(action.index, action.data)
            is EditarCargoAction.AlterarSalarioAjuste -> alterarSalarioAjuste(action.index, action.valor)
            is EditarCargoAction.Salvar -> salvar()
            is EditarCargoAction.Cancelar -> viewModelScope.launch { _eventos.emit(EditarCargoEvent.Voltar) }
        }
    }

    private fun carregarDados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val emprego = empregoRepository.buscarPorId(empregoId)
                _uiState.update { it.copy(nomeEmprego = emprego?.nome ?: "") }

                if (cargoId > 0L) {
                    // Modo edição
                    val cargo = historicoCargoRepository.buscarPorId(cargoId)
                    if (cargo != null) {
                        val ajustes = historicoCargoRepository.listarAjustes(cargoId).first()
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isNovoCargo = false,
                                cargoId = cargoId,
                                funcao = cargo.funcao,
                                salarioInicialStr = cargo.salarioInicial.toString(),
                                dataInicio = cargo.dataInicio,
                                dataInicioFormatada = cargo.dataInicio.format(dateFormatter),
                                dataFim = cargo.dataFim,
                                dataFimFormatada = cargo.dataFim?.format(dateFormatter),
                                isCargoAtual = cargo.dataFim == null,
                                ajustes = ajustes.map { aj ->
                                    AjusteFormItem(
                                        id = aj.id,
                                        dataAjuste = aj.dataAjuste,
                                        dataAjusteStr = aj.dataAjuste.format(dateFormatter),
                                        novoSalarioStr = aj.novoSalario.toString()
                                    )
                                }
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                } else {
                    // Modo criação
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNovoCargo = true,
                            dataInicio = LocalDate.now(),
                            isCargoAtual = true
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar dados do cargo")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun alterarFuncao(valor: String) {
        _uiState.update { it.copy(funcao = valor, funcaoErro = null) }
        validarFormulario()
    }

    private fun alterarSalarioInicial(valor: String) {
        _uiState.update { it.copy(salarioInicialStr = valor, salarioErro = null) }
        validarFormulario()
    }

    private fun alterarDataInicio(data: LocalDate) {
        _uiState.update {
            it.copy(
                dataInicio = data,
                dataInicioFormatada = data.format(dateFormatter),
                showDataInicioPicker = false,
                dataInicioErro = null
            )
        }
        validarFormulario()
    }

    private fun alterarDataFim(data: LocalDate?) {
        _uiState.update {
            it.copy(
                dataFim = data,
                dataFimFormatada = data?.format(dateFormatter),
                showDataFimPicker = false
            )
        }
    }

    private fun toggleCargoAtual(isAtual: Boolean) {
        _uiState.update {
            it.copy(
                isCargoAtual = isAtual,
                dataFim = if (isAtual) null else it.dataFim
            )
        }
    }

    private fun adicionarAjuste() {
        val novoAjuste = AjusteFormItem(
            id = 0L,
            dataAjuste = LocalDate.now(),
            dataAjusteStr = LocalDate.now().format(dateFormatter),
            novoSalarioStr = ""
        )
        _uiState.update { it.copy(ajustes = it.ajustes + novoAjuste) }
    }

    private fun removerAjuste(index: Int) {
        _uiState.update {
            it.copy(ajustes = it.ajustes.toMutableList().also { list -> list.removeAt(index) })
        }
    }

    private fun alterarDataAjuste(index: Int, data: LocalDate) {
        _uiState.update { state ->
            val ajustes = state.ajustes.toMutableList()
            ajustes[index] = ajustes[index].copy(
                dataAjuste = data,
                dataAjusteStr = data.format(dateFormatter)
            )
            state.copy(ajustes = ajustes, ajustePickerIndex = null)
        }
    }

    private fun alterarSalarioAjuste(index: Int, valor: String) {
        _uiState.update { state ->
            val ajustes = state.ajustes.toMutableList()
            ajustes[index] = ajustes[index].copy(novoSalarioStr = valor)
            state.copy(ajustes = ajustes)
        }
    }

    private fun validarFormulario() {
        val state = _uiState.value
        val funcaoValida = state.funcao.isNotBlank()
        val salarioValido = state.salarioInicialStr.replace(",", ".").toDoubleOrNull()?.let { it > 0 } == true
        val dataInicioValida = state.dataInicio != null
        
        // Validar ajustes
        val ajustesValidos = state.ajustes.all { aj ->
            aj.novoSalarioStr.replace(",", ".").toDoubleOrNull()?.let { it > 0 } == true
        }
        
        _uiState.update { it.copy(formularioValido = funcaoValida && salarioValido && dataInicioValida && ajustesValidos) }
    }

    private fun salvar() {
        val state = _uiState.value

        // Validações
        var hasError = false
        if (state.funcao.isBlank()) {
            _uiState.update { it.copy(funcaoErro = "Informe a função/cargo") }
            hasError = true
        }
        val salario = state.salarioInicialStr.replace(",", ".").toDoubleOrNull()
        if (salario == null || salario <= 0) {
            _uiState.update { it.copy(salarioErro = "Informe um salário válido") }
            hasError = true
        }
        if (state.dataInicio == null) {
            _uiState.update { it.copy(dataInicioErro = "Informe a data de início") }
            hasError = true
        }
        
        // Validar se as datas dos ajustes são posteriores à data de início do cargo
        state.ajustes.forEachIndexed { index, ajuste ->
            if (ajuste.dataAjuste.isBefore(state.dataInicio)) {
                viewModelScope.launch { _eventos.emit(EditarCargoEvent.MostrarErro("O ajuste ${index+1} não pode ser anterior ao início do cargo")) }
                hasError = true
            }
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val cargo = HistoricoCargo(
                    id = if (state.isNovoCargo) 0L else state.cargoId,
                    empregoId = empregoId,
                    funcao = state.funcao.trim(),
                    salarioInicial = salario!!,
                    dataInicio = state.dataInicio!!,
                    dataFim = if (state.isCargoAtual) null else state.dataFim
                )

                val savedCargoId = if (state.isNovoCargo) {
                    historicoCargoRepository.salvar(cargo)
                } else {
                    historicoCargoRepository.salvar(cargo)
                    state.cargoId
                }

                // Ajustes no banco de dados para este cargo
                val ajustesAtuais = if (!state.isNovoCargo) {
                    historicoCargoRepository.listarAjustes(state.cargoId).first()
                } else emptyList()

                // IDs dos ajustes que permaneceram na UI
                val idsAjustesUI = state.ajustes.map { it.id }.toSet()

                // Excluir ajustes que foram removidos na UI
                ajustesAtuais.forEach { ajusteBD ->
                    if (ajusteBD.id !in idsAjustesUI) {
                        historicoCargoRepository.excluirAjuste(ajusteBD)
                    }
                }

                // Salvar/Atualizar ajustes da UI
                state.ajustes.forEach { ajusteForm ->
                    val novoSalario = ajusteForm.novoSalarioStr.replace(",", ".").toDoubleOrNull() ?: return@forEach
                    val ajuste = AjusteSalarial(
                        id = ajusteForm.id,
                        historicoCargoId = if (state.isNovoCargo) savedCargoId else state.cargoId,
                        dataAjuste = ajusteForm.dataAjuste,
                        novoSalario = novoSalario
                    )
                    historicoCargoRepository.salvarAjuste(ajuste)
                }

                _uiState.update { it.copy(isSaving = false) }
                _eventos.emit(
                    EditarCargoEvent.SalvoComSucesso(
                        if (state.isNovoCargo) "Cargo criado com sucesso" else "Cargo atualizado com sucesso"
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Erro ao salvar cargo")
                _uiState.update { it.copy(isSaving = false) }
                _eventos.emit(EditarCargoEvent.MostrarErro("Erro ao salvar cargo: ${e.message}"))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// DATA CLASSES
// ════════════════════════════════════════════════════════════════════════════════

data class AjusteFormItem(
    val id: Long = 0L,
    val dataAjuste: LocalDate,
    val dataAjusteStr: String,
    val novoSalarioStr: String
)

data class EditarCargoUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isNovoCargo: Boolean = true,
    val nomeEmprego: String = "",
    val cargoId: Long = 0L,
    val funcao: String = "",
    val funcaoErro: String? = null,
    val salarioInicialStr: String = "",
    val salarioErro: String? = null,
    val dataInicio: LocalDate? = null,
    val dataInicioFormatada: String = "",
    val dataInicioErro: String? = null,
    val dataFim: LocalDate? = null,
    val dataFimFormatada: String? = null,
    val isCargoAtual: Boolean = true,
    val ajustes: List<AjusteFormItem> = emptyList(),
    val ajustePickerIndex: Int? = null,
    val showDataInicioPicker: Boolean = false,
    val showDataFimPicker: Boolean = false,
    val formularioValido: Boolean = false
)

// ════════════════════════════════════════════════════════════════════════════════
// ACTIONS
// ════════════════════════════════════════════════════════════════════════════════

sealed interface EditarCargoAction {
    data class AlterarFuncao(val valor: String) : EditarCargoAction
    data class AlterarSalarioInicial(val valor: String) : EditarCargoAction
    data object AbrirDataInicioPicker : EditarCargoAction
    data object FecharDataInicioPicker : EditarCargoAction
    data class AlterarDataInicio(val data: LocalDate) : EditarCargoAction
    data object AbrirDataFimPicker : EditarCargoAction
    data object FecharDataFimPicker : EditarCargoAction
    data class AlterarDataFim(val data: LocalDate?) : EditarCargoAction
    data class ToggleCargoAtual(val isAtual: Boolean) : EditarCargoAction
    data object AdicionarAjuste : EditarCargoAction
    data class RemoverAjuste(val index: Int) : EditarCargoAction
    data class AbrirAjusteDatePicker(val index: Int) : EditarCargoAction
    data object FecharAjusteDatePicker : EditarCargoAction
    data class AlterarDataAjuste(val index: Int, val data: LocalDate) : EditarCargoAction
    data class AlterarSalarioAjuste(val index: Int, val valor: String) : EditarCargoAction
    data object Salvar : EditarCargoAction
    data object Cancelar : EditarCargoAction
}

// ════════════════════════════════════════════════════════════════════════════════
// EVENTS
// ════════════════════════════════════════════════════════════════════════════════

sealed interface EditarCargoEvent {
    data class SalvoComSucesso(val mensagem: String) : EditarCargoEvent
    data class MostrarErro(val mensagem: String) : EditarCargoEvent
    data object Voltar : EditarCargoEvent
}
