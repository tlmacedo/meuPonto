// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularResumoDiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel da tela de Histórico.
 *
 * FUNCIONALIDADES:
 * - Carrega e exibe os pontos registrados por período
 * - Suporta período RH customizado (diaInicioFechamentoRH da configuração)
 * - Navegação entre períodos (anterior/próximo)
 * - Filtros por status do dia
 * - Cálculo de resumos e saldos
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 3.3.0 - Usa observarPorEmpregoEPeriodo para suporte a múltiplos empregos
 * @updated 4.0.0 - Suporte a período RH customizado (diaInicioFechamentoRH)
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: PontoRepository,
    private val calcularResumoDiaUseCase: CalcularResumoDiaUseCase,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository // NOVO
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var carregarJob: Job? = null
    private var empregoIdAtual: Long? = null
    private var configuracaoAtual: ConfiguracaoEmprego? = null

    init {
        carregarConfiguracaoEHistorico()
    }

    /**
     * Carrega a configuração do emprego ativo e em seguida o histórico.
     * A configuração define o dia de início do período RH.
     */
    private fun carregarConfiguracaoEHistorico() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Obtém o emprego ativo
                val empregoId: Long? = when (val resultado = obterEmpregoAtivoUseCase()) {
                    is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> resultado.emprego.id
                    else -> null
                }

                if (empregoId == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                empregoIdAtual = empregoId

                // Busca a configuração do emprego para obter diaInicioFechamentoRH
                val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
                configuracaoAtual = configuracao

                val diaInicio = configuracao?.diaInicioFechamentoRH ?: 1

                // Cria o período inicial baseado na configuração
                val periodoInicial = PeriodoHistorico.periodoAtual(diaInicio)

                _uiState.update { state ->
                    state.copy(
                        periodoSelecionado = periodoInicial,
                        diaInicioFechamento = diaInicio
                    )
                }

                // Carrega o histórico do período
                carregarHistorico()

            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar configuração e histórico")
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    /**
     * Carrega o histórico do período selecionado.
     */
    fun carregarHistorico() {
        carregarJob?.cancel()
        carregarJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val empregoId = empregoIdAtual ?: run {
                    when (val resultado = obterEmpregoAtivoUseCase()) {
                        is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> {
                            empregoIdAtual = resultado.emprego.id
                            resultado.emprego.id
                        }
                        else -> {
                            _uiState.update { it.copy(isLoading = false) }
                            return@launch
                        }
                    }
                }

                val versoesJornada = versaoJornadaRepository.buscarPorEmprego(empregoId)
                val horariosPorVersao = mutableMapOf<Long, Map<DiaSemana, HorarioDiaSemana>>()

                val periodo = _uiState.value.periodoSelecionado

                // Buscar ajustes de saldo do período
                val ajustesPeriodo = ajusteSaldoRepository.buscarPorPeriodo(
                    empregoId,
                    periodo.dataInicio,
                    periodo.dataFim
                )

                // Agrupar ajustes por data
                val ajustesPorData = ajustesPeriodo.groupBy { it.data }
                    .mapValues { (_, ajustes) -> ajustes.sumOf { it.minutos } }

                repository.observarPorEmpregoEPeriodo(empregoId, periodo.dataInicio, periodo.dataFim)
                    .collect { pontos ->
                        val resumosPorDia = pontos
                            .groupBy { it.data }
                            .map { (data, pontosData) ->
                                val versaoVigente = encontrarVersaoVigente(versoesJornada, data)

                                val horariosDaVersao = versaoVigente?.let { versao ->
                                    horariosPorVersao.getOrPut(versao.id) {
                                        horarioDiaSemanaRepository
                                            .buscarPorVersaoJornada(versao.id)
                                            .associateBy { it.diaSemana }
                                    }
                                } ?: emptyMap()

                                val diaSemana = DiaSemana.fromDayOfWeek(data.dayOfWeek)
                                val horarioDia = horariosDaVersao[diaSemana]

                                calcularResumoDiaUseCase(
                                    pontos = pontosData,
                                    data = data,
                                    horarioDiaSemana = horarioDia
                                )
                            }
                            .sortedByDescending { it.data }

                        // Calcular saldos acumulados
                        val saldosAcumulados = calcularSaldosAcumulados(
                            resumos = resumosPorDia,
                            ajustesPorData = ajustesPorData,
                            saldoInicial = 0 // TODO: buscar saldo de fechamentos anteriores se necessário
                        )

                        _uiState.update { state ->
                            state.copy(
                                resumosPorDia = resumosPorDia,
                                saldosAcumuladosPorDia = saldosAcumulados,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar histórico")
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    /**
     * Calcula os saldos acumulados do banco de horas para cada dia.
     *
     * @param resumos Lista de resumos do período (ordenados por data DECRESCENTE)
     * @param ajustesPorData Mapa de ajustes manuais por data
     * @param saldoInicial Saldo inicial do período (de fechamentos anteriores)
     * @return Mapa de data -> saldo acumulado até aquele dia
     */
    private fun calcularSaldosAcumulados(
        resumos: List<ResumoDia>,
        ajustesPorData: Map<LocalDate, Int>,
        saldoInicial: Int
    ): Map<LocalDate, Int> {
        if (resumos.isEmpty()) return emptyMap()

        // Ordenar por data CRESCENTE para calcular acumulado
        val resumosOrdenados = resumos.sortedBy { it.data }

        val saldosAcumulados = mutableMapOf<LocalDate, Int>()
        var acumulado = saldoInicial

        for (resumo in resumosOrdenados) {
            // Só acumula dias completos (com jornada fechada)
            if (resumo.jornadaCompleta) {
                // Saldo do dia
                acumulado += resumo.saldoDiaMinutos

                // Ajustes manuais do dia
                val ajusteDia = ajustesPorData[resumo.data] ?: 0
                acumulado += ajusteDia
            }

            saldosAcumulados[resumo.data] = acumulado
        }

        return saldosAcumulados
    }

    private fun encontrarVersaoVigente(
        versoes: List<VersaoJornada>,
        data: LocalDate
    ): VersaoJornada? {
        return versoes.find { versao -> versao.contemData(data) }
    }

    // ========================================================================
    // Navegação de Período
    // ========================================================================

    /**
     * Seleciona um novo período para exibição.
     */
    fun selecionarPeriodo(novoPeriodo: PeriodoHistorico) {
        _uiState.update { it.copy(periodoSelecionado = novoPeriodo, diaExpandido = null) }
        carregarHistorico()
    }

    /**
     * Navega para o período anterior.
     */
    fun periodoAnterior() {
        val novoPeriodo = _uiState.value.periodoSelecionado.periodoAnterior()
        selecionarPeriodo(novoPeriodo)
    }

    /**
     * Navega para o próximo período.
     */
    fun proximoPeriodo() {
        if (_uiState.value.podeIrProximoPeriodo) {
            val novoPeriodo = _uiState.value.periodoSelecionado.proximoPeriodo()
            selecionarPeriodo(novoPeriodo)
        }
    }

    /**
     * Volta para o período atual (que contém hoje).
     */
    fun irParaPeriodoAtual() {
        val diaInicio = _uiState.value.diaInicioFechamento
        val periodoAtual = PeriodoHistorico.periodoAtual(diaInicio)
        selecionarPeriodo(periodoAtual)
    }

    // ========================================================================
    // Métodos Legados (Compatibilidade)
    // ========================================================================

    /**
     * @deprecated Use periodoAnterior() em vez disso
     */
    @Deprecated("Use periodoAnterior()", ReplaceWith("periodoAnterior()"))
    fun mesAnterior() {
        periodoAnterior()
    }

    /**
     * @deprecated Use proximoPeriodo() em vez disso
     */
    @Deprecated("Use proximoPeriodo()", ReplaceWith("proximoPeriodo()"))
    fun proximoMes() {
        proximoPeriodo()
    }

    /**
     * @deprecated Use selecionarPeriodo() em vez disso
     */
    @Deprecated("Use selecionarPeriodo()", ReplaceWith("selecionarPeriodo(PeriodoHistorico.fromPeriodoRH(novoMes, diaInicioFechamento))"))
    fun selecionarMes(novoMes: LocalDate) {
        val diaInicio = _uiState.value.diaInicioFechamento
        val novoPeriodo = PeriodoHistorico.fromPeriodoRH(novoMes, diaInicio)
        selecionarPeriodo(novoPeriodo)
    }

    // ========================================================================
    // Filtros e Interação
    // ========================================================================

    /**
     * Altera o filtro de exibição.
     */
    fun alterarFiltro(filtro: FiltroHistorico) {
        _uiState.update { it.copy(filtroAtivo = filtro, diaExpandido = null) }
    }

    /**
     * Expande/colapsa os detalhes de um dia.
     */
    fun toggleDiaExpandido(data: LocalDate) {
        _uiState.update { state ->
            state.copy(
                diaExpandido = if (state.diaExpandido == data) null else data
            )
        }
    }

    /**
     * Limpa a mensagem de erro.
     */
    fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Força o recarregamento da configuração e histórico.
     * Útil quando a configuração do emprego é alterada.
     */
    fun recarregar() {
        carregarConfiguracaoEHistorico()
    }
}
