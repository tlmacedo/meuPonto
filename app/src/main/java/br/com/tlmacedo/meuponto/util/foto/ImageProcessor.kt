// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ImageProcessor.kt
package br.com.tlmacedo.meuponto.util.foto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.FotoFormato
import br.com.tlmacedo.meuponto.util.formatarTamanho // Importação adicionada
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processador central de imagens de comprovante.
 *
 * Orquestra todas as operações de processamento de imagem em um único
 * pipeline configurável, aplicando as definições do emprego.
 *
 * ## Pipeline de processamento:
 * 1. Carregamento com correção de orientação EXIF (configurável)
 * 2. Redimensionamento se exceder [ConfiguracaoEmprego.fotoResolucaoMaxima]
 * 3. Compressão adaptativa JPEG ou fixa PNG conforme [ConfiguracaoEmprego.fotoFormato]
 * 4. Gravação de metadados EXIF (apenas JPEG, configurável)
 * 5. Cálculo de hash MD5 para integridade
 * 6. Retorno de [ImageProcessingResult.Success] com todos os metadados
 *
 * ## Correções aplicadas (12.0.0):
 * - e.printStackTrace() substituído por Timber.e() em todos os blocos catch
 * - [ImageProcessingResult.Success.sizeFormatted] usa [Long.formatarTamanho] centralizado
 * - Contrato de ciclo de vida do [originalBitmap] documentado explicitamente:
 *   o bitmap original NÃO é reciclado internamente — responsabilidade do chamador.
 *   O bitmap intermediário (redimensionado) É reciclado no bloco finally.
 *
 * @param context Contexto da aplicação para acesso ao ContentResolver
 * @param resizer Utilitário de redimensionamento
 * @param compressor Utilitário de compressão
 * @param orientationCorrector Corretor de orientação EXIF
 * @param hashCalculator Calculador de hash MD5
 * @param exifWriter Gravador de metadados EXIF
 *
 * @author Thiago
 * @since 10.0.0
 * @updated 12.0.0 - e.printStackTrace() → Timber.e(); sizeFormatted centralizado;
 *                   contrato de ciclo de vida do bitmap documentado
 */
@Singleton
class ImageProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resizer: ImageResizer,
    private val compressor: ImageCompressor,
    private val orientationCorrector: ImageOrientationCorrector,
    private val hashCalculator: ImageHashCalculator,
    private val exifWriter: ExifDataWriter
) {

    // ========================================================================
    // PROCESSAMENTO PRINCIPAL
    // ========================================================================

    /**
     * Processa uma imagem de URI aplicando todas as configurações do emprego.
     *
     * @param sourceUri URI da imagem de origem (content:// ou file://)
     * @param outputFile Arquivo de destino para a imagem processada
     * @param config Configurações do emprego que controlam o pipeline
     * @param exifMetadata Metadados EXIF a gravar (opcional, apenas JPEG)
     * @return [ImageProcessingResult.Success] com metadados ou [ImageProcessingResult.Error]
     */
    fun processImage(
        sourceUri: Uri,
        outputFile: File,
        config: ConfiguracaoEmprego,
        exifMetadata: FotoExifMetadata? = null
    ): ImageProcessingResult {
        var originalBitmap: Bitmap? = null // Declaração para controle de reciclagem
        return try {
            originalBitmap = if (config.fotoCorrecaoOrientacao) {
                orientationCorrector.loadBitmapWithCorrectOrientation(sourceUri)
            } else {
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }

            if (originalBitmap == null) {
                return ImageProcessingResult.Error("Falha ao carregar imagem do URI: $sourceUri")
            }

            processAndSave(originalBitmap, outputFile, config, exifMetadata)
        } catch (e: Exception) {
            Timber.e(e, "Erro no processamento da imagem do URI: $sourceUri")
            ImageProcessingResult.Error("Erro no processamento: ${e.message}")
        } finally {
            // Recicla o bitmap original se ele foi criado nesta função e não foi passado para processAndSave
            // (processAndSave tem seu próprio controle para o bitmap intermediário)
            originalBitmap?.recycle()
        }
    }

    /**
     * Processa uma imagem de arquivo aplicando todas as configurações do emprego.
     *
     * @param sourceFile Arquivo de origem
     * @param outputFile Arquivo de destino
     * @param config Configurações do emprego
     * @param exifMetadata Metadados EXIF a gravar (opcional, apenas JPEG)
     * @return [ImageProcessingResult.Success] com metadados ou [ImageProcessingResult.Error]
     */
    fun processImage(
        sourceFile: File,
        outputFile: File,
        config: ConfiguracaoEmprego,
        exifMetadata: FotoExifMetadata? = null
    ): ImageProcessingResult {
        var originalBitmap: Bitmap? = null // Declaração para controle de reciclagem
        return try {
            originalBitmap = if (config.fotoCorrecaoOrientacao) {
                orientationCorrector.loadBitmapWithCorrectOrientation(sourceFile)
            } else {
                BitmapFactory.decodeFile(sourceFile.absolutePath)
            }

            if (originalBitmap == null) {
                return ImageProcessingResult.Error("Falha ao carregar imagem do arquivo: ${sourceFile.name}")
            }

            processAndSave(originalBitmap, outputFile, config, exifMetadata)
        } catch (e: Exception) {
            Timber.e(e, "Erro no processamento da imagem do arquivo: ${sourceFile.name}")
            ImageProcessingResult.Error("Erro no processamento: ${e.message}")
        } finally {
            // Recicla o bitmap original se ele foi criado nesta função e não foi passado para processAndSave
            originalBitmap?.recycle()
        }
    }

    // ========================================================================
    // PIPELINE INTERNO
    // ========================================================================

    /**
     * Executa o pipeline completo de processamento e salvamento.
     *
     * ## Contrato de ciclo de vida dos bitmaps:
     * - [originalBitmap]: NÃO é reciclado por esta função. Responsabilidade do
     *   chamador (geralmente [processImage] que deve reciclar após o retorno).
     * - bitmap intermediário (redimensionado): É reciclado no bloco `finally`
     *   se for diferente do [originalBitmap].
     *
     * @param originalBitmap Bitmap carregado e com orientação corrigida
     * @param outputFile Arquivo de destino
     * @param config Configurações do emprego
     * @param exifMetadata Metadados EXIF opcionais
     * @return Resultado do processamento
     */
    private fun processAndSave(
        originalBitmap: Bitmap,
        outputFile: File,
        config: ConfiguracaoEmprego,
        exifMetadata: FotoExifMetadata?
    ): ImageProcessingResult {
        var bitmap = originalBitmap
        var wasResized = false

        try {
            // Etapa 1: Redimensionar se exceder a resolução máxima configurada
            val maxDimension = config.fotoResolucaoMaxima
            if (maxDimension > 0 && resizer.needsResize(bitmap.width, bitmap.height, maxDimension)) {
                val resizedBitmap = resizer.resizeToFit(bitmap, maxDimension)
                if (resizedBitmap !== bitmap) {
                    // Não recicla originalBitmap — apenas substitui a referência local
                    bitmap = resizedBitmap
                    wasResized = true
                }
            }

            // Etapa 2: Determinar formato de compressão
            val format = when (config.fotoFormato) {
                FotoFormato.PNG -> Bitmap.CompressFormat.PNG
                else -> Bitmap.CompressFormat.JPEG
            }

            // Etapa 3: Calcular limite de tamanho
            val maxSizeBytes = if (config.fotoTamanhoMaximoKb > 0) {
                config.fotoTamanhoMaximoKb * 1024L
            } else {
                Long.MAX_VALUE
            }

            // Etapa 4: Comprimir e salvar
            val compressionResult = when {
                format == Bitmap.CompressFormat.JPEG && maxSizeBytes < Long.MAX_VALUE -> {
                    // Compressão adaptativa para JPEG com limite de tamanho configurado
                    compressor.saveToFileAdaptive(
                        bitmap = bitmap,
                        outputFile = outputFile,
                        maxSizeBytes = maxSizeBytes,
                        initialQuality = config.fotoQualidade
                    )
                }
                else -> {
                    // Compressão fixa para PNG ou JPEG sem limite de tamanho
                    val success = compressor.saveToFile(bitmap, outputFile, format, config.fotoQualidade)
                    if (success) {
                        AdaptiveCompressionResult(
                            data = ByteArray(0), // Dados já gravados no arquivo
                            finalQuality = config.fotoQualidade,
                            sizeBytes = outputFile.length(),
                            targetAchieved = true
                        )
                    } else null
                }
            }

            if (compressionResult == null) {
                return ImageProcessingResult.Error("Falha na compressão da imagem")
            }

            // Etapa 5: Gravar metadados EXIF (somente JPEG)
            if (format == Bitmap.CompressFormat.JPEG && exifMetadata != null) {
                val exifWritten = exifWriter.writeMetadata(outputFile, exifMetadata)
                if (!exifWritten) {
                    Timber.w("Falha ao gravar metadados EXIF em: ${outputFile.name}")
                }
            }

            // Etapa 6: Calcular hash MD5 do arquivo final
            val hash = hashCalculator.calculateMd5(outputFile)
                ?: return ImageProcessingResult.Error("Falha ao calcular hash MD5")

            return ImageProcessingResult.Success(
                file = outputFile,
                originalWidth = originalBitmap.width,
                originalHeight = originalBitmap.height,
                finalWidth = bitmap.width,
                finalHeight = bitmap.height,
                wasResized = wasResized,
                finalQuality = compressionResult.finalQuality,
                sizeBytes = outputFile.length(),
                hashMd5 = hash,
                format = config.fotoFormato
            )
        } finally {
            // Recicla o bitmap intermediário se for diferente do original.
            // O originalBitmap NÃO é reciclado aqui — responsabilidade do chamador.
            if (bitmap !== originalBitmap) {
                bitmap.recycle()
            }
        }
    }

    // ========================================================================
    // FUNÇÕES AUXILIARES
    // ========================================================================

    /**
     * Processa uma imagem rapidamente para preview sem salvar em disco.
     *
     * Útil para exibir preview antes da confirmação do usuário.
     * Não aplica o pipeline completo — apenas orientação e resize.
     *
     * @param sourceUri URI da imagem
     * @param maxDimension Dimensão máxima do preview (padrão: 1024px)
     * @param correctOrientation Se deve corrigir orientação EXIF (padrão: true)
     * @return Bitmap do preview ou null em caso de erro
     */
    fun processForPreview(
        sourceUri: Uri,
        maxDimension: Int = 1024,
        correctOrientation: Boolean = true
    ): Bitmap? {
        var loadedBitmap: Bitmap? = null // Declaração para controle de reciclagem
        return try {
            loadedBitmap = if (correctOrientation) {
                orientationCorrector.loadBitmapWithCorrectOrientation(sourceUri)
            } else {
                resizer.loadAndResize(sourceUri, maxDimension)
            }

            loadedBitmap?.let { bmp ->
                if (resizer.needsResize(bmp.width, bmp.height, maxDimension)) {
                    val resized = resizer.resizeToFit(bmp, maxDimension)
                    if (resized !== bmp) bmp.recycle() // Recicla o bitmap original se um novo foi criado
                    resized
                } else {
                    bmp
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao processar preview do URI: $sourceUri")
            null
        } finally {
            // Não recicla loadedBitmap aqui, pois ele pode ter sido retornado ou reciclado internamente pelo resizer.
            // A responsabilidade de reciclagem do bitmap final retornado é do chamador.
        }
    }

    /**
     * Cria thumbnail de uma imagem de arquivo.
     *
     * @param sourceFile Arquivo de origem
     * @param size Tamanho máximo da thumbnail (padrão: [ImageResizer.THUMBNAIL_SIZE])
     * @param correctOrientation Se deve corrigir orientação EXIF (padrão: true)
     * @return Bitmap da thumbnail ou null em caso de erro
     */
    fun createThumbnail(
        sourceFile: File,
        size: Int = ImageResizer.THUMBNAIL_SIZE,
        correctOrientation: Boolean = true
    ): Bitmap? {
        var loadedBitmap: Bitmap? = null // Declaração para controle de reciclagem
        return try {
            loadedBitmap = if (correctOrientation) {
                orientationCorrector.loadBitmapWithCorrectOrientation(sourceFile)
            } else {
                BitmapFactory.decodeFile(sourceFile.absolutePath)
            }

            loadedBitmap?.let { resizer.createThumbnail(it, size) }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao criar thumbnail do arquivo: ${sourceFile.name}")
            null
        } finally {
            // Não recicla loadedBitmap aqui, pois ele pode ter sido retornado ou reciclado internamente pelo resizer.
            // A responsabilidade de reciclagem do bitmap final retornado é do chamador.
        }
    }

    /**
     * Verifica a integridade de um arquivo de imagem comparando o hash MD5.
     *
     * @param file Arquivo a verificar
     * @param expectedHash Hash MD5 armazenado no banco no momento do salvamento
     * @return [IntegrityCheckResult] com resultado e hashes para comparação
     */
    fun verifyIntegrity(file: File, expectedHash: String): IntegrityCheckResult {
        val actualHash = hashCalculator.calculateMd5(file)
        return IntegrityCheckResult(
            isValid = actualHash?.equals(expectedHash, ignoreCase = true) == true,
            expectedHash = expectedHash,
            actualHash = actualHash,
            errorMessage = if (actualHash == null) "Falha ao calcular hash MD5" else null
        )
    }
}

// ============================================================================
// SEALED CLASS DE RESULTADO
// ============================================================================

/**
 * Resultado do processamento de uma imagem pelo [ImageProcessor].
 */
sealed class ImageProcessingResult {

    /**
     * Processamento concluído com sucesso.
     *
     * @property file Arquivo final gravado no disco
     * @property originalWidth Largura original antes de qualquer processamento
     * @property originalHeight Altura original antes de qualquer processamento
     * @property finalWidth Largura final após redimensionamento (se aplicado)
     * @property finalHeight Altura final após redimensionamento (se aplicado)
     * @property wasResized true se houve redimensionamento
     * @property finalQuality Qualidade JPEG final efetivamente aplicada
     * @property sizeBytes Tamanho do arquivo final em bytes
     * @property hashMd5 Hash MD5 do arquivo final para verificação de integridade
     * @property format Formato de imagem utilizado ([FotoFormato])
     */
    data class Success(
        val file: File,
        val originalWidth: Int,
        val originalHeight: Int,
        val finalWidth: Int,
        val finalHeight: Int,
        val wasResized: Boolean,
        val finalQuality: Int,
        val sizeBytes: Long,
        val hashMd5: String,
        val format: FotoFormato
    ) : ImageProcessingResult() {

        /**
         * Tamanho formatado usando [Long.formatarTamanho] centralizado.
         * Corrigido em 12.0.0: substituída lógica inline duplicada.
         */
        val sizeFormatted: String get() = sizeBytes.formatarTamanho()

        /** Dimensões originais formatadas para exibição */
        val originalDimensions: String get() = "${originalWidth}x${originalHeight}"

        /** Dimensões finais formatadas para exibição */
        val finalDimensions: String get() = "${finalWidth}x${finalHeight}"

        /** true se as dimensões foram alteradas durante o processamento */
        val dimensionsChanged: Boolean
            get() = originalWidth != finalWidth || originalHeight != finalHeight
    }

    /**
     * Erro no processamento da imagem.
     *
     * @property message Descrição do erro para log e feedback ao usuário
     */
    data class Error(val message: String) : ImageProcessingResult()

    /** true se o processamento foi bem-sucedido */
    val isSuccess: Boolean get() = this is Success

    /** true se ocorreu um erro */
    val isError: Boolean get() = this is Error

    /** Retorna [Success] ou null */
    fun getOrNull(): Success? = this as? Success

    /** Retorna a mensagem de erro ou null */
    fun errorMessageOrNull(): String? = (this as? Error)?.message
}