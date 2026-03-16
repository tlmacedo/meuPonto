// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/LixeiraRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.Ponto
import kotlinx.coroutines.flow.Flow

/**
 * Repository para operações de Lixeira (Soft Delete).
 *
 * Gerencia pontos excluídos logicamente, permitindo restauração
 * ou exclusão permanente dentro do período de retenção.
 *
 * @author Thiago
 * @since 11.0.0
 */
interface LixeiraRepository {

    /**
     * Move um ponto para a lixeira (soft delete).
     *
     * @param pontoId ID do ponto a ser excluído
     * @return Result indicando sucesso ou falha
     */
    suspend fun moverParaLixeira(pontoId: Long): Result<Unit>

    /**
     * Move múltiplos pontos para a lixeira.
     *
     * @param pontoIds Lista de IDs dos pontos
     * @return Result com quantidade de pontos movidos
     */
    suspend fun moverParaLixeira(pontoIds: List<Long>): Result<Int>

    /**
     * Restaura um ponto da lixeira.
     *
     * @param pontoId ID do ponto a ser restaurado
     * @return Result indicando sucesso ou falha
     */
    suspend fun restaurar(pontoId: Long): Result<Unit>

    /**
     * Restaura múltiplos pontos da lixeira.
     *
     * @param pontoIds Lista de IDs dos pontos
     * @return Result com quantidade de pontos restaurados
     */
    suspend fun restaurar(pontoIds: List<Long>): Result<Int>

    /**
     * Exclui permanentemente um ponto (hard delete).
     *
     * @param pontoId ID do ponto
     * @return Result indicando sucesso ou falha
     */
    suspend fun excluirPermanente(pontoId: Long): Result<Unit>

    /**
     * Exclui permanentemente múltiplos pontos.
     *
     * @param pontoIds Lista de IDs dos pontos
     * @return Result com quantidade de pontos excluídos
     */
    suspend fun excluirPermanente(pontoIds: List<Long>): Result<Int>

    /**
     * Esvazia toda a lixeira (exclui permanentemente todos os itens).
     *
     * @return Result com quantidade de pontos excluídos
     */
    suspend fun esvaziarLixeira(): Result<Int>

    /**
     * Lista todos os pontos na lixeira.
     *
     * @return Flow com lista de pontos deletados
     */
    fun observarPontosNaLixeira(): Flow<List<Ponto>>

    /**
     * Obtém lista de pontos na lixeira (snapshot).
     *
     * @return Lista de pontos deletados
     */
    suspend fun listarPontosNaLixeira(): List<Ponto>

    /**
     * Conta quantos itens estão na lixeira.
     *
     * @return Quantidade de itens
     */
    suspend fun contarItensNaLixeira(): Int

    /**
     * Verifica se a lixeira está vazia.
     *
     * @return true se não há itens na lixeira
     */
    suspend fun lixeiraVazia(): Boolean

    /**
     * Remove automaticamente pontos expirados da lixeira.
     * Pontos com mais de [diasRetencao] dias são excluídos permanentemente.
     *
     * @param diasRetencao Número de dias de retenção (padrão: 30)
     * @return Result com quantidade de pontos removidos
     */
    suspend fun limparExpirados(diasRetencao: Int = 30): Result<Int>

    /**
     * Obtém pontos que serão expirados em breve.
     *
     * @param diasRestantes Dias restantes até expiração
     * @return Lista de pontos prestes a expirar
     */
    suspend fun listarPrestesAExpirar(diasRestantes: Int = 3): List<Ponto>
}
