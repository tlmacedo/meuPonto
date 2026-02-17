// Arquivo: ValidarPontoCompletoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.validacao

import br.com.tlmacedo.meuponto.domain.model.ConfiguracaoEmprego
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Inconsistencia
import br.com.tlmacedo.meuponto.domain.model.InconsistenciaDetectada
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.PontoConstants
import br.com.tlmacedo.meuponto.domain.model.ResultadoValidacao
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use Case orquestrador que executa todas as validações de ponto.
 *
 * @author Thiago
 * @since 2.0.0
 * @updated 2.1.0 - Adaptado para tipo calculado por posição
 */
class ValidarPontoCompletoUseCase @Inject constructor(
    private val validarHorario: ValidarHorarioPontoUseCase,
    private val validarJornada: ValidarJornadaDiariaUseCase,
    private val pontoRepository: PontoRepository
) {
    /**
     * Executa todas as validações para um novo registro de ponto.
     */
    suspend operator fun invoke(
        empregoId: Long,
        ponto: Ponto,
        configuracao: ConfiguracaoEmprego,
        horarioEsperado: HorarioDiaSemana? = null,
        dataHoraAtual: LocalDateTime = LocalDateTime.now()
    ): ResultadoValidacao {
        val todasInconsistencias = mutableListOf<InconsistenciaDetectada>()

        // Buscar pontos existentes para validação
        val data = ponto.dataHora.toLocalDate()
        val pontosExistentes = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        val indiceNovoPonto = pontosExistentes.size

        // 1. Validar quantidade máxima
        if (pontosExistentes.size >= PontoConstants.MAX_PONTOS) {
            return ResultadoValidacao.falha(
                inconsistencias = listOf(
                    InconsistenciaDetectada(
                        inconsistencia = Inconsistencia.REGISTROS_IMPARES,
                        detalhes = "Quantidade máxima de ${PontoConstants.MAX_PONTOS} pontos atingida"
                    )
                ),
                mensagem = "Quantidade máxima de pontos atingida"
            )
        }

        // 2. Validar horário
        val resultadoHorario = validarHorario(
            ponto = ponto,
            horarioEsperado = horarioEsperado,
            configuracao = configuracao,
            dataHoraAtual = dataHoraAtual
        )
        todasInconsistencias.addAll(resultadoHorario.inconsistencias)

        if (resultadoHorario.temInconsistenciasBloqueantes) {
            return ResultadoValidacao.falha(
                inconsistencias = todasInconsistencias,
                mensagem = "Horário do registro inválido"
            )
        }

        // 3. Validar jornada (se for saída)
        val isSaida = indiceNovoPonto % 2 == 1
        if (isSaida && pontosExistentes.isNotEmpty()) {
            val todosPontos = pontosExistentes + ponto
            val resultadoJornada = validarJornada(
                pontos = todosPontos,
                jornadaMaximaMinutos = configuracao.jornadaMaximaDiariaMinutos.toLong(),
                intervaloMinimoMinutos = horarioEsperado?.intervaloMinimoMinutos?.toLong() ?: 60L
            )
            // Converter resultado de jornada em inconsistências
            converterResultadoJornada(resultadoJornada)?.let {
                todasInconsistencias.add(it)
            }
        }

        // Consolidar resultado
        val temBloqueantes = todasInconsistencias.any { it.isBloqueante }

        return if (temBloqueantes) {
            ResultadoValidacao.falha(
                inconsistencias = todasInconsistencias,
                mensagem = "Registro com inconsistências bloqueantes"
            )
        } else {
            ResultadoValidacao(
                isValido = true,
                inconsistencias = todasInconsistencias,
                pontoValidado = ponto,
                mensagens = if (todasInconsistencias.isNotEmpty()) {
                    listOf("Registro válido com ${todasInconsistencias.size} alerta(s)")
                } else {
                    listOf("Registro válido")
                }
            )
        }
    }

    /**
     * Valida um dia completo.
     */
    suspend fun validarDiaCompleto(
        empregoId: Long,
        data: LocalDate,
        configuracao: ConfiguracaoEmprego,
        horarioEsperado: HorarioDiaSemana? = null
    ): ResultadoValidacao {
        val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        val todasInconsistencias = mutableListOf<InconsistenciaDetectada>()

        // Validar jornada do dia (se houver pontos)
        if (pontos.size >= 2) {
            val resultadoJornada = validarJornada(
                pontos = pontos,
                jornadaMaximaMinutos = configuracao.jornadaMaximaDiariaMinutos.toLong(),
                intervaloMinimoMinutos = horarioEsperado?.intervaloMinimoMinutos?.toLong() ?: 60L
            )
            converterResultadoJornada(resultadoJornada)?.let {
                todasInconsistencias.add(it)
            }
        }

        val temBloqueantes = todasInconsistencias.any { it.isBloqueante }

        return ResultadoValidacao(
            isValido = !temBloqueantes,
            inconsistencias = todasInconsistencias,
            mensagens = listOf(
                when {
                    temBloqueantes -> "Dia com inconsistências críticas"
                    todasInconsistencias.isNotEmpty() -> "Dia válido com alertas"
                    else -> "Dia sem inconsistências"
                }
            )
        )
    }

    /**
     * Converte ResultadoValidacaoJornada em InconsistenciaDetectada.
     */
    private fun converterResultadoJornada(
        resultado: ValidarJornadaDiariaUseCase.ResultadoValidacaoJornada
    ): InconsistenciaDetectada? {
        return when (resultado) {
            is ValidarJornadaDiariaUseCase.ResultadoValidacaoJornada.JornadaExcedida -> {
                InconsistenciaDetectada(
                    inconsistencia = Inconsistencia.JORNADA_EXCEDIDA,
                    detalhes = "Jornada excedeu em ${resultado.minutosExcedidos} minutos"
                )
            }
            is ValidarJornadaDiariaUseCase.ResultadoValidacaoJornada.IntervaloInsuficiente -> {
                InconsistenciaDetectada(
                    inconsistencia = Inconsistencia.INTERVALO_ALMOCO_INSUFICIENTE,
                    detalhes = "Intervalo de ${resultado.minutosIntervalo} min, mínimo: ${resultado.minimoNecessario} min"
                )
            }
            ValidarJornadaDiariaUseCase.ResultadoValidacaoJornada.Valida,
            ValidarJornadaDiariaUseCase.ResultadoValidacaoJornada.DadosInsuficientes -> null
        }
    }
}
