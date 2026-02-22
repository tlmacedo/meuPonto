// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ListarPontosPorDataUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.emprego.ObterEmpregoAtivoUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para listar pontos de uma data específica.
 *
 * Retorna um Flow reativo que emite a lista de pontos sempre
 * que houver alterações no banco de dados.
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 3.0.0 - Usa observarPorEmpregoEData para suporte a múltiplos empregos
 */
class ListarPontosPorDataUseCase @Inject constructor(
    private val repository: PontoRepository,
    private val obterEmpregoAtivoUseCase: ObterEmpregoAtivoUseCase
) {
    /**
     * Lista os pontos de uma data específica de forma reativa.
     *
     * @param data Data para filtrar os pontos
     * @return Flow com a lista de pontos da data ordenados por hora
     */
    suspend operator fun invoke(data: LocalDate): Flow<List<Ponto>> {
        val empregoId = when (val resultado = obterEmpregoAtivoUseCase()) {
            is ObterEmpregoAtivoUseCase.Resultado.Sucesso -> resultado.emprego.id
            else -> return emptyFlow()
        }
        return repository.observarPorEmpregoEData(empregoId, data)
    }

    /**
     * Lista os pontos de uma data específica para um emprego específico.
     *
     * @param empregoId ID do emprego
     * @param data Data para filtrar os pontos
     * @return Flow com a lista de pontos da data ordenados por hora
     */
    fun invoke(empregoId: Long, data: LocalDate): Flow<List<Ponto>> {
        return repository.observarPorEmpregoEData(empregoId, data)
    }
}
