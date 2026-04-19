// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoUiState.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Estado da tela de edição/criação de emprego.
 */
data class EditarEmpregoUiState(
    val empregoId: Long? = null,
    val isNovoEmprego: Boolean = true,
    val nome: String = "",
    val nomeErro: String? = null,
    val apelido: String = "",
    val endereco: String = "",
    val descricao: String = "",
    val dataInicioTrabalho: LocalDate? = null,
    val dataTerminoTrabalho: LocalDate? = null,
    val logo: String? = null,

    // DADOS ORIGINAIS (para comparação e salvamento granular)
    val originalNome: String = "",
    val originalApelido: String = "",
    val originalEndereco: String = "",
    val originalDescricao: String = "",
    val originalDataInicioTrabalho: LocalDate? = null,
    val originalDataTerminoTrabalho: LocalDate? = null,
    val originalLogo: String? = null,

    // CONFIGURAÇÕES (RH E BANCO DE HORAS)
    val diaInicioFechamentoRH: Int = 1,
    val bancoHorasHabilitado: Boolean = false,
    val bancoHorasCicloMeses: Int = 6,
    val bancoHorasDataInicioCiclo: LocalDate? = null,
    val bancoHorasZerarAoFinalCiclo: Boolean = false,
    val exigeJustificativaInconsistencia: Boolean = false,

    val originalDiaInicioFechamentoRH: Int = 1,
    val originalBancoHorasHabilitado: Boolean = false,
    val originalBancoHorasCicloMeses: Int = 6,
    val originalBancoHorasDataInicioCiclo: LocalDate? = null,
    val originalBancoHorasZerarAoFinalCiclo: Boolean = false,
    val originalExigeJustificativaInconsistencia: Boolean = false,

    // CONFIGURAÇÕES (OPÇÕES DE REGISTRO / FIXAS)
    val habilitarNsr: Boolean = false,
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,
    val habilitarLocalizacao: Boolean = false,
    val localizacaoAutomatica: Boolean = false,
    val exibirLocalizacaoDetalhes: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val raioGeofencing: Int = 200,
    val fotoHabilitada: Boolean = false,
    val fotoObrigatoria: Boolean = false,
    val fotoValidarComprovante: Boolean = false,
    val comentarioHabilitado: Boolean = true,
    val comentarioObrigatorioHoraExtra: Boolean = false,
    val exibirDuracaoTurno: Boolean = true,
    val exibirDuracaoIntervalo: Boolean = true,

    val originalHabilitarNsr: Boolean = false,
    val originalTipoNsr: TipoNsr = TipoNsr.NUMERICO,
    val originalHabilitarLocalizacao: Boolean = false,
    val originalLocalizacaoAutomatica: Boolean = false,
    val originalExibirLocalizacaoDetalhes: Boolean = true,
    val originalLatitude: Double? = null,
    val originalLongitude: Double? = null,
    val originalRaioGeofencing: Int = 200,
    val originalFotoHabilitada: Boolean = false,
    val originalFotoObrigatoria: Boolean = false,
    val originalFotoValidarComprovante: Boolean = false,
    val originalComentarioHabilitado: Boolean = true,
    val originalComentarioObrigatorioHoraExtra: Boolean = false,
    val originalExibirDuracaoTurno: Boolean = true,
    val originalExibirDuracaoIntervalo: Boolean = true,

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
    val showTerminoTrabalhoPicker: Boolean = false,
    val showInicioCicloBHPicker: Boolean = false,
    val showLocationPicker: Boolean = false
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val tituloTela: String = if (isNovoEmprego) "Novo Emprego" else "Editar Emprego"
    val textoBotaoSalvar: String = if (isNovoEmprego) "Criar Emprego" else "Salvar Alterações"

    val temMudancasDadosBasicos: Boolean = !isNovoEmprego && (
            nome != originalNome ||
                    apelido != originalApelido ||
                    endereco != originalEndereco ||
                    descricao != originalDescricao ||
                    dataInicioTrabalho != originalDataInicioTrabalho ||
                    dataTerminoTrabalho != originalDataTerminoTrabalho ||
                    logo != originalLogo
            )

    val temMudancasRHBank: Boolean = !isNovoEmprego && (
            diaInicioFechamentoRH != originalDiaInicioFechamentoRH ||
                    bancoHorasHabilitado != originalBancoHorasHabilitado ||
                    bancoHorasCicloMeses != originalBancoHorasCicloMeses ||
                    bancoHorasDataInicioCiclo != originalBancoHorasDataInicioCiclo ||
                    bancoHorasZerarAoFinalCiclo != originalBancoHorasZerarAoFinalCiclo ||
                    exigeJustificativaInconsistencia != originalExigeJustificativaInconsistencia
            )

    val temMudancasOpcoesRegistro: Boolean = !isNovoEmprego && (
            habilitarNsr != originalHabilitarNsr ||
                    tipoNsr != originalTipoNsr ||
                    habilitarLocalizacao != originalHabilitarLocalizacao ||
                    localizacaoAutomatica != originalLocalizacaoAutomatica ||
                    exibirLocalizacaoDetalhes != originalExibirLocalizacaoDetalhes ||
                    latitude != originalLatitude ||
                    longitude != originalLongitude ||
                    raioGeofencing != originalRaioGeofencing ||
                    fotoHabilitada != originalFotoHabilitada ||
                    fotoObrigatoria != originalFotoObrigatoria ||
                    fotoValidarComprovante != originalFotoValidarComprovante ||
                    comentarioHabilitado != originalComentarioHabilitado ||
                    comentarioObrigatorioHoraExtra != originalComentarioObrigatorioHoraExtra ||
                    exibirDuracaoTurno != originalExibirDuracaoTurno ||
                    exibirDuracaoIntervalo != originalExibirDuracaoIntervalo
            )

    val temMudancas: Boolean = temMudancasDadosBasicos || temMudancasRHBank || temMudancasOpcoesRegistro

    val formularioValido: Boolean = nome.isNotBlank() &&
            nomeErro == null &&
            (!isNovoEmprego || (funcaoInicial.isNotBlank() && (salarioInicial ?: 0.0) > 0.0))

    val dataInicioTrabalhoFormatada: String
        get() = dataInicioTrabalho?.format(dateFormatter) ?: ""

    val dataTerminoTrabalhoFormatada: String
        get() = dataTerminoTrabalho?.format(dateFormatter) ?: ""

    val dataInicioCicloBHFormatada: String
        get() = bancoHorasDataInicioCiclo?.format(dateFormatter) ?: ""
}

/**
 * Seções do formulário de edição.
 */
enum class SecaoFormulario {
    DADOS_BASICOS,
    RH_E_BANCO_DE_HORAS,
    OPCOES_DE_REGISTRO,
    JORNADAS_VERSIONADAS,
    HISTORICO_CARGOS
}
