// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/validacao/ValidarHorarioPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Inconsistencia
import br.com.tlmacedo.meuponto.domain.model.InconsistenciaDetectada
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResultadoValidacao
import br.com.tlmacedo.meuponto.domain.model.VersaoJornada
import br.com.tlmacedo.meuponto.util.helper.minutosParaHoraMinuto
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Use Case responsável por validar horários de registros de ponto.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
class ValidarHorarioPontoUseCase @Inject constructor() {

    companion object {
        private const val TOLERANCIA_HORARIO_MINUTOS = 60L
        private const val TOLERANCIA_FUTURO_MINUTOS = 5L
        private const val DIAS_RETROATIVO_ALERTA = 7L
    }

    operator fun invoke(
        ponto: Ponto,
        horarioEsperado: HorarioDiaSemana? = null,
        versaoJornada: VersaoJornada? = null,
        dataHoraAtual: LocalDateTime = LocalDateTime.now()
    ): ResultadoValidacao {
        val inconsistencias = mutableListOf<InconsistenciaDetectada>()

        validarRegistroFuturo(ponto, dataHoraAtual)?.let { inconsistencias.add(it) }
        validarRegistroAntigo(ponto, dataHoraAtual)?.let { inconsistencias.add(it) }

        if (horarioEsperado != null && horarioEsperado.temHorariosIdeais) {
            validarHorarioEsperado(ponto, horarioEsperado)?.let { inconsistencias.add(it) }
        }

        return criarResultado(ponto, inconsistencias)
    }

    private fun validarRegistroFuturo(
        ponto: Ponto,
        dataHoraAtual: LocalDateTime
    ): InconsistenciaDetectada? {
        val diferencaMinutos = Duration.between(dataHoraAtual, ponto.dataHora).toMinutes()
        return if (diferencaMinutos > TOLERANCIA_FUTURO_MINUTOS) {
            InconsistenciaDetectada(
                inconsistencia = Inconsistencia.REGISTRO_NO_FUTURO,
                detalhes = "Registro ${diferencaMinutos} minuto(s) no futuro"
            )
        } else null
    }

    private fun validarRegistroAntigo(
        ponto: Ponto,
        dataHoraAtual: LocalDateTime
    ): InconsistenciaDetectada? {
        val diasAtras = Duration.between(ponto.dataHora, dataHoraAtual).toDays()
        return if (diasAtras > DIAS_RETROATIVO_ALERTA) {
            InconsistenciaDetectada(
                inconsistencia = Inconsistencia.REGISTRO_RETROATIVO,
                detalhes = "Registro de $diasAtras dia(s) atrás"
            )
        } else null
    }

    private fun validarHorarioEsperado(
        ponto: Ponto,
        horario: HorarioDiaSemana
    ): InconsistenciaDetectada? {
        val horaPonto = ponto.dataHora.toLocalTime()

        val horariosIdeais = listOfNotNull(
            horario.entradaIdeal,
            horario.saidaIdeal,
            horario.saidaIntervaloIdeal,
            horario.voltaIntervaloIdeal
        )

        if (horariosIdeais.isEmpty()) return null

        val horarioMaisProximo = horariosIdeais.minByOrNull {
            Duration.between(it, horaPonto).abs().toMinutes()
        } ?: return null

        val diferencaMinutos = Duration.between(horarioMaisProximo, horaPonto).abs().toMinutes()

        return if (diferencaMinutos > TOLERANCIA_HORARIO_MINUTOS) {
            InconsistenciaDetectada(
                inconsistencia = Inconsistencia.FORA_HORARIO_ESPERADO,
                detalhes = "Registrado: ${formatarHora(horaPonto)} (diferença de $diferencaMinutos min do esperado)"
            )
        } else null
    }

    fun validarIntervalo(
        saidaIntervalo: Ponto,
        voltaIntervalo: Ponto,
        horario: HorarioDiaSemana

    ): List<InconsistenciaDetectada> {
        val inconsistencias = mutableListOf<InconsistenciaDetectada>()

        val duracaoIntervalo =
            Duration.between(saidaIntervalo.dataHora, voltaIntervalo.dataHora).toMinutes()

        if (duracaoIntervalo < horario.intervaloMinimoMinutos) {
            inconsistencias.add(
                InconsistenciaDetectada(
                    inconsistencia = Inconsistencia.INTERVALO_ALMOCO_INSUFICIENTE,
                    detalhes = "Intervalo de $duracaoIntervalo min, mínimo: ${horario.intervaloMinimoMinutos} min"
                )
            )
        }

        val toleranciaMais = horario.toleranciaIntervaloMaisMinutos
        if (toleranciaMais > 0) {
            val limiteMaximo = horario.intervaloMinimoMinutos + toleranciaMais
            if (duracaoIntervalo > limiteMaximo) {
                inconsistencias.add(
                    InconsistenciaDetectada(
                        inconsistencia = Inconsistencia.INTERVALO_MUITO_LONGO,
                        detalhes = "Intervalo de $duracaoIntervalo min excede limite de $limiteMaximo min"
                    )
                )
            }
        }

        return inconsistencias
    }

    /**
     * Valida o intervalo interjornada (entre dias).
     */
    fun validarIntervaloInterjornada(
        ultimaSaidaDiaAnterior: Ponto,
        primeiraEntradaHoje: Ponto,
        versaoJornada: VersaoJornada
    ): InconsistenciaDetectada? {
        val intervaloMinutos = Duration.between(
            ultimaSaidaDiaAnterior.dataHora,
            primeiraEntradaHoje.dataHora
        ).toMinutes()

        val minimoMinutos = versaoJornada.intervaloMinimoInterjornadaMinutos.toLong()

        return if (intervaloMinutos < minimoMinutos) {
            InconsistenciaDetectada(
                inconsistencia = Inconsistencia.INTERVALO_INTERJORNADA_INSUFICIENTE,
                detalhes = "Intervalo de ${intervaloMinutos.minutosParaHoraMinuto()}, mínimo: ${minimoMinutos.minutosParaHoraMinuto()}"
            )
        } else null
    }

    private fun criarResultado(
        ponto: Ponto,
        inconsistencias: List<InconsistenciaDetectada>
    ): ResultadoValidacao {
        val temBloqueantes = inconsistencias.any { it.isBloqueante }
        return if (temBloqueantes) {
            ResultadoValidacao.falha(inconsistencias)
        } else {
            ResultadoValidacao.sucesso(ponto, inconsistencias)
        }
    }

    private fun formatarHora(hora: LocalTime): String =
        String.format("%02d:%02d", hora.hour, hora.minute)
}
