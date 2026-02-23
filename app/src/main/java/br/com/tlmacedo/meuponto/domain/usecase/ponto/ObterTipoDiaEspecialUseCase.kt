// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ObterTipoDiaEspecialUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

/**
 * Resultado da verificação do tipo de dia especial.
 */
data class ResultadoTipoDia(
    val tipoDiaEspecial: TipoDiaEspecial,
    val feriado: Feriado? = null,
    val ausencia: Ausencia? = null,
    val descricao: String? = null
) {
    val isDiaEspecial: Boolean
        get() = tipoDiaEspecial != TipoDiaEspecial.NORMAL

    val emoji: String
        get() = tipoDiaEspecial.emoji

    val nomeExibicao: String
        get() = descricao ?: feriado?.nome ?: ausencia?.tipoDescricao ?: tipoDiaEspecial.descricao
}

/**
 * Caso de uso para obter o tipo de dia especial de uma data.
 *
 * PRIORIDADE:
 * 1. Ausência (férias, atestado, folga, falta) - sempre tem prioridade
 * 2. Feriado (nacional, estadual, municipal, ponte, facultativo)
 * 3. Dia normal
 *
 * @author Thiago
 * @since 4.0.0
 * @updated 6.0.0 - Corrigido suporte a TipoFolga (DAY_OFF vs COMPENSACAO)
 */
class ObterTipoDiaEspecialUseCase @Inject constructor(
    private val feriadoRepository: FeriadoRepository,
    private val ausenciaRepository: AusenciaRepository
) {

    /**
     * Obtém o tipo de dia especial para uma data.
     *
     * @param empregoId ID do emprego
     * @param data Data a verificar
     * @return Resultado com tipo do dia e detalhes
     */
    suspend operator fun invoke(empregoId: Long, data: LocalDate): ResultadoTipoDia {
        // 1. Verificar ausência primeiro (prioridade)
        val ausencias = ausenciaRepository.buscarPorData(empregoId, data)
        val ausencia = ausencias.firstOrNull()

        if (ausencia != null) {
            return ResultadoTipoDia(
                tipoDiaEspecial = ausencia.tipo.toTipoDiaEspecial(ausencia.tipoFolga),
                ausencia = ausencia,
                descricao = ausencia.descricao ?: ausencia.tipoDescricao
            )
        }

        // 2. Verificar feriado
        val feriados = feriadoRepository.buscarPorData(data)
            .filter { it.aplicavelPara(empregoId) }

        val feriado = feriados.firstOrNull()

        if (feriado != null) {
            val tipoDiaEspecial = when (feriado.tipo) {
                TipoFeriado.NACIONAL,
                TipoFeriado.ESTADUAL,
                TipoFeriado.MUNICIPAL -> TipoDiaEspecial.FERIADO
                TipoFeriado.PONTE -> TipoDiaEspecial.PONTE
                TipoFeriado.FACULTATIVO -> TipoDiaEspecial.FACULTATIVO
            }

            return ResultadoTipoDia(
                tipoDiaEspecial = tipoDiaEspecial,
                feriado = feriado,
                descricao = feriado.nome
            )
        }

        // 3. Dia normal
        return ResultadoTipoDia(
            tipoDiaEspecial = TipoDiaEspecial.NORMAL
        )
    }

    /**
     * Observa o tipo de dia especial (reativo).
     */
    fun observar(empregoId: Long, data: LocalDate): Flow<ResultadoTipoDia> {
        val ausenciasFlow = ausenciaRepository.observarPorData(empregoId, data)
        val feriadosFlow = feriadoRepository.observarPorEmprego(empregoId)

        return combine(ausenciasFlow, feriadosFlow) { ausencias, feriados ->
            // Verificar ausência
            val ausencia = ausencias.firstOrNull()
            if (ausencia != null) {
                return@combine ResultadoTipoDia(
                    tipoDiaEspecial = ausencia.tipo.toTipoDiaEspecial(ausencia.tipoFolga),
                    ausencia = ausencia,
                    descricao = ausencia.descricao ?: ausencia.tipoDescricao
                )
            }

            // Verificar feriado
            val feriado = feriados
                .filter { it.ocorreEm(data) && it.aplicavelPara(empregoId) }
                .firstOrNull()

            if (feriado != null) {
                val tipoDiaEspecial = when (feriado.tipo) {
                    TipoFeriado.NACIONAL,
                    TipoFeriado.ESTADUAL,
                    TipoFeriado.MUNICIPAL -> TipoDiaEspecial.FERIADO
                    TipoFeriado.PONTE -> TipoDiaEspecial.PONTE
                    TipoFeriado.FACULTATIVO -> TipoDiaEspecial.FACULTATIVO
                }

                return@combine ResultadoTipoDia(
                    tipoDiaEspecial = tipoDiaEspecial,
                    feriado = feriado,
                    descricao = feriado.nome
                )
            }

            // Dia normal
            ResultadoTipoDia(tipoDiaEspecial = TipoDiaEspecial.NORMAL)
        }
    }
}
