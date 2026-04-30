package br.com.tlmacedo.meuponto.presentation.screen.ausencias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.AtualizarAusenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.CalcularMetadataFeriasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.CriarAusenciaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ResultadoAtualizarAusencia
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ResultadoCriarAusencia
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.feriado.VerificarDiaEspecialUseCase
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
 * @updated 5.8.0 - Lógica de ciclos e saldos de férias aprimorada
 */
@HiltViewModel
class AusenciaFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val ausenciaRepository: AusenciaRepository,
    private val criarAusenciaUseCase: CriarAusenciaUseCase,
    private val atualizarAusenciaUseCase: AtualizarAusenciaUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val calcularMetadataFeriasUseCase: CalcularMetadataFeriasUseCase,
    private val photoCaptureManager: br.com.tlmacedo.meuponto.util.foto.PhotoCaptureManager,
    val verificarDiaEspecialUseCase: VerificarDiaEspecialUseCase
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

            val resultadoEmprego = obterEmpregoAtivoUseCase()
            if (resultadoEmprego is ObterEmpregoAtivoUseCase.Resultado.Sucesso) {
                val emp = resultadoEmprego.emprego
                val dataInicioTrabalho = emp.dataInicioTrabalho
                val ciclos = gerarCiclosDisponiveis(dataInicioTrabalho)

                if (ausenciaId > 0) {
                    carregarAusencia(ausenciaId, dataInicioTrabalho, ciclos)
                } else {
                    val tipo = tipoInicial?.let {
                        runCatching { TipoAusencia.valueOf(it) }.getOrNull()
                    } ?: TipoAusencia.Ferias

                    val data = dataInicial?.let {
                        runCatching { LocalDate.parse(it) }.getOrNull()
                    } ?: LocalDate.now()

                    val (paInicio, paFim) = if (tipo == TipoAusencia.Ferias) {
                        val sugerido = sugerirCicloComSaldo(dataInicioTrabalho, emp.id)
                        sugerido?.first to sugerido?.second
                    } else {
                        null to null
                    }

                    _uiState.update {
                        it.copy(
                            empregoId = emp.id,
                            dataInicioTrabalho = dataInicioTrabalho,
                            ciclosDisponiveisPA = ciclos,
                            empregoApelido = emp.apelido,
                            empregoLogo = emp.logo,
                            tipo = tipo,
                            dataInicio = data,
                            dataFim = data,
                            dataInicioPeriodoAquisitivo = paInicio,
                            dataFimPeriodoAquisitivo = paFim,
                            isEdicao = false,
                            isLoading = false
                        )
                    }
                    if (tipo == TipoAusencia.Ferias) {
                        atualizarMetadataFerias(data)
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        erro = "Nenhum emprego ativo encontrado"
                    )
                }
            }
        }
    }

    private fun gerarCiclosDisponiveis(dataInicioTrabalho: LocalDate?): List<String> {
        val inicioJob = dataInicioTrabalho ?: return emptyList()
        val hoje = LocalDate.now()
        val diaMesInicio = java.time.MonthDay.from(inicioJob)

        val anoInicioTrabalho = inicioJob.year
        return (anoInicioTrabalho..hoje.year.minus(1)).map { ano ->
            val inicioPA = diaMesInicio.atYear(ano)
            val fimPA = inicioPA.plusYears(1).minusDays(1)
            val fimConcessivo = fimPA.plusYears(1)

            val status = when {
                hoje.isAfter(fimConcessivo) -> "utilizado"
                hoje.isAfter(fimPA) -> "concessivo"
                else -> "aquisitivo"
            }

            "$ano/${ano + 1} $status"
        }
    }

    private suspend fun sugerirCicloComSaldo(
        dataInicioTrabalho: LocalDate?,
        empregoId: Long
    ): Pair<LocalDate, LocalDate>? {
        val inicioJob = dataInicioTrabalho ?: return null
        val hoje = LocalDate.now()
        val diaMesInicio = java.time.MonthDay.from(inicioJob)

        val anoInicioTrabalho = inicioJob.year
        for (ano in anoInicioTrabalho..hoje.year) {
            val inicioPA = diaMesInicio.atYear(ano)
            val fimPA = inicioPA.plusYears(1).minusDays(1)

            val feriasExistentes = ausenciaRepository.buscarFeriasPorPeriodoAquisitivo(
                empregoId, inicioPA, fimPA
            )
            val diasMarcados = feriasExistentes.sumOf { it.quantidadeDias }

            if (diasMarcados < 30) {
                return inicioPA to fimPA
            }
        }

        val inicioUltimo = diaMesInicio.atYear(hoje.year)
        return inicioUltimo to inicioUltimo.plusYears(1).minusDays(1)
    }

    private fun carregarAusencia(id: Long, dataInicioTrabalho: LocalDate?, ciclos: List<String>) {
        viewModelScope.launch {
            val ausencia = ausenciaRepository.buscarPorId(id)

            if (ausencia != null) {
                _uiState.update {
                    AusenciaFormUiState.fromAusencia(ausencia).copy(
                        isLoading = false,
                        dataInicioTrabalho = dataInicioTrabalho,
                        ciclosDisponiveisPA = ciclos
                    )
                }
                if (ausencia.tipo == TipoAusencia.Ferias) {
                    atualizarMetadataFerias(ausencia.dataInicio)
                }
            } else {
                _uiState.update { it.copy(isLoading = false, erro = "Ausência não encontrada") }
            }
        }
    }

    fun onAction(action: AusenciaFormAction) {
        when (action) {
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

            is AusenciaFormAction.SelecionarModoPeriodo -> {
                _uiState.update { it.copy(modoPeriodo = action.modo) }
            }

            is AusenciaFormAction.SelecionarDataInicio -> selecionarDataInicio(action.data)
            is AusenciaFormAction.SelecionarDataFim -> selecionarDataFim(action.data)
            is AusenciaFormAction.AtualizarQuantidadeDias -> {
                val dias = action.dias.coerceIn(1, 365)
                _uiState.update { it.copy(quantidadeDias = dias) }
                if (_uiState.value.tipo == TipoAusencia.Ferias) {
                    atualizarMetadataFerias()
                }
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

            is AusenciaFormAction.SelecionarCicloPeriodoAquisitivo -> {
                _uiState.update { state ->
                    val dataInicioTrabalho = state.dataInicioTrabalho ?: return@update state
                    val anoInicio = action.ciclo.substring(0, 4).toInt()

                    val novaDataInicio = dataInicioTrabalho.withYear(anoInicio)
                    val novaDataFim = novaDataInicio.plusYears(1).minusDays(1)

                    state.copy(
                        dataInicioPeriodoAquisitivo = novaDataInicio,
                        dataFimPeriodoAquisitivo = novaDataFim
                    )
                }
                atualizarMetadataFerias(_uiState.value.dataInicio)
            }

            is AusenciaFormAction.SelecionarDataInicioPeriodoAquisitivo -> {
                _uiState.update {
                    it.copy(
                        dataInicioPeriodoAquisitivo = action.data,
                        showDatePickerInicioPeriodoAquisitivo = false
                    )
                }
                atualizarMetadataFerias(_uiState.value.dataInicio)
            }

            is AusenciaFormAction.SelecionarDataFimPeriodoAquisitivo -> {
                _uiState.update {
                    it.copy(
                        dataFimPeriodoAquisitivo = action.data,
                        showDatePickerFimPeriodoAquisitivo = false
                    )
                }
                atualizarMetadataFerias(_uiState.value.dataInicio)
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

            is AusenciaFormAction.AtualizarDescricao -> {
                _uiState.update { it.copy(descricao = action.descricao) }
            }

            is AusenciaFormAction.AtualizarObservacao -> {
                _uiState.update { it.copy(observacao = action.observacao) }
            }

            is AusenciaFormAction.AtualizarPeriodoAquisitivo -> {
                _uiState.update { it.copy(periodoAquisitivo = action.periodo) }
            }

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
                tipoFolga = state.tipoFolga,
                horaInicio = if (tipo == TipoAusencia.Declaracao) LocalTime.of(
                    8,
                    0
                ) else state.horaInicio,
                periodoAquisitivo = if (tipo == TipoAusencia.Ferias) state.periodoAquisitivo else "",
                imagemUri = if (tipo.permiteAnexo) state.imagemUri else null
            )
        }
        if (tipo == TipoAusencia.Ferias) {
            atualizarMetadataFerias(_uiState.value.dataInicio)
        } else {
            _uiState.update { it.copy(metadataFerias = null) }
        }
    }

    private fun selecionarDataInicio(data: LocalDate) {
        viewModelScope.launch {
            _uiState.update { state ->
                val novaDataFim = if (state.dataFim < data) data else state.dataFim
                state.copy(
                    dataInicio = data,
                    dataFim = novaDataFim,
                    showDatePickerInicio = false
                )
            }
            if (_uiState.value.tipo == TipoAusencia.Ferias) {
                atualizarMetadataFerias(_uiState.value.dataInicio)
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
        if (_uiState.value.tipo == TipoAusencia.Ferias) {
            atualizarMetadataFerias(_uiState.value.dataInicio)
        }
    }

    private fun atualizarMetadataFerias(referencia: LocalDate? = null) {
        viewModelScope.launch {
            val state = _uiState.value
            val metadata = calcularMetadataFeriasUseCase(state.toAusencia(), referencia)
            _uiState.update { it.copy(metadataFerias = metadata) }
        }
    }

    private suspend fun validarRegrasFerias(state: AusenciaFormUiState): String? {
        val inicioAquisitivo =
            state.dataInicioPeriodoAquisitivo ?: return "Período aquisitivo não informado"
        val fimAquisitivo =
            state.dataFimPeriodoAquisitivo ?: return "Período aquisitivo não informado"

        val feriasExistentes = ausenciaRepository.buscarFeriasPorPeriodoAquisitivo(
            state.empregoId, inicioAquisitivo, fimAquisitivo
        ).filter { it.id != state.id }

        val diasNovos = state.totalDias

        if (diasNovos < 5) {
            return "Nenhum período de férias pode ser inferior a 5 dias corridos."
        }

        val diasExistentes = feriasExistentes.sumOf { it.quantidadeDias }
        val totalDias = diasExistentes + diasNovos

        if (totalDias > 30) {
            return "O total de dias de férias para este período ($totalDias) excede o limite legal de 30 dias. Já existem $diasExistentes dias registrados."
        }

        val totalPeriodos = feriasExistentes.size + 1
        if (totalPeriodos > 3) {
            return "As férias só podem ser divididas em no máximo 3 períodos. Este seria o ${totalPeriodos}º."
        }

        val todosPeriodos = feriasExistentes.map { it.quantidadeDias } + diasNovos
        if (totalDias == 30 && todosPeriodos.none { it >= 14 }) {
            return "Pelo menos um dos períodos de férias deve ter no mínimo 14 dias corridos."
        }

        return null
    }

    private fun salvar() {
        val state = _uiState.value

        state.mensagemValidacao?.let { mensagem ->
            _uiState.update { it.copy(erro = mensagem) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSalvando = true, erro = null) }

            if (state.tipo == TipoAusencia.Ferias) {
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
