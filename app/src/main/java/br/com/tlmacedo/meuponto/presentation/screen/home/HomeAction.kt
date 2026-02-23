// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.LocalDate
import java.time.LocalTime

/**
 * Ações possíveis na tela Home.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 2.5.0 - Adicionadas ações para DatePicker
 * @updated 2.7.0 - Adicionadas ações para navegação de emprego (criar/editar)
 * @updated 3.7.0 - Adicionadas ações para NSR dialog
 * @updated 6.0.0 - Adicionada ação para motivo de exclusão (auditoria)
 */
sealed interface HomeAction {

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE REGISTRO DE PONTO
    // ══════════════════════════════════════════════════════════════════════

    data object RegistrarPontoAgora : HomeAction
    data object AbrirTimePickerDialog : HomeAction
    data object FecharTimePickerDialog : HomeAction
    data class RegistrarPontoManual(val hora: LocalTime) : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE NSR
    // ══════════════════════════════════════════════════════════════════════

    /** Atualiza o valor do NSR no dialog */
    data class AtualizarNsr(val nsr: String) : HomeAction

    /** Confirma o registro do ponto com NSR */
    data object ConfirmarRegistroComNsr : HomeAction

    /** Cancela o dialog de NSR */
    data object CancelarNsrDialog : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE EXCLUSÃO DE PONTO
    // ══════════════════════════════════════════════════════════════════════

    data class SolicitarExclusao(val ponto: Ponto) : HomeAction
    data object CancelarExclusao : HomeAction

    /** Atualiza o motivo da exclusão no dialog */
    data class AtualizarMotivoExclusao(val motivo: String) : HomeAction

    /** Confirma a exclusão com motivo */
    data object ConfirmarExclusao : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE NAVEGAÇÃO POR DATA
    // ══════════════════════════════════════════════════════════════════════

    data object DiaAnterior : HomeAction
    data object ProximoDia : HomeAction
    data object IrParaHoje : HomeAction
    data class SelecionarData(val data: LocalDate) : HomeAction

    /** Abre o DatePicker para selecionar uma data */
    data object AbrirDatePicker : HomeAction

    /** Fecha o DatePicker */
    data object FecharDatePicker : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE EMPREGO
    // ══════════════════════════════════════════════════════════════════════

    data object AbrirSeletorEmprego : HomeAction
    data object FecharSeletorEmprego : HomeAction
    data class SelecionarEmprego(val emprego: Emprego) : HomeAction

    /** Navega para criar um novo emprego */
    data object NavegarParaNovoEmprego : HomeAction

    /** Navega para editar o emprego ativo */
    data object NavegarParaEditarEmprego : HomeAction

    /** Abre o menu de opções do emprego (long press) */
    data object AbrirMenuEmprego : HomeAction

    /** Fecha o menu de opções do emprego */
    data object FecharMenuEmprego : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE NAVEGAÇÃO
    // ══════════════════════════════════════════════════════════════════════

    data class EditarPonto(val pontoId: Long) : HomeAction
    data object NavegarParaHistorico : HomeAction
    data object NavegarParaConfiguracoes : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES INTERNAS
    // ══════════════════════════════════════════════════════════════════════

    data object AtualizarHora : HomeAction
    data object LimparErro : HomeAction
    data object RecarregarDados : HomeAction


    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE CICLO DE BANCO DE HORAS
    // ══════════════════════════════════════════════════════════════════════

    data object AbrirDialogFechamentoCiclo : HomeAction
    data object FecharDialogFechamentoCiclo : HomeAction
    data object ConfirmarFechamentoCiclo : HomeAction
    data object NavegarParaHistoricoCiclos : HomeAction
}
