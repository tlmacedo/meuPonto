// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
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
import javax.inject.Inject

/**
 * ViewModel da tela Home.
 *
 * Gerencia o estado da tela principal, coordenando as operações
 * de registro, listagem, cálculo de resumo e banco de horas.
 *
 * @property registrarPontoUseCase Caso de uso para registrar ponto
 * @property listarPontosPorDataUseCase Caso de uso para listar pontos
 * @property calcularResumoDiaUseCase Caso de uso para calcular resumo
 * @property calcularBancoHorasUseCase Caso de uso para calcular banco de horas
 * @property determinarProximoTipoPontoUseCase Caso de uso para determinar próximo tipo
 * @property excluirPontoUseCase Caso de uso para excluir ponto
 *
 * @author Thiago
 * @since 1.0.0
 */
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

    init {
        carregarDados()
        iniciarRelogioAtualizado()
    }

    /**
     * Processa as ações do usuário.
     */
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

    /**
     * Carrega os dados da tela.
     */
    private fun carregarDados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Observa pontos do dia
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

        // Carrega banco de horas separadamente
        viewModelScope.launch {
            calcularBancoHorasUseCase().collect { banco ->
                _uiState.update { it.copy(bancoHoras = banco) }
            }
        }
    }

    /**
     * Inicia o timer para atualizar a hora atual.
     */
    private fun iniciarRelogioAtualizado() {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(horaAtual = LocalTime.now()) }
                delay(1000L) // Atualiza a cada segundo
            }
        }
    }

    /**
     * Atualiza a hora atual.
     */
    private fun atualizarHora() {
        _uiState.update { it.copy(horaAtual = LocalTime.now()) }
    }

    /**
     * Registra um ponto com o horário especificado.
     */
    private fun registrarPonto(hora: LocalTime) {
        viewModelScope.launch {
            val tipo = _uiState.value.proximoTipo
            val dataHora = LocalDateTime.of(LocalDate.now(), hora)

            registrarPontoUseCase(dataHora, tipo)
                .onSuccess {
                    _uiEvent.emit(
                        HomeUiEvent.MostrarMensagem(
                            "${tipo.descricao} registrada às ${hora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}"
                        )
                    )
                    fecharTimePicker()
                }
                .onFailure { error ->
                    _uiEvent.emit(
                        HomeUiEvent.MostrarErro(error.message ?: "Erro ao registrar ponto")
                    )
                }
        }
    }

    /**
     * Abre o dialog de seleção de horário.
     */
    private fun abrirTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = true) }
    }

    /**
     * Fecha o dialog de seleção de horário.
     */
    private fun fecharTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = false) }
    }

    /**
     * Solicita confirmação para excluir um ponto.
     */
    private fun solicitarExclusao(ponto: Ponto) {
        _uiState.update {
            it.copy(
                showDeleteConfirmDialog = true,
                pontoParaExcluir = ponto
            )
        }
    }

    /**
     * Cancela a exclusão de um ponto.
     */
    private fun cancelarExclusao() {
        _uiState.update {
            it.copy(
                showDeleteConfirmDialog = false,
                pontoParaExcluir = null
            )
        }
    }

    /**
     * Confirma a exclusão do ponto selecionado.
     */
    private fun confirmarExclusao() {
        val ponto = _uiState.value.pontoParaExcluir ?: return

        viewModelScope.launch {
            excluirPontoUseCase(ponto)
                .onSuccess {
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("Ponto excluído com sucesso"))
                }
                .onFailure { error ->
                    _uiEvent.emit(HomeUiEvent.MostrarErro(error.message ?: "Erro ao excluir"))
                }

            cancelarExclusao()
        }
    }

    /**
     * Navega para edição de ponto.
     */
    private fun navegarParaEdicao(pontoId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaEdicao(pontoId))
        }
    }

    /**
     * Navega para tela de histórico.
     */
    private fun navegarParaHistorico() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaHistorico)
        }
    }

    /**
     * Navega para tela de configurações.
     */
    private fun navegarParaConfiguracoes() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaConfiguracoes)
        }
    }
}
