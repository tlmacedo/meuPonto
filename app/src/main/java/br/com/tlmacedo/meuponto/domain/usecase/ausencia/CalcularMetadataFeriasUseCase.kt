package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import java.time.LocalDate
import javax.inject.Inject

data class MetadataFerias(
    val sequenciaPeriodo: Int,
    val diasGanhos: Int = 30,
    val diasMarcados: Int,
    val diasAproveitados: Int,
    val diasRestantesParaMarcar: Int,
    val diasRestantesParaAproveitar: Int
)

/**
 * UseCase para calcular metadados de férias (sequência do período e dias restantes).
 *
 * @author Thiago
 * @since 13.0.0
 * @updated 13.1.0 - Adicionados campos de dias aproveitados e restantes para marcar/aproveitar
 */
class CalcularMetadataFeriasUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository
) {
    suspend operator fun invoke(
        ausencia: Ausencia,
        referencia: LocalDate? = null
    ): MetadataFerias? {
        if (ausencia.tipo != TipoAusencia.Ferias) return null
        val inicioAquisitivo = ausencia.dataInicioPeriodoAquisitivo ?: return null
        val fimAquisitivo = ausencia.dataFimPeriodoAquisitivo ?: return null
        val dataRef = referencia ?: LocalDate.now()

        val feriasNoPeriodo = ausenciaRepository.buscarFeriasPorPeriodoAquisitivo(
            ausencia.empregoId,
            inicioAquisitivo,
            fimAquisitivo
        ).filter { it.ativo }.toMutableList()

        // Se for uma nova ausência ou edição, garantir que ela esteja na lista para o cálculo
        if (ausencia.id != 0L) {
            feriasNoPeriodo.removeIf { it.id == ausencia.id }
        }
        feriasNoPeriodo.add(ausencia)
        feriasNoPeriodo.sortBy { it.dataInicio }

        val indice = feriasNoPeriodo.indexOfFirst { it.id == ausencia.id }
        val sequencia = if (indice != -1) indice + 1 else 0

        val diasGanhos = 30
        val diasMarcados = feriasNoPeriodo.sumOf { it.quantidadeDias }

        // Dias aproveitados: dias de férias marcadas que já passaram (ou estão passando) em relação à data de referência
        val diasAproveitados = feriasNoPeriodo.sumOf { f ->
            when {
                dataRef.isAfter(f.dataFim) -> f.quantidadeDias
                dataRef.isBefore(f.dataInicio) -> 0
                else -> java.time.temporal.ChronoUnit.DAYS.between(f.dataInicio, dataRef)
                    .toInt() + 1
            }
        }

        val diasRestantesParaMarcar = (diasGanhos - diasMarcados).coerceAtLeast(0)

        // Dias que faltam aproveitar: Dias ganhos (30) menos os que já foram aproveitados
        val diasRestantesParaAproveitar = (diasGanhos - diasAproveitados).coerceAtLeast(0)

        return MetadataFerias(
            sequenciaPeriodo = sequencia,
            diasGanhos = diasGanhos,
            diasMarcados = diasMarcados,
            diasAproveitados = diasAproveitados,
            diasRestantesParaMarcar = diasRestantesParaMarcar,
            diasRestantesParaAproveitar = diasRestantesParaAproveitar
        )
    }
}
