// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/EmpregoSettingsUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import kotlin.math.absoluteValue

/**
 * Estado da tela de configurações do emprego.
 *
 * @author Thiago
 * @since 8.2.0
 */
data class EmpregoSettingsUiState(
    val isLoading: Boolean = true,
    val emprego: Emprego? = null,
    val versaoVigente: VersaoJornada? = null,
    val totalVersoes: Int = 0,
    val totalAjustes: Int = 0,
    val totalAusencias: Int = 0,
    val saldoBancoHorasMinutos: Int = 0,
    val errorMessage: String? = null
) {
    val saldoBancoHorasFormatado: String
        get() {
            val horas = saldoBancoHorasMinutos.absoluteValue / 60
            val minutos = saldoBancoHorasMinutos.absoluteValue % 60
            val sinal = if (saldoBancoHorasMinutos >= 0) "+" else "-"
            return "$sinal${String.format("%02d:%02d", horas, minutos)}"
        }
}
