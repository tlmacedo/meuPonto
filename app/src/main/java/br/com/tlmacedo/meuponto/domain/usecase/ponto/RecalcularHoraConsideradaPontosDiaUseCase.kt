// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/RecalcularHoraConsideradaPontosDiaUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.ContextoJornadaDia
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.jornada.ObterContextoJornadaDiaUseCase
import timber.log.Timber
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.abs

/**
 * Recalcula a horaConsiderada dos pontos de um dia.
 *
 * REGRA OFICIAL:
 *
 * - hora = hora real do registro.
 * - horaConsiderada = hora usada para cálculo.
 * - Por padrão, horaConsiderada deve ser igual a hora.
 * - horaConsiderada só pode ser diferente de hora quando a tolerância de volta
 *   do intervalo foi aplicada.
 *
 * A tolerância de volta do intervalo:
 *
 * - só pode ser aplicada em retorno de intervalo;
 * - nunca pode ser aplicada no primeiro ponto do dia;
 * - só pode ocorrer em entradas após uma saída anterior;
 * - em índice humano: 3º, 5º, 7º, 9º ponto...
 * - em índice zero-based: 2, 4, 6, 8...
 * - só pode ser aplicada uma vez por dia;
 * - se houver mais de um candidato, escolhe o intervalo cuja saída anterior
 *   está mais próxima da saída ideal configurada para o dia.
 */
class RecalcularHoraConsideradaPontosDiaUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val obterContextoJornadaDiaUseCase: ObterContextoJornadaDiaUseCase
) {

    sealed class Resultado {
        data class Sucesso(
            val empregoId: Long,
            val data: LocalDate,
            val totalPontos: Int,
            val pontosAtualizados: Int,
            val pontoComToleranciaId: Long?,
            val detalhes: List<DetalhePonto>
        ) : Resultado()

        data class SemPontos(
            val empregoId: Long,
            val data: LocalDate
        ) : Resultado()

        data class ContextoNaoEncontrado(
            val empregoId: Long,
            val data: LocalDate,
            val motivo: String
        ) : Resultado()

        data class Erro(
            val mensagem: String
        ) : Resultado()
    }

    data class DetalhePonto(
        val pontoId: Long,
        val indiceHumano: Int,
        val horaReal: LocalTime,
        val horaConsideradaAnterior: LocalTime,
        val horaConsideradaNova: LocalTime,
        val atualizado: Boolean,
        val recebeuTolerancia: Boolean
    )

    private data class TurnoBase(
        val indiceTurno: Int,
        val entrada: Ponto,
        val saida: Ponto?
    )

    private data class CandidatoTolerancia(
        val indiceTurnoAtual: Int,
        val entradaAtual: Ponto,
        val saidaAnterior: Ponto,
        val intervaloRealMinutos: Int,
        val distanciaSaidaIdealMinutos: Int,
        val horaConsideradaCalculada: LocalTime
    )

    suspend operator fun invoke(
        empregoId: Long,
        data: LocalDate
    ): Resultado {
        return try {
            val contexto = obterContexto(empregoId, data)
                ?: return Resultado.ContextoNaoEncontrado(
                    empregoId = empregoId,
                    data = data,
                    motivo = "Não foi possível obter o contexto da jornada para o dia."
                )

            val pontos = pontoRepository
                .buscarPorEmpregoEData(empregoId, data)
                .sortedWith(
                    compareBy<Ponto> { it.data }
                        .thenBy { it.hora }
                        .thenBy { it.id }
                )

            if (pontos.isEmpty()) {
                return Resultado.SemPontos(
                    empregoId = empregoId,
                    data = data
                )
            }

            val pontosNormalizados = calcularPontosNormalizados(
                pontos = pontos,
                contexto = contexto
            )

            var atualizados = 0
            val detalhes = mutableListOf<DetalhePonto>()

            pontosNormalizados.forEachIndexed { index, normalizado ->
                val ponto = normalizado.ponto
                val novaHoraConsiderada = normalizado.horaConsideradaNova

                val precisaAtualizar = ponto.horaConsiderada != novaHoraConsiderada

                if (precisaAtualizar) {
                    pontoRepository.atualizar(
                        ponto.copy(
                            horaConsiderada = novaHoraConsiderada
                        )
                    )

                    atualizados++
                }

                detalhes.add(
                    DetalhePonto(
                        pontoId = ponto.id,
                        indiceHumano = index + 1,
                        horaReal = ponto.hora,
                        horaConsideradaAnterior = ponto.horaConsiderada,
                        horaConsideradaNova = novaHoraConsiderada,
                        atualizado = precisaAtualizar,
                        recebeuTolerancia = normalizado.recebeuTolerancia
                    )
                )
            }

            val pontoComTolerancia = pontosNormalizados
                .firstOrNull { it.recebeuTolerancia }
                ?.ponto
                ?.id

            Timber.i(
                "Hora considerada recalculada: empregoId=$empregoId, data=$data, " +
                        "pontos=${pontos.size}, atualizados=$atualizados, " +
                        "pontoComTolerancia=$pontoComTolerancia"
            )

            Resultado.Sucesso(
                empregoId = empregoId,
                data = data,
                totalPontos = pontos.size,
                pontosAtualizados = atualizados,
                pontoComToleranciaId = pontoComTolerancia,
                detalhes = detalhes
            )
        } catch (e: Exception) {
            Timber.e(e, "Erro ao recalcular horaConsiderada dos pontos do dia")
            Resultado.Erro(
                mensagem = e.message ?: "Erro ao recalcular hora considerada."
            )
        }
    }

    /**
     * Versão pura do cálculo, útil para testes unitários.
     *
     * Não grava no banco.
     */
    fun calcularSemPersistir(
        pontos: List<Ponto>,
        contexto: ContextoJornadaDia
    ): List<Ponto> {
        return calcularPontosNormalizados(
            pontos = pontos.sortedWith(
                compareBy<Ponto> { it.data }
                    .thenBy { it.hora }
                    .thenBy { it.id }
            ),
            contexto = contexto
        ).map { normalizado ->
            normalizado.ponto.copy(
                horaConsiderada = normalizado.horaConsideradaNova
            )
        }
    }

    private data class PontoNormalizado(
        val ponto: Ponto,
        val horaConsideradaNova: LocalTime,
        val recebeuTolerancia: Boolean
    )

    private fun calcularPontosNormalizados(
        pontos: List<Ponto>,
        contexto: ContextoJornadaDia
    ): List<PontoNormalizado> {
        if (pontos.isEmpty()) return emptyList()

        /**
         * Primeiro, todo mundo volta ao padrão:
         *
         * horaConsiderada = hora
         *
         * Depois aplicamos a tolerância em no máximo um ponto.
         */
        val pontoComTolerancia = selecionarPontoQueRecebeTolerancia(
            pontos = pontos,
            intervaloMinimoMinutos = contexto.intervaloMinimoMinutos,
            toleranciaVoltaIntervaloMinutos = contexto.toleranciaVoltaIntervaloMinutos,
            saidaIntervaloIdeal = contexto.saidaIntervaloIdeal
        )

        return pontos.map { ponto ->
            val recebeuTolerancia = pontoComTolerancia?.entradaAtual?.id == ponto.id

            val novaHoraConsiderada = if (recebeuTolerancia) {
                pontoComTolerancia?.horaConsideradaCalculada ?: ponto.hora
            } else {
                ponto.hora
            }

            PontoNormalizado(
                ponto = ponto,
                horaConsideradaNova = novaHoraConsiderada,
                recebeuTolerancia = recebeuTolerancia
            )
        }
    }

    private fun selecionarPontoQueRecebeTolerancia(
        pontos: List<Ponto>,
        intervaloMinimoMinutos: Int,
        toleranciaVoltaIntervaloMinutos: Int,
        saidaIntervaloIdeal: LocalTime?
    ): CandidatoTolerancia? {
        if (pontos.size < 3) return null
        if (intervaloMinimoMinutos <= 0) return null
        if (toleranciaVoltaIntervaloMinutos <= 0) return null

        val turnos = pontos
            .chunked(2)
            .mapIndexed { indiceTurno, par ->
                TurnoBase(
                    indiceTurno = indiceTurno,
                    entrada = par[0],
                    saida = par.getOrNull(1)
                )
            }

        if (turnos.size < 2) return null

        val limiteComTolerancia = intervaloMinimoMinutos + toleranciaVoltaIntervaloMinutos
        val candidatos = mutableListOf<CandidatoTolerancia>()

        /**
         * Começa em 1 porque o turno 0 é a primeira entrada do dia.
         *
         * Turno 0:
         * - entrada = ponto 1
         * - saída = ponto 2
         * - não existe pausa antes
         *
         * Turno 1:
         * - entrada = ponto 3
         * - saída = ponto 4
         * - pausa antes = saída do turno 0 -> entrada do turno 1
         */
        for (indiceTurnoAtual in 1 until turnos.size) {
            val turnoAnterior = turnos[indiceTurnoAtual - 1]
            val turnoAtual = turnos[indiceTurnoAtual]

            val saidaAnterior = turnoAnterior.saida ?: continue
            val entradaAtual = turnoAtual.entrada

            val saidaAnteriorDataHora = saidaAnterior.dataHoraReal()
            val entradaAtualDataHora = entradaAtual.dataHoraReal()

            val intervaloRealMinutos = Duration.between(
                saidaAnteriorDataHora,
                entradaAtualDataHora
            )
                .toMinutes()
                .toInt()

            if (intervaloRealMinutos <= intervaloMinimoMinutos) {
                continue
            }

            if (intervaloRealMinutos > limiteComTolerancia) {
                continue
            }

            val horaConsideradaCalculada = saidaAnteriorDataHora
                .plusMinutes(intervaloMinimoMinutos.toLong())
                .toLocalTime()

            val distanciaSaidaIdeal = calcularDistanciaSaidaIdealMinutos(
                saidaReal = saidaAnterior.hora,
                saidaIdeal = saidaIntervaloIdeal
            )

            candidatos.add(
                CandidatoTolerancia(
                    indiceTurnoAtual = indiceTurnoAtual,
                    entradaAtual = entradaAtual,
                    saidaAnterior = saidaAnterior,
                    intervaloRealMinutos = intervaloRealMinutos,
                    distanciaSaidaIdealMinutos = distanciaSaidaIdeal,
                    horaConsideradaCalculada = horaConsideradaCalculada
                )
            )
        }

        return candidatos.minWithOrNull(
            compareBy<CandidatoTolerancia> { it.distanciaSaidaIdealMinutos }
                .thenBy { it.indiceTurnoAtual }
        )
    }

    private fun calcularDistanciaSaidaIdealMinutos(
        saidaReal: LocalTime,
        saidaIdeal: LocalTime?
    ): Int {
        if (saidaIdeal == null) return 0

        return abs(
            Duration.between(saidaIdeal, saidaReal)
                .toMinutes()
                .toInt()
        )
    }

    private suspend fun obterContexto(
        empregoId: Long,
        data: LocalDate
    ): ContextoJornadaDia? {
        return when (val resultado = obterContextoJornadaDiaUseCase(empregoId, data)) {
            is ObterContextoJornadaDiaUseCase.Resultado.Sucesso -> {
                resultado.contexto
            }

            is ObterContextoJornadaDiaUseCase.Resultado.EmpregoNaoEncontrado,
            is ObterContextoJornadaDiaUseCase.Resultado.ConfiguracaoNaoEncontrada,
            is ObterContextoJornadaDiaUseCase.Resultado.VersaoNaoEncontrada,
            is ObterContextoJornadaDiaUseCase.Resultado.Erro -> {
                Timber.w(
                    "Contexto de jornada não encontrado para empregoId=$empregoId, data=$data: $resultado"
                )
                null
            }
        }
    }

    private fun Ponto.dataHoraReal(): LocalDateTime {
        return LocalDateTime.of(data, hora)
    }
}