// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/pendencias/ValidarIntegridadeDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.pendencias

import br.com.tlmacedo.meuponto.domain.model.Inconsistencia
import br.com.tlmacedo.meuponto.domain.model.InconsistenciaDetectada
import br.com.tlmacedo.meuponto.domain.model.PendenciaDia
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import br.com.tlmacedo.meuponto.domain.usecase.validacao.ValidarPontoCompletoUseCase
import java.time.LocalDate
import javax.inject.Inject

/**
 * Valida a integridade de um dia de trabalho e retorna suas pendências.
 *
 * Combina dados do dia (pontos, ausências, feriado, jornada) para detectar
 * todas as inconsistências que requerem ação ou justificativa do usuário.
 *
 * Retorna null quando o dia não tem pendências ou não precisa ser verificado
 * (dia futuro, descanso sem pontos).
 *
 * @author Thiago
 * @since 13.0.0
 */
class ValidarIntegridadeDiaUseCase @Inject constructor(
    private val obterResumoDia: ObterResumoDiaCompletoUseCase,
    private val validarPontoCompleto: ValidarPontoCompletoUseCase,
    private val versaoJornadaRepository: VersaoJornadaRepository
) {
    suspend operator fun invoke(empregoId: Long, data: LocalDate): PendenciaDia? {
        val resumo = obterResumoDia(empregoId, data)

        if (resumo.isFuturo) return null
        if (resumo.isDescanso && !resumo.temPontos) return null

        val inconsistencias = mutableListOf<InconsistenciaDetectada>()
        val pontos = resumo.pontos

        val ehDiaUtilSemAusencia = !resumo.isDescanso &&
                !resumo.isFerias &&
                !resumo.isAtestado &&
                !resumo.isFolga &&
                !resumo.isFaltaJustificada &&
                !resumo.isFaltaInjustificada &&
                resumo.ausencias.isEmpty()

        if (ehDiaUtilSemAusencia && pontos.isEmpty()) {
            inconsistencias.add(
                InconsistenciaDetectada(inconsistencia = Inconsistencia.FALTA_SEM_JUSTIFICATIVA)
            )
        }

        if (pontos.isNotEmpty() && pontos.size % 2 != 0) {
            inconsistencias.add(
                InconsistenciaDetectada(
                    inconsistencia = Inconsistencia.REGISTROS_IMPARES,
                    detalhes = "${pontos.size} registro(s) no dia"
                )
            )
        }

        val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)
        if (versaoJornada != null) {
            val resultado = validarPontoCompleto.validarDiaCompleto(
                empregoId = empregoId,
                data = data,
                versaoJornada = versaoJornada,
                horarioEsperado = resumo.horarioDiaSemana
            )
            inconsistencias.addAll(resultado.inconsistencias)
        }

        pontos.filter { it.isEditadoManualmente }.forEach { ponto ->
            val jaRegistrado = inconsistencias.any {
                it.inconsistencia == Inconsistencia.REGISTRO_EDITADO &&
                        it.pontoRelacionadoId == ponto.id
            }
            if (!jaRegistrado) {
                inconsistencias.add(
                    InconsistenciaDetectada(
                        inconsistencia = Inconsistencia.REGISTRO_EDITADO,
                        detalhes = "Ponto das ${ponto.horaFormatada} editado manualmente",
                        pontoRelacionadoId = ponto.id
                    )
                )
            }
        }

        if (inconsistencias.isEmpty()) return null

        val temJustificativa = pontos.any { !it.justificativaInconsistencia.isNullOrBlank() }

        return PendenciaDia(
            data = data,
            inconsistencias = inconsistencias.distinctBy { it.inconsistencia },
            quantidadePontos = pontos.size,
            temJustificativa = temJustificativa
        )
    }
}
