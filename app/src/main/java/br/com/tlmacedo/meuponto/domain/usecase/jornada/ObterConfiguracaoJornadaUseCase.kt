// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/jornada/ObterConfiguracaoJornadaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.jornada

import br.com.tlmacedo.meuponto.data.local.datastore.PreferenciasGlobaisDataStore
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoJornada
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obter a configuração de jornada.
 *
 * @property dataStore Fonte de verdade única de preferências globais
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 13.0.0 - Migrado para PreferenciasGlobaisDataStore
 */
class ObterConfiguracaoJornadaUseCase @Inject constructor(
    private val dataStore: PreferenciasGlobaisDataStore
) {
    operator fun invoke(): Flow<ConfiguracaoJornada> = dataStore.configuracaoJornada
}
