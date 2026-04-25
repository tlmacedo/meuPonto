// path: app/src/main/java/br/com/tlmacedo/meuponto/worker/ChamadoStatusPollingWorker.kt
package br.com.tlmacedo.meuponto.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import br.com.tlmacedo.meuponto.domain.service.SistemaNotificacaoService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

@HiltWorker
class ChamadoStatusPollingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val chamadoRepository: ChamadoRepository,
    private val sistemaNotificacaoService: SistemaNotificacaoService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Flow → List via firstOrNull()
            val chamadosAbertos = chamadoRepository
                .listarChamadosAbertos()
                .firstOrNull()
                ?: emptyList()

            for (chamado in chamadosAbertos) {
                val statusAnterior: StatusChamado = chamado.status

                // sincronizarChamado recebe String (identificador), não Long
                val resultSinc = chamadoRepository.sincronizarChamado(chamado.identificador)

                resultSinc.onSuccess { chamadoAtualizado ->
                    if (chamadoAtualizado.status != statusAnterior) {
                        sistemaNotificacaoService.notificarMudancaStatusChamado(
                            identificador = chamadoAtualizado.identificador,
                            statusAnterior = statusAnterior,
                            statusNovo = chamadoAtualizado.status
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Erro no ChamadoStatusPollingWorker")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "ChamadoStatusPollingWorker"
    }
}