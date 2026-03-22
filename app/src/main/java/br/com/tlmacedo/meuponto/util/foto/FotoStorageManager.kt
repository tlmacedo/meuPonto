// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/FotoStorageManager.kt
package br.com.tlmacedo.meuponto.util.foto

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.util.ComprovanteImageStorage
import br.com.tlmacedo.meuponto.util.formatarTamanho
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de armazenamento de fotos de comprovante.
 *
 * Camada de abstração que integra o pipeline completo de processamento
 * e armazenamento físico de imagens de comprovante de ponto.
 *
 * ## Responsabilidades desta classe:
 * - Processar e salvar novas fotos via [ImageProcessor]
 * - Carregar e verificar fotos via [ComprovanteImageStorage]
 * - Gerenciar ciclo de vida dos arquivos físicos no disco
 *
 * ## O que NÃO é responsabilidade desta classe:
 * - Persistir metadados no banco de dados — essa responsabilidade pertence
 *   ao [br.com.tlmacedo.meuponto.domain.usecase.foto.SalvarFotoComprovanteUseCase],
 *   que recebe o [SavePhotoResult.Success] e chama o repositório de fotos.
 *
 * ## Correções aplicadas (12.0.0):
 * - Removido [br.com.tlmacedo.meuponto.data.local.database.dao.FotoComprovanteDao]
 *   do construtor — dependência que existia mas não era usada, criando acoplamento
 *   desnecessário com a camada de dados
 * - Substituído e.printStackTrace() por Timber.e() em todos os blocos catch
 * - [getStorageStats] agora usa [Long.formatarTamanho] centralizado
 *
 * @param context Contexto da aplicação
 * @param imageStorage Gerenciador de armazenamento físico de arquivos
 * @param imageProcessor Processador de imagens (orientação, resize, compressão, EXIF)
 * @param hashCalculator Calculador de hash MD5 para verificação de integridade
 * @param exifWriter Gravador de metadados EXIF
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - Removido FotoComprovanteDao do construtor; e.printStackTrace()
 *                   substituído por Timber.e(); formatarTamanho() centralizado
 */
@Singleton
class FotoStorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageStorage: ComprovanteImageStorage,
    private val imageProcessor: ImageProcessor,
    private val hashCalculator: ImageHashCalculator,
    private val exifWriter: ExifDataWriter
) {

    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")

    // ========================================================================
    // SALVAR FOTO
    // ========================================================================

    /**
     * Processa e salva uma nova foto a partir de URI (câmera ou galeria).
     *
     * ## Pipeline executado:
     * 1. Cria arquivo de destino com nome estruturado por emprego/data
     * 2. Monta metadados EXIF (data atual, GPS opcional, identificadores)
     * 3. Processa via [ImageProcessor] (orientação, resize, compressão adaptativa)
     * 4. Retorna [SavePhotoResult.Success] com caminho relativo e metadados
     *
     * A persistência no banco deve ser feita pelo chamador usando os dados
     * retornados em [SavePhotoResult.Success].
     *
     * @param sourceUri URI da imagem de origem (content:// ou file://)
     * @param empregoId ID do emprego vinculado ao comprovante
     * @param pontoId ID do ponto ao qual a foto será associada
     * @param data Data do ponto para organização de diretórios
     * @param config Configurações do emprego (formato, qualidade, resolução, EXIF)
     * @param gpsData Coordenadas GPS para inclusão no EXIF (opcional)
     * @return [SavePhotoResult.Success] com metadados ou [SavePhotoResult.Error] com causa
     */
    suspend fun savePhoto(
        sourceUri: Uri,
        empregoId: Long,
        pontoId: Long,
        data: LocalDate,
        config: ConfiguracaoEmprego,
        gpsData: GpsData? = null
    ): SavePhotoResult = withContext(Dispatchers.IO) {
        try {
            val outputFile = createOutputFile(empregoId, pontoId, data, config)
            val exifMetadata = buildExifMetadata(empregoId, pontoId, data, config, gpsData)

            val result = imageProcessor.processImage(
                sourceUri = sourceUri,
                outputFile = outputFile,
                config = config,
                exifMetadata = exifMetadata
            )

            buildSaveResult(result, outputFile, empregoId, data)
        } catch (e: Exception) {
            Timber.e(e, "Falha ao salvar foto via URI — empregoId=$empregoId, pontoId=$pontoId")
            SavePhotoResult.Error("Erro ao salvar foto: ${e.message}")
        }
    }

    /**
     * Processa e salva uma nova foto a partir de arquivo (câmera nativa).
     *
     * Após processamento bem-sucedido, o arquivo temporário de origem é
     * deletado automaticamente se for diferente do arquivo de destino.
     *
     * @param sourceFile Arquivo de origem (geralmente temporário do câmera em cache)
     * @param empregoId ID do emprego vinculado
     * @param pontoId ID do ponto
     * @param data Data do ponto
     * @param config Configurações do emprego
     * @param gpsData Coordenadas GPS opcionais
     * @return [SavePhotoResult.Success] com metadados ou [SavePhotoResult.Error] com causa
     */
    suspend fun savePhoto(
        sourceFile: File,
        empregoId: Long,
        pontoId: Long,
        data: LocalDate,
        config: ConfiguracaoEmprego,
        gpsData: GpsData? = null
    ): SavePhotoResult = withContext(Dispatchers.IO) {
        try {
            val outputFile = createOutputFile(empregoId, pontoId, data, config)
            val exifMetadata = buildExifMetadata(empregoId, pontoId, data, config, gpsData)

            val result = imageProcessor.processImage(
                sourceFile = sourceFile,
                outputFile = outputFile,
                config = config,
                exifMetadata = exifMetadata
            )

            val saveResult = buildSaveResult(result, outputFile, empregoId, data)

            // Remove arquivo temporário de origem após processamento bem-sucedido
            if (saveResult is SavePhotoResult.Success &&
                sourceFile.absolutePath != outputFile.absolutePath
            ) {
                if (!sourceFile.delete()) {
                    Timber.w("Não foi possível remover arquivo temporário: ${sourceFile.name}")
                }
            }

            saveResult
        } catch (e: Exception) {
            Timber.e(e, "Falha ao salvar foto via arquivo — empregoId=$empregoId, pontoId=$pontoId")
            SavePhotoResult.Error("Erro ao salvar foto: ${e.message}")
        }
    }

    // ========================================================================
    // CARREGAR FOTO
    // ========================================================================

    /**
     * Carrega uma foto como [Bitmap].
     *
     * @param relativePath Caminho relativo retornado pelo [savePhoto]
     * @return [Bitmap] da imagem ou null se não encontrada ou em caso de erro
     */
    suspend fun loadPhoto(relativePath: String): Bitmap? = withContext(Dispatchers.IO) {
        imageStorage.loadBitmap(relativePath)
    }

    /**
     * Carrega thumbnail otimizada para exibição em listas.
     *
     * Usa [BitmapFactory.Options.inSampleSize] internamente para eficiência
     * de memória, sem carregar a imagem no tamanho original.
     *
     * @param relativePath Caminho relativo da foto
     * @return [Bitmap] da thumbnail ou null se não encontrada
     */
    suspend fun loadThumbnail(relativePath: String): Bitmap? = withContext(Dispatchers.IO) {
        imageStorage.loadThumbnail(relativePath)
    }

    /**
     * Retorna o [File] absoluto correspondente ao caminho relativo.
     *
     * @param relativePath Caminho relativo
     * @return [File] absoluto (pode não existir no disco)
     */
    fun getAbsoluteFile(relativePath: String): File = imageStorage.getAbsoluteFile(relativePath)

    /**
     * Verifica se uma foto existe no armazenamento físico.
     *
     * @param relativePath Caminho relativo
     * @return true se o arquivo existe e é acessível
     */
    fun exists(relativePath: String): Boolean = imageStorage.exists(relativePath)

    // ========================================================================
    // INTEGRIDADE
    // ========================================================================

    /**
     * Verifica a integridade de uma foto comparando o hash MD5.
     *
     * @param relativePath Caminho relativo da foto
     * @param expectedHash Hash MD5 armazenado no banco no momento do salvamento
     * @return [IntegrityCheckResult] com resultado da comparação e hash atual
     */
    suspend fun verifyIntegrity(
        relativePath: String,
        expectedHash: String
    ): IntegrityCheckResult = withContext(Dispatchers.IO) {
        val file = imageStorage.getAbsoluteFile(relativePath)
        imageProcessor.verifyIntegrity(file, expectedHash)
    }

    // ========================================================================
    // DELETAR
    // ========================================================================

    /**
     * Deleta uma foto do armazenamento físico.
     *
     * ATENÇÃO: Esta operação remove apenas o arquivo físico. Para exclusão
     * com soft delete e movimentação para lixeira, usar [ImageTrashManager].
     *
     * @param relativePath Caminho relativo da foto
     * @return true se deletado com sucesso ou se o arquivo já não existia
     */
    suspend fun deletePhoto(relativePath: String): Boolean = withContext(Dispatchers.IO) {
        imageStorage.delete(relativePath)
    }

    /**
     * Deleta todas as fotos de um emprego recursivamente.
     *
     * Deve ser chamado quando o emprego for excluído pelo usuário,
     * após o soft delete e confirmação do período de retenção.
     *
     * @param empregoId ID do emprego
     * @return Número de arquivos físicos deletados
     */
    suspend fun deleteAllForEmprego(empregoId: Long): Int = withContext(Dispatchers.IO) {
        imageStorage.deleteAllForEmprego(empregoId)
    }

    // ========================================================================
    // ESTATÍSTICAS E MANUTENÇÃO
    // ========================================================================

    /**
     * Retorna estatísticas de uso do armazenamento de fotos.
     *
     * @return [StorageStats] com contagem total e tamanho formatado
     */
    suspend fun getStorageStats(): StorageStats = withContext(Dispatchers.IO) {
        val totalBytes = imageStorage.getTotalStorageSize()
        StorageStats(
            totalImages = imageStorage.getTotalImageCount(),
            totalSizeBytes = totalBytes,
            // Usa Long.formatarTamanho() centralizado em FileExtensions.kt
            // em vez de lógica inline duplicada
            totalSizeFormatted = totalBytes.formatarTamanho()
        )
    }

    /**
     * Remove arquivos físicos sem registro correspondente no banco (órfãos).
     *
     * @param validPaths Conjunto de caminhos relativos ativos no banco de dados
     * @return Número de arquivos órfãos removidos
     */
    suspend fun cleanupOrphanFiles(validPaths: Set<String>): Int = withContext(Dispatchers.IO) {
        imageStorage.cleanupOrphanImages(validPaths)
    }

    // ========================================================================
    // HELPERS PRIVADOS
    // ========================================================================

    /**
     * Cria o arquivo de destino com nome e estrutura padronizados.
     *
     * Estrutura: `emprego_{id}/{ano}/{mes}/ponto_{pontoId}_{timestamp}.{ext}`
     *
     * @param empregoId ID do emprego
     * @param pontoId ID do ponto
     * @param data Data do ponto para composição do caminho
     * @param config Configurações do emprego (define a extensão via [FotoFormato])
     * @return [File] de destino pronto para receber a imagem processada
     */
    private fun createOutputFile(
        empregoId: Long,
        pontoId: Long,
        data: LocalDate,
        config: ConfiguracaoEmprego
    ): File {
        val directory = getOrCreateDirectory(empregoId, data)
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val extension = config.fotoFormato.extensao
        val fileName = "ponto_${pontoId}_$timestamp.$extension"
        return File(directory, fileName)
    }

    /**
     * Retorna ou cria o diretório organizado por emprego/ano/mês.
     *
     * @param empregoId ID do emprego
     * @param data Data para composição da hierarquia de diretórios
     * @return [File] do diretório criado e garantido como existente
     */
    private fun getOrCreateDirectory(empregoId: Long, data: LocalDate): File {
        val path = "emprego_$empregoId/${data.year}/${String.format("%02d", data.monthValue)}"
        return File(imageStorage.getComprovantesDirectory(), path).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Constrói o caminho relativo a partir dos componentes.
     *
     * @param empregoId ID do emprego
     * @param data Data do ponto
     * @param fileName Nome do arquivo já gerado
     * @return Caminho relativo no formato `emprego_{id}/{ano}/{mes}/{fileName}`
     */
    private fun getRelativePath(empregoId: Long, data: LocalDate, fileName: String): String {
        return "emprego_$empregoId/${data.year}/${String.format("%02d", data.monthValue)}/$fileName"
    }

    /**
     * Monta os metadados EXIF a partir das configurações e dados disponíveis.
     *
     * Inclui GPS apenas se [ConfiguracaoEmprego.fotoIncluirLocalizacaoExif] for
     * verdadeiro e [gpsData] não for nulo.
     *
     * @param empregoId ID do emprego para comentário EXIF
     * @param pontoId ID do ponto para comentário EXIF
     * @param data Data do ponto para descrição EXIF
     * @param config Configurações que controlam inclusão de GPS
     * @param gpsData Coordenadas GPS opcionais
     * @return [FotoExifMetadata] pronto para ser gravado pelo [ExifDataWriter]
     */
    private fun buildExifMetadata(
        empregoId: Long,
        pontoId: Long,
        data: LocalDate,
        config: ConfiguracaoEmprego,
        gpsData: GpsData?
    ): FotoExifMetadata {
        val userComment = "ponto:$pontoId;emprego:$empregoId"
        val description = "Comprovante de ponto - $data"

        return if (config.fotoIncluirLocalizacaoExif && gpsData != null) {
            FotoExifMetadata(
                dateTime = LocalDateTime.now(),
                latitude = gpsData.latitude,
                longitude = gpsData.longitude,
                altitude = gpsData.altitude,
                userComment = userComment,
                description = description
            )
        } else {
            FotoExifMetadata(
                dateTime = LocalDateTime.now(),
                userComment = userComment,
                description = description
            )
        }
    }

    /**
     * Converte [ImageProcessingResult] em [SavePhotoResult].
     *
     * Em caso de sucesso, deleta o arquivo de saída se o processamento
     * falhar em etapas posteriores para evitar arquivos parciais no disco.
     *
     * @param result Resultado do processamento pelo [ImageProcessor]
     * @param outputFile Arquivo de destino já criado
     * @param empregoId ID do emprego para composição do caminho relativo
     * @param data Data do ponto para composição do caminho relativo
     * @return [SavePhotoResult] correspondente ao resultado
     */
    private fun buildSaveResult(
        result: ImageProcessingResult,
        outputFile: File,
        empregoId: Long,
        data: LocalDate
    ): SavePhotoResult {
        return when (result) {
            is ImageProcessingResult.Success -> {
                val relativePath = getRelativePath(empregoId, data, outputFile.name)
                SavePhotoResult.Success(
                    relativePath = relativePath,
                    absolutePath = outputFile.absolutePath,
                    sizeBytes = result.sizeBytes,
                    hashMd5 = result.hashMd5,
                    width = result.finalWidth,
                    height = result.finalHeight,
                    wasResized = result.wasResized,
                    finalQuality = result.finalQuality
                )
            }
            is ImageProcessingResult.Error -> {
                // Remove arquivo parcial do disco para não deixar lixo
                if (outputFile.exists()) outputFile.delete()
                SavePhotoResult.Error(result.message)
            }
        }
    }
}

// ============================================================================
// DATA CLASSES E SEALED CLASSES DE SUPORTE
// ============================================================================

/**
 * Dados de localização GPS capturados no momento do ponto.
 *
 * @property latitude Latitude em graus decimais
 * @property longitude Longitude em graus decimais
 * @property altitude Altitude em metros (opcional)
 * @property accuracy Precisão da leitura em metros (opcional)
 */
data class GpsData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Float? = null
)

/**
 * Resultado do salvamento de uma foto de comprovante.
 *
 * Em caso de [Success], os dados devem ser persistidos no banco pelo
 * [br.com.tlmacedo.meuponto.domain.usecase.foto.SalvarFotoComprovanteUseCase].
 */
sealed class SavePhotoResult {

    /**
     * Foto salva com sucesso no disco.
     *
     * @property relativePath Caminho relativo para armazenar no banco
     * @property absolutePath Caminho absoluto para uso imediato (não persistir)
     * @property sizeBytes Tamanho final em bytes após processamento
     * @property hashMd5 Hash MD5 para verificação de integridade futura
     * @property width Largura final em pixels (após redimensionamento)
     * @property height Altura final em pixels (após redimensionamento)
     * @property wasResized true se a imagem foi redimensionada durante o processo
     * @property finalQuality Qualidade JPEG final aplicada (pode diferir do configurado)
     */
    data class Success(
        val relativePath: String,
        val absolutePath: String,
        val sizeBytes: Long,
        val hashMd5: String,
        val width: Int,
        val height: Int,
        val wasResized: Boolean,
        val finalQuality: Int
    ) : SavePhotoResult() {

        /**
         * Tamanho formatado usando [Long.formatarTamanho] centralizado.
         * Corrigido: substituída a lógica inline duplicada em 12.0.0.
         */
        val sizeFormatted: String get() = sizeBytes.formatarTamanho()

        /** Dimensões finais formatadas para exibição */
        val dimensoesFormatadas: String get() = "${width}x${height}"
    }

    /**
     * Falha no salvamento da foto.
     *
     * @property message Descrição do erro para log e feedback ao usuário
     */
    data class Error(val message: String) : SavePhotoResult()

    /** true se a operação foi bem-sucedida */
    val isSuccess: Boolean get() = this is Success

    /** true se ocorreu um erro */
    val isError: Boolean get() = this is Error

    /** Retorna [Success] ou null */
    fun getOrNull(): Success? = this as? Success

    /** Retorna a mensagem de erro ou null */
    fun errorMessageOrNull(): String? = (this as? Error)?.message
}

/**
 * Estatísticas de uso do armazenamento de fotos.
 *
 * @property totalImages Número total de imagens armazenadas
 * @property totalSizeBytes Tamanho total em bytes
 * @property totalSizeFormatted Tamanho total formatado para exibição
 */
data class StorageStats(
    val totalImages: Int,
    val totalSizeBytes: Long,
    val totalSizeFormatted: String
) {
    /** true se não há nenhuma imagem armazenada */
    val isEmpty: Boolean get() = totalImages == 0
}