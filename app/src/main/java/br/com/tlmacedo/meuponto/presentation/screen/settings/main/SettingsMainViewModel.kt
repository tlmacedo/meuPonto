package br.com.tlmacedo.meuponto.presentation.screen.settings.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ListarEmpregosUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.TrocarEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.preferencias.ObterPreferenciasGlobaisUseCase
import br.com.tlmacedo.meuponto.util.toRelativeDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * Estado da tela principal de configurações.
 *
 * @author Thiago
 * @since 9.0.0
 */
data class SettingsMainUiState(
    val isLoading: Boolean = true,
    val empregoAtual: Emprego? = null,
    val empregosDisponiveis: List<Emprego> = emptyList(),
    val versaoVigenteDescricao: String? = null,
    val totalVersoes: Int = 0,
    val saldoAtualTexto: String = "--:--",
    val dataUltimoBackup: String = "Nunca realizado",
    val secoesExpandidas: Set<String> = emptySet()
) {
    val empregoAtualId: Long?
        get() = empregoAtual?.id

    val empregoAtualNome: String?
        get() = empregoAtual?.nome

    val temEmpregoConfigurado: Boolean
        get() = empregoAtual != null
}

/**
 * Ações da tela de configurações.
 */
sealed interface SettingsMainAction {
    data class TrocarEmprego(val emprego: Emprego) : SettingsMainAction
    data class AlternarExpansaoSecao(val secao: String) : SettingsMainAction
    data object Recarregar : SettingsMainAction
}

/**
 * Eventos únicos emitidos pelo ViewModel.
 */
sealed interface SettingsMainEvent {
    data class MostrarMensagem(val mensagem: String) : SettingsMainEvent
    data class EmpregoTrocado(val nomeEmprego: String) : SettingsMainEvent
}

/**
 * ViewModel da tela principal de configurações.
 *
 * Gerencia o estado do emprego atual, versão vigente e lista
 * de empregos disponíveis para troca rápida.
 *
 * @author Thiago
 * @since 9.0.0
 */
@HiltViewModel
class SettingsMainViewModel @Inject constructor(
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val listarEmpregosUseCase: ListarEmpregosUseCase,
    private val trocarEmpregoAtivoUseCase: TrocarEmpregoAtivoUseCase,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val calcularSaldoMensalUseCase: br.com.tlmacedo.meuponto.domain.usecase.saldo.CalcularSaldoMensalUseCase,
    private val calcularBancoHorasUseCase: br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase,
    private val obterPreferenciasGlobaisUseCase: ObterPreferenciasGlobaisUseCase,
    private val backupRepository: br.com.tlmacedo.meuponto.domain.repository.BackupRepository,
    private val cloudBackupRepository: br.com.tlmacedo.meuponto.domain.repository.CloudBackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsMainUiState())
    val uiState: StateFlow<SettingsMainUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<SettingsMainEvent>()
    val eventos: SharedFlow<SettingsMainEvent> = _eventos.asSharedFlow()

    init {
        observarDados()
    }

    /**
     * Processa ações da UI.
     */
    fun onAction(action: SettingsMainAction) {
        when (action) {
            is SettingsMainAction.TrocarEmprego -> trocarEmprego(action.emprego)
            is SettingsMainAction.AlternarExpansaoSecao -> alternarExpansaoSecao(action.secao)
            is SettingsMainAction.Recarregar -> recarregar()
        }
    }

    private fun alternarExpansaoSecao(secao: String) {
        _uiState.update { state ->
            val novasSecoes = if (state.secoesExpandidas.contains(secao)) {
                state.secoesExpandidas - secao
            } else {
                state.secoesExpandidas + secao
            }
            state.copy(secoesExpandidas = novasSecoes)
        }
    }

    /**
     * Observa mudanças no emprego ativo, lista de empregos e saldo mensal.
     */
    private fun observarDados() {
        viewModelScope.launch {
            val empregosFlow = listarEmpregosUseCase.observarTodos()
                .map { lista -> lista.map { it.emprego }.filter { !it.arquivado } }
                .distinctUntilChanged()

            val preferenciasFlow = obterPreferenciasGlobaisUseCase()
                .distinctUntilChanged()

            obterEmpregoAtivoUseCase.observar()
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .flatMapLatest { emprego ->
                    if (emprego == null) {
                        combine(empregosFlow, preferenciasFlow) { empregos, prefs ->
                            val dataBackup = obterDataBackupRecente(prefs.ultimoBackupLocal, prefs.ultimoBackupNuvem)
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    empregoAtual = null,
                                    empregosDisponiveis = empregos,
                                    versaoVigenteDescricao = null,
                                    totalVersoes = 0,
                                    dataUltimoBackup = formatarDataBackup(dataBackup)
                                )
                            }
                        }
                    } else {
                        // Quando temos um emprego, observamos também o saldo e versões de forma reativa
                        combine(
                            empregosFlow,
                            preferenciasFlow,
                            versaoJornadaRepository.observarVigente(emprego.id),
                            observarSaldoMensal(emprego.id)
                        ) { empregos, prefs, versao, saldo ->
                            val dataBackup = obterDataBackupRecente(prefs.ultimoBackupLocal, prefs.ultimoBackupNuvem)
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    empregoAtual = emprego,
                                    empregosDisponiveis = empregos,
                                    versaoVigenteDescricao = versao?.let { v ->
                                        "Versão ${v.numeroVersao}" + (v.descricao?.let { d -> " - $d" } ?: "")
                                    },
                                    totalVersoes = 0,
                                    saldoAtualTexto = saldo,
                                    dataUltimoBackup = formatarDataBackup(dataBackup)
                                )
                            }
                        }
                    }
                }.collectLatest { }
        }
    }

    private suspend fun obterDataBackupRecente(prefLocal: Long?, prefNuvem: Long?): Long? {
        val backupLocalReal = backupRepository.obterDataUltimoBackupLocal()
        
        // Só considera a data da nuvem se o usuário ainda estiver autenticado
        val isNuvemValido = cloudBackupRepository.isUsuarioAutenticado()
        val dataNuvemConsiderar = if (isNuvemValido) prefNuvem else 0L

        return listOfNotNull(prefLocal, dataNuvemConsiderar, backupLocalReal).maxOrNull()
    }

    /**
     * Observa o saldo mensal de forma reativa, reagindo a mudanças nos pontos.
     */
    private fun observarSaldoMensal(empregoId: Long) =
        calcularBancoHorasUseCase.invoke(empregoId)
            .flatMapLatest {
                kotlinx.coroutines.flow.flow {
                    val saldo = calcularSaldoMensalUseCase(empregoId, YearMonth.now())
                    emit(saldo.saldoFormatado)
                }
            }

    private fun formatarDataBackup(millis: Long?): String {
        if (millis == null || millis == 0L) return "Nunca realizado"

        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .toRelativeDateTime()
    }

    /**
     * Troca o emprego ativo.
     */
    private fun trocarEmprego(emprego: Emprego) {
        viewModelScope.launch {
            when (val resultado = trocarEmpregoAtivoUseCase(emprego.id)) {
                is TrocarEmpregoAtivoUseCase.Resultado.Sucesso -> {
                    _eventos.emit(SettingsMainEvent.EmpregoTrocado(emprego.nome))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.NaoEncontrado -> {
                    _eventos.emit(SettingsMainEvent.MostrarMensagem("Emprego não encontrado"))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.EmpregoIndisponivel -> {
                    _eventos.emit(SettingsMainEvent.MostrarMensagem("Emprego indisponível"))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.Erro -> {
                    _eventos.emit(SettingsMainEvent.MostrarMensagem(resultado.mensagem))
                }
            }
        }
    }

    /**
     * Recarrega os dados.
     */
    private fun recarregar() {
        _uiState.update { it.copy(isLoading = true) }
        observarDados()
    }
}
