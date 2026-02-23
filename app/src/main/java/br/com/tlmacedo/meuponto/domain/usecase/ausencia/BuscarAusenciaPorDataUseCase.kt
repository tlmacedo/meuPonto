// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ausencia/BuscarAusenciaPorDataUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Resultado da busca de ausência por data.
 */
data class ResultadoAusenciaDia(
    val ausencia: Ausencia?,
    val tipoDiaEspecial: TipoDiaEspecial
) {
    val temAusencia: Boolean get() = ausencia != null
}

/**
 * Caso de uso para buscar ausência em uma data específica.
 *
 * Retorna a ausência (se houver) e o TipoDiaEspecial correspondente
 * para ser usado no cálculo do ResumoDia.
 *
 * @author Thiago
 * @since 4.0.0
 */
class BuscarAusenciaPorDataUseCase @Inject constructor(
    private val ausenciaRepository: AusenciaRepository
) {

    /**
     * Busca ausência em uma data específica para um emprego.
     *
     * @param empregoId ID do emprego
     * @param data Data a verificar
     * @return Resultado com ausência e tipo do dia
     */
    suspend operator fun invoke(empregoId: Long, data: LocalDate): ResultadoAusenciaDia {
        val ausencias = ausenciaRepository.buscarPorData(empregoId, data)

        // Pega a primeira ausência ativa (não deveria haver mais de uma por validação)
        val ausencia = ausencias.firstOrNull()

        val tipoDiaEspecial = ausencia?.tipo?.toTipoDiaEspecial(ausencia.tipoFolga)
            ?: TipoDiaEspecial.NORMAL

        return ResultadoAusenciaDia(
            ausencia = ausencia,
            tipoDiaEspecial = tipoDiaEspecial
        )
    }

    /**
     * Observa ausência em uma data específica (reativo).
     */
    fun observar(empregoId: Long, data: LocalDate): Flow<ResultadoAusenciaDia> {
        return ausenciaRepository.observarPorData(empregoId, data).map { ausencias ->
            val ausencia = ausencias.firstOrNull()
            val tipoDiaEspecial = ausencia?.tipo?.toTipoDiaEspecial(ausencia.tipoFolga)
                ?: TipoDiaEspecial.NORMAL

            ResultadoAusenciaDia(
                ausencia = ausencia,
                tipoDiaEspecial = tipoDiaEspecial
            )
        }
    }
}
