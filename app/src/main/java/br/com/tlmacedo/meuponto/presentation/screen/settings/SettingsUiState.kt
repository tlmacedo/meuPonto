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
        get() = configuracao?.cargaHorariaDiariaFormatada ?: "08:12"

    val jornadaMaximaFormatada: String
        get() = versaoVigente?.jornadaMaximaFormatada
            ?: configuracao?.jornadaMaximaDiariaFormatada
            ?: "10:00"

    val temVersoes: Boolean get() = totalVersoes > 0
    val temFeriados: Boolean get() = totalFeriados > 0
    val temAusencias: Boolean get() = totalAusencias > 0
    val temAjustes: Boolean get() = totalAjustes > 0
}

/**
 * Estado da tela de configurações.
 *
 * @author Thiago
 * @since 3.0.0
 */
data class SettingsUiState(
    // Estado de carregamento
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Empregos
    val empregoAtivo: EmpregoResumo? = null,
    val outrosEmpregos: List<EmpregoResumo> = emptyList(),
    val empregosArquivados: List<Emprego> = emptyList(),

    // Contadores globais
    val totalEmpregos: Int = 0,
    val totalFeriadosGlobais: Int = 0,

    // UI State
    val mostrarSeletorEmprego: Boolean = false,
    val mostrarDialogNovoEmprego: Boolean = false,

    // App Info
    val appVersion: String = "",
    val buildNumber: String = ""
) {
    /** Indica se há empregos cadastrados */
    val temEmpregos: Boolean get() = empregoAtivo != null || outrosEmpregos.isNotEmpty()

    /** Indica se é o primeiro acesso (nenhum emprego cadastrado) */
    val isPrimeiroAcesso: Boolean get() = !temEmpregos && !isLoading

    /** Total de empregos ativos (não arquivados) */
    val totalEmpregosAtivos: Int get() = (if (empregoAtivo != null) 1 else 0) + outrosEmpregos.size

    /** Lista completa de empregos ativos para seleção */
    val todosEmpregosAtivos: List<EmpregoResumo>
        get() = listOfNotNull(empregoAtivo) + outrosEmpregos

    /** Versão formatada para exibição */
    val versaoFormatada: String
        get() = if (buildNumber.isNotEmpty()) "$appVersion ($buildNumber)" else appVersion
}

/**
 * Eventos únicos da tela de configurações.
 */
sealed interface SettingsEvent {
    data class MostrarMensagem(val mensagem: String) : SettingsEvent
    data class NavegarParaEmprego(val empregoId: Long) : SettingsEvent
    data object NavegarParaNovoEmprego : SettingsEvent
}

/**
 * Ações da tela de configurações.
 */
sealed interface SettingsAction {
    /** Abre o seletor de emprego */
    data object AbrirSeletorEmprego : SettingsAction

    /** Fecha o seletor de emprego */
    data object FecharSeletorEmprego : SettingsAction

    /** Troca o emprego ativo */
    data class TrocarEmpregoAtivo(val empregoId: Long) : SettingsAction

    /** Abre dialog para novo emprego */
    data object AbrirDialogNovoEmprego : SettingsAction

    /** Fecha dialog de novo emprego */
    data object FecharDialogNovoEmprego : SettingsAction

    /** Limpa mensagem de erro */
    data object LimparErro : SettingsAction

    /** Recarrega os dados */
    data object Recarregar : SettingsAction
}
