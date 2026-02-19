// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ListarEmpregosUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.TrocarEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.feriado.ObterFeriadosDaDataUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularResumoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.DeterminarProximoTipoPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ExcluirPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterPontosDoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.RegistrarPontoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
 * ViewModel da tela Home.
 *
 * Gerencia o estado da tela principal do aplicativo, incluindo:
 * - Registro e listagem de pontos do dia
 * - Navegação entre datas
 * - Seleção de emprego ativo
 * - Cálculo de resumos e saldos
 * - Versão de jornada vigente
 * - Feriados do dia
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 2.8.0 - Adicionado carregamento de versão de jornada
 * @updated 3.4.0 - Adicionado suporte a feriados
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val registrarPontoUseCase: RegistrarPontoUseCase,
    private val obterPontosDoDiaUseCase: ObterPontosDoDiaUseCase,
    private val calcularResumoDiaUseCase: CalcularResumoDiaUseCase,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase,
    private val determinarProximoTipoPontoUseCase: DeterminarProximoTipoPontoUseCase,
    private val excluirPontoUseCase: ExcluirPontoUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val listarEmpregosUseCase: ListarEmpregosUseCase,
    private val trocarEmpregoAtivoUseCase: TrocarEmpregoAtivoUseCase,
    private val obterFeriadosDaDataUseCase: ObterFeriadosDaDataUseCase,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent: SharedFlow<HomeUiEvent> = _uiEvent.asSharedFlow()

    // Job para coleta de pontos (cancelável ao trocar de data/emprego)
    private var pontosCollectionJob: Job? = null
    private var bancoHorasCollectionJob: Job? = null
    private var versaoJornadaCollectionJob: Job? = null

    init {
        carregarEmpregoAtivo()
        carregarEmpregos()
        carregarPontosDoDia()
        carregarBancoHoras()
        iniciarRelogioAtualizado()
    }

    /**
     * Processa as ações do usuário.
     */
    fun onAction(action: HomeAction) {
        when (action) {
            // Ações de registro de ponto
            is HomeAction.RegistrarPontoAgora -> registrarPonto(LocalTime.now())
            is HomeAction.AbrirTimePickerDialog -> abrirTimePicker()
            is HomeAction.FecharTimePickerDialog -> fecharTimePicker()
            is HomeAction.RegistrarPontoManual -> registrarPonto(action.hora)

            // Ações de exclusão
            is HomeAction.SolicitarExclusao -> solicitarExclusao(action.ponto)
            is HomeAction.CancelarExclusao -> cancelarExclusao()
            is HomeAction.ConfirmarExclusao -> confirmarExclusao()

            // Ações de navegação por data
            is HomeAction.DiaAnterior -> navegarDiaAnterior()
            is HomeAction.ProximoDia -> navegarProximoDia()
            is HomeAction.IrParaHoje -> irParaHoje()
            is HomeAction.SelecionarData -> selecionarData(action.data)

            // Ações de emprego
            is HomeAction.AbrirSeletorEmprego -> abrirSeletorEmprego()
            is HomeAction.FecharSeletorEmprego -> fecharSeletorEmprego()
            is HomeAction.SelecionarEmprego -> selecionarEmprego(action.emprego)

            // Ações de navegação
            is HomeAction.EditarPonto -> navegarParaEdicao(action.pontoId)
            is HomeAction.NavegarParaHistorico -> navegarParaHistorico()
            is HomeAction.NavegarParaConfiguracoes -> navegarParaConfiguracoes()

            // Ações internas
            is HomeAction.AtualizarHora -> atualizarHora()
            is HomeAction.LimparErro -> limparErro()
            is HomeAction.RecarregarDados -> recarregarDados()

            // No when de onAction, adicione:
            is HomeAction.AbrirDatePicker -> abrirDatePicker()
            is HomeAction.FecharDatePicker -> fecharDatePicker()

            is HomeAction.NavegarParaNovoEmprego -> navegarParaNovoEmprego()
            is HomeAction.NavegarParaEditarEmprego -> navegarParaEditarEmprego()
            is HomeAction.AbrirMenuEmprego -> abrirMenuEmprego()
            is HomeAction.FecharMenuEmprego -> fecharMenuEmprego()

        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MENU DE EMPREGO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Abre o menu de opções do emprego.
     */
    private fun abrirMenuEmprego() {
        _uiState.update { it.copy(showEmpregoMenu = true) }
    }

    /**
     * Fecha o menu de opções do emprego.
     */
    private fun fecharMenuEmprego() {
        _uiState.update { it.copy(showEmpregoMenu = false) }
    }

    /**
     * Navega para a tela de criar novo emprego.
     */
    private fun navegarParaNovoEmprego() {
        viewModelScope.launch {
            fecharMenuEmprego()
            _uiEvent.emit(HomeUiEvent.NavegarParaNovoEmprego)
        }
    }

    /**
     * Navega para a tela de editar o emprego ativo.
     */
    private fun navegarParaEditarEmprego() {
        val empregoId = _uiState.value.empregoAtivo?.id ?: return
        viewModelScope.launch {
            fecharMenuEmprego()
            _uiEvent.emit(HomeUiEvent.NavegarParaEditarEmprego(empregoId))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // CARREGAMENTO DE DADOS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Carrega o emprego ativo e inicia a observação dos dados.
     */
    private fun carregarEmpregoAtivo() {
        viewModelScope.launch {
            obterEmpregoAtivoUseCase.observar().collect { emprego ->
                val empregoAnterior = _uiState.value.empregoAtivo
                _uiState.update { it.copy(empregoAtivo = emprego) }

                // Recarrega dados se o emprego mudou
                if (emprego != null && empregoAnterior?.id != emprego.id) {
                    carregarPontosDoDia()  // Isso já carrega versão, banco e feriados
                    carregarBancoHoras()
                }
            }
        }
    }

    /**
     * Carrega a lista de empregos disponíveis.
     */
    private fun carregarEmpregos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEmpregos = true) }
            listarEmpregosUseCase.observarAtivos().collect { empregosComResumo ->
                _uiState.update {
                    it.copy(
                        empregosDisponiveis = empregosComResumo.map { er -> er.emprego },
                        isLoadingEmpregos = false
                    )
                }
            }
        }
    }

    /**
     * Carrega os pontos do dia selecionado.
     *
     * IMPORTANTE: Busca o horário da versão de jornada correta para a data,
     * garantindo que a carga horária exibida corresponda à configuração vigente.
     *
     * @updated 2.8.0 - Corrigido para buscar horário da versão de jornada correta
     * @updated 3.4.0 - Adicionado carregamento de feriados
     */
    private fun carregarPontosDoDia() {
        pontosCollectionJob?.cancel()

        val data = _uiState.value.dataSelecionada
        val empregoId = _uiState.value.empregoAtivo?.id

        pontosCollectionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Buscar feriados do dia
            val feriados = obterFeriadosDaDataUseCase(data, empregoId)
            _uiState.update { it.copy(feriadosDoDia = feriados) }

            // 2. Buscar a versão de jornada vigente para a data selecionada
            val versaoJornada = empregoId?.let {
                versaoJornadaRepository.buscarPorEmpregoEData(it, data)
            }

            // 3. Buscar configuração do dia da semana DA VERSÃO CORRETA
            val diaSemana = DiaSemana.fromDayOfWeek(data.dayOfWeek)
            val horarioDia = versaoJornada?.id?.let { versaoId ->
                horarioDiaSemanaRepository.buscarPorVersaoEDia(versaoId, diaSemana)
            } ?: empregoId?.let {
                // Fallback: se não tiver versão, busca pelo emprego (compatibilidade)
                horarioDiaSemanaRepository.buscarPorEmpregoEDia(it, diaSemana)
            }

            // 4. Atualizar versão de jornada no estado
            _uiState.update { it.copy(versaoJornadaAtual = versaoJornada) }

            obterPontosDoDiaUseCase(data).collect { pontos ->
                val resumo = calcularResumoDiaUseCase(pontos, data, horarioDia)
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
    }

    /**
     * Carrega o banco de horas até a data selecionada.
     * O saldo é dinâmico: calculado desde o último fechamento até a data visualizada.
     */
    private fun carregarBancoHoras() {
        bancoHorasCollectionJob?.cancel()

        val empregoId = _uiState.value.empregoAtivo?.id ?: return
        val dataSelecionada = _uiState.value.dataSelecionada

        bancoHorasCollectionJob = viewModelScope.launch {
            calcularBancoHorasUseCase(
                empregoId = empregoId,
                ateData = dataSelecionada
            ).collect { resultado ->
                _uiState.update { it.copy(bancoHoras = resultado.bancoHoras) }
            }
        }
    }

    /**
     * Recarrega todos os dados da tela.
     */
    private fun recarregarDados() {
        carregarPontosDoDia()
        carregarBancoHoras()
    }

    // ══════════════════════════════════════════════════════════════════════
    // RELÓGIO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Inicia o timer que atualiza a hora a cada segundo.
     */
    private fun iniciarRelogioAtualizado() {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(horaAtual = LocalTime.now()) }
                delay(1000L)
            }
        }
    }

    /**
     * Atualiza a hora manualmente.
     */
    private fun atualizarHora() {
        _uiState.update { it.copy(horaAtual = LocalTime.now()) }
    }

    // ══════════════════════════════════════════════════════════════════════
    // NAVEGAÇÃO POR DATA
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Navega para o dia anterior.
     */
    private fun navegarDiaAnterior() {
        val novaData = _uiState.value.dataSelecionada.minusDays(1)
        if (_uiState.value.podeNavegaAnterior) {
            selecionarData(novaData)
        }
    }

    /**
     * Navega para o próximo dia.
     */
    private fun navegarProximoDia() {
        val novaData = _uiState.value.dataSelecionada.plusDays(1)
        if (_uiState.value.podeNavegarProximo) {
            selecionarData(novaData)
        }
    }

    /**
     * Navega para a data de hoje.
     */
    private fun irParaHoje() {
        selecionarData(LocalDate.now())
    }

    /**
     * Seleciona uma data específica e recarrega os dados.
     * O banco de horas é recalculado até a nova data selecionada.
     * A versão de jornada é atualizada automaticamente em carregarPontosDoDia().
     */
    private fun selecionarData(data: LocalDate) {
        _uiState.update { it.copy(dataSelecionada = data) }
        carregarPontosDoDia()  // Já carrega versão de jornada e feriados
        carregarBancoHoras()
    }

    // ══════════════════════════════════════════════════════════════════════
    // SELEÇÃO DE EMPREGO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Abre o seletor de emprego.
     */
    private fun abrirSeletorEmprego() {
        _uiState.update { it.copy(showEmpregoSelector = true) }
    }

    /**
     * Fecha o seletor de emprego.
     */
    private fun fecharSeletorEmprego() {
        _uiState.update { it.copy(showEmpregoSelector = false) }
    }

    /**
     * Seleciona um emprego como ativo.
     */
    private fun selecionarEmprego(emprego: Emprego) {
        viewModelScope.launch {
            when (val resultado = trocarEmpregoAtivoUseCase(emprego)) {
                is TrocarEmpregoAtivoUseCase.Resultado.Sucesso -> {
                    fecharSeletorEmprego()
                    _uiEvent.emit(HomeUiEvent.EmpregoTrocado(emprego.nome))
                    // Recarrega dados após troca de emprego
                    recarregarDados()
                }
                is TrocarEmpregoAtivoUseCase.Resultado.NaoEncontrado -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Emprego não encontrado"))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.EmpregoIndisponivel -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Emprego indisponível"))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // REGISTRO DE PONTO
    // ══════════════════════════════════════════════════════════════════════

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
     * Registra um ponto com o horário especificado.
     */
    private fun registrarPonto(hora: LocalTime) {
        val empregoId = _uiState.value.empregoAtivo?.id
        if (empregoId == null) {
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("Nenhum emprego ativo selecionado"))
            }
            return
        }

        viewModelScope.launch {
            val data = _uiState.value.dataSelecionada
            val dataHora = LocalDateTime.of(data, hora)

            val parametros = RegistrarPontoUseCase.Parametros(
                empregoId = empregoId,
                dataHora = dataHora
            )

            when (val resultado = registrarPontoUseCase(parametros)) {
                is RegistrarPontoUseCase.Resultado.Sucesso -> {
                    val horaFormatada = hora.format(DateTimeFormatter.ofPattern("HH:mm"))
                    val tipoDescricao = _uiState.value.proximoTipo.descricao
                    _uiEvent.emit(
                        HomeUiEvent.MostrarMensagem("$tipoDescricao registrada às $horaFormatada")
                    )
                    fecharTimePicker()
                }
                is RegistrarPontoUseCase.Resultado.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
                is RegistrarPontoUseCase.Resultado.Validacao -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.erros.joinToString("\n")))
                }
                is RegistrarPontoUseCase.Resultado.SemEmpregoAtivo -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Nenhum emprego ativo configurado"))
                }
                is RegistrarPontoUseCase.Resultado.HorarioInvalido -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.motivo))
                }
                is RegistrarPontoUseCase.Resultado.LimiteAtingido -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Limite de ${resultado.limite} pontos atingido"))
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // EXCLUSÃO DE PONTO
    // ══════════════════════════════════════════════════════════════════════

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
     * Cancela a exclusão do ponto.
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
     * Confirma e executa a exclusão do ponto.
     */
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

    // ══════════════════════════════════════════════════════════════════════
    // NAVEGAÇÃO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Navega para a tela de edição de ponto.
     */
    private fun navegarParaEdicao(pontoId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaEdicao(pontoId))
        }
    }

    /**
     * Navega para a tela de histórico.
     */
    private fun navegarParaHistorico() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaHistorico)
        }
    }

    /**
     * Navega para a tela de configurações.
     */
    private fun navegarParaConfiguracoes() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaConfiguracoes)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Limpa a mensagem de erro atual.
     */
    private fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DATE PICKER
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Abre o DatePicker para seleção de data.
     */
    private fun abrirDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    /**
     * Fecha o DatePicker.
     */
    private fun fecharDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

}
