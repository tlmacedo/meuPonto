// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/pendencias/PendenciasViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.pendencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.pendencias.ListarPendenciasPontoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel da tela de Pendências de Ponto.
 *
 * @author Thiago
 * @since 13.0.0
 */
@HiltViewModel
class PendenciasViewModel @Inject constructor(
    private val obterEmpregoAtivo: ObterEmpregoAtivoUseCase,
    private val listarPendencias: ListarPendenciasPontoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendenciasUiState())
    val uiState = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun onEvent(event: PendenciasEvent) {
        when (event) {
            is PendenciasEvent.Carregar -> carregar()
            is PendenciasEvent.SelecionarTab -> _uiState.update { it.copy(tabSelecionada = event.tab) }
            is PendenciasEvent.LimparErro -> _uiState.update { it.copy(mensagemErro = null) }
        }
    }

    private fun carregar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensagemErro = null) }
            try {
                val empregoId = when (val r = obterEmpregoAtivo()) {
                    is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> r.emprego.id
                    is ObterEmpregoAtivoUseCase.Resultado.NenhumEmpregoCadastrado -> {
                        _uiState.update { it.copy(isLoading = false, mensagemErro = "Nenhum emprego cadastrado") }
                        return@launch
                    }
                    is ObterEmpregoAtivoUseCase.Resultado.Erro -> {
                        _uiState.update { it.copy(isLoading = false, mensagemErro = r.mensagem) }
                        return@launch
                    }
                }

                val state = _uiState.value
                val resultado = listarPendencias(empregoId, state.dataInicio, state.dataFim)
                _uiState.update { it.copy(isLoading = false, resultado = resultado, empregoId = empregoId) }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar pendências")
                _uiState.update { it.copy(isLoading = false, mensagemErro = "Erro ao carregar pendências") }
            }
        }
    }
}
