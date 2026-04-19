// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/PreferenciasRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "meu_ponto_preferences"
)

/**
 * Implementação do repositório de preferências usando DataStore.
 *
 * @updated 12.0.0 - Adicionadas preferências de Autenticação e Biometria
 */
@Singleton
class PreferenciasRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferenciasRepository {

    private object PreferencesKeys {
        val EMPREGO_ATIVO_ID = longPreferencesKey("emprego_ativo_id")
        val PRIMEIRA_EXECUCAO = booleanPreferencesKey("primeira_execucao")
        val ONBOARDING_CONCLUIDO = booleanPreferencesKey("onboarding_concluido")
        val TEMA = stringPreferencesKey("tema")
        val NOTIFICACOES_HABILITADAS = booleanPreferencesKey("notificacoes_habilitadas")
        
        // Fase 1
        val LEMBRAR_ME = booleanPreferencesKey("lembrar_me")
        val ULTIMO_EMAIL_LOGADO = stringPreferencesKey("ultimo_email_logado")
        val BIOMETRIA_HABILITADA = booleanPreferencesKey("biometria_habilitada")
        val BLOQUEIO_AUTOMATICO_HABILITADO = booleanPreferencesKey("bloqueio_automatico_habilitada")
        val OCULTAR_PREVIEW_HABILITADO = booleanPreferencesKey("ocultar_preview_habilitada")

        // Jornada
        val CARGA_HORARIA_PADRAO = intPreferencesKey("carga_horaria_padrao")
        val INTERVALO_MINIMO_PADRAO = intPreferencesKey("intervalo_minimo_padrao")
        val TOLERANCIA_GERAL_PADRAO = intPreferencesKey("tolerancia_geral_padrao")
    }

    private object Defaults {
        const val TEMA = "system"
        const val NOTIFICACOES_HABILITADAS = true
        const val PRIMEIRA_EXECUCAO = true
        const val LEMBRAR_ME = false
        const val BIOMETRIA_HABILITADA = false
        const val BLOQUEIO_AUTOMATICO_HABILITADA = false
        const val OCULTAR_PREVIEW_HABILITADA = false
        
        const val CARGA_HORARIA_PADRAO = 480 // 8 horas
        const val INTERVALO_MINIMO_PADRAO = 60 // 1 hora
        const val TOLERANCIA_GERAL_PADRAO = 10 // 10 minutos
    }

    // ========================================================================
    // Autenticação e Segurança (Fase 1)
    // ========================================================================

    override suspend fun isLembrarMeAtivo(): Boolean {
        return context.dataStore.data.first()[PreferencesKeys.LEMBRAR_ME] ?: Defaults.LEMBRAR_ME
    }

    override suspend fun definirLembrarMe(ativo: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LEMBRAR_ME] = ativo
        }
    }

    override fun observarLembrarMe(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LEMBRAR_ME] ?: Defaults.LEMBRAR_ME
        }
    }

    override suspend fun obterUltimoEmailLogado(): String? {
        return context.dataStore.data.first()[PreferencesKeys.ULTIMO_EMAIL_LOGADO]
    }

    override suspend fun definirUltimoEmailLogado(email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ULTIMO_EMAIL_LOGADO] = email
        }
    }

    override suspend fun isBiometriaHabilitada(): Boolean {
        return context.dataStore.data.first()[PreferencesKeys.BIOMETRIA_HABILITADA]
            ?: Defaults.BIOMETRIA_HABILITADA
    }

    override suspend fun definirBiometriaHabilitada(habilitado: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIA_HABILITADA] = habilitado
        }
    }

    override fun observarBiometriaHabilitada(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.BIOMETRIA_HABILITADA] ?: Defaults.BIOMETRIA_HABILITADA
        }
    }

    override suspend fun isBloqueioAutomaticoHabilitado(): Boolean {
        return context.dataStore.data.first()[PreferencesKeys.BLOQUEIO_AUTOMATICO_HABILITADO]
            ?: Defaults.BLOQUEIO_AUTOMATICO_HABILITADA
    }

    override suspend fun definirBloqueioAutomaticoHabilitado(habilitado: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BLOQUEIO_AUTOMATICO_HABILITADO] = habilitado
        }
    }

    override fun observarBloqueioAutomaticoHabilitado(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.BLOQUEIO_AUTOMATICO_HABILITADO]
                ?: Defaults.BLOQUEIO_AUTOMATICO_HABILITADA
        }
    }

    override suspend fun isOcultarPreviewHabilitado(): Boolean {
        return context.dataStore.data.first()[PreferencesKeys.OCULTAR_PREVIEW_HABILITADO]
            ?: Defaults.OCULTAR_PREVIEW_HABILITADA
    }

    override suspend fun definirOcultarPreviewHabilitado(habilitado: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OCULTAR_PREVIEW_HABILITADO] = habilitado
        }
    }

    override fun observarOcultarPreviewHabilitado(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.OCULTAR_PREVIEW_HABILITADO]
                ?: Defaults.OCULTAR_PREVIEW_HABILITADA
        }
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
    // Configurações de Jornada (Padrões Globais)
    // ========================================================================

    override suspend fun obterCargaHorariaPadrao(): Int {
        return context.dataStore.data.first()[PreferencesKeys.CARGA_HORARIA_PADRAO]
            ?: Defaults.CARGA_HORARIA_PADRAO
    }

    override suspend fun definirCargaHorariaPadrao(minutos: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CARGA_HORARIA_PADRAO] = minutos
        }
    }

    override fun observarCargaHorariaPadrao(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CARGA_HORARIA_PADRAO] ?: Defaults.CARGA_HORARIA_PADRAO
        }
    }

    override suspend fun obterIntervaloMinimoPadrao(): Int {
        return context.dataStore.data.first()[PreferencesKeys.INTERVALO_MINIMO_PADRAO]
            ?: Defaults.INTERVALO_MINIMO_PADRAO
    }

    override suspend fun definirIntervaloMinimoPadrao(minutos: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INTERVALO_MINIMO_PADRAO] = minutos
        }
    }

    override fun observarIntervaloMinimoPadrao(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.INTERVALO_MINIMO_PADRAO] ?: Defaults.INTERVALO_MINIMO_PADRAO
        }
    }

    override suspend fun obterToleranciaGeralPadrao(): Int {
        return context.dataStore.data.first()[PreferencesKeys.TOLERANCIA_GERAL_PADRAO]
            ?: Defaults.TOLERANCIA_GERAL_PADRAO
    }

    override suspend fun definirToleranciaGeralPadrao(minutos: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOLERANCIA_GERAL_PADRAO] = minutos
        }
    }

    override fun observarToleranciaGeralPadrao(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TOLERANCIA_GERAL_PADRAO] ?: Defaults.TOLERANCIA_GERAL_PADRAO
        }
    }

    override suspend fun limparTudo() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
