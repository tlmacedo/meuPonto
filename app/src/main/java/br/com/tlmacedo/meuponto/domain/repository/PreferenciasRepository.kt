// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/PreferenciasRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de preferências do usuário.
 *
 * Define o contrato para operações de leitura/escrita de preferências
 * persistentes usando DataStore, seguindo o princípio de inversão
 * de dependência (DIP).
 *
 * @author Thiago
 * @since 2.0.0
 */
interface PreferenciasRepository {

    // ========================================================================
    // Emprego Ativo
    // ========================================================================

    /**
     * Obtém o ID do emprego atualmente ativo.
     *
     * @return ID do emprego ativo ou null se nenhum foi selecionado
     */
    suspend fun obterEmpregoAtivoId(): Long?

    /**
     * Define o emprego ativo.
     *
     * @param empregoId ID do emprego a ser definido como ativo
     */
    suspend fun definirEmpregoAtivoId(empregoId: Long)

    /**
     * Observa o ID do emprego ativo de forma reativa.
     *
     * @return Flow que emite o ID sempre que houver mudanças
     */
    fun observarEmpregoAtivoId(): Flow<Long?>

    /**
     * Limpa o emprego ativo (desseleciona).
     */
    suspend fun limparEmpregoAtivo()

    // ========================================================================
    // Primeira Execução / Onboarding
    // ========================================================================

    /**
     * Verifica se é a primeira execução do app.
     *
     * @return true se é a primeira execução
     */
    suspend fun isPrimeiraExecucao(): Boolean

    /**
     * Marca que o onboarding foi concluído.
     */
    suspend fun marcarOnboardingConcluido()

    /**
     * Observa o status de primeira execução de forma reativa.
     *
     * @return Flow que emite true se é primeira execução
     */
    fun observarPrimeiraExecucao(): Flow<Boolean>

    // ========================================================================
    // Configurações Gerais do App
    // ========================================================================

    /**
     * Obtém o tema do app (claro, escuro, sistema).
     *
     * @return Tema configurado ("light", "dark", "system")
     */
    suspend fun obterTema(): String

    /**
     * Define o tema do app.
     *
     * @param tema Tema a ser aplicado
     */
    suspend fun definirTema(tema: String)

    /**
     * Observa o tema de forma reativa.
     *
     * @return Flow que emite o tema sempre que houver mudanças
     */
    fun observarTema(): Flow<String>

    /**
     * Obtém se as notificações estão habilitadas globalmente.
     *
     * @return true se notificações estão habilitadas
     */
    suspend fun isNotificacoesHabilitadas(): Boolean

    /**
     * Define se as notificações estão habilitadas.
     *
     * @param habilitadas Status das notificações
     */
    suspend fun definirNotificacoesHabilitadas(habilitadas: Boolean)

    /**
     * Observa o status das notificações de forma reativa.
     *
     * @return Flow que emite o status sempre que houver mudanças
     */
    fun observarNotificacoesHabilitadas(): Flow<Boolean>

    // ========================================================================
    // Utilitários
    // ========================================================================

    /**
     * Limpa todas as preferências (reset do app).
     */
    suspend fun limparTudo()
}
