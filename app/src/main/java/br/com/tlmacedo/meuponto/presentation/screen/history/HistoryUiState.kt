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
 * Wrapper que combina ResumoDia com o saldo acumulado do banco.
 *
 * @property resumoDia Resumo do dia de trabalho
 * @property saldoBancoAcumuladoMinutos Saldo acumulado do banco até este dia (inclusive)
 *
 * @author Thiago
 * @since 7.0.0
 */
data class ResumoDiaComBanco(
    val resumoDia: ResumoDia,
    val saldoBancoAcumuladoMinutos: Int
) {
    // Delegação das propriedades principais para facilitar acesso
    val data: LocalDate get() = resumoDia.data
    val horasTrabalhadasFormatadas: String get() = resumoDia.horasTrabalhadasFormatadas
    val saldoDiaFormatado: String get() = resumoDia.saldoDiaFormatado
    val saldoDiaMinutos: Int get() = resumoDia.saldoDiaMinutos
    val jornadaCompleta: Boolean get() = resumoDia.jornadaCompleta
    val temProblemas: Boolean get() = resumoDia.temProblemas
    val statusDia get() = resumoDia.statusDia
    val quantidadePontos: Int get() = resumoDia.quantidadePontos
    val cargaHorariaDiariaFormatada: String get() = resumoDia.cargaHorariaDiariaFormatada
    val intervalos get() = resumoDia.intervalos
    val temIntervalo: Boolean get() = resumoDia.temIntervalo
    val temToleranciaIntervaloAplicada: Boolean get() = resumoDia.temToleranciaIntervaloAplicada
    val minutosIntervaloReal: Int get() = resumoDia.minutosIntervaloReal
    val minutosIntervaloTotal: Int get() = resumoDia.minutosIntervaloTotal
    val temSaldoPositivo: Boolean get() = resumoDia.temSaldoPositivo
    val temSaldoNegativo: Boolean get() = resumoDia.temSaldoNegativo
    val horasTrabalhadasMinutos: Int get() = resumoDia.horasTrabalhadasMinutos
}

/**
 * Estado da interface da tela de Histórico.
 *
 * @property resumosPorDia Lista de resumos diários com saldo acumulado
 * @property mesSelecionado Mês atualmente selecionado para filtro
 * @property periodoInicio Data de início do período RH atual
 * @property periodoFim Data de fim do período RH atual
 * @property diaInicioFechamentoRH Dia configurado para início do ciclo RH
 * @property saldoInicialBancoMinutos Saldo inicial do banco (fechamento anterior)
 * @property filtroAtivo Filtro de status aplicado
 * @property isLoading Indica se está carregando dados
 * @property errorMessage Mensagem de erro para exibição
 * @property diaExpandido Data do dia atualmente expandido (null = nenhum)
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 7.0.0 - Adicionado suporte a saldo acumulado do banco
 */
data class HistoryUiState(
    val resumosPorDia: List<ResumoDiaComBanco> = emptyList(),
    val mesSelecionado: LocalDate = LocalDate.now(),
    val periodoInicio: LocalDate? = null,
    val periodoFim: LocalDate? = null,
    val diaInicioFechamentoRH: Int = 1,
    val saldoInicialBancoMinutos: Int = 0,
    val filtroAtivo: FiltroHistorico = FiltroHistorico.TODOS,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val diaExpandido: LocalDate? = null
) {
    /** Verifica se há registros para exibir */
    val hasRegistros: Boolean
        get() = resumosPorDia.isNotEmpty()

    /** Total de dias com registro no período */
    val totalDiasComRegistro: Int
        get() = resumosPorDia.size

    /** Registros filtrados conforme filtro ativo */
    val registrosFiltrados: List<ResumoDiaComBanco>
        get() = when (filtroAtivo) {
            FiltroHistorico.TODOS -> resumosPorDia
            FiltroHistorico.COMPLETOS -> resumosPorDia.filter { it.jornadaCompleta }
            FiltroHistorico.INCOMPLETOS -> resumosPorDia.filter { !it.jornadaCompleta }
            FiltroHistorico.COM_PROBLEMAS -> resumosPorDia.filter { it.temProblemas }
        }

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

    /** Verifica se pode navegar para o próximo mês */
    val podeIrProximoMes: Boolean
        get() = !mesSelecionado.plusMonths(1).isAfter(LocalDate.now())

    // ========================================================================
    // FORMATADORES PARA PERÍODO RH
    // ========================================================================

    private val formatadorMesAno = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))
    private val formatadorDataCurta = DateTimeFormatter.ofPattern("dd/MM", Locale("pt", "BR"))

    /**
     * Título do período formatado.
     */
    val tituloPeriodo: String
        get() {
            val mesFormatado = mesSelecionado.format(formatadorMesAno).replaceFirstChar { it.uppercase() }
            return mesFormatado
        }

    /**
     * Subtítulo com o período (apenas se ciclo RH != dia 1).
     */
    val subtituloPeriodo: String?
        get() {
            if (diaInicioFechamentoRH == 1 || periodoInicio == null || periodoFim == null) {
                return null
            }
            val inicioFormatado = periodoInicio.format(formatadorDataCurta)
            val fimFormatado = periodoFim.format(formatadorDataCurta)
            return "$inicioFormatado - $fimFormatado"
        }

    /**
     * Verifica se está usando ciclo RH customizado (não começa no dia 1).
     */
    val usaCicloRHCustomizado: Boolean
        get() = diaInicioFechamentoRH != 1
}
