// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ListarPontosPorDataUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para listar pontos de uma data específica.
 *
 * Retorna um Flow reativo que emite a lista de pontos sempre
 * que houver alterações no banco de dados.
 *
 * @property repository Repositório de pontos
 *
 * @author Thiago
 * @since 1.0.0
 */
class ListarPontosPorDataUseCase @Inject constructor(
    private val repository: PontoRepository
) {
    /**
     * Lista os pontos de uma data específica de forma reativa.
     *
     * @param data Data para filtrar os pontos
     * @return Flow com a lista de pontos da data ordenados por hora
     */
    operator fun invoke(data: LocalDate): Flow<List<Ponto>> {
        return repository.observarPontosPorData(data)
    }
}
