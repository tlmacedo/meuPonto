// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/preferencias/ObterPreferenciasGlobaisUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.preferencias

import br.com.tlmacedo.meuponto.data.local.datastore.PreferenciasGlobaisDataStore
import br.com.tlmacedo.meuponto.domain.model.PreferenciasGlobais
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase para obter preferências globais do aplicativo.
 *
 * @author Thiago
 * @since 8.1.0
 */
class ObterPreferenciasGlobaisUseCase @Inject constructor(
    private val dataStore: PreferenciasGlobaisDataStore
) {
    operator fun invoke(): Flow<PreferenciasGlobais> {
        Timber.d("Observando preferências globais")
        return dataStore.preferenciasGlobais
    }
}
