// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/util/DatabaseCheckpointManager.kt
package br.com.tlmacedo.meuponto.data.local.database.util

import android.util.Log
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de checkpoint WAL para garantir persistência imediata dos dados.
 *
 * O SQLite em modo WAL (Write-Ahead Logging) mantém transações em um arquivo
 * separado (.db-wal) até que um checkpoint seja executado. Este manager
 * garante que os dados sejam consolidados no arquivo principal após cada
 * operação crítica.
 *
 * @author Thiago
 * @since 9.1.0
 */
@Singleton
class DatabaseCheckpointManager @Inject constructor(
    private val database: MeuPontoDatabase
) {
    companion object {
        private const val TAG = "DBCheckpoint"
    }

    /**
     * Executa checkpoint WAL de forma assíncrona.
     *
     * @param mode Modo do checkpoint (PASSIVE, FULL, RESTART, TRUNCATE)
     * @return true se o checkpoint foi executado com sucesso
     */
    suspend fun checkpoint(mode: CheckpointMode = CheckpointMode.PASSIVE): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val db = database.openHelper.writableDatabase
                // Para comandos PRAGMA que não retornam dados, execSQL é suficiente, 
                // mas alguns drivers exigem query/rawQuery se houver retorno ou comportamento específico.
                // Usando rawQuery para garantir compatibilidade com o que o erro SQLITE_OK sugere.
                db.query("PRAGMA wal_checkpoint(${mode.name})").use { cursor ->
                    if (cursor.moveToFirst()) {
                        Log.d(TAG, "Checkpoint ${mode.name} executado com sucesso")
                        true
                    } else {
                        Log.w(TAG, "Checkpoint ${mode.name} retornou cursor vazio")
                        false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao executar checkpoint: ${e.message}", e)
                false
            }
        }

    /**
     * Executa checkpoint de forma síncrona.
     * ATENÇÃO: Usar apenas em IO thread.
     */
    fun checkpointSync(mode: CheckpointMode = CheckpointMode.PASSIVE): Boolean {
        return try {
            val db = database.openHelper.writableDatabase
            db.query("PRAGMA wal_checkpoint(${mode.name})").use { cursor ->
                if (cursor.moveToFirst()) {
                    Log.d(TAG, "Checkpoint ${mode.name} (sync) executado com sucesso")
                    true
                } else {
                    Log.w(TAG, "Checkpoint ${mode.name} (sync) retornou cursor vazio")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao executar checkpoint sync: ${e.message}", e)
            false
        }
    }

    /**
     * Executa checkpoint com estatísticas detalhadas.
     * Útil para debug e monitoramento.
     *
     * @return Resultado com informações sobre páginas processadas
     */
    suspend fun checkpointWithStats(mode: CheckpointMode = CheckpointMode.PASSIVE): CheckpointResult =
        withContext(Dispatchers.IO) {
            try {
                val db = database.openHelper.writableDatabase
                val cursor = db.query("PRAGMA wal_checkpoint(${mode.name})")

                if (cursor.moveToFirst()) {
                    val blocked = cursor.getInt(0)       // 0 = sucesso, 1 = bloqueado
                    val walPages = cursor.getInt(1)      // Total de páginas no WAL
                    val checkpointed = cursor.getInt(2)  // Páginas efetivamente movidas
                    cursor.close()

                    Log.d(TAG, "Checkpoint ${mode.name}: blocked=$blocked, walPages=$walPages, checkpointed=$checkpointed")

                    CheckpointResult(
                        success = blocked == 0,
                        walPages = walPages,
                        checkpointedPages = checkpointed
                    )
                } else {
                    cursor.close()
                    CheckpointResult(success = false, error = "Cursor vazio")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Checkpoint error: ${e.message}", e)
                CheckpointResult(success = false, error = e.message)
            }
        }

    /**
     * Prepara o banco para backup externo.
     * Executa TRUNCATE para garantir que todo conteúdo está no .db principal.
     */
    suspend fun prepareForBackup(): Boolean = checkpoint(CheckpointMode.TRUNCATE)

    /**
     * Modos de checkpoint WAL.
     *
     * - PASSIVE: Não bloqueia, faz checkpoint do que for possível (mais rápido)
     * - FULL: Bloqueia escritas até completar checkpoint de todas as páginas
     * - RESTART: Como FULL, mas reinicia o WAL do início
     * - TRUNCATE: Como RESTART, mas também trunca o arquivo WAL para zero bytes
     */
    enum class CheckpointMode {
        PASSIVE,    // Mais rápido, não bloqueia - ideal para uso após operações
        FULL,       // Garante consistência completa
        RESTART,    // Reinicia o WAL
        TRUNCATE    // Mais limpo para backups (WAL fica vazio)
    }

    /**
     * Resultado detalhado do checkpoint.
     */
    data class CheckpointResult(
        val success: Boolean,
        val walPages: Int = 0,
        val checkpointedPages: Int = 0,
        val error: String? = null
    )
}
