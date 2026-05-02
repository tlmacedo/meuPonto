// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/pendencias/ListarPendenciasPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.pendencias

import br.com.tlmacedo.meuponto.domain.model.PendenciaDia
import br.com.tlmacedo.meuponto.domain.model.StatusPendencia
import java.time.LocalDate
import javax.inject.Inject

/**
 * Lista todas as pendências de ponto em um período, agrupadas por severidade.
 *
 * Itera dia a dia no intervalo fornecido, valida cada um via
 * [ValidarIntegridadeDiaUseCase] e retorna o resultado particionado em
 * Bloqueantes, Pendentes e Informativos.
 *
 * @author Thiago
 * @since 13.0.0
 */
class ListarPendenciasPontoUseCase @Inject constructor(
    private val validarIntegridadeDia: ValidarIntegridadeDiaUseCase
) {
    data class ResultadoPendencias(
        val bloqueantes: List<PendenciaDia>,
        val pendentes: List<PendenciaDia>,
        val informativos: List<PendenciaDia>
    ) {
        val total: Int get() = bloqueantes.size + pendentes.size + informativos.size
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
            bloqueantes = pendencias
                .filter { it.status == StatusPendencia.BLOQUEANTE }
                .sortedByDescending { it.data },
            pendentes = pendencias
                .filter { it.status == StatusPendencia.PENDENTE }
                .sortedByDescending { it.data },
            informativos = pendencias
                .filter { it.status == StatusPendencia.INFORMATIVO }
                .sortedByDescending { it.data }
        )
    }
}
