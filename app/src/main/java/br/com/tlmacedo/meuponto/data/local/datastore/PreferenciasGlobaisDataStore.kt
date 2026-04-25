// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/data/local/datastore/PreferenciasGlobaisDataStore.kt
package br.com.tlmacedo.meuponto.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoData
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoHora
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.TemaEscuro
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prefsGlobaisDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "meuponto_preferencias_globais"
)

/**
 * Gerenciador de preferências globais do aplicativo.
 *
 * @author Thiago
 * @since 8.1.0
 */
@Singleton
class PreferenciasGlobaisDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val TEMA_ESCURO = intPreferencesKey("tema_escuro")
        val USAR_CORES_SISTEMA = booleanPreferencesKey("usar_cores_sistema")
        val COR_DESTAQUE = longPreferencesKey("cor_destaque")
        val LEMBRETE_PONTO_ATIVO = booleanPreferencesKey("lembrete_ponto_ativo")
        val ALERTA_FERIADO_ATIVO = booleanPreferencesKey("alerta_feriado_ativo")
        val ALERTA_BANCO_HORAS_ATIVO = booleanPreferencesKey("alerta_banco_horas_ativo")
        val ANTECEDENCIA_ALERTA_FERIADO = intPreferencesKey("antecedencia_alerta_feriado")
        val LOCALIZACAO_PADRAO_NOME = stringPreferencesKey("localizacao_padrao_nome")
        val LOCALIZACAO_PADRAO_LAT = doublePreferencesKey("localizacao_padrao_lat")
        val LOCALIZACAO_PADRAO_LONG = doublePreferencesKey("localizacao_padrao_long")
        val RAIO_GEOFENCING = intPreferencesKey("raio_geofencing")
        val REGISTRO_AUTOMATICO_GEOFENCING = booleanPreferencesKey("registro_automatico_geofencing")
        val BACKUP_AUTOMATICO = booleanPreferencesKey("backup_automatico")
        val BACKUP_NUVEM_ATIVO = booleanPreferencesKey("backup_nuvem_ativo")
        val ULTIMO_BACKUP_LOCAL = longPreferencesKey("ultimo_backup_local")
        val ULTIMO_BACKUP_NUVEM = longPreferencesKey("ultimo_backup_nuvem")
        val CONTA_GOOGLE_CONECTADA = stringPreferencesKey("conta_google_conectada")
        val FORMATO_DATA = intPreferencesKey("formato_data")
        val FORMATO_HORA = intPreferencesKey("formato_hora")
        val PRIMEIRO_DIA_SEMANA = intPreferencesKey("primeiro_dia_semana")
    }

    val preferenciasGlobais: Flow<PreferenciasGlobais> =
        context.prefsGlobaisDataStore.data.map { prefs ->
            PreferenciasGlobais(
                temaEscuro = TemaEscuro.fromOrdinal(
                    prefs[Keys.TEMA_ESCURO] ?: TemaEscuro.SISTEMA.ordinal
                ),
                usarCoresDoSistema = prefs[Keys.USAR_CORES_SISTEMA] ?: true,
                corDestaque = prefs[Keys.COR_DESTAQUE] ?: 0xFF6200EE,
                lembretePontoAtivo = prefs[Keys.LEMBRETE_PONTO_ATIVO] ?: true,
                alertaFeriadoAtivo = prefs[Keys.ALERTA_FERIADO_ATIVO] ?: true,
                alertaBancoHorasAtivo = prefs[Keys.ALERTA_BANCO_HORAS_ATIVO] ?: true,
                antecedenciaAlertaFeriadoDias = prefs[Keys.ANTECEDENCIA_ALERTA_FERIADO] ?: 7,
                localizacaoPadraoNome = prefs[Keys.LOCALIZACAO_PADRAO_NOME] ?: "",
                localizacaoPadraoLatitude = prefs[Keys.LOCALIZACAO_PADRAO_LAT],
                localizacaoPadraoLongitude = prefs[Keys.LOCALIZACAO_PADRAO_LONG],
                raioGeofencingMetros = prefs[Keys.RAIO_GEOFENCING] ?: 100,
                registroAutomaticoGeofencing = prefs[Keys.REGISTRO_AUTOMATICO_GEOFENCING] ?: false,
                backupAutomaticoAtivo = prefs[Keys.BACKUP_AUTOMATICO] ?: false,
                backupNuvemAtivo = prefs[Keys.BACKUP_NUVEM_ATIVO] ?: false,
                ultimoBackupLocal = prefs[Keys.ULTIMO_BACKUP_LOCAL] ?: 0L,
                ultimoBackupNuvem = prefs[Keys.ULTIMO_BACKUP_NUVEM] ?: 0L,
                contaGoogleConectada = prefs[Keys.CONTA_GOOGLE_CONECTADA],
                formatoData = FormatoData.fromOrdinal(
                    prefs[Keys.FORMATO_DATA] ?: FormatoData.DD_MM_YYYY.ordinal
                ),
                formatoHora = FormatoHora.fromOrdinal(
                    prefs[Keys.FORMATO_HORA] ?: FormatoHora.H24.ordinal
                ),
                primeiroDiaSemana = DayOfWeek.of(
                    prefs[Keys.PRIMEIRO_DIA_SEMANA] ?: DayOfWeek.SUNDAY.value
                )
            )
        }

    suspend fun salvarAparencia(
        temaEscuro: TemaEscuro,
        usarCoresDoSistema: Boolean,
        corDestaque: Long
    ) {
        Timber.d("Salvando aparência: tema=$temaEscuro")
        context.prefsGlobaisDataStore.edit { prefs ->
            prefs[Keys.TEMA_ESCURO] = temaEscuro.ordinal
            prefs[Keys.USAR_CORES_SISTEMA] = usarCoresDoSistema
            prefs[Keys.COR_DESTAQUE] = corDestaque
        }
    }

    suspend fun salvarNotificacoes(
        lembretePontoAtivo: Boolean,
        alertaFeriadoAtivo: Boolean,
        alertaBancoHorasAtivo: Boolean,
        antecedenciaAlertaFeriadoDias: Int
    ) {
        val antecedenciaValidada = antecedenciaAlertaFeriadoDias.coerceIn(
            PreferenciasGlobais.ANTECEDENCIA_FERIADO_MIN,
            PreferenciasGlobais.ANTECEDENCIA_FERIADO_MAX
        )
        Timber.d("Salvando notificações: lembrete=$lembretePontoAtivo")
        context.prefsGlobaisDataStore.edit { prefs ->
            prefs[Keys.LEMBRETE_PONTO_ATIVO] = lembretePontoAtivo
            prefs[Keys.ALERTA_FERIADO_ATIVO] = alertaFeriadoAtivo
            prefs[Keys.ALERTA_BANCO_HORAS_ATIVO] = alertaBancoHorasAtivo
            prefs[Keys.ANTECEDENCIA_ALERTA_FERIADO] = antecedenciaValidada
        }
    }

    suspend fun salvarLocalizacao(
        nome: String,
        latitude: Double?,
        longitude: Double?,
        raioGeofencing: Int,
        registroAutomatico: Boolean
    ) {
        val raioValidado = raioGeofencing.coerceIn(
            PreferenciasGlobais.RAIO_GEOFENCING_MIN,
            PreferenciasGlobais.RAIO_GEOFENCING_MAX
        )
        Timber.d("Salvando localização: nome=$nome, raio=$raioValidado")
        context.prefsGlobaisDataStore.edit { prefs ->
            prefs[Keys.LOCALIZACAO_PADRAO_NOME] = nome
            if (latitude != null) prefs[Keys.LOCALIZACAO_PADRAO_LAT] = latitude
            else prefs.remove(Keys.LOCALIZACAO_PADRAO_LAT)
            if (longitude != null) prefs[Keys.LOCALIZACAO_PADRAO_LONG] = longitude
            else prefs.remove(Keys.LOCALIZACAO_PADRAO_LONG)
            prefs[Keys.RAIO_GEOFENCING] = raioValidado
            prefs[Keys.REGISTRO_AUTOMATICO_GEOFENCING] = registroAutomatico
        }
    }

    suspend fun salvarFormatos(
        formatoData: FormatoData,
        formatoHora: FormatoHora,
        primeiroDiaSemana: DayOfWeek
    ) {
        Timber.d("Salvando formatos: data=$formatoData, hora=$formatoHora")
        context.prefsGlobaisDataStore.edit { prefs ->
            prefs[Keys.FORMATO_DATA] = formatoData.ordinal
            prefs[Keys.FORMATO_HORA] = formatoHora.ordinal
            prefs[Keys.PRIMEIRO_DIA_SEMANA] = primeiroDiaSemana.value
        }
    }

    suspend fun salvarBackup(
        backupAutomaticoAtivo: Boolean,
        backupNuvemAtivo: Boolean? = null,
        ultimoBackup: Long? = null,
        ultimoBackupNuvem: Long? = null,
        contaGoogle: String? = null
    ) {
        Timber.d("Salvando backup: automatico=$backupAutomaticoAtivo")
        context.prefsGlobaisDataStore.edit { prefs ->
            prefs[Keys.BACKUP_AUTOMATICO] = backupAutomaticoAtivo
            backupNuvemAtivo?.let { prefs[Keys.BACKUP_NUVEM_ATIVO] = it }
            ultimoBackup?.let { prefs[Keys.ULTIMO_BACKUP_LOCAL] = it }
            ultimoBackupNuvem?.let { prefs[Keys.ULTIMO_BACKUP_NUVEM] = it }
            contaGoogle?.let {
                if (it.isEmpty()) prefs.remove(Keys.CONTA_GOOGLE_CONECTADA)
                else prefs[Keys.CONTA_GOOGLE_CONECTADA] = it
            }
        }
    }

    suspend fun registrarBackupRealizado(isNuvem: Boolean = false) {
        val now = System.currentTimeMillis()
        Timber.d("Registrando backup realizado: nuvem=$isNuvem, data=$now")
        context.prefsGlobaisDataStore.edit { prefs ->
            if (isNuvem) {
                prefs[Keys.ULTIMO_BACKUP_NUVEM] = now
            } else {
                prefs[Keys.ULTIMO_BACKUP_LOCAL] = now
            }
        }
    }
}
