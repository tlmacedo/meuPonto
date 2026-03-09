// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/horarios/HorariosContracts.kt

package br.com.tlmacedo.meuponto.presentation.screen.settings.horarios

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import java.time.LocalTime

/**
 * Estado da tela de gerenciamento de horários da versão de jornada.
 *
 * @author Thiago
 * @since 4.0.0
 */
data class HorariosUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val versaoJornadaId: Long = 0L,
    val empregoId: Long = 0L,
    val versaoDescricao: String = "",
    val horarios: List<HorarioDiaSemana> = emptyList(),
    val horarioEmEdicao: HorarioDiaSemana? = null,
    val mostrarDialogEdicao: Boolean = false,
    val mostrarDialogCopiar: Boolean = false,
    val diaSelecionadoParaCopiar: DiaSemana? = null,
    val mostrarTimePicker: Boolean = false,
    val campoTimePicker: CampoHorario? = null,
    val errorMessage: String? = null
) {
    val totalCargaHorariaSemanal: Int
        get() = horarios.filter { it.ativo }.sumOf { it.cargaHorariaMinutos }

    val totalCargaHorariaSemanalFormatada: String
        get() {
            val horas = totalCargaHorariaSemanal / 60
            val minutos = totalCargaHorariaSemanal % 60
            return String.format("%02d:%02d", horas, minutos)
        }

    val diasAtivos: Int
        get() = horarios.count { it.ativo }

    val temAlteracoesPendentes: Boolean
        get() = horarioEmEdicao != null
}

/**
 * Campos de horário editáveis via TimePicker.
 */
enum class CampoHorario {
    ENTRADA,
    SAIDA_INTERVALO,
    VOLTA_INTERVALO,
    SAIDA
}

/**
 * Ações da tela de horários.
 */
sealed interface HorariosAction {
    data object Recarregar : HorariosAction
    data class SelecionarDia(val horario: HorarioDiaSemana) : HorariosAction
    data class ToggleAtivo(val diaSemana: DiaSemana) : HorariosAction
    data class AlterarCargaHoraria(val minutos: Int) : HorariosAction
    data class AlterarIntervaloMinimo(val minutos: Int) : HorariosAction
    data class AlterarToleranciaIntervalo(val minutos: Int) : HorariosAction
    data class AbrirTimePicker(val campo: CampoHorario) : HorariosAction
    data class SelecionarHorario(val horario: LocalTime?) : HorariosAction
    data object FecharTimePicker : HorariosAction
    data object FecharDialogEdicao : HorariosAction
    data object SalvarHorario : HorariosAction
    data class AbrirDialogCopiar(val diaSemana: DiaSemana) : HorariosAction
    data object FecharDialogCopiar : HorariosAction
    data class CopiarParaDias(val diasDestino: List<DiaSemana>) : HorariosAction
    data object LimparHorariosIdeais : HorariosAction
    data object LimparErro : HorariosAction
}

/**
 * Eventos emitidos pela tela de horários.
 */
sealed interface HorariosEvent {
    data class MostrarMensagem(val mensagem: String) : HorariosEvent
    data object Voltar : HorariosEvent
}
