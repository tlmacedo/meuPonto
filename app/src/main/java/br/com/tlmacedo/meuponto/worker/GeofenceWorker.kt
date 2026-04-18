package br.com.tlmacedo.meuponto.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import com.google.android.gms.location.Geofence
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Worker para realizar o registro automático de ponto baseado em Geofence.
 */
@HiltWorker
class GeofenceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pontoRepository: PontoRepository,
    private val empregoRepository: EmpregoRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val transitionType = inputData.getInt(KEY_TRANSITION_TYPE, -1)
        Timber.d("GeofenceWorker processando transição: $transitionType")

        return try {
            // 1. Busca o emprego ativo principal
            val empregoAtivo = empregoRepository.observarTodos().firstOrNull()?.find { it.ativo }
                ?: return Result.failure()

            // 2. Cria o registro de ponto
            val agora = LocalDateTime.now()
            val observacao = if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                "Registro automático: Entrada detectada via Geofencing"
            } else {
                "Registro automático: Saída detectada via Geofencing"
            }

            val ponto = Ponto.criar(
                empregoId = empregoAtivo.id,
                dataHora = agora,
                observacao = observacao
            )

            // 3. Salva no banco de dados
            pontoRepository.inserir(ponto)
            
            Timber.i("Ponto registrado automaticamente via Geofencing: ${agora}")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao registrar ponto via Geofencing")
            Result.retry()
        }
    }

    companion object {
        const val KEY_TRANSITION_TYPE = "transition_type"
    }
}
