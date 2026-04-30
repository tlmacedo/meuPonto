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
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.HorarioDiaSemanaRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.repository.VersaoJornadaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.CalcularMetadataFeriasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.MetadataFerias
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

data class ResumoDiaCompleto(
    val data: LocalDate,
    val resumoDia: ResumoDia,
    val ausencias: List<Ausencia>,
    val feriado: Feriado?,
    val feriadosDoDia: List<Feriado> = listOfNotNull(feriado),
    val horarioDiaSemana: HorarioDiaSemana?,
    val tipoDiaEspecial: TipoDiaEspecial,
    val descricaoDiaEspecial: String?,
    val metadataFerias: MetadataFerias? = null
) {
    val pontos: List<Ponto> get() = resumoDia.pontos
    val horasTrabalhadas: Duration get() = resumoDia.horasTrabalhadas
    val horasTrabalhadasMinutos: Int get() = resumoDia.horasTrabalhadasMinutos
    val tempoAbonadoMinutos: Int get() = resumoDia.tempoAbonadoMinutos
    val cargaHorariaEfetiva: Duration get() = resumoDia.cargaHorariaEfetiva
    val cargaHorariaEfetivaMinutos: Int get() = resumoDia.cargaHorariaEfetivaMinutos
    val saldoDia: Duration get() = resumoDia.saldoDia
    val saldoDiaMinutos: Int get() = resumoDia.saldoDiaMinutos

    @Suppress("unused")
    val isDiaEspecial: Boolean get() = tipoDiaEspecial != TipoDiaEspecial.Normal

    @Suppress("unused")
    val zeraJornada: Boolean get() = tipoDiaEspecial.zeraJornada

    val temPontos: Boolean get() = pontos.isNotEmpty()
    val jornadaCompleta: Boolean get() = resumoDia.jornadaCompleta
    val temFeriado: Boolean get() = feriadosDoDia.isNotEmpty()

    val ausenciaPrincipal: Ausencia?
        get() = ausencias.firstOrNull { it.tipo != TipoAusencia.Declaracao }
            ?: ausencias.firstOrNull()

    val declaracoes: List<Ausencia>
        get() = ausencias.filter { it.tipo == TipoAusencia.Declaracao }

    val isFuturo: Boolean get() = resumoDia.isFuturo

    @Suppress("unused")
    val isHoje: Boolean get() = resumoDia.isHoje

    val temProblemas: Boolean get() = resumoDia.temProblemas
    val isDescanso: Boolean get() = tipoDiaEspecial.isDescanso
    val isFerias: Boolean get() = tipoDiaEspecial.isFerias
    val isAtestado: Boolean get() = tipoDiaEspecial.isAtestado
    val isFolga: Boolean get() = tipoDiaEspecial.isFolga || tipoDiaEspecial.isDayOff
    val isDayOff: Boolean get() = tipoDiaEspecial.isDayOff
    val isFaltaJustificada: Boolean get() = tipoDiaEspecial.isFaltaJustificada
    val isFaltaInjustificada: Boolean get() = tipoDiaEspecial.isFaltaInjustificada
    val totalMinutosDeclaracoes: Int get() = declaracoes.sumOf { it.duracaoAbonoMinutos ?: 0 }

    val temToleranciaIntervaloAplicada: Boolean
        get() = resumoDia.temToleranciaIntervaloAplicada

    val minutosToleranciaIntervalo: Int
        get() = if (temToleranciaIntervaloAplicada) {
            abs(resumoDia.minutosIntervaloReal - resumoDia.minutosIntervaloTotal)
        } else {
            0
        }
}

class ObterResumoDiaCompletoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val ausenciaRepository: AusenciaRepository,
    private val feriadoRepository: FeriadoRepository,
    private val horarioDiaSemanaRepository: HorarioDiaSemanaRepository,
    private val versaoJornadaRepository: VersaoJornadaRepository,
    private val calcularMetadataFeriasUseCase: CalcularMetadataFeriasUseCase
) {

    suspend operator fun invoke(empregoId: Long, data: LocalDate): ResumoDiaCompleto {
        val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        val ausencias = ausenciaRepository.buscarPorData(empregoId, data).filter { it.ativo }
        val feriados = feriadoRepository.buscarPorDataEEmprego(data, empregoId)
        val feriado = feriados.firstOrNull()

        val diaSemana = DiaSemana.fromJavaDayOfWeek(data.dayOfWeek)
        val versaoJornada = versaoJornadaRepository.buscarPorEmpregoEData(empregoId, data)

        val horarioDia = versaoJornada?.let { versao ->
            horarioDiaSemanaRepository.buscarPorVersaoEDia(versao.id, diaSemana)
        } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

        val cargaBasePadrao = versaoJornada?.cargaHorariaDiariaMinutos ?: 480
        val acrescimoPontes = versaoJornada?.acrescimoMinutosDiasPontes ?: 0
        val toleranciaGlobal = versaoJornada?.toleranciaIntervaloMaisMinutos ?: 0

        val ausenciaPrincipal = ausencias.firstOrNull { it.tipo != TipoAusencia.Declaracao }
            ?: ausencias.firstOrNull()

        val metadataFerias = ausenciaPrincipal?.let { ausencia ->
            calcularMetadataFeriasUseCase(ausencia, data)
        }

        return invokeComDados(
            data = data,
            pontos = pontos,
            ausencias = ausencias,
            feriado = feriado,
            feriados = feriados,
            horarioDia = horarioDia,
            cargaHorariaBasePadrao = cargaBasePadrao,
            acrescimoPontes = acrescimoPontes,
            toleranciaIntervaloGlobal = toleranciaGlobal,
            metadataFerias = metadataFerias
        )
    }

    fun invokeComDados(
        data: LocalDate,
        pontos: List<Ponto>,
        ausencias: List<Ausencia>,
        feriado: Feriado?,
        feriados: List<Feriado> = listOfNotNull(feriado),
        horarioDia: HorarioDiaSemana?,
        cargaHorariaBasePadrao: Int = 480,
        acrescimoPontes: Int = 0,
        toleranciaIntervaloGlobal: Int = 0,
        metadataFerias: MetadataFerias? = null
    ): ResumoDiaCompleto {
        val resultadoDiaEspecial = determinarTipoDiaEspecial(ausencias, feriado)
        val tipoDiaEspecial = resultadoDiaEspecial.tipo
        val descricaoDiaEspecial = resultadoDiaEspecial.descricao

        val tempoAbonadoMinutos = ausencias
            .filter { it.tipo == TipoAusencia.Declaracao }
            .sumOf { it.duracaoAbonoMinutos ?: 0 }

        val cargaHorariaBase = horarioDia?.cargaHorariaMinutos ?: cargaHorariaBasePadrao
        val cargaHorariaEfetiva = if (cargaHorariaBase > 0) {
            cargaHorariaBase + acrescimoPontes
        } else {
            0
        }

        val resumoDia = ResumoDia(
            data = data,
            pontos = pontos.sortedBy { it.dataHora },
            cargaHorariaDiaria = Duration.ofMinutes(cargaHorariaEfetiva.toLong()),
            intervaloMinimoMinutos = horarioDia?.intervaloMinimoMinutos ?: 60,
            toleranciaIntervaloMinutos = toleranciaIntervaloGlobal,
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
            descricaoDiaEspecial = descricaoDiaEspecial,
            metadataFerias = metadataFerias
        )
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

            val horarioDia = versaoJornada?.let { versao ->
                horarioDiaSemanaRepository.buscarPorVersaoEDia(versao.id, diaSemana)
            } ?: horarioDiaSemanaRepository.buscarPorEmpregoEDia(empregoId, diaSemana)

            val ausenciaPrincipal =
                ausenciasAtivas.firstOrNull { it.tipo != TipoAusencia.Declaracao }
                    ?: ausenciasAtivas.firstOrNull()

            val metadataFerias = ausenciaPrincipal?.let { ausencia ->
                calcularMetadataFeriasUseCase(ausencia, data)
            }

            invokeComDados(
                data = data,
                pontos = pontos,
                ausencias = ausenciasAtivas,
                feriado = feriado,
                feriados = feriados,
                horarioDia = horarioDia,
                cargaHorariaBasePadrao = versaoJornada?.cargaHorariaDiariaMinutos ?: 480,
                acrescimoPontes = versaoJornada?.acrescimoMinutosDiasPontes ?: 0,
                toleranciaIntervaloGlobal = versaoJornada?.toleranciaIntervaloMaisMinutos ?: 0,
                metadataFerias = metadataFerias
            )
        }
    }

    private fun determinarTipoDiaEspecial(
        ausencias: List<Ausencia>,
        feriado: Feriado?
    ): ResultadoDiaEspecial {
        val ausenciaPrincipal = ausencias.firstOrNull { it.tipo != TipoAusencia.Declaracao }

        if (ausenciaPrincipal != null) {
            return ResultadoDiaEspecial(
                tipo = ausenciaPrincipal.tipo.toTipoDiaEspecial(),
                descricao = montarDescricaoAusencia(ausenciaPrincipal)
            )
        }

        if (feriado != null) {
            return ResultadoDiaEspecial(
                tipo = feriado.tipo.toTipoDiaEspecial(),
                descricao = feriado.nome
            )
        }

        return ResultadoDiaEspecial(
            tipo = TipoDiaEspecial.Normal,
            descricao = null
        )
    }

    private fun montarDescricaoAusencia(ausencia: Ausencia): String {
        return buildString {
            append(ausencia.tipoDescricaoCompleta)

            ausencia.observacao
                ?.takeIf { it.isNotBlank() }
                ?.let { observacao ->
                    append(" - ")
                    append(observacao)
                }
        }
    }

    private data class ResultadoDiaEspecial(
        val tipo: TipoDiaEspecial,
        val descricao: String?
    )
}