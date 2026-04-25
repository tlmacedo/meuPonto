// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/global/GlobalSettingsUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.global

import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoData
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoHora
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.TemaEscuro
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Estado da tela de configurações globais.
 *
 * @author Thiago
 * @since 8.1.0
 */
data class GlobalSettingsUiState(
    val isLoading: Boolean = true,
    val preferencias: PreferenciasGlobais = PreferenciasGlobais.DEFAULT,
    val dialogAtivo: DialogTipo? = null,
    val isSaving: Boolean = false,
    val mensagemSucesso: String? = null,
    val mensagemErro: String? = null
) {
    val ultimoBackupFormatado: String
        get() {
            val local = preferencias.ultimoBackupLocal
            val nuvem = preferencias.ultimoBackupNuvem
            val maisRecente = maxOf(local, nuvem)

            return maisRecente.takeIf { it > 0 }?.let { timestamp ->
                val instant = Instant.ofEpochMilli(timestamp)
                val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                dateTime.format(formatter)
            } ?: "Nunca realizado"
        }
}

/**
 * Tipos de diálogos disponíveis.
 */
enum class DialogTipo {
    TEMA,
    FORMATO_DATA,
    FORMATO_HORA,
    PRIMEIRO_DIA_SEMANA,
    NOTIFICACOES,
    LOCALIZACAO
}

/**
 * Eventos da tela de configurações globais.
 */
sealed interface GlobalSettingsEvent {
    data class MostrarSucesso(val mensagem: String) : GlobalSettingsEvent
    data class MostrarErro(val mensagem: String) : GlobalSettingsEvent
    data object Voltar : GlobalSettingsEvent
}

/**
 * Ações do usuário na tela de configurações globais.
 */
sealed interface GlobalSettingsAction {
    data object Voltar : GlobalSettingsAction
    data class AbrirDialog(val tipo: DialogTipo) : GlobalSettingsAction
    data object FecharDialog : GlobalSettingsAction

    // Aparência
    data class AlterarTema(val tema: TemaEscuro) : GlobalSettingsAction
    data class AlterarCoresSistema(val usar: Boolean) : GlobalSettingsAction

    // Formatos
    data class AlterarFormatoData(val formato: FormatoData) : GlobalSettingsAction
    data class AlterarFormatoHora(val formato: FormatoHora) : GlobalSettingsAction
    data class AlterarPrimeiroDiaSemana(val dia: DayOfWeek) : GlobalSettingsAction

    // Notificações
    data class AlterarLembretePonto(val ativo: Boolean) : GlobalSettingsAction
    data class AlterarAlertaFeriado(val ativo: Boolean) : GlobalSettingsAction
    data class AlterarAlertaBancoHoras(val ativo: Boolean) : GlobalSettingsAction
    data class AlterarAntecedenciaFeriado(val dias: Int) : GlobalSettingsAction

    // Localização
    data class AlterarLocalizacaoPadrao(
        val nome: String,
        val latitude: Double?,
        val longitude: Double?
    ) : GlobalSettingsAction

    data class AlterarRaioGeofencing(val metros: Int) : GlobalSettingsAction
    data class AlterarRegistroAutomatico(val ativo: Boolean) : GlobalSettingsAction

    // Backup
    data class AlterarBackupAutomatico(val ativo: Boolean) : GlobalSettingsAction

    data object LimparMensagem : GlobalSettingsAction
}
