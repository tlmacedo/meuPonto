// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings.empregos.editar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.TipoNsr
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.AtualizarEmpregoUseCase
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
import java.time.Duration
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
            is EditarEmpregoAction.AlterarDataInicioTrabalho -> alterarDataInicioTrabalho(action.data)
            
            is EditarEmpregoAction.AlterarCargaHorariaDiaria -> alterarCargaHorariaDiaria(action.duracao)
            is EditarEmpregoAction.AlterarAcrescimoDiasPontes -> alterarAcrescimoDiasPontes(action.minutos)
            is EditarEmpregoAction.AlterarJornadaMaximaDiaria -> alterarJornadaMaximaDiaria(action.minutos)
            is EditarEmpregoAction.AlterarTurnoMaximo -> alterarTurnoMaximo(action.minutos)
            is EditarEmpregoAction.AlterarIntervaloMinimo -> alterarIntervaloMinimo(action.minutos)
            is EditarEmpregoAction.AlterarIntervaloInterjornada -> alterarIntervaloInterjornada(action.minutos)
            is EditarEmpregoAction.AlterarToleranciaIntervaloMais -> alterarToleranciaIntervaloMais(action.minutos)
            
            is EditarEmpregoAction.AlterarHabilitarNsr -> alterarHabilitarNsr(action.habilitado)
            is EditarEmpregoAction.AlterarTipoNsr -> alterarTipoNsr(action.tipo)
            is EditarEmpregoAction.AlterarHabilitarLocalizacao -> alterarHabilitarLocalizacao(action.habilitado)
            is EditarEmpregoAction.AlterarLocalizacaoAutomatica -> alterarLocalizacaoAutomatica(action.automatica)
            is EditarEmpregoAction.AlterarHabilitarFotoComprovante -> alterarHabilitarFotoComprovante(action.habilitado)

            is EditarEmpregoAction.AlterarExigeJustificativa -> alterarExigeJustificativa(action.exigir)
            is EditarEmpregoAction.AlterarPrimeiroDiaSemana -> alterarPrimeiroDiaSemana(action.dia)
            is EditarEmpregoAction.AlterarDiaInicioFechamentoRH -> alterarDiaInicioFechamentoRH(action.dia)
            is EditarEmpregoAction.AlterarBancoHorasHabilitado -> alterarBancoHorasHabilitado(action.habilitado)
            is EditarEmpregoAction.AlterarPeriodoBancoHoras -> alterarPeriodoBancoHoras(action.valor)
            is EditarEmpregoAction.AlterarDataInicioCicloBanco -> alterarDataInicioCicloBanco(action.data)
            is EditarEmpregoAction.AlterarZerarBancoAntesPeriodo -> alterarZerarBancoAntesPeriodo(action.zerar)
            
            is EditarEmpregoAction.ToggleSecao -> toggleSecao(action.secao)
            is EditarEmpregoAction.Salvar -> salvar()
            is EditarEmpregoAction.Cancelar -> cancelar()
            is EditarEmpregoAction.LimparErro -> limparErro()
            else -> {} // Para ações que não precisam de lógica no VM ou ainda não implementadas
        }
    }

    fun setShowInicioTrabalhoPicker(show: Boolean) = _uiState.update { it.copy(showInicioTrabalhoPicker = show) }
    fun setShowDataInicioCicloPicker(show: Boolean) = _uiState.update { it.copy(showDataInicioCicloPicker = show) }

    private fun carregarEmprego(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val resultado = obterEmpregoComConfiguracaoUseCase(id)) {
                is ObterEmpregoComConfiguracaoUseCase.Resultado.Sucesso -> {
                    val emprego = resultado.emprego
                    val config = resultado.configuracao

                    val versaoVigente = versaoJornadaRepository.buscarVigente(id)

                    val periodoBancoValor = when {
                        versaoVigente?.periodoBancoSemanas ?: 0 > 0 -> versaoVigente!!.periodoBancoSemanas
                        versaoVigente?.periodoBancoMeses ?: 0 > 0 -> versaoVigente!!.periodoBancoMeses + 3
                        else -> 0
                    }

                    _uiState.update {
                        it.copy(
                            empregoId = emprego.id,
                            isNovoEmprego = false,
                            nome = emprego.nome,
                            dataInicioTrabalho = emprego.dataInicioTrabalho,
                            
                            cargaHorariaDiaria = Duration.ofMinutes((versaoVigente?.cargaHorariaDiariaMinutos ?: 480).toLong()),
                            acrescimoMinutosDiasPontes = versaoVigente?.acrescimoMinutosDiasPontes ?: 0,
                            jornadaMaximaDiariaMinutos = versaoVigente?.jornadaMaximaDiariaMinutos ?: 600,
                            turnoMaximoMinutos = versaoVigente?.turnoMaximoMinutos ?: 360,
                            intervaloMinimoMinutos = 60, // Padrão
                            intervaloInterjornadaMinutos = versaoVigente?.intervaloMinimoInterjornadaMinutos ?: 660,
                            toleranciaIntervaloMaisMinutos = versaoVigente?.toleranciaIntervaloMaisMinutos ?: 0,
                            
                            habilitarNsr = config.habilitarNsr,
                            tipoNsr = config.tipoNsr,
                            habilitarLocalizacao = config.habilitarLocalizacao,
                            localizacaoAutomatica = config.localizacaoAutomatica,
                            habilitarFotoComprovante = config.fotoHabilitada,

                            exigeJustificativaInconsistencia = versaoVigente?.exigeJustificativaInconsistencia ?: false,
                            primeiroDiaSemana = versaoVigente?.primeiroDiaSemana ?: DiaSemana.SEGUNDA,
                            diaInicioFechamentoRH = versaoVigente?.diaInicioFechamentoRH ?: 1,
                            bancoHorasHabilitado = versaoVigente?.bancoHorasHabilitado ?: false,
                            periodoBancoValor = periodoBancoValor,
                            dataInicioCicloBanco = versaoVigente?.dataInicioCicloBancoAtual,
                            zerarBancoAntesPeriodo = versaoVigente?.zerarBancoAntesPeriodo ?: false,
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
    private fun alterarCargaHorariaDiaria(duracao: Duration) = _uiState.update { it.copy(cargaHorariaDiaria = duracao) }
    private fun alterarAcrescimoDiasPontes(minutos: Int) = _uiState.update { it.copy(acrescimoMinutosDiasPontes = minutos) }
    private fun alterarJornadaMaximaDiaria(minutos: Int) = _uiState.update { it.copy(jornadaMaximaDiariaMinutos = minutos) }
    private fun alterarTurnoMaximo(minutos: Int) = _uiState.update { it.copy(turnoMaximoMinutos = minutos) }
    private fun alterarIntervaloMinimo(minutos: Int) = _uiState.update { it.copy(intervaloMinimoMinutos = minutos) }
    private fun alterarIntervaloInterjornada(minutos: Int) = _uiState.update { it.copy(intervaloInterjornadaMinutos = minutos) }
    private fun alterarToleranciaIntervaloMais(minutos: Int) = _uiState.update { it.copy(toleranciaIntervaloMaisMinutos = minutos) }
    private fun alterarHabilitarNsr(habilitado: Boolean) = _uiState.update { it.copy(habilitarNsr = habilitado) }
    private fun alterarTipoNsr(tipo: TipoNsr) = _uiState.update { it.copy(tipoNsr = tipo) }
    private fun alterarHabilitarLocalizacao(habilitado: Boolean) = _uiState.update { it.copy(habilitarLocalizacao = habilitado) }
    private fun alterarLocalizacaoAutomatica(automatica: Boolean) = _uiState.update { it.copy(localizacaoAutomatica = automatica) }
    private fun alterarHabilitarFotoComprovante(habilitado: Boolean) = _uiState.update { it.copy(habilitarFotoComprovante = habilitado) }
    private fun alterarExigeJustificativa(exigir: Boolean) = _uiState.update { it.copy(exigeJustificativaInconsistencia = exigir) }
    private fun alterarPrimeiroDiaSemana(dia: DiaSemana) = _uiState.update { it.copy(primeiroDiaSemana = dia) }
    private fun alterarDiaInicioFechamentoRH(dia: Int) = _uiState.update { it.copy(diaInicioFechamentoRH = dia.coerceIn(1, 28)) }

    private fun alterarBancoHorasHabilitado(habilitado: Boolean) = _uiState.update { state ->
        state.copy(
            bancoHorasHabilitado = habilitado,
            periodoBancoValor = if (habilitado) state.periodoBancoValor.coerceAtLeast(1) else 0,
            dataInicioCicloBanco = if (habilitado && state.dataInicioCicloBanco == null) LocalDate.now() else state.dataInicioCicloBanco
        )
    }

    private fun alterarPeriodoBancoHoras(valor: Int) = _uiState.update { state ->
        state.copy(periodoBancoValor = valor, bancoHorasHabilitado = if (valor > 0) true else state.bancoHorasHabilitado)
    }

    private fun alterarDataInicioCicloBanco(data: LocalDate?) = _uiState.update { it.copy(dataInicioCicloBanco = data, showDataInicioCicloPicker = false) }
    private fun alterarZerarBancoAntesPeriodo(zerar: Boolean) = _uiState.update { it.copy(zerarBancoAntesPeriodo = zerar) }
    private fun toggleSecao(secao: SecaoFormulario) = _uiState.update { state -> state.copy(secaoExpandida = if (state.secaoExpandida == secao) null else secao) }

    private fun salvar() {
        val state = _uiState.value

        if (state.nome.isBlank()) {
            viewModelScope.launch { _eventos.emit(EditarEmpregoEvent.MostrarErro("Nome é obrigatório")) }
            return
        }

        if (state.bancoHorasHabilitado && state.periodoBancoValor > 0 && state.dataInicioCicloBanco == null) {
            viewModelScope.launch { _eventos.emit(EditarEmpregoEvent.MostrarErro("Data de início do ciclo é obrigatória")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                if (state.isNovoEmprego) {
                    val params = CriarEmpregoUseCase.Parametros(
                        nome = state.nome,
                        dataInicioTrabalho = state.dataInicioTrabalho ?: LocalDate.now(),
                        cargaHorariaDiariaMinutos = state.cargaHorariaDiaria.toMinutes().toInt(),
                        acrescimoMinutosDiasPontes = state.acrescimoMinutosDiasPontes,
                        jornadaMaximaDiariaMinutos = state.jornadaMaximaDiariaMinutos,
                        intervaloMinimoMinutos = state.intervaloMinimoMinutos,
                        intervaloMinimoInterjornadaMinutos = state.intervaloInterjornadaMinutos,
                        turnoMaximoMinutos = state.turnoMaximoMinutos,
                        toleranciaIntervaloMaisMinutos = state.toleranciaIntervaloMaisMinutos,
                        diaInicioFechamentoRH = state.diaInicioFechamentoRH,
                        primeiroDiaSemana = state.primeiroDiaSemana,
                        bancoHorasHabilitado = state.bancoHorasHabilitado,
                        periodoBancoMeses = state.periodoBancoMeses,
                        dataInicioCicloBanco = state.dataInicioCicloBanco,
                        zerarBancoAoFecharCiclo = state.zerarBancoAntesPeriodo,
                        habilitarNsr = state.habilitarNsr,
                        tipoNsr = state.tipoNsr,
                        fotoHabilitada = state.habilitarFotoComprovante,
                        fotoObrigatoria = false, // Padrao inicial
                        exigeJustificativaInconsistencia = state.exigeJustificativaInconsistencia
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
                        dataInicioTrabalho = state.dataInicioTrabalho ?: LocalDate.now(),
                        habilitarNsr = state.habilitarNsr,
                        tipoNsr = state.tipoNsr,
                        fotoHabilitada = state.habilitarFotoComprovante,
                        fotoObrigatoria = false,
                        cargaHorariaDiariaMinutos = state.cargaHorariaDiaria.toMinutes().toInt(),
                        acrescimoMinutosDiasPontes = state.acrescimoMinutosDiasPontes,
                        jornadaMaximaDiariaMinutos = state.jornadaMaximaDiariaMinutos,
                        intervaloMinimoInterjornadaMinutos = state.intervaloInterjornadaMinutos,
                        turnoMaximoMinutos = state.turnoMaximoMinutos,
                        toleranciaIntervaloMaisMinutos = state.toleranciaIntervaloMaisMinutos,
                        exigeJustificativaInconsistencia = state.exigeJustificativaInconsistencia,
                        diaInicioFechamentoRH = state.diaInicioFechamentoRH,
                        primeiroDiaSemana = state.primeiroDiaSemana,
                        bancoHorasHabilitado = state.bancoHorasHabilitado,
                        periodoBancoMeses = state.periodoBancoMeses,
                        dataInicioCicloBanco = state.dataInicioCicloBanco,
                        zerarBancoAoFecharCiclo = state.zerarBancoAntesPeriodo
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
