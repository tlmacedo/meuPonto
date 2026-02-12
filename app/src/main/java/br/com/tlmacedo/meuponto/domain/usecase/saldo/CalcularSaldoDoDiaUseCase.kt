package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.RegistroDiario
import br.com.tlmacedo.meuponto.domain.model.SaldoHoras
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular o saldo de horas de um dia.
 *
 * Busca os pontos do dia e calcula a diferença entre
 * horas trabalhadas e horas esperadas.
 *
 * @property repository Repositório de pontos
 *
 * @author Thiago
 * @since 1.0.0
 */
class CalcularSaldoDoDiaUseCase @Inject constructor(
    private val repository: PontoRepository
) {
    /**
     * Calcula o saldo de horas do dia.
     *
     * @param data Data para calcular
     * @param cargaHorariaMinutos Carga horária esperada em minutos (padrão: 480 = 8h)
     * @return SaldoHoras calculado ou null se não houver pontos suficientes
     */
    suspend operator fun invoke(
        data: LocalDate = LocalDate.now(),
        cargaHorariaMinutos: Int = 480
    ): SaldoHoras? {
        val pontos = repository.buscarPontosPorData(data)
        
        if (pontos.isEmpty()) {
            return null
        }

        val registroDiario = RegistroDiario(
            data = data,
            pontos = pontos,
            cargaHorariaEsperada = cargaHorariaMinutos
        )

        val saldoMinutos = registroDiario.calcularSaldoMinutos() ?: return null
        
        return SaldoHoras(saldoMinutos)
    }
}
