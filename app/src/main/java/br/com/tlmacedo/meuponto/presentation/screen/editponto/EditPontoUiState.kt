// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.editponto

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.model.TipoPonto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Estado da tela de edição de ponto.
 *
 * @author Thiago
 * @since 3.5.0
 */
data class EditPontoUiState(
    // Dados do ponto
    val pontoId: Long = 0,
    val pontoOriginal: Ponto? = null,
    val empregoId: Long = 0,
    val tipoPonto: TipoPonto = TipoPonto.ENTRADA,
    val indice: Int = 0,

    // Campos editáveis
    val data: LocalDate = LocalDate.now(),
    val hora: LocalTime = LocalTime.now(),
    val nsr: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val endereco: String = "",
    val observacao: String = "",
    val motivo: String = "",

    // Configurações do emprego
    val configuracao: ConfiguracaoEmprego? = null,

    // Estados de UI
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showTimePicker: Boolean = false,
    val showDatePicker: Boolean = false,
    val showLocationPicker: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val erro: String? = null
) {
    companion object {
        private val localeBR = Locale("pt", "BR")
        private val formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeBR)
        private val formatterHora = DateTimeFormatter.ofPattern("HH:mm", localeBR)
        private val formatterDataCompleta = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", localeBR)
    }

    // ========================================================================
    // Flags de configuração
    // ========================================================================

    val habilitarNsr: Boolean
        get() = configuracao?.habilitarNsr == true

    val tipoNsr: TipoNsr
        get() = configuracao?.tipoNsr ?: TipoNsr.NUMERICO

    val habilitarLocalizacao: Boolean
        get() = configuracao?.habilitarLocalizacao == true

    val localizacaoAutomatica: Boolean
        get() = configuracao?.localizacaoAutomatica == true

    // ========================================================================
    // Formatação
    // ========================================================================

    val dataFormatada: String
        get() = data.format(formatterData)

    val horaFormatada: String
        get() = hora.format(formatterHora)

    val dataCompletaFormatada: String
        get() = data.format(formatterDataCompleta).replaceFirstChar { it.uppercase() }

    val dataHora: LocalDateTime
        get() = LocalDateTime.of(data, hora)

    val localizacaoFormatada: String
        get() = when {
            endereco.isNotBlank() -> endereco
            latitude != null && longitude != null -> "%.6f, %.6f".format(latitude, longitude)
            else -> "Não definida"
        }

    val temLocalizacao: Boolean
        get() = latitude != null && longitude != null

    // ========================================================================
    // Validações
    // ========================================================================

    val motivoValido: Boolean
        get() = motivo.length >= 5

    val nsrValido: Boolean
        get() = !habilitarNsr || nsr.isNotBlank()

    val localizacaoValida: Boolean
        get() = !habilitarLocalizacao || temLocalizacao

    val podeSalvar: Boolean
        get() = motivoValido && nsrValido && localizacaoValida && !isSaving

    val erroMotivo: String?
        get() = when {
            motivo.isBlank() -> "Informe o motivo da edição"
            motivo.length < 5 -> "O motivo deve ter pelo menos 5 caracteres"
            else -> null
        }

    val erroNsr: String?
        get() = if (habilitarNsr && nsr.isBlank()) "NSR é obrigatório" else null

    val erroLocalizacao: String?
        get() = if (habilitarLocalizacao && !temLocalizacao) "Localização é obrigatória" else null

    // ========================================================================
    // Verificação de alterações
    // ========================================================================

    val temAlteracoes: Boolean
        get() {
            val original = pontoOriginal ?: return false
            return dataHora != original.dataHora ||
                    nsr != (original.nsr ?: "") ||
                    latitude != original.latitude ||
                    longitude != original.longitude ||
                    endereco != (original.endereco ?: "") ||
                    observacao != (original.observacao ?: "")
        }

    val alteracoesResumo: String
        get() {
            val original = pontoOriginal ?: return ""
            val alteracoes = mutableListOf<String>()

            if (dataHora != original.dataHora) {
                alteracoes.add("Horário: ${original.horaFormatada} → $horaFormatada")
            }
            if (nsr != (original.nsr ?: "")) {
                alteracoes.add("NSR: ${original.nsr ?: "(vazio)"} → ${nsr.ifBlank { "(vazio)" }}")
            }
            if (latitude != original.latitude || longitude != original.longitude) {
                alteracoes.add("Localização alterada")
            }
            if (observacao != (original.observacao ?: "")) {
                alteracoes.add("Observação alterada")
            }

            return alteracoes.joinToString("\n")
        }
}
