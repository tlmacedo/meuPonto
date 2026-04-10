// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.AtualizarEmpregoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.AtualizarEmpregoUseCase.Parametros
import br.com.tlmacedo.meuponto.domain.usecase.emprego.CriarEmpregoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoComConfiguracaoUseCase
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
import javax.inject.Inject

/**
 * ViewModel da tela de edição/criação de emprego.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 11.0.0 - Refatorado para usar Criar/AtualizarEmpregoUseCase com suporte total a regras de negócio.
 */
@HiltViewModel
class EditarEmpregoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val obterEmpregoComConfiguracaoUseCase: ObterEmpregoComConfiguracaoUseCase,
    private val criarEmpregoUseCase: CriarEmpregoUseCase,
    private val atualizarEmpregoUseCase: AtualizarEmpregoUseCase,
    private val versaoJornadaRepository: VersaoJornadaRepository
) : ViewModel() {

    private val empregoId: Long = savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val _uiState = MutableStateFlow(EditarEmpregoUiState())
    val uiState: StateFlow<EditarEmpregoUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<EditarEmpregoEvent>()
    val eventos: SharedFlow<EditarEmpregoEvent> = _eventos.asSharedFlow()

    init {
        if (empregoId > 0) {
            carregarEmprego(empregoId)
        } else {
            _uiState.update { it.copy(isNovoEmprego = true, isLoading = false) }
        }
    }

    fun onAction(action: EditarEmpregoAction) {
        when (action) {
            is EditarEmpregoAction.AlterarNome -> alterarNome(action.nome)
            is EditarEmpregoAction.AlterarApelido -> _uiState.update { it.copy(apelido = action.apelido) }
            is EditarEmpregoAction.AlterarEndereco -> _uiState.update { it.copy(endereco = action.endereco) }
            is EditarEmpregoAction.AlterarDataInicioTrabalho -> alterarDataInicioTrabalho(action.data)
            is EditarEmpregoAction.AlterarDataTerminoTrabalho -> _uiState.update { it.copy(dataTerminoTrabalho = action.data, showTerminoTrabalhoPicker = false) }
            is EditarEmpregoAction.AlterarLogo -> _uiState.update { it.copy(logo = action.uri) }

            is EditarEmpregoAction.AlterarHabilitarNsr -> alterarHabilitarNsr(action.habilitado)
            is EditarEmpregoAction.AlterarTipoNsr -> alterarTipoNsr(action.tipo)
            is EditarEmpregoAction.AlterarHabilitarLocalizacao -> alterarHabilitarLocalizacao(action.habilitado)
            is EditarEmpregoAction.AlterarLocalizacaoAutomatica -> alterarLocalizacaoAutomatica(action.automatica)
            is EditarEmpregoAction.AlterarHabilitarFotoComprovante -> alterarHabilitarFotoComprovante(action.habilitado)
            is EditarEmpregoAction.AlterarFotoObrigatoria -> alterarFotoObrigatoria(action.obrigatoria)

            is EditarEmpregoAction.AlterarFuncaoInicial -> _uiState.update { it.copy(funcaoInicial = action.funcao) }
            is EditarEmpregoAction.AlterarSalarioInicial -> _uiState.update { it.copy(salarioInicial = action.valor) }

            is EditarEmpregoAction.ToggleSecao -> toggleSecao(action.secao)
            is EditarEmpregoAction.Salvar -> salvar()
            is EditarEmpregoAction.SalvarDadosBasicos -> salvarDadosBasicos()
            is EditarEmpregoAction.SalvarConfiguracoesGerais -> salvarConfiguracoesGerais()
            is EditarEmpregoAction.Cancelar -> cancelar()
            is EditarEmpregoAction.LimparErro -> limparErro()

            // Ignorar ações migradas para Versão na tela de Emprego
            is EditarEmpregoAction.AlterarCargaHorariaDiaria,
            is EditarEmpregoAction.AlterarAcrescimoDiasPontes,
            is EditarEmpregoAction.AlterarJornadaMaximaDiaria,
            is EditarEmpregoAction.AlterarTurnoMaximo,
            is EditarEmpregoAction.AlterarIntervaloMinimo,
            is EditarEmpregoAction.AlterarIntervaloInterjornada,
            is EditarEmpregoAction.AlterarToleranciaIntervaloMais,
            is EditarEmpregoAction.AlterarExigeJustificativa,
            is EditarEmpregoAction.AlterarPrimeiroDiaSemana,
            is EditarEmpregoAction.AlterarDiaInicioFechamentoRH,
            is EditarEmpregoAction.AlterarZerarSaldoPeriodoRH,
            is EditarEmpregoAction.AlterarBancoHorasHabilitado,
            is EditarEmpregoAction.AlterarPeriodoBancoHoras,
            is EditarEmpregoAction.AlterarDataInicioCicloBanco,
            is EditarEmpregoAction.AlterarZerarBancoAntesPeriodo -> {
                Timber.w("Ação ${action::class.simpleName} migrada para EditarVersaoViewModel e não deve ser usada aqui.")
            }
        }
    }

    fun setShowInicioTrabalhoPicker(show: Boolean) = _uiState.update { it.copy(showInicioTrabalhoPicker = show) }
    fun setShowTerminoTrabalhoPicker(show: Boolean) = _uiState.update { it.copy(showTerminoTrabalhoPicker = show) }

    private fun carregarEmprego(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val resultado = obterEmpregoComConfiguracaoUseCase(id)) {
                is ObterEmpregoComConfiguracaoUseCase.Resultado.Sucesso -> {
                    val emprego = resultado.emprego
                    val config = resultado.configuracao

                    _uiState.update {
                        it.copy(
                            empregoId = emprego.id,
                            isNovoEmprego = false,
                            nome = emprego.nome,
                            apelido = emprego.apelido ?: "",
                            endereco = emprego.endereco ?: "",
                            dataInicioTrabalho = emprego.dataInicioTrabalho,
                            dataTerminoTrabalho = emprego.dataTerminoTrabalho,
                            logo = emprego.logo,

                            originalNome = emprego.nome,
                            originalApelido = emprego.apelido ?: "",
                            originalEndereco = emprego.endereco ?: "",
                            originalDataInicioTrabalho = emprego.dataInicioTrabalho,
                            originalDataTerminoTrabalho = emprego.dataTerminoTrabalho,
                            originalLogo = emprego.logo,
                            
                            habilitarNsr = config.habilitarNsr,
                            tipoNsr = config.tipoNsr,
                            habilitarLocalizacao = config.habilitarLocalizacao,
                            localizacaoAutomatica = config.localizacaoAutomatica,
                            habilitarFotoComprovante = config.fotoHabilitada,
                            fotoObrigatoria = config.fotoObrigatoria,

                            originalHabilitarNsr = config.habilitarNsr,
                            originalTipoNsr = config.tipoNsr,
                            originalHabilitarLocalizacao = config.habilitarLocalizacao,
                            originalLocalizacaoAutomatica = config.localizacaoAutomatica,
                            originalHabilitarFotoComprovante = config.fotoHabilitada,
                            originalFotoObrigatoria = config.fotoObrigatoria,

                            isLoading = false
                        )
                    }
                }
                is ObterEmpregoComConfiguracaoUseCase.Resultado.NaoEncontrado -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _eventos.emit(EditarEmpregoEvent.MostrarErro("Emprego não encontrado"))
                    _eventos.emit(EditarEmpregoEvent.Voltar)
                }
                is ObterEmpregoComConfiguracaoUseCase.Resultado.Erro -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _eventos.emit(EditarEmpregoEvent.MostrarErro(resultado.mensagem))
                }
            }
        }
    }

    private fun alterarNome(nome: String) {
        val erro = when {
            nome.isBlank() -> "Nome é obrigatório"
            nome.length < 2 -> "Nome muito curto"
            else -> null
        }
        _uiState.update { it.copy(nome = nome, nomeErro = erro) }
    }

    private fun alterarDataInicioTrabalho(data: LocalDate?) = _uiState.update { it.copy(dataInicioTrabalho = data, showInicioTrabalhoPicker = false) }
    private fun alterarHabilitarNsr(habilitado: Boolean) = _uiState.update { it.copy(habilitarNsr = habilitado) }
    private fun alterarTipoNsr(tipo: TipoNsr) = _uiState.update { it.copy(tipoNsr = tipo) }
    private fun alterarHabilitarLocalizacao(habilitado: Boolean) = _uiState.update { it.copy(habilitarLocalizacao = habilitado) }
    private fun alterarLocalizacaoAutomatica(automatica: Boolean) = _uiState.update { it.copy(localizacaoAutomatica = automatica) }
    private fun alterarHabilitarFotoComprovante(habilitado: Boolean) = _uiState.update { it.copy(habilitarFotoComprovante = habilitado) }
    private fun alterarFotoObrigatoria(obrigatoria: Boolean) = _uiState.update { it.copy(fotoObrigatoria = obrigatoria) }
    private fun toggleSecao(secao: SecaoFormulario) = _uiState.update { state -> state.copy(secaoExpandida = if (state.secaoExpandida == secao) null else secao) }

    private fun salvarDadosBasicos() {
        val state = _uiState.value
        if (state.nome.isBlank()) {
            viewModelScope.launch { _eventos.emit(EditarEmpregoEvent.MostrarErro("Nome é obrigatório")) }
            return
        }
        executarAtualizacaoGranular(
            Parametros(
                empregoId = state.empregoId!!,
                nome = state.nome,
                apelido = state.apelido,
                endereco = state.endereco,
                dataInicioTrabalho = state.dataInicioTrabalho ?: LocalDate.now(),
                dataTerminoTrabalho = state.dataTerminoTrabalho,
                logo = state.logo,
                habilitarNsr = state.originalHabilitarNsr,
                tipoNsr = state.originalTipoNsr,
                habilitarLocalizacao = state.originalHabilitarLocalizacao,
                localizacaoAutomatica = state.originalLocalizacaoAutomatica,
                fotoHabilitada = state.originalHabilitarFotoComprovante,
                fotoObrigatoria = state.originalFotoObrigatoria
            ),
            sucessoMsg = "Dados básicos atualizados"
        )
    }

    private fun salvarConfiguracoesGerais() {
        val state = _uiState.value
        executarAtualizacaoGranular(
            Parametros(
                empregoId = state.empregoId!!,
                nome = state.originalNome,
                apelido = state.originalApelido,
                endereco = state.originalEndereco,
                dataInicioTrabalho = state.originalDataInicioTrabalho ?: LocalDate.now(),
                dataTerminoTrabalho = state.originalDataTerminoTrabalho,
                logo = state.originalLogo,
                habilitarNsr = state.habilitarNsr,
                tipoNsr = state.tipoNsr,
                habilitarLocalizacao = state.habilitarLocalizacao,
                localizacaoAutomatica = state.localizacaoAutomatica,
                fotoHabilitada = state.habilitarFotoComprovante,
                fotoObrigatoria = state.fotoObrigatoria
            ),
            sucessoMsg = "Configurações gerais atualizadas"
        )
    }

    private fun executarAtualizacaoGranular(params: Parametros, sucessoMsg: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                when (val resultado = atualizarEmpregoUseCase(params)) {
                    is AtualizarEmpregoUseCase.Resultado.Sucesso -> {
                        _eventos.emit(EditarEmpregoEvent.MostrarErro(sucessoMsg)) // Usando erro temporário para snackbar sem fechar
                        carregarEmprego(params.empregoId)
                    }
                    is AtualizarEmpregoUseCase.Resultado.NaoEncontrado -> _eventos.emit(EditarEmpregoEvent.MostrarErro("Emprego não encontrado"))
                    is AtualizarEmpregoUseCase.Resultado.Erro -> _eventos.emit(EditarEmpregoEvent.MostrarErro(resultado.mensagem))
                }
            } catch (e: Exception) {
                _eventos.emit(EditarEmpregoEvent.MostrarErro("Erro ao salvar: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun salvar() {
        val state = _uiState.value

        if (state.nome.isBlank()) {
            viewModelScope.launch { _eventos.emit(EditarEmpregoEvent.MostrarErro("Nome é obrigatório")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                if (state.isNovoEmprego) {
                    val params = CriarEmpregoUseCase.Parametros(
                        nome = state.nome,
                        apelido = state.apelido,
                        endereco = state.endereco,
                        dataInicioTrabalho = state.dataInicioTrabalho ?: LocalDate.now(),
                        dataTerminoTrabalho = state.dataTerminoTrabalho,
                        logo = state.logo,
                        funcao = state.funcaoInicial,
                        salarioInicial = state.salarioInicial ?: 0.0,
                        habilitarNsr = state.habilitarNsr,
                        tipoNsr = state.tipoNsr,
                        habilitarLocalizacao = state.habilitarLocalizacao,
                        localizacaoAutomatica = state.localizacaoAutomatica,
                        fotoHabilitada = state.habilitarFotoComprovante,
                        fotoObrigatoria = state.fotoObrigatoria
                        // Outros parâmetros de CriarEmpregoUseCase agora têm defaults
                        // refletindo a configuração inicial padrão para a Versao 1.
                    )
                    
                    when (val resultado = criarEmpregoUseCase(params)) {
                        is CriarEmpregoUseCase.Resultado.Sucesso -> {
                            _eventos.emit(EditarEmpregoEvent.SalvoComSucesso("Emprego criado com sucesso"))
                        }
                        is CriarEmpregoUseCase.Resultado.Validacao -> {
                            _eventos.emit(EditarEmpregoEvent.MostrarErro(resultado.erros.first()))
                        }
                        is CriarEmpregoUseCase.Resultado.Erro -> {
                            _eventos.emit(EditarEmpregoEvent.MostrarErro(resultado.mensagem))
                        }
                    }
                } else {
                    val params = AtualizarEmpregoUseCase.Parametros(
                        empregoId = state.empregoId!!,
                        nome = state.nome,
                        apelido = state.apelido,
                        endereco = state.endereco,
                        dataInicioTrabalho = state.dataInicioTrabalho ?: LocalDate.now(),
                        dataTerminoTrabalho = state.dataTerminoTrabalho,
                        logo = state.logo,
                        habilitarNsr = state.habilitarNsr,
                        tipoNsr = state.tipoNsr,
                        habilitarLocalizacao = state.habilitarLocalizacao,
                        localizacaoAutomatica = state.localizacaoAutomatica,
                        fotoHabilitada = state.habilitarFotoComprovante,
                        fotoObrigatoria = state.fotoObrigatoria
                    )

                    when (val resultado = atualizarEmpregoUseCase(params)) {
                        is AtualizarEmpregoUseCase.Resultado.Sucesso -> {
                            _eventos.emit(EditarEmpregoEvent.SalvoComSucesso("Alterações salvas com sucesso"))
                        }
                        is AtualizarEmpregoUseCase.Resultado.NaoEncontrado -> {
                            _eventos.emit(EditarEmpregoEvent.MostrarErro("Emprego não encontrado"))
                        }
                        is AtualizarEmpregoUseCase.Resultado.Erro -> {
                            _eventos.emit(EditarEmpregoEvent.MostrarErro(resultado.mensagem))
                        }
                    }
                }
            } catch (e: Exception) {
                _eventos.emit(EditarEmpregoEvent.MostrarErro("Erro ao salvar: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun cancelar() = viewModelScope.launch { _eventos.emit(EditarEmpregoEvent.Voltar) }
    private fun limparErro() = _uiState.update { it.copy(erro = null) }
}
