// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/FiltrarAusenciasUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia.consulta

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import javax.inject.Inject

class FiltrarAusenciasUseCase @Inject constructor() {

    operator fun invoke(
        ausencias: List<Ausencia>,
        tiposSelecionados: Set<TipoAusencia>,
        anoSelecionado: Int?
    ): List<Ausencia> {
        return ausencias.filter { ausencia ->
            val passaTipo = tiposSelecionados.isEmpty() || ausencia.tipo in tiposSelecionados
            val passaAno = anoSelecionado == null ||
                    ausencia.dataInicio.year == anoSelecionado ||
                    ausencia.dataFim.year == anoSelecionado

            passaTipo && passaAno
        }
    }
}