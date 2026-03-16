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
 * @author Thiago
 * @since 1.0.0
 * @updated 11.0.0 - Adicionado suporte a WorkManager com Hilt
 */
@HiltAndroidApp
class MeuPontoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var migracaoManager: MigracaoManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Configuração do WorkManager com HiltWorkerFactory
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

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("MeuPonto Application iniciada")

        // Agendar limpeza da lixeira (executa após o WorkManager estar configurado)
        TrashCleanupWorker.schedule(this)

        // Executa migrações pendentes em background
        executarMigracoes()
    }

    private fun executarMigracoes() {
        applicationScope.launch {
            try {
                migracaoManager.executarMigracoesPendentes()
            } catch (e: Exception) {
                Timber.e(e, "Erro ao executar migrações")
            }
        }
    }
}
