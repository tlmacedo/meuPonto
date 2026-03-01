// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/SettingsViewModel.kt
package br.com.tlmacedo.meuponto.presentation.screen.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.emprego.TrocarEmpregoAtivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel da tela principal de configurações.
 *
 * Gerencia o estado completo da tela, incluindo:
 * - Lista de empregos com resumos
 * - Troca de emprego ativo
 * - Contadores de configurações
 *
 * @author Thiago
 * @since 3.0.0
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val empregoRepository: EmpregoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val feriadoRepository: FeriadoRepository,
    private val ausenciaRepository: AusenciaRepository,
    private val ajusteSaldoRepository: AjusteSaldoRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase,
    private val trocarEmpregoAtivoUseCase: TrocarEmpregoAtivoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<SettingsEvent>()
    val eventos: SharedFlow<SettingsEvent> = _eventos.asSharedFlow()

    init {
        carregarVersaoApp()
        observarDados()
    }

    /**
     * Processa ações da UI.
     */
    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.AbrirSeletorEmprego -> abrirSeletorEmprego()
            is SettingsAction.FecharSeletorEmprego -> fecharSeletorEmprego()
            is SettingsAction.TrocarEmpregoAtivo -> trocarEmpregoAtivo(action.empregoId)
            is SettingsAction.AbrirDialogNovoEmprego -> abrirDialogNovoEmprego()
            is SettingsAction.FecharDialogNovoEmprego -> fecharDialogNovoEmprego()
            is SettingsAction.LimparErro -> limparErro()
            is SettingsAction.Recarregar -> recarregar()
        }
    }

    private fun carregarVersaoApp() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName ?: "1.0.0"
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }

            _uiState.update {
                it.copy(
                    appVersion = versionName,
                    buildNumber = versionCode
                )
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Erro ao obter versão do app")
        }
    }

    private fun observarDados() {
        viewModelScope.launch {
            combine(
                empregoRepository.observarTodos(),
                obterEmpregoAtivoUseCase.observar(),
                feriadoRepository.observarTodosAtivos()
            ) { empregos, empregoAtivo, feriados ->
                Triple(empregos, empregoAtivo, feriados)
            }
                .catch { e ->
                    Timber.e(e, "Erro ao observar dados de configurações")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Erro ao carregar configurações: ${e.message}"
                        )
                    }
                }
                .collect { (empregos, empregoAtivo, feriados) ->
                    processarDados(empregos, empregoAtivo, feriados.size)
                }
        }
    }

    private suspend fun processarDados(
        empregos: List<Emprego>,
        empregoAtivo: Emprego?,
        totalFeriadosGlobais: Int
    ) {
        val empregosAtivos = empregos.filter { !it.arquivado }
        val empregosArquivados = empregos.filter { it.arquivado }

        // Criar resumos para cada emprego ativo
        val resumos = empregosAtivos.map { emprego ->
            criarResumoEmprego(emprego)
        }

        val resumoAtivo = resumos.find { it.emprego.id == empregoAtivo?.id }
        val outrosResumos = resumos.filter { it.emprego.id != empregoAtivo?.id }

        _uiState.update {
            it.copy(
                isLoading = false,
                empregoAtivo = resumoAtivo,
                outrosEmpregos = outrosResumos,
                empregosArquivados = empregosArquivados,
                totalEmpregos = empregos.size,
                totalFeriadosGlobais = totalFeriadosGlobais
            )
        }
    }

    private suspend fun criarResumoEmprego(emprego: Emprego): EmpregoResumo {
        val configuracao = configuracaoEmpregoRepository.buscarPorEmpregoId(emprego.id)
        val versaoVigente = versaoJornadaRepository.buscarVigente(emprego.id)
        val totalVersoes = versaoJornadaRepository.contarPorEmprego(emprego.id)
        val totalAusencias = ausenciaRepository.contarPorEmprego(emprego.id)
        val totalAjustes = ajusteSaldoRepository.contarPorEmprego(emprego.id)

        return EmpregoResumo(
            emprego = emprego,
            configuracao = configuracao,
            versaoVigente = versaoVigente,
            totalVersoes = totalVersoes,
            totalAusencias = totalAusencias,
            totalAjustes = totalAjustes
        )
    }

    private fun abrirSeletorEmprego() {
        _uiState.update { it.copy(mostrarSeletorEmprego = true) }
    }

    private fun fecharSeletorEmprego() {
        _uiState.update { it.copy(mostrarSeletorEmprego = false) }
    }

    private fun trocarEmpregoAtivo(empregoId: Long) {
        viewModelScope.launch {
            fecharSeletorEmprego()

            when (val resultado = trocarEmpregoAtivoUseCase(empregoId)) {
                is TrocarEmpregoAtivoUseCase.Resultado.Sucesso -> {
                    _eventos.emit(SettingsEvent.MostrarMensagem("Emprego alterado com sucesso"))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.NaoEncontrado -> {
                    _eventos.emit(SettingsEvent.MostrarMensagem("Emprego não encontrado"))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.EmpregoIndisponivel -> {
                    _eventos.emit(SettingsEvent.MostrarMensagem("Emprego indisponível"))
                }
                is TrocarEmpregoAtivoUseCase.Resultado.Erro -> {
                    _eventos.emit(SettingsEvent.MostrarMensagem(resultado.mensagem))
                }
            }
        }
    }

    private fun abrirDialogNovoEmprego() {
        _uiState.update { it.copy(mostrarDialogNovoEmprego = true) }
    }

    private fun fecharDialogNovoEmprego() {
        _uiState.update { it.copy(mostrarDialogNovoEmprego = false) }
    }

    private fun limparErro() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun recarregar() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observarDados()
    }
}
