// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/feriado/CalcularDistribuicaoPontesUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.feriado

import br.com.tlmacedo.meuponto.domain.model.feriado.ConfiguracaoPontesAno
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para calcular a distribuição de horas de feriados ponte.
 *
 * Este use case:
 * 1. Conta os dias de ponte registrados para um ano/emprego
 * 2. Calcula o total de horas a compensar
 * 3. Conta os dias úteis do ano (excluindo feriados e fins de semana)
 * 4. Calcula o adicional diário a ser acrescido na jornada
 *
 * @author Thiago
 * @since 3.0.0
 */
class CalcularDistribuicaoPontesUseCase @Inject constructor(
    private val feriadoRepository: FeriadoRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository,
    private val verificarDiaEspecialUseCase: VerificarDiaEspecialUseCase
) {

    /**
     * Resultado do cálculo.
     */
    sealed class Resultado {
        data class Sucesso(val configuracao: ConfiguracaoPontesAno) : Resultado()
        data class SemPontes(val ano: Int) : Resultado()
        data class Erro(val mensagem: String) : Resultado()
    }

    /**
     * Calcula a distribuição de pontes para um ano.
     *
     * @param empregoId ID do emprego
     * @param ano Ano de referência
     * @param cargaHorariaDiariaMinutos Carga horária diária base (opcional, busca do emprego)
     * @return Resultado com a configuração calculada
     */
    suspend operator fun invoke(
        empregoId: Long,
        ano: Int,
        cargaHorariaDiariaMinutos: Int? = null
    ): Resultado {
        return try {
            // 1. Buscar pontes do ano
            val pontes = feriadoRepository.buscarPontesPorAnoEEmprego(ano, empregoId)

            if (pontes.isEmpty()) {
                return Resultado.SemPontes(ano)
            }

            // 2. Obter carga horária diária base
            val cargaDiaria = cargaHorariaDiariaMinutos
                ?: configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)?.cargaHorariaDiariaMinutos
                ?: 480 // 8h padrão

            // 3. Calcular total de horas a compensar
            val diasPonte = pontes.size
            val totalMinutosPonte = diasPonte * cargaDiaria

            // 4. Contar dias úteis do ano
            val diasUteis = contarDiasUteisAno(ano, empregoId)

            // 5. Calcular adicional diário (arredondado para cima)
            val adicionalDiario = if (diasUteis > 0) {
                kotlin.math.ceil(totalMinutosPonte.toDouble() / diasUteis).toInt()
            } else {
                0
            }

            // 6. Montar configuração
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

    /**
     * Calcula e salva a configuração de pontes.
     *
     * @param empregoId ID do emprego
     * @param ano Ano de referência
     * @return Resultado com a configuração salva
     */
    suspend fun calcularESalvar(
        empregoId: Long,
        ano: Int,
        cargaHorariaDiariaMinutos: Int? = null
    ): Resultado {
        val resultado = invoke(empregoId, ano, cargaHorariaDiariaMinutos)

        if (resultado is Resultado.Sucesso) {
            // Verifica se já existe configuração para o ano
            val existente = feriadoRepository.buscarConfiguracaoPontes(empregoId, ano)

            if (existente != null) {
                // Atualiza
                feriadoRepository.atualizarConfiguracaoPontes(
                    resultado.configuracao.copy(id = existente.id)
                )
            } else {
                // Insere nova
                feriadoRepository.inserirConfiguracaoPontes(resultado.configuracao)
            }
        }

        return resultado
    }

    /**
     * Conta os dias úteis de um ano para um emprego.
     *
     * Exclui:
     * - Fins de semana (sábado e domingo)
     * - Feriados nacionais, estaduais e municipais
     * - Pontes (pois são os dias que estão sendo compensados)
     *
     * @param ano Ano de referência
     * @param empregoId ID do emprego
     * @return Quantidade de dias úteis
     */
    private suspend fun contarDiasUteisAno(ano: Int, empregoId: Long): Int {
        val dataInicio = LocalDate.of(ano, 1, 1)
        val dataFim = LocalDate.of(ano, 12, 31)

        // Buscar todos os feriados do ano (exceto pontes, que são os dias sendo compensados)
        val feriados = feriadoRepository.buscarPorAno(ano)
            .filter { it.tipo != TipoFeriado.PONTE }
            .mapNotNull { it.getDataParaAno(ano) }
            .toSet()

        var diasUteis = 0
        var data = dataInicio

        while (!data.isAfter(dataFim)) {
            // Não é fim de semana e não é feriado
            if (data.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) &&
                data !in feriados
            ) {
                diasUteis++
            }
            data = data.plusDays(1)
        }

        return diasUteis
    }

    /**
     * Recalcula a distribuição de pontes para todos os anos com pontes registradas.
     *
     * @param empregoId ID do emprego
     * @return Lista de anos recalculados
     */
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
