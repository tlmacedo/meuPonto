// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/HorarioDiaSemanaRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de horários por dia da semana.
 *
 * Define o contrato para operações de persistência dos horários configurados
 * para cada dia da semana de um emprego.
 *
 * @author Thiago
 * @since 2.0.0
 */
interface HorarioDiaSemanaRepository {

    // ========================================================================
    // Operações de Escrita (CRUD)
    // ========================================================================

    /**
     * Insere um novo horário.
     *
     * @param horario Horário a ser inserido
     * @return ID gerado para o novo registro
     */
    suspend fun inserir(horario: HorarioDiaSemana): Long

    /**
     * Insere múltiplos horários de uma vez.
     *
     * @param horarios Lista de horários a serem inseridos
     * @return Lista de IDs gerados
     */
    suspend fun inserirTodos(horarios: List<HorarioDiaSemana>): List<Long>

    /**
     * Atualiza um horário existente.
     *
     * @param horario Horário com os dados atualizados
     */
    suspend fun atualizar(horario: HorarioDiaSemana)

    /**
     * Remove um horário.
     *
     * @param horario Horário a ser removido
     */
    suspend fun excluir(horario: HorarioDiaSemana)

    /**
     * Remove todos os horários de um emprego.
     *
     * @param empregoId Identificador do emprego
     */
    suspend fun excluirPorEmprego(empregoId: Long)

    // ========================================================================
    // Operações de Leitura
    // ========================================================================

    /**
     * Busca um horário pelo ID.
     *
     * @param id Identificador único do horário
     * @return Horário encontrado ou null
     */
    suspend fun buscarPorId(id: Long): HorarioDiaSemana?

    /**
     * Busca todos os horários de um emprego.
     *
     * @param empregoId Identificador do emprego
     * @return Lista de horários ordenada por dia da semana
     */
    suspend fun buscarPorEmprego(empregoId: Long): List<HorarioDiaSemana>

    /**
     * Busca o horário de um dia específico de um emprego.
     *
     * @param empregoId Identificador do emprego
     * @param diaSemana Dia da semana
     * @return Horário encontrado ou null
     */
    suspend fun buscarPorEmpregoEDia(empregoId: Long, diaSemana: DiaSemana): HorarioDiaSemana?

    /**
     * Busca apenas os dias ativos de um emprego.
     *
     * @param empregoId Identificador do emprego
     * @return Lista de horários dos dias ativos
     */
    suspend fun buscarDiasAtivos(empregoId: Long): List<HorarioDiaSemana>

    /**
     * Conta o número de dias ativos de um emprego.
     *
     * @param empregoId Identificador do emprego
     * @return Quantidade de dias de trabalho
     */
    suspend fun contarDiasAtivos(empregoId: Long): Int

    /**
     * Soma a carga horária semanal total de um emprego.
     *
     * @param empregoId Identificador do emprego
     * @return Total de minutos semanais
     */
    suspend fun somarCargaHorariaSemanal(empregoId: Long): Int

    /**
     * Busca a carga horária de um dia específico.
     *
     * @param empregoId Identificador do emprego
     * @param diaSemana Dia da semana
     * @return Carga horária em minutos ou null se dia inativo
     */
    suspend fun buscarCargaHorariaDia(empregoId: Long, diaSemana: DiaSemana): Int?

    /**
     * Verifica se um dia é ativo.
     *
     * @param empregoId Identificador do emprego
     * @param diaSemana Dia da semana
     * @return true se é dia de trabalho, false se é folga
     */
    suspend fun isDiaAtivo(empregoId: Long, diaSemana: DiaSemana): Boolean

    // ========================================================================
    // Operações Reativas (Flows)
    // ========================================================================

    /**
     * Observa os horários de um emprego de forma reativa.
     *
     * @param empregoId Identificador do emprego
     * @return Flow que emite a lista sempre que houver mudanças
     */
    fun observarPorEmprego(empregoId: Long): Flow<List<HorarioDiaSemana>>

    /**
     * Observa apenas os dias ativos de um emprego de forma reativa.
     *
     * @param empregoId Identificador do emprego
     * @return Flow que emite a lista de dias ativos
     */
    fun observarDiasAtivos(empregoId: Long): Flow<List<HorarioDiaSemana>>

    // Adicionar ao final da interface, antes do fechamento }

    // ========================================================================
    // Operações por Versão de Jornada
    // ========================================================================

    /**
     * Busca todos os horários de uma versão de jornada específica.
     *
     * @param versaoJornadaId Identificador da versão de jornada
     * @return Lista de horários ordenada por dia da semana
     */
    suspend fun buscarPorVersaoJornada(versaoJornadaId: Long): List<HorarioDiaSemana>

    /**
     * Busca o horário de um dia específico de uma versão de jornada.
     *
     * @param versaoJornadaId Identificador da versão de jornada
     * @param diaSemana Dia da semana
     * @return Horário encontrado ou null
     */
    suspend fun buscarPorVersaoEDia(versaoJornadaId: Long, diaSemana: DiaSemana): HorarioDiaSemana?


}
