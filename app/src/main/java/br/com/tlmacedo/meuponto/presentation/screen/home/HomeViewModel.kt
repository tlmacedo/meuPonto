package br.com.tlmacedo.meuponto.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularResumoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.DeterminarProximoTipoPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ExcluirPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ListarPontosPorDataUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.RegistrarPontoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val registrarPontoUseCase: RegistrarPontoUseCase,
    private val listarPontosPorDataUseCase: ListarPontosPorDataUseCase,
    private val calcularResumoDiaUseCase: CalcularResumoDiaUseCase,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase,
    private val determinarProximoTipoPontoUseCase: DeterminarProximoTipoPontoUseCase,
    private val excluirPontoUseCase: ExcluirPontoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent: SharedFlow<HomeUiEvent> = _uiEvent.asSharedFlow()

    private val empregoId: Long = 1L

    init {
        carregarDados()
        iniciarRelogioAtualizado()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.RegistrarPontoAgora -> registrarPonto(LocalTime.now())
            is HomeAction.AbrirTimePickerDialog -> abrirTimePicker()
            is HomeAction.FecharTimePickerDialog -> fecharTimePicker()
            is HomeAction.RegistrarPontoManual -> registrarPonto(action.hora)
            is HomeAction.SolicitarExclusao -> solicitarExclusao(action.ponto)
            is HomeAction.CancelarExclusao -> cancelarExclusao()
            is HomeAction.ConfirmarExclusao -> confirmarExclusao()
            is HomeAction.EditarPonto -> navegarParaEdicao(action.pontoId)
            is HomeAction.NavegarParaHistorico -> navegarParaHistorico()
            is HomeAction.NavegarParaConfiguracoes -> navegarParaConfiguracoes()
            is HomeAction.AtualizarHora -> atualizarHora()
        }
    }

    private fun carregarDados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            listarPontosPorDataUseCase(LocalDate.now()).collect { pontos ->
                val resumo = calcularResumoDiaUseCase(pontos, LocalDate.now())
                val proximoTipo = determinarProximoTipoPontoUseCase(pontos)
                _uiState.update {
                    it.copy(
                        pontosHoje = pontos,
                        resumoDia = resumo,
                        proximoTipo = proximoTipo,
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            calcularBancoHorasUseCase().collect { banco ->
                _uiState.update { it.copy(bancoHoras = banco) }
            }
        }
    }

    private fun iniciarRelogioAtualizado() {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(horaAtual = LocalTime.now()) }
                delay(1000L)
            }
        }
    }

    private fun atualizarHora() {
        _uiState.update { it.copy(horaAtual = LocalTime.now()) }
    }

    private fun registrarPonto(hora: LocalTime) {
        viewModelScope.launch {
            val tipo = _uiState.value.proximoTipo
            val dataHora = LocalDateTime.of(LocalDate.now(), hora)
            val parametros = RegistrarPontoUseCase.Parametros(
                empregoId = empregoId,
                dataHora = dataHora,
                tipo = tipo
            )
            when (val resultado = registrarPontoUseCase(parametros)) {
                is RegistrarPontoUseCase.Resultado.Sucesso -> {
                    val horaFormatada = hora.format(DateTimeFormatter.ofPattern("HH:mm"))
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("${tipo.descricao} registrada às $horaFormatada"))
                    fecharTimePicker()
                }
                is RegistrarPontoUseCase.Resultado.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
                is RegistrarPontoUseCase.Resultado.Validacao -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.erros.joinToString("\n")))
                }
            }
        }
    }

    private fun abrirTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = true) }
    }

    private fun fecharTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = false) }
    }

    private fun solicitarExclusao(ponto: Ponto) {
        _uiState.update { it.copy(showDeleteConfirmDialog = true, pontoParaExcluir = ponto) }
    }

    private fun cancelarExclusao() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false, pontoParaExcluir = null) }
    }

    private fun confirmarExclusao() {
        val ponto = _uiState.value.pontoParaExcluir ?: return
        viewModelScope.launch {
            when (val resultado = excluirPontoUseCase(ponto.id)) {
                is ExcluirPontoUseCase.Resultado.Sucesso -> {
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("Ponto excluído com sucesso"))
                }
                is ExcluirPontoUseCase.Resultado.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
                is ExcluirPontoUseCase.Resultado.NaoEncontrado -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Ponto não encontrado"))
                }
            }
            cancelarExclusao()
        }
    }

    private fun navegarParaEdicao(pontoId: Long) {
        viewModelScope.launch { _uiEvent.emit(HomeUiEvent.NavegarParaEdicao(pontoId)) }
    }

    private fun navegarParaHistorico() {
        viewModelScope.launch { _uiEvent.emit(HomeUiEvent.NavegarParaHistorico) }
    }

    private fun navegarParaConfiguracoes() {
        viewModelScope.launch { _uiEvent.emit(HomeUiEvent.NavegarParaConfiguracoes) }
    }
}