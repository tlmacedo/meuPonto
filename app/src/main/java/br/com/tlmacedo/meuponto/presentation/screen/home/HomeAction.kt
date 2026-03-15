// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import android.net.Uri
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.MotivoEdicao
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.LocalDate
import java.time.LocalTime

/**
 * Ações possíveis na tela Home.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 7.2.0 - Substituídas ações de edição inline por modais
 * @updated 9.0.0 - Adicionadas ações para foto de comprovante
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

    data class AtualizarNsr(val nsr: String) : HomeAction
    data object ConfirmarRegistroComNsr : HomeAction
    data object CancelarNsrDialog : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE FOTO DE COMPROVANTE
    // ══════════════════════════════════════════════════════════════════════

    data object AbrirFotoSourceDialog : HomeAction
    data object FecharFotoSourceDialog : HomeAction
    data object ConfirmarFotoCamera : HomeAction
    data class SelecionarFotoComprovante(val uri: Uri) : HomeAction
    data object RemoverFotoComprovante : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // MODAIS DE PONTO (Nova implementação 7.2.0)
    // ══════════════════════════════════════════════════════════════════════

    // ── EDIÇÃO ──────────────────────────────────────────────────────────────
    /** Abre o modal de edição para um ponto */
    data class AbrirEdicaoModal(val ponto: Ponto) : HomeAction

    /** Fecha o modal de edição */
    data object FecharEdicaoModal : HomeAction

    /** Salva as alterações do modal de edição */
    data class SalvarEdicaoModal(
        val pontoId: Long,
        val hora: LocalTime,
        val nsr: String?,
        val motivo: MotivoEdicao,
        val detalhes: String?
    ) : HomeAction

    // ── EXCLUSÃO ────────────────────────────────────────────────────────────
    /** Abre o modal de confirmação de exclusão */
    data class AbrirExclusaoModal(val ponto: Ponto) : HomeAction

    /** Fecha o modal de exclusão */
    data object FecharExclusaoModal : HomeAction

    /** Confirma a exclusão do ponto */
    data class ConfirmarExclusaoModal(val pontoId: Long, val motivo: String) : HomeAction

    // ── LOCALIZAÇÃO ─────────────────────────────────────────────────────────
    /** Abre o modal de visualização de localização */
    data class AbrirLocalizacaoModal(val ponto: Ponto) : HomeAction

    /** Fecha o modal de localização */
    data object FecharLocalizacaoModal : HomeAction

    // ── FOTO ────────────────────────────────────────────────────────────────
    /** Abre o modal de visualização de foto */
    data class AbrirFotoModal(val ponto: Ponto) : HomeAction

    /** Fecha o modal de foto */
    data object FecharFotoModal : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE EXCLUSÃO DE PONTO (legado - manter por compatibilidade)
    // ══════════════════════════════════════════════════════════════════════

    data class SolicitarExclusao(val ponto: Ponto) : HomeAction
    data object CancelarExclusao : HomeAction
    data class AtualizarMotivoExclusao(val motivo: String) : HomeAction
    data object ConfirmarExclusao : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE NAVEGAÇÃO POR DATA
    // ══════════════════════════════════════════════════════════════════════

    data object DiaAnterior : HomeAction
    data object ProximoDia : HomeAction
    data object IrParaHoje : HomeAction
    data class SelecionarData(val data: LocalDate) : HomeAction
    data object AbrirDatePicker : HomeAction
    data object FecharDatePicker : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE EMPREGO
    // ══════════════════════════════════════════════════════════════════════

    data object AbrirSeletorEmprego : HomeAction
    data object FecharSeletorEmprego : HomeAction
    data class SelecionarEmprego(val emprego: Emprego) : HomeAction
    data object NavegarParaNovoEmprego : HomeAction
    data object NavegarParaEditarEmprego : HomeAction
    data object AbrirMenuEmprego : HomeAction
    data object FecharMenuEmprego : HomeAction
    data object RecarregarConfiguracaoEmprego : HomeAction

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
