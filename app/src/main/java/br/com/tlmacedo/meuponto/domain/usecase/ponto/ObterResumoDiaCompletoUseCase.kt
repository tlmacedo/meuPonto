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
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

data class ResumoDiaCompleto(
    val data: LocalDate,
    val resumoDia: ResumoDia,
    val ausencias: List<Ausencia>,
    val feriado: Feriado?,
    val feriadosDoDia: List<Feriado> = listOfNotNull(feriado),
    val horarioDiaSemana: HorarioDiaSemana?,
    val tipoDiaEspecial: TipoDiaEspecial,
    val descricaoDiaEspecial: String?
) {
    val pontos: List<Ponto> get() = resumoDia.pontos
    val horasTrabalhadas: Duration get() = resumoDia.horasTrabalhadas
    val horasTrabalhadasMinutos: Int get() = resumoDia.horasTrabalhadasMinutos
    val tempoAbonadoMinutos: Int get() = resumoDia.tempoAbonadoMinutos
    val cargaHorariaEfetiva: Duration get() = resumoDia.cargaHorariaEfetiva
    val cargaHorariaEfetivaMinutos: Int get() = resumoDia.cargaHorariaEfetivaMinutos
    val saldoDia: Duration get() = resumoDia.saldoDia
    val saldoDiaMinutos: Int get() = resumoDia.saldoDiaMinutos
    val isDiaEspecial: Boolean get() = tipoDiaEspecial != TipoDiaEspecial.NORMAL
    val zeraJornada: Boolean get() = tipoDiaEspecial.zeraJornada
    val temPontos: Boolean get() = pontos.isNotEmpty()
    val jornadaCompleta: Boolean get() = resumoDia.jornadaCompleta
    val temFeriado: Boolean get() = feriadosDoDia.isNotEmpty()
    val ausenciaPrincipal: Ausencia?
        get() = ausencias.firstOrNull { it.tipo != TipoAusencia.DECLARACAO } ?: ausencias.firstOrNull()
    val declaracoes: List<Ausencia> get() = ausencias.filter { it.tipo == TipoAusencia.DECLARACAO }
}

/**
 * @author Thiago
 * @since 6.0.0
 * @updated 8.0.0 - Migrado para usar VersaoJornada
 */
class ObterResumoDiaCompletoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val ausenciaRepository: AusenciaRepository,
    private val feriadoRepository: FeriadoRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository
) {

    suspend operator fun invoke(empregoId: Long, data: LocalDate): ResumoDiaCompleto {
        val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        val ausencias = ausenciaRepository.buscarPorData(empregoId, data).filter { it.ativo }
        val feriados = feriadoRepository.buscarPorDataEEmprego(data, empregoId)
        val feriado = feriados.firstOrNull()

        val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)
        val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)

        val horarioDia = versaoJornada?.let {
            horarioDiaSemanaRepository.buscarPorVersaoEDia(it.id, diaSemana)
        } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        val (tipoDiaEspecial, descricao) = determinarTipoDiaEspecial(ausencias, feriado)
        val tempoAbonadoMinutos = ausencias
            .filter { it.tipo == TipoAusencia.DECLARACAO }
            .sumOf { it.duracaoAbonoMinutos ?: 0 }

        // Buscar carga da versão de jornada
        val cargaHoraria = horarioDia?.cargaHorariaMinutos
            ?: versaoJornada?.cargaHorariaDiariaMinutos
            ?: 480

        val resumoDia = ResumoDia(
            data = data,
            pontos = pontos.sortedBy { it.dataHora },
            cargaHorariaDiaria = Duration.ofMinutes(cargaHoraria.toLong()),
            intervaloMinimoMinutos = horarioDia?.intervaloMinimoMinutos ?: 60,
            toleranciaIntervaloMinutos = horarioDia?.toleranciaIntervaloMaisMinutos ?: 15,
            tipoDiaEspecial = tipoDiaEspecial,
            saidaIntervaloIdeal = horarioDia?.saidaIntervaloIdeal,
            tempoAbonadoMinutos = tempoAbonadoMinutos
        )

        return ResumoDiaCompleto(data, resumoDia, ausencias, feriado, feriados, horarioDia, tipoDiaEspecial, descricao)
    }

    fun invokeComDados(
        data: LocalDate,
        pontos: List<Ponto>,
        ausencias: List<Ausencia>,
        feriado: Feriado?,
        feriados: List<Feriado> = listOfNotNull(feriado),
        horarioDia: HorarioDiaSemana?,
        cargaHorariaPadrao: Int = 480
    ): ResumoDiaCompleto {
        val (tipoDiaEspecial, descricao) = determinarTipoDiaEspecial(ausencias, feriado)
        val tempoAbonadoMinutos = ausencias
            .filter { it.tipo == TipoAusencia.DECLARACAO }
            .sumOf { it.duracaoAbonoMinutos ?: 0 }

        val resumoDia = ResumoDia(
            data = data,
            pontos = pontos.sortedBy { it.dataHora },
            cargaHorariaDiaria = Duration.ofMinutes((horarioDia?.cargaHorariaMinutos ?: cargaHorariaPadrao).toLong()),
            intervaloMinimoMinutos = horarioDia?.intervaloMinimoMinutos ?: 60,
            toleranciaIntervaloMinutos = horarioDia?.toleranciaIntervaloMaisMinutos ?: 15,
            tipoDiaEspecial = tipoDiaEspecial,
            saidaIntervaloIdeal = horarioDia?.saidaIntervaloIdeal,
            tempoAbonadoMinutos = tempoAbonadoMinutos
        )

        return ResumoDiaCompleto(data, resumoDia, ausencias, feriado, feriados, horarioDia, tipoDiaEspecial, descricao)
    }

    fun observar(empregoId: Long, data: LocalDate): Flow<ResumoDiaCompleto> {
        val pontosFlow = pontoRepository.observarPorEmpregoEData(empregoId, data)
        val ausenciasFlow = ausenciaRepository.observarPorData(empregoId, data)
        val feriadosFlow = feriadoRepository.observarPorDataEEmprego(data, empregoId)

        return combine(pontosFlow, ausenciasFlow, feriadosFlow) { pontos, ausencias, feriados ->
            val ausenciasAtivas = ausencias.filter { it.ativo }
            val feriado = feriados.firstOrNull()

            val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)
            val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)

            val horarioDia = versaoJornada?.let {
                horarioDiaSemanaRepository.buscarPorVersaoEDia(it.id, diaSemana)
            } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

            val cargaPadrao = versaoJornada?.cargaHorariaDiariaMinutos ?: 480

            invokeComDados(data, pontos, ausenciasAtivas, feriado, feriados, horarioDia, cargaPadrao)
        }
    }

    private fun determinarTipoDiaEspecial(ausencias: List<Ausencia>, feriado: Feriado?): Pair<TipoDiaEspecial, String?> {
        val ausenciaPrincipal = ausencias.firstOrNull { it.tipo != TipoAusencia.DECLARACAO }
        if (ausenciaPrincipal != null) {
            val tipoDiaEspecial = ausenciaPrincipal.tipo.toTipoDiaEspecial(ausenciaPrincipal.tipoFolga)
            val descricao = ausenciaPrincipal.tipoDescricaoCompleta + (ausenciaPrincipal.observacao?.let { " - $it" } ?: "")
            return tipoDiaEspecial to descricao
        }

        if (feriado != null) {
            val tipo = when (feriado.tipo) {
                TipoFeriado.NACIONAL, TipoFeriado.ESTADUAL, TipoFeriado.MUNICIPAL -> TipoDiaEspecial.FERIADO
                TipoFeriado.PONTE -> TipoDiaEspecial.PONTE
                TipoFeriado.FACULTATIVO -> TipoDiaEspecial.FACULTATIVO
            }
            return tipo to feriado.nome
        }

        return TipoDiaEspecial.NORMAL to null
    }
}
