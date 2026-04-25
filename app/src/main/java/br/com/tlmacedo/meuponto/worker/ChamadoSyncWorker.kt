// path: app/src/main/java/br/com/tlmacedo/meuponto/worker/ChamadoSyncWorker.kt
package br.com.tlmacedo.meuponto.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import br.com.tlmacedo.meuponto.domain.service.EmailNotificacaoService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class ChamadoSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val chamadoRepository: ChamadoRepository,
    private val emailService: EmailNotificacaoService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val chamadoId = inputData.getLong(KEY_CHAMADO_ID, -1L)
        if (chamadoId == -1L) {
            Timber.e("ChamadoSyncWorker: ID do chamado inválido. Falha.")
            return Result.failure()
        }

        return try {
            val chamado = chamadoRepository.observarPorId(chamadoId).firstOrNull()
                ?: run {
                    Timber.e("ChamadoSyncWorker: Chamado com ID $chamadoId não encontrado. Falha.")
                    return Result.failure()
                }

            // Envia para API
            chamadoRepository.sincronizarChamado(chamado.identificador)
                .fold(
                    onSuccess = {
                        emailService.notificarNovoChamado(chamado)
                        Timber.i("Chamado ${chamado.identificador} sincronizado com sucesso")
                        Result.success()
                    },
                    onFailure = { e ->
                        Timber.e(
                            e,
                            "Falha ao sincronizar chamado (tentativa: $runAttemptCount)"
                        )

                        val errorMessage = e.message ?: ""
                        if (errorMessage.contains("401") || errorMessage.contains(
                                "unauthorized",
                                ignoreCase = true
                            )
                        ) {
                            return@fold Result.failure()
                        }

                        if (runAttemptCount < 3) Result.retry() else Result.failure()
                    }
                )
        } catch (e: Exception) {
            Timber.e(e, "Erro crítico no ChamadoSyncWorker para chamado ID $chamadoId")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_CHAMADO_ID = "chamado_id"

        fun enqueue(context: Context, chamadoId: Long) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ChamadoSyncWorker>()
                .setInputData(workDataOf(KEY_CHAMADO_ID to chamadoId))
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}