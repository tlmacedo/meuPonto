// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/lixeira/LixeiraUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.lixeira

import br.com.tlmacedo.meuponto.domain.model.Ponto

/**
 * Estado da UI da tela de Lixeira.
 *
 * @author Thiago
 * @since 9.2.0
 * @updated 11.0.0 - Refatorado para soft delete
 */
data class LixeiraUiState(
    // Dados
    val pontosNaLixeira: List<PontoLixeiraItem> = emptyList(),
    val isLoading: Boolean = false,
    val mensagemErro: String? = null,

    // Seleção
    val modoSelecao: Boolean = false,
    val pontosSelecionados: Set<Long> = emptySet(),

    // Filtros e ordenação
    val filtroEmpregoId: Long? = null,
    val ordenacao: OrdenacaoLixeira = OrdenacaoLixeira.DATA_EXCLUSAO_DESC,

    // Diálogos
    val showConfirmacaoRestaurar: Boolean = false,
    val showConfirmacaoExcluir: Boolean = false,
    val showConfirmacaoEsvaziar: Boolean = false,
    val pontoParaAcao: Ponto? = null
) {
    /**
     * Lista de pontos filtrados e ordenados para exibição.
     */
    val pontosFiltrados: List<PontoLixeiraItem>
        get() {
            val filtrados = if (filtroEmpregoId != null) {
                pontosNaLixeira.filter { it.ponto.empregoId == filtroEmpregoId }
            } else {
                pontosNaLixeira
            }

            return when (ordenacao) {
                OrdenacaoLixeira.DATA_EXCLUSAO_DESC -> filtrados.sortedByDescending { it.ponto.deletedAt }
                OrdenacaoLixeira.DATA_EXCLUSAO_ASC -> filtrados.sortedBy { it.ponto.deletedAt }
                OrdenacaoLixeira.DIAS_RESTANTES_ASC -> filtrados.sortedBy { it.diasRestantes }
                OrdenacaoLixeira.DIAS_RESTANTES_DESC -> filtrados.sortedByDescending { it.diasRestantes }
                OrdenacaoLixeira.DATA_PONTO_DESC -> filtrados.sortedByDescending { it.ponto.data }
                OrdenacaoLixeira.DATA_PONTO_ASC -> filtrados.sortedBy { it.ponto.data }
            }
        }

    /**
     * Quantidade de itens na lixeira.
     */
    val quantidadeItens: Int get() = pontosNaLixeira.size

    /**
     * Quantidade de itens selecionados.
     */
    val quantidadeSelecionados: Int get() = pontosSelecionados.size

    /**
     * Indica se a lixeira está vazia.
     */
    val isEmpty: Boolean get() = pontosNaLixeira.isEmpty()

    /**
     * Indica se todos os itens filtrados estão selecionados.
     */
    val todosSelecionados: Boolean
        get() = pontosFiltrados.isNotEmpty() &&
                pontosFiltrados.all { it.id in pontosSelecionados }

    /**
     * Quantidade de itens que expiram em breve (≤ 7 dias).
     */
    val quantidadeExpirandoEmBreve: Int
        get() = pontosNaLixeira.count { it.expirandoEmBreve }
}

/**
 * Item de ponto na lixeira com informações adicionais.
 */
data class PontoLixeiraItem(
    val id: Long,
    val ponto: Ponto,
    val nomeEmprego: String,
    val diasRestantes: Int,
    val expirandoEmBreve: Boolean
)

/**
 * Opções de ordenação da lixeira.
 */
enum class OrdenacaoLixeira(val label: String) {
    DATA_EXCLUSAO_DESC("Excluídos recentemente"),
    DATA_EXCLUSAO_ASC("Excluídos primeiro"),
    DIAS_RESTANTES_ASC("Expirando em breve"),
    DIAS_RESTANTES_DESC("Mais tempo restante"),
    DATA_PONTO_DESC("Data do ponto (recente)"),
    DATA_PONTO_ASC("Data do ponto (antigo)")
}
