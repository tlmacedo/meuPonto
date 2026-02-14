// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularResumoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.DeterminarProximoTipoPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ExcluirPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterPontosDoDiaUseCase
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

/**
 * ViewModel da tela principal (Home) do aplicativo.
 *
 * Responsável por gerenciar o estado da tela inicial, incluindo a lista de pontos
 * do dia atual, o resumo de horas trabalhadas, banco de horas acumulado e o
 * relógio em tempo real. Coordena as operações de registro e exclusão de pontos.
 *
 * @property registrarPontoUseCase Caso de uso para registrar novos pontos
 * @property obterPontosDoDiaUseCase Caso de uso para obter pontos do dia (reativo)
 * @property calcularResumoDiaUseCase Caso de uso para calcular resumo diário
 * @property calcularBancoHorasUseCase Caso de uso para calcular banco de horas
 * @property determinarProximoTipoPontoUseCase Caso de uso para determinar próximo tipo
 * @property excluirPontoUseCase Caso de uso para excluir pontos
 *
 * @author Thiago
 * @since 1.0.0
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val registrarPontoUseCase: RegistrarPontoUseCase,
    private val obterPontosDoDiaUseCase: ObterPontosDoDiaUseCase,
    private val calcularResumoDiaUseCase: CalcularResumoDiaUseCase,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase,
    private val determinarProximoTipoPontoUseCase: DeterminarProximoTipoPontoUseCase,
    private val excluirPontoUseCase: ExcluirPontoUseCase
) : ViewModel() {

    // Estado da UI exposto como StateFlow imutável
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Eventos únicos (navegação, snackbar, etc.) como SharedFlow
    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent: SharedFlow<HomeUiEvent> = _uiEvent.asSharedFlow()

    // ID do emprego ativo (futuramente será obtido das configurações)
    private val empregoId: Long = 1L

    init {
        // Carrega os dados iniciais ao criar o ViewModel
        carregarDados()
        // Inicia o relógio que atualiza a cada segundo
        iniciarRelogioAtualizado()
    }

    /**
     * Processa as ações enviadas pela UI.
     *
     * @param action Ação a ser processada
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
     * Carrega os dados iniciais da tela.
     *
     * Observa os pontos do dia atual de forma reativa e recalcula
     * o resumo e o próximo tipo de ponto a cada atualização.
     * Também observa o banco de horas acumulado.
     */
    private fun carregarDados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Observa os pontos do dia atual de forma reativa
            // ObterPontosDoDiaUseCase usa LocalDate.now() como padrão
            obterPontosDoDiaUseCase().collect { pontos ->
                // Calcula o resumo do dia com base nos pontos
                val resumo = calcularResumoDiaUseCase(pontos, LocalDate.now())
                // Determina qual será o próximo tipo de ponto (entrada/saída)
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
        
        // Observa o banco de horas em uma coroutine separada
        viewModelScope.launch {
            calcularBancoHorasUseCase().collect { banco ->
                _uiState.update { it.copy(bancoHoras = banco) }
            }
        }
    }

    /**
     * Inicia o relógio que atualiza a hora atual a cada segundo.
     *
     * Executa em loop infinito dentro do viewModelScope,
     * sendo automaticamente cancelado quando o ViewModel é destruído.
     */
    private fun iniciarRelogioAtualizado() {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(horaAtual = LocalTime.now()) }
                delay(1000L) // Atualiza a cada 1 segundo
            }
        }
    }

    /**
     * Atualiza manualmente a hora atual no estado.
     */
    private fun atualizarHora() {
        _uiState.update { it.copy(horaAtual = LocalTime.now()) }
    }

    /**
     * Registra um novo ponto com a hora especificada.
     *
     * Utiliza o tipo de ponto determinado automaticamente (entrada/saída)
     * e a data atual. Emite eventos de sucesso ou erro para a UI.
     *
     * @param hora Hora do ponto a ser registrado
     */
    private fun registrarPonto(hora: LocalTime) {
        viewModelScope.launch {
            val tipo = _uiState.value.proximoTipo
            val dataHora = LocalDateTime.of(LocalDate.now(), hora)
            
            // Monta os parâmetros para o caso de uso
            val parametros = RegistrarPontoUseCase.Parametros(
                empregoId = empregoId,
                dataHora = dataHora,
                tipo = tipo
            )
            
            // Processa o resultado do registro
            when (val resultado = registrarPontoUseCase(parametros)) {
                is RegistrarPontoUseCase.Resultado.Sucesso -> {
                    val horaFormatada = hora.format(DateTimeFormatter.ofPattern("HH:mm"))
                    _uiEvent.emit(
                        HomeUiEvent.MostrarMensagem("${tipo.descricao} registrada às $horaFormatada")
                    )
                    fecharTimePicker()
                }
                is RegistrarPontoUseCase.Resultado.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
                is RegistrarPontoUseCase.Resultado.Validacao -> {
                    // Junta todas as mensagens de validação em uma única string
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.erros.joinToString("\n")))
                }
            }
        }
    }

    /**
     * Abre o diálogo de seleção de hora para registro manual.
     */
    private fun abrirTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = true) }
    }

    /**
     * Fecha o diálogo de seleção de hora.
     */
    private fun fecharTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = false) }
    }

    /**
     * Solicita confirmação para excluir um ponto.
     *
     * Armazena o ponto no estado e exibe o diálogo de confirmação.
     *
     * @param ponto Ponto que será excluído após confirmação
     */
    private fun solicitarExclusao(ponto: Ponto) {
        _uiState.update { 
            it.copy(showDeleteConfirmDialog = true, pontoParaExcluir = ponto) 
        }
    }

    /**
     * Cancela a operação de exclusão e fecha o diálogo.
     */
    private fun cancelarExclusao() {
        _uiState.update { 
            it.copy(showDeleteConfirmDialog = false, pontoParaExcluir = null) 
        }
    }

    /**
     * Confirma e executa a exclusão do ponto.
     *
     * Utiliza o ponto armazenado no estado para realizar a exclusão.
     * Emite eventos de sucesso ou erro para a UI.
     */
    private fun confirmarExclusao() {
        // Obtém o ponto a ser excluído do estado
        val ponto = _uiState.value.pontoParaExcluir ?: return
        
        viewModelScope.launch {
            // Processa o resultado da exclusão
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
            // Fecha o diálogo independente do resultado
            cancelarExclusao()
        }
    }

    /**
     * Emite evento para navegar para a tela de edição de ponto.
     *
     * @param pontoId ID do ponto a ser editado
     */
    private fun navegarParaEdicao(pontoId: Long) {
        viewModelScope.launch { 
            _uiEvent.emit(HomeUiEvent.NavegarParaEdicao(pontoId)) 
        }
    }

    /**
     * Emite evento para navegar para a tela de histórico.
     */
    private fun navegarParaHistorico() {
        viewModelScope.launch { 
            _uiEvent.emit(HomeUiEvent.NavegarParaHistorico) 
        }
    }

    /**
     * Emite evento para navegar para a tela de configurações.
     */
    private fun navegarParaConfiguracoes() {
        viewModelScope.launch { 
            _uiEvent.emit(HomeUiEvent.NavegarParaConfiguracoes) 
        }
    }
}
