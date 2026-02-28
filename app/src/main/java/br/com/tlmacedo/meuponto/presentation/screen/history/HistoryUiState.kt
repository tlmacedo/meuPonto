// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/history/HistoryUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.history

import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import java.time.LocalDate

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
 * Estado da interface da tela de Histórico.
 *
 * @property resumosPorDia Lista de resumos diários ordenados por data
 * @property mesSelecionado Mês atualmente selecionado para filtro
 * @property filtroAtivo Filtro de status aplicado
 * @property isLoading Indica se está carregando dados
 * @property errorMessage Mensagem de erro para exibição
 * @property diaExpandido Data do dia atualmente expandido (null = nenhum)
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.5.0 - Adicionados filtros, resumo e expansão de dias
 * @updated 3.0.0 - Refatorado para usar ResumoDia (single source of truth)
 */
data class HistoryUiState(
    val resumosPorDia: List<ResumoDia> = emptyList(),
    val mesSelecionado: LocalDate = LocalDate.now(),
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
    val registrosFiltrados: List<ResumoDia>
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
}
