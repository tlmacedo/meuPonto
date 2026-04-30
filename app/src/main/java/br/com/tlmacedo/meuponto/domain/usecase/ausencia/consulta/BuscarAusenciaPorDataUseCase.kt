// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/consulta/BuscarAusenciaPorDataUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia.consulta

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

data class ResultadoAusenciaDia(
    val ausencia: Ausencia?,
    val tipoAusencia: TipoAusencia?
) {
    val temAusencia: Boolean
        get() = ausencia != null

    val isDiaNormal: Boolean
        get() = ausencia == null && tipoAusencia == null
}

class BuscarAusenciaPorDataUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository
) {
    suspend operator fun invoke(
        empregoId: Long,
        data: LocalDate
    ): ResultadoAusenciaDia {
        val ausencia = ausenciaRepository
            .buscarPorData(empregoId, data)
            .firstOrNull { it.ativo }

        return ResultadoAusenciaDia(
            ausencia = ausencia,
            tipoAusencia = ausencia?.tipo
        )
    }

    fun observar(
        empregoId: Long,
        data: LocalDate
    ): Flow<ResultadoAusenciaDia> {
        return ausenciaRepository
            .observarPorData(empregoId, data)
            .map { ausencias ->
                val ausencia = ausencias.firstOrNull { it.ativo }

                ResultadoAusenciaDia(
                    ausencia = ausencia,
                    tipoAusencia = ausencia?.tipo
                )
            }
    }
}