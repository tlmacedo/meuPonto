// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/list/AusenciasUiEvent.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias.list

import android.net.Uri

/**
 * Eventos da tela de ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
sealed interface AusenciasUiEvent {
    data object Voltar : AusenciasUiEvent
    data object NavegarParaNovaAusencia : AusenciasUiEvent
    data class NavegarParaEditarAusencia(val ausenciaId: Long) : AusenciasUiEvent
    data class MostrarMensagem(val mensagem: String) : AusenciasUiEvent
    data class MostrarErro(val mensagem: String) : AusenciasUiEvent
}

/**
 * Eventos do formulário de ausência.
 *
 * @updated 5.4.0 - Eventos para câmera e galeria
 */
sealed interface AusenciaFormUiEvent {
    data object Voltar : AusenciaFormUiEvent
    data object SalvoComSucesso : AusenciaFormUiEvent
    data class MostrarMensagem(val mensagem: String) : AusenciaFormUiEvent
    data class MostrarErro(val mensagem: String) : AusenciaFormUiEvent
    data class AbrirCamera(val uri: Uri) : AusenciaFormUiEvent
    data object AbrirGaleria : AusenciaFormUiEvent
}
