// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/pendencias/ListarPendenciasPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.pendencias

import br.com.tlmacedo.meuponto.domain.model.PendenciaDia
import br.com.tlmacedo.meuponto.domain.model.StatusDiaPonto
import java.time.LocalDate
import javax.inject.Inject

/**
 * Lista todas as pendências de ponto em um período, agrupadas pela nova classificação de produto.
 *
 * @author Thiago
 * @since 13.0.0
 */
class ListarPendenciasPontoUseCase @Inject constructor(
    private val validarIntegridadeDia: ValidarIntegridadeDiaUseCase
) {
    data class ResultadoPendencias(
        val bloqueados: List<PendenciaDia>,
        val pendentes: List<PendenciaDia>,
        val informativos: List<PendenciaDia>,
        val emAndamento: List<PendenciaDia>,
        val normais: List<PendenciaDia>
    ) {
        val total: Int get() = bloqueados.size + pendentes.size + informativos.size + emAndamento.size
        val temPendencias: Boolean get() = total > 0
    }

    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): ResultadoPendencias {
        val pendencias = mutableListOf<PendenciaDia>()
        var data = dataInicio
        while (!data.isAfter(dataFim)) {
            validarIntegridadeDia(empregoId, data)?.let { pendencias.add(it) }
            data = data.plusDays(1)
        }

        return ResultadoPendencias(
            bloqueados = pendencias
                .filter { it.status == StatusDiaPonto.BLOQUEADO }
                .sortedByDescending { it.data },
            pendentes = pendencias
                .filter { it.status == StatusDiaPonto.PENDENTE_JUSTIFICATIVA }
                .sortedByDescending { it.data },
            informativos = pendencias
                .filter { it.status == StatusDiaPonto.INFO }
                .sortedByDescending { it.data },
            emAndamento = pendencias
                .filter { it.status == StatusDiaPonto.EM_ANDAMENTO }
                .sortedByDescending { it.data },
            normais = pendencias
                .filter { it.status == StatusDiaPonto.NORMAL }
                .sortedByDescending { it.data }
        )
    }
}
