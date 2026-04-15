// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/ausencias/AusenciaFormViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.AtualizarAusenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.CriarAusenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ResultadoAtualizarAusencia
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ResultadoCriarAusencia
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
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
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel do formulário de ausência.
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 5.5.0 - Removido SubTipoFolga
 */
@HiltViewModel
class AusenciaFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ausenciaRepository: AusenciaRepository,
    private val criarAusenciaUseCase: CriarAusenciaUseCase,
    private val atualizarAusenciaUseCase: AtualizarAusenciaUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val photoCaptureManager: br.com.tlmacedo.meuponto.util.foto.PhotoCaptureManager
) : ViewModel() {

    private val ausenciaId: Long = savedStateHandle.get<Long>("ausenciaId") ?: 0L
    private val tipoInicial: String? = savedStateHandle.get<String>("tipo")
    private val dataInicial: String? = savedStateHandle.get<String>("data")

    private val _uiState = MutableStateFlow(AusenciaFormUiState())
    val uiState: StateFlow<AusenciaFormUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AusenciaFormUiEvent>()
    val uiEvent: SharedFlow<AusenciaFormUiEvent> = _uiEvent.asSharedFlow()

    init {
        inicializar()
    }

    private fun inicializar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val (empregoId, dataInicioTrabalho) = when (val resultado = obterEmpregoAtivoUseCase()) {
                is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> {
                    resultado.emprego.id to resultado.emprego.dataInicioTrabalho
                }
                else -> 0L to null
            }

            if (ausenciaId > 0) {
                carregarAusencia(ausenciaId, dataInicioTrabalho)
            } else {
                val tipo = tipoInicial?.let {
                    runCatching { TipoAusencia.valueOf(it) }.getOrNull()
                } ?: TipoAusencia.FERIAS

                val data = dataInicial?.let {
                    runCatching { LocalDate.parse(it) }.getOrNull()
                } ?: LocalDate.now()

                val periodoAquisitivo = calcularPeriodoAquisitivo(data, dataInicioTrabalho)

                _uiState.update {
                    it.copy(
                        empregoId = empregoId,
                        dataInicioTrabalho = dataInicioTrabalho,
                        tipo = tipo,
                        dataInicio = data,
                        dataFim = data,
                        dataInicioPeriodoAquisitivo = periodoAquisitivo?.first,
                        dataFimPeriodoAquisitivo = periodoAquisitivo?.second,
                        isEdicao = false,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calcularPeriodoAquisitivo(
        dataReferencia: LocalDate,
        dataInicioTrabalho: LocalDate?
    ): Pair<LocalDate, LocalDate>? {
        var inicio = dataInicioTrabalho ?: return null
        var fim = inicio.plusYears(1).minusDays(1)

        while (dataReferencia.isAfter(fim)) {
            inicio = inicio.plusYears(1)
            fim = fim.plusYears(1)
        }

        return Pair(inicio, fim)
    }

    private suspend fun carregarAusencia(id: Long, dataInicioTrabalho: LocalDate?) {
        val ausencia = ausenciaRepository.buscarPorId(id)

        if (ausencia != null) {
            _uiState.update { 
                AusenciaFormUiState.fromAusencia(ausencia).copy(
                    isLoading = false,
                    dataInicioTrabalho = dataInicioTrabalho
                ) 
            }
        } else {
            _uiState.update { it.copy(isLoading = false, erro = "Ausência não encontrada") }
        }
    }

    fun onAction(action: AusenciaFormAction) {
        when (action) {
            // ================================================================
            // TIPO DE AUSÊNCIA
            // ================================================================
            is AusenciaFormAction.SelecionarTipo -> selecionarTipo(action.tipo)
            is AusenciaFormAction.AbrirTipoSelector -> {
                _uiState.update { it.copy(showTipoSelector = true) }
            }
            is AusenciaFormAction.FecharTipoSelector -> {
                _uiState.update { it.copy(showTipoSelector = false) }
            }
            is AusenciaFormAction.SelecionarTipoFolga -> {
                _uiState.update { it.copy(tipoFolga = action.tipoFolga) }
            }

            // ================================================================
            // PERÍODO
            // ================================================================
            is AusenciaFormAction.SelecionarModoPeriodo -> {
                _uiState.update { it.copy(modoPeriodo = action.modo) }
            }
            is AusenciaFormAction.SelecionarDataInicio -> selecionarDataInicio(action.data)
            is AusenciaFormAction.SelecionarDataFim -> selecionarDataFim(action.data)
            is AusenciaFormAction.AtualizarQuantidadeDias -> {
                val dias = action.dias.coerceIn(1, 365)
                _uiState.update { it.copy(quantidadeDias = dias) }
            }
            is AusenciaFormAction.AbrirDatePickerInicio -> {
                _uiState.update { it.copy(showDatePickerInicio = true) }
            }
            is AusenciaFormAction.FecharDatePickerInicio -> {
                _uiState.update { it.copy(showDatePickerInicio = false) }
            }
            is AusenciaFormAction.AbrirDatePickerFim -> {
                _uiState.update { it.copy(showDatePickerFim = true) }
            }
            is AusenciaFormAction.FecharDatePickerFim -> {
                _uiState.update { it.copy(showDatePickerFim = false) }
            }

            // ================================================================
            // HORÁRIOS (DECLARAÇÃO)
            // ================================================================
            is AusenciaFormAction.SelecionarHoraInicio -> {
                _uiState.update { it.copy(horaInicio = action.hora, showTimePickerInicio = false) }
            }
            is AusenciaFormAction.AtualizarDuracaoDeclaracao -> {
                _uiState.update { state ->
                    val novoState = state.copy(
                        duracaoDeclaracaoHoras = action.horas.coerceIn(0, 12),
                        duracaoDeclaracaoMinutos = action.minutos.coerceIn(0, 59),
                        showDuracaoDeclaracaoPicker = false
                    )
                    // Ajustar abono se necessário
                    if (novoState.duracaoAbonoTotalMinutos > novoState.duracaoDeclaracaoTotalMinutos) {
                        novoState.copy(
                            duracaoAbonoHoras = action.horas,
                            duracaoAbonoMinutos = action.minutos
                        )
                    } else {
                        novoState
                    }
                }
            }
            is AusenciaFormAction.AtualizarDuracaoAbono -> {
                _uiState.update { state ->
                    val novasHoras = action.horas.coerceIn(0, 12)
                    val novosMinutos = action.minutos.coerceIn(0, 59)
                    val totalAbono = novasHoras * 60 + novosMinutos
                    val totalDeclaracao = state.duracaoDeclaracaoTotalMinutos

                    // Não permitir abono maior que declaração
                    if (totalAbono <= totalDeclaracao) {
                        state.copy(
                            duracaoAbonoHoras = novasHoras,
                            duracaoAbonoMinutos = novosMinutos,
                            showDuracaoAbonoPicker = false
                        )
                    } else {
                        state.copy(
                            erro = "O tempo de abono não pode ser maior que a duração da declaração",
                            showDuracaoAbonoPicker = false
                        )
                    }
                }
            }
            is AusenciaFormAction.AbrirTimePickerInicio -> {
                _uiState.update { it.copy(showTimePickerInicio = true) }
            }
            is AusenciaFormAction.FecharTimePickerInicio -> {
                _uiState.update { it.copy(showTimePickerInicio = false) }
            }
            is AusenciaFormAction.AbrirDuracaoDeclaracaoPicker -> {
                _uiState.update { it.copy(showDuracaoDeclaracaoPicker = true) }
            }
            is AusenciaFormAction.FecharDuracaoDeclaracaoPicker -> {
                _uiState.update { it.copy(showDuracaoDeclaracaoPicker = false) }
            }
            is AusenciaFormAction.AbrirDuracaoAbonoPicker -> {
                _uiState.update { it.copy(showDuracaoAbonoPicker = true) }
            }
            is AusenciaFormAction.FecharDuracaoAbonoPicker -> {
                _uiState.update { it.copy(showDuracaoAbonoPicker = false) }
            }

            // ================================================================
            // PERÍODO AQUISITIVO (FERIAS)
            // ================================================================
            is AusenciaFormAction.SelecionarAnoInicioPeriodoAquisitivo -> {
                _uiState.update { state ->
                    val dataInicioTrabalho = state.dataInicioTrabalho ?: return@update state
                    val novaData = state.dataInicioPeriodoAquisitivo?.withYear(action.ano) 
                        ?: dataInicioTrabalho.withYear(action.ano)
                    state.copy(dataInicioPeriodoAquisitivo = novaData)
                }
            }
            is AusenciaFormAction.SelecionarAnoFimPeriodoAquisitivo -> {
                _uiState.update { state ->
                    val dataInicioTrabalho = state.dataInicioTrabalho ?: return@update state
                    val novaData = state.dataFimPeriodoAquisitivo?.withYear(action.ano) 
                        ?: dataInicioTrabalho.plusYears(1).minusDays(1).withYear(action.ano)
                    state.copy(dataFimPeriodoAquisitivo = novaData)
                }
            }
            is AusenciaFormAction.SelecionarDataInicioPeriodoAquisitivo -> {
                _uiState.update { it.copy(dataInicioPeriodoAquisitivo = action.data, showDatePickerInicioPeriodoAquisitivo = false) }
            }
            is AusenciaFormAction.SelecionarDataFimPeriodoAquisitivo -> {
                _uiState.update { it.copy(dataFimPeriodoAquisitivo = action.data, showDatePickerFimPeriodoAquisitivo = false) }
            }
            is AusenciaFormAction.AbrirDatePickerInicioPeriodoAquisitivo -> {
                _uiState.update { it.copy(showDatePickerInicioPeriodoAquisitivo = true) }
            }
            is AusenciaFormAction.FecharDatePickerInicioPeriodoAquisitivo -> {
                _uiState.update { it.copy(showDatePickerInicioPeriodoAquisitivo = false) }
            }
            is AusenciaFormAction.AbrirDatePickerFimPeriodoAquisitivo -> {
                _uiState.update { it.copy(showDatePickerFimPeriodoAquisitivo = true) }
            }
            is AusenciaFormAction.FecharDatePickerFimPeriodoAquisitivo -> {
                _uiState.update { it.copy(showDatePickerFimPeriodoAquisitivo = false) }
            }

            // ================================================================
            // TEXTOS
            // ================================================================
            is AusenciaFormAction.AtualizarDescricao -> {
                _uiState.update { it.copy(descricao = action.descricao) }
            }
            is AusenciaFormAction.AtualizarObservacao -> {
                _uiState.update { it.copy(observacao = action.observacao) }
            }
            is AusenciaFormAction.AtualizarPeriodoAquisitivo -> {
                _uiState.update { it.copy(periodoAquisitivo = action.periodo) }
            }

            // ================================================================
            // ANEXO DE IMAGEM
            // ================================================================
            is AusenciaFormAction.SelecionarImagem -> {
                _uiState.update {
                    it.copy(
                        imagemUri = action.uri,
                        imagemNome = action.nome,
                        showImagePicker = false
                    )
                }
            }
            is AusenciaFormAction.RemoverImagem -> {
                _uiState.update { it.copy(imagemUri = null, imagemNome = null) }
            }
            is AusenciaFormAction.AbrirImagePicker -> {
                _uiState.update { it.copy(showImagePicker = true) }
            }
            is AusenciaFormAction.FecharImagePicker -> {
                _uiState.update { it.copy(showImagePicker = false) }
            }
            is AusenciaFormAction.AbrirCamera -> {
                viewModelScope.launch {
                    val uri = photoCaptureManager.prepareForCameraCapture()
                    _uiState.update { it.copy(showImagePicker = false) }
                    if (uri != null) {
                        _uiEvent.emit(AusenciaFormUiEvent.AbrirCamera(uri))
                    } else {
                        _uiState.update { it.copy(erro = "Erro ao preparar a câmera") }
                    }
                }
            }
            is AusenciaFormAction.OnCameraCaptureResult -> {
                if (action.success) {
                    photoCaptureManager.onCameraCaptureSuccess()
                    val uri = photoCaptureManager.getCapturedPhotoUri()
                    _uiState.update { it.copy(imagemUri = uri?.toString()) }
                } else {
                    photoCaptureManager.onCameraCaptureCancelled()
                }
            }
            is AusenciaFormAction.AbrirGaleria -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(showImagePicker = false) }
                    _uiEvent.emit(AusenciaFormUiEvent.AbrirGaleria)
                }
            }

            // ================================================================
            // AÇÕES PRINCIPAIS
            // ================================================================
            is AusenciaFormAction.Salvar -> salvar()
            is AusenciaFormAction.Cancelar -> {
                viewModelScope.launch {
                    _uiEvent.emit(AusenciaFormUiEvent.Voltar)
                }
            }
            is AusenciaFormAction.LimparErro -> {
                _uiState.update { it.copy(erro = null) }
            }
        }
    }

    private fun selecionarTipo(tipo: TipoAusencia) {
        _uiState.update { state ->
            state.copy(
                tipo = tipo,
                descricao = if (state.descricao.isBlank()) tipo.descricao else state.descricao,
                showTipoSelector = false,
                // CORREÇÃO: manter o tipoFolga do state
                tipoFolga = state.tipoFolga,
                horaInicio = if (tipo == TipoAusencia.DECLARACAO) LocalTime.of(8, 0) else state.horaInicio,
                periodoAquisitivo = if (tipo == TipoAusencia.FERIAS) state.periodoAquisitivo else "",
                imagemUri = if (tipo.permiteAnexo) state.imagemUri else null
            )
        }
    }

    private fun selecionarDataInicio(data: LocalDate) {
        viewModelScope.launch {
            val dataInicioTrabalho = when (val resultado = obterEmpregoAtivoUseCase()) {
                is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> resultado.emprego.dataInicioTrabalho
                else -> null
            }

            _uiState.update { state ->
                val novaDataFim = if (state.dataFim < data) data else state.dataFim
                val periodoAquisitivo = if (state.tipo == TipoAusencia.FERIAS) {
                    calcularPeriodoAquisitivo(data, dataInicioTrabalho)
                } else null

                state.copy(
                    dataInicio = data,
                    dataFim = novaDataFim,
                    dataInicioPeriodoAquisitivo = periodoAquisitivo?.first ?: state.dataInicioPeriodoAquisitivo,
                    dataFimPeriodoAquisitivo = periodoAquisitivo?.second ?: state.dataFimPeriodoAquisitivo,
                    showDatePickerInicio = false
                )
            }
        }
    }

    private fun selecionarDataFim(data: LocalDate) {
        _uiState.update { state ->
            val dataFimValida = if (data < state.dataInicio) state.dataInicio else data
            state.copy(
                dataFim = dataFimValida,
                showDatePickerFim = false
            )
        }
    }

    private suspend fun validarRegrasFerias(state: AusenciaFormUiState): String? {
        val inicioAquisitivo = state.dataInicioPeriodoAquisitivo ?: return "Período aquisitivo não informado"
        val fimAquisitivo = state.dataFimPeriodoAquisitivo ?: return "Período aquisitivo não informado"

        // 1. Buscar todas as férias já registradas para este período aquisitivo
        val feriasExistentes = ausenciaRepository.buscarFeriasPorPeriodoAquisitivo(
            state.empregoId, inicioAquisitivo, fimAquisitivo
        ).filter { it.id != state.id }

        val diasNovos = state.totalDias
        val diasExistentes = feriasExistentes.sumOf { it.quantidadeDias }
        val totalDias = diasExistentes + diasNovos

        // Regra: Máximo 30 dias por período aquisitivo
        if (totalDias > 30) {
            return "O total de dias de férias para este período ($totalDias) excede o limite legal de 30 dias. Já existem $diasExistentes dias registrados."
        }

        // Regra: Máximo 3 splits (períodos)
        val totalPeriodos = feriasExistentes.size + 1
        if (totalPeriodos > 3) {
            return "As férias só podem ser divididas em no máximo 3 períodos. Este seria o ${totalPeriodos}º."
        }

        // Regra: Um dos períodos deve ter pelo menos 15 dias
        val todosPeriodos = feriasExistentes.map { it.quantidadeDias } + diasNovos
        if (totalDias == 30 && todosPeriodos.none { it >= 15 }) {
            return "Pelo menos um dos períodos de férias deve ter no mínimo 15 dias corridos."
        }

        // Regra: Períodos menores não podem ter menos de 5 dias
        if (todosPeriodos.any { it < 5 }) {
            return "Nenhum período de férias pode ser inferior a 5 dias corridos."
        }

        return null
    }

    private fun salvar() {
        val state = _uiState.value

        // Validação básica (UI)
        state.mensagemValidacao?.let { mensagem ->
            _uiState.update { it.copy(erro = mensagem) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSalvando = true, erro = null) }

            // Validação de regras de negócio para Férias
            if (state.tipo == TipoAusencia.FERIAS) {
                val erroNegocio = validarRegrasFerias(state)
                if (erroNegocio != null) {
                    _uiState.update { it.copy(erro = erroNegocio, isSalvando = false) }
                    return@launch
                }
            }

            val ausencia = state.toAusencia()

            val resultado = if (state.isEdicao) {
                atualizarAusenciaUseCase(ausencia)
            } else {
                criarAusenciaUseCase(ausencia)
            }

            when (resultado) {
                is ResultadoCriarAusencia.Sucesso -> {
                    _uiEvent.emit(AusenciaFormUiEvent.MostrarMensagem("Ausência salva com sucesso"))
                    _uiEvent.emit(AusenciaFormUiEvent.SalvoComSucesso)
                }
                is ResultadoCriarAusencia.Erro -> {
                    _uiState.update { it.copy(erro = resultado.mensagem, isSalvando = false) }
                }
                is ResultadoAtualizarAusencia.Sucesso -> {
                    _uiEvent.emit(AusenciaFormUiEvent.MostrarMensagem("Ausência atualizada"))
                    _uiEvent.emit(AusenciaFormUiEvent.SalvoComSucesso)
                }
                is ResultadoAtualizarAusencia.Erro -> {
                    _uiState.update { it.copy(erro = resultado.mensagem, isSalvando = false) }
                }
            }
        }
    }
}
