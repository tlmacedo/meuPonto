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
}
