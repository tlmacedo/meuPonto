package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.HorarioPadrao
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de horários padrão.
 *
 * Define o contrato para operações de persistência de horários padrão
 * de trabalho por dia da semana.
 *
 * @author Thiago
 * @since 2.0.0
 */
interface HorarioPadraoRepository {

    /**
     * Insere um novo horário padrão.
     *
     * @param horarioPadrao Horário padrão a ser inserido
     * @return ID gerado
     */
    suspend fun inserir(horarioPadrao: HorarioPadrao): Long

    /**
     * Atualiza um horário padrão existente.
     *
     * @param horarioPadrao Horário padrão atualizado
     */
    suspend fun atualizar(horarioPadrao: HorarioPadrao)

    /**
     * Exclui um horário padrão.
     *
     * @param horarioPadrao Horário padrão a ser excluído
     */
    suspend fun excluir(horarioPadrao: HorarioPadrao)

    /**
     * Busca um horário padrão pelo ID.
     *
     * @param id Identificador do horário padrão
     * @return Horário padrão ou null
     */
    suspend fun buscarPorId(id: Long): HorarioPadrao?

    /**
     * Busca todos os horários padrão de um emprego.
     *
     * @param empregoId ID do emprego
     * @return Lista de horários padrão ordenados por dia da semana
     */
    suspend fun buscarPorEmprego(empregoId: Long): List<HorarioPadrao>

    /**
     * Busca o horário padrão de um emprego para um dia específico.
     *
     * @param empregoId ID do emprego
     * @param diaSemana Dia da semana (1=Segunda, 7=Domingo)
     * @return Horário padrão ou null se não configurado
     */
    suspend fun buscarPorEmpregoEDiaSemana(empregoId: Long, diaSemana: Int): HorarioPadrao?

    /**
     * Observa os horários padrão de um emprego de forma reativa.
     *
     * @param empregoId ID do emprego
     * @return Flow que emite a lista sempre que houver mudanças
     */
    fun observarPorEmprego(empregoId: Long): Flow<List<HorarioPadrao>>

    /**
     * Exclui todos os horários padrão de um emprego.
     *
     * @param empregoId ID do emprego
     */
    suspend fun excluirPorEmpregoId(empregoId: Long)

    /**
     * Insere ou atualiza um horário padrão.
     *
     * @param horarioPadrao Horário padrão
     * @return ID do registro
     */
    suspend fun upsert(horarioPadrao: HorarioPadrao): Long
}
