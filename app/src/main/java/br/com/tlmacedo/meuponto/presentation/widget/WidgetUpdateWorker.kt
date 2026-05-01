// path: app/src/main/java/br/com/tlmacedo/meuponto/presentation/widget/WidgetUpdateWorker.kt
package br.com.tlmacedo.meuponto.presentation.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularBancoHorasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.saldo.CalcularSaldoDiaUseCase
import br.com.tlmacedo.meuponto.util.helper.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.helper.minutosParaSaldoFormatado
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val Context.widgetDataStore by preferencesDataStore(name = "meuponto_preferences")

/**
 * Worker responsável por atualizar os dados exibidos no widget MeuPonto.
 *
 * Usa os mesmos use cases que a HomeScreen para garantir consistência.
 *
 * @author Thiago
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val empregoRepository: EmpregoRepository,
    private val calcularSaldoDiaUseCase: CalcularSaldoDiaUseCase,
    private val calcularBancoHorasUseCase: CalcularBancoHorasUseCase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Timber.d("WidgetUpdateWorker: iniciando atualização")

        return try {
            // 1. Busca emprego ativo
            val empregoAtivo = empregoRepository
                .observarAtivos()
                .firstOrNull()
                ?.firstOrNull()

            if (empregoAtivo == null) {
                Timber.w("WidgetUpdateWorker: nenhum emprego ativo")
                salvarPlaceholderSemEmprego()
                return Result.success()
            }

            val hoje = LocalDate.now()

            // 2. Resumo do dia via CalcularSaldoDiaUseCase
            val resumoDia = try {
                calcularSaldoDiaUseCase(empregoAtivo.id, hoje)
            } catch (e: Exception) {
                Timber.e(e, "WidgetUpdateWorker: erro ao calcular saldo do dia")
                null
            }

            val minutosTrabalhadosHoje = resumoDia?.trabalhadoMinutos ?: 0
            val saldoDiaMinutos = resumoDia?.saldoMinutos ?: 0

            // 3. Banco de horas via CalcularBancoHorasUseCase
            val resultadoBanco = try {
                calcularBancoHorasUseCase.calcular(empregoAtivo.id, hoje)
            } catch (e: Exception) {
                Timber.e(e, "WidgetUpdateWorker: erro ao calcular banco de horas")
                null
            }

            val saldoTotalMinutos = resultadoBanco
                ?.saldoTotal
                ?.toMinutes()
                ?.toInt()
                ?: 0

            // 4. Formatar textos
            val horasHojeText = minutosTrabalhadosHoje.minutosParaHoraMinuto()
            val saldoDiaText = saldoDiaMinutos.minutosParaSaldoFormatado()
            val saldoTotalText = saldoTotalMinutos.minutosParaSaldoFormatado()
            val saldoTotalNeg = saldoTotalMinutos < 0
            val ultimaAtualizacao = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

            // 5. Gravar no DataStore
            applicationContext.widgetDataStore.edit { prefs ->
                prefs[KEY_APELIDO_EMPREGO] = empregoAtivo.apelido ?: empregoAtivo.nome
                prefs[KEY_HORAS_HOJE] = horasHojeText
                prefs[KEY_SALDO_DIA] = saldoDiaText
                prefs[KEY_SALDO_TOTAL] = saldoTotalText
                prefs[KEY_SALDO_TOTAL_NEGATIVO] = saldoTotalNeg.toString()
                prefs[KEY_PREVISAO_SAIDA] = "--:--"
                prefs[KEY_ULTIMA_ATUALIZACAO] = ultimaAtualizacao
            }

            Timber.d("WidgetUpdateWorker: concluído — $horasHojeText / $saldoDiaText / $saldoTotalText")
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "WidgetUpdateWorker: erro inesperado")
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private suspend fun salvarPlaceholderSemEmprego() {
        applicationContext.widgetDataStore.edit { prefs ->
            prefs[KEY_APELIDO_EMPREGO] = "Sem emprego ativo"
            prefs[KEY_HORAS_HOJE] = "--h --min"
            prefs[KEY_SALDO_DIA] = "+--h --min"
            prefs[KEY_SALDO_TOTAL] = "+--h --min"
            prefs[KEY_SALDO_TOTAL_NEGATIVO] = "false"
            prefs[KEY_PREVISAO_SAIDA] = "--:--"
            prefs[KEY_ULTIMA_ATUALIZACAO] = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        }
    }

    // -------------------------------------------------------------------------
    // Companion
    // -------------------------------------------------------------------------

    companion object {
        const val WORK_NAME = "WidgetUpdateWorker"

        // Chaves do DataStore — devem bater com as que MeuPontoWidget lê
        val KEY_APELIDO_EMPREGO = stringPreferencesKey("apelido_emprego")
        val KEY_HORAS_HOJE = stringPreferencesKey("horas_hoje")
        val KEY_SALDO_DIA = stringPreferencesKey("saldo_dia")
        val KEY_SALDO_TOTAL = stringPreferencesKey("saldo_total")
        val KEY_SALDO_TOTAL_NEGATIVO = stringPreferencesKey("saldo_total_negativo")
        val KEY_PREVISAO_SAIDA = stringPreferencesKey("previsao_saida")
        val KEY_ULTIMA_ATUALIZACAO = stringPreferencesKey("ultima_atualizacao")

        fun scheduleNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}