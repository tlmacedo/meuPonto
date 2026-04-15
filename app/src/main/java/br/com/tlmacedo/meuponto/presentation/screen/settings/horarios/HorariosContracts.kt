// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/horarios/HorariosContracts.kt

package br.com.tlmacedo.meuponto.presentation.screen.settings.horarios

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import java.time.Duration
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
    val empregoApelido: String? = null,
    val empregoLogo: String? = null,
    val versaoDescricao: String = "",
    val versaoJornada: VersaoJornada? = null,
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

    // Validações para o horário em edição
    val avisoJornadaExcedida: String?
        get() {
            val h = horarioEmEdicao ?: return null
            val max = versaoJornada?.jornadaMaximaDiariaMinutos ?: 600
            return if (h.cargaHorariaMinutos > max)
                "Carga horária (${h.cargaHorariaMinutos} min) excede o máximo de $max min."
            else null
        }

    val avisoTurnoMaximo: String?
        get() {
            val h = horarioEmEdicao ?: return null
            val max = versaoJornada?.turnoMaximoMinutos ?: 360
            
            val turno1 = if (h.entradaIdeal != null && h.saidaIntervaloIdeal != null)
                Duration.between(h.entradaIdeal, h.saidaIntervaloIdeal).toMinutes().toInt()
            else 0
            
            val turno2 = if (h.voltaIntervaloIdeal != null && h.saidaIdeal != null)
                Duration.between(h.voltaIntervaloIdeal, h.saidaIdeal).toMinutes().toInt()
            else 0

            return if (turno1 > max || turno2 > max)
                "Atenção: Turno sem intervalo acima de ${max / 60}h pode violar regras."
            else null
        }

    val avisoIntervaloMinimo: String?
        get() {
            val h = horarioEmEdicao ?: return null
            val min = h.intervaloMinimoMinutos
            
            val intervaloReal = if (h.saidaIntervaloIdeal != null && h.voltaIntervaloIdeal != null)
                Duration.between(h.saidaIntervaloIdeal, h.voltaIntervaloIdeal).toMinutes().toInt()
            else null

            return if (intervaloReal != null && intervaloReal < min)
                "Atenção: Intervalo configurado (${intervaloReal} min) é menor que o mínimo de $min min."
            else null
        }

    val canSaveHorario: Boolean
        get() {
            val h = horarioEmEdicao ?: return false
            val max = versaoJornada?.jornadaMaximaDiariaMinutos ?: 600
            return h.cargaHorariaMinutos <= max
        }
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
