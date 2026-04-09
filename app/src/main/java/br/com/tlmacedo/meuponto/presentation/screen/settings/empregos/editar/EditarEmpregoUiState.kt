// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.PeriodoRH
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Estado da tela de edição/criação de emprego.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Refatoração completa do sistema de ciclos de banco de horas
 * @updated 9.0.0 - Adicionado habilitarFotoComprovante
 */
data class EditarEmpregoUiState(
    val empregoId: Long? = null,
    val isNovoEmprego: Boolean = true,
    val nome: String = "",
    val nomeErro: String? = null,
    val apelido: String = "",
    val endereco: String = "",
    val dataInicioTrabalho: LocalDate? = null,
    val dataTerminoTrabalho: LocalDate? = null,

    // NSR E LOCALIZAÇÃO
    val habilitarNsr: Boolean = false,
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,
    val habilitarLocalizacao: Boolean = false,
    val localizacaoAutomatica: Boolean = false,

    // FOTO COMPROVANTE
    val habilitarFotoComprovante: Boolean = false,
    val fotoObrigatoria: Boolean = false,

    // CARGO INICIAL (Apenas para novos empregos)
    val funcaoInicial: String = "",
    val salarioInicial: Double? = null,

    // ESTADOS DE UI
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val erro: String? = null,
    val secaoExpandida: SecaoFormulario? = SecaoFormulario.DADOS_BASICOS,

    // Pickers
    val showInicioTrabalhoPicker: Boolean = false,
    val showTerminoTrabalhoPicker: Boolean = false
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Propriedades computadas básicas
    val tituloTela: String = if (isNovoEmprego) "Novo Emprego" else "Editar Emprego"
    val textoBotaoSalvar: String = if (isNovoEmprego) "Criar Emprego" else "Salvar Alterações"

    val formularioValido: Boolean = nome.isNotBlank() &&
            nomeErro == null &&
            (!isNovoEmprego || (funcaoInicial.isNotBlank() && (salarioInicial ?: 0.0) > 0.0))

    // Formatações de data
    val dataInicioTrabalhoFormatada: String
        get() = dataInicioTrabalho?.format(dateFormatter) ?: ""

    val dataTerminoTrabalhoFormatada: String
        get() = dataTerminoTrabalho?.format(dateFormatter) ?: ""
}

/**
 * Seções do formulário de edição.
 */
enum class SecaoFormulario {
    DADOS_BASICOS,
    HISTORICO_CARGOS,
    CONFIGURACOES_GERAIS,
    JORNADAS_VERSIONADAS
}
