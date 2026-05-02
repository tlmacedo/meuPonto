// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularResumoDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso responsável por transformar uma lista de registros de ponto e configurações
 * em um objeto [ResumoDia] consolidado.
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 12.0.0 - Suporte a múltiplos horários e acréscimos de dias pontes
 */
class CalcularResumoDiaUseCase @Inject constructor() {

    /**
     * Calcula o resumo do dia com base em configurações detalhadas de horário.
     *
     * @param pontos Lista de pontos registrados no dia
     * @param data Data de referência
     * @param horarioDiaSemana Configurações de horário para este dia da semana
     * @param toleranciaIntervaloGlobal Tolerância configurada para o intervalo
     * @param acrescimoDiasPontes Minutos extras a serem somados na jornada (compensação)
     * @param tipoAusencia Tipo de ausência registrado para o dia (opcional)
     * @param tempoAbonadoMinutos Minutos abonados por justificativa (opcional)
     * @return Objeto [ResumoDia] com os cálculos consolidados
     */
    operator fun invoke(
        pontos: List<Ponto>,
        data: LocalDate = LocalDate.now(),
        horarioDiaSemana: HorarioDiaSemana? = null,
        toleranciaIntervaloGlobal: Int? = null,
        acrescimoDiasPontes: Int = 0,
        tipoAusencia: TipoAusencia? = null,
        tempoAbonadoMinutos: Int = 0
    ): ResumoDia {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        val cargaHorariaBase = horarioDiaSemana?.cargaHorariaMinutos ?: 480
        val jornadaPrevista = cargaHorariaBase + acrescimoDiasPontes

        return ResumoDia(
            data = data,
            entrada = pontosOrdenados.getOrNull(0)?.hora,
            saidaAlmoco = pontosOrdenados.getOrNull(1)?.hora,
            voltaAlmoco = pontosOrdenados.getOrNull(2)?.hora,
            saida = pontosOrdenados.getOrNull(3)?.hora,
            jornadaPrevistaMinutos = jornadaPrevista,
            intervaloPrevistoMinutos = horarioDiaSemana?.intervaloMinimoMinutos ?: 60,
            toleranciaIntervaloMinutos = toleranciaIntervaloGlobal ?: 0,
            tipoAusencia = tipoAusencia
        )
    }

    /**
     * Versão simplificada do cálculo de resumo.
     *
     * @param pontos Lista de pontos registrados
     * @param data Data de referência
     * @param cargaHorariaDiariaMinutos Carga horária total prevista para o dia
     * @param tipoAusencia Tipo de ausência (opcional)
     * @param tempoAbonadoMinutos Minutos abonados (opcional)
     * @return Objeto [ResumoDia] consolidado
     */
    operator fun invoke(
        pontos: List<Ponto>,
        data: LocalDate,
        cargaHorariaDiariaMinutos: Int,
        tipoAusencia: TipoAusencia? = null,
        tempoAbonadoMinutos: Int = 0
    ): ResumoDia {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }

        return ResumoDia(
            data = data,
            entrada = pontosOrdenados.getOrNull(0)?.hora,
            saidaAlmoco = pontosOrdenados.getOrNull(1)?.hora,
            voltaAlmoco = pontosOrdenados.getOrNull(2)?.hora,
            saida = pontosOrdenados.getOrNull(3)?.hora,
            jornadaPrevistaMinutos = cargaHorariaDiariaMinutos,
            tipoAusencia = tipoAusencia
        )
    }
}
