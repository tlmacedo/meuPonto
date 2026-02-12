// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/jornada/SalvarConfiguracaoJornadaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.jornada

import br.com.tlmacedo.meuponto.data.local.datastore.PreferencesDataStore
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoJornada
import timber.log.Timber
import javax.inject.Inject

/**
 * Caso de uso para salvar a configuração de jornada.
 *
 * Persiste as configurações de jornada de trabalho do usuário
 * no DataStore de preferências.
 *
 * @property dataStore DataStore de preferências
 *
 * @author Thiago
 * @since 1.0.0
 */
class SalvarConfiguracaoJornadaUseCase @Inject constructor(
    private val dataStore: PreferencesDataStore
) {
    /**
     * Salva a configuração de jornada.
     *
     * @param configuracao Configuração a ser salva
     * @return Result indicando sucesso ou falha
     */
    suspend operator fun invoke(configuracao: ConfiguracaoJornada): Result<Unit> {
        return try {
            dataStore.salvarConfiguracaoJornada(configuracao)
            Timber.d("Configuração de jornada salva: $configuracao")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao salvar configuração de jornada")
            Result.failure(e)
        }
    }
}
