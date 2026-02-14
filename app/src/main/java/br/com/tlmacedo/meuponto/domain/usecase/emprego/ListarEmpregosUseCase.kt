package br.com.tlmacedo.meuponto.domain.usecase.emprego

import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.saldo.CalcularSaldoDiaUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Caso de uso para listar empregos com informações adicionais.
 */
class ListarEmpregosUseCase @Inject constructor(
    private val empregoRepository: EmpregoRepository,
    private val pontoRepository: PontoRepository,
    private val calcularSaldoDiaUseCase: CalcularSaldoDiaUseCase
) {
    data class EmpregoComResumo(
        val emprego: Emprego,
        val totalPontos: Int = 0,
        val saldoTotalMinutos: Long = 0
    )

    fun observarAtivos(): Flow<List<EmpregoComResumo>> {
        return empregoRepository.observarAtivos().map { empregos ->
            empregos.map { emprego ->
                val totalPontos = pontoRepository.contarPorEmprego(emprego.id)
                EmpregoComResumo(
                    emprego = emprego,
                    totalPontos = totalPontos
                )
            }
        }
    }

    fun observarTodos(): Flow<List<EmpregoComResumo>> {
        return empregoRepository.observarTodos().map { empregos ->
            empregos.map { emprego ->
                val totalPontos = pontoRepository.contarPorEmprego(emprego.id)
                EmpregoComResumo(
                    emprego = emprego,
                    totalPontos = totalPontos
                )
            }
        }
    }

    suspend fun buscarAtivos(): List<EmpregoComResumo> {
        return empregoRepository.buscarAtivos().map { emprego ->
            val totalPontos = pontoRepository.contarPorEmprego(emprego.id)
            EmpregoComResumo(
                emprego = emprego,
                totalPontos = totalPontos
            )
        }
    }
}
