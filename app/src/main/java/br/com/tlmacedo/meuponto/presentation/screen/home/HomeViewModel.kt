// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/home/HomeViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.home

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.model.MotivoEdicao
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
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
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularResumoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.DeterminarProximoTipoPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ExcluirPontoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterPontosDoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.RegistrarPontoUseCase
import br.com.tlmacedo.meuponto.util.ComprovanteImageStorage
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
    private val pontoRepository: PontoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent: SharedFlow<HomeUiEvent> = _uiEvent.asSharedFlow()

    private var pontosCollectionJob: Job? = null
    private var bancoHorasCollectionJob: Job? = null
    private var versaoJornadaCollectionJob: Job? = null

    init {
        carregarEmpregoAtivo()
        carregarEmpregos()
        carregarPontosDoDia()
        carregarBancoHoras()
        iniciarRelogioAtualizado()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.RecarregarConfiguracaoEmprego -> recarregarConfiguracaoEmprego()
            is HomeAction.RegistrarPontoAgora -> iniciarRegistroPonto(LocalTime.now())
            is HomeAction.AbrirTimePickerDialog -> abrirTimePicker()
            is HomeAction.FecharTimePickerDialog -> fecharTimePicker()
            is HomeAction.RegistrarPontoManual -> iniciarRegistroPonto(action.hora)
            is HomeAction.AtualizarNsr -> atualizarNsr(action.nsr)
            is HomeAction.ConfirmarRegistroComNsr -> confirmarRegistroComNsr()
            is HomeAction.CancelarNsrDialog -> cancelarNsrDialog()
            is HomeAction.AbrirFotoSourceDialog -> abrirFotoSourceDialog()
            is HomeAction.FecharFotoSourceDialog -> fecharFotoSourceDialog()
            is HomeAction.ConfirmarFotoCamera -> confirmarFotoCamera()
            is HomeAction.SelecionarFotoComprovante -> selecionarFotoComprovante(action.uri)
            is HomeAction.RemoverFotoComprovante -> removerFotoComprovante()

            // ══════════════════════════════════════════════════════════════════════
            // MODAIS DE PONTO
            // ══════════════════════════════════════════════════════════════════════
            is HomeAction.AbrirEdicaoModal -> abrirEdicaoModal(action.ponto)
            is HomeAction.FecharEdicaoModal -> fecharEdicaoModal()
            is HomeAction.SalvarEdicaoModal -> salvarEdicaoModal(
                action.pontoId, action.hora, action.nsr, action.motivo, action.detalhes
            )
            is HomeAction.AbrirExclusaoModal -> abrirExclusaoModal(action.ponto)
            is HomeAction.FecharExclusaoModal -> fecharExclusaoModal()
            is HomeAction.ConfirmarExclusaoModal -> confirmarExclusaoModal(action.pontoId, action.motivo)
            is HomeAction.AbrirLocalizacaoModal -> abrirLocalizacaoModal(action.ponto)
            is HomeAction.FecharLocalizacaoModal -> fecharLocalizacaoModal()
            is HomeAction.AbrirFotoModal -> abrirFotoModal(action.ponto)
            is HomeAction.FecharFotoModal -> fecharFotoModal()

            // Editar ponto (navegação para tela completa)
            is HomeAction.EditarPonto -> {
                viewModelScope.launch {
                    _uiEvent.emit(HomeUiEvent.NavegarParaEditarPonto(action.pontoId))
                }
            }

            // Exclusão legado (manter por compatibilidade)
            is HomeAction.SolicitarExclusao -> solicitarExclusao(action.ponto)
            is HomeAction.CancelarExclusao -> cancelarExclusao()
            is HomeAction.AtualizarMotivoExclusao -> atualizarMotivoExclusao(action.motivo)
            is HomeAction.ConfirmarExclusao -> confirmarExclusao()

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
            android.util.Log.d("HomeViewModel", "Configuração recarregada: fotoObrigatoria=${configuracao?.fotoObrigatoria}")
        }
    }

    // ── EDIÇÃO ──────────────────────────────────────────────────────────────

    private fun abrirEdicaoModal(ponto: Ponto) {
        val indice = _uiState.value.getIndicePonto(ponto.id)
        _uiState.update {
            it.copy(
                edicaoModal = EdicaoModalState(
                    ponto = ponto,
                    indicePonto = indice
                )
            )
        }
    }

    private fun fecharEdicaoModal() {
        _uiState.update { it.copy(edicaoModal = null) }
    }

    private fun salvarEdicaoModal(
        pontoId: Long,
        hora: LocalTime,
        nsr: String?,
        motivo: MotivoEdicao,
        detalhes: String?
    ) {
        viewModelScope.launch {
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
                    val pontoAtualizado = pontoAtual.copy(
                        dataHora = LocalDateTime.of(pontoAtual.data, hora),
                        horaConsiderada = hora,
                        nsr = nsr,
                        isEditadoManualmente = true,
                        observacao = buildString {
                            pontoAtual.observacao?.let { append(it).append(" | ") }
                            append("[Editado: $motivoCompleto]")
                        }.take(500),
                        atualizadoEm = LocalDateTime.now()
                    )

                    pontoRepository.atualizar(pontoAtualizado)
                }

                _uiState.update { it.copy(edicaoModal = null) }
                _uiEvent.emit(HomeUiEvent.MostrarMensagem("Ponto atualizado com sucesso"))
                carregarPontosDoDia()
                carregarBancoHoras()

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
                                "$qtd ciclos fechados. Saldo total zerado: ${formatarMinutos(saldoTotal)}"
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
                        versaoJornadaAtual = null,
                        proximoTipo = proximoTipo,
                        isLoading = false
                    )
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

        android.util.Log.d("HomeViewModel", "iniciarRegistroPonto: hora=$hora, fotoObrigatoria=${_uiState.value.fotoObrigatoria}, fotoUri=${_uiState.value.fotoComprovanteUri}")

        if (_uiState.value.fotoObrigatoria && _uiState.value.fotoComprovanteUri == null) {
            val uri = criarCameraUri()
            android.util.Log.d("HomeViewModel", "Abrindo diálogo de foto. cameraUri=$uri")
            _uiState.update {
                it.copy(
                    horaPendenteParaRegistro = hora,
                    showFotoSourceDialog = true,
                    cameraUri = uri
                )
            }
            return
        }

        continuarFluxoRegistro(hora)
    }

    private fun continuarFluxoRegistro(hora: LocalTime) {
        android.util.Log.d("HomeViewModel", "continuarFluxoRegistro: hora=$hora, nsrHabilitado=${_uiState.value.nsrHabilitado}")

        if (_uiState.value.nsrHabilitado) {
            _uiState.update {
                it.copy(
                    showNsrDialog = true,
                    nsrPendente = "",
                    horaPendenteParaRegistro = hora
                )
            }
        } else {
            registrarPonto(hora, null)
        }
    }

    private fun atualizarNsr(nsr: String) {
        _uiState.update { it.copy(nsrPendente = nsr) }
    }

    private fun confirmarRegistroComNsr() {
        val hora = _uiState.value.horaPendenteParaRegistro
        val nsr = _uiState.value.nsrPendente

        android.util.Log.d("HomeViewModel", "confirmarRegistroComNsr: hora=$hora, nsr=$nsr")

        if (hora == null) {
            android.util.Log.e("HomeViewModel", "confirmarRegistroComNsr: hora é null!")
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("Erro interno. Tente novamente."))
            }
            _uiState.update {
                it.copy(
                    showNsrDialog = false,
                    nsrPendente = "",
                    horaPendenteParaRegistro = null
                )
            }
            return
        }

        if (nsr.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("NSR é obrigatório"))
            }
            return
        }

        _uiState.update {
            it.copy(
                showNsrDialog = false,
                nsrPendente = "",
                horaPendenteParaRegistro = null
            )
        }

        registrarPonto(hora, nsr)
    }

    private fun cancelarNsrDialog() {
        _uiState.update {
            it.copy(
                showNsrDialog = false,
                nsrPendente = "",
                horaPendenteParaRegistro = null,
                fotoComprovanteUri = null
            )
        }
    }

    private fun registrarPonto(hora: LocalTime, nsr: String?) {
        val empregoId = _uiState.value.empregoAtivo?.id
        if (empregoId == null) {
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("Nenhum emprego ativo selecionado"))
            }
            return
        }

        if (_uiState.value.fotoObrigatoria && _uiState.value.fotoComprovanteUri == null) {
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("Foto do comprovante é obrigatória"))
            }
            return
        }

        android.util.Log.d("HomeViewModel", "registrarPonto: hora=$hora, nsr=$nsr, fotoUri=${_uiState.value.fotoComprovanteUri}")

        viewModelScope.launch {
            val data = _uiState.value.dataSelecionada
            val dataHora = LocalDateTime.of(data, hora)
            val fotoUri = _uiState.value.fotoComprovanteUri

            val parametros = RegistrarPontoUseCase.Parametros(
                empregoId = empregoId,
                dataHora = dataHora,
                nsr = nsr
            )

            when (val resultado = registrarPontoUseCase(parametros)) {
                is RegistrarPontoUseCase.Resultado.Sucesso -> {
                    fotoUri?.let { uri ->
                        salvarFotoComprovante(uri, resultado.pontoId, empregoId, dataHora)
                    }

                    _uiState.update { it.copy(fotoComprovanteUri = null) }

                    val horaFormatada = hora.format(DateTimeFormatter.ofPattern("HH:mm"))
                    val tipoDescricao = _uiState.value.proximoTipo.descricao
                    _uiEvent.emit(
                        HomeUiEvent.MostrarMensagem("$tipoDescricao registrada às $horaFormatada")
                    )
                }
                is RegistrarPontoUseCase.Resultado.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
                is RegistrarPontoUseCase.Resultado.Validacao -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.erros.joinToString("\n")))
                }
                is RegistrarPontoUseCase.Resultado.SemEmpregoAtivo -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Nenhum emprego ativo configurado"))
                }
                is RegistrarPontoUseCase.Resultado.HorarioInvalido -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.motivo))
                }
                is RegistrarPontoUseCase.Resultado.LimiteAtingido -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Limite de ${resultado.limite} pontos atingido"))
                }
                is RegistrarPontoUseCase.Resultado.LocalizacaoObrigatoria -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Localização é obrigatória para este emprego"))
                }
                is RegistrarPontoUseCase.Resultado.NsrObrigatorio -> {
                    _uiState.update {
                        it.copy(
                            showNsrDialog = true,
                            nsrPendente = "",
                            horaPendenteParaRegistro = hora
                        )
                    }
                }
            }
        }
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

    private suspend fun salvarFotoComprovante(
        uri: Uri,
        pontoId: Long,
        empregoId: Long,
        dataHora: LocalDateTime
    ) {
        try {
            val relativePath = comprovanteImageStorage.saveFromUri(
                uri = uri,
                empregoId = empregoId,
                pontoId = pontoId,
                dataHora = dataHora
            )

            if (relativePath != null) {
                pontoRepository.atualizarFotoComprovante(pontoId, relativePath)
                android.util.Log.d("HomeViewModel", "Foto salva: $relativePath")
            } else {
                android.util.Log.w("HomeViewModel", "Falha ao salvar foto")
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Erro ao salvar foto: ${e.message}")
        }
    }

    private fun abrirFotoSourceDialog() {
        val uri = criarCameraUri()
        _uiState.update {
            it.copy(
                showFotoSourceDialog = true,
                cameraUri = uri
            )
        }
    }

    private fun fecharFotoSourceDialog() {
        _uiState.update {
            it.copy(
                showFotoSourceDialog = false,
                cameraUri = null
            )
        }
    }

    private fun confirmarFotoCamera() {
        val cameraUri = _uiState.value.cameraUri
        val horaPendente = _uiState.value.horaPendenteParaRegistro

        android.util.Log.d("HomeViewModel", "confirmarFotoCamera: cameraUri=$cameraUri, horaPendente=$horaPendente")

        if (cameraUri == null) {
            android.util.Log.e("HomeViewModel", "confirmarFotoCamera: cameraUri é null!")
            _uiState.update {
                it.copy(
                    showFotoSourceDialog = false,
                    horaPendenteParaRegistro = null
                )
            }
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("Erro ao capturar foto. Tente novamente."))
            }
            return
        }

        _uiState.update {
            it.copy(
                fotoComprovanteUri = cameraUri,
                cameraUri = null,
                showFotoSourceDialog = false
            )
        }

        continuarRegistroAposFoto()
    }

    private fun selecionarFotoComprovante(uri: Uri) {
        android.util.Log.d("HomeViewModel", "selecionarFotoComprovante: uri=$uri, horaPendente=${_uiState.value.horaPendenteParaRegistro}")

        _uiState.update {
            it.copy(
                fotoComprovanteUri = uri,
                showFotoSourceDialog = false
            )
        }

        continuarRegistroAposFoto()
    }

    private fun removerFotoComprovante() {
        _uiState.update { it.copy(fotoComprovanteUri = null) }
    }

    private fun continuarRegistroAposFoto() {
        val hora = _uiState.value.horaPendenteParaRegistro

        android.util.Log.d("HomeViewModel", "continuarRegistroAposFoto: hora=$hora, nsrHabilitado=${_uiState.value.nsrHabilitado}")

        if (hora == null) {
            android.util.Log.e("HomeViewModel", "continuarRegistroAposFoto: horaPendenteParaRegistro é null!")
            return
        }

        if (_uiState.value.nsrHabilitado) {
            _uiState.update {
                it.copy(
                    showNsrDialog = true,
                    nsrPendente = ""
                )
            }
        } else {
            _uiState.update { it.copy(horaPendenteParaRegistro = null) }
            registrarPonto(hora, null)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // EXCLUSÃO DE PONTO (Legado - manter por compatibilidade)
    // ══════════════════════════════════════════════════════════════════════

    private fun solicitarExclusao(ponto: Ponto) {
        // Fechar modais se houver algum aberto
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

        _uiState.update {
            it.copy(
                showDeleteConfirmDialog = true,
                pontoParaExcluir = ponto
            )
        }
    }

    private fun cancelarExclusao() {
        _uiState.update {
            it.copy(
                showDeleteConfirmDialog = false,
                pontoParaExcluir = null,
                motivoExclusao = ""
            )
        }
    }

    private fun confirmarExclusao() {
        val ponto = _uiState.value.pontoParaExcluir ?: return
        val motivo = _uiState.value.motivoExclusao.trim()

        if (motivo.length < 5) {
            viewModelScope.launch {
                _uiEvent.emit(HomeUiEvent.MostrarErro("Informe um motivo válido (mínimo 5 caracteres)"))
            }
            return
        }

        viewModelScope.launch {
            val parametros = ExcluirPontoUseCase.Parametros(
                pontoId = ponto.id,
                motivo = motivo
            )

            when (val resultado = excluirPontoUseCase(parametros)) {
                is ExcluirPontoUseCase.Resultado.Sucesso -> {
                    _uiEvent.emit(HomeUiEvent.MostrarMensagem("Ponto excluído com sucesso"))
                }
                is ExcluirPontoUseCase.Resultado.Erro -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.mensagem))
                }
                is ExcluirPontoUseCase.Resultado.NaoEncontrado -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro("Ponto não encontrado"))
                }
                is ExcluirPontoUseCase.Resultado.Validacao -> {
                    _uiEvent.emit(HomeUiEvent.MostrarErro(resultado.erros.joinToString("\n")))
                }
            }

            _uiState.update {
                it.copy(
                    showDeleteConfirmDialog = false,
                    pontoParaExcluir = null,
                    motivoExclusao = ""
                )
            }
        }
    }

    private fun atualizarMotivoExclusao(motivo: String) {
        _uiState.update { it.copy(motivoExclusao = motivo) }
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
}
