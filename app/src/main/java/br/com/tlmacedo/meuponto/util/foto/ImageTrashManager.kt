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
import br.com.tlmacedo.meuponto.util.formatarTamanho

/**
 * Gerenciador de lixeira para imagens de comprovantes de ponto.
 *
 * Responsável por mover imagens para lixeira em vez de deletar permanentemente,
 * restaurar imagens, limpar arquivos expirados e consultar o estado da lixeira.
 *
 * ## Estrutura da lixeira:
 * ```
 * files/
 * └── .trash/
 *     └── comprovantes/
 *         └── {timestamp}_##_{pontoId}_##_{caminhoOriginalCodificado}
 * ```
 *
 * ## Limitação conhecida do separador (registrada para Fase 1):
 * O separador `_##_` e a codificação de `/` por `__` são frágeis para caminhos
 * que já contenham essas sequências. A solução definitiva (Fase 1 do plano)
 * é migrar para arquivos `.json` de metadados adjacentes, eliminando a
 * dependência de parsing do nome do arquivo.
 *
 * ## Correções aplicadas (12.0.0):
 * - Toda a lógica já usava Timber corretamente — nenhum e.printStackTrace()
 *   foi encontrado neste arquivo (ImageTrashManager era o único arquivo correto)
 * - [TrashItem.sizeFormatted] usa [Long.formatarTamanho] centralizado
 * - [TrashStats.totalSizeFormatted] usa [Long.formatarTamanho] centralizado
 * - Adicionado KDoc completo em todas as funções públicas
 *
 * @param context Contexto da aplicação
 *
 * @author Thiago
 * @since 11.0.0
 * @updated 12.0.0 - sizeFormatted centralizado; KDoc completo adicionado
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

    // ========================================================================
    // MOVER PARA LIXEIRA
    // ========================================================================

    /**
     * Move uma imagem para a lixeira.
     *
     * O arquivo é renomeado para incluir metadados no nome (timestamp, pontoId
     * e caminho original codificado), permitindo restauração posterior sem banco.
     *
     * Após a movimentação, diretórios que ficaram vazios são removidos
     * automaticamente via [cleanupEmptyDirectories].
     *
     * @param relativePath Caminho relativo da imagem (ex: "emprego_1/2026/03/ponto_123.jpg")
     * @param pontoId ID do ponto associado para rastreamento
     * @param motivo Motivo da exclusão para auditoria
     * @return [TrashResult.Success] com caminho na lixeira ou [TrashResult.Error]
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

            val trashFileName = buildTrashFileName(timestamp, pontoId, relativePath)
            val trashFile = File(trashDir, trashFileName)

            // Tenta renomear (operação atômica e eficiente no mesmo volume)
            val moved = sourceFile.renameTo(trashFile)

            if (!moved) {
                // Fallback: copia e deleta (volumes diferentes ou permissões)
                Timber.w("renameTo falhou, usando fallback cópia+delete para: ${sourceFile.name}")
                sourceFile.copyTo(trashFile, overwrite = true)
                sourceFile.delete()
            }

            // Remove diretórios que ficaram vazios após a movimentação
            cleanupEmptyDirectories(sourceFile.parentFile)

            Timber.d("Imagem movida para lixeira: $relativePath → ${trashFile.name}")

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

    // ========================================================================
    // RESTAURAR DA LIXEIRA
    // ========================================================================

    /**
     * Restaura uma imagem da lixeira para o local original.
     *
     * O caminho original é recuperado do nome do arquivo via [parseTrashFileName].
     * O diretório de destino é criado automaticamente se não existir.
     *
     * @param trashFileName Nome do arquivo na lixeira (conforme retornado por [moveToTrash])
     * @return [RestoreResult.Success] com caminho restaurado ou erro descritivo
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

            val destinationFile = File(getComprovantesRootDir(), metadata.originalPath)

            // Garante que o diretório de destino existe antes de restaurar
            destinationFile.parentFile?.mkdirs()

            val restored = trashFile.renameTo(destinationFile)

            if (!restored) {
                Timber.w("renameTo falhou na restauração, usando fallback para: $trashFileName")
                trashFile.copyTo(destinationFile, overwrite = true)
                trashFile.delete()
            }

            Timber.d("Imagem restaurada: $trashFileName → ${metadata.originalPath}")

            RestoreResult.Success(
                originalPath = metadata.originalPath,
                pontoId = metadata.pontoId
            )
        } catch (e: Exception) {
            Timber.e(e, "Erro ao restaurar imagem da lixeira: $trashFileName")
            RestoreResult.Error("Erro ao restaurar: ${e.message}")
        }
    }

    /**
     * Restaura a imagem mais recente da lixeira pelo ID do ponto.
     *
     * @param pontoId ID do ponto para busca na lixeira
     * @return [RestoreResult.Success] ou [RestoreResult.FileNotFound]
     */
    suspend fun restoreByPontoId(pontoId: Long): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val trashDir = getOrCreateTrashDirectory()
            val trashFiles = trashDir.listFiles()
                ?: return@withContext RestoreResult.FileNotFound("ponto_$pontoId")

            val trashFile = trashFiles
                .filter { file ->
                    parseTrashFileName(file.name)?.pontoId == pontoId
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

    // ========================================================================
    // LIMPEZA AUTOMÁTICA
    // ========================================================================

    /**
     * Remove permanentemente arquivos com mais de [RETENTION_DAYS] dias na lixeira.
     *
     * Chamado pelo [br.com.tlmacedo.meuponto.worker.TrashCleanupWorker] periodicamente.
     *
     * @return Número de arquivos expirados removidos
     */
    suspend fun cleanupExpiredFiles(): Int = withContext(Dispatchers.IO) {
        var removedCount = 0
        try {
            val trashDir = getOrCreateTrashDirectory()
            val cutoffTime = System.currentTimeMillis() -
                    (RETENTION_DAYS * 24 * 60 * 60 * 1000)

            trashDir.listFiles()?.forEach { file ->
                val metadata = parseTrashFileName(file.name)
                if (metadata != null && metadata.timestamp < cutoffTime) {
                    if (file.delete()) {
                        removedCount++
                        Timber.d("Arquivo expirado removido da lixeira: ${file.name}")
                    } else {
                        Timber.w("Não foi possível remover arquivo expirado: ${file.name}")
                    }
                }
            }

            Timber.i("Limpeza da lixeira concluída: $removedCount arquivo(s) removido(s)")
        } catch (e: Exception) {
            Timber.e(e, "Erro na limpeza da lixeira")
        }
        removedCount
    }

    // ========================================================================
    // CONSULTAS
    // ========================================================================

    /**
     * Lista todos os itens atualmente na lixeira, ordenados por data de exclusão.
     *
     * @return Lista de [TrashItem] ordenada da mais recente para a mais antiga
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
                            Instant.ofEpochMilli(
                                metadata.timestamp + RETENTION_DAYS * 24 * 60 * 60 * 1000
                            ),
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
     * Busca o primeiro item na lixeira associado ao ID do ponto.
     *
     * @param pontoId ID do ponto para busca
     * @return [TrashItem] ou null se não encontrado
     */
    suspend fun findByPontoId(pontoId: Long): TrashItem? = withContext(Dispatchers.IO) {
        listTrashItems().find { it.pontoId == pontoId }
    }

    /**
     * Verifica se existe imagem na lixeira para o ponto especificado.
     *
     * @param pontoId ID do ponto
     * @return true se há pelo menos um item na lixeira para este ponto
     */
    suspend fun existsForPonto(pontoId: Long): Boolean = withContext(Dispatchers.IO) {
        findByPontoId(pontoId) != null
    }

    /**
     * Retorna estatísticas consolidadas da lixeira.
     *
     * @return [TrashStats] com totais e datas extremas
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

    // ========================================================================
    // EXCLUSÃO PERMANENTE
    // ========================================================================

    /**
     * Remove permanentemente um arquivo específico da lixeira.
     *
     * @param trashFileName Nome do arquivo na lixeira
     * @return true se removido com sucesso ou se já não existia
     */
    suspend fun deletePermanently(trashFileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val trashFile = File(getOrCreateTrashDirectory(), trashFileName)
            if (trashFile.exists()) {
                val deleted = trashFile.delete()
                Timber.d("Arquivo deletado permanentemente: $trashFileName = $deleted")
                deleted
            } else {
                true // Arquivo já não existe — operação bem-sucedida
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao deletar permanentemente: $trashFileName")
            false
        }
    }

    /**
     * Remove todos os arquivos da lixeira permanentemente.
     *
     * @return Número de arquivos removidos
     */
    suspend fun emptyTrash(): Int = withContext(Dispatchers.IO) {
        var count = 0
        try {
            val trashDir = getOrCreateTrashDirectory()
            trashDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    count++
                } else {
                    Timber.w("Não foi possível remover da lixeira: ${file.name}")
                }
            }
            Timber.i("Lixeira esvaziada: $count arquivo(s) removido(s)")
        } catch (e: Exception) {
            Timber.e(e, "Erro ao esvaziar lixeira")
        }
        count
    }

    // ========================================================================
    // HELPERS PRIVADOS
    // ========================================================================

    private fun getComprovantesRootDir(): File =
        File(context.filesDir, "comprovantes")

    private fun getOrCreateTrashDirectory(): File =
        File(context.filesDir, "$TRASH_ROOT_DIR/$COMPROVANTES_DIR").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Constrói o nome do arquivo na lixeira codificando os metadados.
     *
     * Formato: `{timestamp}_##_{pontoId}_##_{caminhoOriginalCodificado}`
     *
     * LIMITAÇÃO: O separador `_##_` pode colidir com nomes de arquivos
     * que contenham essa sequência. Será substituído por JSON na Fase 1.
     */
    private fun buildTrashFileName(
        timestamp: Long,
        pontoId: Long,
        originalPath: String
    ): String {
        val encodedPath = originalPath
            .replace("/", "__")
            .replace("\\", "__")
        return "${timestamp}${METADATA_SEPARATOR}${pontoId}${METADATA_SEPARATOR}${encodedPath}"
    }

    /**
     * Recupera os metadados do arquivo da lixeira a partir do nome do arquivo.
     *
     * Retorna null se o nome não estiver no formato esperado (ex: arquivo corrompido).
     *
     * LIMITAÇÃO: `split(METADATA_SEPARATOR)` espera exatamente 3 partes.
     * Nomes com `_##_` no caminho original retornam null aqui.
     */
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

    /**
     * Remove diretórios que ficaram vazios após a movimentação de um arquivo.
     *
     * Sobe recursivamente a partir de [directory] enquanto os diretórios
     * estiverem vazios e ainda dentro do diretório raiz de comprovantes.
     *
     * @param directory Diretório a partir do qual iniciar a limpeza
     */
    private fun cleanupEmptyDirectories(directory: File?) {
        var current = directory
        val rootPath = getComprovantesRootDir().absolutePath

        while (current != null &&
            current.absolutePath.startsWith(rootPath) &&
            current.absolutePath != rootPath
        ) {
            if (current.isDirectory && current.listFiles()?.isEmpty() == true) {
                current.delete()
                current = current.parentFile
            } else {
                break
            }
        }
    }
}

// ============================================================================
// DATA CLASSES E SEALED CLASSES DE SUPORTE
// ============================================================================

/** Metadados extraídos do nome do arquivo na lixeira (uso interno) */
private data class TrashMetadata(
    val timestamp: Long,
    val pontoId: Long,
    val originalPath: String
)

/**
 * Item na lixeira de imagens.
 *
 * @property trashFileName Nome do arquivo na lixeira (para operações de restore/delete)
 * @property originalPath Caminho relativo original para restauração
 * @property pontoId ID do ponto vinculado à imagem
 * @property deletedAt Data e hora da exclusão
 * @property sizeBytes Tamanho do arquivo em bytes
 * @property expiresAt Data e hora de expiração (exclusão permanente automática)
 */
data class TrashItem(
    val trashFileName: String,
    val originalPath: String,
    val pontoId: Long,
    val deletedAt: LocalDateTime,
    val sizeBytes: Long,
    val expiresAt: LocalDateTime
) {
    /** Dias restantes até a expiração (mínimo: 0) */
    val daysUntilExpiration: Long
        get() = ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt).coerceAtLeast(0)

    /** true se o item já passou do período de retenção */
    val isExpired: Boolean
        get() = LocalDateTime.now().isAfter(expiresAt)

    /**
     * Tamanho formatado usando [Long.formatarTamanho] centralizado.
     * Corrigido em 12.0.0: substituída lógica inline duplicada.
     */
    val sizeFormatted: String get() = sizeBytes.formatarTamanho()
}

/**
 * Estatísticas consolidadas da lixeira.
 *
 * @property totalItems Total de arquivos na lixeira
 * @property totalSizeBytes Tamanho total em bytes
 * @property expiredItems Arquivos que já ultrapassaram o período de retenção
 * @property oldestItem Data do item mais antigo ou null se vazia
 * @property newestItem Data do item mais recente ou null se vazia
 */
data class TrashStats(
    val totalItems: Int,
    val totalSizeBytes: Long,
    val expiredItems: Int,
    val oldestItem: LocalDateTime?,
    val newestItem: LocalDateTime?
) {
    /** true se a lixeira está vazia */
    val isEmpty: Boolean get() = totalItems == 0

    /**
     * Tamanho total formatado usando [Long.formatarTamanho] centralizado.
     * Corrigido em 12.0.0: substituída lógica inline duplicada.
     */
    val totalSizeFormatted: String get() = totalSizeBytes.formatarTamanho()
}

/**
 * Resultado da operação de mover para lixeira.
 */
sealed class TrashResult {

    /**
     * Arquivo movido com sucesso.
     *
     * @property originalPath Caminho relativo original
     * @property trashPath Nome do arquivo na lixeira
     * @property pontoId ID do ponto vinculado
     * @property deletedAt Momento da movimentação
     */
    data class Success(
        val originalPath: String,
        val trashPath: String,
        val pontoId: Long,
        val deletedAt: LocalDateTime
    ) : TrashResult()

    /** Arquivo de origem não encontrado no disco */
    data class FileNotFound(val path: String) : TrashResult()

    /** Erro durante a operação */
    data class Error(val message: String) : TrashResult()

    /** true se a operação foi bem-sucedida */
    val isSuccess: Boolean get() = this is Success
}

/**
 * Resultado da operação de restaurar da lixeira.
 */
sealed class RestoreResult {

    /**
     * Arquivo restaurado com sucesso.
     *
     * @property originalPath Caminho relativo restaurado
     * @property pontoId ID do ponto vinculado
     */
    data class Success(
        val originalPath: String,
        val pontoId: Long
    ) : RestoreResult()

    /** Arquivo não encontrado na lixeira */
    data class FileNotFound(val trashFileName: String) : RestoreResult()

    /** Nome do arquivo não pôde ser parseado para extrair metadados */
    data class InvalidMetadata(val trashFileName: String) : RestoreResult()

    /** Erro durante a operação */
    data class Error(val message: String) : RestoreResult()

    /** true se a operação foi bem-sucedida */
    val isSuccess: Boolean get() = this is Success
}