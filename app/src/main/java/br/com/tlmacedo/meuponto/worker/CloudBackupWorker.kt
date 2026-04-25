// path: app/src/main/java/br/com/tlmacedo/meuponto/worker/CloudBackupWorker.kt

package br.com.tlmacedo.meuponto.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.tlmacedo.meuponto.domain.repository.CloudBackupRepository
import br.com.tlmacedo.meuponto.domain.service.SistemaNotificacaoService
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
    private val cloudBackupRepository: CloudBackupRepository,
    private val sistemaNotificacaoService: SistemaNotificacaoService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("Iniciando CloudBackupWorker")

        return try {
            if (!cloudBackupRepository.isUsuarioAutenticado()) {
                Timber.w("Backup cancelado: usuário não autenticado")
                // Se o usuário não está autenticado, não faz sentido retentar.
                // Pode ser útil notificar o usuário para que ele faça login.
                sistemaNotificacaoService.notificarErroBackup("Backup cancelado: usuário não autenticado. Por favor, faça login novamente.")
                return Result.failure()
            }

            // Primeiro sincroniza fotos
            cloudBackupRepository.sincronizarFotos()

            // Depois faz o backup do banco
            return cloudBackupRepository.uploadBackup()
                .fold(
                    onSuccess = {
                        Timber.d("Backup na nuvem concluído com sucesso")
                        Result.success()
                    },
                    onFailure = { e ->
                        Timber.e(
                            e,
                            "Erro ao realizar backup na nuvem (tentativa: $runAttemptCount)"
                        )

                        val errorMessage = e.message ?: "Erro desconhecido"

                        if (errorMessage.contains("401") || errorMessage.contains(
                                "unauthorized",
                                ignoreCase = true
                            )
                        ) {
                            return@fold Result.failure()
                        }

                        // Notifica o usuário após um certo número de tentativas falhas
                        if (runAttemptCount >= 2) {
                            sistemaNotificacaoService.notificarErroBackup("Falha no backup na nuvem: $errorMessage")
                        }


                        if (runAttemptCount < 3) Result.retry() else Result.failure()
                    }
                )
        } catch (e: Exception) {
            Timber.e(e, "Falha crítica no CloudBackupWorker")

            val errorMessage = e.message ?: "Falha crítica"

            if (runAttemptCount >= 2) {
                sistemaNotificacaoService.notificarErroBackup("Falha crítica no backup na nuvem: $errorMessage")
            }

            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "CloudBackupWorker"
    }
}