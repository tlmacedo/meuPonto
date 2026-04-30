// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/CalcularResumoDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import java.time.LocalDate
import javax.inject.Inject

class CalcularResumoDiaUseCase @Inject constructor() {

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