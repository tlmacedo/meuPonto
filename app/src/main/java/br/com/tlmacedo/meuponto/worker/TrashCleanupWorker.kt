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
 * Executa a cada [REPEAT_INTERVAL_HOURS] horas e remove permanentemente
 * arquivos com mais de 30 dias na lixeira (conforme política do
 * [ImageTrashManager]).
 *
 * ## Constraints
 * - [Constraints.Builder.setRequiresBatteryNotLow]: evita executar com bateria baixa
 * - [Constraints.Builder.setRequiresStorageNotLow]: evita executar se o
 *   armazenamento estiver cheio, pois a limpeza pode precisar de espaço temporário
 *
 * @param imageTrashManager Gerenciador de lixeira de imagens injetado pelo Hilt
 *
 * @author Thiago
 * @since 11.0.0
 * @updated 12.0.0 - Adicionado constraint setRequiresStorageNotLow
 */
@HiltWorker
class TrashCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageTrashManager: ImageTrashManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        /** Nome único do trabalho periódico no WorkManager */
        const val WORK_NAME = "trash_cleanup_worker"

        /** Intervalo de execução em horas */
        private const val REPEAT_INTERVAL_HOURS = 24L

        /**
         * Agenda o worker para execução periódica.
         *
         * Usa [ExistingPeriodicWorkPolicy.KEEP] para não reagendar se já estiver
         * agendado com os mesmos parâmetros, evitando duplicação.
         *
         * @param context Contexto da aplicação
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
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
         * Cancela o worker periódico.
         *
         * @param context Contexto da aplicação
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Timber.d("TrashCleanupWorker cancelado")
        }
    }

    /**
     * Executa a limpeza dos arquivos expirados na lixeira.
     *
     * Em caso de exceção, retorna [Result.retry] para que o WorkManager
     * tente novamente na próxima janela de execução.
     *
     * @return [Result.success] se a limpeza foi concluída (mesmo sem remover nada),
     *         ou [Result.retry] em caso de erro inesperado
     */
    override suspend fun doWork(): Result {
        return try {
            Timber.d("Iniciando limpeza automática da lixeira...")
            val removedCount = imageTrashManager.cleanupExpiredFiles()
            Timber.i("Limpeza da lixeira concluída: $removedCount arquivo(s) removido(s)")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Erro na limpeza automática da lixeira")
            Result.retry()
        }
    }
}