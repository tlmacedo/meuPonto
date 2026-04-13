// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoAction.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.LocalDate

/**
 * Ações disponíveis na tela de edição de emprego.
 */
sealed class EditarEmpregoAction {
    // Dados básicos
    data class AlterarNome(val nome: String) : EditarEmpregoAction()
    data class AlterarApelido(val apelido: String) : EditarEmpregoAction()
    data class AlterarEndereco(val endereco: String) : EditarEmpregoAction()
    data class AlterarDescricao(val descricao: String) : EditarEmpregoAction()
    data class AlterarDataInicioTrabalho(val data: LocalDate?) : EditarEmpregoAction()
    data class AlterarDataTerminoTrabalho(val data: LocalDate?) : EditarEmpregoAction()
    data class AlterarLogo(val uri: String?) : EditarEmpregoAction()

    // Configurações RH e Banco de Horas
    data class AlterarDiaInicioFechamentoRH(val dia: Int) : EditarEmpregoAction()
    data class AlterarBancoHorasHabilitado(val habilitado: Boolean) : EditarEmpregoAction()
    data class AlterarBancoHorasCicloMeses(val meses: Int) : EditarEmpregoAction()
    data class AlterarBancoHorasDataInicioCiclo(val data: LocalDate?) : EditarEmpregoAction()
    data class AlterarBancoHorasZerarAoFinalCiclo(val zerar: Boolean) : EditarEmpregoAction()
    data class AlterarExigeJustificativaInconsistencia(val exige: Boolean) : EditarEmpregoAction()

    // Opções de Registro
    data class AlterarHabilitarNsr(val habilitar: Boolean) : EditarEmpregoAction()
    data class AlterarTipoNsr(val tipo: TipoNsr) : EditarEmpregoAction()
    data class AlterarHabilitarLocalizacao(val habilitar: Boolean) : EditarEmpregoAction()
    data class AlterarLocalizacaoAutomatica(val habilitar: Boolean) : EditarEmpregoAction()
    data class AlterarExibirLocalizacaoDetalhes(val exibir: Boolean) : EditarEmpregoAction()
    data class AlterarFotoHabilitada(val habilitar: Boolean) : EditarEmpregoAction()
    data class AlterarFotoObrigatoria(val obrigatoria: Boolean) : EditarEmpregoAction()
    data class AlterarFotoValidarComprovante(val validar: Boolean) : EditarEmpregoAction()
    data class AlterarComentarioHabilitado(val habilitar: Boolean) : EditarEmpregoAction()
    data class AlterarComentarioObrigatorioHoraExtra(val obrigatorio: Boolean) : EditarEmpregoAction()
    data class AlterarExibirDuracaoTurno(val exibir: Boolean) : EditarEmpregoAction()
    data class AlterarExibirDuracaoIntervalo(val exibir: Boolean) : EditarEmpregoAction()

    // Cargo Inicial (para criação)
    data class AlterarFuncaoInicial(val funcao: String) : EditarEmpregoAction()
    data class AlterarSalarioInicial(val valor: Double?) : EditarEmpregoAction()

    // Salvar Granular
    data object SalvarDadosBasicos : EditarEmpregoAction()
    data object SalvarRHBank : EditarEmpregoAction()
    data object SalvarOpcoesRegistro : EditarEmpregoAction()

    // UI
    data class ToggleSecao(val secao: SecaoFormulario) : EditarEmpregoAction()
    data object Salvar : EditarEmpregoAction()
    data object Cancelar : EditarEmpregoAction()
    data object LimparErro : EditarEmpregoAction()
}
