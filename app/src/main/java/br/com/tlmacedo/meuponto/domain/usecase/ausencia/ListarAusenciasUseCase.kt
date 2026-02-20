// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/ListarAusenciasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

/**
 * Caso de uso para listar ausências.
 *
 * @author Thiago
 * @since 4.0.0
 */
class ListarAusenciasUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository
) {

    /**
     * Lista todas as ausências de um emprego.
     */
    suspend fun porEmprego(empregoId: Long): List<Ausencia> {
        return ausenciaRepository.buscarAtivasPorEmprego(empregoId)
    }

    /**
     * Observa todas as ausências de um emprego (reativo).
     */
    fun observarPorEmprego(empregoId: Long): Flow<List<Ausencia>> {
        return ausenciaRepository.observarAtivasPorEmprego(empregoId)
    }

    /**
     * Lista ausências de um mês específico.
     */
    suspend fun porMes(empregoId: Long, mes: YearMonth): List<Ausencia> {
        return ausenciaRepository.buscarPorMes(empregoId, mes)
    }

    /**
     * Observa ausências de um mês específico (reativo).
     */
    fun observarPorMes(empregoId: Long, mes: YearMonth): Flow<List<Ausencia>> {
        return ausenciaRepository.observarPorMes(empregoId, mes)
    }

    /**
     * Lista ausências por tipo.
     */
    suspend fun porTipo(empregoId: Long, tipo: TipoAusencia): List<Ausencia> {
        return ausenciaRepository.buscarPorTipo(empregoId, tipo)
    }

    /**
     * Lista ausências de um ano.
     */
    suspend fun porAno(empregoId: Long, ano: Int): List<Ausencia> {
        return ausenciaRepository.buscarPorAno(empregoId, ano)
    }
}
