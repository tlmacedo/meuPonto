// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/MeuPontoApplication.kt
package br.com.tlmacedo.meuponto

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import br.com.tlmacedo.meuponto.domain.service.MigracaoManager
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
 * Responsável pela inicialização do Hilt para injeção de dependências
 * e configuração inicial do aplicativo.
 *
 * ## Inicialização do WorkManager
 * O app implementa [Configuration.Provider] com [HiltWorkerFactory], o que
 * significa que o WorkManager é inicializado de forma lazy e já está disponível
 * antes mesmo de [onCreate] ser invocado. Por isso, não é necessário nenhum
 * delay artificial antes de agendar workers.
 *
 * ## Logging
 * Em debug: [Timber.DebugTree] (log no Logcat).
 * Em release: adicionar [CrashlyticsTree] na Fase 6 do plano de desenvolvimento.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 11.0.0 - Adicionado suporte a WorkManager com Hilt
 * @updated 12.0.0 - Removido delay(1000) desnecessário no agendamento do
 *                   TrashCleanupWorker; o WorkManager já está disponível via
 *                   Configuration.Provider antes do onCreate
 */
@HiltAndroidApp
class MeuPontoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var migracaoManager: MigracaoManager

    /**
     * Escopo de coroutines com ciclo de vida da aplicação.
     * Usa [SupervisorJob] para que falhas individuais não cancelem outras coroutines.
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Configuração do WorkManager usando [HiltWorkerFactory].
     *
     * Ao implementar [Configuration.Provider], o WorkManager é inicializado
     * de forma lazy pelo sistema na primeira chamada, já com o factory do Hilt.
     * Isso garante que workers com injeção de dependências funcionem corretamente.
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
     * Inicializa o Timber conforme o build type.
     *
     * Em debug: planta [Timber.DebugTree] com log completo no Logcat.
     * Em release: não planta nenhuma árvore até que o Crashlytics seja
     * configurado na Fase 6 do plano de desenvolvimento.
     */
    private fun inicializarLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("MeuPonto Application iniciada")
    }

    /**
     * Executa migrações de dados pendentes em background.
     *
     * Falhas são capturadas e registradas via Timber sem propagar
     * para o escopo principal da aplicação.
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
     * O WorkManager já está disponível via [Configuration.Provider], portanto
     * não é necessário nenhum delay antes de chamar [TrashCleanupWorker.schedule].
     */
    private fun agendarWorkers() {
        applicationScope.launch {
            TrashCleanupWorker.schedule(this@MeuPontoApplication)
        }
    }
}