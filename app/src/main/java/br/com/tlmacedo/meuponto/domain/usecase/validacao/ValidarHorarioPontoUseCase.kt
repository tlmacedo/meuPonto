// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/validacao/ValidarHorarioPontoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Inconsistencia
import br.com.tlmacedo.meuponto.domain.model.InconsistenciaDetectada
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResultadoValidacao
import br.com.tlmacedo.meuponto.util.minutosParaHoraMinuto
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Use Case responsável por validar horários de registros de ponto.
 *
 * Verifica se o horário do registro está dentro dos parâmetros esperados,
 * considerando:
 * - Horários ideais configurados
 * - Data/hora no futuro
 * - Registros muito antigos
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 2.11.0 - Usa formatadores padronizados de MinutosExtensions
 */
class ValidarHorarioPontoUseCase @Inject constructor() {

    companion object {
        /** Tolerância padrão para considerar fora do horário (em minutos) */
        private const val TOLERANCIA_HORARIO_MINUTOS = 60L

        /** Tolerância máxima para registro no futuro (em minutos) */
        private const val TOLERANCIA_FUTURO_MINUTOS = 5L

        /** Dias máximos para registro retroativo sem alerta */
        private const val DIAS_RETROATIVO_ALERTA = 7L
    }

    /**
     * Valida o horário de um registro de ponto.
     *
     * @param ponto Ponto a ser validado
     * @param horarioEsperado Configuração de horário do dia (opcional)
     * @param configuracao Configuração do emprego (opcional)
     * @param dataHoraAtual Data/hora atual para comparação
     * @return ResultadoValidacao com o resultado da validação
     */
    operator fun invoke(
        ponto: Ponto,
        horarioEsperado: HorarioDiaSemana? = null,
        configuracao: ConfiguracaoEmprego? = null,
        dataHoraAtual: LocalDateTime = LocalDateTime.now()
    ): ResultadoValidacao {
        val inconsistencias = mutableListOf<InconsistenciaDetectada>()

        // Validar registro no futuro
        validarRegistroFuturo(ponto, dataHoraAtual)?.let {
            inconsistencias.add(it)
        }

        // Validar registro muito antigo
        validarRegistroAntigo(ponto, dataHoraAtual)?.let {
            inconsistencias.add(it)
        }

        // Validar horário esperado (se configurado)
        if (horarioEsperado != null && horarioEsperado.temHorariosIdeais) {
            validarHorarioEsperado(ponto, horarioEsperado)?.let {
                inconsistencias.add(it)
            }
        }

        return criarResultado(ponto, inconsistencias)
    }

    /**
     * Valida se o registro está no futuro.
     */
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

    /**
     * Valida se o registro é muito antigo.
     */
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

    /**
     * Valida se o horário está dentro do esperado para o dia.
     * Nota: Sem o índice, verifica apenas se está próximo de algum horário ideal.
     */
    private fun validarHorarioEsperado(
        ponto: Ponto,
        horario: HorarioDiaSemana
    ): InconsistenciaDetectada? {
        val horaPonto = ponto.dataHora.toLocalTime()

        // Verificar proximidade com horários ideais configurados
        val horariosIdeais = listOfNotNull(
            horario.entradaIdeal,
            horario.saidaIdeal,
            horario.saidaIntervaloIdeal,
            horario.voltaIntervaloIdeal
        )

        if (horariosIdeais.isEmpty()) return null

        // Encontrar o horário ideal mais próximo
        val horarioMaisProximo = horariosIdeais.minByOrNull {
            Duration.between(it, horaPonto).abs().toMinutes()
        } ?: return null

        val diferencaMinutos = Duration.between(horarioMaisProximo, horaPonto).abs().toMinutes()

        return if (diferencaMinutos > TOLERANCIA_HORARIO_MINUTOS) {
            InconsistenciaDetectada(
                inconsistencia = Inconsistencia.FORA_HORARIO_ESPERADO,
                detalhes = buildString {
                    append("Registrado: ${formatarHora(horaPonto)} ")
                    append("(diferença de $diferencaMinutos min do esperado)")
                }
            )
        } else null
    }

    /**
     * Valida especificamente horários de intervalo.
     *
     * @param saidaIntervalo Ponto de saída para intervalo
     * @param voltaIntervalo Ponto de volta do intervalo
     * @param horario Configuração de horário do dia
     * @return Lista de inconsistências encontradas
     */
    fun validarIntervalo(
        saidaIntervalo: Ponto,
        voltaIntervalo: Ponto,
        horario: HorarioDiaSemana
    ): List<InconsistenciaDetectada> {
        val inconsistencias = mutableListOf<InconsistenciaDetectada>()

        val duracaoIntervalo = Duration.between(
            saidaIntervalo.dataHora,
            voltaIntervalo.dataHora
        ).toMinutes()

        // Verificar intervalo mínimo
        if (duracaoIntervalo < horario.intervaloMinimoMinutos) {
            inconsistencias.add(
                InconsistenciaDetectada(
                    inconsistencia = Inconsistencia.INTERVALO_ALMOCO_INSUFICIENTE,
                    detalhes = buildString {
                        append("Intervalo de $duracaoIntervalo min, ")
                        append("mínimo: ${horario.intervaloMinimoMinutos} min")
                    }
                )
            )
        }

        // Verificar tolerância para mais
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
     *
     * @param ultimaSaidaDiaAnterior Último ponto de saída do dia anterior
     * @param primeiraEntradaHoje Primeiro ponto de entrada de hoje
     * @param configuracao Configuração do emprego
     * @return Inconsistência se houver violação, null caso contrário
     */
    fun validarIntervaloInterjornada(
        ultimaSaidaDiaAnterior: Ponto,
        primeiraEntradaHoje: Ponto,
        configuracao: ConfiguracaoEmprego
    ): InconsistenciaDetectada? {
        val intervaloMinutos = Duration.between(
            ultimaSaidaDiaAnterior.dataHora,
            primeiraEntradaHoje.dataHora
        ).toMinutes()

        val minimoMinutos = configuracao.intervaloMinimoInterjornadaMinutos.toLong()

        return if (intervaloMinutos < minimoMinutos) {
            InconsistenciaDetectada(
                inconsistencia = Inconsistencia.INTERVALO_INTERJORNADA_INSUFICIENTE,
                detalhes = buildString {
                    append("Intervalo de ${intervaloMinutos.minutosParaHoraMinuto()}, ")
                    append("mínimo: ${minimoMinutos.minutosParaHoraMinuto()}")
                }
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

    private fun formatarHora(hora: LocalTime): String {
        return String.format("%02d:%02d", hora.hour, hora.minute)
    }
}
