// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/MeuPontoApplication.kt
package br.com.tlmacedo.meuponto

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import br.com.tlmacedo.meuponto.domain.service.MigracaoManager
import br.com.tlmacedo.meuponto.util.logging.CrashlyticsTree
import br.com.tlmacedo.meuponto.worker.TrashCleanupWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Classe principal da aplicação MeuPonto.
 *
 * Implementa [Configuration.Provider] para integração entre Hilt e WorkManager,
 * permitindo injeção de dependências nos Workers via [HiltWorkerFactory].
 *
 * ## Correção aplicada (12.0.0):
 * Removido `kotlinx.coroutines.delay(1000)` que era usado como workaround
 * para aguardar inicialização do WorkManager. Como esta classe implementa
 * [Configuration.Provider], o WorkManager usa [workManagerConfiguration]
 * antes mesmo do [onCreate] ser chamado — o delay era desnecessário e frágil
 * (poderia falhar em dispositivos lentos ou ser removido em dispositivos rápidos
 * onde o delay seria maior que o necessário).
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 11.0.0 - Adicionado suporte a WorkManager com Hilt
 * @updated 12.0.0 - Removido delay desnecessário no agendamento do TrashCleanupWorker
 */
@HiltAndroidApp
class MeuPontoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var migracaoManager: MigracaoManager

    /**
     * Escopo de coroutine vinculado ao ciclo de vida da aplicação.
     * Usa [SupervisorJob] para que falhas em uma coroutine não cancelem as demais.
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Configuração do WorkManager com suporte a injeção de dependências via Hilt.
     *
     * Esta propriedade é acessada pelo WorkManager antes do [onCreate],
     * garantindo que [HiltWorkerFactory] esteja disponível quando qualquer
     * Worker for instanciado.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) android.util.Log.DEBUG
                else android.util.Log.INFO
            )
            .build()

    override fun onCreate() {
        super.onCreate()
        inicializarLogging()
        executarMigracoes()
        agendarWorkers()
    }

    /**
     * Inicializa o Timber com DebugTree em debug e CrashlyticsTree em release.
     */
    private fun inicializarLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
        Timber.d("MeuPonto Application iniciada")
    }

    /**
     * Executa migrações de dados pendentes em background.
     * Falhas são capturadas e registradas sem propagar para o escopo principal.
     */
    private fun executarMigracoes() {
        applicationScope.launch {
            try {
                migracaoManager.executarMigracoesPendentes()
            } catch (e: Exception) {
                Timber.e(e, "Erro ao executar migrações pendentes")
            }
        }
    }

    /**
     * Agenda workers periódicos da aplicação.
     *
     * ## Correção (12.0.0):
     * O [kotlinx.coroutines.delay] de 1000ms foi removido. Como esta classe
     * implementa [Configuration.Provider], o WorkManager já está configurado
     * com [HiltWorkerFactory] antes do [onCreate] ser chamado.
     * O delay era um workaround desnecessário e frágil.
     */
    private fun agendarWorkers() {
        applicationScope.launch {
            // ✅ Correto: sem delay — WorkManager já está pronto via Configuration.Provider
            // ❌ Errado (original): kotlinx.coroutines.delay(1000) antes do schedule
            TrashCleanupWorker.schedule(this@MeuPontoApplication)
        }
    }
}