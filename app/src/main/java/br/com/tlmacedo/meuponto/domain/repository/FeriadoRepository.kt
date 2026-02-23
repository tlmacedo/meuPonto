// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/FeriadoRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.feriado.ConfiguracaoPontesAno
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repositório para gerenciamento de feriados e configurações de pontes.
 *
 * @author Thiago
 * @since 3.0.0
 */
interface FeriadoRepository {

    // ========================================================================
    // CRUD de Feriados
    // ========================================================================

    suspend fun inserir(feriado: Feriado): Long
    suspend fun inserirTodos(feriados: List<Feriado>): List<Long>
    suspend fun atualizar(feriado: Feriado)
    suspend fun excluir(feriado: Feriado)
    suspend fun excluirPorId(id: Long)

    // ========================================================================
    // Consultas de Feriados
    // ========================================================================

    suspend fun buscarPorId(id: Long): Feriado?
    fun observarPorId(id: Long): Flow<Feriado?>

    /**
     * Busca todos os feriados (ativos e inativos).
     * Usado na tela de gerenciamento de feriados.
     */
    suspend fun buscarTodos(): List<Feriado>

    /**
     * Observa todos os feriados (ativos e inativos).
     * Usado na tela de gerenciamento de feriados para atualização em tempo real.
     */
    fun observarTodos(): Flow<List<Feriado>>

    suspend fun buscarTodosAtivos(): List<Feriado>
    fun observarTodosAtivos(): Flow<List<Feriado>>

    suspend fun buscarPorAno(ano: Int): List<Feriado>
    fun observarPorAno(ano: Int): Flow<List<Feriado>>

    suspend fun buscarPorData(data: LocalDate): List<Feriado>
    suspend fun buscarPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): List<Feriado>

    suspend fun buscarPorTipo(tipo: TipoFeriado): List<Feriado>
    fun observarPorTipo(tipo: TipoFeriado): Flow<List<Feriado>>

    suspend fun buscarPorEmprego(empregoId: Long): List<Feriado>
    fun observarPorEmprego(empregoId: Long): Flow<List<Feriado>>

    /**
     * Busca feriados aplicáveis a uma data e emprego específicos.
     * Query otimizada que filtra direto no banco.
     */
    suspend fun buscarPorDataEEmprego(data: LocalDate, empregoId: Long): List<Feriado>

    /**
     * Observa feriados aplicáveis a uma data e emprego específicos.
     * Query otimizada para uso reativo.
     */
    fun observarPorDataEEmprego(data: LocalDate, empregoId: Long): Flow<List<Feriado>>

    // ========================================================================
    // Consultas Específicas de Pontes
    // ========================================================================

    suspend fun buscarPontesPorAnoEEmprego(ano: Int, empregoId: Long): List<Feriado>
    suspend fun contarPontesPorAnoEEmprego(ano: Int, empregoId: Long): Int

    // ========================================================================
    // Validações
    // ========================================================================

    suspend fun existeFeriadoDuplicado(
        nome: String,
        data: LocalDate?,
        diaMes: java.time.MonthDay?,
        excluirId: Long = 0
    ): Boolean

    suspend fun existemFeriadosNacionaisImportados(): Boolean

    // ========================================================================
    // Operações de Limpeza
    // ========================================================================

    suspend fun limparFeriadosAntigos(anoAtual: Int)
    suspend fun desativarPorEmprego(empregoId: Long)

    // ========================================================================
    // CRUD de Configuração de Pontes
    // ========================================================================

    suspend fun inserirConfiguracaoPontes(config: ConfiguracaoPontesAno): Long
    suspend fun atualizarConfiguracaoPontes(config: ConfiguracaoPontesAno)
    suspend fun excluirConfiguracaoPontes(config: ConfiguracaoPontesAno)

    // ========================================================================
    // Consultas de Configuração de Pontes
    // ========================================================================

    suspend fun buscarConfiguracaoPontes(empregoId: Long, ano: Int): ConfiguracaoPontesAno?
    fun observarConfiguracaoPontes(empregoId: Long, ano: Int): Flow<ConfiguracaoPontesAno?>

    suspend fun buscarTodasConfiguracoesPontes(empregoId: Long): List<ConfiguracaoPontesAno>
    fun observarTodasConfiguracoesPontes(empregoId: Long): Flow<List<ConfiguracaoPontesAno>>

    /**
     * Busca o adicional diário de pontes para uma data específica.
     * Retorna 0 se não houver configuração.
     */
    suspend fun buscarAdicionalDiarioPontes(empregoId: Long, data: LocalDate): Int
}
