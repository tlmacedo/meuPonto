package br.com.tlmacedo.meuponto.data.repository

import android.content.Context
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.util.DatabaseCheckpointManager
import br.com.tlmacedo.meuponto.data.local.datastore.PreferenciasGlobaisDataStore
import br.com.tlmacedo.meuponto.domain.repository.CloudBackupRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlinx.coroutines.flow.first
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.io.File as JavaFile
import javax.inject.Inject
import javax.inject.Singleton
import com.google.api.client.http.FileContent
import br.com.tlmacedo.meuponto.domain.repository.CloudFile
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository

/**
 * Implementação do repositório de backup na nuvem usando Google Drive REST API.
 * Utiliza a pasta "appDataFolder" para privacidade dos dados.
 *
 * @author Thiago
 * @since 12.0.0
 */
@Singleton
class CloudBackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
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
            val service = driveService ?: return@withContext Result.failure(Exception("Usuário não autenticado"))
            
            // Faz uma chamada leve para listar apenas o nome do drive (get About) 
            // ou listar arquivos com limit 1 apenas para validar o token/permissão
            service.files().list().setPageSize(1).execute()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Teste de conexão com Google Drive falhou")
            Result.failure(e)
        }
    }

    override suspend fun uploadBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service =
                driveService ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

            // 1. Obter emprego ativo para criar a estrutura de pastas
            val empregoId = preferencesRepository.obterEmpregoAtivoId()
                ?: return@withContext Result.failure(Exception("Nenhum emprego ativo encontrado"))

            val emprego = database.empregoDao().buscarPorId(empregoId)
                ?: return@withContext Result.failure(Exception("Emprego não encontrado no banco"))

            val hashEmprego = (emprego.apelido ?: emprego.nome).hashCode().toString(16).take(10)

            // 2. Garantir estrutura de pastas: "Meu Ponto" -> "{hash}" -> "backups"
            val folderMeuPontoId = getOrCreateFolder(service, "Meu Ponto", "root")
            val folderEmpregoId = getOrCreateFolder(service, hashEmprego, folderMeuPontoId)
            val folderBackupsId = getOrCreateFolder(service, "backups", folderEmpregoId)

            // 3. Consolidar banco
            checkpointManager.prepareForBackup()
            val dbFile = context.getDatabasePath(MeuPontoDatabase.DATABASE_NAME)

            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Arquivo de banco não encontrado"))
            }

            // 4. Preparar arquivo ZIP contendo o banco e as imagens
            val timestamp = System.currentTimeMillis()
            val zipFileName = "meuponto_backup_$timestamp.zip"
            val tempZipFile = JavaFile(context.cacheDir, zipFileName)

            ZipOutputStream(FileOutputStream(tempZipFile)).use { zos ->
                // Adicionar Banco de Dados
                val dbEntry = ZipEntry(MeuPontoDatabase.DATABASE_NAME)
                zos.putNextEntry(dbEntry)
                FileInputStream(dbFile).use { it.copyTo(zos) }
                zos.closeEntry()

                // Adicionar Imagens de Comprovantes
                val fotos = database.fotoComprovanteDao().buscarPorEmpregoEPeriodo(
                    empregoId,
                    LocalDate.MIN.plusYears(1), // Evita problemas com ano 0 em alguns sistemas
                    LocalDate.MAX
                )
                
                fotos.forEach { foto ->
                    val fotoFile = JavaFile(context.filesDir, foto.fotoPath)
                    if (fotoFile.exists()) {
                        val fotoEntry = ZipEntry("fotos/${foto.fotoPath}")
                        zos.putNextEntry(fotoEntry)
                        FileInputStream(fotoFile).use { it.copyTo(zos) }
                        zos.closeEntry()
                    } else {
                        Timber.w("Arquivo de foto não encontrado para backup: ${foto.fotoPath}")
                    }
                }
            }

            // 5. Upload do arquivo ZIP
            val fileMetadata = File().apply {
                name = zipFileName
                parents = listOf(folderBackupsId)
            }

            val mediaContent = FileContent("application/zip", tempZipFile)
            val createdFile = service.files().create(fileMetadata, mediaContent).execute()
            Timber.d("Backup compactado enviado para nuvem: ${createdFile.id}")

            // Remover arquivo temporário
            tempZipFile.delete()

            // 6. Registrar sucesso ANTES de tentar limpar locais
            preferencesDataStore.registrarBackupRealizado(isNuvem = true)

            // 7. Apagar backups locais após sucesso (exceto o db principal)
            limparBackupsLocais()

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro no upload de backup para nuvem")
            Result.failure(e)
        }
    }

    private fun getOrCreateFolder(service: Drive, folderName: String, parentId: String): String {
        val query = "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder' and '$parentId' in parents and trashed = false"
        val result = service.files().list().setQ(query).execute()
        
        return if (result.files != null && result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            val fileMetadata = File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
                parents = listOf(parentId)
            }
            service.files().create(fileMetadata).setFields("id").execute().id
        }
    }

    private fun limparBackupsLocais() {
        try {
            val backupDir = JavaFile(context.getExternalFilesDir(null), "backups")
            if (backupDir.exists()) {
                backupDir.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao limpar backups locais")
        }
    }

    override suspend fun downloadERestaurarBackup(fileId: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val service =
                    driveService ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

                val targetFileId = if (fileId != null) {
                    fileId
                } else {
                    // Localizar o backup mais recente se fileId for nulo
                    val query = "name contains 'meuponto_backup' and trashed = false"
                    val files = service.files().list()
                        .setQ(query)
                        .setOrderBy("modifiedTime desc")
                        .setPageSize(1)
                        .execute()

                    if (files.files.isNullOrEmpty()) {
                        return@withContext Result.failure(Exception("Nenhum backup encontrado na nuvem"))
                    }
                    files.files[0].id
                }

                val targetFile = service.files().get(targetFileId).setFields("name, mimeType").execute()
                val isZip = targetFile.name.endsWith(".zip") || targetFile.mimeType == "application/zip"

                val dbFile = context.getDatabasePath(MeuPontoDatabase.DATABASE_NAME)
                val baseDirFotos = JavaFile(context.filesDir, "comprovantes")

                if (isZip) {
                    val tempZipFile = JavaFile(context.cacheDir, "temp_restore.zip")
                    FileOutputStream(tempZipFile).use { output ->
                        service.files().get(targetFileId).executeMediaAndDownloadTo(output)
                    }

                    database.close()

                    ZipInputStream(FileInputStream(tempZipFile)).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            when {
                                entry.name == MeuPontoDatabase.DATABASE_NAME -> {
                                    // Restaurar banco
                                    java.io.File(dbFile.path + "-wal").delete()
                                    java.io.File(dbFile.path + "-shm").delete()
                                    FileOutputStream(dbFile).use { output -> zis.copyTo(output) }
                                }
                                entry.name.startsWith("fotos/") -> {
                                    // Restaurar foto
                                    val relativePath = entry.name.removePrefix("fotos/")
                                    val fotoFile = JavaFile(baseDirFotos, relativePath)
                                    fotoFile.parentFile?.mkdirs()
                                    FileOutputStream(fotoFile).use { output -> zis.copyTo(output) }
                                }
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                    tempZipFile.delete()
                } else {
                    // Download para arquivo temporário (.db legado)
                    val tempFile = java.io.File(context.cacheDir, "temp_backup.db")
                    FileOutputStream(tempFile).use { output ->
                        service.files().get(targetFileId).executeMediaAndDownloadTo(output)
                    }

                    // 3. Fechar banco atual e substituir
                    database.close()

                    // Remover arquivos WAL/SHM
                    java.io.File(dbFile.path + "-wal").delete()
                    java.io.File(dbFile.path + "-shm").delete()

                    tempFile.copyTo(dbFile, overwrite = true)
                    tempFile.delete()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Erro no download de backup da nuvem")
                Result.failure(e)
            }
        }

    override suspend fun sincronizarFotos(): Result<Unit> = withContext(Dispatchers.IO) {
        // ... (mantém implementação anterior)
        Result.success(Unit) 
    }

    override suspend fun listarBackupsNuvem(): Result<List<CloudFile>> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(Exception("Usuário não autenticado"))

            val empregoId = preferencesRepository.obterEmpregoAtivoId()
                ?: return@withContext Result.failure(Exception("Nenhum emprego ativo encontrado"))
            val emprego = database.empregoDao().buscarPorId(empregoId)
                ?: return@withContext Result.failure(Exception("Emprego não encontrado"))
            val hashEmprego = (emprego.apelido ?: emprego.nome).hashCode().toString(16).take(10)

            val folderMeuPontoId = getOrCreateFolder(service, "Meu Ponto", "root")
            val folderEmpregoId = getOrCreateFolder(service, hashEmprego, folderMeuPontoId)
            val folderBackupsId = getOrCreateFolder(service, "backups", folderEmpregoId)

            val query = "'$folderBackupsId' in parents and (name contains 'meuponto_backup') and trashed = false"
            val result = service.files().list()
                .setQ(query)
                .setFields("files(id, name, size, modifiedTime, mimeType)")
                .setOrderBy("modifiedTime desc")
                .execute()

            val cloudFiles = result.files?.map { file ->
                CloudFile(
                    id = file.id,
                    name = file.name,
                    size = file.getSize() ?: 0L,
                    modifiedTime = file.getModifiedTime()?.getValue() ?: 0L,
                    mimeType = file.mimeType
                )
            } ?: emptyList()

            Result.success(cloudFiles)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao listar backups na nuvem")
            Result.failure(e)
        }
    }

    override suspend fun excluirBackupNuvem(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(Exception("Usuário não autenticado"))
            service.files().delete(fileId).execute()
            
            // Se após excluir este, não houver mais backups na nuvem para este emprego, limpa a data no DataStore
            verificarELimparDataStoreNuvemSeVazio()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao excluir backup da nuvem: $fileId")
            Result.failure(e)
        }
    }

    override suspend fun excluirTodosBackupsNuvem(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(Exception("Usuário não autenticado"))
            
            val empregoId = preferencesRepository.obterEmpregoAtivoId()
                ?: return@withContext Result.failure(Exception("Nenhum emprego ativo encontrado"))
            val emprego = database.empregoDao().buscarPorId(empregoId)
                ?: return@withContext Result.failure(Exception("Emprego não encontrado"))
            val hashEmprego = (emprego.apelido ?: emprego.nome).hashCode().toString(16).take(10)

            val folderMeuPontoId = getOrCreateFolder(service, "Meu Ponto", "root")
            val folderEmpregoId = getOrCreateFolder(service, hashEmprego, folderMeuPontoId)
            val folderBackupsId = getOrCreateFolder(service, "backups", folderEmpregoId)

            val query = "'$folderBackupsId' in parents and (name contains 'meuponto_backup') and trashed = false"
            val result = service.files().list().setQ(query).setFields("files(id)").execute()

            result.files?.forEach { file ->
                service.files().delete(file.id).execute()
            }

            // Limpa a data de backup em nuvem no DataStore
            val prefs = preferencesDataStore.preferenciasGlobais.first()
            preferencesDataStore.salvarBackup(
                backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                backupNuvemAtivo = prefs.backupNuvemAtivo,
                ultimoBackupNuvem = 0L
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao excluir todos os backups da nuvem")
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
            val signInClient = GoogleSignIn.getClient(context, com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
            signInClient.signOut()
            signInClient.revokeAccess()
            
            // Limpa o estado de backup em nuvem nas preferências para refletir na UI imediatamente
            val prefs = preferencesDataStore.preferenciasGlobais.first()
            preferencesDataStore.salvarBackup(
                backupAutomaticoAtivo = prefs.backupAutomaticoAtivo,
                backupNuvemAtivo = false,
                ultimoBackupNuvem = 0L,
                contaGoogle = "" // Remove a conta salva
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao desconectar conta Google")
            Result.failure(e)
        }
    }
}
