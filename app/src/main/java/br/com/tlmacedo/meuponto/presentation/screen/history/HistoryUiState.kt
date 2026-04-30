// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import br.com.tlmacedo.meuponto.domain.extensions.isFeriadoOrFalse
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Filtros disponíveis para a tela de histórico.
 *
 * @updated 7.8.0 - Adicionado filtro FUTUROS
 */
enum class FiltroHistorico(
    val descricao: String,
    val emoji: String? = null,
    val isSecundario: Boolean = false
) {
    // Filtros principais (chips visíveis)
    TODOS("Todos"),
    UTEIS("Úteis"),
    COMPLETOS("Completos"),
    INCOMPLETOS("Incompletos"),
    COM_PROBLEMAS("Problemas"),

    // Filtros secundários (acionados pelo resumo)
    FUTUROS("Futuros", "🔮", true),
    DESCANSO("Descanso", "🛋️", true),
    FERIADOS("Feriados", "🎉", true),
    FERIAS("Férias", "🏖️", true),
    FOLGAS("Folgas", "😴", true),
    DAY_OFF("Day-off", "🎁", true),
    ATESTADOS("Atestados", "🏥", true),
    DECLARACOES("Declarações", "📄", true),
    FALTAS("Faltas", "❌", true),

    // Novo modo de visualização
    LISTA("Lista", "📝"),
    CALENDARIO("Calendário", "📅");

    companion object {
        /** Retorna apenas os filtros principais (para exibir nos chips) */
        val principais: List<FiltroHistorico>
            get() = entries.filter { !it.isSecundario && it != LISTA && it != CALENDARIO }
    }
}

/**
 * Representa um período de exibição no histórico.
 */
data class PeriodoHistorico(
    val dataInicio: LocalDate,
    val dataFim: LocalDate,
    val diaInicioFechamento: Int = 1
) {
    val descricaoFormatada: String
        get() {
            val locale = Locale.forLanguageTag("pt-BR")
            return if (diaInicioFechamento == 1) {
                val formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", locale)
                dataInicio.format(formatter).replaceFirstChar { it.uppercase() }
            } else {
                val formatterDiaMes = DateTimeFormatter.ofPattern("dd/MM", locale)
                val formatterCompleto = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
                if (dataInicio.year == dataFim.year) {
                    "${dataInicio.format(formatterCompleto)} a ${dataFim.format(formatterCompleto)}"
                } else {
                    "${dataInicio.format(formatterCompleto)} a ${dataFim.format(formatterCompleto)}"
                }
            }
        }

    val descricaoCurta: String
        get() {
            val locale = Locale.forLanguageTag("pt-BR")
            return if (diaInicioFechamento == 1) {
                val formatter = DateTimeFormatter.ofPattern("MMM/yyyy", locale)
                dataInicio.format(formatter).replaceFirstChar { it.uppercase() }
            } else {
                val formatter = DateTimeFormatter.ofPattern("dd/MM", locale)
                "${dataInicio.format(formatter)}-${dataFim.format(formatter)}"
            }
        }

    /** Total de dias no período completo (incluindo futuros) */
    val totalDias: Int
        get() = (dataFim.toEpochDay() - dataInicio.toEpochDay() + 1).toInt()

    /**
     * Total de dias até hoje (excluindo dias futuros).
     * Para períodos passados, retorna o total de dias.
     * Para períodos futuros, retorna 0.
     * Para o período atual, retorna apenas os dias até hoje.
     */
    val totalDiasAteHoje: Int
        get() {
            val hoje = LocalDate.now()
            return when {
                dataInicio.isAfter(hoje) -> 0
                dataFim.isBefore(hoje) || dataFim == hoje -> totalDias
                else -> (hoje.toEpochDay() - dataInicio.toEpochDay() + 1).toInt()
            }
        }

    val contemHoje: Boolean
        get() {
            val hoje = LocalDate.now()
            return !hoje.isBefore(dataInicio) && !hoje.isAfter(dataFim)
        }

    val isFuturo: Boolean get() = dataInicio.isAfter(LocalDate.now())
    val isPassado: Boolean get() = dataFim.isBefore(LocalDate.now())

    companion object {
        fun fromMesCalendario(dataReferencia: LocalDate): PeriodoHistorico {
            return PeriodoHistorico(
                dataInicio = dataReferencia.withDayOfMonth(1),
                dataFim = dataReferencia.withDayOfMonth(dataReferencia.lengthOfMonth()),
                diaInicioFechamento = 1
            )
        }

        fun fromPeriodoRH(dataReferencia: LocalDate, diaInicioFechamento: Int): PeriodoHistorico {
            val diaFechamento = diaInicioFechamento.coerceIn(1, 28)
            if (diaFechamento == 1) return fromMesCalendario(dataReferencia)

            val dataInicio = if (dataReferencia.dayOfMonth >= diaFechamento) {
                dataReferencia.withDayOfMonth(diaFechamento)
            } else {
                dataReferencia.minusMonths(1).withDayOfMonth(diaFechamento)
            }
            val dataFim = dataInicio.plusMonths(1).minusDays(1)

            return PeriodoHistorico(dataInicio, dataFim, diaFechamento)
        }

        fun periodoAtual(diaInicioFechamento: Int = 1) =
            fromPeriodoRH(LocalDate.now(), diaInicioFechamento)
    }

    fun periodoAnterior() = fromPeriodoRH(dataInicio.minusDays(1), diaInicioFechamento)
    fun proximoPeriodo() = fromPeriodoRH(dataFim.plusDays(1), diaInicioFechamento)
}

/**
 * Informações extras de um dia para exibição no histórico.
 */
data class InfoDiaHistorico(
    val resumoDia: ResumoDia,
    val ausencias: List<Ausencia> = emptyList(),
    val feriado: Feriado? = null,
    val descricaoDiaEspecial: String? = null,
    val isSemJornada: Boolean = false,

    /**
     * Depois da refatoração, ResumoDia não deve mais ser fonte de pontos reais.
     * Quem tiver List<Ponto> deve passar aqui.
     */
    val pontosDoDia: List<Ponto> = emptyList(),

    /**
     * Intervalos reais derivados dos pontos.
     * Devem vir de ResumoDiaCompleto.intervalos quando disponível.
     */
    val intervalosDoDia: List<IntervaloPonto> = emptyList()
) {
    // =========================================================================
    // Delegações básicas
    // =========================================================================

    val data: LocalDate
        get() = resumoDia.data

    val pontos: List<Ponto>
        get() = pontosDoDia

    val jornadaCompleta: Boolean
        get() = pontos.size >= 4 || resumoDia.possuiPontoCompleto

    val horasTrabalhadasMinutos: Int
        get() = resumoDia.horasTrabalhadasMinutos

    val saldoDiaMinutos: Int
        get() = resumoDia.saldoDiaMinutos

    val tipoAusencia: TipoAusencia?
        get() = resumoDia.tipoAusencia

    val statusDia
        get() = resumoDia.statusDia

    val temProblemas: Boolean
        get() = resumoDia.temProblemas

    val intervalos: List<IntervaloPonto>
        get() = intervalosDoDia

    val temIntervalo: Boolean
        get() = intervalos.isNotEmpty() || resumoDia.temIntervalo

    // =========================================================================
    // Propriedades calculadas
    // =========================================================================

    val temFeriado: Boolean
        get() = feriado != null || resumoDia.tipoAusencia.isFeriadoOrFalse

    val temAusencia: Boolean
        get() = ausencias.isNotEmpty()

    /**
     * É dia de falta, justificada ou injustificada.
     */
    val isFalta: Boolean
        get() = ausencias.any {
            it.tipo == TipoAusencia.Falta.Justificada ||
                    it.tipo == TipoAusencia.Falta.Injustificada
        }

    /**
     * É dia de descanso sem jornada.
     */
    val isDescanso: Boolean
        get() = isSemJornada &&
                pontos.isEmpty() &&
                !temFeriado &&
                ausencias.isEmpty()

    /**
     * É dia útil: tem jornada programada e não é feriado/descanso/férias/falta/atestado.
     */
    val isDiaUtil: Boolean
        get() = !isSemJornada &&
                !temFeriado &&
                tipoAusencia != TipoAusencia.Ferias &&
                !isFalta &&
                atestados.isEmpty()

    /**
     * É dia completo: 2 turnos ou pelo menos 4h trabalhadas.
     */
    val isCompleto: Boolean
        get() = pontos.size >= 4 || horasTrabalhadasMinutos >= 240

    /**
     * É dia incompleto: útil e sem registro ou com menos de 4h trabalhadas.
     */
    val isIncompleto: Boolean
        get() = isDiaUtil && (pontos.isEmpty() || horasTrabalhadasMinutos < 240)

    val declaracoes: List<Ausencia>
        get() = ausencias.filter { it.tipo == TipoAusencia.Declaracao }

    val atestados: List<Ausencia>
        get() = ausencias.filter { it.tipo == TipoAusencia.Atestado }

    val ausenciaPrincipal: Ausencia?
        get() = ausencias.firstOrNull { it.tipo != TipoAusencia.Declaracao }
            ?: ausencias.firstOrNull()

    val totalMinutosDeclaracoes: Int
        get() = declaracoes.sumOf { it.duracaoAbonoMinutos ?: 0 }

    /**
     * Emoji do dia baseado na prioridade visual.
     */
    val emoji: String
        get() = when {
            temFeriado && pontos.isNotEmpty() -> "⭐"
            temFeriado -> "🎉"
            ausenciaPrincipal != null -> getEmojiAusencia(ausenciaPrincipal!!)
            isSemJornada -> "🛋️"
            resumoDia.isFuturo -> "🔮"
            jornadaCompleta -> "✅"
            pontos.isNotEmpty() && !jornadaCompleta -> {
                if (resumoDia.isHoje) "🔄" else "⚠️"
            }
            pontos.isEmpty() && !isSemJornada -> "⬜"
            else -> "📅"
        }

    private fun getEmojiAusencia(ausencia: Ausencia): String {
        return when (ausencia.tipo) {
            TipoAusencia.Folga -> "😴"
            TipoAusencia.Ferias -> "🏖️"

            TipoAusencia.Feriado.Oficial -> "🎉"
            TipoAusencia.Feriado.DiaPonte -> "🌉"
            TipoAusencia.Feriado.Facultativo -> "📌"

            TipoAusencia.Atestado -> "🏥"
            TipoAusencia.Declaracao -> "📄"

            TipoAusencia.DayOff -> {
                if (ausencia.tipoFolga == TipoFolga.DAY_OFF) "🎁" else "😴"
            }

            TipoAusencia.DiminuirBanco -> "⏳"
            TipoAusencia.Falta.Justificada -> "📝"
            TipoAusencia.Falta.Injustificada -> "❌"
        }
    }

    val descricaoCurta: String?
        get() = when {
            temFeriado -> feriado?.nome ?: descricaoDiaEspecial ?: "Feriado"
            ausenciaPrincipal != null -> ausenciaPrincipal?.tipoDescricaoCompleta
            isSemJornada -> "Descanso"
            else -> null
        }
}

/**
 * Resumo consolidado do período para exibição.
 *
 * @updated 7.8.0 - Adicionado diasFuturos e cálculo de dias úteis
 */
data class ResumoPeriodo(
    val totalMinutosTrabalhados: Int = 0,
    val saldoPeriodoMinutos: Int = 0,
    val diasCompletos: Int = 0,
    val diasIncompletos: Int = 0,
    val diasComProblemas: Int = 0,
    val diasUteis: Int = 0,
    val totalHorasUteisMinutos: Int = 0,
    val diasTrabalhados: Int = 0,
    val diasAusenciaTotal: Int = 0,
    val quantidadeFaltas: Int = 0,
    val totalMinutosAusenciaAbonada: Int = 0,
    val totalMinutosAusenciaNaoAbonada: Int = 0,
    val diasDescansoTotal: Int = 0,
    val quantidadeFerias: Int = 0,
    val quantidadeFeriados: Int = 0,
    val diasFolgasSemanaisTotal: Int = 0,
    val diasDescanso: Int = 0,
    val diasFeriado: Int = 0,
    val diasFerias: Int = 0,
    val diasFolga: Int = 0,
    val diasFolgaDayOff: Int = 0,
    val diasFolgaCompensacao: Int = 0,
    val diasAtestado: Int = 0,
    val diasFaltaJustificada: Int = 0,
    val diasFaltaInjustificada: Int = 0,
    val diasFaltas: Int = 0,
    val diasFeriasFeriadosDescanso: Int = 0,
    val totalMinutosAbonados: Int = 0,
    val totalMinutosTolerancia: Int = 0,
    val totalMinutosDeclaracoesAtestadosFaltas: Int = 0,
    val totalDiasPeriodo: Int = 0,
    val totalAjustesMinutos: Int = 0,
    val quantidadeDeclaracoes: Int = 0,
    val totalMinutosDeclaracoes: Int = 0,
    val quantidadeAtestados: Int = 0,
    val nomesFeriados: List<String> = emptyList(),
    // Previsão de dias futuros
    val diasDescansoFuturo: Int = 0,
    val diasFeriadoFuturo: Int = 0,
    val diasFeriasFuturo: Int = 0,
    val diasFolgaFuturo: Int = 0,
    val diasFuturos: Int = 0
) {
    val totalDiasAusencia: Int
        get() = diasFerias + diasFolga + diasAtestado + diasFaltaJustificada + diasFaltaInjustificada

    val temAusencias: Boolean get() = diasAusenciaTotal > 0
    val temTempoAbonado: Boolean get() = totalMinutosAbonados > 0
    val temToleranciaAplicada: Boolean get() = totalMinutosTolerancia > 0
    val temDeclaracoes: Boolean get() = quantidadeDeclaracoes > 0
    val temAtestados: Boolean get() = quantidadeAtestados > 0
    val temDiasFuturos: Boolean get() = diasFuturos > 0

    /** Total de faltas e atestados formatado */
    val ausenciasDescricao: String
        get() = buildString {
            val parts = mutableListOf<String>()
            if (quantidadeFaltas > 0) parts.add("$quantidadeFaltas Faltas")
            if (quantidadeAtestados > 0) parts.add("$quantidadeAtestados Atestados")
            append(parts.joinToString("/ "))
        }

    /** Descanso (Férias + Feriados) formatado */
    val descansoDescricao: String
        get() = buildString {
            val parts = mutableListOf<String>()
            if (quantidadeFerias > 0) parts.add("$quantidadeFerias Férias")
            if (quantidadeFeriados > 0) parts.add("$quantidadeFeriados Feriado")
            append(parts.joinToString("/ "))
        }

    /** Progresso da meta do período em porcentagem */
    val progressoMeta: Float
        get() = if (totalHorasUteisMinutos > 0) {
            val numerador =
                totalMinutosTrabalhados + (totalMinutosAusenciaAbonada - totalMinutosAusenciaNaoAbonada)
            (numerador.toFloat() / totalHorasUteisMinutos.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val porcentagemMeta: String
        get() = String.format(Locale.forLanguageTag("pt-BR"), "%.2f%%", progressoMeta * 100)

    /** Total de dias de faltas (justificadas + injustificadas) */
    val totalDiasFaltas: Int get() = diasFaltaJustificada + diasFaltaInjustificada

    /** Total de descansos (passado + futuro) */
    val totalDescanso: Int get() = diasDescanso + diasDescansoFuturo

    /** Total de feriados (passado + futuro) */
    val totalFeriados: Int get() = diasFeriado + diasFeriadoFuturo

    /** Total de férias (passado + futuro) */
    val totalFerias: Int get() = diasFerias + diasFeriasFuturo

    /** Total de folgas (passado + futuro, exceto day-off) */
    val totalFolgas: Int get() = diasFolgaCompensacao + diasFolgaFuturo

    /** Indica se há previsão de dias futuros */
    val temPrevisaoFutura: Boolean
        get() = diasDescansoFuturo > 0 || diasFeriadoFuturo > 0 ||
                diasFeriasFuturo > 0 || diasFolgaFuturo > 0
}

/**
 * Estado da interface da tela de Histórico.
 */
data class HistoryUiState(
    val nomeEmprego: String? = null,
    val apelidoEmprego: String? = null,
    val logoEmprego: String? = null,
    val diasHistorico: List<InfoDiaHistorico> = emptyList(),
    val periodoSelecionado: PeriodoHistorico = PeriodoHistorico.periodoAtual(),
    val periodosSelecionados: List<PeriodoHistorico> = listOf(periodoSelecionado),
    val periodosDisponiveis: List<PeriodoHistorico> = emptyList(),
    val showPeriodoSelector: Boolean = false,
    val diaInicioFechamento: Int = 1,
    val filtroAtivo: FiltroHistorico = FiltroHistorico.TODOS,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val csvParaExportar: String? = null,
    val errorMessage: String? = null,
    val diasExpandidos: List<LocalDate> = emptyList(),
    val resumoExpandido: Boolean = true,
    val visualizacaoCalendario: Boolean = false,
    val filtrosAtivos: Set<FiltroHistorico> = emptySet(),
    val saldosAcumuladosPorDia: Map<LocalDate, Int> = emptyMap(),
    val saldoInicialPeriodo: Int = 0,
    val resumoPeriodo: ResumoPeriodo = ResumoPeriodo(),
    val todasAusencias: List<Ausencia> = emptyList(),
    val todosFeriados: List<Feriado> = emptyList()
) {
    val hasRegistros: Boolean get() = diasHistorico.isNotEmpty()

    val totalDiasComRegistro: Int
        get() = diasHistorico.count { it.pontos.isNotEmpty() }

    /** Aplica o filtro ativo e restrição de período à lista de dias */
    val registrosFiltrados: List<InfoDiaHistorico>
        get() {
            // Se não for calendário, filtra estritamente pelo período selecionado
            val base = if (visualizacaoCalendario) {
                diasHistorico
            } else {
                diasHistorico.filter { dia ->
                    periodosSelecionados.any { p -> dia.data in p.dataInicio..p.dataFim }
                }
            }

            if (filtrosAtivos.isEmpty()) return base

            return base.filter { dia ->
                filtrosAtivos.any { filtro ->
                    when (filtro) {
                        FiltroHistorico.TODOS, FiltroHistorico.LISTA, FiltroHistorico.CALENDARIO -> true
                        FiltroHistorico.UTEIS -> dia.isDiaUtil
                        FiltroHistorico.COMPLETOS -> dia.isCompleto
                        FiltroHistorico.INCOMPLETOS -> dia.isIncompleto
                        FiltroHistorico.COM_PROBLEMAS -> dia.temProblemas
                        FiltroHistorico.FUTUROS -> dia.resumoDia.isFuturo
                        FiltroHistorico.DESCANSO -> dia.isDescanso && !dia.temFeriado && !(dia.ausencias.any { it.tipo == TipoAusencia.Ferias }) && !dia.isFalta
                        FiltroHistorico.FERIADOS -> dia.temFeriado && !(dia.ausencias.any { it.tipo == TipoAusencia.Ferias }) && !dia.isFalta
                        FiltroHistorico.FERIAS -> dia.ausencias.any { it.tipo == TipoAusencia.Ferias } && !dia.isFalta
                        FiltroHistorico.FOLGAS -> dia.ausencias.any { it.tipo == TipoAusencia.DayOff && it.tipoFolga != TipoFolga.DAY_OFF }
                        FiltroHistorico.DAY_OFF -> dia.ausencias.any { it.tipo == TipoAusencia.DayOff && it.tipoFolga == TipoFolga.DAY_OFF }
                        FiltroHistorico.ATESTADOS -> dia.ausencias.any { it.tipo == TipoAusencia.Atestado }
                        FiltroHistorico.DECLARACOES -> dia.declaracoes.isNotEmpty()
                        FiltroHistorico.FALTAS -> dia.isFalta
                    }
                }
            }
        }

    // Delegações para ResumoPeriodo
    val totalMinutosTrabalhados: Int get() = resumoPeriodo.totalMinutosTrabalhados
    val saldoTotalMinutos: Int get() = resumoPeriodo.saldoPeriodoMinutos
    val diasComProblemas: Int get() = resumoPeriodo.diasComProblemas
    val diasCompletos: Int get() = resumoPeriodo.diasCompletos

    /** Sempre pode avançar para períodos futuros */
    val podeIrProximoPeriodo: Boolean get() = true

    val isPeriodoAtual: Boolean get() = periodosSelecionados.any { it.contemHoje }
    val usaPeriodoRHCustomizado: Boolean get() = diaInicioFechamento != 1

    val periodoDescricao: String
        get() = when {
            periodosSelecionados.isEmpty() -> "Nenhum período"
            periodosSelecionados.size == 1 -> periodosSelecionados.first().descricaoFormatada
            else -> {
                val minData = periodosSelecionados.minOf { it.dataInicio }
                val maxData = periodosSelecionados.maxOf { it.dataFim }
                val locale = Locale.forLanguageTag("pt-BR")
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
                "${minData.format(formatter)} a ${maxData.format(formatter)}"
            }
        }

    val periodoSubtitulo: String?
        get() = if (usaPeriodoRHCustomizado) "Período RH: dia $diaInicioFechamento" else null

    /** Saldo acumulado total (anterior + período) */
    val saldoAcumuladoTotal: Int
        get() = saldoInicialPeriodo + resumoPeriodo.saldoPeriodoMinutos

    val dataMaisAntiga: LocalDate? get() = periodosSelecionados.minOfOrNull { it.dataInicio }
    val dataMaisRecente: LocalDate? get() = periodosSelecionados.maxOfOrNull { it.dataFim }

    /** Saldo acumulado do dia mais antigo selecionado */
    val saldoDiaMaisAntigo: Int
        get() = dataMaisAntiga?.let { saldosAcumuladosPorDia[it] } ?: saldoInicialPeriodo

    /** Saldo acumulado do dia mais recente selecionado */
    val saldoDiaMaisRecente: Int
        get() = dataMaisRecente?.let { saldosAcumuladosPorDia[it] } ?: saldoAcumuladoTotal

    fun saldoAcumuladoAte(data: LocalDate): Int =
        saldosAcumuladosPorDia[data] ?: saldoInicialPeriodo
}
