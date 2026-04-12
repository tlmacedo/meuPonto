package br.com.tlmacedo.meuponto.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.tlmacedo.meuponto.domain.repository.CloudBackupRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Worker para realizar o backup na nuvem em segundo plano.
 *
 * @author Thiago
 * @since 12.0.0
 */
@HiltWorker
class CloudBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cloudBackupRepository: CloudBackupRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("Iniciando CloudBackupWorker")
        
        return try {
            if (!cloudBackupRepository.isUsuarioAutenticado()) {
                Timber.w("Backup cancelado: usuário não autenticado")
                return Result.failure()
            }

            // Primeiro sincroniza fotos
            cloudBackupRepository.sincronizarFotos()

            // Depois faz o backup do banco
            cloudBackupRepository.uploadBackup()
                .fold(
                    onSuccess = {
                        Timber.d("Backup na nuvem concluído com sucesso")
                        Result.success()
                    },
                    onFailure = { e ->
                        Timber.e(e, "Erro ao realizar backup na nuvem")
                        if (runAttemptCount < 3) Result.retry() else Result.failure()
                    }
                )
        } catch (e: Exception) {
            Timber.e(e, "Falha crítica no CloudBackupWorker")
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "CloudBackupWorker"
    }
}
