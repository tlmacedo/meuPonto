// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.relatorio.ExportarRelatorioCsvUseCase
import br.com.tlmacedo.meuponto.domain.usecase.relatorio.GerarResumoPeriodoUseCase
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

/**
 * ViewModel da tela de Histórico.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val ausenciaRepository: AusenciaRepository,
    private val feriadoRepository: FeriadoRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val obterResumoDiaCompletoUseCase: ObterResumoDiaCompletoUseCase,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val gerarResumoPeriodoUseCase: GerarResumoPeriodoUseCase,
    private val exportarRelatorioCsvUseCase: ExportarRelatorioCsvUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var carregarJob: Job? = null
    private var empregoIdAtual: Long? = null
    private var versaoVigenteAtual: VersaoJornada? = null

    private data class VersaoCache(
        val versao: VersaoJornada,
        val horariosPorDia: Map<DiaSemana, HorarioDiaSemana>
    )

    init {
        // Aplica filtro inicial se fornecido via navegação
        savedStateHandle.get<String>(MeuPontoDestinations.ARG_FILTRO)?.let { filtroStr ->
            try {
                val filtro = FiltroHistorico.valueOf(filtroStr)
                if (filtro == FiltroHistorico.CALENDARIO) {
                    _uiState.update {
                        it.copy(
                            visualizacaoCalendario = true,
                            filtrosAtivos = emptySet()
                        )
                    }
                } else {
                    _uiState.update { it.copy(filtrosAtivos = setOf(filtro)) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Filtro inválido recebido: $filtroStr")
            }
        }
        carregarConfiguracaoEHistorico()
    }

    fun toggleVisualizacao() =
        _uiState.update { it.copy(visualizacaoCalendario = !it.visualizacaoCalendario) }

    fun alterarFiltro(filtro: FiltroHistorico) {
        _uiState.update { state ->
            val novosFiltros = if (state.filtrosAtivos.contains(filtro)) {
                state.filtrosAtivos - filtro
            } else {
                state.filtrosAtivos + filtro
            }
            state.copy(filtrosAtivos = novosFiltros, diaExpandido = null)
        }
    }

    fun limparFiltros() =
        _uiState.update { it.copy(filtrosAtivos = emptySet(), diaExpandido = null) }

    private fun carregarConfiguracaoEHistorico() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                obterEmpregoAtivoUseCase.observar().collect { emprego ->
                    val empregoId = emprego?.id

                    if (empregoId == null) {
                        _uiState.update { it.copy(isLoading = false) }
                        return@collect
                    }

                    empregoIdAtual = empregoId
                    val versaoVigente = versaoJornadaRepository.buscarVigente(empregoId)
                    versaoVigenteAtual = versaoVigente

                    val diaInicio = versaoVigente?.diaInicioFechamentoRH ?: 1
                    
                    val periodosDisponiveis = gerarPeriodosDisponiveis(diaInicio)

                    val periodoIncial = if (_uiState.value.nomeEmprego == null) {
                        PeriodoHistorico.periodoAtual(diaInicio)
                    } else {
                        _uiState.value.periodoSelecionado.copy(diaInicioFechamento = diaInicio)
                    }

                    _uiState.update { state ->
                        state.copy(
                            nomeEmprego = emprego.nome,
                            apelidoEmprego = emprego.apelido,
                            logoEmprego = emprego.logo,
                            periodoSelecionado = periodoIncial,
                            periodosSelecionados = listOf(periodoIncial),
                            periodosDisponiveis = periodosDisponiveis,
                            diaInicioFechamento = diaInicio
                        )
                    }

                    carregarHistorico()
                }

            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar configuração e histórico")
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    private fun gerarPeriodosDisponiveis(diaInicio: Int): List<PeriodoHistorico> {
        val hoje = LocalDate.now()
        val periodos = mutableListOf<PeriodoHistorico>()
        
        var ref = hoje.plusMonths(1)
        repeat(14) {
            periodos.add(PeriodoHistorico.fromPeriodoRH(ref, diaInicio))
            ref = ref.minusMonths(1)
        }
        return periodos.sortedByDescending { it.dataInicio }
    }

    fun carregarHistorico() {
        carregarJob?.cancel()
        carregarJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val empregoId = empregoIdAtual ?: return@launch
                val periodos = _uiState.value.periodosSelecionados
                if (periodos.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // Para garantir que o calendário mostre os meses inteiros, carregamos do primeiro ao último dia de todos os meses envolvidos
                val dataInicioTotal = periodos.minOf { it.dataInicio }.withDayOfMonth(1)
                val dataFimTotal = periodos.maxOf { it.dataFim }.let { it.withDayOfMonth(it.lengthOfMonth()) }
                val hoje = LocalDate.now()

                val saldoInicialPeriodo = if (dataInicioTotal.isAfter(hoje)) {
                    if (periodos.any { !it.isFuturo }) {
                        val ref = periodos.filter { !it.isFuturo }.minOf { it.dataInicio }
                        calcularSaldoInicialDoPeriodo(empregoId, ref)
                    } else {
                        calcularSaldoInicialDoPeriodo(empregoId, hoje.plusDays(1))
                    }
                } else {
                    calcularSaldoInicialDoPeriodo(empregoId, dataInicioTotal)
                }

                combine(
                    pontoRepository.observarPorEmpregoEPeriodo(empregoId, dataInicioTotal, dataFimTotal),
                    ausenciaRepository.observarAtivasPorEmprego(empregoId),
                    feriadoRepository.observarTodosAtivos(),
                    ajusteSaldoRepository.observarPorEmprego(empregoId)
                ) { pontos, ausencias, feriados, ajustes ->
                    processarDadosPeriodos(
                        empregoId = empregoId,
                        dataInicio = dataInicioTotal,
                        dataFim = dataFimTotal,
                        periodosSelecionados = periodos,
                        pontos = pontos,
                        ausencias = ausencias,
                        feriados = feriados,
                        ajustes = ajustes,
                        saldoInicialPeriodo = saldoInicialPeriodo
                    )
                }.collect { resultado ->
                    _uiState.update { state ->
                        state.copy(
                            diasHistorico = resultado.diasHistorico,
                            saldosAcumuladosPorDia = resultado.saldosAcumulados,
                            saldoInicialPeriodo = saldoInicialPeriodo,
                            resumoPeriodo = resultado.resumoPeriodo,
                            todasAusencias = resultado.todasAusencias,
                            todosFeriados = resultado.todosFeriados,
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

    private suspend fun processarDadosPeriodos(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        periodosSelecionados: List<PeriodoHistorico>,
        pontos: List<Ponto>,
        ausencias: List<Ausencia>,
        feriados: List<Feriado>,
        ajustes: List<AjusteSaldo>,
        saldoInicialPeriodo: Int
    ): ResultadoProcessamento {
        val hoje = LocalDate.now()
        val pontosPorDia = pontos.groupBy { it.data }

        val ausenciasNoPeriodo = ausencias.filter {
            it.ativo && it.dataInicio <= dataFim && it.dataFim >= dataInicio
        }

        val feriadosPorData = mutableMapOf<LocalDate, Feriado>()
        val nomesFeriadosPeriodo = mutableListOf<String>()
        val anosNoPeriodo = (dataInicio.year..dataFim.year).toList()
        for (feriado in feriados.filter { it.ativo }) {
            for (ano in anosNoPeriodo) {
                feriado.getDataParaAno(ano)?.let { data ->
                    if (data in dataInicio..dataFim) {
                        feriadosPorData[data] = feriado
                        if (feriado.nome !in nomesFeriadosPeriodo) {
                            nomesFeriadosPeriodo.add(feriado.nome)
                        }
                    }
                }
            }
        }

        val ausenciasPorData = mutableMapOf<LocalDate, MutableList<Ausencia>>()
        for (ausencia in ausenciasNoPeriodo) {
            var data = maxOf(ausencia.dataInicio, dataInicio)
            while (data <= minOf(ausencia.dataFim, dataFim)) {
                ausenciasPorData.getOrPut(data) { mutableListOf() }.add(ausencia)
                data = data.plusDays(1)
            }
        }

        val ajustesPorData = ajustes.filter { it.data in dataInicio..minOf(dataFim, hoje) }
            .groupBy { it.data }
            .mapValues { (_, list) -> list.sumOf { it.minutos } }

        val versaoCache = mutableMapOf<Long, VersaoCache>()
        val horarioSemVersaoCache = mutableMapOf<DiaSemana, HorarioDiaSemana?>()

        var totalMinutosTrabalhados = 0
        var saldoPeriodoMinutos = 0
        var diasCompletos = 0
        var diasComProblemas = 0
        var diasDescanso = 0
        var diasFeriado = 0
        var diasFerias = 0
        var diasFolga = 0
        var diasFolgaDayOff = 0
        var diasFolgaCompensacao = 0
        var diasAtestado = 0
        var diasFaltaJustificada = 0
        var diasFaltaInjustificada = 0
        var totalMinutosAbonados = 0
        var totalMinutosTolerancia = 0
        var diasUteisSemRegistro = 0
        var quantidadeDeclaracoes = 0
        var totalMinutosDeclaracoes = 0
        var quantidadeAtestados = 0

        var diasFuturos = 0
        var diasDescansoFuturo = 0
        var diasFeriadoFuturo = 0
        var diasFeriasFuturo = 0
        var diasFolgaFuturo = 0

        val diasHistorico = mutableListOf<InfoDiaHistorico>()
        val saldosAcumulados = mutableMapOf<LocalDate, Int>()
        var saldoAcumulado = saldoInicialPeriodo

        var dataAtual = dataInicio
        while (dataAtual <= dataFim) {
            val isFuturo = dataAtual.isAfter(hoje)
            val isInAnySelectedPeriod = periodosSelecionados.any { dataAtual >= it.dataInicio && dataAtual <= it.dataFim }
            
            val pontosNoDia = if (isFuturo) emptyList() else (pontosPorDia[dataAtual] ?: emptyList())
            val ausenciasDoDia = ausenciasPorData[dataAtual] ?: emptyList()
            val feriadoDoDia = feriadosPorData[dataAtual]

            val diaSemana = DiaSemana.fromJavaDayOfWeek(dataAtual.dayOfWeek)
            val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, dataAtual)

            val cargaBasePadrao = versaoJornada?.cargaHorariaDiariaMinutos ?: 480
            val acrescimoPontes = versaoJornada?.acrescimoMinutosDiasPontes ?: 0
            val toleranciaGlobal = versaoJornada?.toleranciaIntervaloMaisMinutos ?: 0

            val horarioDia = if (versaoJornada != null) {
                val cached = versaoCache[versaoJornada.id] ?: run {
                    val horarios =
                        horarioDiaSemanaRepository.buscarPorVersaoJornada(versaoJornada.id)
                            .associateBy { it.diaSemana }
                    VersaoCache(versaoJornada, horarios).also { versaoCache[versaoJornada.id] = it }
                }
                cached.horariosPorDia[diaSemana]
            } else {
                horarioSemVersaoCache.getOrPut(diaSemana) {
                    horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)
                }
            }

            val resumoCompleto = obterResumoDiaCompletoUseCase.invokeComDados(
                data = dataAtual,
                pontos = pontosNoDia,
                ausencias = ausenciasDoDia,
                feriado = feriadoDoDia,
                horarioDia = horarioDia,
                cargaHorariaBasePadrao = cargaBasePadrao,
                acrescimoPontes = acrescimoPontes,
                toleranciaIntervaloGlobal = toleranciaGlobal
            )

            val resumoDia = resumoCompleto.resumoDia
            val jornadaEsperada = horarioDia?.cargaHorariaMinutos ?: 0
            val isSemJornada = jornadaEsperada == 0

            val descricaoDiaEspecial = when {
                feriadoDoDia != null -> feriadoDoDia.nome
                resumoCompleto.ausenciaPrincipal != null -> {
                    val ausencia = resumoCompleto.ausenciaPrincipal!!
                    buildString {
                        append(ausencia.tipoDescricaoCompleta)
                        ausencia.observacao?.let { append(" - $it") }
                    }
                }
                else -> null
            }

            val infoDia = InfoDiaHistorico(
                resumoDia = resumoDia,
                ausencias = ausenciasDoDia,
                feriado = feriadoDoDia,
                descricaoDiaEspecial = descricaoDiaEspecial,
                isSemJornada = isSemJornada && pontosNoDia.isEmpty()
            )
            diasHistorico.add(infoDia)

            // Só contabilizamos para o resumo se o dia estiver dentro de um dos períodos selecionados
            if (isInAnySelectedPeriod) {
                if (isFuturo) {
                    diasFuturos++
                    if (isSemJornada && feriadoDoDia == null && ausenciasDoDia.isEmpty()) diasDescansoFuturo++
                    if (feriadoDoDia != null) diasFeriadoFuturo++
                    for (ausencia in ausenciasDoDia) {
                        when (ausencia.tipo) {
                            TipoAusencia.FERIAS -> diasFeriasFuturo++
                            TipoAusencia.FOLGA -> diasFolgaFuturo++
                            else -> {}
                        }
                    }
                } else {
                    if (isSemJornada && pontosNoDia.isEmpty() && feriadoDoDia == null && ausenciasDoDia.isEmpty()) diasDescanso++
                    if (feriadoDoDia != null) diasFeriado++

                    for (ausencia in ausenciasDoDia) {
                        when (ausencia.tipo) {
                            TipoAusencia.FERIAS -> diasFerias++
                            TipoAusencia.FOLGA -> {
                                diasFolga++
                                when (ausencia.tipoFolga) {
                                    TipoFolga.DAY_OFF -> diasFolgaDayOff++
                                    TipoFolga.COMPENSACAO, null -> {
                                        diasFolgaCompensacao++
                                    }
                                }
                            }
                            TipoAusencia.ATESTADO -> {
                                diasAtestado++; quantidadeAtestados++
                            }
                            TipoAusencia.DECLARACAO -> {
                                quantidadeDeclaracoes++
                                totalMinutosDeclaracoes += ausencia.duracaoAbonoMinutos ?: 0
                            }
                            TipoAusencia.FALTA_JUSTIFICADA -> diasFaltaJustificada++
                            TipoAusencia.FALTA_INJUSTIFICADA -> {
                                diasFaltaInjustificada++
                            }
                        }
                    }

                    if (jornadaEsperada > 0 && pontosNoDia.isEmpty() && 
                        dataAtual.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) &&
                        resumoDia.tipoDiaEspecial == TipoDiaEspecial.NORMAL && feriadoDoDia == null
                    ) {
                        diasUteisSemRegistro++
                    }

                    if (resumoDia.jornadaCompleta) {
                        diasCompletos++
                        totalMinutosTrabalhados += resumoDia.horasTrabalhadasMinutos
                    }
                    if (resumoDia.temProblemas) diasComProblemas++

                    totalMinutosAbonados += resumoDia.tempoAbonadoMinutos

                    if (resumoDia.temToleranciaIntervaloAplicada) {
                        totalMinutosTolerancia += abs(resumoDia.minutosIntervaloReal - resumoDia.minutosIntervaloTotal)
                    }

                    saldoAcumulado += resumoDia.saldoDiaMinutos
                    saldoAcumulado += ajustesPorData[dataAtual] ?: 0
                    saldoPeriodoMinutos += resumoDia.saldoDiaMinutos
                }
            }

            saldosAcumulados[dataAtual] = saldoAcumulado
            dataAtual = dataAtual.plusDays(1)
        }

        val totalAjustes = ajustesPorData.filter { entry -> periodosSelecionados.any { p -> entry.key >= p.dataInicio && entry.key <= p.dataFim } }.values.sum()

        val resumoPeriodo = ResumoPeriodo(
            totalMinutosTrabalhados = totalMinutosTrabalhados,
            saldoPeriodoMinutos = saldoPeriodoMinutos,
            diasCompletos = diasCompletos,
            diasComProblemas = diasComProblemas,
            diasDescanso = diasDescanso,
            diasFeriado = diasFeriado,
            diasFerias = diasFerias,
            diasFolga = diasFolga,
            diasFolgaDayOff = diasFolgaDayOff,
            diasFolgaCompensacao = diasFolgaCompensacao,
            diasAtestado = diasAtestado,
            diasFaltaJustificada = diasFaltaJustificada,
            diasFaltaInjustificada = diasFaltaInjustificada,
            totalMinutosAbonados = totalMinutosAbonados,
            totalMinutosTolerancia = totalMinutosTolerancia,
            diasUteisSemRegistro = diasUteisSemRegistro,
            totalDiasPeriodo = periodosSelecionados.sumOf { it.totalDias },
            totalAjustesMinutos = totalAjustes,
            quantidadeDeclaracoes = quantidadeDeclaracoes,
            totalMinutosDeclaracoes = totalMinutosDeclaracoes,
            quantidadeAtestados = quantidadeAtestados,
            nomesFeriados = nomesFeriadosPeriodo,
            diasDescansoFuturo = diasDescansoFuturo,
            diasFeriadoFuturo = diasFeriadoFuturo,
            diasFeriasFuturo = diasFeriasFuturo,
            diasFolgaFuturo = diasFolgaFuturo,
            diasFuturos = diasFuturos
        )

        return ResultadoProcessamento(
            diasHistorico = diasHistorico.sortedByDescending { it.data },
            saldosAcumulados = saldosAcumulados,
            resumoPeriodo = resumoPeriodo,
            todasAusencias = ausencias,
            todosFeriados = feriados
        )
    }

    private suspend fun calcularSaldoInicialDoPeriodo(
        empregoId: Long,
        dataInicioPeriodo: LocalDate
    ): Int {
        val hoje = LocalDate.now()
        val dataReferencia =
            if (dataInicioPeriodo.isAfter(hoje)) hoje else dataInicioPeriodo.minusDays(1)

        if (dataReferencia.isBefore(LocalDate.of(2020, 1, 1))) return 0

        return try {
            val resultado = calcularBancoHorasUseCase.calcularAteData(empregoId, dataReferencia)
            resultado.saldoTotal.toMinutes().toInt()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao calcular saldo inicial do período para $dataReferencia")
            0
        }
    }

    fun togglePeriodoSelection(periodo: PeriodoHistorico) {
        _uiState.update { state ->
            val novosPeriodos = if (state.periodosSelecionados.contains(periodo)) {
                if (state.periodosSelecionados.size > 1) state.periodosSelecionados - periodo
                else state.periodosSelecionados
            } else {
                (state.periodosSelecionados + periodo).sortedBy { it.dataInicio }
            }
            state.copy(
                periodosSelecionados = novosPeriodos, 
                periodoSelecionado = novosPeriodos.last(),
                diaExpandido = null
            )
        }
        carregarHistorico()
    }

    fun setShowPeriodoSelector(show: Boolean) =
        _uiState.update { it.copy(showPeriodoSelector = show) }

    fun selecionarPeriodoUnico(periodo: PeriodoHistorico) {
        _uiState.update {
            it.copy(
                periodosSelecionados = listOf(periodo),
                periodoSelecionado = periodo,
                showPeriodoSelector = false,
                diaExpandido = null
            )
        }
        carregarHistorico()
    }

    fun periodoAnterior() {
        val atual = _uiState.value.periodoSelecionado
        val novo = atual.periodoAnterior()
        selecionarPeriodoUnico(novo)
    }

    fun proximoPeriodo() {
        val atual = _uiState.value.periodoSelecionado
        val novo = atual.proximoPeriodo()
        selecionarPeriodoUnico(novo)
    }

    fun irParaPeriodoAtual() {
        val novo = PeriodoHistorico.periodoAtual(_uiState.value.diaInicioFechamento)
        selecionarPeriodoUnico(novo)
    }

    fun toggleDiaExpandido(data: LocalDate) =
        _uiState.update { it.copy(diaExpandido = if (it.diaExpandido == data) null else data) }

    fun toggleResumoExpandido() =
        _uiState.update { it.copy(resumoExpandido = !it.resumoExpandido) }

    fun limparErro() = _uiState.update { it.copy(errorMessage = null) }

    fun exportarParaCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val empregoId = empregoIdAtual ?: return@launch
                val periodos = _uiState.value.periodosSelecionados
                if (periodos.isEmpty()) return@launch

                val dataInicio = periodos.minOf { it.dataInicio }
                val dataFim = periodos.maxOf { it.dataFim }

                val resumoPeriodo = gerarResumoPeriodoUseCase(
                    empregoId,
                    dataInicio,
                    dataFim
                )

                val csv = exportarRelatorioCsvUseCase(resumoPeriodo)
                _uiState.update { it.copy(csvParaExportar = csv, isExporting = false) }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao exportar CSV")
                _uiState.update {
                    it.copy(
                        errorMessage = "Erro ao gerar CSV: ${e.message}",
                        isExporting = false
                    )
                }
            }
        }
    }

    fun limparCsvExportado() = _uiState.update { it.copy(csvParaExportar = null) }

    fun recarregar() = carregarConfiguracaoEHistorico()
}

private data class ResultadoProcessamento(
    val diasHistorico: List<InfoDiaHistorico>,
    val saldosAcumulados: Map<LocalDate, Int>,
    val resumoPeriodo: ResumoPeriodo,
    val todasAusencias: List<Ausencia>,
    val todosFeriados: List<Feriado>
)
