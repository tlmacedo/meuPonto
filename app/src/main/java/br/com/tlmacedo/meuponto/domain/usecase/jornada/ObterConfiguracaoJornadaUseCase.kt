// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/jornada/ObterConfiguracaoJornadaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.jornada

import br.com.tlmacedo.meuponto.data.local.datastore.PreferencesDataStore
import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoJornada
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obter a configuração de jornada.
 *
 * Retorna um Flow reativo com a configuração atual de jornada
 * de trabalho do usuário, emitindo atualizações quando houver mudanças.
 *
 * @property dataStore DataStore de preferências
 *
 * @author Thiago
 * @since 1.0.0
 */
class ObterConfiguracaoJornadaUseCase @Inject constructor(
    private val dataStore: PreferencesDataStore
) {
    /**
     * Observa a configuração de jornada de forma reativa.
     *
     * @return Flow que emite a configuração sempre que houver mudanças
     */
    operator fun invoke(): Flow<ConfiguracaoJornada> {
        return dataStore.configuracaoJornada
    }
}
