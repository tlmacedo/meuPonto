// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ajuste/ListarAjustesUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ajuste

import br.com.tlmacedo.meuponto.domain.model.AjusteSaldo
import br.com.tlmacedo.meuponto.domain.model.SaldoHoras
import br.com.tlmacedo.meuponto.domain.repository.AjusteSaldoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para listar e consultar ajustes de saldo.
 *
 * Permite buscar ajustes por período, tipo e emprego,
 * com sumarização e agrupamento.
 *
 * @property ajusteSaldoRepository Repositório de ajustes
 *
 * @author Thiago
 * @since 2.0.0
 */
class ListarAjustesUseCase @Inject constructor(
    private val ajusteSaldoRepository: AjusteSaldoRepository
) {
    /**
     * Tipo de ajuste para filtro.
     */
    enum class TipoAjuste {
        TODOS,
        CREDITOS,  // Positivos
        DEBITOS    // Negativos
    }

    /**
     * Resumo dos ajustes de um período.
     *
     * @property totalAjustes Quantidade total de ajustes
     * @property saldoTotal Soma de todos os ajustes
     * @property totalCreditos Soma dos créditos (positivos)
     * @property totalDebitos Soma dos débitos (negativos)
     * @property ajustes Lista de ajustes
     */
    data class ResumoAjustes(
        val totalAjustes: Int,
        val saldoTotal: SaldoHoras,
        val totalCreditos: SaldoHoras,
        val totalDebitos: SaldoHoras,
        val ajustes: List<AjusteSaldo>
    )

    /**
     * Lista ajustes de um emprego em tempo real (Flow).
     *
     * @param empregoId ID do emprego
     * @return Flow de lista de ajustes ordenados por data decrescente
     */
    fun observar(empregoId: Long): Flow<List<AjusteSaldo>> {
        return ajusteSaldoRepository.observarPorEmprego(empregoId)
    }

    /**
     * Busca ajustes de um período específico.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data inicial (inclusive)
     * @param dataFim Data final (inclusive)
     * @param tipo Filtro por tipo de ajuste
     * @return Lista de ajustes do período
     */
    suspend operator fun invoke(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate,
        tipo: TipoAjuste = TipoAjuste.TODOS
    ): List<AjusteSaldo> {
        val ajustes = ajusteSaldoRepository.buscarPorPeriodo(empregoId, dataInicio, dataFim)

        return when (tipo) {
            TipoAjuste.TODOS -> ajustes
            TipoAjuste.CREDITOS -> ajustes.filter { it.minutos > 0 }
            TipoAjuste.DEBITOS -> ajustes.filter { it.minutos < 0 }
        }
    }

    /**
     * Busca ajustes de um dia específico.
     *
     * @param empregoId ID do emprego
     * @param data Data para buscar
     * @return Lista de ajustes do dia
     */
    suspend fun porDia(empregoId: Long, data: LocalDate): List<AjusteSaldo> {
        return invoke(empregoId, data, data)
    }

    /**
     * Gera resumo dos ajustes de um período.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return [ResumoAjustes] com totalizações
     */
    suspend fun resumo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): ResumoAjustes {
        val ajustes = invoke(empregoId, dataInicio, dataFim)

        val totalCreditos = ajustes.filter { it.minutos > 0 }.sumOf { it.minutos }
        val totalDebitos = ajustes.filter { it.minutos < 0 }.sumOf { it.minutos }
        val saldoTotal = totalCreditos + totalDebitos

        return ResumoAjustes(
            totalAjustes = ajustes.size,
            saldoTotal = SaldoHoras(saldoTotal),
            totalCreditos = SaldoHoras(totalCreditos),
            totalDebitos = SaldoHoras(totalDebitos),
            ajustes = ajustes
        )
    }

    /**
     * Agrupa ajustes por mês.
     *
     * @param empregoId ID do emprego
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Mapa de Ano-Mês -> Lista de ajustes
     */
    suspend fun agrupadoPorMes(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Map<String, List<AjusteSaldo>> {
        val ajustes = invoke(empregoId, dataInicio, dataFim)

        return ajustes.groupBy { ajuste ->
            "${ajuste.data.year}-${ajuste.data.monthValue.toString().padStart(2, '0')}"
        }
    }

    /**
     * Observa resumo em tempo real.
     *
     * @param empregoId ID do emprego
     * @return Flow de [ResumoAjustes]
     */
    fun observarResumo(empregoId: Long): Flow<ResumoAjustes> {
        return ajusteSaldoRepository.observarPorEmprego(empregoId).map { ajustes ->
            val totalCreditos = ajustes.filter { it.minutos > 0 }.sumOf { it.minutos }
            val totalDebitos = ajustes.filter { it.minutos < 0 }.sumOf { it.minutos }
            val saldoTotal = totalCreditos + totalDebitos

            ResumoAjustes(
                totalAjustes = ajustes.size,
                saldoTotal = SaldoHoras(saldoTotal),
                totalCreditos = SaldoHoras(totalCreditos),
                totalDebitos = SaldoHoras(totalDebitos),
                ajustes = ajustes
            )
        }
    }
}
