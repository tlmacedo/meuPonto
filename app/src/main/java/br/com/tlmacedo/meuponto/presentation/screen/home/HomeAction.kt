// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.LocalTime

/**
 * Ações possíveis na tela Home.
 *
 * Define todas as interações do usuário que podem modificar o estado
 * da tela principal ou disparar navegação.
 *
 * @author Thiago
 * @since 1.0.0
 */
sealed interface HomeAction {
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

    /**
     * Atualiza a hora atual (chamado pelo timer).
     */
    data object AtualizarHora : HomeAction
}
