package br.com.tlmacedo.meuponto.presentation.screen.settings.versoes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.versaojornada.AtualizarVersaoJornadaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.versaojornada.CriarVersaoJornadaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.versaojornada.VersaoJornadaUseCases
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
import javax.inject.Inject

/**
 * ViewModel da tela de edição de versão de jornada.
 *
 * Recebe tanto empregoId quanto versaoId via navegação para garantir
 * consistência e permitir criação de novas versões diretamente.
 *
 * @author Thiago
 * @since 4.0.0
 */
@HiltViewModel
class EditarVersaoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaUseCases: VersaoJornadaUseCases
) : ViewModel() {

    private val empregoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val versaoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_VERSAO_ID) ?: -1L

    private val _uiState = MutableStateFlow(EditarVersaoUiState())
    val uiState: StateFlow<EditarVersaoUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<EditarVersaoEvent>()
    val eventos: SharedFlow<EditarVersaoEvent> = _eventos.asSharedFlow()

    init {
        validarArgumentosECarregar()
    }

    private fun validarArgumentosECarregar() {
        when {
            empregoId <= 0L -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Emprego inválido"
                    )
                }
            }
            versaoId > 0L -> {
                carregarVersao(versaoId)
            }
            else -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNovaVersao = true,
                        empregoId = empregoId
                    )
                }
            }
        }
    }

    fun onAction(action: EditarVersaoAction) {
        when (action) {
            is EditarVersaoAction.AlterarDescricao -> alterarDescricao(action.descricao)
            is EditarVersaoAction.AlterarDataInicio -> alterarDataInicio(action.data)
            is EditarVersaoAction.AlterarDataFim -> alterarDataFim(action.data)
            is EditarVersaoAction.AlterarJornadaMaxima -> alterarJornadaMaxima(action.minutos)
            is EditarVersaoAction.AlterarIntervaloInterjornada -> alterarIntervaloInterjornada(action.minutos)
            is EditarVersaoAction.AlterarIntervaloAlmoco -> _uiState.update { it.copy(intervaloMinimoAlmocoMinutos = action.minutos) }
            is EditarVersaoAction.AlterarIntervaloDescanso -> _uiState.update { it.copy(intervaloMinimoDescansoMinutos = action.minutos) }
            is EditarVersaoAction.AlterarToleranciaIntervalo -> alterarToleranciaIntervalo(action.minutos)
            is EditarVersaoAction.AlterarToleranciaRetornoIntervalo -> _uiState.update { it.copy(toleranciaRetornoIntervaloMinutos = action.minutos) }
            is EditarVersaoAction.AlterarTurnoMaximo -> _uiState.update { it.copy(turnoMaximoMinutos = action.minutos) }

            // Carga horária
            is EditarVersaoAction.AlterarCargaHorariaDiaria -> _uiState.update { it.copy(cargaHorariaDiariaMinutos = action.minutos) }
            is EditarVersaoAction.AlterarAcrescimoDiasPontes -> _uiState.update { it.copy(acrescimoMinutosDiasPontes = action.minutos) }
            is EditarVersaoAction.AlterarCargaHorariaSemanal -> _uiState.update { it.copy(cargaHorariaSemanalMinutos = action.minutos) }

            // Período/Saldo
            is EditarVersaoAction.AlterarPrimeiroDiaSemana -> _uiState.update { it.copy(primeiroDiaSemana = action.dia) }
            is EditarVersaoAction.AlterarDiaInicioFechamentoRH -> _uiState.update { it.copy(diaInicioFechamentoRH = action.dia) }
            is EditarVersaoAction.AlterarZerarSaldoSemanal -> _uiState.update { it.copy(zerarSaldoSemanal = action.zerar) }
            is EditarVersaoAction.AlterarZerarSaldoPeriodoRH -> _uiState.update { it.copy(zerarSaldoPeriodoRH = action.zerar) }
            is EditarVersaoAction.AlterarOcultarSaldoTotal -> _uiState.update { it.copy(ocultarSaldoTotal = action.ocultar) }

            // Banco de Horas
            is EditarVersaoAction.AlterarBancoHorasHabilitado -> _uiState.update { it.copy(bancoHorasHabilitado = action.habilitar) }
            is EditarVersaoAction.AlterarPeriodoBancoDias -> _uiState.update { it.copy(periodoBancoDias = action.dias) }
            is EditarVersaoAction.AlterarPeriodoBancoSemanas -> _uiState.update { it.copy(periodoBancoSemanas = action.semanas) }
            is EditarVersaoAction.AlterarPeriodoBancoMeses -> _uiState.update { it.copy(periodoBancoMeses = action.meses) }
            is EditarVersaoAction.AlterarPeriodoBancoAnos -> _uiState.update { it.copy(periodoBancoAnos = action.anos) }
            is EditarVersaoAction.AlterarDataInicioCicloBancoAtual -> _uiState.update { it.copy(dataInicioCicloBancoAtual = action.data, showDataInicioCicloBancoPicker = false) }
            is EditarVersaoAction.AlterarProgressoCicloBanco -> alterarProgressoCicloBanco(action.progresso)
            is EditarVersaoAction.AlterarZerarBancoAntesPeriodo -> _uiState.update { it.copy(zerarBancoAntesPeriodo = action.zerar) }
            is EditarVersaoAction.AlterarHabilitarSugestaoAjuste -> _uiState.update { it.copy(habilitarSugestaoAjuste = action.habilitar) }
            is EditarVersaoAction.AlterarDiasUteisLembreteFechamento -> _uiState.update { it.copy(diasUteisLembreteFechamento = action.dias) }

            // Validação
            is EditarVersaoAction.AlterarExigeJustificativaInconsistencia -> _uiState.update { it.copy(exigeJustificativaInconsistencia = action.exige) }

            is EditarVersaoAction.MostrarDataInicioPicker -> mostrarDataInicioPicker(action.mostrar)
            is EditarVersaoAction.MostrarDataFimPicker -> mostrarDataFimPicker(action.mostrar)
            is EditarVersaoAction.MostrarDataInicioCicloBancoPicker -> _uiState.update { it.copy(showDataInicioCicloBancoPicker = action.mostrar) }
            is EditarVersaoAction.ToggleSecao -> toggleSecao(action.secao)
            is EditarVersaoAction.Salvar -> salvar()
            is EditarVersaoAction.SalvarVigencia -> salvarVigencia()
            is EditarVersaoAction.SalvarJornada -> salvarJornada()
            is EditarVersaoAction.SalvarFechamento -> salvarFechamento()
            is EditarVersaoAction.SalvarBancoHoras -> salvarBancoHoras()
            is EditarVersaoAction.SalvarValidacao -> salvarValidacao()
            is EditarVersaoAction.Cancelar -> cancelar()
            is EditarVersaoAction.LimparErro -> limparErro()
            is EditarVersaoAction.ConfigurarHorarios -> configurarHorarios()
        }
    }

    private fun carregarVersao(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val versao = versaoJornadaRepository.buscarPorId(id)

                if (versao != null) {
                    if (versao.empregoId != empregoId) {
                        Timber.w(
                            "Versão %d pertence ao emprego %d, mas foi solicitada para emprego %d",
                            id, versao.empregoId, empregoId
                        )
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Versão não pertence a este emprego"
                            )
                        }
                        return@launch
                    }

                    val horarios = horarioDiaSemanaRepository.buscarPorVersaoJornada(id)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNovaVersao = false,
                            empregoId = versao.empregoId,
                            versaoId = versao.id,
                            descricao = versao.descricao ?: "",
                            dataInicio = versao.dataInicio,
                            dataFim = versao.dataFim,
                            numeroVersao = versao.numeroVersao,
                            vigente = versao.vigente,
                            jornadaMaximaDiariaMinutos = versao.jornadaMaximaDiariaMinutos,
                            intervaloMinimoInterjornadaMinutos = versao.intervaloMinimoInterjornadaMinutos,
                            intervaloMinimoAlmocoMinutos = versao.intervaloMinimoAlmocoMinutos,
                            intervaloMinimoDescansoMinutos = versao.intervaloMinimoDescansoMinutos,
                            toleranciaIntervaloMaisMinutos = versao.toleranciaIntervaloMaisMinutos,
                            toleranciaRetornoIntervaloMinutos = versao.toleranciaRetornoIntervaloMinutos,
                            turnoMaximoMinutos = versao.turnoMaximoMinutos,

                            // Carga horária
                            cargaHorariaDiariaMinutos = versao.cargaHorariaDiariaMinutos,
                            acrescimoMinutosDiasPontes = versao.acrescimoMinutosDiasPontes,
                            cargaHorariaSemanalMinutos = versao.cargaHorariaSemanalMinutos,

                            // Período/Saldo
                            primeiroDiaSemana = versao.primeiroDiaSemana,
                            diaInicioFechamentoRH = versao.diaInicioFechamentoRH,
                            zerarSaldoSemanal = versao.zerarSaldoSemanal,
                            zerarSaldoPeriodoRH = versao.zerarSaldoPeriodoRH,
                            ocultarSaldoTotal = versao.ocultarSaldoTotal,

                            // Banco de Horas
                            bancoHorasHabilitado = versao.bancoHorasHabilitado,
                            periodoBancoDias = versao.periodoBancoDias,
                            periodoBancoSemanas = versao.periodoBancoSemanas,
                            periodoBancoMeses = versao.periodoBancoMeses,
                            periodoBancoAnos = versao.periodoBancoAnos,
                            dataInicioCicloBancoAtual = versao.dataInicioCicloBancoAtual,
                            diasUteisLembreteFechamento = versao.diasUteisLembreteFechamento,
                            habilitarSugestaoAjuste = versao.habilitarSugestaoAjuste,
                            zerarBancoAntesPeriodo = versao.zerarBancoAntesPeriodo,

                            // Validação
                            exigeJustificativaInconsistencia = versao.exigeJustificativaInconsistencia,

                            originalDescricao = versao.descricao ?: "",
                            originalDataInicio = versao.dataInicio,
                            originalDataFim = versao.dataFim,
                            originalJornadaMaximaDiariaMinutos = versao.jornadaMaximaDiariaMinutos,
                            originalIntervaloMinimoInterjornadaMinutos = versao.intervaloMinimoInterjornadaMinutos,
                            originalIntervaloMinimoAlmocoMinutos = versao.intervaloMinimoAlmocoMinutos,
                            originalIntervaloMinimoDescansoMinutos = versao.intervaloMinimoDescansoMinutos,
                            originalToleranciaIntervaloMaisMinutos = versao.toleranciaIntervaloMaisMinutos,
                            originalToleranciaRetornoIntervaloMinutos = versao.toleranciaRetornoIntervaloMinutos,
                            originalTurnoMaximoMinutos = versao.turnoMaximoMinutos,
                            originalCargaHorariaDiariaMinutos = versao.cargaHorariaDiariaMinutos,
                            originalAcrescimoMinutosDiasPontes = versao.acrescimoMinutosDiasPontes,
                            originalCargaHorariaSemanalMinutos = versao.cargaHorariaSemanalMinutos,
                            originalPrimeiroDiaSemana = versao.primeiroDiaSemana,
                            originalDiaInicioFechamentoRH = versao.diaInicioFechamentoRH,
                            originalZerarSaldoSemanal = versao.zerarSaldoSemanal,
                            originalZerarSaldoPeriodoRH = versao.zerarSaldoPeriodoRH,
                            originalOcultarSaldoTotal = versao.ocultarSaldoTotal,
                            originalBancoHorasHabilitado = versao.bancoHorasHabilitado,
                            originalPeriodoBancoDias = versao.periodoBancoDias,
                            originalPeriodoBancoSemanas = versao.periodoBancoSemanas,
                            originalPeriodoBancoMeses = versao.periodoBancoMeses,
                            originalPeriodoBancoAnos = versao.periodoBancoAnos,
                            originalDataInicioCicloBancoAtual = versao.dataInicioCicloBancoAtual,
                            originalDiasUteisLembreteFechamento = versao.diasUteisLembreteFechamento,
                            originalHabilitarSugestaoAjuste = versao.habilitarSugestaoAjuste,
                            originalZerarBancoAntesPeriodo = versao.zerarBancoAntesPeriodo,
                            originalExigeJustificativaInconsistencia = versao.exigeJustificativaInconsistencia,

                            horarios = horarios
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _eventos.emit(EditarVersaoEvent.MostrarMensagem("Versão não encontrada"))
                    _eventos.emit(EditarVersaoEvent.Voltar)
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar versão %d", id)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao carregar versão"
                    )
                }
            }
        }
    }

    private fun alterarDescricao(descricao: String) {
        _uiState.update { it.copy(descricao = descricao) }
    }

    private fun alterarDataInicio(data: java.time.LocalDate) {
        _uiState.update { it.copy(dataInicio = data, showDataInicioPicker = false) }
    }

    private fun alterarDataFim(data: java.time.LocalDate?) {
        _uiState.update { it.copy(dataFim = data, showDataFimPicker = false) }
    }

    private fun alterarJornadaMaxima(minutos: Int) {
        _uiState.update { it.copy(jornadaMaximaDiariaMinutos = minutos) }
    }

    private fun alterarIntervaloInterjornada(minutos: Int) {
        _uiState.update { it.copy(intervaloMinimoInterjornadaMinutos = minutos) }
    }

    private fun alterarToleranciaIntervalo(minutos: Int) {
        _uiState.update { it.copy(toleranciaIntervaloMaisMinutos = minutos) }
    }

    private fun mostrarDataInicioPicker(mostrar: Boolean) {
        _uiState.update { it.copy(showDataInicioPicker = mostrar) }
    }

    private fun mostrarDataFimPicker(mostrar: Boolean) {
        _uiState.update { it.copy(showDataFimPicker = mostrar) }
    }

    private fun toggleSecao(secao: SecaoVersao) {
        _uiState.update { state ->
            state.copy(
                secaoExpandida = if (state.secaoExpandida == secao) null else secao
            )
        }
    }

    private fun salvarVigencia() {
        val state = _uiState.value
        executarAtualizacaoGranular(
            criarParams(state).copy(
                descricao = state.descricao,
                dataInicio = state.dataInicio,
                dataFim = state.dataFim
            ),
            "Vigência atualizada"
        )
    }

    private fun salvarJornada() {
        val state = _uiState.value
        executarAtualizacaoGranular(
            criarParams(state).copy(
                cargaHorariaDiariaMinutos = state.cargaHorariaDiariaMinutos,
                jornadaMaximaDiariaMinutos = state.jornadaMaximaDiariaMinutos,
                intervaloMinimoAlmocoMinutos = state.intervaloMinimoAlmocoMinutos,
                intervaloMinimoDescansoMinutos = state.intervaloMinimoDescansoMinutos,
                toleranciaRetornoIntervaloMinutos = state.toleranciaRetornoIntervaloMinutos,
                intervaloMinimoInterjornadaMinutos = state.intervaloMinimoInterjornadaMinutos,
                turnoMaximoMinutos = state.turnoMaximoMinutos
            ),
            "Jornada atualizada"
        )
    }

    private fun salvarFechamento() {
        val state = _uiState.value
        executarAtualizacaoGranular(
            criarParams(state).copy(
                diaInicioFechamentoRH = state.diaInicioFechamentoRH,
                primeiroDiaSemana = state.primeiroDiaSemana,
                zerarSaldoPeriodoRH = state.zerarSaldoPeriodoRH,
                ocultarSaldoTotal = state.ocultarSaldoTotal
            ),
            "Fechamento atualizado"
        )
    }

    private fun salvarBancoHoras() {
        val state = _uiState.value
        executarAtualizacaoGranular(
            criarParams(state).copy(
                bancoHorasHabilitado = state.bancoHorasHabilitado,
                periodoBancoDias = state.periodoBancoDias,
                periodoBancoSemanas = state.periodoBancoSemanas,
                periodoBancoMeses = state.periodoBancoMeses,
                periodoBancoAnos = state.periodoBancoAnos,
                dataInicioCicloBancoAtual = state.dataInicioCicloBancoAtual,
                habilitarSugestaoAjuste = state.habilitarSugestaoAjuste
            ),
            "Banco de horas atualizado"
        )
    }

    private fun salvarValidacao() {
        val state = _uiState.value
        executarAtualizacaoGranular(
            criarParams(state).copy(
                exigeJustificativaInconsistencia = state.exigeJustificativaInconsistencia,
                toleranciaIntervaloMaisMinutos = state.toleranciaIntervaloMaisMinutos
            ),
            "Regras de validação atualizadas"
        )
    }

    private fun criarParams(state: EditarVersaoUiState) = AtualizarVersaoJornadaUseCase.Params(
        versaoId = state.versaoId!!,
        dataInicio = state.originalDataInicio,
        dataFim = state.originalDataFim,
        descricao = state.originalDescricao.ifBlank { null },
        vigente = state.vigente,
        jornadaMaximaDiariaMinutos = state.originalJornadaMaximaDiariaMinutos,
        intervaloMinimoInterjornadaMinutos = state.originalIntervaloMinimoInterjornadaMinutos,
        intervaloMinimoAlmocoMinutos = state.originalIntervaloMinimoAlmocoMinutos,
        intervaloMinimoDescansoMinutos = state.originalIntervaloMinimoDescansoMinutos,
        toleranciaIntervaloMaisMinutos = state.originalToleranciaIntervaloMaisMinutos,
        toleranciaRetornoIntervaloMinutos = state.originalToleranciaRetornoIntervaloMinutos,
        turnoMaximoMinutos = state.originalTurnoMaximoMinutos,
        cargaHorariaDiariaMinutos = state.originalCargaHorariaDiariaMinutos,
        acrescimoMinutosDiasPontes = state.originalAcrescimoMinutosDiasPontes,
        cargaHorariaSemanalMinutos = state.originalCargaHorariaSemanalMinutos,
        primeiroDiaSemana = state.originalPrimeiroDiaSemana,
        diaInicioFechamentoRH = state.originalDiaInicioFechamentoRH,
        zerarSaldoSemanal = state.originalZerarSaldoSemanal,
        zerarSaldoPeriodoRH = state.originalZerarSaldoPeriodoRH,
        ocultarSaldoTotal = state.originalOcultarSaldoTotal,
        bancoHorasHabilitado = state.originalBancoHorasHabilitado,
        periodoBancoDias = state.originalPeriodoBancoDias,
        periodoBancoSemanas = state.originalPeriodoBancoSemanas,
        periodoBancoMeses = state.originalPeriodoBancoMeses,
        periodoBancoAnos = state.originalPeriodoBancoAnos,
        dataInicioCicloBancoAtual = state.originalDataInicioCicloBancoAtual,
        diasUteisLembreteFechamento = state.originalDiasUteisLembreteFechamento,
        habilitarSugestaoAjuste = state.originalHabilitarSugestaoAjuste,
        zerarBancoAntesPeriodo = state.originalZerarBancoAntesPeriodo,
        exigeJustificativaInconsistencia = state.originalExigeJustificativaInconsistencia
    )

    private fun executarAtualizacaoGranular(params: AtualizarVersaoJornadaUseCase.Params, msg: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                versaoJornadaUseCases.atualizar(params).onSuccess {
                    _eventos.emit(EditarVersaoEvent.MostrarMensagem(msg))
                    carregarVersao(params.versaoId)
                }.onFailure { e ->
                    _eventos.emit(EditarVersaoEvent.MostrarMensagem(e.message ?: "Erro ao salvar"))
                }
            } catch (e: Exception) {
                _eventos.emit(EditarVersaoEvent.MostrarMensagem("Erro ao salvar: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun alterarProgressoCicloBanco(progresso: Float) {
        val p = progresso.toInt()
        _uiState.update { state ->
            when {
                p <= 5 -> state.copy(
                    periodoBancoDias = p + 1,
                    periodoBancoSemanas = 0,
                    periodoBancoMeses = 0,
                    periodoBancoAnos = 0
                )
                p <= 8 -> state.copy(
                    periodoBancoDias = 0,
                    periodoBancoSemanas = p - 5,
                    periodoBancoMeses = 0,
                    periodoBancoAnos = 0
                )
                p <= 19 -> state.copy(
                    periodoBancoDias = 0,
                    periodoBancoSemanas = 0,
                    periodoBancoMeses = p - 8,
                    periodoBancoAnos = 0
                )
                else -> state.copy(
                    periodoBancoDias = 0,
                    periodoBancoSemanas = 0,
                    periodoBancoMeses = 0,
                    periodoBancoAnos = 1
                )
            }
        }
    }


    private fun salvar() {
        val state = _uiState.value

        if (state.empregoId <= 0L) {
            viewModelScope.launch {
                _eventos.emit(EditarVersaoEvent.MostrarMensagem("Emprego inválido"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val resultado = if (state.isNovaVersao) {
                        val params = CriarVersaoJornadaUseCase.Params(
                            dataInicio = state.dataInicio,
                            dataFim = state.dataFim,
                            descricao = state.descricao.ifBlank { null },
                            vigente = state.vigente,
                            jornadaMaximaDiariaMinutos = state.jornadaMaximaDiariaMinutos,
                            intervaloMinimoInterjornadaMinutos = state.intervaloMinimoInterjornadaMinutos,
                            intervaloMinimoAlmocoMinutos = state.intervaloMinimoAlmocoMinutos,
                            intervaloMinimoDescansoMinutos = state.intervaloMinimoDescansoMinutos,
                            toleranciaIntervaloMaisMinutos = state.toleranciaIntervaloMaisMinutos,
                            toleranciaRetornoIntervaloMinutos = state.toleranciaRetornoIntervaloMinutos,
                            turnoMaximoMinutos = state.turnoMaximoMinutos,

                        // Carga horária
                        cargaHorariaDiariaMinutos = state.cargaHorariaDiariaMinutos,
                        acrescimoMinutosDiasPontes = state.acrescimoMinutosDiasPontes,
                        cargaHorariaSemanalMinutos = state.cargaHorariaSemanalMinutos,

                        // Período/Saldo
                        primeiroDiaSemana = state.primeiroDiaSemana,
                        diaInicioFechamentoRH = state.diaInicioFechamentoRH,
                        zerarSaldoSemanal = state.zerarSaldoSemanal,
                        zerarSaldoPeriodoRH = state.zerarSaldoPeriodoRH,
                        ocultarSaldoTotal = state.ocultarSaldoTotal,

                        // Banco de Horas
                        bancoHorasHabilitado = state.bancoHorasHabilitado,
                        periodoBancoDias = state.periodoBancoDias,
                        periodoBancoSemanas = state.periodoBancoSemanas,
                        periodoBancoMeses = state.periodoBancoMeses,
                        periodoBancoAnos = state.periodoBancoAnos,
                        dataInicioCicloBancoAtual = state.dataInicioCicloBancoAtual,
                        diasUteisLembreteFechamento = state.diasUteisLembreteFechamento,
                        habilitarSugestaoAjuste = state.habilitarSugestaoAjuste,
                        zerarBancoAntesPeriodo = state.zerarBancoAntesPeriodo,

                        // Validação
                        exigeJustificativaInconsistencia = state.exigeJustificativaInconsistencia,

                        criarHorariosPadrao = true
                    )
                    versaoJornadaUseCases.criar(params)
                } else {
                        val params = AtualizarVersaoJornadaUseCase.Params(
                            versaoId = state.versaoId!!,
                            dataInicio = state.dataInicio,
                            dataFim = state.dataFim,
                            descricao = state.descricao.ifBlank { null },
                            vigente = state.vigente,
                            jornadaMaximaDiariaMinutos = state.jornadaMaximaDiariaMinutos,
                            intervaloMinimoInterjornadaMinutos = state.intervaloMinimoInterjornadaMinutos,
                            intervaloMinimoAlmocoMinutos = state.intervaloMinimoAlmocoMinutos,
                            intervaloMinimoDescansoMinutos = state.intervaloMinimoDescansoMinutos,
                            toleranciaIntervaloMaisMinutos = state.toleranciaIntervaloMaisMinutos,
                            toleranciaRetornoIntervaloMinutos = state.toleranciaRetornoIntervaloMinutos,
                            turnoMaximoMinutos = state.turnoMaximoMinutos,

                        // Carga horária
                        cargaHorariaDiariaMinutos = state.cargaHorariaDiariaMinutos,
                        acrescimoMinutosDiasPontes = state.acrescimoMinutosDiasPontes,
                        cargaHorariaSemanalMinutos = state.cargaHorariaSemanalMinutos,

                        // Período/Saldo
                        primeiroDiaSemana = state.primeiroDiaSemana,
                        diaInicioFechamentoRH = state.diaInicioFechamentoRH,
                        zerarSaldoSemanal = state.zerarSaldoSemanal,
                        zerarSaldoPeriodoRH = state.zerarSaldoPeriodoRH,
                        ocultarSaldoTotal = state.ocultarSaldoTotal,

                        // Banco de Horas
                        bancoHorasHabilitado = state.bancoHorasHabilitado,
                        periodoBancoDias = state.periodoBancoDias,
                        periodoBancoSemanas = state.periodoBancoSemanas,
                        periodoBancoMeses = state.periodoBancoMeses,
                        periodoBancoAnos = state.periodoBancoAnos,
                        dataInicioCicloBancoAtual = state.dataInicioCicloBancoAtual,
                        diasUteisLembreteFechamento = state.diasUteisLembreteFechamento,
                        habilitarSugestaoAjuste = state.habilitarSugestaoAjuste,
                        zerarBancoAntesPeriodo = state.zerarBancoAntesPeriodo,

                        // Validação
                        exigeJustificativaInconsistencia = state.exigeJustificativaInconsistencia
                    )
                    versaoJornadaUseCases.atualizar(params)
                }

                resultado.onSuccess {
                    _eventos.emit(EditarVersaoEvent.MostrarMensagem("Versão salva com sucesso"))
                    _eventos.emit(EditarVersaoEvent.SalvoComSucesso)
                }.onFailure { e ->
                    _eventos.emit(EditarVersaoEvent.MostrarMensagem(e.message ?: "Erro ao salvar"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao salvar versão")
                _eventos.emit(
                    EditarVersaoEvent.MostrarMensagem("Erro ao salvar: ${e.message}")
                )
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun cancelar() {
        viewModelScope.launch {
            _eventos.emit(EditarVersaoEvent.Voltar)
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun configurarHorarios() {
        val versaoId = _uiState.value.versaoId ?: return

        viewModelScope.launch {
            _eventos.emit(EditarVersaoEvent.NavegarParaHorarios(versaoId))
        }
    }
}
