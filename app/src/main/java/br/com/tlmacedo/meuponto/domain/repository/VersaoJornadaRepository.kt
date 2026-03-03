// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/VersaoJornadaRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Interface do repositório de versões de jornada.
 *
 * Define o contrato para operações de persistência e consulta
 * de versões de jornada de trabalho.
 *
 * @author Thiago
 * @since 2.7.0
 * @updated 4.0.0 - Adicionados métodos de consulta para UseCases
 */
interface VersaoJornadaRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    /**
     * Insere uma nova versão de jornada.
     *
     * @param versao Versão a ser inserida
     * @return ID da versão inserida
     */
    suspend fun inserir(versao: VersaoJornada): Long

    /**
     * Atualiza uma versão de jornada existente.
     *
     * @param versao Versão com dados atualizados
     */
    suspend fun atualizar(versao: VersaoJornada)

    /**
     * Exclui uma versão de jornada.
     *
     * @param versao Versão a ser excluída
     */
    suspend fun excluir(versao: VersaoJornada)

    /**
     * Exclui uma versão de jornada pelo ID.
     *
     * @param id ID da versão a ser excluída
     */
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Operações de Leitura por ID
    // ========================================================================

    /**
     * Busca uma versão de jornada pelo ID.
     *
     * @param id ID da versão
     * @return Versão encontrada ou null
     */
    suspend fun buscarPorId(id: Long): VersaoJornada?

    /**
     * Observa uma versão de jornada pelo ID de forma reativa.
     *
     * @param id ID da versão
     * @return Flow que emite a versão sempre que houver mudanças
     */
    fun observarPorId(id: Long): Flow<VersaoJornada?>

    // ========================================================================
    // Operações de Leitura por Emprego
    // ========================================================================

    /**
     * Busca todas as versões de jornada de um emprego (alias para listarPorEmprego).
     *
     * @param empregoId ID do emprego
     * @return Lista de versões
     */
    suspend fun buscarPorEmprego(empregoId: Long): List<VersaoJornada>

    /**
     * Lista todas as versões de jornada de um emprego.
     *
     * @param empregoId ID do emprego
     * @return Lista de versões ordenadas por data de início (mais recente primeiro)
     */
    suspend fun listarPorEmprego(empregoId: Long): List<VersaoJornada>

    /**
     * Observa todas as versões de um emprego de forma reativa.
     *
     * @param empregoId ID do emprego
     * @return Flow que emite a lista sempre que houver mudanças
     */
    fun observarPorEmprego(empregoId: Long): Flow<List<VersaoJornada>>

    /**
     * Busca a versão vigente (atual) de um emprego.
     *
     * @param empregoId ID do emprego
     * @return Versão vigente ou null se não houver
     */
    suspend fun buscarVigente(empregoId: Long): VersaoJornada?

    /**
     * Observa a versão vigente de um emprego de forma reativa.
     *
     * @param empregoId ID do emprego
     * @return Flow que emite a versão vigente sempre que houver mudanças
     */
    fun observarVigente(empregoId: Long): Flow<VersaoJornada?>

    /**
     * Verifica se existe ao menos uma versão para o emprego.
     *
     * @param empregoId ID do emprego
     * @return true se existir ao menos uma versão
     */
    suspend fun existeParaEmprego(empregoId: Long): Boolean

    // ========================================================================
    // Consultas por Data
    // ========================================================================

    /**
     * Busca a versão de jornada aplicável para uma data específica.
     *
     * @param empregoId ID do emprego
     * @param data Data de referência
     * @return Versão aplicável ou null
     */
    suspend fun buscarPorData(empregoId: Long, data: LocalDate): VersaoJornada?

    /**
     * Busca a versão de jornada vigente para uma data específica (alias).
     *
     * @param empregoId ID do emprego
     * @param data Data de referência
     * @return Versão aplicável ou null
     */
    suspend fun buscarPorEmpregoEData(empregoId: Long, data: LocalDate): VersaoJornada?

    /**
     * Observa a versão aplicável para uma data específica de forma reativa.
     *
     * @param empregoId ID do emprego
     * @param data Data de referência
     * @return Flow que emite a versão sempre que houver mudanças
     */
    fun observarPorEmpregoEData(empregoId: Long, data: LocalDate): Flow<VersaoJornada?>

    // ========================================================================
    // Operações de Versionamento
    // ========================================================================

    /**
     * Cria uma nova versão de jornada.
     * Automaticamente fecha a versão anterior (define dataFim).
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data de início da nova versão
     * @param descricao Descrição opcional da versão
     * @param copiarDaVersaoAnterior Se deve copiar os horários da versão anterior
     * @return ID da nova versão criada
     */
    suspend fun criarNovaVersao(
        empregoId: Long,
        dataInicio: LocalDate,
        descricao: String? = null,
        copiarDaVersaoAnterior: Boolean = true
    ): Long

    /**
     * Verifica se existe sobreposição de períodos com outras versões.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data de início do período
     * @param dataFim Data de fim do período (null = sem fim)
     * @param excluirId ID da versão a excluir da verificação (para edição)
     * @return true se houver sobreposição
     */
    suspend fun existeSobreposicao(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate?,
        excluirId: Long = 0
    ): Boolean

    /**
     * Busca a versão anterior a uma data.
     *
     * @param empregoId ID do emprego
     * @param data Data de referência
     * @return Versão anterior ou null
     */
    suspend fun buscarVersaoAnterior(empregoId: Long, data: LocalDate): VersaoJornada?

    /**
     * Busca a próxima versão após uma data.
     *
     * @param empregoId ID do emprego
     * @param data Data de referência
     * @return Próxima versão ou null
     */
    suspend fun buscarProximaVersao(empregoId: Long, data: LocalDate): VersaoJornada?

    // ========================================================================
    // Contagens
    // ========================================================================

    /**
     * Conta quantas versões um emprego possui.
     *
     * @param empregoId ID do emprego
     * @return Quantidade de versões
     */
    suspend fun contarPorEmprego(empregoId: Long): Int
}
