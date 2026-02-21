// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularResumoDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
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
 * TOLERÂNCIA DE INTERVALO:
 * - A tolerância é aplicada APENAS UMA VEZ por dia
 * - É aplicada na pausa cujo horário de saída seja mais próximo do saidaIntervaloIdeal
 * - Isso evita que múltiplas pausas no mesmo dia recebam tolerância indevidamente
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.11.0 - Simplificado: ResumoDia calcula horasTrabalhadas internamente
 * @updated 4.0.0 - Adicionado suporte a dias especiais (feriado, férias, folga, falta, atestado)
 * @updated 4.2.0 - Adicionado saidaIntervaloIdeal para tolerância única de intervalo
 */
class CalcularResumoDiaUseCase @Inject constructor() {

    /**
     * Calcula o resumo do dia com configurações de tolerância e tipo de dia especial.
     *
     * @param pontos Lista de pontos do dia
     * @param data Data do resumo
     * @param horarioDiaSemana Configuração do dia (opcional, para tolerâncias e horário ideal)
     * @param tipoDiaEspecial Tipo de dia especial (feriado, férias, etc.)
     */
    operator fun invoke(
        pontos: List<Ponto>,
        data: LocalDate = LocalDate.now(),
        horarioDiaSemana: HorarioDiaSemana? = null,
        tipoDiaEspecial: TipoDiaEspecial = TipoDiaEspecial.NORMAL
    ): ResumoDia {
        // Valores padrão se não houver configuração
        val cargaHoraria = horarioDiaSemana?.cargaHorariaMinutos ?: 480
        val intervaloMinimo = horarioDiaSemana?.intervaloMinimoMinutos ?: 60
        val toleranciaIntervalo = horarioDiaSemana?.toleranciaIntervaloMaisMinutos ?: 15
        val saidaIntervaloIdeal = horarioDiaSemana?.saidaIntervaloIdeal

        return ResumoDia(
            data = data,
            pontos = pontos.sortedBy { it.dataHora },
            cargaHorariaDiaria = Duration.ofMinutes(cargaHoraria.toLong()),
            intervaloMinimoMinutos = intervaloMinimo,
            toleranciaIntervaloMinutos = toleranciaIntervalo,
            tipoDiaEspecial = tipoDiaEspecial,
            saidaIntervaloIdeal = saidaIntervaloIdeal
        )
    }

    /**
     * Sobrecarga para compatibilidade com código existente.
     * Nota: Esta sobrecarga NÃO passa saidaIntervaloIdeal, então aplica tolerância na primeira pausa elegível.
     */
    operator fun invoke(
        pontos: List<Ponto>,
        data: LocalDate,
        cargaHorariaDiariaMinutos: Int,
        tipoDiaEspecial: TipoDiaEspecial = TipoDiaEspecial.NORMAL
    ): ResumoDia {
        return ResumoDia(
            data = data,
            pontos = pontos.sortedBy { it.dataHora },
            cargaHorariaDiaria = Duration.ofMinutes(cargaHorariaDiariaMinutos.toLong()),
            tipoDiaEspecial = tipoDiaEspecial
        )
    }
}
