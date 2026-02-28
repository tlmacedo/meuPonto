// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.BuscarAusenciaPorDataUseCase
import br.com.tlmacedo.meuponto.domain.usecase.banco.FecharCicloUseCase
import br.com.tlmacedo.meuponto.domain.usecase.banco.InicializarCiclosRetroativosUseCase
import br.com.tlmacedo.meuponto.domain.usecase.banco.ReverterFechamentoIncorretoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.banco.VerificarCicloPendenteUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ListarEmpregosUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.TrocarEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularResumoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.DeterminarProximoTipoPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ExcluirPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterPontosDoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
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
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs

/**
 * ViewModel da tela Home.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 6.2.0 - Adicionado suporte a ciclos de banco de horas
 * @updated 6.3.0 - Adicionado suporte a reversão de fechamentos incorretos
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
    private val buscarAusenciaPorDataUseCase: BuscarAusenciaPorDataUseCase,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val obterResumoDiaCompletoUseCase: ObterResumoDiaCompletoUseCase,
    private val verificarCicloPendenteUseCase: VerificarCicloPendenteUseCase,
    private val fecharCicloUseCase: FecharCicloUseCase,
    private val inicializarCiclosRetroativosUseCase: InicializarCiclosRetroativosUseCase,
    private val reverterFechamentoIncorretoUseCase: ReverterFechamentoIncorretoUseCase,
    private val fechamentoPeriodoRepository: FechamentoPeriodoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent: SharedFlow<HomeUiEvent> = _uiEvent.asSharedFlow()

    private var pontosCollectionJob: Job? = null
    private var bancoHorasCollectionJob: Job? = null
    private var versaoJornadaCollectionJob: Job? = null

    init {
        carregarEmpregoAtivo()
        carregarEmpregos()
        carregarPontosDoDia()
        carregarBancoHoras()
        iniciarRelogioAtualizado()

        // ═══════════════════════════════════════════════════════════════════════
        // CORREÇÃO TEMPORÁRIA: Reverter fechamentos incorretos
        // TODO: Remover após executar uma vez com sucesso!
        // ═══════════════════════════════════════════════════════════════════════
        // reverterFechamentosIncorretos()
    }

    /**
     * TEMPORÁRIO: Reverte fechamentos de ciclo incorretos.
     *
     * O ciclo correto é: 11/08/2025 ~ 10/02/2026
     * A data de início do ciclo atual deve ser 11/08/2025 (pois ainda não venceu de verdade)
     *
     * REMOVER ESTE MÉTODO APÓS EXECUTAR COM SUCESSO!
     */
    private fun reverterFechamentosIncorretos() {
        viewModelScope.launch {
            // Aguardar emprego ser carregado
            delay(2000)

            val empregoId = _uiState.value.empregoAtivo?.id
            if (empregoId == null) {
                android.util.Log.w("REVERTER_DEBUG", "EmpregoId ainda é null, aguardando...")
                delay(3000)
            }

            val empregoIdFinal = _uiState.value.empregoAtivo?.id ?: return@launch

            android.util.Log.d("REVERTER_DEBUG", "Iniciando reversão para emprego: $empregoIdFinal")

            // Data de início do ciclo CORRETA (seu ciclo original)
            val dataInicioCicloCorreta = LocalDate.of(2025, 8, 11)

            when (val resultado = reverterFechamentoIncorretoUseCase(empregoIdFinal, dataInicioCicloCorreta)) {
                is ReverterFechamentoIncorretoUseCase.Resultado.Sucesso -> {
                    android.util.Log.d("REVERTER_DEBUG", "✅ SUCESSO!")
                    android.util.Log.d("REVERTER_DEBUG", "   Fechamentos removidos: ${resultado.fechamentosRemovidos}")
                    android.util.Log.d("REVERTER_DEBUG", "   Ajustes removidos: ${resultado.ajustesRemovidos}")

                    // Recarregar dados após correção
                    carregarBancoHoras()
                    verificarCicloBancoHoras()

                    _uiEvent.emit(HomeUiEvent.MostrarMensagem(
                        "Dados corrigidos! ${resultado.fechamentosRemovidos} fechamentos e ${resultado.ajustesRemovidos} ajustes removidos."
                    ))
                }
                is ReverterFechamentoIncorretoUseCase.Resultado.Erro -> {
                    android.util.Log.e("REVERTER_DEBUG", "❌ ERRO: ${resultado.mensagem}")
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.RegistrarPontoAgora -> iniciarRegistroPonto(LocalTime.now())
            is HomeAction.AbrirTimePickerDialog -> abrirTimePicker()
            is HomeAction.FecharTimePickerDialog -> fecharTimePicker()
            is HomeAction.RegistrarPontoManual -> iniciarRegistroPonto(action.hora)
            is HomeAction.AtualizarNsr -> atualizarNsr(action.nsr)
            is HomeAction.ConfirmarRegistroComNsr -> confirmarRegistroComNsr()
            is HomeAction.CancelarNsrDialog -> cancelarNsrDialog()
            is HomeAction.EditarPonto -> {
                viewModelScope.launch {
                    _uiEvent.emit(HomeUiEvent.NavegarParaEditarPonto(action.pontoId))
                }
            }
            is HomeAction.SolicitarExclusao -> solicitarExclusao(action.ponto)
            is HomeAction.CancelarExclusao -> cancelarExclusao()
            is HomeAction.AtualizarMotivoExclusao -> atualizarMotivoExclusao(action.motivo)
            is HomeAction.ConfirmarExclusao -> confirmarExclusao()
            is HomeAction.DiaAnterior -> navegarDiaAnterior()
            is HomeAction.ProximoDia -> navegarProximoDia()
            is HomeAction.IrParaHoje -> irParaHoje()
            is HomeAction.SelecionarData -> selecionarData(action.data)
            is HomeAction.AbrirSeletorEmprego -> abrirSeletorEmprego()
            is HomeAction.FecharSeletorEmprego -> fecharSeletorEmprego()
            is HomeAction.SelecionarEmprego -> selecionarEmprego(action.emprego)
            is HomeAction.NavegarParaHistorico -> navegarParaHistorico()
            is HomeAction.NavegarParaConfiguracoes -> navegarParaConfiguracoes()
            is HomeAction.AtualizarHora -> atualizarHora()
            is HomeAction.LimparErro -> limparErro()
            is HomeAction.RecarregarDados -> recarregarDados()
            is HomeAction.AbrirDatePicker -> abrirDatePicker()
            is HomeAction.FecharDatePicker -> fecharDatePicker()
            is HomeAction.NavegarParaNovoEmprego -> navegarParaNovoEmprego()
            is HomeAction.NavegarParaEditarEmprego -> navegarParaEditarEmprego()
            is HomeAction.AbrirMenuEmprego -> abrirMenuEmprego()
            is HomeAction.FecharMenuEmprego -> fecharMenuEmprego()
            is HomeAction.AbrirDialogFechamentoCiclo -> abrirDialogFechamentoCiclo()
            is HomeAction.FecharDialogFechamentoCiclo -> fecharDialogFechamentoCiclo()
            is HomeAction.ConfirmarFechamentoCiclo -> confirmarFechamentoCiclo()
            is HomeAction.NavegarParaHistoricoCiclos -> navegarParaHistoricoCiclos()
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // CICLO DE BANCO DE HORAS
    // ══════════════════════════════════════════════════════════════════════

    private fun verificarCicloBancoHoras() {
        viewModelScope.launch {
            val empregoId = _uiState.value.empregoAtivo?.id
            android.util.Log.d("CICLO_DEBUG", "verificarCicloBancoHoras - empregoId: $empregoId")

            if (empregoId == null) {
                android.util.Log.d("CICLO_DEBUG", "empregoId é null, retornando")
                return@launch
            }

            // Inicializar ciclos retroativos - DESABILITADO TEMPORARIAMENTE
             val resultadoInit = inicializarCiclosRetroativosUseCase(empregoId)
             android.util.Log.d("CICLO_DEBUG", "inicializarCiclos resultado: $resultadoInit")

            // Verificar estado do ciclo atual
            val resultado = verificarCicloPendenteUseCase(empregoId)
            android.util.Log.d("CICLO_DEBUG", "verificarCicloPendente resultado: $resultado")

            when (resultado) {
                is VerificarCicloPendenteUseCase.Resultado.CicloPendente -> {
                    android.util.Log.d("CICLO_DEBUG", "CICLO PENDENTE detectado!")
                    _uiState.update {
                        it.copy(
                            estadoCiclo = EstadoCiclo.Pendente(
                                ciclo = resultado.ciclo,
                                diasAposVencimento = resultado.diasAposVencimento
                            )
                        )
                    }
                }

                is VerificarCicloPendenteUseCase.Resultado.CicloProximoDoFim -> {
                    _uiState.update {
                        it.copy(
                            estadoCiclo = EstadoCiclo.ProximoDoFim(
                                ciclo = resultado.ciclo,
                                diasRestantes = resultado.diasRestantes
                            )
                        )
                    }
                }

                is VerificarCicloPendenteUseCase.Resultado.CicloEmAndamento -> {
                    _uiState.update {
                        it.copy(
                            estadoCiclo = EstadoCiclo.EmAndamento(
                                ciclo = resultado.ciclo,
                                diasRestantes = resultado.diasRestantes
                            )
                        )
                    }
                }

                is VerificarCicloPendenteUseCase.Resultado.SemConfiguracao,
                is VerificarCicloPendenteUseCase.Resultado.BancoNaoHabilitado,
                is VerificarCicloPendenteUseCase.Resultado.CicloNaoConfigurado -> {
                    _uiState.update { it.copy(estadoCiclo = EstadoCiclo.Nenhum) }
                }
            }
        }
    }

    private fun abrirDialogFechamentoCiclo() {
        _uiState.update { it.copy(showFechamentoCicloDialog = true) }
    }

    private fun fecharDialogFechamentoCiclo() {
        _uiState.update { it.copy(showFechamentoCicloDialog = false) }
    }

    private fun confirmarFechamentoCiclo() {
        viewModelScope.launch {
            val empregoId = _uiState.value.empregoAtivo?.id ?: return@launch

            _uiState.update { it.copy(isLoading = true, showFechamentoCicloDialog = false) }

            when (val resultado = fecharCicloUseCase.fecharCiclosPendentes(empregoId)) {
                is FecharCicloUseCase.ResultadoMultiplo.Sucesso -> {
                    val qtd = resultado.ciclosFechados.size
                    val saldoTotal = resultado.ciclosFechados.sumOf { it.saldoAtualMinutos }

                    _uiEvent.emit(
                        HomeUiEvent.MostrarMensagem(
                            if (qtd == 1) {
                                "Ciclo fechado. Saldo zerado: ${formatarMinutos(saldoTotal)}"
                            } else {
                                "$qtd ciclos fechados. Saldo total zerado: ${formatarMinutos(saldoTotal)}"
                            }
                        )
                    )

                    resultado.novoCiclo?.let { novoCiclo ->
                        _uiState.update {
                            it.copy(
                                estadoCiclo = EstadoCiclo.EmAndamento(
                                    ciclo = novoCiclo,
                                    diasRestantes = ChronoUnit.DAYS
                                        .between(LocalDate.now(), novoCiclo.dataFim).toInt()
                                )
                            )
                        }
                    }

                    carregarBancoHoras()
                }

                is FecharCicloUseCase.ResultadoMultiplo.NenhumPendente -> {
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("Nenhum ciclo pendente"))
                }

                is FecharCicloUseCase.ResultadoMultiplo.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun navegarParaHistoricoCiclos() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaHistoricoCiclos)
        }
    }

    private fun formatarMinutos(minutos: Int): String {
        val sinal = if (minutos >= 0) "+" else "-"
        val total = abs(minutos)
        val horas = total / 60
        val mins = total % 60
        return "$sinal${String.format("%02d:%02d", horas, mins)}"
    }

    // ══════════════════════════════════════════════════════════════════════
    // MENU DE EMPREGO
    // ══════════════════════════════════════════════════════════════════════

    private fun abrirMenuEmprego() {
        _uiState.update { it.copy(showEmpregoMenu = true) }
    }

    private fun fecharMenuEmprego() {
        _uiState.update { it.copy(showEmpregoMenu = false) }
    }

    private fun navegarParaNovoEmprego() {
        viewModelScope.launch {
            fecharMenuEmprego()
            _uiEvent.emit(HomeUiEvent.NavegarParaNovoEmprego)
        }
    }

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

    private fun carregarEmpregoAtivo() {
        viewModelScope.launch {
            obterEmpregoAtivoUseCase.observar().collect { emprego ->
                val empregoAnterior = _uiState.value.empregoAtivo
                _uiState.update { it.copy(empregoAtivo = emprego) }

                if (emprego != null && empregoAnterior?.id != emprego.id) {
                    carregarConfiguracaoEmprego(emprego.id)
                    carregarPontosDoDia()
                    carregarBancoHoras()
                    carregarFechamentoCicloAnterior()
                    verificarCicloBancoHoras()
                }
            }
        }
    }

    private fun carregarConfiguracaoEmprego(empregoId: Long) {
        viewModelScope.launch {
            val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
            _uiState.update { it.copy(configuracaoEmprego = configuracao) }
        }
    }

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

    private fun carregarPontosDoDia() {
        pontosCollectionJob?.cancel()

        val data = _uiState.value.dataSelecionada
        val empregoId = _uiState.value.empregoAtivo?.id ?: return

        pontosCollectionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            obterResumoDiaCompletoUseCase.observar(empregoId, data).collect { resumoCompleto ->
                val proximoTipo = determinarProximoTipoPontoUseCase(resumoCompleto.pontos)

                _uiState.update {
                    it.copy(
                        pontosHoje = resumoCompleto.pontos,
                        resumoDia = resumoCompleto.resumoDia,
                        feriadosDoDia = resumoCompleto.feriadosDoDia,
                        ausenciaDoDia = resumoCompleto.ausenciaPrincipal,
                        versaoJornadaAtual = null,
                        proximoTipo = proximoTipo,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun determinarTipoDiaEspecial(
        feriados: List<Feriado>,
        ausencia: Ausencia? = null
    ): TipoDiaEspecial {
        if (ausencia != null) {
            return ausencia.tipo.toTipoDiaEspecial(ausencia.tipoFolga)
        }

        if (feriados.isEmpty()) return TipoDiaEspecial.NORMAL

        val temFeriadoFolga = feriados.any { feriado ->
            feriado.tipo in TipoFeriado.tiposFolga()
        }

        return if (temFeriadoFolga) {
            TipoDiaEspecial.FERIADO
        } else {
            TipoDiaEspecial.NORMAL
        }
    }

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

    private fun carregarFechamentoCicloAnterior() {
        viewModelScope.launch {
            val empregoId = _uiState.value.empregoAtivo?.id ?: return@launch
            val dataSelecionada = _uiState.value.dataSelecionada

            // Buscar fechamento cujo dataFimPeriodo seja o dia anterior à data selecionada
            val fechamento = fechamentoPeriodoRepository.buscarUltimoFechamentoBancoAteData(
                empregoId = empregoId,
                ateData = dataSelecionada.plusDays(1) // +1 para incluir o fechamento do dia anterior
            )

            // Verificar se a data selecionada é o dia seguinte ao fechamento
            val fechamentoRelevante = fechamento?.takeIf {
                it.dataFimPeriodo.plusDays(1) == dataSelecionada
            }

            _uiState.update { it.copy(fechamentoCicloAnterior = fechamentoRelevante) }
        }
    }
    private fun recarregarDados() {
        carregarPontosDoDia()
        carregarBancoHoras()
    }

    // ══════════════════════════════════════════════════════════════════════
    // RELÓGIO
    // ══════════════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════════════
    // NAVEGAÇÃO POR DATA
    // ══════════════════════════════════════════════════════════════════════

    private fun navegarDiaAnterior() {
        val novaData = _uiState.value.dataSelecionada.minusDays(1)
        if (_uiState.value.podeNavegaAnterior) {
            selecionarData(novaData)
        }
    }

    private fun navegarProximoDia() {
        val novaData = _uiState.value.dataSelecionada.plusDays(1)
        if (_uiState.value.podeNavegarProximo) {
            selecionarData(novaData)
        }
    }

    private fun irParaHoje() {
        selecionarData(LocalDate.now())
    }

    private fun selecionarData(data: LocalDate) {
        _uiState.update { it.copy(dataSelecionada = data) }
        carregarPontosDoDia()
        carregarBancoHoras()
        carregarFechamentoCicloAnterior()
    }

    // ══════════════════════════════════════════════════════════════════════
    // SELEÇÃO DE EMPREGO
    // ══════════════════════════════════════════════════════════════════════

    private fun abrirSeletorEmprego() {
        _uiState.update { it.copy(showEmpregoSelector = true) }
    }

    private fun fecharSeletorEmprego() {
        _uiState.update { it.copy(showEmpregoSelector = false) }
    }

    private fun selecionarEmprego(emprego: Emprego) {
        viewModelScope.launch {
            when (val resultado = trocarEmpregoAtivoUseCase(emprego)) {
                is TrocarEmpregoAtivoUseCase.Resultado.Sucesso -> {
                    fecharSeletorEmprego()
                    _uiEvent.emit(HomeUiEvent.EmpregoTrocado(emprego.nome))
                    carregarConfiguracaoEmprego(emprego.id)
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

    private fun abrirTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = true) }
    }

    private fun fecharTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = false) }
    }

    private fun iniciarRegistroPonto(hora: LocalTime) {
        val empregoId = _uiState.value.empregoAtivo?.id
        if (empregoId == null) {
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("Nenhum emprego ativo selecionado"))
            }
            return
        }

        fecharTimePicker()

        if (_uiState.value.nsrHabilitado) {
            _uiState.update {
                it.copy(
                    showNsrDialog = true,
                    nsrPendente = "",
                    horaPendenteParaRegistro = hora
                )
            }
        } else {
            registrarPonto(hora, null)
        }
    }

    private fun atualizarNsr(nsr: String) {
        _uiState.update { it.copy(nsrPendente = nsr) }
    }

    private fun confirmarRegistroComNsr() {
        val hora = _uiState.value.horaPendenteParaRegistro ?: return
        val nsr = _uiState.value.nsrPendente

        if (nsr.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("NSR é obrigatório"))
            }
            return
        }

        _uiState.update {
            it.copy(
                showNsrDialog = false,
                nsrPendente = "",
                horaPendenteParaRegistro = null
            )
        }

        registrarPonto(hora, nsr)
    }

    private fun cancelarNsrDialog() {
        _uiState.update {
            it.copy(
                showNsrDialog = false,
                nsrPendente = "",
                horaPendenteParaRegistro = null
            )
        }
    }

    private fun registrarPonto(hora: LocalTime, nsr: String?) {
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
                dataHora = dataHora,
                nsr = nsr
            )

            when (val resultado = registrarPontoUseCase(parametros)) {
                is RegistrarPontoUseCase.Resultado.Sucesso -> {
                    val horaFormatada = hora.format(DateTimeFormatter.ofPattern("HH:mm"))
                    val tipoDescricao = _uiState.value.proximoTipo.descricao
                    _uiEvent.emit(
                        HomeUiEvent.MostrarMensagem("$tipoDescricao registrada às $horaFormatada")
                    )
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
                is RegistrarPontoUseCase.Resultado.LocalizacaoObrigatoria -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Localização é obrigatória para este emprego"))
                }
                is RegistrarPontoUseCase.Resultado.NsrObrigatorio -> {
                    _uiState.update {
                        it.copy(
                            showNsrDialog = true,
                            nsrPendente = "",
                            horaPendenteParaRegistro = hora
                        )
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // EXCLUSÃO DE PONTO
    // ══════════════════════════════════════════════════════════════════════

    private fun solicitarExclusao(ponto: Ponto) {
        _uiState.update {
            it.copy(
                showDeleteConfirmDialog = true,
                pontoParaExcluir = ponto
            )
        }
    }

    private fun cancelarExclusao() {
        _uiState.update {
            it.copy(
                showDeleteConfirmDialog = false,
                pontoParaExcluir = null,
                motivoExclusao = ""
            )
        }
    }

    private fun confirmarExclusao() {
        val ponto = _uiState.value.pontoParaExcluir ?: return
        val motivo = _uiState.value.motivoExclusao.trim()

        if (motivo.length < 5) {
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("Informe um motivo válido (mínimo 5 caracteres)"))
            }
            return
        }

        viewModelScope.launch {
            val parametros = ExcluirPontoUseCase.Parametros(
                pontoId = ponto.id,
                motivo = motivo
            )

            when (val resultado = excluirPontoUseCase(parametros)) {
                is ExcluirPontoUseCase.Resultado.Sucesso -> {
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("Ponto excluído com sucesso"))
                }
                is ExcluirPontoUseCase.Resultado.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
                is ExcluirPontoUseCase.Resultado.NaoEncontrado -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Ponto não encontrado"))
                }
                is ExcluirPontoUseCase.Resultado.Validacao -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.erros.joinToString("\n")))
                }
            }

            _uiState.update {
                it.copy(
                    showDeleteConfirmDialog = false,
                    pontoParaExcluir = null,
                    motivoExclusao = ""
                )
            }
        }
    }

    private fun atualizarMotivoExclusao(motivo: String) {
        _uiState.update { it.copy(motivoExclusao = motivo) }
    }

    // ══════════════════════════════════════════════════════════════════════
    // NAVEGAÇÃO
    // ══════════════════════════════════════════════════════════════════════

    private fun navegarParaEdicao(pontoId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaEditarPonto(pontoId))
        }
    }

    private fun navegarParaHistorico() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaHistorico)
        }
    }

    private fun navegarParaConfiguracoes() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaConfiguracoes)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════════════

    private fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DATE PICKER
    // ══════════════════════════════════════════════════════════════════════

    private fun abrirDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    private fun fecharDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }
}
