package br.com.tlmacedo.meuponto.presentation.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.CalcularResumoDiaUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ponto.DeterminarProximoTipoPontoUseCase
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PreferenciasRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import androidx.hilt.work.HiltWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Worker responsável por coletar dados reais do banco e atualizar o estado do widget.
 * 
 * Este Worker sincroniza as informações de horas trabalhadas e o próximo tipo de registro
 * com o armazenamento persistente do Widget (Glance State).
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pontoRepository: PontoRepository,
    private val empregoRepository: EmpregoRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val calcularResumoDiaUseCase: CalcularResumoDiaUseCase,
    private val determinarProximoTipoPontoUseCase: DeterminarProximoTipoPontoUseCase,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("Iniciando atualização de dados do Widget")
        
        return try {
            // 1. Obter emprego ativo e dados básicos
            val empregoId = preferenciasRepository.obterEmpregoAtivoId() 
                ?: return Result.failure()
            
            val emprego = empregoRepository.buscarPorId(empregoId)
                ?: return Result.failure()

            val hoje = LocalDate.now()
            val pontosHoje = pontoRepository.buscarPorEmpregoEData(empregoId, hoje)
            
            // 2. Calcular resumo do dia atual
            val diaSemana = DiaSemana.fromJavaDayOfWeek(hoje.dayOfWeek)
            val horarioDia = horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)
            
            val resumo = calcularResumoDiaUseCase(
                pontos = pontosHoje,
                data = hoje,
                horarioDiaSemana = horarioDia
            )
            
            val proximoPonto = determinarProximoTipoPontoUseCase(pontosHoje)

            // 3. Atualizar o estado de todos os widgets MeuPonto ativos
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(MeuPontoWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[stringPreferencesKey("horas_hoje")] = resumo.horasTrabalhadasFormatadas
                        this[stringPreferencesKey("proximo_ponto")] = proximoPonto.descricao
                        this[stringPreferencesKey("apelido_emprego")] = emprego.apelido ?: emprego.nome
                    }
                }
                // Solicita a recomposição imediata do widget
                MeuPontoWidget().update(applicationContext, glanceId)
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Erro ao atualizar dados do widget")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "WidgetUpdateWorker"
    }
}
