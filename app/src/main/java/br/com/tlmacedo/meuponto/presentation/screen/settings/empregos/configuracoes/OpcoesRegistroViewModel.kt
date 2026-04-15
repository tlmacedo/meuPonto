package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.configuracoes

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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OpcoesRegistroViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val configuracaoRepository: ConfiguracaoEmpregoRepository,
    private val empregoRepository: EmpregoRepository
) : ViewModel() {

    private val empregoId: Long = checkNotNull(savedStateHandle[MeuPontoDestinations.ARG_EMPREGO_ID])

    private val _uiState = MutableStateFlow(OpcoesRegistroUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<OpcoesRegistroEvent>()
    val eventos = _eventos.asSharedFlow()

    private var configuracaoOriginal: ConfiguracaoEmprego? = null

    init {
        carregarConfiguracao()
    }

    fun onAction(action: OpcoesRegistroAction) {
        when (action) {
            is OpcoesRegistroAction.AlterarHabilitarNsr -> _uiState.update { it.copy(habilitarNsr = action.habilitar) }
            is OpcoesRegistroAction.AlterarTipoNsr -> _uiState.update { it.copy(tipoNsr = action.tipo) }
            is OpcoesRegistroAction.AlterarHabilitarLocalizacao -> _uiState.update { it.copy(habilitarLocalizacao = action.habilitar) }
            is OpcoesRegistroAction.AlterarLocalizacaoAutomatica -> _uiState.update { it.copy(localizacaoAutomatica = action.automatica) }
            is OpcoesRegistroAction.AlterarExibirLocalizacaoDetalhes -> _uiState.update { it.copy(exibirLocalizacaoDetalhes = action.exibir) }
            is OpcoesRegistroAction.AlterarFotoHabilitada -> _uiState.update { it.copy(fotoHabilitada = action.habilitada) }
            is OpcoesRegistroAction.AlterarFotoObrigatoria -> _uiState.update { it.copy(fotoObrigatoria = action.obrigatoria) }
            is OpcoesRegistroAction.AlterarFotoValidarComprovante -> _uiState.update { it.copy(fotoValidarComprovante = action.validar) }
            is OpcoesRegistroAction.AlterarComentarioHabilitado -> _uiState.update {
                it.copy(
                    comentarioHabilitado = action.habilitado,
                    comentarioObrigatorioHoraExtra = if (!action.habilitado) false else it.comentarioObrigatorioHoraExtra
                )
            }
            is OpcoesRegistroAction.AlterarComentarioObrigatorioHoraExtra -> {
                if (_uiState.value.comentarioHabilitado) {
                    _uiState.update { it.copy(comentarioObrigatorioHoraExtra = action.obrigatorio) }
                }
            }
            is OpcoesRegistroAction.AlterarLimiteHoraExtraSemComentario -> _uiState.update { it.copy(limiteHoraExtraSemComentario = action.limite) }
            is OpcoesRegistroAction.AlterarExibirDuracaoTurno -> _uiState.update { it.copy(exibirDuracaoTurno = action.exibir) }
            is OpcoesRegistroAction.AlterarExibirDuracaoIntervalo -> _uiState.update { it.copy(exibirDuracaoIntervalo = action.exibir) }
            OpcoesRegistroAction.Salvar -> salvar()
            OpcoesRegistroAction.Voltar -> viewModelScope.launch { _eventos.emit(OpcoesRegistroEvent.Voltar) }
        }
    }

    private fun carregarConfiguracao() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val emprego = empregoRepository.buscarPorId(empregoId)
            val config = configuracaoRepository.buscarPorEmpregoId(empregoId)
            
            configuracaoOriginal = config
            
            _uiState.update {
                it.copy(
                    isLoading = false,
                    nomeEmprego = emprego?.nome ?: "",
                    apelidoEmprego = emprego?.apelido,
                    logoEmprego = emprego?.logo,
                    habilitarNsr = config?.habilitarNsr ?: false,
                    tipoNsr = config?.tipoNsr ?: TipoNsr.NUMERICO,
                    habilitarLocalizacao = config?.habilitarLocalizacao ?: false,
                    localizacaoAutomatica = config?.localizacaoAutomatica ?: false,
                    exibirLocalizacaoDetalhes = config?.exibirLocalizacaoDetalhes ?: false,
                    fotoHabilitada = config?.fotoHabilitada ?: false,
                    fotoObrigatoria = config?.fotoObrigatoria ?: false,
                    fotoValidarComprovante = config?.fotoValidarComprovante ?: false,
                    comentarioHabilitado = config?.comentarioHabilitado ?: false,
                    comentarioObrigatorioHoraExtra = config?.comentarioObrigatorioHoraExtra ?: false,
                    limiteHoraExtraSemComentario = config?.limiteHoraExtraSemComentario ?: 0,
                    exibirDuracaoTurno = config?.exibirDuracaoTurno ?: true,
                    exibirDuracaoIntervalo = config?.exibirDuracaoIntervalo ?: true
                )
            }
        }
    }

    private fun salvar() {
        val configAtual = uiState.value
        val configParaSalvar = (configuracaoOriginal ?: ConfiguracaoEmprego(empregoId = empregoId)).copy(
            habilitarNsr = configAtual.habilitarNsr,
            tipoNsr = configAtual.tipoNsr,
            habilitarLocalizacao = configAtual.habilitarLocalizacao,
            localizacaoAutomatica = configAtual.localizacaoAutomatica,
            exibirLocalizacaoDetalhes = configAtual.exibirLocalizacaoDetalhes,
            fotoHabilitada = configAtual.fotoHabilitada,
            fotoObrigatoria = configAtual.fotoObrigatoria,
            fotoValidarComprovante = configAtual.fotoValidarComprovante,
            comentarioHabilitado = configAtual.comentarioHabilitado,
            comentarioObrigatorioHoraExtra = configAtual.comentarioObrigatorioHoraExtra,
            limiteHoraExtraSemComentario = configAtual.limiteHoraExtraSemComentario,
            exibirDuracaoTurno = configAtual.exibirDuracaoTurno,
            exibirDuracaoIntervalo = configAtual.exibirDuracaoIntervalo
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            if (configParaSalvar.id == 0L) {
                val novoId = configuracaoRepository.inserir(configParaSalvar)
                configuracaoOriginal = configParaSalvar.copy(id = novoId)
            } else {
                configuracaoRepository.atualizar(configParaSalvar)
                configuracaoOriginal = configParaSalvar
            }
            _uiState.update { it.copy(isSaving = false) }
            _eventos.emit(OpcoesRegistroEvent.SalvoComSucesso("Configurações de registro salvas com sucesso!"))
        }
    }
}

data class OpcoesRegistroUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val nomeEmprego: String = "",
    val apelidoEmprego: String? = null,
    val logoEmprego: String? = null,
    val habilitarNsr: Boolean = false,
    val tipoNsr: TipoNsr = TipoNsr.NUMERICO,
    val habilitarLocalizacao: Boolean = false,
    val localizacaoAutomatica: Boolean = false,
    val exibirLocalizacaoDetalhes: Boolean = false,
    val fotoHabilitada: Boolean = false,
    val fotoObrigatoria: Boolean = false,
    val fotoValidarComprovante: Boolean = false,
    val comentarioHabilitado: Boolean = false,
    val comentarioObrigatorioHoraExtra: Boolean = false,
    val limiteHoraExtraSemComentario: Int = 0,
    val exibirDuracaoTurno: Boolean = true,
    val exibirDuracaoIntervalo: Boolean = true
)

sealed interface OpcoesRegistroAction {
    data class AlterarHabilitarNsr(val habilitar: Boolean) : OpcoesRegistroAction
    data class AlterarTipoNsr(val tipo: TipoNsr) : OpcoesRegistroAction
    data class AlterarHabilitarLocalizacao(val habilitar: Boolean) : OpcoesRegistroAction
    data class AlterarLocalizacaoAutomatica(val automatica: Boolean) : OpcoesRegistroAction
    data class AlterarExibirLocalizacaoDetalhes(val exibir: Boolean) : OpcoesRegistroAction
    data class AlterarFotoHabilitada(val habilitada: Boolean) : OpcoesRegistroAction
    data class AlterarFotoObrigatoria(val obrigatoria: Boolean) : OpcoesRegistroAction
    data class AlterarFotoValidarComprovante(val validar: Boolean) : OpcoesRegistroAction
    data class AlterarComentarioHabilitado(val habilitado: Boolean) : OpcoesRegistroAction
    data class AlterarComentarioObrigatorioHoraExtra(val obrigatorio: Boolean) : OpcoesRegistroAction
    data class AlterarLimiteHoraExtraSemComentario(val limite: Int) : OpcoesRegistroAction
    data class AlterarExibirDuracaoTurno(val exibir: Boolean) : OpcoesRegistroAction
    data class AlterarExibirDuracaoIntervalo(val exibir: Boolean) : OpcoesRegistroAction
    data object Salvar : OpcoesRegistroAction
    data object Voltar : OpcoesRegistroAction
}

sealed interface OpcoesRegistroEvent {
    data class SalvoComSucesso(val mensagem: String) : OpcoesRegistroEvent
    data object Voltar : OpcoesRegistroEvent
}
