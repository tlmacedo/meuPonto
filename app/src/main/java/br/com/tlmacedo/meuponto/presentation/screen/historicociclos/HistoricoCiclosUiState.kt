// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/historicociclos/HistoricoCiclosUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.historicociclos

import br.com.tlmacedo.meuponto.domain.model.CicloBancoHoras

/**
 * Estado da UI da tela de Histórico de Ciclos do Banco de Horas.
 *
 * @property ciclos Lista de ciclos do banco de horas
 * @property cicloExpandido ID do ciclo expandido (null se nenhum)
 * @property isLoading Indica se está carregando dados
 * @property errorMessage Mensagem de erro, se houver
 * @property empregoNome Nome do emprego atual
 *
 * @author Thiago
 * @since 9.0.0
 */
data class HistoricoCiclosUiState(
    val ciclos: List<CicloBancoHoras> = emptyList(),
    val cicloExpandido: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val empregoNome: String = ""
) {
    /**
     * Verifica se há ciclos para exibir.
     */
    val hasCiclos: Boolean
        get() = ciclos.isNotEmpty()

    /**
     * Retorna o ciclo atual (se existir).
     */
    val cicloAtual: CicloBancoHoras?
        get() = ciclos.firstOrNull { it.isCicloAtual }

    /**
     * Retorna apenas os ciclos históricos (fechados).
     */
    val ciclosHistoricos: List<CicloBancoHoras>
        get() = ciclos.filter { !it.isCicloAtual }

    /**
     * Total de ciclos fechados.
     */
    val totalCiclosFechados: Int
        get() = ciclos.count { it.isFechado }

    /**
     * Saldo total acumulado de todos os ciclos fechados.
     */
    val saldoTotalFechamentos: Int
        get() = ciclos.filter { it.isFechado }.sumOf { it.fechamento?.saldoAnteriorMinutos ?: 0 }
}
