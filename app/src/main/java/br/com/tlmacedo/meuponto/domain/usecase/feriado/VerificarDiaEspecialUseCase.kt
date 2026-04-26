// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/feriado/VerificarDiaEspecialUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.feriado

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.ResultadoVerificacaoDia
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para verificar se um dia é especial (feriado, fim de semana, ponte).
 *
 * Retorna informações detalhadas sobre o dia, incluindo:
 * - Se é feriado e qual tipo
 * - Se é fim de semana
 * - Se é dia útil
 * - Carga horária esperada
 * - Se permite registro de ponto
 *
 * @author Thiago
 * @since 3.0.0
 */
class VerificarDiaEspecialUseCase @Inject constructor(
    private val feriadoRepository: FeriadoRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository
) {

    /**
     * Verifica as características de um dia específico.
     *
     * @param data Data a verificar
     * @param empregoId ID do emprego para contexto
     * @param ufEmprego UF do emprego (para feriados estaduais)
     * @param municipioEmprego Município do emprego (para feriados municipais)
     * @return Resultado com informações do dia
     */
    suspend operator fun invoke(
        data: LocalDate,
        empregoId: Long,
        ufEmprego: String? = null,
        municipioEmprego: String? = null
    ): ResultadoVerificacaoDia {
        // 1. Verificar se é fim de semana
        val isFimDeSemana = data.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

        // 2. Buscar feriados aplicáveis à data
        val feriadosNaData = feriadoRepository.buscarPorData(data)
            .filter { it.ativo && it.aplicavelPara(empregoId, ufEmprego, municipioEmprego) }

        // 3. Determinar o feriado mais relevante (prioridade: específico > nacional)
        val feriado = determinarFeriadoPrioritario(feriadosNaData, empregoId)
        val isPonte = feriado?.tipo == TipoFeriado.PONTE

        // 4. Buscar configuração de jornada para o dia
        val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)
        val diaSemana = DiaSemana.fromDayOfWeek(data.dayOfWeek)
        val horarioDia = versaoJornada?.id?.let {
            horarioDiaSemanaRepository.buscarPorVersaoEDia(it, diaSemana)
        }

        // 5. Calcular carga horária esperada
        val cargaHorariaEsperada = when {
            // Feriado de folga = 0
            feriado != null && feriado.isFolga -> 0
            // Ponte = 0 (já foi distribuído na jornada diária)
            isPonte -> 0
            // Fim de semana (sem configuração de trabalho) = 0
            isFimDeSemana && horarioDia?.cargaHorariaMinutos == null -> 0
            // Dia com configuração = carga configurada
            horarioDia != null -> horarioDia.cargaHorariaMinutos
            // Sem configuração = 0
            else -> 0
        }

        // 6. Determinar se permite registro de ponto
        val permiteRegistro = when {
            // Feriado oficial não permite (a menos que seja facultativo)
            feriado != null && feriado.tipo in TipoFeriado.tiposFolga() -> false
            // Ponte não permite
            isPonte -> false
            // Fim de semana sem jornada configurada não permite
            isFimDeSemana && horarioDia == null -> false
            // Demais casos permite
            else -> true
        }

        // 7. Montar mensagem descritiva
        val mensagem = when {
            feriado != null -> "${feriado.tipo.emoji} ${feriado.nome}"
            isFimDeSemana -> "🛋️ Fim de semana"
            else -> null
        }

        return ResultadoVerificacaoDia(
            data = data,
            feriado = feriado,
            isPonte = isPonte,
            isFimDeSemana = isFimDeSemana,
            isDiaUtil = !isFimDeSemana && feriado == null,
            permiteRegistroPonto = permiteRegistro,
            cargaHorariaEsperadaMinutos = cargaHorariaEsperada,
            mensagem = mensagem
        )
    }

    /**
     * Verifica múltiplas datas de uma vez.
     *
     * @param datas Lista de datas a verificar
     * @param empregoId ID do emprego
     * @return Mapa de data para resultado
     */
    suspend fun verificarMultiplas(
        datas: List<LocalDate>,
        empregoId: Long,
        ufEmprego: String? = null,
        municipioEmprego: String? = null
    ): Map<LocalDate, ResultadoVerificacaoDia> {
        return datas.associateWith { data ->
            invoke(data, empregoId, ufEmprego, municipioEmprego)
        }
    }

    /**
     * Conta dias úteis em um período.
     *
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @param empregoId ID do emprego
     * @return Quantidade de dias úteis
     */
    suspend fun contarDiasUteis(
        dataInicio: LocalDate,
        dataFim: LocalDate,
        empregoId: Long,
        ufEmprego: String? = null,
        municipioEmprego: String? = null
    ): Int {
        var diasUteis = 0
        var data = dataInicio

        while (!data.isAfter(dataFim)) {
            val resultado = invoke(data, empregoId, ufEmprego, municipioEmprego)
            if (resultado.isDiaUtil) {
                diasUteis++
            }
            data = data.plusDays(1)
        }

        return diasUteis
    }

    /**
     * Determina o feriado prioritário quando há múltiplos na mesma data.
     *
     * Prioridade:
     * 1. Específico do emprego
     * 2. Municipal
     * 3. Estadual
     * 4. Nacional
     * 5. Facultativo
     * 6. Ponte
     */
    private fun determinarFeriadoPrioritario(
        feriados: List<Feriado>,
        empregoId: Long
    ): Feriado? {
        if (feriados.isEmpty()) return null
        if (feriados.size == 1) return feriados.first()

        // Ordenar por prioridade
        return feriados.sortedWith(
            compareBy(
                { if (it.empregoId == empregoId) 0 else 1 },
                {
                    when (it.tipo) {
                        TipoFeriado.MUNICIPAL -> 0
                        TipoFeriado.ESTADUAL -> 1
                        TipoFeriado.NACIONAL -> 2
                        TipoFeriado.FACULTATIVO -> 3
                        TipoFeriado.PONTE -> 4
                    }
                }
            )).first()
    }
}
