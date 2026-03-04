// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/SettingsUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada

/**
 * Resumo das informações de um emprego para exibição na tela de configurações.
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
data class EmpregoResumo(
    val emprego: Emprego,
    val configuracao: ConfiguracaoEmprego?,
    val versaoVigente: VersaoJornada?,
    val totalVersoes: Int = 0,
    val totalFeriados: Int = 0,
    val totalAusencias: Int = 0,
    val totalAjustes: Int = 0
) {
    val cargaHorariaFormatada: String
        get() = versaoVigente?.cargaHorariaDiariaFormatada ?: "08:00"

    val jornadaMaximaFormatada: String
        get() = versaoVigente?.jornadaMaximaFormatada ?: "10:00"

    val temVersoes: Boolean get() = totalVersoes > 0
    val temFeriados: Boolean get() = totalFeriados > 0
    val temAusencias: Boolean get() = totalAusencias > 0
    val temAjustes: Boolean get() = totalAjustes > 0
}

/**
 * Estado da tela de configurações.
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val empregoAtivo: EmpregoResumo? = null,
    val outrosEmpregos: List<EmpregoResumo> = emptyList(),
    val empregosArquivados: List<Emprego> = emptyList(),
    val totalEmpregos: Int = 0,
    val totalFeriadosGlobais: Int = 0,
    val mostrarSeletorEmprego: Boolean = false,
    val mostrarDialogNovoEmprego: Boolean = false,
    val appVersion: String = "",
    val buildNumber: String = ""
) {
    val temEmpregos: Boolean get() = empregoAtivo != null || outrosEmpregos.isNotEmpty()
    val isPrimeiroAcesso: Boolean get() = !temEmpregos && !isLoading
    val totalEmpregosAtivos: Int get() = (if (empregoAtivo != null) 1 else 0) + outrosEmpregos.size
    val todosEmpregosAtivos: List<EmpregoResumo> get() = listOfNotNull(empregoAtivo) + outrosEmpregos
    val versaoFormatada: String get() = if (buildNumber.isNotEmpty()) "$appVersion ($buildNumber)" else appVersion
}

sealed interface SettingsEvent {
    data class MostrarMensagem(val mensagem: String) : SettingsEvent
    data class NavegarParaEmprego(val empregoId: Long) : SettingsEvent
    data object NavegarParaNovoEmprego : SettingsEvent
}

sealed interface SettingsAction {
    data object AbrirSeletorEmprego : SettingsAction
    data object FecharSeletorEmprego : SettingsAction
    data class TrocarEmpregoAtivo(val empregoId: Long) : SettingsAction
    data object AbrirDialogNovoEmprego : SettingsAction
    data object FecharDialogNovoEmprego : SettingsAction
    data object LimparErro : SettingsAction
    data object Recarregar : SettingsAction
}
