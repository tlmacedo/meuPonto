// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
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
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
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
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 7.8.0 - Adicionado contador de dias futuros e filtro FUTUROS
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
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var carregarJob: Job? = null
    private var empregoIdAtual: Long? = null
    private var configuracaoAtual: ConfiguracaoEmprego? = null

    private data class VersaoCache(
        val versao: VersaoJornada,
        val horariosPorDia: Map<DiaSemana, HorarioDiaSemana>
    )

    init {
        carregarConfiguracaoEHistorico()
    }

    private fun carregarConfiguracaoEHistorico() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val empregoId: Long? = when (val resultado = obterEmpregoAtivoUseCase()) {
                    is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> resultado.emprego.id
                    else -> null
                }

                if (empregoId == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                empregoIdAtual = empregoId
                val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
                configuracaoAtual = configuracao

                val diaInicio = configuracao?.diaInicioFechamentoRH ?: 1
                val periodoInicial = PeriodoHistorico.periodoAtual(diaInicio)

                _uiState.update { state ->
                    state.copy(
                        periodoSelecionado = periodoInicial,
                        diaInicioFechamento = diaInicio
                    )
                }

                carregarHistorico()

            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar configuração e histórico")
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

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

                val periodo = _uiState.value.periodoSelecionado
                val dataInicio = periodo.dataInicio
                val dataFim = periodo.dataFim
                val hoje = LocalDate.now()

                val saldoInicialPeriodo = if (dataInicio.isAfter(hoje)) {
                    calcularSaldoInicialDoPeriodo(empregoId, hoje.plusDays(1))
                } else {
                    calcularSaldoInicialDoPeriodo(empregoId, dataInicio)
                }

                combine(
                    pontoRepository.observarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim),
                    ausenciaRepository.observarAtivasPorEmprego(empregoId),
                    feriadoRepository.observarTodosAtivos(),
                    ajusteSaldoRepository.observarPorEmprego(empregoId)
                ) { pontos, ausencias, feriados, ajustes ->
                    processarDadosPeriodo(
                        empregoId = empregoId,
                        dataInicio = dataInicio,
                        dataFim = dataFim,
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

    private data class ResultadoProcessamento(
        val diasHistorico: List<InfoDiaHistorico>,
        val saldosAcumulados: Map<LocalDate, Int>,
        val resumoPeriodo: ResumoPeriodo
    )

    private suspend fun processarDadosPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        pontos: List<Ponto>,
        ausencias: List<Ausencia>,
        feriados: List<Feriado>,
        ajustes: List<AjusteSaldo>,
        saldoInicialPeriodo: Int
    ): ResultadoProcessamento {
        val hoje = LocalDate.now()

        val pontosPorDia = pontos.filter { it.data in dataInicio..dataFim }.groupBy { it.data }

        val ausenciasNoPeriodo = ausencias.filter {
            it.ativo && it.dataInicio <= dataFim && it.dataFim >= dataInicio
        }

        // Mapear feriados por data
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

        // Mapear ausências por data
        val ausenciasPorData = mutableMapOf<LocalDate, MutableList<Ausencia>>()
        for (ausencia in ausenciasNoPeriodo) {
            var data = maxOf(ausencia.dataInicio, dataInicio)
            while (data <= minOf(ausencia.dataFim, dataFim)) {
                ausenciasPorData.getOrPut(data) { mutableListOf() }.add(ausencia)
                data = data.plusDays(1)
            }
        }

        // Ajustes só contam até hoje
        val ajustesPorData = ajustes.filter { it.data in dataInicio..minOf(dataFim, hoje) }
            .groupBy { it.data }
            .mapValues { (_, list) -> list.sumOf { it.minutos } }

        // Cache de versões de jornada
        val versaoCache = mutableMapOf<Long, VersaoCache>()
        val horarioSemVersaoCache = mutableMapOf<DiaSemana, HorarioDiaSemana?>()
        val cargaPadrao = configuracaoAtual?.cargaHorariaDiariaMinutos ?: 480

        // Métricas - separadas por passado/futuro
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

        // Contadores para dias futuros (previsão)
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
            val pontosNoDia = if (isFuturo) emptyList() else (pontosPorDia[dataAtual] ?: emptyList())
            val ausenciasDoDia = ausenciasPorData[dataAtual] ?: emptyList()
            val feriadoDoDia = feriadosPorData[dataAtual]

            val diaSemana = DiaSemana.fromJavaDayOfWeek(dataAtual.dayOfWeek)
            val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, dataAtual)

            val horarioDia = if (versaoJornada != null) {
                val cached = versaoCache[versaoJornada.id] ?: run {
                    val horarios = horarioDiaSemanaRepository.buscarPorVersaoJornada(versaoJornada.id)
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
                cargaHorariaPadrao = cargaPadrao
            )

            val resumoDia = resumoCompleto.resumoDia
            val jornadaEsperada = horarioDia?.cargaHorariaMinutos ?: 0
            val isFimDeSemana = dataAtual.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
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

            // ================================================================
            // CONTABILIZAR MÉTRICAS
            // ================================================================

            if (isFuturo) {
                // Contar total de dias futuros
                diasFuturos++

                // Contagem de dias futuros (previsão)
                if (isSemJornada && feriadoDoDia == null && ausenciasDoDia.isEmpty()) {
                    diasDescansoFuturo++
                }
                if (feriadoDoDia != null) {
                    diasFeriadoFuturo++
                }
                for (ausencia in ausenciasDoDia) {
                    when (ausencia.tipo) {
                        TipoAusencia.FERIAS -> diasFeriasFuturo++
                        TipoAusencia.FOLGA -> diasFolgaFuturo++
                        else -> {}
                    }
                }
            } else {
                // Contagem de dias passados/hoje (real)
                if (isSemJornada && pontosNoDia.isEmpty() && feriadoDoDia == null && ausenciasDoDia.isEmpty()) {
                    diasDescanso++
                }

                if (feriadoDoDia != null) {
                    diasFeriado++
                }

                var temFolgaCompensacao = false
                var temFaltaInjustificada = false

                for (ausencia in ausenciasDoDia) {
                    when (ausencia.tipo) {
                        TipoAusencia.FERIAS -> diasFerias++
                        TipoAusencia.FOLGA -> {
                            diasFolga++
                            when (ausencia.tipoFolga) {
                                TipoFolga.DAY_OFF -> diasFolgaDayOff++
                                TipoFolga.COMPENSACAO, null -> {
                                    diasFolgaCompensacao++
                                    temFolgaCompensacao = true
                                }
                            }
                        }
                        TipoAusencia.ATESTADO -> {
                            diasAtestado++
                            quantidadeAtestados++
                        }
                        TipoAusencia.DECLARACAO -> {
                            quantidadeDeclaracoes++
                            totalMinutosDeclaracoes += ausencia.duracaoAbonoMinutos ?: 0
                        }
                        TipoAusencia.FALTA_JUSTIFICADA -> diasFaltaJustificada++
                        TipoAusencia.FALTA_INJUSTIFICADA -> {
                            diasFaltaInjustificada++
                            temFaltaInjustificada = true
                        }
                    }
                }

                if (jornadaEsperada > 0 && pontosNoDia.isEmpty() && !isFimDeSemana &&
                    resumoDia.tipoDiaEspecial == TipoDiaEspecial.NORMAL && feriadoDoDia == null) {
                    diasUteisSemRegistro++
                }

                if (resumoDia.jornadaCompleta) {
                    diasCompletos++
                    totalMinutosTrabalhados += resumoDia.horasTrabalhadasMinutos
                }
                if (resumoDia.temProblemas) {
                    diasComProblemas++
                }

                totalMinutosAbonados += resumoDia.tempoAbonadoMinutos

                if (resumoDia.temToleranciaIntervaloAplicada) {
                    totalMinutosTolerancia += abs(resumoDia.minutosIntervaloReal - resumoDia.minutosIntervaloTotal)
                }

                val deveAcumularSaldo = resumoDia.jornadaCompleta ||
                        resumoDia.isJornadaZerada ||
                        (isSemJornada && pontosNoDia.isEmpty()) ||
                        temFolgaCompensacao ||
                        temFaltaInjustificada

                if (deveAcumularSaldo) {
                    saldoAcumulado += resumoDia.saldoDiaMinutos
                    saldoAcumulado += ajustesPorData[dataAtual] ?: 0
                    saldoPeriodoMinutos += resumoDia.saldoDiaMinutos
                }
            }

            saldosAcumulados[dataAtual] = saldoAcumulado

            dataAtual = dataAtual.plusDays(1)
        }

        val totalAjustes = ajustesPorData.values.sum()
        saldoPeriodoMinutos += totalAjustes

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
            totalDiasPeriodo = diasHistorico.size,
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
            resumoPeriodo = resumoPeriodo
        )
    }

    private suspend fun calcularSaldoInicialDoPeriodo(
        empregoId: Long,
        dataInicioPeriodo: LocalDate
    ): Int {
        val hoje = LocalDate.now()
        if (dataInicioPeriodo.isAfter(hoje)) {
            return try {
                val resultado = calcularBancoHorasUseCase.calcularAteData(empregoId, hoje)
                resultado.saldoTotal.toMinutes().toInt()
            } catch (e: Exception) {
                Timber.e(e, "Erro ao calcular saldo inicial do período")
                0
            }
        }

        val dataFimCalculo = dataInicioPeriodo.minusDays(1)
        if (dataFimCalculo.isBefore(LocalDate.of(2020, 1, 1))) return 0

        return try {
            val resultado = calcularBancoHorasUseCase.calcularAteData(empregoId, dataFimCalculo)
            resultado.saldoTotal.toMinutes().toInt()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao calcular saldo inicial do período")
            0
        }
    }

    fun selecionarPeriodo(novoPeriodo: PeriodoHistorico) {
        _uiState.update { it.copy(periodoSelecionado = novoPeriodo, diaExpandido = null) }
        carregarHistorico()
    }

    fun periodoAnterior() = selecionarPeriodo(_uiState.value.periodoSelecionado.periodoAnterior())

    fun proximoPeriodo() {
        selecionarPeriodo(_uiState.value.periodoSelecionado.proximoPeriodo())
    }

    fun irParaPeriodoAtual() = selecionarPeriodo(
        PeriodoHistorico.periodoAtual(_uiState.value.diaInicioFechamento)
    )

    fun alterarFiltro(filtro: FiltroHistorico) {
        _uiState.update { it.copy(filtroAtivo = filtro, diaExpandido = null) }
    }

    fun toggleDiaExpandido(data: LocalDate) {
        _uiState.update { it.copy(diaExpandido = if (it.diaExpandido == data) null else data) }
    }

    fun limparErro() = _uiState.update { it.copy(errorMessage = null) }
    fun recarregar() = carregarConfiguracaoEHistorico()
}
