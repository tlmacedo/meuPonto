// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularBancoHorasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.BancoHoras
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular o banco de horas acumulado.
 *
 * Calcula o saldo total de horas (positivo ou negativo) baseado em
 * todos os registros de ponto até uma data específica.
 *
 * @property repository Repositório de pontos para busca dos dados
 * @property calcularResumoDiaUseCase UseCase para calcular resumo diário
 *
 * @author Thiago
 * @since 1.0.0
 */
class CalcularBancoHorasUseCase @Inject constructor(
    private val repository: PontoRepository,
    private val calcularResumoDiaUseCase: CalcularResumoDiaUseCase
) {
    /**
     * Calcula o banco de horas até uma data específica.
     *
     * @param ateData Data limite para cálculo (padrão: ontem)
     * @param cargaHorariaDiariaMinutos Carga horária diária esperada em minutos (padrão: 480 = 8h)
     * @return Flow que emite o BancoHoras atualizado
     */
    operator fun invoke(
        ateData: LocalDate = LocalDate.now().minusDays(1),
        cargaHorariaDiariaMinutos: Int = 480
    ): Flow<BancoHoras> {
        return repository.observarTodos().map { pontos: List<Ponto> ->
            val pontosPorDia: Map<LocalDate, List<Ponto>> = pontos
                .filter { ponto -> ponto.data <= ateData }
                .groupBy { ponto -> ponto.data }

            var saldoTotal = Duration.ZERO

            pontosPorDia.forEach { (data: LocalDate, pontosNoDia: List<Ponto>) ->
                val resumo = calcularResumoDiaUseCase(
                    pontos = pontosNoDia,
                    data = data,
                    cargaHorariaDiariaMinutos = cargaHorariaDiariaMinutos
                )
                if (resumo.jornadaCompleta) {
                    saldoTotal = saldoTotal.plus(resumo.saldoDia)
                }
            }

            BancoHoras(saldoTotal = saldoTotal)
        }
    }
}
