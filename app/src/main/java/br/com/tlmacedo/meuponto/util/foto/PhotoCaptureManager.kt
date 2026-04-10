// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/PhotoCaptureManager.kt
package br.com.tlmacedo.meuponto.util.foto

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.FotoOrigem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber // Importação adicionada
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de captura de fotos de comprovante.
 *
 * Coordena o fluxo completo de captura via câmera ou seleção da galeria,
 * gerenciando arquivos temporários, estado da captura e delegando o
 * processamento para [FotoStorageManager].
 *
 * ## Fluxo de captura via câmera:
 * 1. Chamar [prepareForCameraCapture] — cria arquivo temporário e retorna URI
 * 2. Usar o URI com [ActivityResultContracts.TakePicture]
 * 3. Se captura bem-sucedida: chamar [onCameraCaptureSuccess]
 * 4. Se cancelado: chamar [onCameraCaptureCancelled]
 * 5. Chamar [processAndSavePhoto] com os dados do ponto
 *
 * ## Fluxo de seleção via galeria:
 * 1. Usar [ActivityResultContracts.PickVisualMedia] ou [GetContent]
 * 2. Com o URI retornado: chamar [onGalleryPhotoSelected]
 * 3. Chamar [processAndSavePhoto] com os dados do ponto
 *
 * ## Correções aplicadas (12.0.0):
 * - [prepareForCameraCapture]: e.printStackTrace() substituído por Timber.e()
 * - [processAndSavePhoto]: e.printStackTrace() substituído por Timber.e()
 * - [cleanupTempFile]: catch silencioso ("// Ignora erro na limpeza") substituído
 *   por Timber.w() para rastreamento de falhas de limpeza
 * - [cleanupOldTempFiles]: catch silencioso substituído por Timber.w()
 * - Adicionado [cleanupOldTempFiles] no bloco init do singleton para eliminar
 *   arquivos temporários acumulados de sessões anteriores
 *
 * @param context Contexto da aplicação
 * @param storageManager Gerenciador de armazenamento de fotos
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() → Timber; catch silencioso → Timber.w();
 *                   limpeza de temporários antigos na inicialização do singleton
 */
@Singleton
class PhotoCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageManager: FotoStorageManager
) {

    companion object {
        /**
         * Diretório de cache para arquivos temporários da câmera.
         * Deve coincidir com o path declarado em file_paths.xml:
         * `<cache-path name="temp_camera" path="temp_camera/" />`
         */
        private const val TEMP_DIR = "temp_camera"

        /** Sufixo da authority do FileProvider — deve coincidir com AndroidManifest.xml */
        private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

        /** Tempo máximo de retenção de arquivos temporários em minutos */
        private const val TEMP_FILE_MAX_AGE_MINUTES = 60
    }

    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")

    /** Estado observável do fluxo de captura */
    private val _captureState = MutableStateFlow<CaptureState>(CaptureState.Idle)

    /**
     * Estado atual da captura.
     * Observe via [StateFlow] na UI para reagir às transições de estado.
     */
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    /** Arquivo temporário atual durante captura via câmera */
    private var currentTempFile: File? = null

    /** URI do arquivo temporário atual para uso com ActivityResultLauncher */
    private var currentTempUri: Uri? = null

    init {
        // Limpa arquivos temporários de sessões anteriores ao inicializar o singleton.
        // Evita acúmulo de arquivos no cache entre sessões do app.
        cleanupOldTempFiles(TEMP_FILE_MAX_AGE_MINUTES)
    }

    // ========================================================================
    // CAPTURA VIA CÂMERA
    // ========================================================================

    /**
     * Prepara para captura via câmera.
     *
     * Cria um arquivo temporário em cache e retorna o URI para uso com
     * [ActivityResultContracts.TakePicture]. O URI usa [FileProvider] para
     * compartilhamento seguro com o app de câmera do sistema.
     *
     * @return URI para passar ao Intent da câmera, ou null em caso de erro
     */
    fun prepareForCameraCapture(): Uri? {
        return try {
            cleanupTempFile() // Limpa arquivo anterior se existir

            val tempDir = File(context.cacheDir, TEMP_DIR).apply {
                if (!exists()) mkdirs()
            }

            val timestamp = LocalDateTime.now().format(timestampFormatter)
            val fileName = "CAPTURE_$timestamp.jpg"
            currentTempFile = File(tempDir, fileName)

            val authority = "${context.packageName}$FILE_PROVIDER_SUFFIX"
            currentTempUri = FileProvider.getUriForFile(context, authority, currentTempFile!!)

            _captureState.value = CaptureState.WaitingForCamera(currentTempUri!!)

            Timber.d("Câmera preparada: ${currentTempFile!!.name}")
            currentTempUri
        } catch (e: Exception) {
            Timber.e(e, "Falha ao preparar captura via câmera") // Correção aqui
            _captureState.value = CaptureState.Error("Erro ao preparar câmera: ${e.message}")
            null
        }
    }

    /**
     * Chamado quando a captura via câmera foi bem-sucedida.
     *
     * Verifica se o arquivo temporário existe e tem conteúdo antes de
     * transicionar para [CaptureState.PhotoCaptured].
     */
    fun onCameraCaptureSuccess() {
        val file = currentTempFile
        val uri = currentTempUri

        if (file == null || uri == null) {
            Timber.e("onCameraCaptureSuccess: arquivo temporário ou URI não encontrado")
            _captureState.value = CaptureState.Error("Arquivo temporário não encontrado")
            return
        }

        if (!file.exists() || file.length() == 0L) {
            Timber.w("onCameraCaptureSuccess: arquivo vazio ou inexistente: ${file.name}")
            _captureState.value = CaptureState.Error("Arquivo de captura vazio ou inexistente")
            cleanupTempFile()
            return
        }

        Timber.d("Captura via câmera bem-sucedida: ${file.name} (${file.length()} bytes)")
        _captureState.value = CaptureState.PhotoCaptured(
            uri = uri,
            file = file,
            source = FotoOrigem.CAMERA
        )
    }

    /**
     * Chamado quando a captura via câmera foi cancelada pelo usuário.
     *
     * Limpa o arquivo temporário e transiciona para [CaptureState.Cancelled].
     */
    fun onCameraCaptureCancelled() {
        Timber.d("Captura via câmera cancelada pelo usuário")
        cleanupTempFile()
        _captureState.value = CaptureState.Cancelled
    }

    // ========================================================================
    // SELEÇÃO VIA GALERIA
    // ========================================================================

    /**
     * Chamado quando uma foto foi selecionada da galeria.
     *
     * @param uri URI da foto selecionada pelo usuário (content://)
     */
    fun onGalleryPhotoSelected(uri: Uri) {
        Timber.d("Foto selecionada da galeria: $uri")
        _captureState.value = CaptureState.PhotoCaptured(
            uri = uri,
            file = null, // Galeria não usa arquivo temporário local
            source = FotoOrigem.GALERIA
        )
    }

    // ========================================================================
    // PROCESSAMENTO E SALVAMENTO
    // ========================================================================

    /**
     * Processa e salva a foto capturada ou selecionada.
     *
     * Deve ser chamado após o estado ser [CaptureState.PhotoCaptured].
     * Delega o processamento para [FotoStorageManager] e atualiza o estado.
     *
     * ATENÇÃO: Esta função não persiste metadados no banco de dados.
     * O [SavePhotoResult.Success] retornado deve ser usado pelo use case
     * chamador para persistir os dados via repositório.
     *
     * @param empregoId ID do emprego vinculado ao ponto
     * @param pontoId ID do ponto ao qual a foto será associada
     * @param data Data do ponto para organização de diretórios
     * @param config Configurações do emprego para o pipeline de processamento
     * @param gpsData Dados de localização GPS opcionais para inclusão no EXIF
     * @return [SavePhotoResult.Success] com metadados ou [SavePhotoResult.Error]
     */
    suspend fun processAndSavePhoto(
        empregoId: Long,
        pontoId: Long,
        data: LocalDate,
        config: ConfiguracaoEmprego,
        gpsData: GpsData? = null
    ): SavePhotoResult {
        val currentState = _captureState.value

        if (currentState !is CaptureState.PhotoCaptured) {
            return SavePhotoResult.Error("Nenhuma foto capturada para processar")
        }

        _captureState.value = CaptureState.Processing

        return try {
            val result = when (currentState.source) {
                FotoOrigem.CAMERA -> {
                    currentState.file?.let { file ->
                        storageManager.savePhoto(
                            sourceFile = file,
                            empregoId = empregoId,
                            pontoId = pontoId,
                            data = data,
                            config = config,
                            gpsData = gpsData
                        )
                    } ?: SavePhotoResult.Error("Arquivo temporário da câmera não disponível")
                }
                FotoOrigem.GALERIA -> {
                    storageManager.savePhoto(
                        sourceUri = currentState.uri,
                        empregoId = empregoId,
                        pontoId = pontoId,
                        data = data,
                        config = config,
                        gpsData = gpsData
                    )
                }
                else -> SavePhotoResult.Error("Origem da foto inválida: ${currentState.source}")
            }

            when (result) {
                is SavePhotoResult.Success -> {
                    Timber.i("Foto salva com sucesso: ${result.relativePath}")
                    _captureState.value = CaptureState.Completed(result)
                    cleanupTempFile()
                }
                is SavePhotoResult.Error -> {
                    Timber.e("Falha ao salvar foto: ${result.message}") // Correção aqui
                    _captureState.value = CaptureState.Error(result.message)
                }
            }

            result
        } catch (e: Exception) {
            Timber.e(e, "Exceção não tratada ao processar foto") // Correção aqui
            val error = SavePhotoResult.Error("Erro ao processar foto: ${e.message}")
            _captureState.value = CaptureState.Error(error.message)
            error
        }
    }

    // ========================================================================
    // CONSULTAS DE ESTADO
    // ========================================================================

    /**
     * Verifica se há uma foto capturada pronta para processar.
     *
     * @return true se o estado atual é [CaptureState.PhotoCaptured]
     */
    fun hasPhotoCaptured(): Boolean = _captureState.value is CaptureState.PhotoCaptured

    /**
     * Retorna a origem da foto capturada.
     *
     * @return [FotoOrigem] ou null se não houver foto capturada
     */
    fun getCapturedPhotoSource(): FotoOrigem? =
        (_captureState.value as? CaptureState.PhotoCaptured)?.source

    /**
     * Retorna o URI da foto capturada para exibição de preview.
     *
     * @return URI ou null se não houver foto capturada
     */
    fun getCapturedPhotoUri(): Uri? =
        (_captureState.value as? CaptureState.PhotoCaptured)?.uri

    // ========================================================================
    // RESET E LIMPEZA
    // ========================================================================

    /**
     * Reseta o estado para [CaptureState.Idle] e limpa arquivos temporários.
     *
     * Deve ser chamado quando a tela de captura for fechada sem conclusão,
     * ou quando o usuário cancelar o fluxo manualmente.
     */
    fun reset() {
        cleanupTempFile()
        _captureState.value = CaptureState.Idle
        Timber.d("PhotoCaptureManager resetado para Idle")
    }

    /**
     * Remove arquivos temporários antigos do diretório de cache.
     *
     * Chamado automaticamente no [init] do singleton para limpar resquícios
     * de sessões anteriores do app.
     *
     * Corrigido em 12.0.0: catch silencioso substituído por Timber.w()
     * para rastreamento de falhas de limpeza acumuladas.
     *
     * @param maxAgeMinutes Idade máxima dos arquivos em minutos (padrão: 60)
     * @return Número de arquivos temporários removidos
     */
    fun cleanupOldTempFiles(maxAgeMinutes: Int = TEMP_FILE_MAX_AGE_MINUTES): Int {
        return try {
            val tempDir = File(context.cacheDir, TEMP_DIR)
            if (!tempDir.exists()) return 0

            val maxAgeMillis = maxAgeMinutes * 60 * 1000L
            val cutoffTime = System.currentTimeMillis() - maxAgeMillis
            var count = 0

            tempDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        count++
                        Timber.d("Arquivo temporário antigo removido: ${file.name}")
                    } else {
                        Timber.w("Não foi possível remover arquivo temporário: ${file.name}") // Correção aqui
                    }
                }
            }

            if (count > 0) {
                Timber.i("Limpeza de temporários: $count arquivo(s) removido(s)")
            }
            count
        } catch (e: Exception) {
            Timber.w(e, "Falha na limpeza de arquivos temporários antigos") // Correção aqui
            0
        }
    }

    // ========================================================================
    // HELPERS PRIVADOS
    // ========================================================================

    /**
     * Remove o arquivo temporário atual da câmera e limpa as referências.
     *
     * Corrigido em 12.0.0: o catch silencioso original ("// Ignora erro na limpeza")
     * foi substituído por Timber.w() para que falhas de limpeza sejam rastreadas
     * no Crashlytics em produção sem interromper o fluxo.
     */
    private fun cleanupTempFile() {
        currentTempFile?.let { file ->
            try {
                if (file.exists()) {
                    val deleted = file.delete()
                    if (!deleted) {
                        Timber.w("Não foi possível remover arquivo temporário: ${file.name}") // Correção aqui
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Falha ao remover arquivo temporário: ${file.name}") // Correção aqui
            }
        }
        currentTempFile = null
        currentTempUri = null
    }
}

// ============================================================================
// SEALED CLASS DE ESTADOS
// ============================================================================

/**
 * Estados possíveis do fluxo de captura de foto.
 *
 * O fluxo esperado é:
 * [Idle] → [WaitingForCamera] → [PhotoCaptured] → [Processing] → [Completed]
 *                                                               ↘ [Error]
 * Qualquer estado pode transicionar para [Cancelled] ou [Error].
 */
sealed class CaptureState {

    /** Aguardando início da captura — estado inicial */
    object Idle : CaptureState()

    /**
     * Aguardando retorno da câmera do sistema.
     *
     * @property uri URI do arquivo temporário passado para a câmera
     */
    data class WaitingForCamera(val uri: Uri) : CaptureState()

    /**
     * Foto capturada ou selecionada, pronta para processamento.
     *
     * @property uri URI da foto (câmera ou galeria)
     * @property file Arquivo temporário (apenas para câmera; null para galeria)
     * @property source Origem da foto ([FotoOrigem.CAMERA] ou [FotoOrigem.GALERIA])
     */
    data class PhotoCaptured(
        val uri: Uri,
        val file: File?,
        val source: FotoOrigem
    ) : CaptureState()

    /** Processando a foto (compressão, orientação, EXIF, hash) */
    object Processing : CaptureState()

    /**
     * Foto processada e salva com sucesso.
     *
     * @property result Resultado com caminho relativo e metadados para persistência
     */
    data class Completed(val result: SavePhotoResult.Success) : CaptureState()

    /** Captura cancelada pelo usuário */
    object Cancelled : CaptureState()

    /**
     * Erro no fluxo de captura ou processamento.
     *
     * @property message Descrição do erro para exibição ao usuário
     */
    data class Error(val message: String) : CaptureState()

    /** true se está em estado ativo (não idle, não completado, não cancelado) */
    val isActive: Boolean
        get() = this is WaitingForCamera || this is PhotoCaptured || this is Processing

    /** true se o fluxo foi concluído (com sucesso ou com erro) */
    val isFinished: Boolean
        get() = this is Completed || this is Error || this is Cancelled
}