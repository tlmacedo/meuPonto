package br.com.tlmacedo.meuponto.presentation.screen.settings.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais
import br.com.tlmacedo.meuponto.domain.repository.BackupRepository
import br.com.tlmacedo.meuponto.domain.repository.CloudBackupRepository
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.preferencias.SalvarPreferenciasGlobaisUseCase
import br.com.tlmacedo.meuponto.data.local.datastore.PreferenciasGlobaisDataStore
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.worker.CloudBackupWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import br.com.tlmacedo.meuponto.domain.repository.CloudFile
import br.com.tlmacedo.meuponto.domain.repository.LocalBackupFile
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository

/**
 * Estado da tela de backup.
 */
data class BackupUiState(
    val isLoading: Boolean = true,
    val isProcessando: Boolean = false,
    val operacaoAtual: String? = null,
    val totalEmpregos: Int = 0,
    val totalPontos: Int = 0,
    val totalFeriados: Int = 0,
    val tamanhoEstimado: String = "...",
    val backupNuvemAtivo: Boolean = false,
    val ultimoBackupLocal: Long? = null,
    val ultimoBackupNuvem: Long? = null,
    val contaGoogle: String? = null,
    val isGoogleAutenticado: Boolean = false,
    val backupsLocais: List<LocalBackupFile> = emptyList(),
    val backupsNuvem: List<CloudFile> = emptyList(),
    val mesesParaLimpeza: Int = 24
)

/**
 * Ações da tela de backup.
 */
sealed interface BackupAction {
    data class ExportarBackup(val outputStream: OutputStream) : BackupAction
    data class ImportarBackup(val inputStream: InputStream) : BackupAction
    data object LimparDadosAntigos : BackupAction
    data object Recarregar : BackupAction
    data object BackupNuvemAgora : BackupAction
    data class ToggleBackupNuvem(val ativo: Boolean) : BackupAction
    data object AutenticarGoogle : BackupAction
    data object DeslogarGoogle : BackupAction
    data class RestaurarBackupNuvem(val fileId: String) : BackupAction
    data class RestaurarBackupLocal(val nomeArquivo: String) : BackupAction
    data class ExcluirBackupLocal(val nomeArquivo: String) : BackupAction
    data object ExcluirTodosBackupsLocais : BackupAction
    data class ExcluirBackupNuvem(val fileId: String) : BackupAction
    data object ExcluirTodosBackupsNuvem : BackupAction
    data class AlterarMesesLimpeza(val meses: Int) : BackupAction
}

/**
 * Eventos da tela de backup.
 */
sealed interface BackupEvent {
    data class MostrarMensagem(val mensagem: String) : BackupEvent
    data object ExportacaoConcluida : BackupEvent
    data object ImportacaoConcluida : BackupEvent
    data class LimpezaConcluida(val registrosRemovidos: Int) : BackupEvent
    data object SolicitarDestinoExportacao : BackupEvent
    data object SolicitarOrigemImportacao : BackupEvent
    data object SolicitarAutenticacaoGoogle : BackupEvent
}

/**
 * ViewModel da tela de backup e dados.
 *
 * @author Thiago
 * @since 9.1.0
 */
@HiltViewModel
@Suppress("DEPRECATION") // observarTodos é usado intencionalmente para backup completo
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MeuPontoDatabase,
    private val backupRepository: BackupRepository,
    private val cloudBackupRepository: CloudBackupRepository,
    private val empregoRepository: EmpregoRepository,
    private val pontoRepository: PontoRepository,
    private val feriadoRepository: FeriadoRepository,
    private val preferencesDataStore: PreferenciasGlobaisDataStore,
    private val salvarPreferenciasGlobaisUseCase: SalvarPreferenciasGlobaisUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _eventos = MutableSharedFlow<BackupEvent>()
    val eventos: SharedFlow<BackupEvent> = _eventos.asSharedFlow()

    init {
        carregarEstatisticas()
        observarPreferencias()
    }

    private fun observarPreferencias() {
        viewModelScope.launch {
            preferencesDataStore.preferenciasGlobais.collectLatest { prefs ->
                // 1. Resposta rápida com dados persistidos
                _uiState.update {
                    it.copy(
                        backupNuvemAtivo = prefs.backupNuvemAtivo,
                        contaGoogle = prefs.contaGoogleConectada,
                        isGoogleAutenticado = prefs.contaGoogleConectada != null,
                        ultimoBackupLocal = if (prefs.ultimoBackupLocal > 0) prefs.ultimoBackupLocal else null,
                        ultimoBackupNuvem = if (prefs.ultimoBackupNuvem > 0) prefs.ultimoBackupNuvem else null
                    )
                }

                // 2. Validação em background da conexão real
                val contaLogada = cloudBackupRepository.getContaConectada()
                val isAutenticadoReal = if (contaLogada != null) {
                    cloudBackupRepository.testarConexao().isSuccess
                } else {
                    false
                }

                // 3. Verificação de backups locais
                val ultimoLocalReal = backupRepository.obterDataUltimoBackupLocal() ?: 0L

                // 4. Sincronizar DataStore se houver mudança na conta ou desconexão
                if (isAutenticadoReal && contaLogada != prefs.contaGoogleConectada) {
                    preferencesDataStore.salvarBackup(
                        backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                        backupNuvemAtivo = prefs.backupNuvemAtivo,
                        contaGoogle = contaLogada
                    )
                } else if (!isAutenticadoReal && prefs.contaGoogleConectada != null && contaLogada == null) {
                    // Perda de conexão ou deslogado externamente: limpa persistência da nuvem
                    preferencesDataStore.salvarBackup(
                        backupAutomaticoAtivo = false,
                        backupNuvemAtivo = false,
                        contaGoogle = "" // "" sinaliza remoção no DataStore
                    )
                    cancelarBackupNuvem()
                }

                // 5. Sincronizar data de backup local se arquivos foram removidos
                if (ultimoLocalReal == 0L && prefs.ultimoBackupLocal != 0L) {
                    preferencesDataStore.salvarBackup(
                        backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                        ultimoBackup = 0L
                    )
                }

                // 6. Atualizar UI com o estado real validado
                _uiState.update {
                    it.copy(
                        isGoogleAutenticado = isAutenticadoReal,
                        contaGoogle = if (isAutenticadoReal) contaLogada else (if (isAutenticadoReal) null else prefs.contaGoogleConectada),
                        backupNuvemAtivo = if (isAutenticadoReal) prefs.backupNuvemAtivo else false,
                        ultimoBackupLocal = if (ultimoLocalReal > 0) ultimoLocalReal else null
                    )
                }

                // 7. Carregar lista da nuvem e validar data do último backup na nuvem
                if (isAutenticadoReal) {
                    cloudBackupRepository.listarBackupsNuvem().onSuccess { backups ->
                        val ultimoNuvemReal = backups.maxByOrNull { it.modifiedTime }?.modifiedTime ?: 0L
                        
                        _uiState.update {
                            it.copy(
                                backupsNuvem = backups,
                                ultimoBackupNuvem = if (ultimoNuvemReal > 0) ultimoNuvemReal else null
                            )
                        }

                        if (ultimoNuvemReal != prefs.ultimoBackupNuvem) {
                            preferencesDataStore.salvarBackup(
                                backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                                ultimoBackupNuvem = ultimoNuvemReal
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(backupsNuvem = emptyList()) }
                }
            }
        }
    }

    private fun carregarBackupsNuvem() {
        viewModelScope.launch {
            cloudBackupRepository.listarBackupsNuvem()
                .onSuccess { backups ->
                    _uiState.update { it.copy(backupsNuvem = backups) }
                }
                .onFailure { e ->
                    Timber.e(e, "Erro ao carregar lista de backups")
                }
        }
    }

    fun onAction(action: BackupAction) {
        when (action) {
            is BackupAction.ExportarBackup -> exportarBackup(action.outputStream)
            is BackupAction.ImportarBackup -> importarBackup(action.inputStream)
            BackupAction.LimparDadosAntigos -> limparDadosAntigos()
            BackupAction.Recarregar -> carregarEstatisticas()
            BackupAction.BackupNuvemAgora -> backupNuvemAgora()
            is BackupAction.ToggleBackupNuvem -> toggleBackupNuvem(action.ativo)
            BackupAction.AutenticarGoogle -> {
                _uiState.update { it.copy(isProcessando = true, operacaoAtual = "autenticar_google") }
                viewModelScope.launch { 
                    try {
                        _eventos.emit(BackupEvent.SolicitarAutenticacaoGoogle)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
                        _eventos.emit(BackupEvent.MostrarMensagem("Erro ao iniciar autenticação"))
                    }
                }
            }
            BackupAction.DeslogarGoogle -> deslogarGoogle()
            is BackupAction.RestaurarBackupNuvem -> restaurarBackupNuvem(action.fileId)
            is BackupAction.RestaurarBackupLocal -> restaurarBackupLocal(action.nomeArquivo)
            is BackupAction.ExcluirBackupLocal -> excluirBackupLocal(action.nomeArquivo)
            BackupAction.ExcluirTodosBackupsLocais -> excluirTodosBackupsLocais()
            is BackupAction.ExcluirBackupNuvem -> excluirBackupNuvem(action.fileId)
            BackupAction.ExcluirTodosBackupsNuvem -> excluirTodosBackupsNuvem()
            is BackupAction.AlterarMesesLimpeza -> {
                _uiState.update { it.copy(mesesParaLimpeza = action.meses) }
            }
        }
    }

    /**
     * Chamado pelo fragmento/activity quando o launcher de autenticação retorna.
     */
    fun onGoogleAuthResult() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "validando_conexao") }
            
            // Aguarda um pouco para o Google Play Services atualizar o estado da conta
            kotlinx.coroutines.delay(1000)
            
            val contaLogada = cloudBackupRepository.getContaConectada()
            val sucesso = if (contaLogada != null) {
                cloudBackupRepository.testarConexao().isSuccess
            } else {
                false
            }

            if (sucesso) {
                _uiState.update { 
                    it.copy(
                        isGoogleAutenticado = true,
                        contaGoogle = contaLogada,
                        isProcessando = false,
                        operacaoAtual = null
                    )
                }
                carregarBackupsNuvem()
                _eventos.emit(BackupEvent.MostrarMensagem("Google Drive conectado com sucesso!"))
            } else {
                _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
                _eventos.emit(BackupEvent.MostrarMensagem("Não foi possível confirmar a conexão"))
            }
        }
    }

    fun iniciarExportacao() {
        viewModelScope.launch {
            _eventos.emit(BackupEvent.SolicitarDestinoExportacao)
        }
    }

    fun iniciarImportacao() {
        viewModelScope.launch {
            _eventos.emit(BackupEvent.SolicitarOrigemImportacao)
        }
    }

    private fun carregarEstatisticas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val totalEmpregos = empregoRepository.contarTodos()

                // Conta pontos usando o flow (primeiro valor)
                val pontos = pontoRepository.observarTodos().first()
                val totalPontos = pontos.size

                // Conta feriados usando a lista
                val feriados = feriadoRepository.buscarTodos()
                val totalFeriados = feriados.size

                val backupsLocais = backupRepository.obterBackupsLocais()

                // Cálculo real do tamanho dos arquivos (Banco + Fotos)
                val dbFile = context.getDatabasePath(MeuPontoDatabase.DATABASE_NAME)
                val tamanhoBanco = if (dbFile.exists()) dbFile.length() else 0L
                val tamanhoFotos = database.fotoComprovanteDao().calcularTamanhoTotal() ?: 0L
                val tamanhoEstimado = formatarTamanho(tamanhoBanco + tamanhoFotos)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalEmpregos = totalEmpregos,
                        totalPontos = totalPontos,
                        totalFeriados = totalFeriados,
                        backupsLocais = backupsLocais,
                        tamanhoEstimado = tamanhoEstimado
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar estatísticas")
                _uiState.update { it.copy(isLoading = false) }
                _eventos.emit(BackupEvent.MostrarMensagem("Erro ao carregar estatísticas"))
            }
        }
    }

    private fun formatarTamanho(bytes: Long): String {
        return when {
            bytes < 1024L -> "${bytes}B"
            bytes < 1024L * 1024L -> "${bytes / 1024L}KB"
            else -> "${bytes / (1024L * 1024L)}MB"
        }
    }

    private fun exportarBackup(outputStream: OutputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "exportar") }

            // Primeiro cria um backup local padronizado
            backupRepository.realizarBackupLocal()

            // Depois exporta para o destino solicitado pelo usuário
            backupRepository.exportarBanco(outputStream)
                .onSuccess {
                    _eventos.emit(BackupEvent.ExportacaoConcluida)
                }
                .onFailure { e ->
                    Timber.e(e, "Erro ao exportar backup")
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao exportar: ${e.message}"))
                }

            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun importarBackup(inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "importar") }

            backupRepository.importarBanco(inputStream)
                .onSuccess {
                    _eventos.emit(BackupEvent.ImportacaoConcluida)
                    carregarEstatisticas()
                }
                .onFailure { e ->
                    Timber.e(e, "Erro ao importar backup")
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao importar: ${e.message}"))
                }

            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun limparDadosAntigos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "limpar") }

            try {
                val meses = _uiState.value.mesesParaLimpeza
                val dataLimite = LocalDate.now().minus(meses.toLong(), ChronoUnit.MONTHS)
                val registrosRemovidos = pontoRepository.excluirPontosAnterioresA(dataLimite)

                _eventos.emit(BackupEvent.LimpezaConcluida(registrosRemovidos))
                carregarEstatisticas()
            } catch (e: Exception) {
                Timber.e(e, "Erro ao limpar dados")
                _eventos.emit(BackupEvent.MostrarMensagem("Erro ao limpar: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
            }
        }
    }

    private fun backupNuvemAgora() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "backup_nuvem") }

            cloudBackupRepository.uploadBackup()
                .onSuccess {
                    _eventos.emit(BackupEvent.MostrarMensagem("Backup na nuvem (com imagens) concluído!"))
                    carregarBackupsNuvem() // Atualiza a lista de backups
                }
                .onFailure { e ->
                    Timber.e(e, "Erro no backup em nuvem")
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro no backup: ${e.message}"))
                }

            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun toggleBackupNuvem(ativo: Boolean) {
        viewModelScope.launch {
            val currentPrefs = preferencesDataStore.preferenciasGlobais.first()
            preferencesDataStore.salvarBackup(
                backupAutomaticoAtivo = currentPrefs.backupAutomaticoAtivo,
                backupNuvemAtivo = ativo
            )
        }
        if (ativo) {
            agendarBackupNuvem()
        } else {
            cancelarBackupNuvem()
        }
    }

    private fun agendarBackupNuvem() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CloudBackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun cancelarBackupNuvem() {
        WorkManager.getInstance(context).cancelUniqueWork(CloudBackupWorker.WORK_NAME)
    }

    private fun restaurarBackupNuvem(fileId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "restaurar_nuvem") }

            cloudBackupRepository.downloadERestaurarBackup(fileId)
                .onSuccess {
                    _eventos.emit(BackupEvent.MostrarMensagem("Backup restaurado com sucesso!"))
                    carregarEstatisticas()
                }
                .onFailure { e ->
                    Timber.e(e, "Erro ao restaurar backup da nuvem")
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao restaurar: ${e.message}"))
                }

            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun restaurarBackupLocal(nomeArquivo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "restaurar_local") }

            backupRepository.restaurarBackupLocal(nomeArquivo)
                .onSuccess {
                    _eventos.emit(BackupEvent.MostrarMensagem("Backup restaurado com sucesso!"))
                    carregarEstatisticas()
                }
                .onFailure { e ->
                    Timber.e(e, "Erro ao restaurar backup local")
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao restaurar: ${e.message}"))
                }

            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun deslogarGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "deslogar_google") }
            cloudBackupRepository.desconectarConta()
                .onSuccess {
                    preferencesDataStore.salvarBackup(
                        backupAutomaticoAtivo = false,
                        backupNuvemAtivo = false,
                        contaGoogle = ""
                    )
                    cancelarBackupNuvem()
                    _uiState.update { 
                        it.copy(
                            isGoogleAutenticado = false, 
                            contaGoogle = null,
                            backupNuvemAtivo = false,
                            backupsNuvem = emptyList()
                        ) 
                    }
                    _eventos.emit(BackupEvent.MostrarMensagem("Conta Google desconectada"))
                }
                .onFailure { e ->
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao desconectar: ${e.message}"))
                }
            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun excluirBackupLocal(nomeArquivo: String) {
        viewModelScope.launch {
            backupRepository.excluirBackupLocal(nomeArquivo)
                .onSuccess {
                    _eventos.emit(BackupEvent.MostrarMensagem("Backup excluído"))
                    carregarEstatisticas()
                }
                .onFailure { e ->
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao excluir: ${e.message}"))
                }
        }
    }

    private fun excluirTodosBackupsLocais() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "excluir_todos_locais") }
            backupRepository.excluirTodosBackupsLocais()
                .onSuccess {
                    _eventos.emit(BackupEvent.MostrarMensagem("Todos os backups locais foram excluídos"))
                    carregarEstatisticas()
                }
                .onFailure { e ->
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao excluir todos: ${e.message}"))
                }
            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun excluirBackupNuvem(fileId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "excluir_nuvem") }
            cloudBackupRepository.excluirBackupNuvem(fileId)
                .onSuccess {
                    _eventos.emit(BackupEvent.MostrarMensagem("Backup removido da nuvem"))
                    carregarBackupsNuvem()
                }
                .onFailure { e ->
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao excluir da nuvem: ${e.message}"))
                }
            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }

    private fun excluirTodosBackupsNuvem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessando = true, operacaoAtual = "excluir_todos_nuvem") }
            cloudBackupRepository.excluirTodosBackupsNuvem()
                .onSuccess {
                    _eventos.emit(BackupEvent.MostrarMensagem("Todos os backups da nuvem foram excluídos"))
                    carregarBackupsNuvem()
                }
                .onFailure { e ->
                    _eventos.emit(BackupEvent.MostrarMensagem("Erro ao excluir todos da nuvem: ${e.message}"))
                }
            _uiState.update { it.copy(isProcessando = false, operacaoAtual = null) }
        }
    }
}
