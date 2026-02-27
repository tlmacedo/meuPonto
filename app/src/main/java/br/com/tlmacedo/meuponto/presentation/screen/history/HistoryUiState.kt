// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Filtros disponíveis para a tela de histórico.
 */
enum class FiltroHistorico(val descricao: String) {
    TODOS("Todos"),
    COMPLETOS("Completos"),
    INCOMPLETOS("Incompletos"),
    COM_PROBLEMAS("Com problemas")
}

/**
 * Representa um período de exibição no histórico.
 *
 * @property dataInicio Data de início do período (inclusive)
 * @property dataFim Data de fim do período (inclusive)
 * @property diaInicioFechamento Dia do mês que inicia o período RH (1-28)
 *
 * @author Thiago
 * @since 4.0.0
 */
data class PeriodoHistorico(
    val dataInicio: LocalDate,
    val dataFim: LocalDate,
    val diaInicioFechamento: Int = 1
) {
    /**
     * Descrição formatada do período.
     *
     * Exemplos:
     * - Período normal (dia 1): "Fevereiro de 2026"
     * - Período customizado (dia 16): "16/01 a 15/02/2026"
     */
    val descricaoFormatada: String
        get() {
            val locale = Locale("pt", "BR")

            return if (diaInicioFechamento == 1) {
                // Período coincide com mês calendário
                val formatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", locale)
                dataInicio.format(formatter).replaceFirstChar { it.uppercase() }
            } else {
                // Período customizado - exibe intervalo completo
                val formatterDiaMes = DateTimeFormatter.ofPattern("dd/MM", locale)
                val formatterCompleto = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)

                if (dataInicio.year == dataFim.year) {
                    "${dataInicio.format(formatterDiaMes)} a ${dataFim.format(formatterCompleto)}"
                } else {
                    "${dataInicio.format(formatterCompleto)} a ${dataFim.format(formatterCompleto)}"
                }
            }
        }

    /**
     * Descrição curta do período (para badges/chips).
     *
     * Exemplos:
     * - "Fev/2026"
     * - "16/01-15/02"
     */
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

    /**
     * Verifica se este período contém a data de hoje.
     */
    val contemHoje: Boolean
        get() {
            val hoje = LocalDate.now()
            return !hoje.isBefore(dataInicio) && !hoje.isAfter(dataFim)
        }

    /**
     * Verifica se este período é futuro (começa após hoje).
     */
    val isFuturo: Boolean
        get() = dataInicio.isAfter(LocalDate.now())

    /**
     * Verifica se este período é passado (terminou antes de hoje).
     */
    val isPassado: Boolean
        get() = dataFim.isBefore(LocalDate.now())

    companion object {
        /**
         * Cria um período baseado no mês calendário (dia 1 ao último dia).
         */
        fun fromMesCalendario(dataReferencia: LocalDate): PeriodoHistorico {
            return PeriodoHistorico(
                dataInicio = dataReferencia.withDayOfMonth(1),
                dataFim = dataReferencia.withDayOfMonth(dataReferencia.lengthOfMonth()),
                diaInicioFechamento = 1
            )
        }

        /**
         * Cria um período RH baseado na configuração de fechamento.
         *
         * @param dataReferencia Qualquer data dentro do período desejado
         * @param diaInicioFechamento Dia do mês que inicia o período (1-28)
         */
        fun fromPeriodoRH(dataReferencia: LocalDate, diaInicioFechamento: Int): PeriodoHistorico {
            val diaFechamento = diaInicioFechamento.coerceIn(1, 28)

            // Se dia de fechamento é 1, usa mês calendário
            if (diaFechamento == 1) {
                return fromMesCalendario(dataReferencia)
            }

            // Calcula início do período
            val dataInicio = if (dataReferencia.dayOfMonth >= diaFechamento) {
                dataReferencia.withDayOfMonth(diaFechamento)
            } else {
                dataReferencia.minusMonths(1).withDayOfMonth(diaFechamento)
            }

            // Fim é o dia anterior ao início do próximo período
            val dataFim = dataInicio.plusMonths(1).minusDays(1)

            return PeriodoHistorico(
                dataInicio = dataInicio,
                dataFim = dataFim,
                diaInicioFechamento = diaFechamento
            )
        }

        /**
         * Cria o período atual (que contém hoje).
         */
        fun periodoAtual(diaInicioFechamento: Int = 1): PeriodoHistorico {
            return fromPeriodoRH(LocalDate.now(), diaInicioFechamento)
        }
    }

    /**
     * Retorna o período anterior a este.
     */
    fun periodoAnterior(): PeriodoHistorico {
        return fromPeriodoRH(dataInicio.minusDays(1), diaInicioFechamento)
    }

    /**
     * Retorna o próximo período após este.
     */
    fun proximoPeriodo(): PeriodoHistorico {
        return fromPeriodoRH(dataFim.plusDays(1), diaInicioFechamento)
    }
}

/**
 * Estado da interface da tela de Histórico.
 *
 * @property resumosPorDia Lista de resumos diários ordenados por data
 * @property periodoSelecionado Período atualmente selecionado para exibição
 * @property diaInicioFechamento Dia do mês que inicia o período RH (da configuração)
 * @property filtroAtivo Filtro de status aplicado
 * @property isLoading Indica se está carregando dados
 * @property errorMessage Mensagem de erro para exibição
 * @property diaExpandido Data do dia atualmente expandido (null = nenhum)
 * @property saldosAcumuladosPorDia Mapa com saldo acumulado até cada dia
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.5.0 - Adicionados filtros, resumo e expansão de dias
 * @updated 3.0.0 - Refatorado para usar ResumoDia (single source of truth)
 * @updated 4.0.0 - Suporte a período RH customizado (diaInicioFechamentoRH)
 */
data class HistoryUiState(
    val resumosPorDia: List<ResumoDia> = emptyList(),
    val periodoSelecionado: PeriodoHistorico = PeriodoHistorico.periodoAtual(),
    val diaInicioFechamento: Int = 1,
    val filtroAtivo: FiltroHistorico = FiltroHistorico.TODOS,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val diaExpandido: LocalDate? = null,
    /** Mapa de data -> saldo acumulado do banco até aquele dia (em minutos) */
    val saldosAcumuladosPorDia: Map<LocalDate, Int> = emptyMap(),
    /** Saldo inicial do período (vindo de fechamentos anteriores) */
    val saldoInicialPeriodo: Int = 0
) {
    // ========================================================================
    // Propriedades de Compatibilidade (deprecated)
    // ========================================================================

    /**
     * @deprecated Use periodoSelecionado.dataInicio em vez disso
     */
    @Deprecated("Use periodoSelecionado", ReplaceWith("periodoSelecionado.dataInicio"))
    val mesSelecionado: LocalDate
        get() = periodoSelecionado.dataInicio

    // ========================================================================
    // Verificações de Estado
    // ========================================================================

    /** Verifica se há registros para exibir */
    val hasRegistros: Boolean
        get() = resumosPorDia.isNotEmpty()

    /** Total de dias com registro no período */
    val totalDiasComRegistro: Int
        get() = resumosPorDia.size

    /** Registros filtrados conforme filtro ativo */
    val registrosFiltrados: List<ResumoDia>
        get() = when (filtroAtivo) {
            FiltroHistorico.TODOS -> resumosPorDia
            FiltroHistorico.COMPLETOS -> resumosPorDia.filter { it.jornadaCompleta }
            FiltroHistorico.INCOMPLETOS -> resumosPorDia.filter { !it.jornadaCompleta }
            FiltroHistorico.COM_PROBLEMAS -> resumosPorDia.filter { it.temProblemas }
        }

    // ========================================================================
    // Cálculos de Totais
    // ========================================================================

    /** Total de minutos trabalhados no período (apenas dias completos) */
    val totalMinutosTrabalhados: Int
        get() = resumosPorDia
            .filter { it.jornadaCompleta }
            .sumOf { it.horasTrabalhadasMinutos }

    /** Saldo total do período em minutos */
    val saldoTotalMinutos: Int
        get() = resumosPorDia
            .filter { it.jornadaCompleta }
            .sumOf { it.saldoDiaMinutos }

    /** Quantidade de dias com problemas */
    val diasComProblemas: Int
        get() = resumosPorDia.count { it.temProblemas }

    /** Quantidade de dias completos */
    val diasCompletos: Int
        get() = resumosPorDia.count { it.jornadaCompleta }

    // ========================================================================
    // Navegação
    // ========================================================================

    /**
     * Verifica se pode navegar para o próximo período.
     * Não permite navegar para períodos totalmente futuros.
     */
    val podeIrProximoPeriodo: Boolean
        get() = !periodoSelecionado.proximoPeriodo().isFuturo

    /**
     * Verifica se o período atual está selecionado.
     */
    val isPeriodoAtual: Boolean
        get() = periodoSelecionado.contemHoje

    /**
     * Indica se está usando período RH customizado (diferente do mês calendário).
     */
    val usaPeriodoRHCustomizado: Boolean
        get() = diaInicioFechamento != 1

    // ========================================================================
    // Descrições para UI
    // ========================================================================

    /**
     * Descrição do período para exibição no navegador.
     */
    val periodoDescricao: String
        get() = periodoSelecionado.descricaoFormatada

    /**
     * Subtítulo com informação do período RH (se customizado).
     */
    val periodoSubtitulo: String?
        get() = if (usaPeriodoRHCustomizado) {
            "Período RH: dia $diaInicioFechamento"
        } else null

    /**
     * Obtém o saldo acumulado até uma data específica.
     */
    fun saldoAcumuladoAte(data: LocalDate): Int? {
        return saldosAcumuladosPorDia[data]
    }
}
