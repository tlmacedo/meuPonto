// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/pendencias/ValidarIntegridadeDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.pendencias

import br.com.tlmacedo.meuponto.core.util.LocationUtils
import br.com.tlmacedo.meuponto.domain.model.Inconsistencia
import br.com.tlmacedo.meuponto.domain.model.InconsistenciaDetectada
import br.com.tlmacedo.meuponto.domain.model.PendenciaDia
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ObterResumoDiaCompletoUseCase
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Valida a integridade de um dia de trabalho seguindo as regras oficiais de produto.
 *
 * @author Thiago
 * @since 14.0.0
 */
class ValidarIntegridadeDiaUseCase @Inject constructor(
    private val obterResumoDia: ObterResumoDiaCompletoUseCase,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val pontoRepository: PontoRepository
) {
    suspend operator fun invoke(empregoId: Long, data: LocalDate): PendenciaDia? {
        val resumo = obterResumoDia(empregoId, data)
        if (resumo.isFuturo) return null

        /**
         * REGRA DE OURO:
         * Dias sem jornada planejada (Folga, Férias, Feriado, Pontes)
         * não geram inconsistências, mas pontos registrados continuam válidos.
         */
        val temJornadaPlanejada = (resumo.horarioDiaSemana?.ativo == true && resumo.cargaHorariaEfetivaMinutos > 0)
        if (!temJornadaPlanejada && !resumo.isDiaEspecial) {
            // Se não tem jornada e não é dia especial (feriado/ausencia), pode ser fim de semana sem contrato.
            if (resumo.pontos.isEmpty()) return null
        }

        val inconsistencias = mutableListOf<InconsistenciaDetectada>()
        val pontos = resumo.pontos
        val isHoje = resumo.isHoje

        // 1. Bloqueantes (Somente dias passados)
        if (!isHoje && pontos.isNotEmpty()) {
            // Entrada sem saída ou quantidade ímpar em dia anterior
            if (pontos.size % 2 != 0) {
                inconsistencias.add(InconsistenciaDetectada(Inconsistencia.REGISTROS_IMPARES_PASSADO))
            }
        }

        // 2. Pendentes de Justificativa (Apenas se houver jornada planejada)
        if (temJornadaPlanejada) {
            // Intervalo menor que o mínimo
            resumo.intervalos.filter { it.temPausaAntes }.forEach { intervalo ->
                val pausaReal = intervalo.pausaAntesMinutosReal ?: 0
                if (pausaReal < resumo.resumoDia.intervaloPrevistoMinutos) {
                    inconsistencias.add(
                        InconsistenciaDetectada(
                            Inconsistencia.INTERVALO_MINIMO_INSUFICIENTE,
                            detalhes = "Realizado: ${pausaReal}min | Mínimo: ${resumo.resumoDia.intervaloPrevistoMinutos}min"
                        )
                    )
                }
            }

            // Turno maior que 6h
            resumo.intervalos.forEach { intervalo ->
                if (intervalo.duracaoTurnoMinutos > 360) {
                    inconsistencias.add(
                        InconsistenciaDetectada(
                            Inconsistencia.TURNO_EXCEDIDO_6H,
                            detalhes = "Turno de ${intervalo.duracaoTurnoMinutos / 60}h ${intervalo.duracaoTurnoMinutos % 60}min"
                        )
                    )
                }
            }

            // Jornada acima de 10h
            if (resumo.horasTrabalhadasMinutos > 600) {
                inconsistencias.add(
                    InconsistenciaDetectada(
                        Inconsistencia.JORNADA_EXCEDIDA_10H,
                        detalhes = "Total: ${resumo.horasTrabalhadasMinutos / 60}h ${resumo.horasTrabalhadasMinutos % 60}min"
                    )
                )
            }

            // Descanso entre jornadas menor que 11h
            val primeiroPontoDia = pontos.firstOrNull()
            if (primeiroPontoDia != null) {
                val pontosAnteriores = pontoRepository.buscarPorEmpregoEData(empregoId, data.minusDays(1))
                val ultimoPontoAnterior = pontosAnteriores.sortedBy { it.dataHora }.lastOrNull()
                if (ultimoPontoAnterior != null) {
                    val descansoMinutos = Duration.between(ultimoPontoAnterior.dataHora, primeiroPontoDia.dataHora).toMinutes()
                    if (descansoMinutos < 660) { // 11h = 660min
                        inconsistencias.add(
                            InconsistenciaDetectada(
                                Inconsistencia.DESCANSO_INTERJORNADA_INSUFICIENTE,
                                detalhes = "Intervalo de ${descansoMinutos / 60}h ${descansoMinutos % 60}min entre dias"
                            )
                        )
                    }
                }
            }
        }

        // 3. Informativas
        if (resumo.isDiaEspecial && resumo.temPontos) {
            inconsistencias.add(InconsistenciaDetectada(Inconsistencia.TRABALHO_EM_DIA_ESPECIAL))
        }

        if (resumo.saldoDiaMinutos < 0) {
            inconsistencias.add(InconsistenciaDetectada(Inconsistencia.SALDO_NEGATIVO))
        }

        if (temJornadaPlanejada && resumo.horasTrabalhadasMinutos < 480 && resumo.horasTrabalhadasMinutos > 0) {
            // Jornada menor que 8h (Informativo, não é problema)
            inconsistencias.add(InconsistenciaDetectada(Inconsistencia.JORNADA_REDUZIDA))
        }

        // 4. Segurança e Localização (Configurações do Emprego)
        val config = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
        if (config != null) {
            if (config.fotoObrigatoria) {
                pontos.filter { !it.temFotoComprovante }.forEach { ponto ->
                    inconsistencias.add(InconsistenciaDetectada(Inconsistencia.COMPROVANTE_AUSENTE, pontoRelacionadoId = ponto.id))
                }
            }

            if (config.habilitarLocalizacao && config.latitude != null && config.longitude != null) {
                pontos.forEach { ponto ->
                    if (ponto.latitude == null || ponto.longitude == null) {
                        inconsistencias.add(InconsistenciaDetectada(Inconsistencia.FORA_DO_GEOFENCING, pontoRelacionadoId = ponto.id))
                    } else {
                        val dist = LocationUtils.calcularDistancia(config.latitude, config.longitude, ponto.latitude, ponto.longitude)
                        if (dist > config.raioGeofencing) {
                            inconsistencias.add(InconsistenciaDetectada(Inconsistencia.FORA_DO_GEOFENCING, detalhes = "${dist.toInt()}m do local", pontoRelacionadoId = ponto.id))
                        }
                    }
                }
            }
        }

        if (inconsistencias.isEmpty()) return null

        return PendenciaDia(
            data = data,
            inconsistencias = inconsistencias.distinctBy { it.inconsistencia },
            quantidadePontos = pontos.size,
            temJustificativa = pontos.any { !it.justificativaInconsistencia.isNullOrBlank() },
            isHoje = isHoje
        )
    }
}
