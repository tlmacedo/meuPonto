// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/feriado/CalcularDistribuicaoPontesUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.feriado

import br.com.tlmacedo.meuponto.domain.model.feriado.ConfiguracaoPontesAno
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular a distribuição de horas de feriados ponte.
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
class CalcularDistribuicaoPontesUseCase @Inject constructor(
    private val feriadoRepository: FeriadoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val verificarDiaEspecialUseCase: VerificarDiaEspecialUseCase
) {

    sealed class Resultado {
        data class Sucesso(val configuracao: ConfiguracaoPontesAno) : Resultado()
        data class SemPontes(val ano: Int) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
    }

    suspend operator fun invoke(
        empregoId: Long,
        ano: Int,
        cargaHorariaDiariaMinutos: Int? = null
    ): Resultado {
        return try {
            val pontes = feriadoRepository.buscarPontesPorAnoEEmprego(ano, empregoId)

            if (pontes.isEmpty()) {
                return Resultado.SemPontes(ano)
            }

            // Obter carga horária da versão de jornada vigente
            val cargaDiaria = cargaHorariaDiariaMinutos
                ?: versaoJornadaRepository.buscarVigente(empregoId)?.cargaHorariaDiariaMinutos
                ?: 480

            val diasPonte = pontes.size
            val totalMinutosPonte = diasPonte * cargaDiaria
            val diasUteis = contarDiasUteisAno(ano, empregoId)

            val adicionalDiario = if (diasUteis > 0) {
                kotlin.math.ceil(totalMinutosPonte.toDouble() / diasUteis).toInt()
            } else {
                0
            }

            val configuracao = ConfiguracaoPontesAno(
                empregoId = empregoId,
                ano = ano,
                diasPonte = diasPonte,
                cargaHorariaPonteMinutos = totalMinutosPonte,
                diasUteisAno = diasUteis,
                adicionalDiarioMinutos = adicionalDiario,
                observacao = buildString {
                    append("Pontes: ${pontes.joinToString { it.nome }}")
                    append("\n$diasPonte dias × ${cargaDiaria / 60}h = ${totalMinutosPonte / 60}h")
                    append("\n${totalMinutosPonte / 60}h / $diasUteis dias = ${adicionalDiario}min/dia")
                }
            )

            Resultado.Sucesso(configuracao)
        } catch (e: Exception) {
            Resultado.Erro("Erro ao calcular distribuição: ${e.message}")
        }
    }

    suspend fun calcularESalvar(
        empregoId: Long,
        ano: Int,
        cargaHorariaDiariaMinutos: Int? = null
    ): Resultado {
        val resultado = invoke(empregoId, ano, cargaHorariaDiariaMinutos)

        if (resultado is Resultado.Sucesso) {
            val existente = feriadoRepository.buscarConfiguracaoPontes(empregoId, ano)
            if (existente != null) {
                feriadoRepository.atualizarConfiguracaoPontes(resultado.configuracao.copy(id = existente.id))
            } else {
                feriadoRepository.inserirConfiguracaoPontes(resultado.configuracao)
            }
        }

        return resultado
    }

    private suspend fun contarDiasUteisAno(ano: Int, empregoId: Long): Int {
        val dataInicio = LocalDate.of(ano, 1, 1)
        val dataFim = LocalDate.of(ano, 12, 31)

        val feriados = feriadoRepository.buscarPorAno(ano)
            .filter { it.tipo != TipoFeriado.PONTE }
            .mapNotNull { it.getDataParaAno(ano) }
            .toSet()

        var diasUteis = 0
        var data = dataInicio

        while (!data.isAfter(dataFim)) {
            if (data.dayOfWeek !in listOf(
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                ) && data !in feriados
            ) {
                diasUteis++
            }
            data = data.plusDays(1)
        }

        return diasUteis
    }

    suspend fun recalcularTodos(empregoId: Long): List<Int> {
        val anoAtual = LocalDate.now().year
        val anosParaCalcular = listOf(anoAtual - 1, anoAtual, anoAtual + 1)
        val anosRecalculados = mutableListOf<Int>()

        for (ano in anosParaCalcular) {
            val resultado = calcularESalvar(empregoId, ano)
            if (resultado is Resultado.Sucesso) {
                anosRecalculados.add(ano)
            }
        }

        return anosRecalculados
    }
}
