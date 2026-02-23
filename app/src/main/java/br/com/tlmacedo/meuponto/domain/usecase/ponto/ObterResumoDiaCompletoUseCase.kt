// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ObterResumoDiaCompletoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.model.DiaSemana
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.TipoDiaEspecial
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.TipoFeriado
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.repository.ConfiguracaoEmpregoRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

/**
 * Resultado completo do cálculo de um dia.
 *
 * Esta é a ÚNICA FONTE DE VERDADE para cálculos de um dia.
 * Todos os outros UseCases e ViewModels devem consumir este resultado.
 *
 * FÓRMULAS:
 * - trabalhado = soma dos intervalos (com tolerância aplicada)
 * - abono = soma dos duracaoAbonoMinutos das declarações do dia
 * - jornadaEfetiva = se (zeraJornada) 0 else jornada - abono
 * - saldoDia = trabalhado - jornadaEfetiva
 *
 * @author Thiago
 * @since 6.0.0
 * @updated 6.1.0 - Corrigido bug de feriados não aparecendo na Home
 */
data class ResumoDiaCompleto(
    val data: LocalDate,
    val resumoDia: ResumoDia,
    val ausencias: List<Ausencia>,
    val feriado: Feriado?,
    val feriadosDoDia: List<Feriado> = listOfNotNull(feriado), // Lista completa de feriados
    val horarioDiaSemana: HorarioDiaSemana?,
    val tipoDiaEspecial: TipoDiaEspecial,
    val descricaoDiaEspecial: String?
) {
    // Delegações para facilitar acesso
    val pontos: List<Ponto> get() = resumoDia.pontos
    val horasTrabalhadas: Duration get() = resumoDia.horasTrabalhadas
    val horasTrabalhadasMinutos: Int get() = resumoDia.horasTrabalhadasMinutos
    val tempoAbonadoMinutos: Int get() = resumoDia.tempoAbonadoMinutos
    val cargaHorariaEfetiva: Duration get() = resumoDia.cargaHorariaEfetiva
    val cargaHorariaEfetivaMinutos: Int get() = resumoDia.cargaHorariaEfetivaMinutos
    val saldoDia: Duration get() = resumoDia.saldoDia
    val saldoDiaMinutos: Int get() = resumoDia.saldoDiaMinutos

    // Propriedades auxiliares
    val isDiaEspecial: Boolean get() = tipoDiaEspecial != TipoDiaEspecial.NORMAL
    val zeraJornada: Boolean get() = tipoDiaEspecial.zeraJornada
    val temPontos: Boolean get() = pontos.isNotEmpty()
    val jornadaCompleta: Boolean get() = resumoDia.jornadaCompleta

    // Verifica se há feriados no dia
    val temFeriado: Boolean get() = feriadosDoDia.isNotEmpty()

    // Ausência principal (para exibição)
    val ausenciaPrincipal: Ausencia?
        get() = ausencias.firstOrNull { it.tipo != TipoAusencia.DECLARACAO }
            ?: ausencias.firstOrNull()

    // Declarações do dia (para cálculo de abono)
    val declaracoes: List<Ausencia>
        get() = ausencias.filter { it.tipo == TipoAusencia.DECLARACAO }
}

/**
 * Caso de uso para obter o resumo COMPLETO de um dia.
 *
 * Esta é a ÚNICA FONTE DE VERDADE para cálculos de um dia de trabalho.
 * Centraliza:
 * - Busca de pontos
 * - Busca de ausências (incluindo declarações)
 * - Busca de feriados
 * - Busca da versão de jornada correta
 * - Cálculo do tipo de dia especial
 * - Cálculo do tempo abonado
 * - Montagem do ResumoDia com todos os parâmetros corretos
 *
 * @author Thiago
 * @since 6.0.0
 * @updated 6.1.0 - Unificada busca de feriados usando query otimizada
 */
class ObterResumoDiaCompletoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val ausenciaRepository: AusenciaRepository,
    private val feriadoRepository: FeriadoRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val configuracaoEmpregoRepository: ConfiguracaoEmpregoRepository
) {

    /**
     * Obtém o resumo completo de um dia de forma síncrona.
     */
    suspend operator fun invoke(empregoId: Long, data: LocalDate): ResumoDiaCompleto {
        // 1. Buscar pontos do dia
        val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)

        // 2. Buscar ausências do dia
        val ausencias = ausenciaRepository.buscarPorData(empregoId, data)
            .filter { it.ativo }

        // 3. Buscar feriados do dia - USANDO QUERY OTIMIZADA
        val feriados = feriadoRepository.buscarPorDataEEmprego(data, empregoId)
        val feriado = feriados.firstOrNull()

        // 4. Buscar configuração de jornada
        val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)
        val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)

        val horarioDia = versaoJornada?.let {
            horarioDiaSemanaRepository.buscarPorVersaoEDia(it.id, diaSemana)
        } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        // 5. Determinar tipo de dia especial
        val (tipoDiaEspecial, descricao) = determinarTipoDiaEspecial(ausencias, feriado)

        // 6. Calcular tempo abonado (soma de todas as declarações)
        val tempoAbonadoMinutos = ausencias
            .filter { it.tipo == TipoAusencia.DECLARACAO }
            .sumOf { it.duracaoAbonoMinutos ?: 0 }

        // 7. Montar ResumoDia com todos os parâmetros
        val resumoDia = ResumoDia(
            data = data,
            pontos = pontos.sortedBy { it.dataHora },
            cargaHorariaDiaria = Duration.ofMinutes(
                (horarioDia?.cargaHorariaMinutos
                    ?: configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)?.cargaHorariaDiariaMinutos
                    ?: 480).toLong()
            ),
            intervaloMinimoMinutos = horarioDia?.intervaloMinimoMinutos ?: 60,
            toleranciaIntervaloMinutos = horarioDia?.toleranciaIntervaloMaisMinutos ?: 15,
            tipoDiaEspecial = tipoDiaEspecial,
            saidaIntervaloIdeal = horarioDia?.saidaIntervaloIdeal,
            tempoAbonadoMinutos = tempoAbonadoMinutos
        )

        return ResumoDiaCompleto(
            data = data,
            resumoDia = resumoDia,
            ausencias = ausencias,
            feriado = feriado,
            feriadosDoDia = feriados,
            horarioDiaSemana = horarioDia,
            tipoDiaEspecial = tipoDiaEspecial,
            descricaoDiaEspecial = descricao
        )
    }

    /**
     * Obtém o resumo completo de um dia com dados já carregados.
     * Útil para cálculo em lote (banco de horas) evitando N+1 queries.
     */
    fun invokeComDados(
        data: LocalDate,
        pontos: List<Ponto>,
        ausencias: List<Ausencia>,
        feriado: Feriado?,
        feriados: List<Feriado> = listOfNotNull(feriado),
        horarioDia: HorarioDiaSemana?,
        cargaHorariaPadrao: Int = 480
    ): ResumoDiaCompleto {
        // 1. Determinar tipo de dia especial
        val (tipoDiaEspecial, descricao) = determinarTipoDiaEspecial(ausencias, feriado)

        // 2. Calcular tempo abonado
        val tempoAbonadoMinutos = ausencias
            .filter { it.tipo == TipoAusencia.DECLARACAO }
            .sumOf { it.duracaoAbonoMinutos ?: 0 }

        // 3. Montar ResumoDia
        val resumoDia = ResumoDia(
            data = data,
            pontos = pontos.sortedBy { it.dataHora },
            cargaHorariaDiaria = Duration.ofMinutes(
                (horarioDia?.cargaHorariaMinutos ?: cargaHorariaPadrao).toLong()
            ),
            intervaloMinimoMinutos = horarioDia?.intervaloMinimoMinutos ?: 60,
            toleranciaIntervaloMinutos = horarioDia?.toleranciaIntervaloMaisMinutos ?: 15,
            tipoDiaEspecial = tipoDiaEspecial,
            saidaIntervaloIdeal = horarioDia?.saidaIntervaloIdeal,
            tempoAbonadoMinutos = tempoAbonadoMinutos
        )

        return ResumoDiaCompleto(
            data = data,
            resumoDia = resumoDia,
            ausencias = ausencias,
            feriado = feriado,
            feriadosDoDia = feriados,
            horarioDiaSemana = horarioDia,
            tipoDiaEspecial = tipoDiaEspecial,
            descricaoDiaEspecial = descricao
        )
    }

    /**
     * Observa o resumo completo de um dia (reativo).
     *
     * @updated 6.1.0 - Usando query otimizada para feriados
     */
    fun observar(empregoId: Long, data: LocalDate): Flow<ResumoDiaCompleto> {
        val pontosFlow = pontoRepository.observarPorEmpregoEData(empregoId, data)
        val ausenciasFlow = ausenciaRepository.observarPorData(empregoId, data)
        // CORRIGIDO: Usar query otimizada que filtra no banco
        val feriadosFlow = feriadoRepository.observarPorDataEEmprego(data, empregoId)

        return combine(pontosFlow, ausenciasFlow, feriadosFlow) { pontos, ausencias, feriados ->
            val ausenciasAtivas = ausencias.filter { it.ativo }
            val feriado = feriados.firstOrNull()

            // Buscar configuração de jornada
            val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)
            val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)

            val horarioDia = versaoJornada?.let {
                horarioDiaSemanaRepository.buscarPorVersaoEDia(it.id, diaSemana)
            } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

            val cargaPadrao = configuracaoEmpregoRepository.buscarPorEmpregoId(empregoId)
                ?.cargaHorariaDiariaMinutos ?: 480

            invokeComDados(
                data = data,
                pontos = pontos,
                ausencias = ausenciasAtivas,
                feriado = feriado,
                feriados = feriados,
                horarioDia = horarioDia,
                cargaHorariaPadrao = cargaPadrao
            )
        }
    }

    private fun determinarTipoDiaEspecial(
        ausencias: List<Ausencia>,
        feriado: Feriado?
    ): Pair<TipoDiaEspecial, String?> {
        // Ausência que NÃO é declaração tem prioridade
        val ausenciaPrincipal = ausencias.firstOrNull { it.tipo != TipoAusencia.DECLARACAO }
        if (ausenciaPrincipal != null) {
            val tipoDiaEspecial = ausenciaPrincipal.tipo.toTipoDiaEspecial(ausenciaPrincipal.tipoFolga)
            val descricao = ausenciaPrincipal.tipoDescricaoCompleta +
                    (ausenciaPrincipal.observacao?.let { " - $it" } ?: "")
            return tipoDiaEspecial to descricao
        }

        // Feriado
        if (feriado != null) {
            val tipo = when (feriado.tipo) {
                TipoFeriado.NACIONAL,
                TipoFeriado.ESTADUAL,
                TipoFeriado.MUNICIPAL -> TipoDiaEspecial.FERIADO
                TipoFeriado.PONTE -> TipoDiaEspecial.PONTE
                TipoFeriado.FACULTATIVO -> TipoDiaEspecial.FACULTATIVO
            }
            return tipo to feriado.nome
        }

        return TipoDiaEspecial.NORMAL to null
    }
}
