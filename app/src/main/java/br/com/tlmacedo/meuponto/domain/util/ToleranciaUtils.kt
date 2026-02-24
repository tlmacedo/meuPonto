// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/util/ToleranciaUtils.kt
package br.com.tlmacedo.meuponto.domain.util

import br.com.tlmacedo.meuponto.domain.model.Ponto
import java.time.Duration
import java.time.LocalTime
import kotlin.math.abs

/**
 * Utilitário para aplicar tolerância de intervalo aos pontos.
 *
 * Preenche horaConsiderada em cada ponto:
 * - Se não precisar de ajuste: horaConsiderada = dataHora (cópia)
 * - Se precisar de ajuste: horaConsiderada = hora ajustada (tolerância aplicada)
 *
 * REGRA DE TOLERÂNCIA (CLT Art. 71):
 * - Apenas UMA pausa por dia recebe tolerância (a "pausa principal" / almoço)
 * - A pausa principal é identificada pelo saidaIntervaloIdeal ou pela primeira pausa >= intervaloMinimo
 * - Se a pausa estiver entre [intervaloMinimo, intervaloMinimo + tolerancia], ajusta para intervaloMinimo
 * - O ajuste é feito na ENTRADA de volta (adiantando-a para reduzir a pausa considerada)
 *
 * EXEMPLO:
 * - Saída almoço: 12:31
 * - Entrada volta: 13:45 (pausa real = 74 min)
 * - Intervalo mínimo: 60 min, tolerância: 15 min
 * - Como 74 está em [60, 75], aplica tolerância
 * - horaConsiderada da entrada volta = 12:31 + 60 = 13:31
 *
 * @author Thiago
 * @since 6.7.0
 */
object ToleranciaUtils {

    /**
     * Aplica tolerância de intervalo aos pontos, preenchendo horaConsiderada.
     *
     * @param pontos Lista de pontos (será ordenada internamente)
     * @param intervaloMinimoMinutos Intervalo mínimo obrigatório (ex: 60)
     * @param toleranciaMinutos Tolerância máxima (ex: 15 → aceita até 75 min)
     * @param saidaIntervaloIdeal Horário ideal de saída para almoço (para identificar pausa principal)
     * @return Lista de pontos com horaConsiderada preenchida (ordenada por dataHora)
     */
    fun aplicarTolerancia(
        pontos: List<Ponto>,
        intervaloMinimoMinutos: Int,
        toleranciaMinutos: Int,
        saidaIntervaloIdeal: LocalTime? = null
    ): List<Ponto> {
        if (pontos.isEmpty()) return pontos

        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        val limiteInferior = intervaloMinimoMinutos
        val limiteSuperior = intervaloMinimoMinutos + toleranciaMinutos

        // 1. Identificar todas as pausas (entre saída e próxima entrada)
        data class InfoPausa(
            val indiceEntrada: Int,
            val indiceSaida: Int,
            val pausaMinutos: Int,
            val elegivelTolerancia: Boolean
        )

        val pausas = mutableListOf<InfoPausa>()

        var i = 1
        while (i < pontosOrdenados.size) {
            val saida = pontosOrdenados[i]
            val entradaVolta = pontosOrdenados.getOrNull(i + 1)

            if (entradaVolta != null) {
                val pausaMinutos = Duration.between(saida.dataHora, entradaVolta.dataHora)
                    .toMinutes().toInt()

                pausas.add(
                    InfoPausa(
                        indiceEntrada = i + 1,
                        indiceSaida = i,
                        pausaMinutos = pausaMinutos,
                        elegivelTolerancia = pausaMinutos in limiteInferior..limiteSuperior
                    )
                )
            }
            i += 2
        }

        // 2. Determinar qual pausa PODE receber tolerância
        val pausasLongas = pausas.filter { it.pausaMinutos >= limiteInferior }

        val indiceEntradaComTolerancia: Int? = if (pausasLongas.isEmpty()) {
            null
        } else {
            val pausaPrincipal = if (saidaIntervaloIdeal != null) {
                pausasLongas.minByOrNull { pausa ->
                    val horaSaida = pontosOrdenados[pausa.indiceSaida].dataHora.toLocalTime()
                    abs(Duration.between(horaSaida, saidaIntervaloIdeal).toMinutes())
                }
            } else {
                pausasLongas.firstOrNull()
            }

            if (pausaPrincipal?.elegivelTolerancia == true) {
                pausaPrincipal.indiceEntrada
            } else {
                null
            }
        }

        // 3. Construir lista com horaConsiderada preenchida
        return pontosOrdenados.mapIndexed { index, ponto ->
            val novaHoraConsiderada = if (index == indiceEntradaComTolerancia) {
                val saidaAnterior = pontosOrdenados[index - 1]
                saidaAnterior.dataHora.plusMinutes(intervaloMinimoMinutos.toLong())
            } else {
                ponto.dataHora
            }

            ponto.copy(horaConsiderada = novaHoraConsiderada)
        }
    }
}
