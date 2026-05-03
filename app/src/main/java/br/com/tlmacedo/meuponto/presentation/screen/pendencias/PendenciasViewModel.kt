package br.com.tlmacedo.meuponto.presentation.screen.pendencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.PendenciaDia
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.pendencias.CalcularSaudeDoEmpregoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.pendencias.ListarPendenciasPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.pendencias.ResolverInconsistenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.pendencias.SugerirJustificativaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PendenciasViewModel @Inject constructor(
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val listarPendenciasPontoUseCase: ListarPendenciasPontoUseCase,
    private val sugerirJustificativaUseCase: SugerirJustificativaUseCase,
    private val resolverInconsistenciaUseCase: ResolverInconsistenciaUseCase,
    private val calcularSaudeDoEmpregoUseCase: CalcularSaudeDoEmpregoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendenciasUiState())
    val uiState: StateFlow<PendenciasUiState> = _uiState.asStateFlow()

    init {
        onEvent(PendenciasEvent.Carregar)
    }

    fun onEvent(event: PendenciasEvent) {
        when (event) {
            PendenciasEvent.Carregar -> carregarPendencias()
            is PendenciasEvent.SelecionarTab -> {
                _uiState.update { it.copy(tabSelecionada = event.tab) }
            }
            PendenciasEvent.LimparErro -> _uiState.update { it.copy(mensagemErro = null) }
            PendenciasEvent.LimparSucesso -> _uiState.update { it.copy(mensagemSucesso = null) }
            is PendenciasEvent.AbrirDialogoJustificativa -> abrirDialogoJustificativa(event.pendencia)
            PendenciasEvent.FecharDialogoJustificativa -> _uiState.update { it.copy(dialogoJustificativa = null) }
            is PendenciasEvent.AlterarTextoJustificativa -> {
                _uiState.update { state ->
                    state.copy(
                        dialogoJustificativa = state.dialogoJustificativa?.copy(textoAtual = event.texto)
                    )
                }
            }
            is PendenciasEvent.SelecionarSugestao -> {
                _uiState.update { state ->
                    state.copy(
                        dialogoJustificativa = state.dialogoJustificativa?.copy(textoAtual = event.sugestao)
                    )
                }
            }
            is PendenciasEvent.ConfirmarJustificativa -> salvarJustificativa(event.data, event.justificativa)
        }
    }

    private fun carregarPendencias() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val emprego = obterEmpregoAtivoUseCase.observar().first()
            if (emprego == null) {
                _uiState.update { it.copy(isLoading = false, mensagemErro = "Nenhum emprego ativo selecionado") }
                return@launch
            }

            try {
                val resultado = listarPendenciasPontoUseCase(
                    empregoId = emprego.id,
                    dataInicio = _uiState.value.dataInicio,
                    dataFim = _uiState.value.dataFim
                )
                val saude = calcularSaudeDoEmpregoUseCase(emprego.id)

                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        resultado = resultado,
                        saude = saude,
                        empregoId = emprego.id
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, mensagemErro = "Erro ao carregar pendências: ${e.message}") }
            }
        }
    }

    private fun abrirDialogoJustificativa(pendencia: PendenciaDia) {
        val sugestoes = sugerirJustificativaUseCase(pendencia.inconsistencias.map { it.inconsistencia })
        _uiState.update {
            it.copy(
                dialogoJustificativa = DialogoJustificativaState(
                    pendencia = pendencia,
                    sugestoes = sugestoes
                )
            )
        }
    }

    private fun salvarJustificativa(data: LocalDate, justificativa: String) {
        val empregoId = _uiState.value.empregoId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSalvandoJustificativa = true) }
            
            when (val resultado = resolverInconsistenciaUseCase(empregoId, data, justificativa)) {
                is ResolverInconsistenciaUseCase.Resultado.Sucesso -> {
                    _uiState.update { 
                        it.copy(
                            isSalvandoJustificativa = false,
                            dialogoJustificativa = null,
                            mensagemSucesso = "Justificativa salva com sucesso"
                        )
                    }
                    carregarPendencias()
                }
                is ResolverInconsistenciaUseCase.Resultado.Erro -> {
                    _uiState.update { 
                        it.copy(
                            isSalvandoJustificativa = false,
                            mensagemErro = resultado.mensagem
                        )
                    }
                }
            }
        }
    }
}
