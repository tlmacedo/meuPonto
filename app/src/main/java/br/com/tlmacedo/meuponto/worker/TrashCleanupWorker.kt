// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/worker/TrashCleanupWorker.kt
package br.com.tlmacedo.meuponto.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.tlmacedo.meuponto.util.foto.ImageTrashManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Worker para limpeza automática da lixeira de imagens.
 *
 * Executa diariamente e remove arquivos com mais de 30 dias.
 *
 * @author Thiago
 * @since 11.0.0
 */
@HiltWorker
class TrashCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageTrashManager: ImageTrashManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "trash_cleanup_worker"
        private const val REPEAT_INTERVAL_HOURS = 24L

        /**
         * Agenda o worker para execução periódica.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<TrashCleanupWorker>(
                REPEAT_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

            Timber.d("TrashCleanupWorker agendado para execução a cada $REPEAT_INTERVAL_HOURS horas")
        }

        /**
         * Cancela o worker.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Timber.d("TrashCleanupWorker cancelado")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Iniciando limpeza da lixeira...")

            val removedCount = imageTrashManager.cleanupExpiredFiles()

            Timber.i("Limpeza da lixeira concluída: $removedCount arquivos removidos")

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Erro na limpeza da lixeira")
            Result.retry()
        }
    }
}
