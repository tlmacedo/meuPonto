package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.configuracao

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

/**
 * ViewModel para a tela de configuração geral do emprego.
 *
 * Gerencia as configurações de:
 * - Info RH (dia de fechamento, banco de horas)
 * - Info Extra (NSR, localização, foto, justificativa)
 *
 * @author Thiago
 * @since 29.0.0
 */
@HiltViewModel
class ConfiguracaoGeralEmpregoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val empregoRepository: EmpregoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository
) : ViewModel() {

    private val empregoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val _uiState = MutableStateFlow(ConfiguracaoGeralUiState())
    val uiState: StateFlow<ConfiguracaoGeralUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<ConfiguracaoGeralEvent>()
    val eventos: SharedFlow<ConfiguracaoGeralEvent> = _eventos.asSharedFlow()

    private var configuracaoOriginal: ConfiguracaoEmprego? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    init {
        carregarDados()
    }

    fun onAction(action: ConfiguracaoGeralAction) {
        when (action) {
            is ConfiguracaoGeralAction.AlterarDiaFechamentoRH ->
                _uiState.update { it.copy(diaFechamentoRhStr = action.valor) }
            is ConfiguracaoGeralAction.ToggleBancoHoras ->
                _uiState.update { it.copy(bancoHorasHabilitado = action.habilitado) }
            is ConfiguracaoGeralAction.AlterarCicloMeses ->
                _uiState.update { it.copy(bancoHorasCicloMesesStr = action.valor) }
            is ConfiguracaoGeralAction.AlterarDataInicioCiclo ->
                _uiState.update { it.copy(bancoHorasDataInicioCicloStr = action.valor) }
            is ConfiguracaoGeralAction.ToggleZerarSaldoCiclo ->
                _uiState.update { it.copy(bancoHorasZerarAoFinalCiclo = action.zerar) }
            is ConfiguracaoGeralAction.ToggleNsr ->
                _uiState.update { it.copy(habilitarNsr = action.habilitado) }
            is ConfiguracaoGeralAction.AlterarTipoNsr ->
                _uiState.update { it.copy(tipoNsr = action.tipo) }
            is ConfiguracaoGeralAction.ToggleLocalizacao ->
                _uiState.update { it.copy(habilitarLocalizacao = action.habilitado) }
            is ConfiguracaoGeralAction.ToggleLocalizacaoAutomatica ->
                _uiState.update { it.copy(localizacaoAutomatica = action.automatica) }
            is ConfiguracaoGeralAction.ToggleFoto ->
                _uiState.update { it.copy(fotoHabilitada = action.habilitada) }
            is ConfiguracaoGeralAction.ToggleFotoObrigatoria ->
                _uiState.update { it.copy(fotoObrigatoria = action.obrigatoria) }
            is ConfiguracaoGeralAction.AlterarFotoQualidade ->
                alterarFotoQualidade(action.valor)
            is ConfiguracaoGeralAction.AlterarFotoResolucao ->
                _uiState.update { it.copy(fotoResolucaoStr = action.valor) }
            is ConfiguracaoGeralAction.AlterarFotoTamanhoMaximo ->
                _uiState.update { it.copy(fotoTamanhoMaximoStr = action.valor) }
            is ConfiguracaoGeralAction.ToggleFotoOcr ->
                _uiState.update { it.copy(fotoRegistrarPontoOcr = action.habilitado) }
            is ConfiguracaoGeralAction.ToggleJustificativa ->
                _uiState.update { it.copy(exigeJustificativa = action.exige) }
            is ConfiguracaoGeralAction.Salvar -> salvar()
        }
    }

    private fun carregarDados() {
        if (empregoId <= 0L) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val emprego = empregoRepository.buscarPorId(empregoId)
                val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
                    ?: ConfiguracaoEmprego.criarPadrao(empregoId)

                configuracaoOriginal = configuracao

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        nomeEmprego = emprego?.nome ?: "",
                        // Info RH
                        diaFechamentoRhStr = configuracao.diaInicioFechamentoRH.toString(),
                        bancoHorasHabilitado = configuracao.bancoHorasHabilitado,
                        bancoHorasCicloMesesStr = configuracao.bancoHorasCicloMeses.toString(),
                        bancoHorasDataInicioCicloStr = configuracao.bancoHorasDataInicioCiclo
                            ?.format(dateFormatter) ?: "",
                        bancoHorasZerarAoFinalCiclo = configuracao.bancoHorasZerarAoFinalCiclo,
                        // NSR
                        habilitarNsr = configuracao.habilitarNsr,
                        tipoNsr = configuracao.tipoNsr,
                        // Localização
                        habilitarLocalizacao = configuracao.habilitarLocalizacao,
                        localizacaoAutomatica = configuracao.localizacaoAutomatica,
                        // Foto
                        fotoHabilitada = configuracao.fotoHabilitada,
                        fotoObrigatoria = configuracao.fotoObrigatoria,
                        fotoQualidade = configuracao.fotoQualidade,
                        fotoQualidadeStr = configuracao.fotoQualidade.toString(),
                        fotoResolucaoStr = configuracao.fotoResolucaoMaxima.toString(),
                        fotoTamanhoMaximoStr = configuracao.fotoTamanhoMaximoKb.toString(),
                        fotoRegistrarPontoOcr = configuracao.fotoRegistrarPontoOcr,
                        // Justificativa
                        exigeJustificativa = configuracao.exigeJustificativaInconsistencia
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar configuração do emprego %d", empregoId)
                _uiState.update { it.copy(isLoading = false) }
                _eventos.emit(ConfiguracaoGeralEvent.MostrarErro("Erro ao carregar configurações"))
            }
        }
    }

    private fun alterarFotoQualidade(valor: String) {
        val qualidade = valor.toIntOrNull()?.coerceIn(60, 100) ?: _uiState.value.fotoQualidade
        _uiState.update {
            it.copy(
                fotoQualidadeStr = valor,
                fotoQualidade = qualidade
            )
        }
    }

    private fun salvar() {
        val state = _uiState.value

        // Validar dia de fechamento
        val diaFechamento = state.diaFechamentoRhStr.toIntOrNull()
        if (diaFechamento == null || diaFechamento !in 1..28) {
            viewModelScope.launch {
                _eventos.emit(ConfiguracaoGeralEvent.MostrarErro("Informe um dia de fechamento entre 1 e 28"))
            }
            return
        }

        // Parsear data de início do ciclo
        var dataInicioCiclo: LocalDate? = null
        if (state.bancoHorasHabilitado && state.bancoHorasDataInicioCicloStr.isNotBlank()) {
            try {
                dataInicioCiclo = LocalDate.parse(state.bancoHorasDataInicioCicloStr, dateFormatter)
            } catch (e: DateTimeParseException) {
                viewModelScope.launch {
                    _eventos.emit(ConfiguracaoGeralEvent.MostrarErro("Data de início do ciclo inválida. Use o formato dd/MM/yyyy"))
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val original = configuracaoOriginal
                val configuracao = ConfiguracaoEmprego(
                    id = original?.id ?: 0L,
                    empregoId = empregoId,
                    // Info RH
                    diaInicioFechamentoRH = diaFechamento,
                    bancoHorasHabilitado = state.bancoHorasHabilitado,
                    bancoHorasCicloMeses = state.bancoHorasCicloMesesStr.toIntOrNull() ?: 6,
                    bancoHorasDataInicioCiclo = dataInicioCiclo,
                    bancoHorasZerarAoFinalCiclo = state.bancoHorasZerarAoFinalCiclo,
                    // NSR
                    habilitarNsr = state.habilitarNsr,
                    tipoNsr = state.tipoNsr,
                    // Localização
                    habilitarLocalizacao = state.habilitarLocalizacao,
                    localizacaoAutomatica = state.localizacaoAutomatica,
                    // Foto
                    fotoHabilitada = state.fotoHabilitada,
                    fotoObrigatoria = state.fotoObrigatoria,
                    fotoQualidade = state.fotoQualidade,
                    fotoResolucaoMaxima = state.fotoResolucaoStr.toIntOrNull() ?: 1920,
                    fotoTamanhoMaximoKb = state.fotoTamanhoMaximoStr.toIntOrNull() ?: 1024,
                    fotoRegistrarPontoOcr = state.fotoRegistrarPontoOcr,
                    // Justificativa
                    exigeJustificativaInconsistencia = state.exigeJustificativa,
                    // Manter campos não editados
                    exibirDuracaoTurno = original?.exibirDuracaoTurno ?: true,
                    exibirDuracaoIntervalo = original?.exibirDuracaoIntervalo ?: true,
                    fotoFormato = original?.fotoFormato ?: br.com.tlmacedo.meuponto.domain.model.FotoFormato.JPEG,
                    fotoCorrecaoOrientacao = original?.fotoCorrecaoOrientacao ?: true,
                    fotoApenasCamera = original?.fotoApenasCamera ?: false,
                    fotoIncluirLocalizacaoExif = original?.fotoIncluirLocalizacaoExif ?: true,
                    fotoBackupNuvemHabilitado = original?.fotoBackupNuvemHabilitado ?: false,
                    fotoBackupApenasWifi = original?.fotoBackupApenasWifi ?: true,
                    fotoLocalArmazenamento = original?.fotoLocalArmazenamento,
                    exibirLocalizacaoDetalhes = original?.exibirLocalizacaoDetalhes ?: true
                )

                if (original == null || original.id == 0L) {
                    configuracaoEmpregoRepository.inserir(configuracao)
                } else {
                    configuracaoEmpregoRepository.atualizar(configuracao)
                }

                configuracaoOriginal = configuracao
                _uiState.update { it.copy(isSaving = false) }
                _eventos.emit(ConfiguracaoGeralEvent.SalvoComSucesso("Configurações salvas com sucesso"))
            } catch (e: Exception) {
                Timber.e(e, "Erro ao salvar configuração do emprego %d", empregoId)
                _uiState.update { it.copy(isSaving = false) }
                _eventos.emit(ConfiguracaoGeralEvent.MostrarErro("Erro ao salvar: ${e.message}"))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════════
// UI STATE
// ════════════════════════════════════════════════════════════════════════════════

data class ConfiguracaoGeralUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val nomeEmprego: String = "",
    // Info RH
    val diaFechamentoRhStr: String = "11",
    val bancoHorasHabilitado: Boolean = false,
    val bancoHorasCicloMesesStr: String = "6",
    val bancoHorasDataInicioCicloStr: String = "",
    val bancoHorasZerarAoFinalCiclo: Boolean = false,
    // NSR
    val habilitarNsr: Boolean = false,
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,
    // Localização
    val habilitarLocalizacao: Boolean = false,
    val localizacaoAutomatica: Boolean = false,
    // Foto
    val fotoHabilitada: Boolean = false,
    val fotoObrigatoria: Boolean = false,
    val fotoQualidade: Int = 85,
    val fotoQualidadeStr: String = "85",
    val fotoResolucaoStr: String = "1920",
    val fotoTamanhoMaximoStr: String = "1024",
    val fotoRegistrarPontoOcr: Boolean = false,
    // Justificativa
    val exigeJustificativa: Boolean = false
)

// ════════════════════════════════════════════════════════════════════════════════
// ACTIONS
// ════════════════════════════════════════════════════════════════════════════════

sealed interface ConfiguracaoGeralAction {
    data class AlterarDiaFechamentoRH(val valor: String) : ConfiguracaoGeralAction
    data class ToggleBancoHoras(val habilitado: Boolean) : ConfiguracaoGeralAction
    data class AlterarCicloMeses(val valor: String) : ConfiguracaoGeralAction
    data class AlterarDataInicioCiclo(val valor: String) : ConfiguracaoGeralAction
    data class ToggleZerarSaldoCiclo(val zerar: Boolean) : ConfiguracaoGeralAction
    data class ToggleNsr(val habilitado: Boolean) : ConfiguracaoGeralAction
    data class AlterarTipoNsr(val tipo: TipoNsr) : ConfiguracaoGeralAction
    data class ToggleLocalizacao(val habilitado: Boolean) : ConfiguracaoGeralAction
    data class ToggleLocalizacaoAutomatica(val automatica: Boolean) : ConfiguracaoGeralAction
    data class ToggleFoto(val habilitada: Boolean) : ConfiguracaoGeralAction
    data class ToggleFotoObrigatoria(val obrigatoria: Boolean) : ConfiguracaoGeralAction
    data class AlterarFotoQualidade(val valor: String) : ConfiguracaoGeralAction
    data class AlterarFotoResolucao(val valor: String) : ConfiguracaoGeralAction
    data class AlterarFotoTamanhoMaximo(val valor: String) : ConfiguracaoGeralAction
    data class ToggleFotoOcr(val habilitado: Boolean) : ConfiguracaoGeralAction
    data class ToggleJustificativa(val exige: Boolean) : ConfiguracaoGeralAction
    data object Salvar : ConfiguracaoGeralAction
}

// ════════════════════════════════════════════════════════════════════════════════
// EVENTS
// ════════════════════════════════════════════════════════════════════════════════

sealed interface ConfiguracaoGeralEvent {
    data class SalvoComSucesso(val mensagem: String) : ConfiguracaoGeralEvent
    data class MostrarErro(val mensagem: String) : ConfiguracaoGeralEvent
    data object Voltar : ConfiguracaoGeralEvent
}
