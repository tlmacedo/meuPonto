package br.com.tlmacedo.meuponto.domain.repository

import java.io.InputStream
import java.io.OutputStream

/**
 * Interface para operações de backup e restauração do banco de dados.
 */
interface BackupRepository {
    /**
     * Exporta o banco de dados atual para o stream fornecido.
     * @param outputStream Stream de destino (ex: do SAF)
     * @return Result indicando sucesso ou erro
     */
    suspend fun exportarBanco(outputStream: OutputStream): Result<Unit>

    /**
     * Importa o banco de dados a partir do stream fornecido.
     * @param inputStream Stream de origem (ex: do SAF)
     * @return Result indicando sucesso ou erro
     */
    suspend fun importarBanco(inputStream: InputStream): Result<Unit>

    /**
     * Realiza um backup local em pasta padronizada.
     */
    suspend fun realizarBackupLocal(): Result<Unit>

    /**
     * Retorna o timestamp do último backup local encontrado.
     */
    suspend fun obterDataUltimoBackupLocal(): Long?

    /**
     * Retorna a lista de backups locais disponíveis.
     */
    suspend fun obterBackupsLocais(): List<LocalBackupFile>

    /**
     * Exclui um arquivo de backup local específico.
     */
    suspend fun excluirBackupLocal(nomeArquivo: String): Result<Unit>

    /**
     * Exclui todos os arquivos de backup local da pasta do emprego ativo.
     */
    suspend fun excluirTodosBackupsLocais(): Result<Unit>

    /**
     * Restaura o banco de dados a partir de um arquivo de backup local específico.
     */
    suspend fun restaurarBackupLocal(nomeArquivo: String): Result<Unit>
}

/**
 * Representa um arquivo de backup local.
 */
data class LocalBackupFile(
    val nome: String,
    val caminho: String,
    val tamanho: Long,
    val dataCriacao: Long
)
