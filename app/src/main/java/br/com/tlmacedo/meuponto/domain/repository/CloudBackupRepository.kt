package br.com.tlmacedo.meuponto.domain.repository

/**
 * Interface para operações de backup e restauração na nuvem (Google Drive).
 */
interface CloudBackupRepository {
    /**
     * Realiza o upload do banco de dados para o Google Drive.
     * @return Result indicando sucesso ou erro.
     */
    suspend fun uploadBackup(): Result<Unit>

    /**
     * Realiza o download de um backup específico do Google Drive e restaura o banco.
     * Se [fileId] for nulo, tenta restaurar o mais recente.
     * @return Result indicando sucesso ou erro.
     */
    suspend fun downloadERestaurarBackup(fileId: String? = null): Result<Unit>

    /**
     * Verifica se o usuário está autenticado com o Google.
     */
    suspend fun isUsuarioAutenticado(): Boolean

    /**
     * Retorna o e-mail da conta conectada, se houver.
     */
    suspend fun getContaConectada(): String?

    /**
     * Realiza uma chamada de teste à API do Google Drive para validar a conexão.
     * @return Result indicando se a conexão está realmente ativa e autorizada.
     */
    suspend fun testarConexao(): Result<Unit>

    /**
     * Sincroniza fotos de comprovantes não sincronizadas para o Google Drive.
     * @return Result indicando sucesso ou erro.
     */
    suspend fun sincronizarFotos(): Result<Unit>

    /**
     * Lista os arquivos de backup (.db) presentes na pasta de backups da nuvem.
     * @return Result com a lista de arquivos (nome e metadados básicos).
     */
    suspend fun listarBackupsNuvem(): Result<List<CloudFile>>

    /**
     * Exclui um backup específico da nuvem.
     */
    suspend fun excluirBackupNuvem(fileId: String): Result<Unit>

    /**
     * Exclui todos os backups da nuvem vinculados ao emprego ativo.
     */
    suspend fun excluirTodosBackupsNuvem(): Result<Unit>

    /**
     * Sincroniza a data do último backup na nuvem com o armazenamento local (DataStore).
     * Útil para garantir integridade quando o usuário troca de dispositivo.
     */
    suspend fun sincronizarStatusUltimoBackup(): Result<Long>

    /**
     * Desconecta a conta Google do aplicativo.
     * @return Result indicando sucesso ou erro.
     */
    suspend fun desconectarConta(): Result<Unit>
}

/**
 * Representa um arquivo simplificado na nuvem.
 */
data class CloudFile(
    val id: String,
    val name: String,
    val size: Long,
    val modifiedTime: Long,
    val mimeType: String
)
