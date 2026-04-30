package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

data class ResultadoAusenciaDia(
    val ausencia: Ausencia?,
    val tipoDiaEspecial: TipoDiaEspecial
) {
    val temAusencia: Boolean
        get() = ausencia != null
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
            .firstOrNull()

        return ResultadoAusenciaDia(
            ausencia = ausencia,
            tipoDiaEspecial = ausencia?.tipo?.tipoDiaEspecial
                ?: TipoDiaEspecial.Normal
        )
    }

    fun observar(
        empregoId: Long,
        data: LocalDate
    ): Flow<ResultadoAusenciaDia> {
        return ausenciaRepository.observarPorData(empregoId, data).map { ausencias ->
            val ausencia = ausencias.firstOrNull()

            ResultadoAusenciaDia(
                ausencia = ausencia,
                tipoDiaEspecial = ausencia?.tipo?.tipoDiaEspecial
                    ?: TipoDiaEspecial.Normal
            )
        }
    }
}