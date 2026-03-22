// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/ComprovanteImageStorage.kt
package br.com.tlmacedo.meuponto.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilitário para gerenciar o armazenamento físico de fotos de comprovantes.
 *
 * Responsável exclusivamente pelo acesso ao sistema de arquivos: salvar,
 * carregar, deletar e listar arquivos de imagem. A persistência de metadados
 * no banco de dados é responsabilidade dos repositórios e use cases.
 *
 * Estrutura de diretórios:
 * ```
 * files/
 * └── comprovantes/
 *     └── emprego_{id}/
 *         └── {ano}/
 *             └── {mes}/
 *                 └── ponto_{pontoId}_{timestamp}.jpg
 * ```
 *
 * ## Threads
 * Todos os métodos públicos são `suspend` e executam em [Dispatchers.IO].
 * Nunca chamá-los diretamente da main thread.
 *
 * @author Thiago
 * @since 9.0.0
 * @updated 12.0.0 - Todos os métodos de I/O migrados para suspend + withContext(IO);
 *                   e.printStackTrace() substituído por Timber.e();
 *                   cleanupOrphanImages corrigido para usar File.relativeTo()
 */
@Singleton
class ComprovanteImageStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ROOT_DIR = "comprovantes"
        private const val IMAGE_QUALITY = 85
        private const val MAX_IMAGE_DIMENSION = 1920
        private const val THUMBNAIL_SIZE = 200
    }

    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    // ========================================================================
    // ACESSO AO CONTEXTO E DIRETÓRIO
    // ========================================================================

    /**
     * Contexto da aplicação (necessário para FileProvider).
     */
    val appContext: Context get() = context

    /**
     * Retorna o diretório raiz de comprovantes.
     * Exposto para componentes que precisam do diretório base (ex: FileProvider).
     *
     * @return Diretório raiz criado se não existir
     */
    fun getComprovantesDirectory(): File = getRootDirectory()

    // ========================================================================
    // SUPORTE À CÂMERA
    // ========================================================================

    /**
     * Cria um arquivo temporário para captura via câmera.
     *
     * O arquivo é criado no diretório do emprego/data correspondente para
     * que, após a captura, possa ser movido sem operação de cópia.
     *
     * @param empregoId ID do emprego
     * @param data Data do ponto
     * @return File temporário criado no sistema de arquivos
     */
    fun createTempFileForCamera(empregoId: Long, data: LocalDate): File {
        val directory = getOrCreateDirectory(empregoId, data)
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val fileName = "temp_camera_$timestamp.jpg"
        return File(directory, fileName)
    }

    // ========================================================================
    // SALVAR IMAGEM
    // ========================================================================

    /**
     * Salva uma imagem de comprovante a partir de um URI (galeria ou câmera).
     *
     * Decodifica a imagem, redimensiona se necessário e salva em JPEG com
     * qualidade [IMAGE_QUALITY]. Executa em [Dispatchers.IO].
     *
     * @param uri URI da imagem original (content:// ou file://)
     * @param empregoId ID do emprego associado
     * @param pontoId ID do ponto (pode ser 0 se ainda não persistido)
     * @param dataHora Data e hora do ponto para nomear o arquivo
     * @return Caminho relativo da imagem salva, ou null em caso de erro
     */
    suspend fun saveFromUri(
        uri: Uri,
        empregoId: Long,
        pontoId: Long,
        dataHora: LocalDateTime
    ): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                Timber.w("Falha ao decodificar bitmap do URI: $uri")
                return@withContext null
            }

            val resizedBitmap = resizeIfNeeded(originalBitmap)
            val result = saveBitmap(resizedBitmap, empregoId, pontoId, dataHora)

            if (resizedBitmap !== originalBitmap) resizedBitmap.recycle()
            originalBitmap.recycle()

            result
        } catch (e: Exception) {
            Timber.e(e, "Falha ao salvar imagem do URI: $uri, empregoId=$empregoId, pontoId=$pontoId")
            null
        }
    }

    /**
     * Salva uma imagem de comprovante a partir de um Bitmap.
     *
     * @param bitmap Bitmap da imagem (não reciclado internamente — responsabilidade do chamador)
     * @param empregoId ID do emprego associado
     * @param pontoId ID do ponto
     * @param dataHora Data e hora do ponto
     * @return Caminho relativo da imagem salva, ou null em caso de erro
     */
    suspend fun saveFromBitmap(
        bitmap: Bitmap,
        empregoId: Long,
        pontoId: Long,
        dataHora: LocalDateTime
    ): String? = withContext(Dispatchers.IO) {
        try {
            val resizedBitmap = resizeIfNeeded(bitmap)
            val result = saveBitmap(resizedBitmap, empregoId, pontoId, dataHora)
            if (resizedBitmap !== bitmap) resizedBitmap.recycle()
            result
        } catch (e: Exception) {
            Timber.e(e, "Falha ao salvar bitmap, empregoId=$empregoId, pontoId=$pontoId")
            null
        }
    }

    /**
     * Salva o bitmap em arquivo e retorna o caminho relativo.
     *
     * @param bitmap Bitmap pronto para salvar (já redimensionado)
     * @param empregoId ID do emprego
     * @param pontoId ID do ponto
     * @param dataHora Data e hora para composição do nome do arquivo
     * @return Caminho relativo ou null em caso de erro
     */
    private fun saveBitmap(
        bitmap: Bitmap,
        empregoId: Long,
        pontoId: Long,
        dataHora: LocalDateTime
    ): String? {
        return try {
            val data = dataHora.toLocalDate()
            val directory = getOrCreateDirectory(empregoId, data)
            val fileName = generateFileName(pontoId, dataHora)
            val file = File(directory, fileName)

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
                outputStream.flush()
            }

            getRelativePath(empregoId, data, fileName)
        } catch (e: Exception) {
            Timber.e(e, "Falha ao gravar arquivo de imagem no disco")
            null
        }
    }

    // ========================================================================
    // CARREGAR IMAGEM
    // ========================================================================

    /**
     * Carrega uma imagem de comprovante como Bitmap.
     *
     * @param relativePath Caminho relativo retornado por [saveFromUri] ou [saveFromBitmap]
     * @return Bitmap da imagem ou null se não encontrada ou em caso de erro
     */
    suspend fun loadBitmap(relativePath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = getFileFromRelativePath(relativePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                Timber.w("Arquivo de imagem não encontrado: $relativePath")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao carregar bitmap: $relativePath")
            null
        }
    }

    /**
     * Carrega uma thumbnail otimizada para exibição em listas.
     *
     * Usa [BitmapFactory.Options.inSampleSize] para eficiência de memória,
     * decodificando a imagem já reduzida sem carregar o tamanho completo.
     *
     * @param relativePath Caminho relativo da imagem
     * @return Bitmap da thumbnail ou null se não encontrada
     */
    suspend fun loadThumbnail(relativePath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = getFileFromRelativePath(relativePath)
            if (!file.exists()) {
                Timber.w("Arquivo não encontrado para thumbnail: $relativePath")
                return@withContext null
            }

            // Primeira passagem: obtém apenas as dimensões sem alocar memória
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // Calcula fator de escala para o tamanho de thumbnail desejado
            val scaleFactor = maxOf(
                options.outWidth / THUMBNAIL_SIZE,
                options.outHeight / THUMBNAIL_SIZE
            ).coerceAtLeast(1)

            // Segunda passagem: decodifica com escala reduzida
            options.apply {
                inJustDecodeBounds = false
                inSampleSize = scaleFactor
            }

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            Timber.e(e, "Falha ao carregar thumbnail: $relativePath")
            null
        }
    }

    /**
     * Retorna o [File] absoluto correspondente ao caminho relativo.
     *
     * @param relativePath Caminho relativo da imagem
     * @return File apontando para o arquivo (pode não existir)
     */
    fun getAbsoluteFile(relativePath: String): File {
        return getFileFromRelativePath(relativePath)
    }

    /**
     * Verifica se uma imagem existe no armazenamento.
     *
     * @param relativePath Caminho relativo da imagem
     * @return true se o arquivo existe e é legível
     */
    fun exists(relativePath: String): Boolean {
        return try {
            getFileFromRelativePath(relativePath).exists()
        } catch (e: Exception) {
            Timber.w(e, "Erro ao verificar existência: $relativePath")
            false
        }
    }

    // ========================================================================
    // DELETAR IMAGEM
    // ========================================================================

    /**
     * Deleta uma imagem de comprovante do sistema de arquivos.
     *
     * @param relativePath Caminho relativo da imagem
     * @return true se deletado com sucesso ou se o arquivo já não existia
     */
    suspend fun delete(relativePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getFileFromRelativePath(relativePath)
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) Timber.w("Não foi possível deletar: $relativePath")
                deleted
            } else {
                true // Arquivo já não existe — operação considerada bem-sucedida
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao deletar imagem: $relativePath")
            false
        }
    }

    /**
     * Deleta todas as imagens de um emprego recursivamente.
     *
     * Deve ser chamado quando o emprego é excluído pelo usuário.
     *
     * @param empregoId ID do emprego
     * @return Número de arquivos deletados
     */
    suspend fun deleteAllForEmprego(empregoId: Long): Int = withContext(Dispatchers.IO) {
        try {
            val empregoDir = File(getRootDirectory(), "emprego_$empregoId")
            if (empregoDir.exists()) {
                val count = empregoDir.walkTopDown().filter { it.isFile }.count()
                empregoDir.deleteRecursively()
                Timber.i("Deletados $count arquivos do empregoId=$empregoId")
                count
            } else {
                0
            }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao deletar arquivos do empregoId=$empregoId")
            0
        }
    }

    // ========================================================================
    // MANUTENÇÃO E ESTATÍSTICAS
    // ========================================================================

    /**
     * Obtém o tamanho total ocupado por comprovantes em bytes.
     *
     * @return Total em bytes ou 0 em caso de erro
     */
    suspend fun getTotalStorageSize(): Long = withContext(Dispatchers.IO) {
        try {
            getRootDirectory().walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        } catch (e: Exception) {
            Timber.e(e, "Falha ao calcular tamanho total de armazenamento")
            0L
        }
    }

    /**
     * Obtém o tamanho total formatado usando [Long.formatarTamanho].
     *
     * @return String formatada (ex: "12.5 MB")
     */
    suspend fun getTotalStorageSizeFormatted(): String = getTotalStorageSize().formatarTamanho()

    /**
     * Conta o número total de comprovantes armazenados.
     *
     * @return Total de arquivos .jpg no diretório raiz
     */
    suspend fun getTotalImageCount(): Int = withContext(Dispatchers.IO) {
        try {
            getRootDirectory().walkTopDown()
                .filter { it.isFile && it.extension.lowercase() == "jpg" }
                .count()
        } catch (e: Exception) {
            Timber.e(e, "Falha ao contar imagens")
            0
        }
    }

    /**
     * Remove imagens órfãs (sem ponto associado no banco de dados).
     *
     * Deve ser chamado com a lista de caminhos relativos que ainda estão
     * referenciados no banco. Qualquer arquivo não listado será deletado.
     *
     * Corrigido em 12.0.0: usa [File.relativeTo] em vez de manipulação
     * de string com removePrefix, que era frágil a variações de separador.
     *
     * @param validPaths Conjunto de caminhos relativos em uso no banco
     * @return Número de arquivos órfãos removidos
     */
    suspend fun cleanupOrphanImages(validPaths: Set<String>): Int = withContext(Dispatchers.IO) {
        var removedCount = 0
        val rootDir = getRootDirectory()
        try {
            rootDir.walkTopDown()
                .filter { it.isFile && it.extension.lowercase() == "jpg" }
                .forEach { file ->
                    // Usa File.relativeTo() para cálculo robusto de caminho relativo
                    val relativePath = file.relativeTo(rootDir).path

                    if (relativePath !in validPaths) {
                        if (file.delete()) {
                            removedCount++
                            Timber.d("Arquivo órfão removido: $relativePath")
                        } else {
                            Timber.w("Não foi possível remover arquivo órfão: $relativePath")
                        }
                    }
                }
            Timber.i("Limpeza de órfãos concluída: $removedCount arquivo(s) removido(s)")
        } catch (e: Exception) {
            Timber.e(e, "Falha na limpeza de imagens órfãs")
        }
        removedCount
    }

    // ========================================================================
    // HELPERS PRIVADOS
    // ========================================================================

    /**
     * Retorna o diretório raiz de comprovantes, criando-o se necessário.
     */
    private fun getRootDirectory(): File {
        return File(context.filesDir, ROOT_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Retorna ou cria o diretório organizado por emprego/ano/mês.
     *
     * @param empregoId ID do emprego
     * @param data Data para compor a estrutura de diretórios
     */
    private fun getOrCreateDirectory(empregoId: Long, data: LocalDate): File {
        val path = "emprego_$empregoId/${data.year}/${String.format("%02d", data.monthValue)}"
        return File(getRootDirectory(), path).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Gera nome de arquivo único baseado no ID do ponto e timestamp do ponto.
     *
     * @param pontoId ID do ponto
     * @param dataHora Data e hora do ponto (não o momento atual)
     */
    private fun generateFileName(pontoId: Long, dataHora: LocalDateTime): String {
        val timestamp = dataHora.format(timestampFormatter)
        return "ponto_${pontoId}_$timestamp.jpg"
    }

    /**
     * Constrói o caminho relativo a partir dos componentes.
     *
     * @param empregoId ID do emprego
     * @param data Data do ponto
     * @param fileName Nome do arquivo
     */
    private fun getRelativePath(empregoId: Long, data: LocalDate, fileName: String): String {
        return "emprego_$empregoId/${data.year}/${String.format("%02d", data.monthValue)}/$fileName"
    }

    /**
     * Resolve o [File] absoluto a partir do caminho relativo.
     *
     * @param relativePath Caminho relativo dentro do diretório raiz
     */
    private fun getFileFromRelativePath(relativePath: String): File {
        return File(getRootDirectory(), relativePath)
    }

    /**
     * Redimensiona o bitmap se alguma dimensão ultrapassar [MAX_IMAGE_DIMENSION].
     *
     * Mantém o aspect ratio original. Retorna o bitmap original sem cópia
     * se já estiver dentro do limite — o chamador deve verificar se o
     * bitmap retornado é diferente do original antes de reciclar.
     *
     * @param bitmap Bitmap a verificar e redimensionar
     * @return Bitmap redimensionado ou o próprio original se não precisar
     */
    private fun resizeIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return bitmap
        }

        val ratio = minOf(
            MAX_IMAGE_DIMENSION.toFloat() / width,
            MAX_IMAGE_DIMENSION.toFloat() / height
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}