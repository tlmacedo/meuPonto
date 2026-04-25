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
            val chamadosAbertos = chamadoRepository
                .listarChamadosAbertos()
                .firstOrNull()
                ?: emptyList()

            var allSucceeded = true

            for (chamado in chamadosAbertos) {
                val statusAnterior: StatusChamado = chamado.status

                val resultSinc = chamadoRepository.sincronizarChamado(chamado.identificador)

                resultSinc.onSuccess { chamadoAtualizado ->
                    if (chamadoAtualizado.status != statusAnterior) {
                        sistemaNotificacaoService.notificarMudancaStatusChamado(
                            identificador = chamadoAtualizado.identificador,
                            statusAnterior = statusAnterior,
                            statusNovo = chamadoAtualizado.status
                        )
                    }
                }.onFailure { e ->
                    Timber.e(
                        e,
                        "Falha ao sincronizar status do chamado ${chamado.identificador} durante o polling."
                    )
                    allSucceeded = false
                    val errorMessage = e.message ?: ""
                    if (errorMessage.contains("401") || errorMessage.contains(
                            "unauthorized",
                            ignoreCase = true
                        )
                    ) {
                        // Se for erro de autenticação, não adianta tentar novamente imediatamente
                        allSucceeded = false
                    }
                }
            }

            if (allSucceeded) Result.success() else Result.retry() // Se algum falhou, tenta novamente mais tarde
        } catch (e: Exception) {
            Timber.e(e, "Erro crítico no ChamadoStatusPollingWorker")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "ChamadoStatusPollingWorker"
    }
}