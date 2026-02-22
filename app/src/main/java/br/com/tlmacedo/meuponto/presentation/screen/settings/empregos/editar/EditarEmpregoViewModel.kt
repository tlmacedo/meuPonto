// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoComConfiguracaoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.validacao.ValidarEmpregoUseCase
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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel da tela de edição/criação de emprego.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 3.0.0 - Refatoração completa do sistema de ciclos de banco de horas
 */
@HiltViewModel
class EditarEmpregoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val empregoRepository: EmpregoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val obterEmpregoComConfiguracaoUseCase: ObterEmpregoComConfiguracaoUseCase,
    private val validarEmpregoUseCase: ValidarEmpregoUseCase
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
            is EditarEmpregoAction.AlterarDataInicioTrabalho -> alterarDataInicioTrabalho(action.data)
            is EditarEmpregoAction.AlterarCargaHorariaDiaria -> alterarCargaHorariaDiaria(action.duracao)
            is EditarEmpregoAction.AlterarJornadaMaximaDiaria -> alterarJornadaMaximaDiaria(action.minutos)
            is EditarEmpregoAction.AlterarIntervaloMinimo -> alterarIntervaloMinimo(action.minutos)
            is EditarEmpregoAction.AlterarIntervaloInterjornada -> alterarIntervaloInterjornada(action.minutos)
            is EditarEmpregoAction.AlterarToleranciaIntervaloMais -> alterarToleranciaIntervaloMais(action.minutos)
            is EditarEmpregoAction.AlterarHabilitarNsr -> alterarHabilitarNsr(action.habilitado)
            is EditarEmpregoAction.AlterarTipoNsr -> alterarTipoNsr(action.tipo)
            is EditarEmpregoAction.AlterarHabilitarLocalizacao -> alterarHabilitarLocalizacao(action.habilitado)
            is EditarEmpregoAction.AlterarLocalizacaoAutomatica -> alterarLocalizacaoAutomatica(action.automatica)
            is EditarEmpregoAction.AlterarExigeJustificativa -> alterarExigeJustificativa(action.exigir)
            is EditarEmpregoAction.AlterarPrimeiroDiaSemana -> alterarPrimeiroDiaSemana(action.dia)
            is EditarEmpregoAction.AlterarDiaInicioFechamentoRH -> alterarDiaInicioFechamentoRH(action.dia)
            is EditarEmpregoAction.AlterarBancoHorasHabilitado -> alterarBancoHorasHabilitado(action.habilitado)
            is EditarEmpregoAction.AlterarPeriodoBancoHoras -> alterarPeriodoBancoHoras(action.valor)
            is EditarEmpregoAction.AlterarDataInicioCicloBanco -> alterarDataInicioCicloBanco(action.data)
            is EditarEmpregoAction.AlterarZerarSaldoPeriodoRH -> alterarZerarSaldoPeriodoRH(action.zerar)
            is EditarEmpregoAction.AlterarZerarBancoAntesPeriodo -> alterarZerarBancoAntesPeriodo(action.zerar)
            is EditarEmpregoAction.ToggleSecao -> toggleSecao(action.secao)
            is EditarEmpregoAction.Salvar -> salvar()
            is EditarEmpregoAction.Cancelar -> cancelar()
            is EditarEmpregoAction.LimparErro -> limparErro()
        }
    }

    fun setShowInicioTrabalhoPicker(show: Boolean) {
        _uiState.update { it.copy(showInicioTrabalhoPicker = show) }
    }

    fun setShowDataInicioCicloPicker(show: Boolean) {
        _uiState.update { it.copy(showDataInicioCicloPicker = show) }
    }

    private fun carregarEmprego(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val resultado = obterEmpregoComConfiguracaoUseCase(id)) {
                is ObterEmpregoComConfiguracaoUseCase.Resultado.Sucesso -> {
                    val emprego = resultado.emprego
                    val config = resultado.configuracao

                    // Converte semanas/meses para valor do slider
                    val periodoBancoValor = when {
                        config.periodoBancoSemanas > 0 -> config.periodoBancoSemanas
                        config.periodoBancoMeses > 0 -> config.periodoBancoMeses + 3
                        else -> 0
                    }

                    _uiState.update {
                        it.copy(
                            empregoId = emprego.id,
                            isNovoEmprego = false,
                            nome = emprego.nome,
                            dataInicioTrabalho = emprego.dataInicioTrabalho,
                            cargaHorariaDiaria = Duration.ofMinutes(config.cargaHorariaDiariaMinutos.toLong()),
                            jornadaMaximaDiariaMinutos = config.jornadaMaximaDiariaMinutos,
                            intervaloMinimoMinutos = config.intervaloMinimoMinutos,
                            intervaloInterjornadaMinutos = config.intervaloMinimoInterjornadaMinutos,
                            toleranciaIntervaloMaisMinutos = config.toleranciaIntervaloMaisMinutos,
                            habilitarNsr = config.habilitarNsr,
                            tipoNsr = config.tipoNsr,
                            habilitarLocalizacao = config.habilitarLocalizacao,
                            localizacaoAutomatica = config.localizacaoAutomatica,
                            exigeJustificativaInconsistencia = config.exigeJustificativaInconsistencia,
                            primeiroDiaSemana = config.primeiroDiaSemana,
                            diaInicioFechamentoRH = config.diaInicioFechamentoRH,
                            bancoHorasHabilitado = config.bancoHorasHabilitado,
                            periodoBancoValor = periodoBancoValor,
                            dataInicioCicloBanco = config.dataInicioCicloBancoAtual,
                            zerarSaldoPeriodoRH = config.zerarSaldoPeriodoRH,
                            zerarBancoAntesPeriodo = config.zerarBancoAntesPeriodo,
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

    private fun alterarDataInicioTrabalho(data: LocalDate?) {
        _uiState.update { it.copy(dataInicioTrabalho = data, showInicioTrabalhoPicker = false) }
    }

    private fun alterarCargaHorariaDiaria(duracao: Duration) {
        _uiState.update { it.copy(cargaHorariaDiaria = duracao) }
    }

    private fun alterarJornadaMaximaDiaria(minutos: Int) {
        _uiState.update { it.copy(jornadaMaximaDiariaMinutos = minutos) }
    }

    private fun alterarIntervaloMinimo(minutos: Int) {
        _uiState.update { it.copy(intervaloMinimoMinutos = minutos) }
    }

    private fun alterarIntervaloInterjornada(minutos: Int) {
        _uiState.update { it.copy(intervaloInterjornadaMinutos = minutos) }
    }

    private fun alterarToleranciaIntervaloMais(minutos: Int) {
        _uiState.update { it.copy(toleranciaIntervaloMaisMinutos = minutos) }
    }

    private fun alterarHabilitarNsr(habilitado: Boolean) {
        _uiState.update { it.copy(habilitarNsr = habilitado) }
    }

    private fun alterarTipoNsr(tipo: TipoNsr) {
        _uiState.update { it.copy(tipoNsr = tipo) }
    }

    private fun alterarHabilitarLocalizacao(habilitado: Boolean) {
        _uiState.update { it.copy(habilitarLocalizacao = habilitado) }
    }

    private fun alterarLocalizacaoAutomatica(automatica: Boolean) {
        _uiState.update { it.copy(localizacaoAutomatica = automatica) }
    }

    private fun alterarExigeJustificativa(exigir: Boolean) {
        _uiState.update { it.copy(exigeJustificativaInconsistencia = exigir) }
    }

    private fun alterarPrimeiroDiaSemana(dia: DiaSemana) {
        _uiState.update { it.copy(primeiroDiaSemana = dia) }
    }

    private fun alterarDiaInicioFechamentoRH(dia: Int) {
        _uiState.update { it.copy(diaInicioFechamentoRH = dia.coerceIn(1, 28)) }
    }

    private fun alterarBancoHorasHabilitado(habilitado: Boolean) {
        _uiState.update { state ->
            state.copy(
                bancoHorasHabilitado = habilitado,
                periodoBancoValor = if (habilitado) state.periodoBancoValor.coerceAtLeast(1) else 0,
                dataInicioCicloBanco = if (habilitado && state.dataInicioCicloBanco == null) {
                    LocalDate.now()
                } else {
                    state.dataInicioCicloBanco
                }
            )
        }
    }

    private fun alterarPeriodoBancoHoras(valor: Int) {
        _uiState.update { state ->
            state.copy(
                periodoBancoValor = valor,
                bancoHorasHabilitado = if (valor > 0) true else state.bancoHorasHabilitado
            )
        }
    }

    private fun alterarDataInicioCicloBanco(data: LocalDate?) {
        _uiState.update {
            it.copy(
                dataInicioCicloBanco = data,
                showDataInicioCicloPicker = false
            )
        }
    }

    private fun alterarZerarSaldoPeriodoRH(zerar: Boolean) {
        _uiState.update { it.copy(zerarSaldoPeriodoRH = zerar) }
    }

    private fun alterarZerarBancoAntesPeriodo(zerar: Boolean) {
        _uiState.update { it.copy(zerarBancoAntesPeriodo = zerar) }
    }

    private fun toggleSecao(secao: SecaoFormulario) {
        _uiState.update { state ->
            state.copy(
                secaoExpandida = if (state.secaoExpandida == secao) null else secao
            )
        }
    }

    private fun salvar() {
        val state = _uiState.value

        if (state.nome.isBlank()) {
            viewModelScope.launch {
                _eventos.emit(EditarEmpregoEvent.MostrarErro("Nome é obrigatório"))
            }
            return
        }

        if (state.bancoHorasHabilitado && state.periodoBancoValor > 0 && state.dataInicioCicloBanco == null) {
            viewModelScope.launch {
                _eventos.emit(EditarEmpregoEvent.MostrarErro("Data de início do ciclo é obrigatória"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val (semanas, meses) = when {
                    state.periodoBancoValor in 1..3 -> state.periodoBancoValor to 0
                    state.periodoBancoValor > 3 -> 0 to (state.periodoBancoValor - 3)
                    else -> 0 to 0
                }

                if (state.isNovoEmprego) {
                    val emprego = Emprego(
                        nome = state.nome.trim(),
                        dataInicioTrabalho = state.dataInicioTrabalho,
                        ativo = true,
                        arquivado = false
                    )
                    val novoEmpregoId = empregoRepository.inserir(emprego)

                    val configuracao = ConfiguracaoEmprego(
                        empregoId = novoEmpregoId,
                        cargaHorariaDiariaMinutos = state.cargaHorariaDiaria.toMinutes().toInt(),
                        jornadaMaximaDiariaMinutos = state.jornadaMaximaDiariaMinutos,
                        intervaloMinimoMinutos = state.intervaloMinimoMinutos,
                        intervaloMinimoInterjornadaMinutos = state.intervaloInterjornadaMinutos,
                        toleranciaIntervaloMaisMinutos = state.toleranciaIntervaloMaisMinutos,
                        habilitarNsr = state.habilitarNsr,
                        tipoNsr = state.tipoNsr,
                        habilitarLocalizacao = state.habilitarLocalizacao,
                        localizacaoAutomatica = state.localizacaoAutomatica,
                        exigeJustificativaInconsistencia = state.exigeJustificativaInconsistencia,
                        primeiroDiaSemana = state.primeiroDiaSemana,
                        diaInicioFechamentoRH = state.diaInicioFechamentoRH,
                        zerarSaldoPeriodoRH = state.zerarSaldoPeriodoRH,
                        bancoHorasHabilitado = state.bancoHorasHabilitado,
                        periodoBancoSemanas = semanas,
                        periodoBancoMeses = meses,
                        dataInicioCicloBancoAtual = state.dataInicioCicloBanco,
                        zerarBancoAntesPeriodo = state.zerarBancoAntesPeriodo
                    )
                    configuracaoEmpregoRepository.inserir(configuracao)

                    _eventos.emit(EditarEmpregoEvent.SalvoComSucesso("Emprego criado com sucesso"))
                } else {
                    val empregoExistente = empregoRepository.buscarPorId(state.empregoId!!)
                    if (empregoExistente != null) {
                        val empregoAtualizado = empregoExistente.copy(
                            nome = state.nome.trim(),
                            dataInicioTrabalho = state.dataInicioTrabalho,
                            atualizadoEm = LocalDateTime.now()
                        )
                        empregoRepository.atualizar(empregoAtualizado)
                    }

                    val configExistente = configuracaoEmpregoRepository.buscarPorEmpregoId(state.empregoId)
                    if (configExistente != null) {
                        val configAtualizada = configExistente.copy(
                            cargaHorariaDiariaMinutos = state.cargaHorariaDiaria.toMinutes().toInt(),
                            jornadaMaximaDiariaMinutos = state.jornadaMaximaDiariaMinutos,
                            intervaloMinimoMinutos = state.intervaloMinimoMinutos,
                            intervaloMinimoInterjornadaMinutos = state.intervaloInterjornadaMinutos,
                            toleranciaIntervaloMaisMinutos = state.toleranciaIntervaloMaisMinutos,
                            habilitarNsr = state.habilitarNsr,
                            tipoNsr = state.tipoNsr,
                            habilitarLocalizacao = state.habilitarLocalizacao,
                            localizacaoAutomatica = state.localizacaoAutomatica,
                            exigeJustificativaInconsistencia = state.exigeJustificativaInconsistencia,
                            primeiroDiaSemana = state.primeiroDiaSemana,
                            diaInicioFechamentoRH = state.diaInicioFechamentoRH,
                            zerarSaldoPeriodoRH = state.zerarSaldoPeriodoRH,
                            bancoHorasHabilitado = state.bancoHorasHabilitado,
                            periodoBancoSemanas = semanas,
                            periodoBancoMeses = meses,
                            dataInicioCicloBancoAtual = state.dataInicioCicloBanco,
                            zerarBancoAntesPeriodo = state.zerarBancoAntesPeriodo,
                            atualizadoEm = LocalDateTime.now()
                        )
                        configuracaoEmpregoRepository.atualizar(configAtualizada)
                    }

                    _eventos.emit(EditarEmpregoEvent.SalvoComSucesso("Alterações salvas com sucesso"))
                }
            } catch (e: Exception) {
                _eventos.emit(EditarEmpregoEvent.MostrarErro("Erro ao salvar: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun cancelar() {
        viewModelScope.launch {
            _eventos.emit(EditarEmpregoEvent.Voltar)
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }
}
