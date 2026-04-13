// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel da tela de edição/criação de emprego.
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
            // Dados básicos
            is EditarEmpregoAction.AlterarNome -> alterarNome(action.nome)
            is EditarEmpregoAction.AlterarApelido -> _uiState.update { it.copy(apelido = action.apelido) }
            is EditarEmpregoAction.AlterarEndereco -> _uiState.update { it.copy(endereco = action.endereco) }
            is EditarEmpregoAction.AlterarDescricao -> _uiState.update { it.copy(descricao = action.descricao) }
            is EditarEmpregoAction.AlterarDataInicioTrabalho -> alterarDataInicioTrabalho(action.data)
            is EditarEmpregoAction.AlterarDataTerminoTrabalho -> _uiState.update { it.copy(dataTerminoTrabalho = action.data, showTerminoTrabalhoPicker = false) }
            is EditarEmpregoAction.AlterarLogo -> _uiState.update { it.copy(logo = action.uri) }

            // RH e Banco de Horas
            is EditarEmpregoAction.AlterarDiaInicioFechamentoRH -> _uiState.update { it.copy(diaInicioFechamentoRH = action.dia) }
            is EditarEmpregoAction.AlterarBancoHorasHabilitado -> _uiState.update { it.copy(bancoHorasHabilitado = action.habilitado) }
            is EditarEmpregoAction.AlterarBancoHorasCicloMeses -> _uiState.update { it.copy(bancoHorasCicloMeses = action.meses) }
            is EditarEmpregoAction.AlterarBancoHorasDataInicioCiclo -> _uiState.update { it.copy(bancoHorasDataInicioCiclo = action.data, showInicioCicloBHPicker = false) }
            is EditarEmpregoAction.AlterarBancoHorasZerarAoFinalCiclo -> _uiState.update { it.copy(bancoHorasZerarAoFinalCiclo = action.zerar) }
            is EditarEmpregoAction.AlterarExigeJustificativaInconsistencia -> _uiState.update { it.copy(exigeJustificativaInconsistencia = action.exige) }

            // Opções de Registro
            is EditarEmpregoAction.AlterarHabilitarNsr -> _uiState.update { it.copy(habilitarNsr = action.habilitar) }
            is EditarEmpregoAction.AlterarTipoNsr -> _uiState.update { it.copy(tipoNsr = action.tipo) }
            is EditarEmpregoAction.AlterarHabilitarLocalizacao -> _uiState.update { it.copy(habilitarLocalizacao = action.habilitar) }
            is EditarEmpregoAction.AlterarLocalizacaoAutomatica -> _uiState.update { it.copy(localizacaoAutomatica = action.habilitar) }
            is EditarEmpregoAction.AlterarExibirLocalizacaoDetalhes -> _uiState.update { it.copy(exibirLocalizacaoDetalhes = action.exibir) }
            is EditarEmpregoAction.AlterarFotoHabilitada -> _uiState.update { it.copy(fotoHabilitada = action.habilitar) }
            is EditarEmpregoAction.AlterarFotoObrigatoria -> _uiState.update { it.copy(fotoObrigatoria = action.obrigatoria) }
            is EditarEmpregoAction.AlterarFotoValidarComprovante -> _uiState.update { it.copy(fotoValidarComprovante = action.validar) }
            is EditarEmpregoAction.AlterarComentarioHabilitado -> _uiState.update { it.copy(comentarioHabilitado = action.habilitar) }
            is EditarEmpregoAction.AlterarComentarioObrigatorioHoraExtra -> _uiState.update { it.copy(comentarioObrigatorioHoraExtra = action.obrigatorio) }
            is EditarEmpregoAction.AlterarExibirDuracaoTurno -> _uiState.update { it.copy(exibirDuracaoTurno = action.exibir) }
            is EditarEmpregoAction.AlterarExibirDuracaoIntervalo -> _uiState.update { it.copy(exibirDuracaoIntervalo = action.exibir) }

            // Cargo Inicial
            is EditarEmpregoAction.AlterarFuncaoInicial -> _uiState.update { it.copy(funcaoInicial = action.funcao) }
            is EditarEmpregoAction.AlterarSalarioInicial -> _uiState.update { it.copy(salarioInicial = action.valor) }

            // UI
            is EditarEmpregoAction.ToggleSecao -> toggleSecao(action.secao)
            is EditarEmpregoAction.Salvar -> salvar()
            is EditarEmpregoAction.SalvarDadosBasicos -> salvarDadosBasicos()
            is EditarEmpregoAction.SalvarRHBank -> salvarRHBank()
            is EditarEmpregoAction.SalvarOpcoesRegistro -> salvarOpcoesRegistro()
            is EditarEmpregoAction.Cancelar -> cancelar()
            is EditarEmpregoAction.LimparErro -> limparErro()
        }
    }

    fun setShowInicioTrabalhoPicker(show: Boolean) = _uiState.update { it.copy(showInicioTrabalhoPicker = show) }
    fun setShowTerminoTrabalhoPicker(show: Boolean) = _uiState.update { it.copy(showTerminoTrabalhoPicker = show) }
    fun setShowInicioCicloBHPicker(show: Boolean) = _uiState.update { it.copy(showInicioCicloBHPicker = show) }

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
                            descricao = emprego.descricao ?: "",
                            dataInicioTrabalho = emprego.dataInicioTrabalho,
                            dataTerminoTrabalho = emprego.dataTerminoTrabalho,
                            logo = emprego.logo,

                            originalNome = emprego.nome,
                            originalApelido = emprego.apelido ?: "",
                            originalEndereco = emprego.endereco ?: "",
                            originalDescricao = emprego.descricao ?: "",
                            originalDataInicioTrabalho = emprego.dataInicioTrabalho,
                            originalDataTerminoTrabalho = emprego.dataTerminoTrabalho,
                            originalLogo = emprego.logo,

                            diaInicioFechamentoRH = config.diaInicioFechamentoRH,
                            bancoHorasHabilitado = config.bancoHorasHabilitado,
                            bancoHorasCicloMeses = config.bancoHorasCicloMeses,
                            bancoHorasDataInicioCiclo = config.bancoHorasDataInicioCiclo,
                            bancoHorasZerarAoFinalCiclo = config.bancoHorasZerarAoFinalCiclo,
                            exigeJustificativaInconsistencia = config.exigeJustificativaInconsistencia,

                            originalDiaInicioFechamentoRH = config.diaInicioFechamentoRH,
                            originalBancoHorasHabilitado = config.bancoHorasHabilitado,
                            originalBancoHorasCicloMeses = config.bancoHorasCicloMeses,
                            originalBancoHorasDataInicioCiclo = config.bancoHorasDataInicioCiclo,
                            originalBancoHorasZerarAoFinalCiclo = config.bancoHorasZerarAoFinalCiclo,
                            originalExigeJustificativaInconsistencia = config.exigeJustificativaInconsistencia,

                            habilitarNsr = config.habilitarNsr,
                            tipoNsr = config.tipoNsr,
                            habilitarLocalizacao = config.habilitarLocalizacao,
                            localizacaoAutomatica = config.localizacaoAutomatica,
                            exibirLocalizacaoDetalhes = config.exibirLocalizacaoDetalhes,
                            fotoHabilitada = config.fotoHabilitada,
                            fotoObrigatoria = config.fotoObrigatoria,
                            fotoValidarComprovante = config.fotoValidarComprovante,
                            comentarioHabilitado = config.comentarioHabilitado,
                            comentarioObrigatorioHoraExtra = config.comentarioObrigatorioHoraExtra,
                            exibirDuracaoTurno = config.exibirDuracaoTurno,
                            exibirDuracaoIntervalo = config.exibirDuracaoIntervalo,

                            originalHabilitarNsr = config.habilitarNsr,
                            originalTipoNsr = config.tipoNsr,
                            originalHabilitarLocalizacao = config.habilitarLocalizacao,
                            originalLocalizacaoAutomatica = config.localizacaoAutomatica,
                            originalExibirLocalizacaoDetalhes = config.exibirLocalizacaoDetalhes,
                            originalFotoHabilitada = config.fotoHabilitada,
                            originalFotoObrigatoria = config.fotoObrigatoria,
                            originalFotoValidarComprovante = config.fotoValidarComprovante,
                            originalComentarioHabilitado = config.comentarioHabilitado,
                            originalComentarioObrigatorioHoraExtra = config.comentarioObrigatorioHoraExtra,
                            originalExibirDuracaoTurno = config.exibirDuracaoTurno,
                            originalExibirDuracaoIntervalo = config.exibirDuracaoIntervalo,

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
                descricao = state.descricao,
                dataInicioTrabalho = state.dataInicioTrabalho ?: LocalDate.now(),
                dataTerminoTrabalho = state.dataTerminoTrabalho,
                logo = state.logo,

                habilitarNsr = state.originalHabilitarNsr,
                tipoNsr = state.originalTipoNsr,
                habilitarLocalizacao = state.originalHabilitarLocalizacao,
                localizacaoAutomatica = state.originalLocalizacaoAutomatica,
                exibirLocalizacaoDetalhes = state.originalExibirLocalizacaoDetalhes,
                fotoHabilitada = state.originalFotoHabilitada,
                fotoObrigatoria = state.originalFotoObrigatoria,
                fotoValidarComprovante = state.originalFotoValidarComprovante,

                diaInicioFechamentoRH = state.originalDiaInicioFechamentoRH,
                bancoHorasHabilitado = state.originalBancoHorasHabilitado,
                bancoHorasCicloMeses = state.originalBancoHorasCicloMeses,
                bancoHorasDataInicioCiclo = state.originalBancoHorasDataInicioCiclo,
                bancoHorasZerarAoFinalCiclo = state.originalBancoHorasZerarAoFinalCiclo,
                exigeJustificativaInconsistencia = state.originalExigeJustificativaInconsistencia,

                comentarioHabilitado = state.originalComentarioHabilitado,
                comentarioObrigatorioHoraExtra = state.originalComentarioObrigatorioHoraExtra,
                exibirDuracaoTurno = state.originalExibirDuracaoTurno,
                exibirDuracaoIntervalo = state.originalExibirDuracaoIntervalo
            ),
            sucessoMsg = "Dados básicos atualizados"
        )
    }

    private fun salvarRHBank() {
        val state = _uiState.value
        executarAtualizacaoGranular(
            Parametros(
                empregoId = state.empregoId!!,
                nome = state.originalNome,
                apelido = state.originalApelido,
                endereco = state.originalEndereco,
                descricao = state.originalDescricao,
                dataInicioTrabalho = state.originalDataInicioTrabalho ?: LocalDate.now(),
                dataTerminoTrabalho = state.originalDataTerminoTrabalho,
                logo = state.originalLogo,

                habilitarNsr = state.originalHabilitarNsr,
                tipoNsr = state.originalTipoNsr,
                habilitarLocalizacao = state.originalHabilitarLocalizacao,
                localizacaoAutomatica = state.originalLocalizacaoAutomatica,
                exibirLocalizacaoDetalhes = state.originalExibirLocalizacaoDetalhes,
                fotoHabilitada = state.originalFotoHabilitada,
                fotoObrigatoria = state.originalFotoObrigatoria,
                fotoValidarComprovante = state.originalFotoValidarComprovante,

                diaInicioFechamentoRH = state.diaInicioFechamentoRH,
                bancoHorasHabilitado = state.bancoHorasHabilitado,
                bancoHorasCicloMeses = state.bancoHorasCicloMeses,
                bancoHorasDataInicioCiclo = state.bancoHorasDataInicioCiclo,
                bancoHorasZerarAoFinalCiclo = state.bancoHorasZerarAoFinalCiclo,
                exigeJustificativaInconsistencia = state.exigeJustificativaInconsistencia,

                comentarioHabilitado = state.originalComentarioHabilitado,
                comentarioObrigatorioHoraExtra = state.originalComentarioObrigatorioHoraExtra,
                exibirDuracaoTurno = state.originalExibirDuracaoTurno,
                exibirDuracaoIntervalo = state.originalExibirDuracaoIntervalo
            ),
            sucessoMsg = "Configurações de RH e Banco atualizadas"
        )
    }

    private fun salvarOpcoesRegistro() {
        val state = _uiState.value
        executarAtualizacaoGranular(
            Parametros(
                empregoId = state.empregoId!!,
                nome = state.originalNome,
                apelido = state.originalApelido,
                endereco = state.originalEndereco,
                descricao = state.originalDescricao,
                dataInicioTrabalho = state.originalDataInicioTrabalho ?: LocalDate.now(),
                dataTerminoTrabalho = state.originalDataTerminoTrabalho,
                logo = state.originalLogo,

                habilitarNsr = state.habilitarNsr,
                tipoNsr = state.tipoNsr,
                habilitarLocalizacao = state.habilitarLocalizacao,
                localizacaoAutomatica = state.localizacaoAutomatica,
                exibirLocalizacaoDetalhes = state.exibirLocalizacaoDetalhes,
                fotoHabilitada = state.fotoHabilitada,
                fotoObrigatoria = state.fotoObrigatoria,
                fotoValidarComprovante = state.fotoValidarComprovante,

                diaInicioFechamentoRH = state.originalDiaInicioFechamentoRH,
                bancoHorasHabilitado = state.originalBancoHorasHabilitado,
                bancoHorasCicloMeses = state.originalBancoHorasCicloMeses,
                bancoHorasDataInicioCiclo = state.originalBancoHorasDataInicioCiclo,
                bancoHorasZerarAoFinalCiclo = state.originalBancoHorasZerarAoFinalCiclo,
                exigeJustificativaInconsistencia = state.originalExigeJustificativaInconsistencia,

                comentarioHabilitado = state.comentarioHabilitado,
                comentarioObrigatorioHoraExtra = state.comentarioObrigatorioHoraExtra,
                exibirDuracaoTurno = state.exibirDuracaoTurno,
                exibirDuracaoIntervalo = state.exibirDuracaoIntervalo
            ),
            sucessoMsg = "Opções de registro atualizadas"
        )
    }

    private fun executarAtualizacaoGranular(params: Parametros, sucessoMsg: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                when (val resultado = atualizarEmpregoUseCase(params)) {
                    is AtualizarEmpregoUseCase.Resultado.Sucesso -> {
                        _eventos.emit(EditarEmpregoEvent.MostrarErro(sucessoMsg))
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
                        fotoHabilitada = state.fotoHabilitada,
                        fotoObrigatoria = state.fotoObrigatoria
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
                    val params = Parametros(
                        empregoId = state.empregoId!!,
                        nome = state.nome,
                        apelido = state.apelido,
                        endereco = state.endereco,
                        descricao = state.descricao,
                        dataInicioTrabalho = state.dataInicioTrabalho ?: LocalDate.now(),
                        dataTerminoTrabalho = state.dataTerminoTrabalho,
                        logo = state.logo,

                        habilitarNsr = state.habilitarNsr,
                        tipoNsr = state.tipoNsr,
                        habilitarLocalizacao = state.habilitarLocalizacao,
                        localizacaoAutomatica = state.localizacaoAutomatica,
                        exibirLocalizacaoDetalhes = state.exibirLocalizacaoDetalhes,
                        fotoHabilitada = state.fotoHabilitada,
                        fotoObrigatoria = state.fotoObrigatoria,
                        fotoValidarComprovante = state.fotoValidarComprovante,

                        diaInicioFechamentoRH = state.diaInicioFechamentoRH,
                        bancoHorasHabilitado = state.bancoHorasHabilitado,
                        bancoHorasCicloMeses = state.bancoHorasCicloMeses,
                        bancoHorasDataInicioCiclo = state.bancoHorasDataInicioCiclo,
                        bancoHorasZerarAoFinalCiclo = state.bancoHorasZerarAoFinalCiclo,
                        exigeJustificativaInconsistencia = state.exigeJustificativaInconsistencia,

                        comentarioHabilitado = state.comentarioHabilitado,
                        comentarioObrigatorioHoraExtra = state.comentarioObrigatorioHoraExtra,
                        exibirDuracaoTurno = state.exibirDuracaoTurno,
                        exibirDuracaoIntervalo = state.exibirDuracaoIntervalo
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
