// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/service/MigracaoManager.kt
package br.com.tlmacedo.meuponto.domain.service

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import br.com.tlmacedo.meuponto.domain.usecase.ponto.RecalcularToleranciasPontosUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.migracaoDataStore by preferencesDataStore(name = "migracoes")

/**
 * Gerenciador de migrações de dados do aplicativo.
 *
 * Controla a execução única de migrações necessárias após atualizações
 * que alterem a lógica de cálculo ou estrutura de dados.
 *
 * @author Thiago
 * @since 7.1.0
 */
@Singleton
class MigracaoManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recalcularToleranciasPontosUseCase: RecalcularToleranciasPontosUseCase
) {

    companion object {
        // Chaves das migrações - adicione novas migrações aqui
        private val MIGRACAO_V7_1_TOLERANCIA_INTERVALO = booleanPreferencesKey("migracao_v7_1_tolerancia_intervalo")
    }

    /**
     * Executa todas as migrações pendentes.
     * Deve ser chamado na inicialização do app.
     */
    suspend fun executarMigracoesPendentes() {
        Timber.i("Verificando migrações pendentes...")

        executarMigracaoToleranciaIntervalo()

        // Adicione novas migrações aqui no futuro
        // executarMigracaoV7_2_xxx()

        Timber.i("Verificação de migrações concluída")
    }

    /**
     * Migração v7.1.0 - Recalcula tolerâncias de volta do intervalo.
     *
     * Corrige o bug onde a horaConsiderada da volta do intervalo
     * não estava sendo calculada corretamente.
     */
    private suspend fun executarMigracaoToleranciaIntervalo() {
        val jaExecutada = context.migracaoDataStore.data
            .map { preferences -> preferences[MIGRACAO_V7_1_TOLERANCIA_INTERVALO] ?: false }
            .first()

        if (jaExecutada) {
            Timber.d("Migração v7.1 (tolerância intervalo) já executada")
            return
        }

        Timber.i("Executando migração v7.1 - Recálculo de tolerâncias de intervalo...")

        try {
            val resultado = recalcularToleranciasPontosUseCase()

            if (resultado.sucesso) {
                Timber.i(
                    "Migração v7.1 concluída: %d pontos processados, %d atualizados",
                    resultado.totalProcessados,
                    resultado.totalAtualizados
                )

                // Marca a migração como executada
                context.migracaoDataStore.edit { preferences ->
                    preferences[MIGRACAO_V7_1_TOLERANCIA_INTERVALO] = true
                }
            } else {
                Timber.e("Migração v7.1 concluída com erros: ${resultado.erros}")
                // Não marca como executada para tentar novamente
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao executar migração v7.1")
            // Não marca como executada para tentar novamente
        }
    }

    /**
     * Força a reexecução de uma migração específica.
     * Útil para debug ou correções manuais.
     */
    suspend fun resetarMigracao(chave: String) {
        val preferencesKey = when (chave) {
            "v7.1_tolerancia_intervalo" -> MIGRACAO_V7_1_TOLERANCIA_INTERVALO
            else -> {
                Timber.w("Chave de migração desconhecida: $chave")
                return
            }
        }

        context.migracaoDataStore.edit { preferences ->
            preferences[preferencesKey] = false
        }
        Timber.i("Migração $chave resetada")
    }
}
