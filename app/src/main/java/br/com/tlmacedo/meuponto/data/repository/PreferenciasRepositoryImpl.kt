// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/PreferenciasRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extensão para criar o DataStore de preferências.
 *
 * Cria uma instância única do DataStore vinculada ao contexto da aplicação.
 * O nome "meu_ponto_preferences" será usado para o arquivo de preferências.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "meu_ponto_preferences"
)

/**
 * Implementação do repositório de preferências usando DataStore.
 *
 * Persiste configurações do usuário de forma assíncrona e type-safe,
 * substituindo o SharedPreferences tradicional com uma API baseada em Flow.
 *
 * @property context Contexto da aplicação para acesso ao DataStore
 *
 * @author Thiago
 * @since 2.0.0
 */
@Singleton
class PreferenciasRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferenciasRepository {

    // ========================================================================
    // Chaves do DataStore
    // ========================================================================

    /**
     * Objeto que contém todas as chaves tipadas usadas no DataStore.
     * Centraliza as definições para evitar duplicação e erros de digitação.
     */
    private object PreferencesKeys {
        val EMPREGO_ATIVO_ID = longPreferencesKey("emprego_ativo_id")
        val PRIMEIRA_EXECUCAO = booleanPreferencesKey("primeira_execucao")
        val ONBOARDING_CONCLUIDO = booleanPreferencesKey("onboarding_concluido")
        val TEMA = stringPreferencesKey("tema")
        val NOTIFICACOES_HABILITADAS = booleanPreferencesKey("notificacoes_habilitadas")
    }

    // ========================================================================
    // Valores Padrão
    // ========================================================================

    /**
     * Objeto que contém os valores padrão para cada preferência.
     * Usado quando uma preferência ainda não foi definida pelo usuário.
     */
    private object Defaults {
        const val TEMA = "system"
        const val NOTIFICACOES_HABILITADAS = true
        const val PRIMEIRA_EXECUCAO = true
    }

    // ========================================================================
    // Emprego Ativo
    // ========================================================================

    override suspend fun obterEmpregoAtivoId(): Long? {
        return context.dataStore.data.first()[PreferencesKeys.EMPREGO_ATIVO_ID]
    }

    override suspend fun definirEmpregoAtivoId(empregoId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMPREGO_ATIVO_ID] = empregoId
        }
    }

    override fun observarEmpregoAtivoId(): Flow<Long?> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.EMPREGO_ATIVO_ID]
        }
    }

    override suspend fun limparEmpregoAtivo() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.EMPREGO_ATIVO_ID)
        }
    }

    // ========================================================================
    // Primeira Execução / Onboarding
    // ========================================================================

    override suspend fun isPrimeiraExecucao(): Boolean {
        // Considera primeira execução se o onboarding ainda não foi concluído
        val onboardingConcluido = context.dataStore.data
            .first()[PreferencesKeys.ONBOARDING_CONCLUIDO] ?: false
        return !onboardingConcluido
    }

    override suspend fun marcarOnboardingConcluido() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_CONCLUIDO] = true
            preferences[PreferencesKeys.PRIMEIRA_EXECUCAO] = false
        }
    }

    override fun observarPrimeiraExecucao(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            // Inverte o valor: se onboarding não foi concluído, é primeira execução
            !(preferences[PreferencesKeys.ONBOARDING_CONCLUIDO] ?: false)
        }
    }

    // ========================================================================
    // Configurações Gerais do App
    // ========================================================================

    override suspend fun obterTema(): String {
        return context.dataStore.data.first()[PreferencesKeys.TEMA] ?: Defaults.TEMA
    }

    override suspend fun definirTema(tema: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMA] = tema
        }
    }

    override fun observarTema(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TEMA] ?: Defaults.TEMA
        }
    }

    override suspend fun isNotificacoesHabilitadas(): Boolean {
        return context.dataStore.data.first()[PreferencesKeys.NOTIFICACOES_HABILITADAS]
            ?: Defaults.NOTIFICACOES_HABILITADAS
    }

    override suspend fun definirNotificacoesHabilitadas(habilitadas: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICACOES_HABILITADAS] = habilitadas
        }
    }

    override fun observarNotificacoesHabilitadas(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.NOTIFICACOES_HABILITADAS]
                ?: Defaults.NOTIFICACOES_HABILITADAS
        }
    }

    // ========================================================================
    // Utilitários
    // ========================================================================

    override suspend fun limparTudo() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
