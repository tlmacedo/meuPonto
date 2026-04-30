// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/consulta/MontarCalendarioAusenciasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia.consulta

import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.presentation.screen.history.InfoDiaHistorico
import java.time.LocalDate
import javax.inject.Inject

class MontarCalendarioAusenciasUseCase @Inject constructor() {

    operator fun invoke(
        ausencias: List<Ausencia>,
        ano: Int
    ): List<InfoDiaHistorico> {
        val ausenciasAtivas = ausencias.filter { it.ativo }
        val dias = mutableListOf<InfoDiaHistorico>()

        var dataAtual = LocalDate.of(ano, 1, 1)
        val dataFim = LocalDate.of(ano, 12, 31)

        while (!dataAtual.isAfter(dataFim)) {
            val ausenciasDoDia = ausenciasAtivas.filter { ausencia ->
                !dataAtual.isBefore(ausencia.dataInicio) &&
                        !dataAtual.isAfter(ausencia.dataFim)
            }

            dias.add(
                InfoDiaHistorico(
                    resumoDia = ResumoDia(
                        data = dataAtual,
                        tipoAusencia = obterTipoAusenciaPrioritaria(ausenciasDoDia)
                    ),
                    ausencias = ausenciasDoDia
                )
            )

            dataAtual = dataAtual.plusDays(1)
        }

        return dias
    }

    private fun obterTipoAusenciaPrioritaria(
        ausenciasDoDia: List<Ausencia>
    ): TipoAusencia? {
        if (ausenciasDoDia.isEmpty()) return null

        return ausenciasDoDia
            .map { it.tipo }
            .maxByOrNull { tipo -> tipo.prioridadeCalendario }
    }

    private val TipoAusencia.prioridadeCalendario: Int
        get() = when (this) {
            TipoAusencia.Falta.Injustificada -> 100
            TipoAusencia.Falta.Justificada -> 90
            TipoAusencia.Atestado -> 80
            TipoAusencia.Declaracao -> 70
            TipoAusencia.DayOff -> 60
            TipoAusencia.DiminuirBanco -> 50
            TipoAusencia.Ferias -> 40
            TipoAusencia.Feriado.Oficial -> 30
            TipoAusencia.Feriado.DiaPonte -> 20
            TipoAusencia.Feriado.Facultativo -> 10
            TipoAusencia.Folga -> 5
        }
}