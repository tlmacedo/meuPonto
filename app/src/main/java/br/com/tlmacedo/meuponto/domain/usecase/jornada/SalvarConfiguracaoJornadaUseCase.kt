// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/jornada/SalvarConfiguracaoJornadaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.jornada

import br.com.tlmacedo.meuponto.data.local.datastore.PreferenciasGlobaisDataStore
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoJornada
import timber.log.Timber
import javax.inject.Inject

/**
 * Caso de uso para salvar a configuração de jornada.
 *
 * @property dataStore Fonte de verdade única de preferências globais
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 13.0.0 - Migrado para PreferenciasGlobaisDataStore
 */
class SalvarConfiguracaoJornadaUseCase @Inject constructor(
    private val dataStore: PreferenciasGlobaisDataStore
) {
    suspend operator fun invoke(configuracao: ConfiguracaoJornada): Result<Unit> {
        return try {
            dataStore.salvarConfiguracaoJornada(configuracao)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao salvar configuração de jornada")
            Result.failure(e)
        }
    }
}
