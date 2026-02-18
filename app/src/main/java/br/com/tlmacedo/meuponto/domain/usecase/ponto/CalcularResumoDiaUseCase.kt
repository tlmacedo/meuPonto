// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularResumoDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular o resumo de um dia de trabalho.
 *
 * ARQUITETURA SIMPLIFICADA:
 * - Este UseCase apenas monta o ResumoDia com os parâmetros corretos
 * - O ResumoDia é responsável por calcular horasTrabalhadas a partir dos intervalos
 * - Isso garante consistência (single source of truth)
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Simplificado (tipo calculado por posição)
 * @updated 2.6.0 - Usa horaEfetiva para considerar tolerâncias
 * @updated 2.8.0 - Recebe HorarioDiaSemana para configuração de tolerância de intervalo
 * @updated 2.11.0 - Simplificado: ResumoDia calcula horasTrabalhadas internamente
 */
class CalcularResumoDiaUseCase @Inject constructor() {

    /**
     * Calcula o resumo do dia com configurações de tolerância.
     *
     * @param pontos Lista de pontos do dia
     * @param data Data do resumo
     * @param horarioDiaSemana Configuração do dia (opcional, para tolerâncias)
     */
    operator fun invoke(
        pontos: List<Ponto>,
        data: LocalDate = LocalDate.now(),
        horarioDiaSemana: HorarioDiaSemana? = null
    ): ResumoDia {
        // Valores padrão se não houver configuração
        val cargaHoraria = horarioDiaSemana?.cargaHorariaMinutos ?: 480
        val intervaloMinimo = horarioDiaSemana?.intervaloMinimoMinutos ?: 60
        val toleranciaIntervalo = horarioDiaSemana?.toleranciaIntervaloMaisMinutos ?: 15

        return ResumoDia(
            data = data,
            pontos = pontos.sortedBy { it.dataHora },
            cargaHorariaDiaria = Duration.ofMinutes(cargaHoraria.toLong()),
            intervaloMinimoMinutos = intervaloMinimo,
            toleranciaIntervaloMinutos = toleranciaIntervalo
        )
    }

    /**
     * Sobrecarga para compatibilidade com código existente.
     */
    operator fun invoke(
        pontos: List<Ponto>,
        data: LocalDate,
        cargaHorariaDiariaMinutos: Int
    ): ResumoDia {
        return ResumoDia(
            data = data,
            pontos = pontos.sortedBy { it.dataHora },
            cargaHorariaDiaria = Duration.ofMinutes(cargaHorariaDiariaMinutos.toLong())
        )
    }
}
