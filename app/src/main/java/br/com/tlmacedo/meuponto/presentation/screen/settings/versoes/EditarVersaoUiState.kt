package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Estado da tela de edição de versão de jornada.
 */
data class EditarVersaoUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isNovaVersao: Boolean = true,
    val empregoId: Long = 0L,  // ✅ Adicionado
    val versaoId: Long? = null,
    val descricao: String = "",
    val dataInicio: LocalDate = LocalDate.now(),
    val dataFim: LocalDate? = null,
    val numeroVersao: Int = 1,
    val vigente: Boolean = false,
    val jornadaMaximaDiariaMinutos: Int = 600,
    val intervaloMinimoInterjornadaMinutos: Int = 660,
    val toleranciaIntervaloMaisMinutos: Int = 0,
    val turnoMaximoMinutos: Int = 360,

    // Carga horária
    val cargaHorariaDiariaMinutos: Int = 480,
    val acrescimoMinutosDiasPontes: Int = 12,
    val cargaHorariaSemanalMinutos: Int = 2460,

    // Período/Saldo
    val primeiroDiaSemana: DiaSemana = DiaSemana.SEGUNDA,
    val diaInicioFechamentoRH: Int = 1,
    val zerarSaldoSemanal: Boolean = false,
    val zerarSaldoPeriodoRH: Boolean = false,
    val ocultarSaldoTotal: Boolean = false,

    // Banco de Horas
    val bancoHorasHabilitado: Boolean = false,
    val periodoBancoSemanas: Int = 0,
    val periodoBancoMeses: Int = 0,
    val dataInicioCicloBancoAtual: LocalDate? = null,
    val diasUteisLembreteFechamento: Int = 3,
    val habilitarSugestaoAjuste: Boolean = false,
    val zerarBancoAntesPeriodo: Boolean = false,

    // Validação
    val exigeJustificativaInconsistencia: Boolean = false,

    val horarios: List<HorarioDiaSemana> = emptyList(),
    val showDataInicioPicker: Boolean = false,
    val showDataFimPicker: Boolean = false,
    val showDataInicioCicloBancoPicker: Boolean = false,
    val secaoExpandida: SecaoVersao? = SecaoVersao.INFORMACOES_BASICAS,
    val errorMessage: String? = null
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val dataInicioFormatada: String
        get() = dataInicio.format(dateFormatter)

    val dataFimFormatada: String?
        get() = dataFim?.format(dateFormatter)

    val jornadaMaximaFormatada: String
        get() = formatarMinutosEmHoras(jornadaMaximaDiariaMinutos)

    val intervaloInterjornadaFormatado: String
        get() = formatarMinutosEmHoras(intervaloMinimoInterjornadaMinutos)

    val toleranciaIntervaloFormatada: String
        get() = "${toleranciaIntervaloMaisMinutos} min"

    val podeSalvar: Boolean
        get() = !isSaving && empregoId > 0L

    val titulo: String
        get() = if (descricao.isNotBlank()) descricao else "Versão $numeroVersao"

    private fun formatarMinutosEmHoras(minutos: Int): String {
        val horas = minutos / 60
        val mins = minutos % 60
        return String.format("%02d:%02d", horas, mins)
    }
}
