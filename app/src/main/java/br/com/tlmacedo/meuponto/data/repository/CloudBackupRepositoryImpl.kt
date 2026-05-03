package br.com.tlmacedo.meuponto.data.repository

import android.content.Context
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.util.DatabaseCheckpointManager
import br.com.tlmacedo.meuponto.data.local.datastore.PreferenciasGlobaisDataStore
import br.com.tlmacedo.meuponto.domain.repository.CloudBackupRepository
import br.com.tlmacedo.meuponto.domain.repository.CloudFile
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Collections
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File as JavaFile

/**
 * Implementação do repositório de backup na nuvem usando Google Drive REST API.
 *
 * @author Thiago
 * @since 12.0.0
 * @updated 14.2.0 - Melhorada integridade de restauração e adicionada sincronização de status.
 */
@Suppress("DEPRECATION")
@Singleton
class CloudBackupRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: MeuPontoDatabase,
    private val checkpointManager: DatabaseCheckpointManager,
    private val preferencesDataStore: PreferenciasGlobaisDataStore,
    private val preferencesRepository: PreferenciasRepository
) : CloudBackupRepository {

    private val driveService: Drive?
        get() {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
            val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE)
            ).apply {
                selectedAccount = account.account
            }

            return Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("MeuPonto").build()
        }

    override suspend fun isUsuarioAutenticado(): Boolean = withContext(Dispatchers.IO) {
        GoogleSignIn.getLastSignedInAccount(context) != null
    }

    override suspend fun getContaConectada(): String? = withContext(Dispatchers.IO) {
        GoogleSignIn.getLastSignedInAccount(context)?.email
    }

    override suspend fun testarConexao(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = driveService
                ?: return@withContext Result.failure(Exception("Usuário não autenticado"))
            service.files().list().setPageSize(1).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Teste de conexão com Google Drive falhou")
            Result.failure(e)
        }
    }

    override suspend fun sincronizarStatusUltimoBackup(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val backups = listarBackupsNuvem().getOrNull() ?: emptyList()
            val ultimoTimestamp = backups.maxByOrNull { it.modifiedTime }?.modifiedTime ?: 0L
            
            if (ultimoTimestamp > 0) {
                val prefs = preferencesDataStore.preferenciasGlobais.first()
                preferencesDataStore.salvarBackup(
                    backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                    backupNuvemAtivo = prefs.backupNuvemAtivo,
                    ultimoBackupNuvem = ultimoTimestamp
                )
            }
            Result.success(ultimoTimestamp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = driveService
                    ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

            val empregoId = preferencesRepository.obterEmpregoAtivoId()
                ?: return@withContext Result.failure(Exception("Nenhum emprego ativo encontrado"))

            val emprego = database.empregoDao().buscarPorId(empregoId)
                ?: return@withContext Result.failure(Exception("Emprego não encontrado no banco"))

            val hashEmprego = (emprego.apelido ?: emprego.nome).hashCode().toString(16).take(10)

            val folderMeuPontoId = getOrCreateFolder(service, "Meu Ponto", "root")
            val folderEmpregoId = getOrCreateFolder(service, hashEmprego, folderMeuPontoId)
            val folderBackupsId = getOrCreateFolder(service, "backups", folderEmpregoId)

            checkpointManager.prepareForBackup()
            val dbFile = context.getDatabasePath(MeuPontoDatabase.DATABASE_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Arquivo de banco não encontrado"))
            }

            val timestamp = System.currentTimeMillis()
            val zipFileName = "meuponto_db_$timestamp.zip"
            val tempZipFile = JavaFile(context.cacheDir, zipFileName)

            try {
                ZipOutputStream(FileOutputStream(tempZipFile)).use { zos ->
                    val dbEntry = ZipEntry(MeuPontoDatabase.DATABASE_NAME)
                    zos.putNextEntry(dbEntry)
                    FileInputStream(dbFile).use { it.copyTo(zos) }
                    zos.closeEntry()
                }

                val dbMetadata = File().apply {
                    name = zipFileName
                    parents = listOf(folderBackupsId)
                }
                val mediaContent = FileContent("application/zip", tempZipFile)
                service.files().create(dbMetadata, mediaContent)
                    .setFields("id, name, size, modifiedTime")
                    .execute()
            } finally {
                if (tempZipFile.exists()) tempZipFile.delete()
            }

            limparBackupsAntigosNuvem(service, folderBackupsId)
            sincronizarFotos()
            preferencesDataStore.registrarBackupRealizado(isNuvem = true)

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro no upload de backup para nuvem")
            Result.failure(e)
        }
    }

    override suspend fun downloadERestaurarBackup(fileId: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val service = driveService
                        ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

                val targetFileId = fileId ?: run {
                    val query = "name contains 'meuponto_db' and trashed = false"
                    val files = service.files().list().setQ(query).setOrderBy("modifiedTime desc").setPageSize(1).execute()
                    files.files?.firstOrNull()?.id
                } ?: return@withContext Result.failure(Exception("Nenhum backup encontrado na nuvem"))

                val targetFile = service.files().get(targetFileId).setFields("name, mimeType").execute()
                val isZip = targetFile.name.endsWith(".zip") || targetFile.mimeType == "application/zip"

                val dbFile = context.getDatabasePath(MeuPontoDatabase.DATABASE_NAME)
                val backupDeSeguranca = JavaFile(dbFile.path + ".pre_restore")

                try {
                    dbFile.copyTo(backupDeSeguranca, overwrite = true)
                } catch (e: Exception) {
                    Timber.w("Não foi possível criar backup de segurança antes da restauração")
                }

                try {
                    if (isZip) {
                        val tempZipFile = JavaFile(context.cacheDir, "temp_restore.zip")
                        FileOutputStream(tempZipFile).use { output ->
                            service.files().get(targetFileId).executeMediaAndDownloadTo(output)
                        }

                        database.close()

                        ZipInputStream(FileInputStream(tempZipFile)).use { zis ->
                            var entry = zis.nextEntry
                            while (entry != null) {
                                if (entry.name == MeuPontoDatabase.DATABASE_NAME) {
                                    JavaFile(dbFile.path + "-wal").delete()
                                    JavaFile(dbFile.path + "-shm").delete()
                                    FileOutputStream(dbFile).use { output -> zis.copyTo(output) }
                                }
                                zis.closeEntry()
                                entry = zis.nextEntry
                            }
                        }
                        tempZipFile.delete()
                    } else {
                        val tempFile = JavaFile(context.cacheDir, "temp_backup.db")
                        FileOutputStream(tempFile).use { output ->
                            service.files().get(targetFileId).executeMediaAndDownloadTo(output)
                        }
                        database.close()
                        JavaFile(dbFile.path + "-wal").delete()
                        JavaFile(dbFile.path + "-shm").delete()
                        tempFile.copyTo(dbFile, overwrite = true)
                        tempFile.delete()
                    }
                    
                    if (backupDeSeguranca.exists()) backupDeSeguranca.delete()
                    
                    Result.success(Unit)
                } catch (e: Exception) {
                    if (backupDeSeguranca.exists()) {
                        backupDeSeguranca.copyTo(dbFile, overwrite = true)
                        backupDeSeguranca.delete()
                    }
                    throw e
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro no download de backup da nuvem")
                Result.failure(e)
            }
        }

    override suspend fun sincronizarFotos(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = driveService
                ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

            val empregoId = preferencesRepository.obterEmpregoAtivoId()
                ?: return@withContext Result.failure(Exception("Nenhum emprego ativo encontrado"))

            val emprego = database.empregoDao().buscarPorId(empregoId)
                ?: return@withContext Result.failure(Exception("Emprego não encontrado"))

            val hashEmprego = (emprego.apelido ?: emprego.nome).hashCode().toString(16).take(10)

            val folderMeuPontoId = getOrCreateFolder(service, "Meu Ponto", "root")
            val folderEmpregoId = getOrCreateFolder(service, hashEmprego, folderMeuPontoId)
            val folderPhotosId = getOrCreateFolder(service, "photos", folderEmpregoId)

            val fotosNaoSincronizadas = database.fotoComprovanteDao().buscarNaoSincronizadasPorEmprego(empregoId)
            if (fotosNaoSincronizadas.isEmpty()) return@withContext Result.success(Unit)

            val fotosAgrupadas = fotosNaoSincronizadas.groupBy {
                val data = it.data
                "${data.year}/${data.monthValue.toString().padStart(2, '0')}"
            }

            fotosAgrupadas.forEach { (diretorio, fotos) ->
                val (ano, mes) = diretorio.split("/")
                val folderAnoId = getOrCreateFolder(service, ano, folderPhotosId)
                val folderMesId = getOrCreateFolder(service, mes, folderAnoId)

                fotos.forEach { fotoEntity ->
                    try {
                        val fotoFile = JavaFile(context.filesDir, fotoEntity.fotoPath)
                        if (!fotoFile.exists()) return@forEach

                        if (!fileQueryExists(service, fotoFile.name, folderMesId)) {
                            val fileMetadata = File().apply {
                                name = fotoFile.name
                                parents = listOf(folderMesId)
                            }
                            val mediaContent = FileContent("image/jpeg", fotoFile)
                            val uploadedFile = service.files().create(fileMetadata, mediaContent).setFields("id").execute()

                            database.fotoComprovanteDao().marcarComoSincronizado(
                                id = fotoEntity.id,
                                sincronizadoEm = java.time.Instant.now(),
                                cloudFileId = uploadedFile.id,
                                atualizadoEm = java.time.Instant.now()
                            )
                        } else {
                            val cloudId = service.files().list()
                                .setQ("name = '${fotoFile.name}' and '$folderMesId' in parents")
                                .setFields("files(id)")
                                .execute().files?.firstOrNull()?.id ?: ""

                            database.fotoComprovanteDao().marcarComoSincronizado(
                                id = fotoEntity.id,
                                sincronizadoEm = java.time.Instant.now(),
                                cloudFileId = cloudId,
                                atualizadoEm = java.time.Instant.now()
                            )
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Erro ao sincronizar foto individual")
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro crítico ao sincronizar fotos")
            Result.failure(e)
        }
    }

    override suspend fun listarBackupsNuvem(): Result<List<CloudFile>> =
        withContext(Dispatchers.IO) {
            try {
                val service = driveService
                    ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

                val empregoId = preferencesRepository.obterEmpregoAtivoId()
                    ?: return@withContext Result.failure(Exception("Nenhum emprego ativo encontrado"))
                val emprego = database.empregoDao().buscarPorId(empregoId)
                    ?: return@withContext Result.failure(Exception("Emprego não encontrado"))
                val hashEmprego = (emprego.apelido ?: emprego.nome).hashCode().toString(16).take(10)

                val folderMeuPontoId = getOrCreateFolder(service, "Meu Ponto", "root")
                val folderEmpregoId = getOrCreateFolder(service, hashEmprego, folderMeuPontoId)
                val folderBackupsId = getOrCreateFolder(service, "backups", folderEmpregoId)

                val query = "'$folderBackupsId' in parents and (name contains 'meuponto_db') and trashed = false"
                val result = service.files().list().setQ(query).setFields("files(id, name, size, modifiedTime, mimeType)").setOrderBy("modifiedTime desc").execute()

                val cloudFiles = result.files?.map { file ->
                    CloudFile(
                        id = file.id,
                        name = file.name,
                        size = file.getSize() ?: 0L,
                        modifiedTime = file.modifiedTime?.value ?: 0L,
                        mimeType = file.mimeType
                    )
                } ?: emptyList()

                Result.success(cloudFiles)
            } catch (e: Exception) {
                Timber.e(e, "Erro ao listar backups na nuvem")
                Result.failure(e)
            }
        }

    override suspend fun excluirBackupNuvem(fileId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val service = driveService
                    ?: return@withContext Result.failure(Exception("Usuário não autenticado"))
                service.files().delete(fileId).execute()
                verificarELimparDataStoreNuvemSeVazio()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun excluirTodosBackupsNuvem(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = driveService
                ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

            val empregoId = preferencesRepository.obterEmpregoAtivoId()
                ?: return@withContext Result.failure(Exception("Nenhum emprego ativo encontrado"))
            val emprego = database.empregoDao().buscarPorId(empregoId)
                ?: return@withContext Result.failure(Exception("Emprego não encontrado"))
            val hashEmprego = (emprego.apelido ?: emprego.nome).hashCode().toString(16).take(10)

            val folderMeuPontoId = getOrCreateFolder(service, "Meu Ponto", "root")
            val folderEmpregoId = getOrCreateFolder(service, hashEmprego, folderMeuPontoId)
            val folderBackupsId = getOrCreateFolder(service, "backups", folderEmpregoId)

            val query = "'$folderBackupsId' in parents and (name contains 'meuponto_db') and trashed = false"
            val result = service.files().list().setQ(query).setFields("files(id)").execute()

            result.files?.forEach { file -> service.files().delete(file.id).execute() }

            val prefs = preferencesDataStore.preferenciasGlobais.first()
            preferencesDataStore.salvarBackup(
                backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                backupNuvemAtivo = prefs.backupNuvemAtivo,
                ultimoBackupNuvem = 0L
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun verificarELimparDataStoreNuvemSeVazio() {
        val backups = listarBackupsNuvem().getOrNull()
        if (backups.isNullOrEmpty()) {
            val prefs = preferencesDataStore.preferenciasGlobais.first()
            preferencesDataStore.salvarBackup(
                backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                backupNuvemAtivo = prefs.backupNuvemAtivo,
                ultimoBackupNuvem = 0L
            )
        }
    }

    override suspend fun desconectarConta(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val signInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
            signInClient.signOut()
            signInClient.revokeAccess()

            val prefs = preferencesDataStore.preferenciasGlobais.first()
            preferencesDataStore.salvarBackup(
                backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                backupNuvemAtivo = false,
                ultimoBackupNuvem = 0L,
                contaGoogle = ""
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun limparBackupsAntigosNuvem(service: Drive, folderBackupsId: String) {
        try {
            val query = "'$folderBackupsId' in parents and name contains 'meuponto_db' and trashed = false"
            val result = service.files().list().setQ(query).setOrderBy("modifiedTime desc").setFields("files(id, name)").execute()
            val files = result.files ?: return
            if (files.size > 5) {
                files.drop(5).forEach { file -> service.files().delete(file.id).execute() }
            }
        } catch (e: Exception) {
            Timber.w(e, "Erro ao limpar backups antigos")
        }
    }

    private fun getOrCreateFolder(service: Drive, folderName: String, parentId: String): String {
        val query = "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder' and '$parentId' in parents and trashed = false"
        val result = service.files().list().setQ(query).execute()

        return result.files?.firstOrNull()?.id
            ?: run {
                val fileMetadata = File().apply {
                    name = folderName
                    mimeType = "application/vnd.google-apps.folder"
                    parents = listOf(parentId)
                }
                service.files().create(fileMetadata).setFields("id").execute().id
            }
    }

    private fun fileQueryExists(service: Drive, fileName: String, parentId: String): Boolean {
        val query = "name = '$fileName' and '$parentId' in parents and trashed = false"
        val result = service.files().list().setQ(query).setFields("files(id)").execute()
        return !result.files.isNullOrEmpty()
    }
}
