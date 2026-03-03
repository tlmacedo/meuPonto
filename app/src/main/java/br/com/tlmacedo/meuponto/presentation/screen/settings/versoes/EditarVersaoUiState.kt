// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/versoes/EditarVersaoUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Estado da tela de edição de versão de jornada.
 *
 * @author Thiago
 * @since 4.0.0
 */
data class EditarVersaoUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val versaoId: Long? = null,
    val isNovaVersao: Boolean = true,
    
    // Dados da versão
    val descricao: String = "",
    val dataInicio: LocalDate = LocalDate.now(),
    val dataFim: LocalDate? = null,
    val numeroVersao: Int = 1,
    val vigente: Boolean = true,
    
    // Configurações de jornada
    val jornadaMaximaDiariaMinutos: Int = 600,
    val intervaloMinimoInterjornadaMinutos: Int = 660,
    val toleranciaIntervaloMaisMinutos: Int = 0,
    
    // Horários por dia da semana
    val horarios: List<HorarioDiaSemana> = emptyList(),
    
    // UI State
    val secaoExpandida: SecaoVersao? = SecaoVersao.DADOS_BASICOS,
    val errorMessage: String? = null,
    
    // Pickers
    val showDataInicioPicker: Boolean = false,
    val showDataFimPicker: Boolean = false
) {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
    
    val tituloTela: String 
        get() = if (isNovaVersao) "Nova Versão" else "Editar Versão $numeroVersao"
    
    val textoBotaoSalvar: String 
        get() = if (isNovaVersao) "Criar Versão" else "Salvar Alterações"
    
    val dataInicioFormatada: String 
        get() = dataInicio.format(DATE_FORMATTER)
    
    val dataFimFormatada: String 
        get() = dataFim?.format(DATE_FORMATTER) ?: "Sem data fim (vigente)"
    
    val jornadaMaximaFormatada: String
        get() {
            val h = jornadaMaximaDiariaMinutos / 60
            val m = jornadaMaximaDiariaMinutos % 60
            return String.format("%02d:%02d", h, m)
        }
    
    val intervaloInterjornadaFormatado: String
        get() {
            val h = intervaloMinimoInterjornadaMinutos / 60
            val m = intervaloMinimoInterjornadaMinutos % 60
            return String.format("%02d:%02d", h, m)
        }
    
    val formularioValido: Boolean 
        get() = true // Sempre válido, valores têm defaults
}

/**
 * Seções do formulário de edição de versão.
 */
enum class SecaoVersao {
    DADOS_BASICOS,
    JORNADA,
    HORARIOS
}

/**
 * Eventos da tela de edição de versão.
 */
sealed interface EditarVersaoEvent {
    data class MostrarMensagem(val mensagem: String) : EditarVersaoEvent
    data object SalvoComSucesso : EditarVersaoEvent
    data object Voltar : EditarVersaoEvent
    data class NavegarParaHorarios(val versaoId: Long) : EditarVersaoEvent
}

/**
 * Ações da tela de edição de versão.
 */
sealed interface EditarVersaoAction {
    // Dados básicos
    data class AlterarDescricao(val descricao: String) : EditarVersaoAction
    data class AlterarDataInicio(val data: LocalDate) : EditarVersaoAction
    data class AlterarDataFim(val data: LocalDate?) : EditarVersaoAction
    
    // Configurações de jornada
    data class AlterarJornadaMaxima(val minutos: Int) : EditarVersaoAction
    data class AlterarIntervaloInterjornada(val minutos: Int) : EditarVersaoAction
    data class AlterarToleranciaIntervalo(val minutos: Int) : EditarVersaoAction
    
    // Pickers
    data class MostrarDataInicioPicker(val mostrar: Boolean) : EditarVersaoAction
    data class MostrarDataFimPicker(val mostrar: Boolean) : EditarVersaoAction
    
    // UI
    data class ToggleSecao(val secao: SecaoVersao) : EditarVersaoAction
    data object Salvar : EditarVersaoAction
    data object Cancelar : EditarVersaoAction
    data object LimparErro : EditarVersaoAction
    
    // Navegação
    data object ConfigurarHorarios : EditarVersaoAction
}
