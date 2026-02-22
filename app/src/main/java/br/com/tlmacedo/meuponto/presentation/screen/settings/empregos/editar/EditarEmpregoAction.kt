// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.Duration
import java.time.LocalDate

/**
 * Ações disponíveis na tela de edição de emprego.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Novas ações para ciclo de banco de horas
 */
sealed class EditarEmpregoAction {
    // Dados básicos
    data class AlterarNome(val nome: String) : EditarEmpregoAction()
    data class AlterarDataInicioTrabalho(val data: LocalDate?) : EditarEmpregoAction()

    // Jornada
    data class AlterarCargaHorariaDiaria(val duracao: Duration) : EditarEmpregoAction()
    data class AlterarJornadaMaximaDiaria(val minutos: Int) : EditarEmpregoAction()
    data class AlterarIntervaloMinimo(val minutos: Int) : EditarEmpregoAction()
    data class AlterarIntervaloInterjornada(val minutos: Int) : EditarEmpregoAction()

    // Tolerâncias
    data class AlterarToleranciaIntervaloMais(val minutos: Int) : EditarEmpregoAction()

    // NSR e Localização
    data class AlterarHabilitarNsr(val habilitado: Boolean) : EditarEmpregoAction()
    data class AlterarTipoNsr(val tipo: TipoNsr) : EditarEmpregoAction()
    data class AlterarHabilitarLocalizacao(val habilitado: Boolean) : EditarEmpregoAction()
    data class AlterarLocalizacaoAutomatica(val automatica: Boolean) : EditarEmpregoAction()

    // Validações
    data class AlterarExigeJustificativa(val exigir: Boolean) : EditarEmpregoAction()

    // Período RH
    data class AlterarPrimeiroDiaSemana(val dia: DiaSemana) : EditarEmpregoAction()
    data class AlterarDiaInicioFechamentoRH(val dia: Int) : EditarEmpregoAction()
    data class AlterarZerarSaldoPeriodoRH(val zerar: Boolean) : EditarEmpregoAction()

    // Banco de Horas - Ciclo
    data class AlterarBancoHorasHabilitado(val habilitado: Boolean) : EditarEmpregoAction()
    data class AlterarPeriodoBancoHoras(val valor: Int) : EditarEmpregoAction()
    data class AlterarDataInicioCicloBanco(val data: LocalDate?) : EditarEmpregoAction()
    data class AlterarZerarBancoAntesPeriodo(val zerar: Boolean) : EditarEmpregoAction()

    // UI
    data class ToggleSecao(val secao: SecaoFormulario) : EditarEmpregoAction()
    data object Salvar : EditarEmpregoAction()
    data object Cancelar : EditarEmpregoAction()
    data object LimparErro : EditarEmpregoAction()
}
