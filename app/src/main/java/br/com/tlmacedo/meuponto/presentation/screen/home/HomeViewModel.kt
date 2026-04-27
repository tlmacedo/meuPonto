// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.data.local.database.dao.FotoComprovanteDao
import br.com.tlmacedo.meuponto.data.local.database.entity.FotoComprovanteEntity
import br.com.tlmacedo.meuponto.data.service.LocationService
import br.com.tlmacedo.meuponto.data.service.OcrService
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.MotivoEdicao
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.Usuario
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.model.toTipoJornadaDia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.repository.AuthRepository
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FechamentoPeriodoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.BuscarAusenciaPorDataUseCase
import br.com.tlmacedo.meuponto.domain.usecase.banco.FecharCicloUseCase
import br.com.tlmacedo.meuponto.domain.usecase.banco.InicializarCiclosRetroativosUseCase
import br.com.tlmacedo.meuponto.domain.usecase.banco.ReverterFechamentoIncorretoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.banco.VerificarCicloPendenteUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ListarEmpregosUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.TrocarEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.feriado.VerificarDiaEspecialUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularResumoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.DeterminarProximoTipoPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ExcluirPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterPontosDoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.RegistrarPontoUseCase
import br.com.tlmacedo.meuponto.util.ComprovanteImageStorage
import br.com.tlmacedo.meuponto.util.foto.ImageHashCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs

/**
 * ViewModel da tela Home.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 6.2.0 - Adicionado suporte a ciclos de banco de horas
 * @updated 6.3.0 - Adicionado suporte a reversão de fechamentos incorretos
 * @updated 7.2.0 - Substituída edição inline por modais
 * @updated 9.0.0 - Adicionado suporte a foto de comprovante
 * @updated 10.0.0 - Corrigido fluxo de registro com foto obrigatória
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val registrarPontoUseCase: RegistrarPontoUseCase,
    private val obterPontosDoDiaUseCase: ObterPontosDoDiaUseCase,
    private val calcularResumoDiaUseCase: CalcularResumoDiaUseCase,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase,
    private val determinarProximoTipoPontoUseCase: DeterminarProximoTipoPontoUseCase,
    private val excluirPontoUseCase: ExcluirPontoUseCase,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val listarEmpregosUseCase: ListarEmpregosUseCase,
    private val trocarEmpregoAtivoUseCase: TrocarEmpregoAtivoUseCase,
    private val buscarAusenciaPorDataUseCase: BuscarAusenciaPorDataUseCase,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val obterResumoDiaCompletoUseCase: ObterResumoDiaCompletoUseCase,
    private val verificarCicloPendenteUseCase: VerificarCicloPendenteUseCase,
    private val fecharCicloUseCase: FecharCicloUseCase,
    private val comprovanteImageStorage: ComprovanteImageStorage,
    private val inicializarCiclosRetroativosUseCase: InicializarCiclosRetroativosUseCase,
    private val reverterFechamentoIncorretoUseCase: ReverterFechamentoIncorretoUseCase,
    private val fechamentoPeriodoRepository: FechamentoPeriodoRepository,
    private val pontoRepository: PontoRepository,
    private val authRepository: AuthRepository,
    private val locationService: LocationService,
    private val ocrService: OcrService,
    private val fotoComprovanteDao: FotoComprovanteDao,
    private val imageHashCalculator: ImageHashCalculator,
    private val syncPontoStatusWithWearUseCase: br.com.tlmacedo.meuponto.domain.usecase.wear.SyncPontoStatusWithWearUseCase,
    val verificarDiaEspecialUseCase: VerificarDiaEspecialUseCase,
    val ausenciaRepository: AusenciaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent: SharedFlow<HomeUiEvent> = _uiEvent.asSharedFlow()

    private var pontosCollectionJob: Job? = null
    private var bancoHorasCollectionJob: Job? = null
    private var versaoJornadaCollectionJob: Job? = null

    private var usuarioLogado: Usuario? = null

    init {
        carregarUsuarioLogado()
        carregarEmpregoAtivo()
        carregarEmpregos()
        carregarPontosDoDia()
        carregarBancoHoras()
        iniciarRelogioAtualizado()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.RecarregarConfiguracaoEmprego -> recarregarConfiguracaoEmprego()
            is HomeAction.RegistrarPontoAgora -> abrirRegistrarPontoModal(
                LocalDateTime.of(
                    _uiState.value.dataSelecionada,
                    LocalTime.now()
                )
            )

            is HomeAction.AbrirTimePickerDialog -> abrirTimePicker()
            is HomeAction.FecharTimePickerDialog -> fecharTimePicker()
            is HomeAction.RegistrarPontoManual -> abrirRegistrarPontoModal(
                LocalDateTime.of(
                    _uiState.value.dataSelecionada,
                    action.hora
                )
            )

            // ══════════════════════════════════════════════════════════════════════
            // MODAIS DE PONTO
            // ══════════════════════════════════════════════════════════════════════
            is HomeAction.AbrirEdicaoModal -> abrirEdicaoModal(action.ponto)
            is HomeAction.FecharEdicaoModal -> fecharEdicaoModal()
            is HomeAction.AtualizarFotoEdicaoModal -> atualizarFotoEdicaoModal(
                action.uri,
                action.origem
            )

            is HomeAction.RemoverFotoEdicaoModal -> removerFotoEdicaoModal()
            is HomeAction.ReprocessarOcrEdicaoModal -> reprocessarOcrEdicaoModal()
            is HomeAction.SalvarEdicaoModal -> salvarEdicaoModal(
                action.pontoId,
                action.hora,
                action.nsr,
                action.motivo,
                action.detalhes,
                action.observacao
            )

            is HomeAction.AbrirExclusaoModal -> abrirExclusaoModal(action.ponto)
            is HomeAction.FecharExclusaoModal -> fecharExclusaoModal()
            is HomeAction.ConfirmarExclusaoModal -> confirmarExclusaoModal(
                action.pontoId,
                action.motivo
            )

            is HomeAction.AbrirLocalizacaoModal -> abrirLocalizacaoModal(action.ponto)
            is HomeAction.FecharLocalizacaoModal -> fecharLocalizacaoModal()
            is HomeAction.AbrirFotoModal -> abrirFotoModal(action.ponto)
            is HomeAction.FecharFotoModal -> fecharFotoModal()
            is HomeAction.SalvarFotoModal -> salvarFotoModal(action.pontoId, action.path)

            // Editar ponto (navegação para tela completa)
            is HomeAction.EditarPonto -> {
                viewModelScope.launch {
                    _uiEvent.emit(HomeUiEvent.NavegarParaEditarPonto(action.pontoId))
                }
            }

            // Navegação por data
            is HomeAction.DiaAnterior -> navegarDiaAnterior()
            is HomeAction.ProximoDia -> navegarProximoDia()
            is HomeAction.IrParaHoje -> irParaHoje()
            is HomeAction.SelecionarData -> selecionarData(action.data)
            is HomeAction.AbrirSeletorEmprego -> abrirSeletorEmprego()
            is HomeAction.FecharSeletorEmprego -> fecharSeletorEmprego()
            is HomeAction.SelecionarEmprego -> selecionarEmprego(action.emprego)
            is HomeAction.NavegarParaHistorico -> navegarParaHistorico()
            is HomeAction.NavegarParaConfiguracoes -> navegarParaConfiguracoes()
            is HomeAction.AtualizarHora -> atualizarHora()
            is HomeAction.LimparErro -> limparErro()
            is HomeAction.RecarregarDados -> recarregarDados()
            is HomeAction.MostrarMensagem -> {
                viewModelScope.launch {
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem(action.mensagem))
                }
            }

            is HomeAction.AbrirDatePicker -> abrirDatePicker()
            is HomeAction.FecharDatePicker -> fecharDatePicker()
            is HomeAction.NavegarParaNovoEmprego -> navegarParaNovoEmprego()
            is HomeAction.NavegarParaEditarEmprego -> navegarParaEditarEmprego()
            is HomeAction.AbrirMenuEmprego -> abrirMenuEmprego()
            is HomeAction.FecharMenuEmprego -> fecharMenuEmprego()
            is HomeAction.AbrirDialogFechamentoCiclo -> abrirDialogFechamentoCiclo()
            is HomeAction.FecharDialogFechamentoCiclo -> fecharDialogFechamentoCiclo()
            is HomeAction.ConfirmarFechamentoCiclo -> confirmarFechamentoCiclo()
            is HomeAction.NavegarParaHistoricoCiclos -> navegarParaHistoricoCiclos()
            is HomeAction.NavegarParaEditarJornada -> navegarParaEditarJornada()

            // NOVO MODAL DE REGISTRO
            is HomeAction.AbrirRegistrarPontoModal -> abrirRegistrarPontoModal(action.dataHora)
            is HomeAction.FecharRegistrarPontoModal -> fecharRegistrarPontoModal()
            is HomeAction.AtualizarNsrRegistroModal -> atualizarNsrRegistroModal(action.nsr)
            is HomeAction.AtualizarFotoRegistroModal -> atualizarFotoRegistroModal(
                action.uri,
                action.origem
            )

            is HomeAction.AtualizarHoraRegistroModal -> atualizarHoraRegistroModal(action.hora)
            is HomeAction.AbrirTimePickerRegistroModal -> abrirTimePickerRegistroModal()
            is HomeAction.FecharTimePickerRegistroModal -> fecharTimePickerRegistroModal()
            is HomeAction.CapturarLocalizacaoRegistroModal -> capturarLocalizacaoRegistroModal()
            is HomeAction.ConfirmarRegistroPontoModal -> confirmarRegistroPontoModal()
            is HomeAction.AtualizarObservacaoRegistroModal -> atualizarObservacaoRegistroModal(
                action.observacao
            )

            is HomeAction.ReprocessarOcrRegistroModal -> {
                val uri = _uiState.value.registrarPontoModal?.fotoUri
                val origem = _uiState.value.registrarPontoModal?.fotoOrigem
                    ?: br.com.tlmacedo.meuponto.domain.model.FotoOrigem.NENHUMA
                if (uri != null) {
                    atualizarFotoRegistroModal(uri, origem)
                }
            }

            // FOTO DE COMPROVANTE
            is HomeAction.AbrirFotoSourceDialog -> abrirFotoSourceDialog()
            is HomeAction.FecharFotoSourceDialog -> fecharFotoSourceDialog()
            is HomeAction.AbrirCameraCapture -> abrirCameraCapture()
            is HomeAction.FecharCameraCapture -> fecharCameraCapture()
            is HomeAction.ConfirmarFotoCamera -> {
                if (_uiState.value.registrarPontoModal != null) {
                    atualizarFotoRegistroModal(
                        _uiState.value.cameraUri,
                        br.com.tlmacedo.meuponto.domain.model.FotoOrigem.CAMERA
                    )
                } else if (_uiState.value.edicaoModal != null) {
                    atualizarFotoEdicaoModal(
                        _uiState.value.cameraUri,
                        br.com.tlmacedo.meuponto.domain.model.FotoOrigem.CAMERA
                    )
                } else {
                    confirmarFotoCamera()
                }
            }

            is HomeAction.SelecionarFotoComprovante -> {
                if (_uiState.value.registrarPontoModal != null) {
                    atualizarFotoRegistroModal(action.uri, action.origem)
                } else if (_uiState.value.edicaoModal != null) {
                    atualizarFotoEdicaoModal(action.uri, action.origem)
                } else {
                    selecionarFotoComprovante(action.uri)
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MODAIS DE PONTO
    // ══════════════════════════════════════════════════════════════════════

    // ══════════════════════════════════════════════════════════════════════
    // RECARREGAR CONFIGURAÇÃO (quando volta de outras telas)
    // ══════════════════════════════════════════════════════════════════════
    private fun recarregarConfiguracaoEmprego() {
        val empregoId = _uiState.value.empregoAtivo?.id ?: return
        viewModelScope.launch {
            val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
            _uiState.update { it.copy(configuracaoEmprego = configuracao) }
            android.util.Log.d(
                "HomeViewModel",
                "Configuração recarregada: fotoObrigatoria=${configuracao?.fotoObrigatoria}"
            )
        }
    }

    // ── EDIÇÃO ──────────────────────────────────────────────────────────────

    private fun abrirEdicaoModal(ponto: Ponto) {
        val indice = _uiState.value.getIndicePonto(ponto.id)

        val fotoPathAbsoluto = ponto.fotoComprovantePath?.let { relativePath ->
            if (relativePath.startsWith("/")) relativePath
            else comprovanteImageStorage.getComprovantesDirectory()
                ?.resolve(relativePath)?.absolutePath
        }

        _uiState.update {
            it.copy(
                edicaoModal = EdicaoModalState(
                    ponto = ponto,
                    indicePonto = indice,
                    fotoPathAbsoluto = fotoPathAbsoluto
                )
            )
        }
    }

    private fun fecharEdicaoModal() {
        _uiState.update { it.copy(edicaoModal = null) }
    }

    private fun atualizarFotoEdicaoModal(
        uri: Uri?,
        origem: br.com.tlmacedo.meuponto.domain.model.FotoOrigem = br.com.tlmacedo.meuponto.domain.model.FotoOrigem.NENHUMA
    ) {
        _uiState.update { state ->
            state.copy(
                edicaoModal = state.edicaoModal?.copy(
                    fotoUri = uri,
                    fotoOrigem = origem,
                    fotoRemovida = uri == null,
                    isProcessingOcr = uri != null && state.configuracaoEmprego?.fotoRegistrarPontoOcr == true
                ),
                showFotoSourceDialog = false
            )
        }

        if (uri != null && _uiState.value.configuracaoEmprego?.fotoRegistrarPontoOcr == true) {
            viewModelScope.launch {
                val configuracao = _uiState.value.configuracaoEmprego ?: return@launch
                val dataSelecionada = _uiState.value.dataSelecionada
                val empregoAtivo = _uiState.value.empregoAtivo ?: return@launch
                val empregoId = empregoAtivo.id

                // Busca horários habituais
                val diaSemana = DiaSemana.fromJavaDayOfWeek(dataSelecionada.dayOfWeek)
                val horarioDia =
                    horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)
                val habituais = listOfNotNull(
                    horarioDia?.entradaIdeal,
                    horarioDia?.saidaIntervaloIdeal,
                    horarioDia?.voltaIntervaloIdeal,
                    horarioDia?.saidaIdeal
                )

                val resultadosOcr = ocrService.extrairDadosMultiplosComprovantes(
                    uri = uri,
                    horariosHabituais = habituais,
                    empregoId = empregoId
                )

                if (resultadosOcr.isNotEmpty()) {
                    val resultado = resultadosOcr.first()
                    val nsrLimpissimo = resultado.nsr?.replace(Regex("[^0-9]"), "")
                        ?.replaceFirst("^0+".toRegex(), "")
                    val finalUri =
                        resultado.imagemRecortadaPath?.let { Uri.fromFile(File(it)) } ?: uri

                    _uiState.update { state ->
                        state.copy(
                            edicaoModal = state.edicaoModal?.copy(
                                ocrSucesso = true,
                                isProcessingOcr = false,
                                fotoUri = finalUri,
                                ponto = state.edicaoModal.ponto.copy(
                                    nsr = if (configuracao.habilitarNsr) (nsrLimpissimo
                                        ?: state.edicaoModal.ponto.nsr) else state.edicaoModal.ponto.nsr,
                                    nsrAutoFilled = configuracao.habilitarNsr && nsrLimpissimo != null,
                                    dataHora = resultado.hora?.let {
                                        LocalDateTime.of(state.edicaoModal.ponto.data, it)
                                    } ?: state.edicaoModal.ponto.dataHora,
                                    horaAutoFilled = resultado.hora != null,
                                    dataAutoFilled = resultado.data != null
                                )
                            )
                        )
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("Dados extraídos com sucesso!"))
                } else {
                    _uiState.update { state ->
                        state.copy(edicaoModal = state.edicaoModal?.copy(isProcessingOcr = false))
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("Não foi possível extrair dados automaticamente."))
                }
            }
        }
    }

    private fun removerFotoEdicaoModal() {
        _uiState.update { state ->
            state.copy(
                edicaoModal = state.edicaoModal?.copy(
                    fotoUri = null,
                    fotoPathAbsoluto = null,
                    fotoRemovida = true
                )
            )
        }
    }

    private fun reprocessarOcrEdicaoModal() {
        val modal = _uiState.value.edicaoModal ?: return
        val uri = modal.fotoUri ?: modal.fotoPathAbsoluto?.let { Uri.fromFile(File(it)) }
        ?: return

        atualizarFotoEdicaoModal(uri, modal.fotoOrigem)
    }

    private fun salvarEdicaoModal(
        pontoId: Long,
        hora: LocalTime,
        nsr: String?,
        motivo: MotivoEdicao,
        detalhes: String?,
        observacao: String? = null
    ) {
        viewModelScope.launch {
            val modalState = _uiState.value.edicaoModal ?: return@launch

            _uiState.update { state ->
                state.copy(
                    edicaoModal = state.edicaoModal?.copy(isSaving = true)
                )
            }

            try {
                val motivoCompleto = when {
                    motivo == MotivoEdicao.OUTRO -> detalhes ?: ""
                    motivo.requerDetalhes && !detalhes.isNullOrBlank() ->
                        "${motivo.descricao}: $detalhes"

                    else -> motivo.descricao
                }

                // Buscar o ponto atual
                val pontoAtual = _uiState.value.pontosHoje.find { it.id == pontoId }

                if (pontoAtual != null) {
                    var novoPath = pontoAtual.fotoComprovantePath

                    // Lógica de foto
                    if (modalState.fotoRemovida) {
                        novoPath = null
                        fotoComprovanteDao.excluirPorPontoId(pontoId)
                    }

                    modalState.fotoUri?.let { uri ->
                        val dataHoraPonto = LocalDateTime.of(pontoAtual.data, hora)
                        val relativePath = comprovanteImageStorage.saveFromUri(
                            uri = uri,
                            empregoId = _uiState.value.empregoAtivo?.id ?: 0L,
                            pontoId = pontoId,
                            dataHora = dataHoraPonto
                        )
                        if (relativePath != null) {
                            novoPath = relativePath
                        }
                    }

                    val pontoAtualizado = pontoAtual.copy(
                        dataHora = LocalDateTime.of(pontoAtual.data, hora),
                        horaConsiderada = hora,
                        nsr = nsr,
                        fotoComprovantePath = novoPath,
                        fotoOrigem = modalState.fotoOrigem,
                        isEditadoManualmente = true,
                        nsrAutoFilled = if (nsr != pontoAtual.nsr) false else pontoAtual.nsrAutoFilled,
                        horaAutoFilled = if (hora != pontoAtual.hora) false else pontoAtual.horaAutoFilled,
                        dataAutoFilled = pontoAtual.dataAutoFilled,
                        observacao = buildString {
                            observacao?.let { append(it).append(" | ") }
                            append("[Editado: $motivoCompleto]")
                        }.take(500),
                        atualizadoEm = LocalDateTime.now()
                    )

                    pontoRepository.atualizar(pontoAtualizado)

                    // Atualizar snapshot se houver foto
                    novoPath?.let { path ->
                        registrarSnapshotFotoComprovante(pontoId, path, modalState.fotoOrigem)
                    }
                }

                _uiState.update { it.copy(edicaoModal = null) }
                _uiEvent.emit(HomeUiEvent.MostrarMensagem("Ponto atualizado com sucesso"))
                carregarPontosDoDia()
                carregarBancoHoras()
                atualizarWidget()

            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Erro ao salvar edição: ${e.message}")
                _uiState.update { state ->
                    state.copy(
                        edicaoModal = state.edicaoModal?.copy(isSaving = false)
                    )
                }
                _uiEvent.emit(HomeUiEvent.MostrarErro("Erro ao atualizar: ${e.message}"))
            }
        }
    }

    // ── EXCLUSÃO ────────────────────────────────────────────────────────────

    private fun abrirExclusaoModal(ponto: Ponto) {
        val indice = _uiState.value.getIndicePonto(ponto.id)
        _uiState.update {
            it.copy(
                exclusaoModal = ExclusaoModalState(
                    ponto = ponto,
                    indicePonto = indice
                )
            )
        }
    }

    private fun fecharExclusaoModal() {
        _uiState.update { it.copy(exclusaoModal = null) }
    }

    private fun confirmarExclusaoModal(pontoId: Long, motivo: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    exclusaoModal = state.exclusaoModal?.copy(isDeleting = true)
                )
            }

            try {
                val parametros = ExcluirPontoUseCase.Parametros(
                    pontoId = pontoId,
                    motivo = motivo
                )

                when (val resultado = excluirPontoUseCase(parametros)) {
                    is ExcluirPontoUseCase.Resultado.Sucesso -> {
                        _uiState.update { it.copy(exclusaoModal = null) }
                        _uiEvent.emit(HomeUiEvent.MostrarMensagem("Ponto excluído com sucesso"))
                        carregarPontosDoDia()
                        carregarBancoHoras()
                        atualizarWidget()
                    }

                    is ExcluirPontoUseCase.Resultado.Erro -> {
                        _uiState.update { state ->
                            state.copy(
                                exclusaoModal = state.exclusaoModal?.copy(isDeleting = false)
                            )
                        }
                        _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                    }

                    is ExcluirPontoUseCase.Resultado.NaoEncontrado -> {
                        _uiState.update { it.copy(exclusaoModal = null) }
                        _uiEvent.emit(HomeUiEvent.MostrarErro("Ponto não encontrado"))
                    }

                    is ExcluirPontoUseCase.Resultado.Validacao -> {
                        _uiState.update { state ->
                            state.copy(
                                exclusaoModal = state.exclusaoModal?.copy(isDeleting = false)
                            )
                        }
                        _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.erros.joinToString("\n")))
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        exclusaoModal = state.exclusaoModal?.copy(isDeleting = false)
                    )
                }
                _uiEvent.emit(HomeUiEvent.MostrarErro("Erro ao excluir: ${e.message}"))
            }
        }
    }

    // ── LOCALIZAÇÃO ─────────────────────────────────────────────────────────

    private fun abrirLocalizacaoModal(ponto: Ponto) {
        val indice = _uiState.value.getIndicePonto(ponto.id)
        _uiState.update {
            it.copy(
                localizacaoModal = LocalizacaoModalState(
                    ponto = ponto,
                    indicePonto = indice
                )
            )
        }
    }

    private fun fecharLocalizacaoModal() {
        _uiState.update { it.copy(localizacaoModal = null) }
    }

    // ── FOTO ────────────────────────────────────────────────────────────────

    private fun abrirFotoModal(ponto: Ponto) {
        val indice = _uiState.value.getIndicePonto(ponto.id)

        // Usar o caminho da foto armazenado no próprio ponto
        val fotoPath = ponto.fotoComprovantePath?.let { relativePath ->
            // Se for caminho absoluto, usar diretamente
            if (relativePath.startsWith("/")) {
                relativePath
            } else {
                // Construir caminho completo a partir do diretório base
                comprovanteImageStorage.getComprovantesDirectory()
                    ?.resolve(relativePath)
                    ?.absolutePath
            }
        }

        _uiState.update {
            it.copy(
                fotoModal = FotoModalState(
                    ponto = ponto,
                    indicePonto = indice,
                    fotoPath = fotoPath
                )
            )
        }
    }

    private fun fecharFotoModal() {
        _uiState.update { it.copy(fotoModal = null) }
    }

    private fun salvarFotoModal(pontoId: Long, path: String) {
        viewModelScope.launch {
            // Ao salvar no modal, o arquivo já é sobrescrito no disco.
            // Precisamos atualizar a origem da foto para "EDITADA" no banco de dados.
            try {
                // Se o path for absoluto, converter para relativo se estiver dentro do dir de comprovantes
                val relativePath = if (path.startsWith("/")) {
                    val baseDir = comprovanteImageStorage.getComprovantesDirectory()?.absolutePath
                    if (baseDir != null && path.startsWith(baseDir)) {
                        path.substring(baseDir.length).removePrefix("/")
                    } else {
                        path
                    }
                } else {
                    path
                }

                pontoRepository.atualizarFotoComprovante(
                    pontoId = pontoId,
                    fotoPath = relativePath,
                    fotoOrigem = br.com.tlmacedo.meuponto.domain.model.FotoOrigem.EDITADA
                )

                // Registrar Snapshot atualizado
                registrarSnapshotFotoComprovante(
                    pontoId,
                    relativePath,
                    br.com.tlmacedo.meuponto.domain.model.FotoOrigem.EDITADA
                )

                _uiEvent.emit(HomeUiEvent.MostrarMensagem("Foto editada e salva com sucesso"))
                carregarPontosDoDia()
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Erro ao atualizar foto editada: ${e.message}")
                _uiEvent.emit(HomeUiEvent.MostrarErro("Erro ao salvar edição da foto"))
            }
        }
    }

    // ── NOVO MODAL DE REGISTRO (UNIFICADO) ──────────────────────────────────

    private fun abrirRegistrarPontoModal(dataHora: LocalDateTime) {
        viewModelScope.launch {
            val empregoId = _uiState.value.empregoAtivo?.id ?: 0L
            val diaSemana = DiaSemana.fromJavaDayOfWeek(dataHora.dayOfWeek)
            val horarioDiaSemana =
                horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

            val resumoComNovoPonto = calcularResumoDiaUseCase(
                pontos = _uiState.value.pontosHoje + Ponto(
                    id = 0,
                    empregoId = empregoId,
                    dataHora = dataHora,
                    horaConsiderada = dataHora.toLocalTime()
                ),
                data = dataHora.toLocalDate(),
                horarioDiaSemana = horarioDiaSemana,
                tipoDiaEspecial = _uiState.value.resumoDia.tipoDiaEspecial
            )

            val config = _uiState.value.configuracaoEmprego
            val observacaoObrigatoria = config?.comentarioObrigatorioHoraExtra == true &&
                    (resumoComNovoPonto.saldoDiaMinutos > config.limiteHoraExtraSemComentario)

            _uiState.update {
                it.copy(
                    registrarPontoModal = RegistrarPontoModalState(
                        dataHora = dataHora,
                        isObservacaoObrigatoria = observacaoObrigatoria
                    )
                )
            }

            // Iniciar captura de localização se habilitado e for automática
            if (_uiState.value.localizacaoHabilitada && _uiState.value.configuracaoEmprego?.localizacaoAutomatica == true) {
                capturarLocalizacaoRegistroModal()
            }
        }
    }

    private fun fecharRegistrarPontoModal() {
        _uiState.update { it.copy(registrarPontoModal = null) }
    }

    private fun atualizarNsrRegistroModal(nsr: String) {
        _uiState.update { state ->
            state.copy(
                registrarPontoModal = state.registrarPontoModal?.copy(
                    nsr = nsr,
                    nsrAutoFilled = false
                )
            )
        }
    }

    private fun atualizarObservacaoRegistroModal(observacao: String) {
        _uiState.update { state ->
            state.copy(
                registrarPontoModal = state.registrarPontoModal?.copy(observacao = observacao)
            )
        }
    }

    private fun atualizarFotoRegistroModal(
        uri: Uri?,
        origem: br.com.tlmacedo.meuponto.domain.model.FotoOrigem = br.com.tlmacedo.meuponto.domain.model.FotoOrigem.NENHUMA
    ) {
        _uiState.update { state ->
            state.copy(
                registrarPontoModal = state.registrarPontoModal?.copy(
                    fotoUri = uri,
                    fotoOrigem = origem,
                    isProcessingOcr = uri != null && state.configuracaoEmprego?.fotoRegistrarPontoOcr == true
                ),
                showFotoSourceDialog = false
            )
        }

        // Bloqueio de duplicidade e OCR Real com Validações
        if (uri != null) {
            viewModelScope.launch {
                val configuracao = _uiState.value.configuracaoEmprego ?: return@launch
                val dataSelecionada = _uiState.value.dataSelecionada
                val empregoAtivo = _uiState.value.empregoAtivo ?: return@launch
                val empregoId = empregoAtivo.id

                // 1. SEMPRE verificar duplicidade pelo Hash MD5 (Independente da flag de validação)
                val hash = imageHashCalculator.calculateMd5(uri)
                if (hash != null) {
                    val fotoExistente = fotoComprovanteDao.buscarPorHash(hash)
                    if (fotoExistente != null) {
                        limparFotoOcrInvalida()
                        val dataFormatada =
                            fotoExistente.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        _uiEvent.emit(HomeUiEvent.MostrarErro("COMPROVANTE DUPLICADO\n\nEste comprovante já foi registrado no dia $dataFormatada e não pode ser reutilizado."))
                        return@launch
                    }
                }

                // Só prossegue se o OCR estiver habilitado
                if (configuracao.fotoRegistrarPontoOcr != true) return@launch

                // Busca horários habituais para o triplo-check de hora
                val diaSemana = DiaSemana.fromJavaDayOfWeek(dataSelecionada.dayOfWeek)
                val horarioDia =
                    horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)
                val habituais = listOfNotNull(
                    horarioDia?.entradaIdeal,
                    horarioDia?.saidaIntervaloIdeal,
                    horarioDia?.voltaIntervaloIdeal,
                    horarioDia?.saidaIdeal
                )

                // OCR Real (Suporte a múltiplos comprovantes)
                val resultadosOcr = ocrService.extrairDadosMultiplosComprovantes(
                    uri = uri,
                    horariosHabituais = habituais,
                    empregoId = empregoId
                )

                if (resultadosOcr.isNotEmpty()) {
                    val nomeUsuario = usuarioLogado?.nome ?: ""
                    val validarComprovante = configuracao.fotoValidarComprovante

                    // Se houver apenas um comprovante
                    if (resultadosOcr.size == 1) {
                        val resultado = resultadosOcr.first()

                        // Validações Condicionais (Somente se fotoValidarComprovante estiver ativa)
                        if (validarComprovante) {
                            // 1. Validação de Data
                            if (resultado.data != null && resultado.data != dataSelecionada) {
                                limparFotoOcrInvalida()
                                val dataFormatada =
                                    resultado.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                _uiEvent.emit(HomeUiEvent.MostrarErro("COMPROVANTE INVÁLIDO\n\nA data extraída ($dataFormatada) não corresponde ao dia selecionado."))
                                return@launch
                            }

                            // 2. Validação de Usuário
                            val nomeValido = resultado.nomeTrabalhador?.let { nomeExtraido ->
                                nomeExtraido.contains(nomeUsuario, ignoreCase = true) ||
                                        nomeUsuario.contains(nomeExtraido, ignoreCase = true)
                            } ?: true

                            if (!nomeValido) {
                                limparFotoOcrInvalida()
                                _uiEvent.emit(HomeUiEvent.MostrarErro("COMPROVANTE INVÁLIDO\n\nEste comprovante pertence a outro trabalhador (${resultado.nomeTrabalhador})."))
                                return@launch
                            }

                            // 3. Validação de Empregador (CNPJ)
                            if (resultado.cnpj != null && !empregoAtivo.cnpj.isNullOrBlank()) {
                                val cnpjComprovante = resultado.cnpj.replace(Regex("[^0-9]"), "")
                                val cnpjEmprego = empregoAtivo.cnpj.replace(Regex("[^0-9]"), "")

                                if (cnpjComprovante != cnpjEmprego) {
                                    limparFotoOcrInvalida()
                                    val empresaNome = resultado.razaoSocial ?: "outra empresa"
                                    _uiEvent.emit(HomeUiEvent.MostrarErro("COMPROVANTE INVÁLIDO\n\nEste comprovante pertence a $empresaNome (CNPJ: ${resultado.cnpj})."))
                                    return@launch
                                }
                            }
                        }

                        // Sucesso (ou validações ignoradas) -> Preencher campos
                        val nsrLimpissimo = resultado.nsr?.replace(Regex("[^0-9]"), "")
                            ?.replaceFirst("^0+".toRegex(), "")

                        // Substituir imagem pela imagem recortada se disponível
                        val finalUri =
                            resultado.imagemRecortadaPath?.let { Uri.fromFile(File(it)) }
                                ?: uri

                        _uiState.update { state ->
                            state.copy(
                                registrarPontoModal = state.registrarPontoModal?.copy(
                                    nsr = if (configuracao.habilitarNsr) (nsrLimpissimo
                                        ?: state.registrarPontoModal.nsr) else state.registrarPontoModal.nsr,
                                    nsrAutoFilled = configuracao.habilitarNsr && nsrLimpissimo != null,
                                    dataHora = resultado.hora?.let {
                                        LocalDateTime.of(
                                            state.registrarPontoModal.dataHora.toLocalDate(),
                                            it
                                        )
                                    } ?: state.registrarPontoModal.dataHora,
                                    horaAutoFilled = resultado.hora != null,
                                    dataAutoFilled = resultado.data != null,
                                    fotoUri = finalUri,
                                    isProcessingOcr = false,
                                    ocrSucesso = true
                                )
                            )
                        }
                        _uiEvent.emit(HomeUiEvent.MostrarMensagem("Dados extraídos com sucesso!"))
                    } else {
                        // Múltiplos comprovantes (o processarMultiplosComprovantes também deve respeitar a flag)
                        _uiState.update { it.copy(registrarPontoModal = null) }
                        processarMultiplosComprovantes(
                            resultadosOcr,
                            dataSelecionada,
                            nomeUsuario,
                            empregoAtivo,
                            configuracao
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            registrarPontoModal = state.registrarPontoModal?.copy(
                                isProcessingOcr = false
                            )
                        )
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("Não foi possível extrair dados automaticamente."))
                }
            }
        }
    }

    private suspend fun processarMultiplosComprovantes(
        resultados: List<br.com.tlmacedo.meuponto.domain.model.PontoOcrResult>,
        dataSelecionada: LocalDate,
        nomeUsuario: String,
        empregoAtivo: Emprego?,
        configuracao: ConfiguracaoEmprego
    ) {
        val pontosParaRegistrar = mutableListOf<RegistrarPontoUseCase.Parametros>()
        val imagensParaSalvar = mutableListOf<Pair<Int, Uri>>()
        var erros = 0
        val validarComprovante = configuracao.fotoValidarComprovante

        for (resultado in resultados) {
            // 1. Verificação de Duplicidade por Hash (Sempre obrigatória)
            var isDuplicado = false
            resultado.imagemRecortadaPath?.let { path ->
                val hash = imageHashCalculator.calculateMd5(File(path))
                if (hash != null && fotoComprovanteDao.buscarPorHash(hash) != null) {
                    isDuplicado = true
                }
            }
            if (isDuplicado) {
                erros++
                continue
            }

            // 2. Validações Condicionais
            val dataValida =
                !validarComprovante || resultado.data == null || resultado.data == dataSelecionada
            val nomeValido = !validarComprovante || resultado.nomeTrabalhador?.let {
                it.contains(
                    nomeUsuario,
                    ignoreCase = true
                ) || nomeUsuario.contains(it, ignoreCase = true)
            } ?: true
            val cnpjValido =
                if (validarComprovante && resultado.cnpj != null && !empregoAtivo?.cnpj.isNullOrBlank()) {
                    resultado.cnpj.replace(
                        Regex("[^0-9]"),
                        ""
                    ) == empregoAtivo?.cnpj?.replace(Regex("[^0-9]"), "")
                } else true

            if (dataValida && nomeValido && cnpjValido) {
                val dataHora = LocalDateTime.of(dataSelecionada, resultado.hora ?: LocalTime.now())
                val nsr =
                    resultado.nsr?.replace(Regex("[^0-9]"), "")?.replaceFirst("^0+".toRegex(), "")

                pontosParaRegistrar.add(
                    RegistrarPontoUseCase.Parametros(
                        empregoId = empregoAtivo?.id ?: 0L,
                        dataHora = dataHora,
                        nsr = if (configuracao.habilitarNsr) nsr else null
                    )
                )

                resultado.imagemRecortadaPath?.let {
                    imagensParaSalvar.add(
                        pontosParaRegistrar.size - 1 to Uri.fromFile(
                            File(
                                it
                            )
                        )
                    )
                }
            } else {
                erros++
            }
        }

        if (pontosParaRegistrar.isNotEmpty()) {
            var registrados = 0
            for (i in pontosParaRegistrar.indices) {
                val result = registrarPontoUseCase(pontosParaRegistrar[i])
                if (result is RegistrarPontoUseCase.Resultado.Sucesso) {
                    registrados++
                    // Salvar imagem se houver
                    val imgUri = imagensParaSalvar.find { it.first == i }?.second
                    val dataHora = pontosParaRegistrar[i].dataHora ?: LocalDateTime.now()
                    if (imgUri != null) {
                        salvarFotoComprovante(
                            imgUri,
                            result.pontoId,
                            empregoAtivo?.id ?: 0L,
                            dataHora,
                            br.com.tlmacedo.meuponto.domain.model.FotoOrigem.GALERIA
                        )
                    }
                }
            }
            _uiEvent.emit(HomeUiEvent.MostrarMensagem("Registrados $registrados pontos com sucesso.${if (erros > 0) " $erros ignorados por divergência de dados." else ""}"))
            carregarPontosDoDia()
            carregarBancoHoras()
        } else if (erros > 0) {
            _uiEvent.emit(HomeUiEvent.MostrarErro("Nenhum comprovante válido encontrado na imagem."))
        }
    }

    private fun limparFotoOcrInvalida() {
        _uiState.update { state ->
            state.copy(
                registrarPontoModal = state.registrarPontoModal?.copy(
                    fotoUri = null,
                    isProcessingOcr = false
                )
            )
        }
    }

    private fun atualizarHoraRegistroModal(hora: LocalTime) {
        viewModelScope.launch {
            val modal = _uiState.value.registrarPontoModal ?: return@launch
            val empregoId = _uiState.value.empregoAtivo?.id ?: return@launch
            val novaDataHora = LocalDateTime.of(modal.dataHora.toLocalDate(), hora)

            val diaSemana = DiaSemana.fromJavaDayOfWeek(novaDataHora.dayOfWeek)
            val horarioDiaSemana =
                horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

            val resumoComNovoPonto = calcularResumoDiaUseCase(
                pontos = _uiState.value.pontosHoje + Ponto(
                    id = 0,
                    empregoId = empregoId,
                    dataHora = novaDataHora,
                    horaConsiderada = novaDataHora.toLocalTime()
                ),
                data = novaDataHora.toLocalDate(),
                horarioDiaSemana = horarioDiaSemana,
                tipoDiaEspecial = _uiState.value.resumoDia.tipoDiaEspecial
            )

            val config = _uiState.value.configuracaoEmprego
            val observacaoObrigatoria = config?.comentarioObrigatorioHoraExtra == true &&
                    (resumoComNovoPonto.saldoDiaMinutos > config.limiteHoraExtraSemComentario)

            _uiState.update { state ->
                state.copy(
                    registrarPontoModal = modal.copy(
                        dataHora = novaDataHora,
                        showTimePicker = false,
                        horaAutoFilled = false,
                        isObservacaoObrigatoria = observacaoObrigatoria
                    )
                )
            }
        }
    }

    private fun abrirTimePickerRegistroModal() {
        _uiState.update { state ->
            state.copy(
                registrarPontoModal = state.registrarPontoModal?.copy(showTimePicker = true)
            )
        }
    }

    private fun fecharTimePickerRegistroModal() {
        _uiState.update { state ->
            state.copy(
                registrarPontoModal = state.registrarPontoModal?.copy(showTimePicker = false)
            )
        }
    }

    private fun capturarLocalizacaoRegistroModal() {
        if (!_uiState.value.localizacaoHabilitada) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    registrarPontoModal = state.registrarPontoModal?.copy(
                        isCapturingLocation = true,
                        erroLocalizacao = null
                    )
                )
            }

            if (locationService.hasLocationPermission()) {
                try {
                    val localizacao = locationService.getCurrentLocation()
                    if (localizacao != null) {
                        val endereco = locationService.getAddressFromLocation(
                            localizacao.latitude,
                            localizacao.longitude
                        )
                        _uiState.update { state ->
                            state.copy(
                                registrarPontoModal = state.registrarPontoModal?.copy(
                                    latitude = localizacao.latitude,
                                    longitude = localizacao.longitude,
                                    endereco = endereco,
                                    isCapturingLocation = false
                                )
                            )
                        }
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                registrarPontoModal = state.registrarPontoModal?.copy(
                                    isCapturingLocation = false,
                                    erroLocalizacao = "Não foi possível obter a localização. Verifique se o GPS está ligado."
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { state ->
                        state.copy(
                            registrarPontoModal = state.registrarPontoModal?.copy(
                                isCapturingLocation = false,
                                erroLocalizacao = "Erro ao capturar localização: ${e.message}"
                            )
                        )
                    }
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        registrarPontoModal = state.registrarPontoModal?.copy(
                            isCapturingLocation = false,
                            erroLocalizacao = "Permissão de localização negada"
                        )
                    )
                }
                _uiEvent.emit(HomeUiEvent.SolicitarPermissaoLocalizacao)
            }
        }
    }

    private fun confirmarRegistroPontoModal() {
        viewModelScope.launch {
            val modalState = _uiState.value.registrarPontoModal ?: return@launch
            val empregoId = _uiState.value.empregoAtivo?.id ?: return@launch

            // Validar foto obrigatória
            if (_uiState.value.fotoObrigatoria && modalState.fotoUri == null) {
                _uiEvent.emit(HomeUiEvent.MostrarErro("A foto do comprovante é obrigatória."))
                return@launch
            }

            // Validar NSR obrigatório
            if (_uiState.value.nsrHabilitado && modalState.nsr.isBlank()) {
                _uiEvent.emit(HomeUiEvent.MostrarErro("O NSR é obrigatório."))
                return@launch
            }

            // Validar comentário obrigatório
            val config = _uiState.value.configuracaoEmprego
            val diaSemana = DiaSemana.fromJavaDayOfWeek(modalState.dataHora.dayOfWeek)
            val horarioDiaSemana =
                horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

            val resumoComNovoPonto = calcularResumoDiaUseCase(
                pontos = _uiState.value.pontosHoje + Ponto(
                    id = 0,
                    empregoId = empregoId,
                    dataHora = modalState.dataHora,
                    horaConsiderada = modalState.dataHora.toLocalTime()
                ),
                data = modalState.dataHora.toLocalDate(),
                horarioDiaSemana = horarioDiaSemana,
                tipoDiaEspecial = _uiState.value.resumoDia.tipoDiaEspecial
            )

            val comentarioObrigatorio = config?.comentarioObrigatorioHoraExtra == true &&
                    (resumoComNovoPonto.saldoDiaMinutos > config.limiteHoraExtraSemComentario)
            if (comentarioObrigatorio && modalState.observacao.isBlank()) {
                _uiEvent.emit(HomeUiEvent.MostrarErro("A observação é obrigatória quando há horas extras acima de ${config.limiteHoraExtraSemComentario} min."))
                return@launch
            }

            // Validar justificativa para inconsistência
            if (config?.exigeJustificativaInconsistencia == true &&
                resumoComNovoPonto.temProblemas &&
                modalState.observacao.isBlank()
            ) {
                val problemas =
                    resumoComNovoPonto.listaInconsistencias.joinToString("\n• ", prefix = "• ")
                _uiEvent.emit(HomeUiEvent.MostrarErro("JUSTIFICATIVA OBRIGATÓRIA\n\nEste dia apresenta as seguintes inconsistências:\n\n$problemas\n\nPor favor, preencha a observação com o motivo."))
                return@launch
            }

            _uiState.update { state ->
                state.copy(
                    registrarPontoModal = state.registrarPontoModal?.copy(isSaving = true)
                )
            }

            val parametros = RegistrarPontoUseCase.Parametros(
                empregoId = empregoId,
                dataHora = modalState.dataHora,
                observacao = modalState.observacao.takeIf { it.isNotBlank() },
                nsr = if (_uiState.value.nsrHabilitado) modalState.nsr else null,
                latitude = modalState.latitude,
                longitude = modalState.longitude,
                endereco = modalState.endereco,
                nsrAutoFilled = modalState.nsrAutoFilled,
                horaAutoFilled = modalState.horaAutoFilled,
                dataAutoFilled = modalState.dataAutoFilled
            )

            // 1. Verificação de Duplicidade por Hash (Última trava antes de salvar)
            modalState.fotoUri?.let { uri ->
                val hash = imageHashCalculator.calculateMd5(uri)
                if (hash != null) {
                    val fotoExistente = fotoComprovanteDao.buscarPorHash(hash)
                    if (fotoExistente != null) {
                        _uiState.update { state ->
                            state.copy(
                                registrarPontoModal = state.registrarPontoModal?.copy(
                                    isSaving = false
                                )
                            )
                        }
                        val dataFormatada =
                            fotoExistente.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        _uiEvent.emit(HomeUiEvent.MostrarErro("COMPROVANTE DUPLICADO\n\nEste comprovante já foi registrado no dia $dataFormatada e não pode ser reutilizado."))
                        return@launch
                    }
                }
            }

            when (val resultado = registrarPontoUseCase(parametros)) {
                is RegistrarPontoUseCase.Resultado.Sucesso -> {
                    // Salvar foto se houver
                    modalState.fotoUri?.let { uri ->
                        salvarFotoComprovante(
                            uri,
                            resultado.pontoId,
                            empregoId,
                            modalState.dataHora,
                            modalState.fotoOrigem
                        )
                    }

                    _uiState.update { it.copy(registrarPontoModal = null) }

                    val horaFormatada =
                        modalState.dataHora.format(DateTimeFormatter.ofPattern("HH:mm"))
                    val tipoDescricao = _uiState.value.proximoTipo.descricao
                    _uiEvent.emit(
                        HomeUiEvent.MostrarMensagem("$tipoDescricao registrada às $horaFormatada")
                    )

                    carregarPontosDoDia()
                    carregarBancoHoras()
                    atualizarWidget()
                }

                is RegistrarPontoUseCase.Resultado.LocalizacaoObrigatoria -> {
                    _uiState.update { state ->
                        state.copy(registrarPontoModal = state.registrarPontoModal?.copy(isSaving = false))
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarErro("A localização é obrigatória para este registro. Verifique as permissões de GPS."))
                }

                is RegistrarPontoUseCase.Resultado.NsrObrigatorio -> {
                    _uiState.update { state ->
                        state.copy(registrarPontoModal = state.registrarPontoModal?.copy(isSaving = false))
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarErro("O NSR é obrigatório para este registro."))
                }

                is RegistrarPontoUseCase.Resultado.HorarioInvalido -> {
                    _uiState.update { state ->
                        state.copy(registrarPontoModal = state.registrarPontoModal?.copy(isSaving = false))
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.motivo))
                }

                is RegistrarPontoUseCase.Resultado.LimiteAtingido -> {
                    _uiState.update { state ->
                        state.copy(registrarPontoModal = state.registrarPontoModal?.copy(isSaving = false))
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Limite de registros diários atingido."))
                }

                is RegistrarPontoUseCase.Resultado.VersaoNaoEncontrada -> {
                    _uiState.update { state ->
                        state.copy(registrarPontoModal = state.registrarPontoModal?.copy(isSaving = false))
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Configuração de jornada não encontrada."))
                }

                is RegistrarPontoUseCase.Resultado.Validacao -> {
                    _uiState.update { state ->
                        state.copy(registrarPontoModal = state.registrarPontoModal?.copy(isSaving = false))
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.erros.joinToString("\n")))
                }

                is RegistrarPontoUseCase.Resultado.Erro -> {
                    _uiState.update { state ->
                        state.copy(registrarPontoModal = state.registrarPontoModal?.copy(isSaving = false))
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }

                else -> {
                    _uiState.update { state ->
                        state.copy(registrarPontoModal = state.registrarPontoModal?.copy(isSaving = false))
                    }
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Erro ao registrar ponto."))
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // CICLO DE BANCO DE HORAS
    // ══════════════════════════════════════════════════════════════════════

    private fun verificarCicloBancoHoras() {
        viewModelScope.launch {
            val empregoId = _uiState.value.empregoAtivo?.id
            android.util.Log.d("CICLO_DEBUG", "verificarCicloBancoHoras - empregoId: $empregoId")

            if (empregoId == null) {
                android.util.Log.d("CICLO_DEBUG", "empregoId é null, retornando")
                return@launch
            }

            // Inicializar ciclos retroativos
            val resultadoInit = inicializarCiclosRetroativosUseCase(empregoId)
            android.util.Log.d("CICLO_DEBUG", "inicializarCiclos resultado: $resultadoInit")

            // Verificar estado do ciclo atual
            val resultado = verificarCicloPendenteUseCase(empregoId)
            android.util.Log.d("CICLO_DEBUG", "verificarCicloPendente resultado: $resultado")

            when (resultado) {
                is VerificarCicloPendenteUseCase.Resultado.CicloPendente -> {
                    android.util.Log.d("CICLO_DEBUG", "CICLO PENDENTE detectado!")
                    _uiState.update {
                        it.copy(
                            estadoCiclo = EstadoCiclo.Pendente(
                                ciclo = resultado.ciclo,
                                diasAposVencimento = resultado.diasAposVencimento
                            )
                        )
                    }
                }

                is VerificarCicloPendenteUseCase.Resultado.CicloProximoDoFim -> {
                    _uiState.update {
                        it.copy(
                            estadoCiclo = EstadoCiclo.ProximoDoFim(
                                ciclo = resultado.ciclo,
                                diasRestantes = resultado.diasRestantes
                            )
                        )
                    }
                }

                is VerificarCicloPendenteUseCase.Resultado.CicloEmAndamento -> {
                    _uiState.update {
                        it.copy(
                            estadoCiclo = EstadoCiclo.EmAndamento(
                                ciclo = resultado.ciclo,
                                diasRestantes = resultado.diasRestantes
                            )
                        )
                    }
                }

                is VerificarCicloPendenteUseCase.Resultado.SemVersaoJornada,
                is VerificarCicloPendenteUseCase.Resultado.BancoNaoHabilitado,
                is VerificarCicloPendenteUseCase.Resultado.CicloNaoConfigurado -> {
                    _uiState.update { it.copy(estadoCiclo = EstadoCiclo.Nenhum) }
                }
            }
        }
    }

    private fun abrirDialogFechamentoCiclo() {
        _uiState.update { it.copy(showFechamentoCicloDialog = true) }
    }

    private fun fecharDialogFechamentoCiclo() {
        _uiState.update { it.copy(showFechamentoCicloDialog = false) }
    }

    private fun confirmarFechamentoCiclo() {
        viewModelScope.launch {
            val empregoId = _uiState.value.empregoAtivo?.id ?: return@launch

            _uiState.update { it.copy(isLoading = true, showFechamentoCicloDialog = false) }

            when (val resultado = fecharCicloUseCase.fecharCiclosPendentes(empregoId)) {
                is FecharCicloUseCase.ResultadoMultiplo.Sucesso -> {
                    val qtd = resultado.ciclosFechados.size
                    val saldoTotal = resultado.ciclosFechados.sumOf { it.saldoAtualMinutos }

                    _uiEvent.emit(
                        HomeUiEvent.MostrarMensagem(
                            if (qtd == 1) {
                                "Ciclo fechado. Saldo zerado: ${formatarMinutos(saldoTotal)}"
                            } else {
                                "$qtd ciclos fechados. Saldo total zerado: ${
                                    formatarMinutos(
                                        saldoTotal
                                    )
                                }"
                            }
                        )
                    )

                    resultado.novoCiclo?.let { novoCiclo ->
                        _uiState.update {
                            it.copy(
                                estadoCiclo = EstadoCiclo.EmAndamento(
                                    ciclo = novoCiclo,
                                    diasRestantes = ChronoUnit.DAYS
                                        .between(LocalDate.now(), novoCiclo.dataFim).toInt()
                                )
                            )
                        }
                    }

                    carregarBancoHoras()
                }

                is FecharCicloUseCase.ResultadoMultiplo.NenhumPendente -> {
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("Nenhum ciclo pendente"))
                }

                is FecharCicloUseCase.ResultadoMultiplo.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun navegarParaHistoricoCiclos() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaHistoricoCiclos)
        }
    }

    private fun formatarMinutos(minutos: Int): String {
        val sinal = if (minutos >= 0) "+" else "-"
        val total = abs(minutos)
        val horas = total / 60
        val mins = total % 60
        return "$sinal${String.format("%02d:%02d", horas, mins)}"
    }

    // ══════════════════════════════════════════════════════════════════════
    // MENU DE EMPREGO
    // ══════════════════════════════════════════════════════════════════════

    private fun abrirMenuEmprego() {
        _uiState.update { it.copy(showEmpregoMenu = true) }
    }

    private fun fecharMenuEmprego() {
        _uiState.update { it.copy(showEmpregoMenu = false) }
    }

    private fun navegarParaNovoEmprego() {
        viewModelScope.launch {
            fecharMenuEmprego()
            _uiEvent.emit(HomeUiEvent.NavegarParaNovoEmprego)
        }
    }

    private fun navegarParaEditarEmprego() {
        val empregoId = _uiState.value.empregoAtivo?.id ?: return
        viewModelScope.launch {
            fecharMenuEmprego()
            _uiEvent.emit(HomeUiEvent.NavegarParaEditarEmprego(empregoId))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // CARREGAMENTO DE DADOS
    // ══════════════════════════════════════════════════════════════════════

    private fun carregarUsuarioLogado() {
        viewModelScope.launch {
            authRepository.observarUsuarioLogado().collect { usuario ->
                usuarioLogado = usuario
            }
        }
    }

    private fun carregarEmpregoAtivo() {
        viewModelScope.launch {
            obterEmpregoAtivoUseCase.observar().collect { emprego ->
                val empregoAnterior = _uiState.value.empregoAtivo
                _uiState.update { it.copy(empregoAtivo = emprego) }

                if (emprego != null && empregoAnterior?.id != emprego.id) {
                    carregarConfiguracaoEmprego(emprego.id)
                    carregarPontosDoDia()
                    carregarBancoHoras()
                    carregarFechamentoCicloAnterior()
                    verificarCicloBancoHoras()
                }
            }
        }
    }

    private fun carregarConfiguracaoEmprego(empregoId: Long) {
        viewModelScope.launch {
            val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
            _uiState.update { it.copy(configuracaoEmprego = configuracao) }
        }
    }

    private fun carregarEmpregos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEmpregos = true) }
            listarEmpregosUseCase.observarAtivos().collect { empregosComResumo ->
                _uiState.update {
                    it.copy(
                        empregosDisponiveis = empregosComResumo.map { er -> er.emprego },
                        isLoadingEmpregos = false
                    )
                }
            }
        }
    }

    private fun carregarPontosDoDia() {
        pontosCollectionJob?.cancel()

        val data = _uiState.value.dataSelecionada
        val empregoId = _uiState.value.empregoAtivo?.id ?: return

        pontosCollectionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            obterResumoDiaCompletoUseCase.observar(empregoId, data).collect { resumoCompleto ->
                val proximoTipo = determinarProximoTipoPontoUseCase(resumoCompleto.pontos)

                _uiState.update {
                    it.copy(
                        pontosHoje = resumoCompleto.pontos,
                        resumoDia = resumoCompleto.resumoDia,
                        feriadosDoDia = resumoCompleto.feriadosDoDia,
                        ausenciaDoDia = resumoCompleto.ausenciaPrincipal,
                        metadataFerias = resumoCompleto.metadataFerias,
                        versaoJornadaAtual = null,
                        proximoTipo = proximoTipo,
                        isLoading = false
                    )
                }
                
                // Sincronizar com Wear OS sempre que os pontos do dia mudarem
                if (data == LocalDate.now()) {
                    syncPontoStatusWithWearUseCase()
                }
            }
        }
    }

    private fun determinarTipoDiaEspecial(
        feriados: List<Feriado>,
        ausencia: Ausencia? = null
    ): TipoDiaEspecial {
        if (ausencia != null) {
            return ausencia.tipo.toTipoDiaEspecial(ausencia.tipoFolga)
        }

        if (feriados.isEmpty()) return TipoDiaEspecial.NORMAL

        val temFeriadoFolga = feriados.any { feriado ->
            feriado.tipo in TipoFeriado.tiposFolga()
        }

        return if (temFeriadoFolga) {
            TipoDiaEspecial.FERIADO
        } else {
            TipoDiaEspecial.NORMAL
        }
    }

    private fun carregarBancoHoras() {
        bancoHorasCollectionJob?.cancel()

        val empregoId = _uiState.value.empregoAtivo?.id ?: return
        val dataSelecionada = _uiState.value.dataSelecionada

        bancoHorasCollectionJob = viewModelScope.launch {
            calcularBancoHorasUseCase(
                empregoId = empregoId,
                ateData = dataSelecionada
            ).collect { resultado ->
                _uiState.update { it.copy(bancoHoras = resultado.bancoHoras) }
            }
        }
    }

    private fun carregarFechamentoCicloAnterior() {
        viewModelScope.launch {
            val empregoId = _uiState.value.empregoAtivo?.id ?: return@launch
            val dataSelecionada = _uiState.value.dataSelecionada

            val fechamento = fechamentoPeriodoRepository.buscarUltimoFechamentoBancoAteData(
                empregoId = empregoId,
                ateData = dataSelecionada.plusDays(1)
            )

            val fechamentoRelevante = fechamento?.takeIf {
                it.dataFimPeriodo.plusDays(1) == dataSelecionada
            }

            _uiState.update { it.copy(fechamentoCicloAnterior = fechamentoRelevante) }
        }
    }

    private fun recarregarDados() {
        carregarPontosDoDia()
        carregarBancoHoras()
    }

    // ══════════════════════════════════════════════════════════════════════
    // RELÓGIO
    // ══════════════════════════════════════════════════════════════════════

    private fun iniciarRelogioAtualizado() {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(horaAtual = LocalTime.now()) }
                delay(1000L)
            }
        }
    }

    private fun atualizarHora() {
        _uiState.update { it.copy(horaAtual = LocalTime.now()) }
    }

    // ══════════════════════════════════════════════════════════════════════
    // NAVEGAÇÃO POR DATA
    // ══════════════════════════════════════════════════════════════════════

    private fun navegarDiaAnterior() {
        val novaData = _uiState.value.dataSelecionada.minusDays(1)
        if (_uiState.value.podeNavegaAnterior) {
            selecionarData(novaData)
        }
    }

    private fun navegarProximoDia() {
        val novaData = _uiState.value.dataSelecionada.plusDays(1)
        if (_uiState.value.podeNavegarProximo) {
            selecionarData(novaData)
        }
    }

    private fun irParaHoje() {
        selecionarData(LocalDate.now())
    }

    private fun selecionarData(data: LocalDate) {
        // Fechar modais ao trocar de data
        if (_uiState.value.temModalAberto) {
            _uiState.update {
                it.copy(
                    edicaoModal = null,
                    exclusaoModal = null,
                    localizacaoModal = null,
                    fotoModal = null
                )
            }
        }

        _uiState.update { it.copy(dataSelecionada = data) }
        carregarPontosDoDia()
        carregarBancoHoras()
        carregarFechamentoCicloAnterior()
    }

    // ══════════════════════════════════════════════════════════════════════
    // SELEÇÃO DE EMPREGO
    // ══════════════════════════════════════════════════════════════════════

    private fun abrirSeletorEmprego() {
        _uiState.update { it.copy(showEmpregoSelector = true) }
    }

    private fun fecharSeletorEmprego() {
        _uiState.update { it.copy(showEmpregoSelector = false) }
    }

    private fun selecionarEmprego(emprego: Emprego) {
        viewModelScope.launch {
            // Fechar modais ao trocar de emprego
            if (_uiState.value.temModalAberto) {
                _uiState.update {
                    it.copy(
                        edicaoModal = null,
                        exclusaoModal = null,
                        localizacaoModal = null,
                        fotoModal = null
                    )
                }
            }

            when (val resultado = trocarEmpregoAtivoUseCase(emprego)) {
                is TrocarEmpregoAtivoUseCase.Resultado.Sucesso -> {
                    fecharSeletorEmprego()
                    _uiEvent.emit(HomeUiEvent.EmpregoTrocado(emprego.nome))
                    carregarConfiguracaoEmprego(emprego.id)
                    recarregarDados()
                }

                is TrocarEmpregoAtivoUseCase.Resultado.NaoEncontrado -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Emprego não encontrado"))
                }

                is TrocarEmpregoAtivoUseCase.Resultado.EmpregoIndisponivel -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Emprego indisponível"))
                }

                is TrocarEmpregoAtivoUseCase.Resultado.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // REGISTRO DE PONTO
    // ══════════════════════════════════════════════════════════════════════

    private fun abrirTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = true) }
    }

    private fun fecharTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = false) }
    }

    private fun iniciarRegistroPonto(hora: LocalTime) {
        val empregoId = _uiState.value.empregoAtivo?.id
        if (empregoId == null) {
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("Nenhum emprego ativo selecionado"))
            }
            return
        }

        fecharTimePicker()

        val data = _uiState.value.dataSelecionada
        val dataHora = LocalDateTime.of(data, hora)

        abrirRegistrarPontoModal(dataHora)
    }

    // ══════════════════════════════════════════════════════════════════════
    // FOTO DE COMPROVANTE
    // ══════════════════════════════════════════════════════════════════════

    fun criarCameraUri(): Uri? {
        return try {
            val empregoId = _uiState.value.empregoAtivo?.id ?: return null
            val data = _uiState.value.dataSelecionada

            val tempFile = comprovanteImageStorage.createTempFileForCamera(empregoId, data)
            FileProvider.getUriForFile(
                comprovanteImageStorage.appContext,
                "${comprovanteImageStorage.appContext.packageName}.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Erro ao criar URI da câmera: ${e.message}")
            null
        }
    }

    fun getComprovantesDirectory(): File? {
        return try {
            comprovanteImageStorage.getComprovantesDirectory()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun registrarSnapshotFotoComprovante(
        pontoId: Long,
        relativePath: String,
        fotoOrigem: br.com.tlmacedo.meuponto.domain.model.FotoOrigem
    ) {
        try {
            val baseDir = comprovanteImageStorage.getComprovantesDirectory()
            val file = if (relativePath.startsWith("/")) File(relativePath)
            else File(baseDir, relativePath)

            if (!file.exists()) {
                android.util.Log.e(
                    "HomeViewModel",
                    "Arquivo não encontrado para snapshot: ${file.absolutePath}"
                )
                return
            }

            val hash = imageHashCalculator.calculateMd5(file) ?: ""
            val size = file.length()

            val ponto = pontoRepository.buscarPorId(pontoId) ?: return
            val empregoId = ponto.empregoId
            val data = ponto.data
            val versao = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)

            val resumo = obterResumoDiaCompletoUseCase(empregoId, data)
            val pontosOrdenados = resumo.pontos.sortedBy { it.dataHora }
            val indice = pontosOrdenados.indexOfFirst { it.id == pontoId } + 1

            val saldoBanco = _uiState.value.bancoHoras.saldoTotalMinutos
            val existente = fotoComprovanteDao.buscarPorPontoId(pontoId)

            val entity = FotoComprovanteEntity(
                id = existente?.id ?: 0L,
                pontoId = pontoId,
                empregoId = empregoId,
                data = data,
                diaSemana = data.dayOfWeek,
                hora = ponto.dataHora.toLocalTime(),
                indicePontoDia = if (indice > 0) indice else 0,
                nsr = ponto.nsr,
                versaoJornada = versao?.numeroVersao ?: 0,
                tipoJornadaDia = resumo.tipoDiaEspecial.toTipoJornadaDia(),
                horasTrabalhadasDiaMinutos = resumo.horasTrabalhadasMinutos.toLong(),
                saldoDiaMinutos = resumo.saldoDiaMinutos.toLong(),
                saldoBancoHorasMinutos = saldoBanco.toLong(),
                fotoPath = relativePath,
                fotoTimestamp = Instant.now(),
                fotoOrigem = fotoOrigem,
                fotoTamanhoBytes = size,
                fotoHashMd5 = hash,
                observacao = ponto.observacao,
                criadoEm = existente?.criadoEm ?: Instant.now(),
                atualizadoEm = Instant.now()
            )

            fotoComprovanteDao.inserir(entity)
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Erro ao registrar snapshot: ${e.message}")
        }
    }

    private suspend fun salvarFotoComprovante(
        uri: Uri,
        pontoId: Long,
        empregoId: Long,
        dataHora: LocalDateTime,
        fotoOrigem: br.com.tlmacedo.meuponto.domain.model.FotoOrigem = br.com.tlmacedo.meuponto.domain.model.FotoOrigem.NENHUMA
    ) {
        try {
            val relativePath = comprovanteImageStorage.saveFromUri(
                uri = uri,
                empregoId = empregoId,
                pontoId = pontoId,
                dataHora = dataHora
            )

            if (relativePath != null) {
                // 1. Atualiza o path no PontoEntity (legado/compatibilidade)
                pontoRepository.atualizarFotoComprovante(pontoId, relativePath, fotoOrigem)

                // 2. Criar Snapshot completo na FotoComprovanteEntity para Auditoria
                registrarSnapshotFotoComprovante(pontoId, relativePath, fotoOrigem)

                android.util.Log.d("HomeViewModel", "Foto salva e snapshot criado: $relativePath")
            } else {
                android.util.Log.w("HomeViewModel", "Falha ao salvar foto")
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Erro ao salvar foto comprovante: ${e.message}")
        }
    }

    private fun abrirFotoSourceDialog() {
        _uiState.update { it.copy(showFotoSourceDialog = true) }
    }

    private fun fecharFotoSourceDialog() {
        _uiState.update { it.copy(showFotoSourceDialog = false) }
    }

    private fun abrirCameraCapture() {
        _uiState.update { it.copy(showCameraCapture = true, showFotoSourceDialog = false) }
    }

    private fun fecharCameraCapture() {
        _uiState.update { it.copy(showCameraCapture = false) }
    }

    private fun confirmarFotoCamera() {
        val uri = _uiState.value.cameraUri ?: return
        fecharFotoSourceDialog()

        if (_uiState.value.registrarPontoModal != null) {
            atualizarFotoRegistroModal(uri, br.com.tlmacedo.meuponto.domain.model.FotoOrigem.CAMERA)
        } else if (_uiState.value.edicaoModal != null) {
            atualizarFotoEdicaoModal(uri, br.com.tlmacedo.meuponto.domain.model.FotoOrigem.CAMERA)
        }
    }

    private fun selecionarFotoComprovante(uri: Uri) {
        fecharFotoSourceDialog()

        if (_uiState.value.registrarPontoModal != null) {
            atualizarFotoRegistroModal(
                uri,
                br.com.tlmacedo.meuponto.domain.model.FotoOrigem.GALERIA
            )
        } else if (_uiState.value.edicaoModal != null) {
            atualizarFotoEdicaoModal(uri, br.com.tlmacedo.meuponto.domain.model.FotoOrigem.GALERIA)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // NAVEGAÇÃO
    // ══════════════════════════════════════════════════════════════════════

    private fun navegarParaEdicao(pontoId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaEditarPonto(pontoId))
        }
    }

    private fun navegarParaHistorico() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaHistorico)
        }
    }

    private fun navegarParaConfiguracoes() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaConfiguracoes)
        }
    }

    private fun navegarParaEditarJornada() {
        val empregoId = _uiState.value.empregoAtivo?.id ?: return
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavegarParaEditarJornada(empregoId))
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════════════

    private fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DATE PICKER
    // ══════════════════════════════════════════════════════════════════════

    private fun abrirDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    private fun fecharDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    private fun atualizarWidget() {
        val request =
            androidx.work.OneTimeWorkRequestBuilder<br.com.tlmacedo.meuponto.presentation.widget.WidgetUpdateWorker>()
                .build()
        // O WorkManager usa o contexto da aplicação para agendar a tarefa de atualização do widget
        androidx.work.WorkManager.getInstance(comprovanteImageStorage.appContext).enqueue(request)
    }
}
