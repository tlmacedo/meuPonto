// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/lixeira/LixeiraAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.lixeira

import br.com.tlmacedo.meuponto.domain.model.Ponto

/**
 * Ações que podem ser executadas na tela de Lixeira.
 *
 * @author Thiago
 * @since 9.2.0
 * @updated 11.0.0 - Refatorado para soft delete
 */
sealed interface LixeiraAction {

    // === Seleção ===
    data class ToggleSelecao(val pontoId: Long) : LixeiraAction
    data object SelecionarTodos : LixeiraAction
    data object LimparSelecao : LixeiraAction
    data object AtivarModoSelecao : LixeiraAction
    data object DesativarModoSelecao : LixeiraAction

    // === Restaurar ===
    data class SolicitarRestaurar(val ponto: Ponto) : LixeiraAction
    data object ConfirmarRestaurar : LixeiraAction
    data object RestaurarSelecionados : LixeiraAction
    data object CancelarRestaurar : LixeiraAction

    // === Excluir permanente ===
    data class SolicitarExcluir(val ponto: Ponto) : LixeiraAction
    data object ConfirmarExcluir : LixeiraAction
    data object ExcluirSelecionados : LixeiraAction
    data object CancelarExcluir : LixeiraAction

    // === Esvaziar lixeira ===
    data object SolicitarEsvaziarLixeira : LixeiraAction
    data object ConfirmarEsvaziarLixeira : LixeiraAction
    data object CancelarEsvaziarLixeira : LixeiraAction

    // === Filtros e ordenação ===
    data class FiltrarPorEmprego(val empregoId: Long?) : LixeiraAction
    data class AlterarOrdenacao(val ordenacao: OrdenacaoLixeira) : LixeiraAction

    // === Outros ===
    data object Recarregar : LixeiraAction
    data object LimparErro : LixeiraAction
    data object Voltar : LixeiraAction
}
