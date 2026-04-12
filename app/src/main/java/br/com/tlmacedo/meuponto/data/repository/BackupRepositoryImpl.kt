package br.com.tlmacedo.meuponto.data.repository

import android.content.Context
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.util.DatabaseCheckpointManager
import br.com.tlmacedo.meuponto.data.local.datastore.PreferenciasGlobaisDataStore
import br.com.tlmacedo.meuponto.domain.repository.BackupRepository
import br.com.tlmacedo.meuponto.domain.repository.LocalBackupFile
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de backup usando cópia binária do SQLite.
 *
 * @author Thiago
 * @since 9.1.0
 */
@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MeuPontoDatabase,
    private val checkpointManager: DatabaseCheckpointManager,
    private val preferencesDataStore: PreferenciasGlobaisDataStore,
    private val preferencesRepository: PreferenciasRepository
) : BackupRepository {

    override suspend fun exportarBanco(outputStream: OutputStream): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Garante que o banco está consolidado no arquivo principal (.db)
                checkpointManager.prepareForBackup()

                // 2. Localiza o arquivo do banco
                val dbFile = context.getDatabasePath(MeuPontoDatabase.DATABASE_NAME)
                if (!dbFile.exists()) {
                    return@withContext Result.failure(Exception("Arquivo de banco não encontrado"))
                }

                // 3. Copia o arquivo para o outputStream
                FileInputStream(dbFile).use { input ->
                    input.copyTo(outputStream)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Erro ao exportar banco")
                Result.failure(e)
            } finally {
                try {
                    outputStream.close()
                } catch (e: Exception) {
                    Timber.e(e, "Erro ao fechar stream de exportação")
                }
            }
        }

    override suspend fun importarBanco(inputStream: InputStream): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Fecha o banco atual antes de substituir o arquivo
                database.close()

                // 2. Caminho do arquivo principal
                val dbFile = context.getDatabasePath(MeuPontoDatabase.DATABASE_NAME)

                // 3. Remove arquivos auxiliares (WAL/SHM) para evitar inconsistências
                val walFile = File(dbFile.path + "-wal")
                val shmFile = File(dbFile.path + "-shm")

                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()

                // 4. Sobrescreve o arquivo principal
                dbFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Erro ao importar banco")
                Result.failure(e)
            } finally {
                try {
                    inputStream.close()
                } catch (e: Exception) {
                    Timber.e(e, "Erro ao fechar stream de importação")
                }
            }
        }

    override suspend fun realizarBackupLocal(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val folderBackups = getBackupFolder()
            if (!folderBackups.exists()) folderBackups.mkdirs()

            // O backup local agora foca apenas no banco de dados (.db)
            val fileName = "meuponto_backup_${System.currentTimeMillis()}.db"
            val backupFile = File(folderBackups, fileName)

            checkpointManager.prepareForBackup()
            val dbFile = context.getDatabasePath(MeuPontoDatabase.DATABASE_NAME)

            if (!dbFile.exists()) return@withContext Result.failure(Exception("Banco não encontrado"))

            dbFile.inputStream().use { input ->
                backupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            preferencesDataStore.registrarBackupRealizado(isNuvem = false)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao realizar backup local")
            Result.failure(e)
        }
    }

    override suspend fun obterDataUltimoBackupLocal(): Long? = withContext(Dispatchers.IO) {
        try {
            val folder = getBackupFolder()
            if (!folder.exists()) return@withContext null

            folder.listFiles { file -> file.name.startsWith("meuponto_backup") }
                ?.maxByOrNull { it.lastModified() }
                ?.lastModified()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun obterBackupsLocais(): List<LocalBackupFile> = withContext(Dispatchers.IO) {
        try {
            val folder = getBackupFolder()
            if (!folder.exists()) return@withContext emptyList()

            folder.listFiles { file -> file.name.startsWith("meuponto_backup") }
                ?.map {
                    LocalBackupFile(
                        nome = it.name,
                        caminho = it.absolutePath,
                        tamanho = it.length(),
                        dataCriacao = it.lastModified()
                    )
                }
                ?.sortedByDescending { it.dataCriacao }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun excluirBackupLocal(nomeArquivo: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val folder = getBackupFolder()
                val file = File(folder, nomeArquivo)
                if (file.exists() && file.delete()) {
                    verificarELimparDataStoreSeVazio()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Arquivo não encontrado ou erro ao excluir"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun excluirTodosBackupsLocais(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val folder = getBackupFolder()
            if (!folder.exists()) {
                preferencesDataStore.salvarBackup(
                    backupAutomaticoAtivo = preferencesDataStore.preferenciasGlobais.first().backupAutomaticoAtivo,
                    ultimoBackup = 0L
                )
                return@withContext Result.success(Unit)
            }

            folder.listFiles { file -> file.name.startsWith("meuponto_backup") }
                ?.forEach { it.delete() }

            preferencesDataStore.salvarBackup(
                backupAutomaticoAtivo = preferencesDataStore.preferenciasGlobais.first().backupAutomaticoAtivo,
                ultimoBackup = 0L
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun verificarELimparDataStoreSeVazio() {
        val backups = obterBackupsLocais()
        if (backups.isEmpty()) {
            val prefs = preferencesDataStore.preferenciasGlobais.first()
            preferencesDataStore.salvarBackup(
                backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                ultimoBackup = 0L
            )
        }
    }

    override suspend fun restaurarBackupLocal(nomeArquivo: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val folder = getBackupFolder()
                val backupFile = File(folder, nomeArquivo)
                if (!backupFile.exists()) {
                    return@withContext Result.failure(Exception("Arquivo de backup não encontrado"))
                }

                backupFile.inputStream().use { input ->
                    importarBanco(input)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun getBackupFolder(): File {
        val empregoId = preferencesRepository.obterEmpregoAtivoId() ?: 0L
        val emprego = database.empregoDao().buscarPorId(empregoId)
        val hashEmprego = (emprego?.apelido ?: emprego?.nome ?: "default").hashCode().toString(16).take(10)
        
        return File(context.getExternalFilesDir(null), "Meu Ponto/$hashEmprego/backups")
    }
}
