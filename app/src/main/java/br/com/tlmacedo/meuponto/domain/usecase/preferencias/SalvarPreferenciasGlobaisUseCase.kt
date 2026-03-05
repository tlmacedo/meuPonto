// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/preferencias/SalvarPreferenciasGlobaisUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.preferencias

import br.com.tlmacedo.meuponto.data.local.datastore.PreferenciasGlobaisDataStore
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoData
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.FormatoHora
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais.TemaEscuro
import timber.log.Timber
import java.time.DayOfWeek
import javax.inject.Inject

/**
 * UseCase para salvar preferências globais do aplicativo.
 *
 * @author Thiago
 * @since 8.1.0
 */
class SalvarPreferenciasGlobaisUseCase @Inject constructor(
    private val dataStore: PreferenciasGlobaisDataStore
) {
    suspend fun salvarAparencia(
        temaEscuro: TemaEscuro,
        usarCoresDoSistema: Boolean,
        corDestaque: Long
    ) {
        Timber.d("UseCase: Salvando aparência")
        dataStore.salvarAparencia(temaEscuro, usarCoresDoSistema, corDestaque)
    }

    suspend fun salvarNotificacoes(
        lembretePontoAtivo: Boolean,
        alertaFeriadoAtivo: Boolean,
        alertaBancoHorasAtivo: Boolean,
        antecedenciaAlertaFeriadoDias: Int
    ) {
        Timber.d("UseCase: Salvando notificações")
        dataStore.salvarNotificacoes(
            lembretePontoAtivo,
            alertaFeriadoAtivo,
            alertaBancoHorasAtivo,
            antecedenciaAlertaFeriadoDias
        )
    }

    suspend fun salvarLocalizacao(
        nome: String,
        latitude: Double?,
        longitude: Double?,
        raioGeofencing: Int,
        registroAutomatico: Boolean
    ) {
        Timber.d("UseCase: Salvando localização")
        dataStore.salvarLocalizacao(nome, latitude, longitude, raioGeofencing, registroAutomatico)
    }

    suspend fun salvarFormatos(
        formatoData: FormatoData,
        formatoHora: FormatoHora,
        primeiroDiaSemana: DayOfWeek
    ) {
        Timber.d("UseCase: Salvando formatos")
        dataStore.salvarFormatos(formatoData, formatoHora, primeiroDiaSemana)
    }

    suspend fun salvarBackup(backupAutomaticoAtivo: Boolean) {
        Timber.d("UseCase: Salvando backup")
        dataStore.salvarBackup(backupAutomaticoAtivo)
    }

    suspend fun registrarBackupRealizado() {
        Timber.d("UseCase: Registrando backup realizado")
        dataStore.registrarBackupRealizado()
    }
}
