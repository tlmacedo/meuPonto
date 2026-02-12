package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para obter os pontos de um dia específico.
 *
 * Retorna um Flow que emite a lista atualizada de pontos
 * sempre que houver alterações no banco de dados.
 *
 * @property repository Repositório de pontos
 *
 * @author Thiago
 * @since 1.0.0
 */
class ObterPontosDoDiaUseCase @Inject constructor(
    private val repository: PontoRepository
) {
    /**
     * Observa os pontos de uma data específica.
     *
     * @param data Data para buscar (padrão: hoje)
     * @return Flow com a lista de pontos ordenados por hora
     */
    operator fun invoke(data: LocalDate = LocalDate.now()): Flow<List<Ponto>> {
        return repository.observarPontosPorData(data)
    }
}
