package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import javax.inject.Inject

data class MetadataFerias(
    val sequenciaPeriodo: Int,
    val diasRestantes: Int
)

/**
 * UseCase para calcular metadados de férias (sequência do período e dias restantes).
 *
 * @author Thiago
 * @since 13.0.0
 */
class CalcularMetadataFeriasUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository
) {
    suspend operator fun invoke(ausencia: Ausencia): MetadataFerias? {
        if (ausencia.tipo != TipoAusencia.FERIAS) return null
        val inicioAquisitivo = ausencia.dataInicioPeriodoAquisitivo ?: return null
        val fimAquisitivo = ausencia.dataFimPeriodoAquisitivo ?: return null

        val feriasNoPeriodo = ausenciaRepository.buscarFeriasPorPeriodoAquisitivo(
            ausencia.empregoId,
            inicioAquisitivo,
            fimAquisitivo
        ).filter { it.ativo }.sortedBy { it.dataInicio }

        val indice = feriasNoPeriodo.indexOfFirst { it.id == ausencia.id }
        val sequencia = if (indice != -1) indice + 1 else 0

        val diasGanhos = 30 // Assumindo 30 dias padrão, isso poderia vir de configuração no futuro
        val diasUsados = feriasNoPeriodo.sumOf { it.quantidadeDias }
        val diasRestantes = (diasGanhos - diasUsados).coerceAtLeast(0)

        return MetadataFerias(
            sequenciaPeriodo = sequencia,
            diasRestantes = diasRestantes
        )
    }
}
