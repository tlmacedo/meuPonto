// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

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
    CALENDARIO("Calendário", "📅");

    companion object {
        /** Retorna apenas os filtros principais (para exibir nos chips) */
        val principais: List<FiltroHistorico>
            get() = entries.filter { !it.isSecundario }
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
            val locale = Locale("pt", "BR")
            return if (diaInicioFechamento == 1) {
                val formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", locale)
                dataInicio.format(formatter).replaceFirstChar { it.uppercase() }
            } else {
                val formatterDiaMes = DateTimeFormatter.ofPattern("dd/MM", locale)
                val formatterCompleto = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
                if (dataInicio.year == dataFim.year) {
                    "${dataInicio.format(formatterDiaMes)} a ${dataFim.format(formatterCompleto)}"
                } else {
                    "${dataInicio.format(formatterCompleto)} a ${dataFim.format(formatterCompleto)}"
                }
            }
        }

    val descricaoCurta: String
        get() {
            val locale = Locale("pt", "BR")
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

        fun periodoAtual(diaInicioFechamento: Int = 1) = fromPeriodoRH(LocalDate.now(), diaInicioFechamento)
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
    val isSemJornada: Boolean = false
) {
    // Delegações para ResumoDia
    val data: LocalDate get() = resumoDia.data
    val pontos get() = resumoDia.pontos
    val jornadaCompleta get() = resumoDia.jornadaCompleta
    val horasTrabalhadasMinutos get() = resumoDia.horasTrabalhadasMinutos
    val saldoDiaMinutos get() = resumoDia.saldoDiaMinutos
    val tipoDiaEspecial get() = resumoDia.tipoDiaEspecial
    val statusDia get() = resumoDia.statusDia
    val temProblemas get() = resumoDia.temProblemas
    val intervalos get() = resumoDia.intervalos
    val temIntervalo get() = resumoDia.temIntervalo

    // Propriedades calculadas
    val temFeriado: Boolean get() = feriado != null
    val temAusencia: Boolean get() = ausencias.isNotEmpty()

    /** É dia de descanso (fim de semana sem jornada) */
    val isDescanso: Boolean
        get() = isSemJornada && pontos.isEmpty() && feriado == null && ausencias.isEmpty()

    /** Declarações do dia */
    val declaracoes: List<Ausencia>
        get() = ausencias.filter { it.tipo == TipoAusencia.DECLARACAO }

    /** Atestados do dia */
    val atestados: List<Ausencia>
        get() = ausencias.filter { it.tipo == TipoAusencia.ATESTADO }

    /** Ausência principal (que não é declaração) */
    val ausenciaPrincipal: Ausencia?
        get() = ausencias.firstOrNull { it.tipo != TipoAusencia.DECLARACAO }

    /** Total de minutos abonados por declarações */
    val totalMinutosDeclaracoes: Int
        get() = declaracoes.sumOf { it.duracaoAbonoMinutos ?: 0 }

    /** Emoji do dia baseado no tipo */
    val emoji: String
        get() = when {
            temFeriado && pontos.isNotEmpty() -> "⭐"
            temFeriado -> "🎉"
            ausenciaPrincipal != null -> getEmojiAusencia(ausenciaPrincipal!!)
            isSemJornada -> "🛋️"
            resumoDia.isFuturo -> "🔮"
            jornadaCompleta -> "✅"
            pontos.isNotEmpty() && !jornadaCompleta -> if (resumoDia.isHoje) "🔄" else "⚠️"
            pontos.isEmpty() && !isSemJornada -> "⬜"
            else -> "📅"
        }

    private fun getEmojiAusencia(ausencia: Ausencia): String {
        return when (ausencia.tipo) {
            TipoAusencia.FERIAS -> "🏖️"
            TipoAusencia.ATESTADO -> "🏥"
            TipoAusencia.DECLARACAO -> "📄"
            TipoAusencia.FALTA_JUSTIFICADA -> "📝"
            TipoAusencia.FOLGA -> if (ausencia.tipoFolga == TipoFolga.DAY_OFF) "🎁" else "😴"
            TipoAusencia.FALTA_INJUSTIFICADA -> "❌"
        }
    }

    /** Descrição curta para exibição */
    val descricaoCurta: String?
        get() = when {
            temFeriado -> feriado?.nome
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
    val diasComProblemas: Int = 0,
    val diasDescanso: Int = 0,
    val diasFeriado: Int = 0,
    val diasFerias: Int = 0,
    val diasFolga: Int = 0,
    val diasFolgaDayOff: Int = 0,
    val diasFolgaCompensacao: Int = 0,
    val diasAtestado: Int = 0,
    val diasFaltaJustificada: Int = 0,
    val diasFaltaInjustificada: Int = 0,
    val totalMinutosAbonados: Int = 0,
    val totalMinutosTolerancia: Int = 0,
    val diasUteisSemRegistro: Int = 0,
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

    val temAusencias: Boolean get() = totalDiasAusencia > 0
    val temTempoAbonado: Boolean get() = totalMinutosAbonados > 0
    val temToleranciaAplicada: Boolean get() = totalMinutosTolerancia > 0
    val temDeclaracoes: Boolean get() = quantidadeDeclaracoes > 0
    val temAtestados: Boolean get() = quantidadeAtestados > 0
    val temDiasFuturos: Boolean get() = diasFuturos > 0

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

    /**
     * Total de dias úteis no período.
     * Dias úteis = Total - Descansos - Feriados - Férias - Folgas - Day-offs
     */
    val diasUteis: Int
        get() = totalDiasPeriodo - totalDescanso - totalFeriados - totalFerias - totalFolgas - diasFolgaDayOff
}

/**
 * Estado da interface da tela de Histórico.
 */
data class HistoryUiState(
    val nomeEmprego: String? = null,
    val apelidoEmprego: String? = null,
    val diasHistorico: List<InfoDiaHistorico> = emptyList(),
    val periodoSelecionado: PeriodoHistorico = PeriodoHistorico.periodoAtual(),
    val diaInicioFechamento: Int = 1,
    val filtroAtivo: FiltroHistorico = FiltroHistorico.TODOS,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val diaExpandido: LocalDate? = null,
    val saldosAcumuladosPorDia: Map<LocalDate, Int> = emptyMap(),
    val saldoInicialPeriodo: Int = 0,
    val resumoPeriodo: ResumoPeriodo = ResumoPeriodo(),
    val todasAusencias: List<Ausencia> = emptyList(),
    val todosFeriados: List<Feriado> = emptyList()
) {
    val hasRegistros: Boolean get() = diasHistorico.isNotEmpty()

    val totalDiasComRegistro: Int
        get() = diasHistorico.count { it.pontos.isNotEmpty() }

    /** Aplica o filtro ativo à lista de dias */
    val registrosFiltrados: List<InfoDiaHistorico>
        get() = when (filtroAtivo) {
            FiltroHistorico.TODOS -> diasHistorico
            FiltroHistorico.COMPLETOS -> diasHistorico.filter { it.jornadaCompleta }
            FiltroHistorico.INCOMPLETOS -> diasHistorico.filter {
                !it.jornadaCompleta && it.pontos.isNotEmpty() && !it.resumoDia.isFuturo
            }
            FiltroHistorico.COM_PROBLEMAS -> diasHistorico.filter { it.temProblemas }
            FiltroHistorico.FUTUROS -> diasHistorico.filter { it.resumoDia.isFuturo }
            FiltroHistorico.DESCANSO -> diasHistorico.filter { it.isDescanso }
            FiltroHistorico.FERIADOS -> diasHistorico.filter { it.temFeriado }
            FiltroHistorico.FERIAS -> diasHistorico.filter {
                it.ausencias.any { a -> a.tipo == TipoAusencia.FERIAS }
            }
            FiltroHistorico.FOLGAS -> diasHistorico.filter {
                it.ausencias.any { a -> a.tipo == TipoAusencia.FOLGA && a.tipoFolga != TipoFolga.DAY_OFF }
            }
            FiltroHistorico.DAY_OFF -> diasHistorico.filter {
                it.ausencias.any { a -> a.tipo == TipoAusencia.FOLGA && a.tipoFolga == TipoFolga.DAY_OFF }
            }
            FiltroHistorico.ATESTADOS -> diasHistorico.filter {
                it.ausencias.any { a -> a.tipo == TipoAusencia.ATESTADO }
            }
            FiltroHistorico.DECLARACOES -> diasHistorico.filter { it.declaracoes.isNotEmpty() }
            FiltroHistorico.FALTAS -> diasHistorico.filter {
                it.ausencias.any { a ->
                    a.tipo == TipoAusencia.FALTA_JUSTIFICADA || a.tipo == TipoAusencia.FALTA_INJUSTIFICADA
                }
            }
            FiltroHistorico.CALENDARIO -> diasHistorico
        }

    // Delegações para ResumoPeriodo
    val totalMinutosTrabalhados: Int get() = resumoPeriodo.totalMinutosTrabalhados
    val saldoTotalMinutos: Int get() = resumoPeriodo.saldoPeriodoMinutos
    val diasComProblemas: Int get() = resumoPeriodo.diasComProblemas
    val diasCompletos: Int get() = resumoPeriodo.diasCompletos

    /** Sempre pode avançar para períodos futuros */
    val podeIrProximoPeriodo: Boolean get() = true

    val isPeriodoAtual: Boolean get() = periodoSelecionado.contemHoje
    val usaPeriodoRHCustomizado: Boolean get() = diaInicioFechamento != 1

    val periodoDescricao: String get() = periodoSelecionado.descricaoFormatada
    val periodoSubtitulo: String?
        get() = if (usaPeriodoRHCustomizado) "Período RH: dia $diaInicioFechamento" else null

    /** Saldo acumulado total (anterior + período) */
    val saldoAcumuladoTotal: Int
        get() = saldoInicialPeriodo + resumoPeriodo.saldoPeriodoMinutos

    fun saldoAcumuladoAte(data: LocalDate): Int? = saldosAcumuladosPorDia[data]
}
