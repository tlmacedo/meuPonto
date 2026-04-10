// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import android.net.Uri
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import br.com.tlmacedo.meuponto.domain.model.MotivoEdicao
import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.LocalDate
import java.time.LocalDateTime
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

    // ── NOVO MODAL DE REGISTRO (UNIFICADO) ──────────────────────────────────
    /** Abre o modal de registro unificado */
    data class AbrirRegistrarPontoModal(val dataHora: LocalDateTime) : HomeAction

    /** Fecha o modal de registro unificado */
    data object FecharRegistrarPontoModal : HomeAction

    /** Atualiza o NSR no modal de registro */
    data class AtualizarNsrRegistroModal(val nsr: String) : HomeAction

    /** Atualiza a foto no modal de registro */
    data class AtualizarFotoRegistroModal(val uri: Uri?, val origem: FotoOrigem = FotoOrigem.NENHUMA) : HomeAction

    /** Atualiza a hora no modal de registro */
    data class AtualizarHoraRegistroModal(val hora: LocalTime) : HomeAction

    /** Abre o seletor de hora no modal de registro */
    data object AbrirTimePickerRegistroModal : HomeAction

    /** Fecha o seletor de hora no modal de registro */
    data object FecharTimePickerRegistroModal : HomeAction

    /** Solicita captura de localização no modal */
    data object CapturarLocalizacaoRegistroModal : HomeAction

    /** Confirma o registro do ponto a partir do modal */
    data object ConfirmarRegistroPontoModal : HomeAction

    /** Atualiza a observação no modal de registro */
    data class AtualizarObservacaoRegistroModal(val observacao: String) : HomeAction

    // ── FOTO DE COMPROVANTE (Ações globais/fluxo antigo) ─────────────────────
    /** Abre o diálogo de seleção de fonte da foto (Câmera/Galeria) */
    data object AbrirFotoSourceDialog : HomeAction

    /** Fecha o diálogo de seleção de fonte da foto */
    data object FecharFotoSourceDialog : HomeAction

    /** Confirma a foto capturada pela câmera */
    data object ConfirmarFotoCamera : HomeAction

    /** Seleciona uma foto da galeria */
    data class SelecionarFotoComprovante(val uri: Uri, val origem: FotoOrigem) : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // MODAIS DE PONTO (Nova implementação 7.2.0)
    // ══════════════════════════════════════════════════════════════════════

    // ── EDIÇÃO ──────────────────────────────────────────────────────────────
    /** Abre o modal de edição para um ponto */
    data class AbrirEdicaoModal(val ponto: Ponto) : HomeAction

    /** Fecha o modal de edição */
    data object FecharEdicaoModal : HomeAction

    /** Atualiza a foto no modal de edição */
    data class AtualizarFotoEdicaoModal(val uri: Uri?, val origem: FotoOrigem = FotoOrigem.NENHUMA) : HomeAction

    /** Remove a foto no modal de edição */
    data object RemoverFotoEdicaoModal : HomeAction

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

    /** Salva a foto editada no modal */
    data class SalvarFotoModal(val pontoId: Long, val path: String) : HomeAction

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
    data class MostrarMensagem(val mensagem: String) : HomeAction

    // ══════════════════════════════════════════════════════════════════════
    // AÇÕES DE CICLO DE BANCO DE HORAS
    // ══════════════════════════════════════════════════════════════════════

    data object AbrirDialogFechamentoCiclo : HomeAction
    data object FecharDialogFechamentoCiclo : HomeAction
    data class ConfirmarFechamentoCiclo(val saldoAnterior: Long, val motivo: String) : HomeAction
    data object NavegarParaHistoricoCiclos : HomeAction
}
