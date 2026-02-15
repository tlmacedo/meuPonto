// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.LocalDate
import java.time.LocalTime

/**
 * Ações possíveis na tela Home.
 *
 * Define todas as interações do usuário que podem modificar o estado
 * da tela principal ou disparar navegação.
 *
 * @author Thiago
 * @since 2.0.0
 */
sealed interface HomeAction {

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE REGISTRO DE PONTO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Registra ponto com horário atual.
     */
    data object RegistrarPontoAgora : HomeAction

    /**
     * Abre dialog para registrar ponto com horário manual.
     */
    data object AbrirTimePickerDialog : HomeAction

    /**
     * Fecha dialog de seleção de horário.
     */
    data object FecharTimePickerDialog : HomeAction

    /**
     * Registra ponto com horário específico.
     */
    data class RegistrarPontoManual(val hora: LocalTime) : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE EXCLUSÃO DE PONTO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Solicita confirmação para excluir um ponto.
     */
    data class SolicitarExclusao(val ponto: Ponto) : HomeAction

    /**
     * Cancela exclusão de ponto.
     */
    data object CancelarExclusao : HomeAction

    /**
     * Confirma exclusão do ponto.
     */
    data object ConfirmarExclusao : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE NAVEGAÇÃO POR DATA
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Navega para o dia anterior.
     */
    data object DiaAnterior : HomeAction

    /**
     * Navega para o próximo dia.
     */
    data object ProximoDia : HomeAction

    /**
     * Navega para hoje.
     */
    data object IrParaHoje : HomeAction

    /**
     * Seleciona uma data específica.
     */
    data class SelecionarData(val data: LocalDate) : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE EMPREGO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Abre o seletor de emprego.
     */
    data object AbrirSeletorEmprego : HomeAction

    /**
     * Fecha o seletor de emprego.
     */
    data object FecharSeletorEmprego : HomeAction

    /**
     * Seleciona um emprego como ativo.
     */
    data class SelecionarEmprego(val emprego: Emprego) : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE NAVEGAÇÃO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Navega para editar um ponto existente.
     */
    data class EditarPonto(val pontoId: Long) : HomeAction

    /**
     * Navega para tela de histórico.
     */
    data object NavegarParaHistorico : HomeAction

    /**
     * Navega para tela de configurações.
     */
    data object NavegarParaConfiguracoes : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES INTERNAS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Atualiza a hora atual (chamado pelo timer).
     */
    data object AtualizarHora : HomeAction

    /**
     * Limpa mensagem de erro.
     */
    data object LimparErro : HomeAction

    /**
     * Recarrega os dados da tela.
     */
    data object RecarregarDados : HomeAction
}
