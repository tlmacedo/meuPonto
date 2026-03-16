// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ImageTrashManager.kt
package br.com.tlmacedo.meuponto.util.foto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de lixeira para imagens de comprovantes.
 *
 * Responsável por:
 * - Mover imagens para lixeira em vez de deletar permanentemente
 * - Restaurar imagens da lixeira
 * - Limpar automaticamente imagens com mais de 30 dias
 * - Manter metadados para rastreamento
 *
 * Estrutura da lixeira:
 * ```
 * files/
 * └── .trash/
 *     └── comprovantes/
 *         └── {timestamp}_{pontoId}_{originalFileName}
 * ```
 *
 * @author Thiago
 * @since 11.0.0
 */
@Singleton
class ImageTrashManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TRASH_ROOT_DIR = ".trash"
        private const val COMPROVANTES_DIR = "comprovantes"
        private const val RETENTION_DAYS = 30L
        private const val METADATA_SEPARATOR = "_##_"
    }

    // ════════════════════════════════════════════════════════════════════════
    // MOVER PARA LIXEIRA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Move uma imagem para a lixeira.
     *
     * @param relativePath Caminho relativo da imagem (ex: emprego_1/2026/03/ponto_123.jpg)
     * @param pontoId ID do ponto associado
     * @param motivo Motivo da exclusão (para auditoria)
     * @return Resultado da operação com caminho na lixeira
     */
    suspend fun moveToTrash(
        relativePath: String,
        pontoId: Long,
        motivo: String
    ): TrashResult = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(getComprovantesRootDir(), relativePath)

            if (!sourceFile.exists()) {
                Timber.w("Arquivo não encontrado para mover para lixeira: $relativePath")
                return@withContext TrashResult.FileNotFound(relativePath)
            }

            val trashDir = getOrCreateTrashDirectory()
            val timestamp = System.currentTimeMillis()
            val originalFileName = sourceFile.name

            // Nome no formato: {timestamp}_##_{pontoId}_##_{caminhoOriginal}
            // Preserva o caminho original para restauração
            val trashFileName = buildTrashFileName(timestamp, pontoId, relativePath)
            val trashFile = File(trashDir, trashFileName)

            // Move o arquivo
            val moved = sourceFile.renameTo(trashFile)

            if (!moved) {
                // Fallback: copia e deleta
                sourceFile.copyTo(trashFile, overwrite = true)
                sourceFile.delete()
            }

            // Limpa diretórios vazios
            cleanupEmptyDirectories(sourceFile.parentFile)

            Timber.d("Imagem movida para lixeira: $relativePath -> ${trashFile.name}")

            TrashResult.Success(
                originalPath = relativePath,
                trashPath = trashFile.name,
                pontoId = pontoId,
                deletedAt = LocalDateTime.now()
            )
        } catch (e: Exception) {
            Timber.e(e, "Erro ao mover imagem para lixeira: $relativePath")
            TrashResult.Error("Erro ao mover para lixeira: ${e.message}")
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // RESTAURAR DA LIXEIRA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Restaura uma imagem da lixeira para o local original.
     *
     * @param trashFileName Nome do arquivo na lixeira
     * @return Resultado da restauração com caminho restaurado
     */
    suspend fun restoreFromTrash(trashFileName: String): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val trashFile = File(getOrCreateTrashDirectory(), trashFileName)

            if (!trashFile.exists()) {
                Timber.w("Arquivo não encontrado na lixeira: $trashFileName")
                return@withContext RestoreResult.FileNotFound(trashFileName)
            }

            val metadata = parseTrashFileName(trashFileName)
                ?: return@withContext RestoreResult.InvalidMetadata(trashFileName)

            val originalPath = metadata.originalPath
            val destinationFile = File(getComprovantesRootDir(), originalPath)

            // Garante que o diretório de destino existe
            destinationFile.parentFile?.mkdirs()

            // Move de volta
            val restored = trashFile.renameTo(destinationFile)

            if (!restored) {
                // Fallback: copia e deleta
                trashFile.copyTo(destinationFile, overwrite = true)
                trashFile.delete()
            }

            Timber.d("Imagem restaurada da lixeira: $trashFileName -> $originalPath")

            RestoreResult.Success(
                originalPath = originalPath,
                pontoId = metadata.pontoId
            )
        } catch (e: Exception) {
            Timber.e(e, "Erro ao restaurar imagem da lixeira: $trashFileName")
            RestoreResult.Error("Erro ao restaurar: ${e.message}")
        }
    }

    /**
     * Restaura imagem da lixeira pelo ID do ponto.
     *
     * @param pontoId ID do ponto
     * @return Resultado da restauração
     */
    suspend fun restoreByPontoId(pontoId: Long): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val trashDir = getOrCreateTrashDirectory()
            val trashFiles = trashDir.listFiles() ?: return@withContext RestoreResult.FileNotFound("ponto_$pontoId")

            // Procura o arquivo mais recente do ponto
            val trashFile = trashFiles
                .filter { file ->
                    val metadata = parseTrashFileName(file.name)
                    metadata?.pontoId == pontoId
                }
                .maxByOrNull { it.lastModified() }

            if (trashFile == null) {
                return@withContext RestoreResult.FileNotFound("ponto_$pontoId")
            }

            restoreFromTrash(trashFile.name)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao restaurar imagem pelo pontoId: $pontoId")
            RestoreResult.Error("Erro ao restaurar: ${e.message}")
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // LIMPEZA AUTOMÁTICA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Remove permanentemente arquivos com mais de 30 dias na lixeira.
     *
     * @return Quantidade de arquivos removidos
     */
    suspend fun cleanupExpiredFiles(): Int = withContext(Dispatchers.IO) {
        var removedCount = 0
        try {
            val trashDir = getOrCreateTrashDirectory()
            val cutoffTime = System.currentTimeMillis() - (RETENTION_DAYS * 24 * 60 * 60 * 1000)

            trashDir.listFiles()?.forEach { file ->
                val metadata = parseTrashFileName(file.name)
                if (metadata != null && metadata.timestamp < cutoffTime) {
                    if (file.delete()) {
                        removedCount++
                        Timber.d("Arquivo expirado removido da lixeira: ${file.name}")
                    }
                }
            }

            Timber.i("Limpeza da lixeira concluída: $removedCount arquivos removidos")
        } catch (e: Exception) {
            Timber.e(e, "Erro na limpeza da lixeira")
        }
        removedCount
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONSULTAS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Lista todos os itens na lixeira.
     */
    suspend fun listTrashItems(): List<TrashItem> = withContext(Dispatchers.IO) {
        try {
            val trashDir = getOrCreateTrashDirectory()
            trashDir.listFiles()
                ?.mapNotNull { file ->
                    val metadata = parseTrashFileName(file.name) ?: return@mapNotNull null
                    TrashItem(
                        trashFileName = file.name,
                        originalPath = metadata.originalPath,
                        pontoId = metadata.pontoId,
                        deletedAt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(metadata.timestamp),
                            ZoneId.systemDefault()
                        ),
                        sizeBytes = file.length(),
                        expiresAt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(metadata.timestamp + RETENTION_DAYS * 24 * 60 * 60 * 1000),
                            ZoneId.systemDefault()
                        )
                    )
                }
                ?.sortedByDescending { it.deletedAt }
                ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao listar itens da lixeira")
            emptyList()
        }
    }

    /**
     * Busca item na lixeira pelo ID do ponto.
     */
    suspend fun findByPontoId(pontoId: Long): TrashItem? = withContext(Dispatchers.IO) {
        listTrashItems().find { it.pontoId == pontoId }
    }

    /**
     * Verifica se existe imagem na lixeira para o ponto.
     */
    suspend fun existsForPonto(pontoId: Long): Boolean = withContext(Dispatchers.IO) {
        findByPontoId(pontoId) != null
    }

    /**
     * Obtém estatísticas da lixeira.
     */
    suspend fun getTrashStats(): TrashStats = withContext(Dispatchers.IO) {
        try {
            val items = listTrashItems()
            val totalSize = items.sumOf { it.sizeBytes }
            val expiredCount = items.count {
                ChronoUnit.DAYS.between(it.deletedAt, LocalDateTime.now()) >= RETENTION_DAYS
            }

            TrashStats(
                totalItems = items.size,
                totalSizeBytes = totalSize,
                expiredItems = expiredCount,
                oldestItem = items.minByOrNull { it.deletedAt }?.deletedAt,
                newestItem = items.maxByOrNull { it.deletedAt }?.deletedAt
            )
        } catch (e: Exception) {
            Timber.e(e, "Erro ao obter estatísticas da lixeira")
            TrashStats(0, 0, 0, null, null)
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // EXCLUSÃO PERMANENTE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Remove permanentemente um arquivo da lixeira.
     */
    suspend fun deletePermanently(trashFileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val trashFile = File(getOrCreateTrashDirectory(), trashFileName)
            if (trashFile.exists()) {
                val deleted = trashFile.delete()
                Timber.d("Arquivo deletado permanentemente: $trashFileName = $deleted")
                deleted
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao deletar permanentemente: $trashFileName")
            false
        }
    }

    /**
     * Limpa toda a lixeira.
     */
    suspend fun emptyTrash(): Int = withContext(Dispatchers.IO) {
        var count = 0
        try {
            val trashDir = getOrCreateTrashDirectory()
            trashDir.listFiles()?.forEach { file ->
                if (file.delete()) count++
            }
            Timber.i("Lixeira esvaziada: $count arquivos removidos")
        } catch (e: Exception) {
            Timber.e(e, "Erro ao esvaziar lixeira")
        }
        count
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS PRIVADOS
    // ════════════════════════════════════════════════════════════════════════

    private fun getComprovantesRootDir(): File {
        return File(context.filesDir, "comprovantes")
    }

    private fun getOrCreateTrashDirectory(): File {
        return File(context.filesDir, "$TRASH_ROOT_DIR/$COMPROVANTES_DIR").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun buildTrashFileName(timestamp: Long, pontoId: Long, originalPath: String): String {
        // Codifica o path original substituindo / por __
        val encodedPath = originalPath.replace("/", "__").replace("\\", "__")
        return "${timestamp}${METADATA_SEPARATOR}${pontoId}${METADATA_SEPARATOR}${encodedPath}"
    }

    private fun parseTrashFileName(fileName: String): TrashMetadata? {
        return try {
            val parts = fileName.split(METADATA_SEPARATOR)
            if (parts.size != 3) return null

            val timestamp = parts[0].toLongOrNull() ?: return null
            val pontoId = parts[1].toLongOrNull() ?: return null
            val originalPath = parts[2].replace("__", "/")

            TrashMetadata(timestamp, pontoId, originalPath)
        } catch (e: Exception) {
            Timber.w("Erro ao parsear nome do arquivo da lixeira: $fileName")
            null
        }
    }

    private fun cleanupEmptyDirectories(directory: File?) {
        var current = directory
        val rootPath = getComprovantesRootDir().absolutePath

        while (current != null &&
            current.absolutePath.startsWith(rootPath) &&
            current.absolutePath != rootPath) {
            if (current.isDirectory && (current.listFiles()?.isEmpty() == true)) {
                current.delete()
                current = current.parentFile
            } else {
                break
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// DATA CLASSES
// ════════════════════════════════════════════════════════════════════════════

private data class TrashMetadata(
    val timestamp: Long,
    val pontoId: Long,
    val originalPath: String
)

/**
 * Item na lixeira.
 */
data class TrashItem(
    val trashFileName: String,
    val originalPath: String,
    val pontoId: Long,
    val deletedAt: LocalDateTime,
    val sizeBytes: Long,
    val expiresAt: LocalDateTime
) {
    val daysUntilExpiration: Long
        get() = ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt).coerceAtLeast(0)

    val isExpired: Boolean
        get() = LocalDateTime.now().isAfter(expiresAt)

    val sizeFormatted: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> String.format("%.1f KB", sizeBytes / 1024.0)
            else -> String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0))
        }
}

/**
 * Estatísticas da lixeira.
 */
data class TrashStats(
    val totalItems: Int,
    val totalSizeBytes: Long,
    val expiredItems: Int,
    val oldestItem: LocalDateTime?,
    val newestItem: LocalDateTime?
) {
    val isEmpty: Boolean get() = totalItems == 0

    val totalSizeFormatted: String
        get() = when {
            totalSizeBytes < 1024 -> "$totalSizeBytes B"
            totalSizeBytes < 1024 * 1024 -> String.format("%.1f KB", totalSizeBytes / 1024.0)
            else -> String.format("%.2f MB", totalSizeBytes / (1024.0 * 1024.0))
        }
}

/**
 * Resultado da operação de mover para lixeira.
 */
sealed class TrashResult {
    data class Success(
        val originalPath: String,
        val trashPath: String,
        val pontoId: Long,
        val deletedAt: LocalDateTime
    ) : TrashResult()

    data class FileNotFound(val path: String) : TrashResult()
    data class Error(val message: String) : TrashResult()

    val isSuccess: Boolean get() = this is Success
}

/**
 * Resultado da operação de restaurar da lixeira.
 */
sealed class RestoreResult {
    data class Success(
        val originalPath: String,
        val pontoId: Long
    ) : RestoreResult()

    data class FileNotFound(val trashFileName: String) : RestoreResult()
    data class InvalidMetadata(val trashFileName: String) : RestoreResult()
    data class Error(val message: String) : RestoreResult()

    val isSuccess: Boolean get() = this is Success
}
