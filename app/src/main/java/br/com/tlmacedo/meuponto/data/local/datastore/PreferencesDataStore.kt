// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/datastore/PreferencesDataStore.kt
package br.com.tlmacedo.meuponto.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoJornada
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Extensão para criar o DataStore de preferências */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "meuponto_preferences"
)

/**
 * Gerenciador de preferências do aplicativo usando DataStore.
 *
 * Responsável por persistir e recuperar as configurações do usuário
 * de forma assíncrona e type-safe utilizando Jetpack DataStore.
 *
 * @property context Contexto da aplicação para acesso ao DataStore
 *
 * @author Thiago
 * @since 1.0.0
 */
@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ========================================================================
    // Chaves de preferências
    // ========================================================================

    private object Keys {
        val CARGA_HORARIA_DIARIA = intPreferencesKey("carga_horaria_diaria")
        val CARGA_HORARIA_SEMANAL = intPreferencesKey("carga_horaria_semanal")
        val INTERVALO_MINIMO = intPreferencesKey("intervalo_minimo")
        val TOLERANCIA = intPreferencesKey("tolerancia")
        val JORNADA_MAXIMA = intPreferencesKey("jornada_maxima")
        val HORA_ENTRADA_PADRAO = intPreferencesKey("hora_entrada_padrao")
        val MINUTO_ENTRADA_PADRAO = intPreferencesKey("minuto_entrada_padrao")
        val HORA_SAIDA_PADRAO = intPreferencesKey("hora_saida_padrao")
        val MINUTO_SAIDA_PADRAO = intPreferencesKey("minuto_saida_padrao")
        val TEMA_ESCURO = booleanPreferencesKey("tema_escuro")
        val NOTIFICACOES_ATIVAS = booleanPreferencesKey("notificacoes_ativas")
        val PRIMEIRO_ACESSO = booleanPreferencesKey("primeiro_acesso")
    }

    // ========================================================================
    // Configuração de Jornada
    // ========================================================================

    /**
     * Observa as configurações de jornada de forma reativa.
     *
     * @return Flow que emite a configuração atual sempre que houver mudanças
     */
    val configuracaoJornada: Flow<ConfiguracaoJornada> = context.dataStore.data.map { prefs ->
        ConfiguracaoJornada(
            cargaHorariaDiariaMinutos = prefs[Keys.CARGA_HORARIA_DIARIA]
                ?: ConfiguracaoJornada.DEFAULT_CARGA_HORARIA_DIARIA,
            cargaHorariaSemanalMinutos = prefs[Keys.CARGA_HORARIA_SEMANAL]
                ?: ConfiguracaoJornada.DEFAULT_CARGA_HORARIA_SEMANAL,
            intervaloMinimoMinutos = prefs[Keys.INTERVALO_MINIMO]
                ?: ConfiguracaoJornada.DEFAULT_INTERVALO_MINIMO,
            toleranciaMinutos = prefs[Keys.TOLERANCIA]
                ?: ConfiguracaoJornada.DEFAULT_TOLERANCIA,
            jornadaMaximaDiariaMinutos = prefs[Keys.JORNADA_MAXIMA]
                ?: ConfiguracaoJornada.DEFAULT_JORNADA_MAXIMA,
            horaEntradaPadrao = prefs[Keys.HORA_ENTRADA_PADRAO]
                ?: ConfiguracaoJornada.DEFAULT_HORA_ENTRADA,
            minutoEntradaPadrao = prefs[Keys.MINUTO_ENTRADA_PADRAO]
                ?: ConfiguracaoJornada.DEFAULT_MINUTO_ENTRADA,
            horaSaidaPadrao = prefs[Keys.HORA_SAIDA_PADRAO]
                ?: ConfiguracaoJornada.DEFAULT_HORA_SAIDA,
            minutoSaidaPadrao = prefs[Keys.MINUTO_SAIDA_PADRAO]
                ?: ConfiguracaoJornada.DEFAULT_MINUTO_SAIDA
        )
    }

    /**
     * Salva as configurações de jornada.
     *
     * @param configuracao Configuração a ser salva
     */
    suspend fun salvarConfiguracaoJornada(configuracao: ConfiguracaoJornada) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CARGA_HORARIA_DIARIA] = configuracao.cargaHorariaDiariaMinutos
            prefs[Keys.CARGA_HORARIA_SEMANAL] = configuracao.cargaHorariaSemanalMinutos
            prefs[Keys.INTERVALO_MINIMO] = configuracao.intervaloMinimoMinutos
            prefs[Keys.TOLERANCIA] = configuracao.toleranciaMinutos
            prefs[Keys.JORNADA_MAXIMA] = configuracao.jornadaMaximaDiariaMinutos
            prefs[Keys.HORA_ENTRADA_PADRAO] = configuracao.horaEntradaPadrao
            prefs[Keys.MINUTO_ENTRADA_PADRAO] = configuracao.minutoEntradaPadrao
            prefs[Keys.HORA_SAIDA_PADRAO] = configuracao.horaSaidaPadrao
            prefs[Keys.MINUTO_SAIDA_PADRAO] = configuracao.minutoSaidaPadrao
        }
    }

    // ========================================================================
    // Preferências de Tema
    // ========================================================================

    /**
     * Observa a preferência de tema escuro.
     *
     * @return Flow que emite true se tema escuro está ativo
     */
    val temaEscuro: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.TEMA_ESCURO] ?: false
    }

    /**
     * Define a preferência de tema escuro.
     *
     * @param ativo Se true, ativa o tema escuro
     */
    suspend fun setTemaEscuro(ativo: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TEMA_ESCURO] = ativo
        }
    }

    // ========================================================================
    // Preferências de Notificações
    // ========================================================================

    /**
     * Observa a preferência de notificações.
     *
     * @return Flow que emite true se notificações estão ativas
     */
    val notificacoesAtivas: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICACOES_ATIVAS] ?: true
    }

    /**
     * Define a preferência de notificações.
     *
     * @param ativas Se true, ativa as notificações
     */
    suspend fun setNotificacoesAtivas(ativas: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICACOES_ATIVAS] = ativas
        }
    }

    // ========================================================================
    // Primeiro Acesso
    // ========================================================================

    /**
     * Observa se é o primeiro acesso do usuário.
     *
     * @return Flow que emite true se é o primeiro acesso
     */
    val isPrimeiroAcesso: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIMEIRO_ACESSO] ?: true
    }

    /**
     * Marca que o primeiro acesso foi concluído.
     */
    suspend fun marcarPrimeiroAcessoConcluido() {
        context.dataStore.edit { prefs ->
            prefs[Keys.PRIMEIRO_ACESSO] = false
        }
    }
}
