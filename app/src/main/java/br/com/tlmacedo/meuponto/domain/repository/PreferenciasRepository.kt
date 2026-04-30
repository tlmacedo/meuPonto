// file: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/PreferenciasRepository.kt
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
 * @updated 12.0.0 - Adicionadas preferências de Autenticação e Biometria
 */
interface PreferenciasRepository {

    // ========================================================================
    // Autenticação e Segurança (Fase 1)
    // ========================================================================

    /**
     * Verifica se o recurso de "Lembrar-me" está ativo.
     *
     * @return true se ativo
     */
    suspend fun isLembrarMeAtivo(): Boolean

    /**
     * Define o status do recurso "Lembrar-me".
     *
     * @param ativo Novo status
     */
    suspend fun definirLembrarMe(ativo: Boolean)

    /**
     * Observa o status do "Lembrar-me" de forma reativa.
     *
     * @return Flow com o status
     */
    fun observarLembrarMe(): Flow<Boolean>

    /**
     * Obtém o e-mail do último usuário logado (usado para preencher o campo de login).
     *
     * @return E-mail ou null
     */
    suspend fun obterUltimoEmailLogado(): String?

    /**
     * Define o e-mail do último usuário logado.
     *
     * @param email E-mail a ser salvo
     */
    suspend fun definirUltimoEmailLogado(email: String)

    /**
     * Verifica se o uso de biometria está habilitado para o app.
     *
     * @return true se habilitado
     */
    suspend fun isBiometriaHabilitada(): Boolean

    /**
     * Define se o uso de biometria está habilitado.
     *
     * @param habilitado Novo status
     */
    suspend fun definirBiometriaHabilitada(habilitado: Boolean)

    /**
     * Observa o status da biometria de forma reativa.
     *
     * @return Flow com o status
     */
    fun observarBiometriaHabilitada(): Flow<Boolean>

    /**
     * Verifica se o bloqueio automático está habilitado.
     *
     * @return true se habilitado
     */
    suspend fun isBloqueioAutomaticoHabilitado(): Boolean

    /**
     * Define se o bloqueio automático está habilitado.
     *
     * @param habilitado Novo status
     */
    suspend fun definirBloqueioAutomaticoHabilitado(habilitado: Boolean)

    /**
     * Observa o status do bloqueio automático de forma reativa.
     *
     * @return Flow com o status
     */
    fun observarBloqueioAutomaticoHabilitado(): Flow<Boolean>

    /**
     * Verifica se o preview na tela de recentes deve ser ocultado.
     *
     * @return true se deve ocultar
     */
    suspend fun isOcultarPreviewHabilitado(): Boolean

    /**
     * Define se o preview na tela de recentes deve ser ocultado.
     *
     * @param habilitado Novo status
     */
    suspend fun definirOcultarPreviewHabilitado(habilitado: Boolean)

    /**
     * Observa o status de ocultar preview de forma reativa.
     *
     * @return Flow com o status
     */
    fun observarOcultarPreviewHabilitado(): Flow<Boolean>

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
    // Configurações de Jornada (Padrões Globais)
    // ========================================================================

    /**
     * Obtém a carga horária diária padrão em minutos.
     * Valor default: 480 (8 horas).
     */
    suspend fun obterCargaHorariaPadrao(): Int

    /**
     * Define a carga horária diária padrão.
     */
    suspend fun definirCargaHorariaPadrao(minutos: Int)

    /**
     * Observa a carga horária padrão.
     */
    fun observarCargaHorariaPadrao(): Flow<Int>

    /**
     * Obtém o intervalo mínimo padrão em minutos.
     * Valor default: 60 (1 hora).
     */
    suspend fun obterIntervaloMinimoPadrao(): Int

    /**
     * Define o intervalo mínimo padrão.
     */
    suspend fun definirIntervaloMinimoPadrao(minutos: Int)

    /**
     * Observa o intervalo mínimo padrão.
     */
    fun observarIntervaloMinimoPadrao(): Flow<Int>

    /**
     * Obtém a tolerância geral padrão em minutos.
     * Valor default: 10 minutos.
     */
    suspend fun obterToleranciaGeralPadrao(): Int

    /**
     * Define a tolerância geral padrão.
     */
    suspend fun definirToleranciaGeralPadrao(minutos: Int)

    /**
     * Observa a tolerância geral padrão.
     */
    fun observarToleranciaGeralPadrao(): Flow<Int>

    // ========================================================================
    // Utilitários
    // ========================================================================

    /**
     * Limpa todas as preferências (reset do app).
     */
    suspend fun limparTudo()

}
