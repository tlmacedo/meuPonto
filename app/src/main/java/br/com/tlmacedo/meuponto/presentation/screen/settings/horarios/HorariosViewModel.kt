package br.com.tlmacedo.meuponto.presentation.screen.settings.horarios

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.presentation.navigation.MeuPontoDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel da tela de gerenciamento de horários por dia da semana.
 *
 * Gerencia os horários associados a uma versão de jornada específica,
 * permitindo edição individual de cada dia, cópia entre dias e
 * configuração de horários ideais.
 *
 * @author Thiago
 * @since 4.0.0
 */
@HiltViewModel
class HorariosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val empregoRepository: EmpregoRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository
) : ViewModel() {

    private val versaoJornadaId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_VERSAO_ID) ?: -1L

    private val empregoId: Long =
        savedStateHandle.get<Long>(MeuPontoDestinations.ARG_EMPREGO_ID) ?: -1L

    private val _uiState = MutableStateFlow(HorariosUiState())
    val uiState: StateFlow<HorariosUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<HorariosEvent>()
    val eventos: SharedFlow<HorariosEvent> = _eventos.asSharedFlow()

    init {
        validarArgumentosECarregar()
    }

    fun onAction(action: HorariosAction) {
        when (action) {
            is HorariosAction.Recarregar -> observarHorarios()
            is HorariosAction.SelecionarDia -> selecionarDia(action.horario)
            is HorariosAction.ToggleAtivo -> toggleAtivo(action.diaSemana)
            is HorariosAction.AlterarCargaHoraria -> alterarCargaHoraria(action.minutos)
            is HorariosAction.AlterarIntervaloMinimo -> alterarIntervaloMinimo(action.minutos)
            is HorariosAction.AlterarToleranciaIntervalo -> alterarToleranciaIntervalo(action.minutos)
            is HorariosAction.AlterarToleranciaIntervalo -> alterarToleranciaIntervalo(action.minutos)
            is HorariosAction.AbrirTimePicker -> abrirTimePicker(action.campo)
            is HorariosAction.SelecionarHorario -> selecionarHorario(action.horario)
            is HorariosAction.FecharTimePicker -> fecharTimePicker()
            is HorariosAction.FecharDialogEdicao -> fecharDialogEdicao()
            is HorariosAction.SalvarHorario -> salvarHorario()
            is HorariosAction.AbrirDialogCopiar -> abrirDialogCopiar(action.diaSemana)
            is HorariosAction.FecharDialogCopiar -> fecharDialogCopiar()
            is HorariosAction.CopiarParaDias -> copiarParaDias(action.diasDestino)
            is HorariosAction.LimparHorariosIdeais -> limparHorariosIdeais()
            is HorariosAction.LimparErro -> limparErro()
        }
    }

    private fun validarArgumentosECarregar() {
        when {
            versaoJornadaId <= 0L -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Versão de jornada inválida"
                    )
                }
            }
            empregoId <= 0L -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Emprego inválido"
                    )
                }
            }
            else -> {
                carregarDadosIniciais()
            }
        }
    }

    private fun carregarDadosIniciais() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    versaoJornadaId = versaoJornadaId,
                    empregoId = empregoId
                )
            }

            try {
                val versao = versaoJornadaRepository.buscarPorId(versaoJornadaId)
                val emprego = empregoRepository.buscarPorId(empregoId)

                if (versao == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Versão não encontrada"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        versaoDescricao = versao.descricao ?: "Versão ${versao.numeroVersao}",
                        versaoJornada = versao,
                        empregoApelido = emprego?.apelido ?: emprego?.nome,
                        empregoLogo = emprego?.logo
                    )
                }

                observarHorarios()
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar dados iniciais")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao carregar dados"
                    )
                }
            }
        }
    }

    private fun observarHorarios() {
        viewModelScope.launch {
            horarioDiaSemanaRepository.observarPorVersaoJornada(versaoJornadaId)
                .catch { e ->
                    Timber.e(e, "Erro ao observar horários da versão %d", versaoJornadaId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Erro ao carregar horários"
                        )
                    }
                }
                .collect { horarios ->
                    if (horarios.isEmpty()) {
                        criarHorariosPadrao()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                horarios = horarios.sortedBy { h -> h.diaSemana.ordinal }
                            )
                        }
                    }
                }
        }
    }

    private suspend fun criarHorariosPadrao() {
        try {
            val horariosPadrao = HorarioDiaSemana.criarTodosPadrao(
                empregoId = empregoId,
                versaoJornadaId = versaoJornadaId
            )
            horarioDiaSemanaRepository.inserirTodos(horariosPadrao)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao criar horários padrão")
            _eventos.emit(HorariosEvent.MostrarMensagem("Erro ao criar horários: ${e.message}"))
        }
    }

    private fun selecionarDia(horario: HorarioDiaSemana) {
        _uiState.update {
            it.copy(
                horarioEmEdicao = horario,
                mostrarDialogEdicao = true
            )
        }
    }

    private fun toggleAtivo(diaSemana: DiaSemana) {
        val horario = _uiState.value.horarios.find { it.diaSemana == diaSemana } ?: return

        viewModelScope.launch {
            try {
                val horarioAtualizado = horario.copy(
                    ativo = !horario.ativo,
                    atualizadoEm = LocalDateTime.now()
                )
                horarioDiaSemanaRepository.atualizar(horarioAtualizado)
            } catch (e: Exception) {
                Timber.e(e, "Erro ao alternar estado do dia %s", diaSemana)
                _eventos.emit(HorariosEvent.MostrarMensagem("Erro: ${e.message}"))
            }
        }
    }

    private fun alterarCargaHoraria(minutos: Int) {
        _uiState.update { state ->
            state.copy(
                horarioEmEdicao = state.horarioEmEdicao?.copy(
                    cargaHorariaMinutos = minutos.coerceIn(0, 1440)
                )
            )
        }
    }

    private fun alterarIntervaloMinimo(minutos: Int) {
        _uiState.update { state ->
            state.copy(
                horarioEmEdicao = state.horarioEmEdicao?.copy(
                    intervaloMinimoMinutos = minutos.coerceIn(0, 240)
                )
            )
        }
    }

    private fun alterarToleranciaIntervalo(minutos: Int) {
        _uiState.update { state ->
            state.copy(
                horarioEmEdicao = state.horarioEmEdicao?.copy(
                    toleranciaIntervaloMaisMinutos = minutos.coerceIn(0, 60)
                )
            )
        }
    }

    private fun abrirTimePicker(campo: CampoHorario) {
        _uiState.update {
            it.copy(
                mostrarTimePicker = true,
                campoTimePicker = campo
            )
        }
    }

    private fun selecionarHorario(horario: LocalTime?) {
        val campo = _uiState.value.campoTimePicker ?: return

        _uiState.update { state ->
            val horarioAtualizado = when (campo) {
                CampoHorario.ENTRADA -> state.horarioEmEdicao?.copy(entradaIdeal = horario)
                CampoHorario.SAIDA_INTERVALO -> state.horarioEmEdicao?.copy(saidaIntervaloIdeal = horario)
                CampoHorario.VOLTA_INTERVALO -> state.horarioEmEdicao?.copy(voltaIntervaloIdeal = horario)
                CampoHorario.SAIDA -> state.horarioEmEdicao?.copy(saidaIdeal = horario)
            }
            state.copy(
                horarioEmEdicao = horarioAtualizado,
                mostrarTimePicker = false,
                campoTimePicker = null
            )
        }
    }

    private fun fecharTimePicker() {
        _uiState.update {
            it.copy(
                mostrarTimePicker = false,
                campoTimePicker = null
            )
        }
    }

    private fun fecharDialogEdicao() {
        _uiState.update {
            it.copy(
                mostrarDialogEdicao = false,
                horarioEmEdicao = null
            )
        }
    }

    private fun salvarHorario() {
        val horario = _uiState.value.horarioEmEdicao ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val horarioAtualizado = horario.copy(atualizadoEm = LocalDateTime.now())
                horarioDiaSemanaRepository.atualizar(horarioAtualizado)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        mostrarDialogEdicao = false,
                        horarioEmEdicao = null
                    )
                }
                _eventos.emit(
                    HorariosEvent.MostrarMensagem("${horario.diaSemana.descricao} atualizado")
                )
            } catch (e: Exception) {
                Timber.e(e, "Erro ao salvar horário")
                _uiState.update { it.copy(isSaving = false) }
                _eventos.emit(HorariosEvent.MostrarMensagem("Erro ao salvar: ${e.message}"))
            }
        }
    }

    private fun abrirDialogCopiar(diaSemana: DiaSemana) {
        _uiState.update {
            it.copy(
                mostrarDialogCopiar = true,
                diaSelecionadoParaCopiar = diaSemana
            )
        }
    }

    private fun fecharDialogCopiar() {
        _uiState.update {
            it.copy(
                mostrarDialogCopiar = false,
                diaSelecionadoParaCopiar = null
            )
        }
    }

    private fun copiarParaDias(diasDestino: List<DiaSemana>) {
        val diaOrigem = _uiState.value.diaSelecionadoParaCopiar ?: return
        val horarioOrigem = _uiState.value.horarios.find { it.diaSemana == diaOrigem } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val horariosParaAtualizar = _uiState.value.horarios
                    .filter { it.diaSemana in diasDestino }
                    .map { destino ->
                        destino.copy(
                            ativo = horarioOrigem.ativo,
                            cargaHorariaMinutos = horarioOrigem.cargaHorariaMinutos,
                            entradaIdeal = horarioOrigem.entradaIdeal,
                            saidaIntervaloIdeal = horarioOrigem.saidaIntervaloIdeal,
                            voltaIntervaloIdeal = horarioOrigem.voltaIntervaloIdeal,
                            saidaIdeal = horarioOrigem.saidaIdeal,
                            intervaloMinimoMinutos = horarioOrigem.intervaloMinimoMinutos,
                            toleranciaIntervaloMaisMinutos = horarioOrigem.toleranciaIntervaloMaisMinutos,
                            atualizadoEm = LocalDateTime.now()
                        )
                    }

                horariosParaAtualizar.forEach { horarioDiaSemanaRepository.atualizar(it) }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        mostrarDialogCopiar = false,
                        diaSelecionadoParaCopiar = null
                    )
                }
                _eventos.emit(
                    HorariosEvent.MostrarMensagem(
                        "Configurações copiadas para ${diasDestino.size} dia(s)"
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Erro ao copiar horários")
                _uiState.update { it.copy(isSaving = false) }
                _eventos.emit(HorariosEvent.MostrarMensagem("Erro ao copiar: ${e.message}"))
            }
        }
    }

    private fun limparHorariosIdeais() {
        val horario = _uiState.value.horarioEmEdicao ?: return

        _uiState.update { state ->
            state.copy(
                horarioEmEdicao = horario.copy(
                    entradaIdeal = null,
                    saidaIntervaloIdeal = null,
                    voltaIntervaloIdeal = null,
                    saidaIdeal = null
                )
            )
        }
    }

    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
