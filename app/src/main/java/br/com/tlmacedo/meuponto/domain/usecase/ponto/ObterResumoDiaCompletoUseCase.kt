// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/usecase/ponto/ObterResumoDiaCompletoUseCase.kt
package br.com.tlmacedo.meuponto.domain.usecase.ponto

import br.com.tlmacedo.meuponto.domain.extensions.isAtestadoOrFalse
import br.com.tlmacedo.meuponto.domain.extensions.isDayOffOrFalse
import br.com.tlmacedo.meuponto.domain.extensions.isDescansoOrFalse
import br.com.tlmacedo.meuponto.domain.extensions.isFaltaInjustificadaOrFalse
import br.com.tlmacedo.meuponto.domain.extensions.isFaltaJustificadaOrFalse
import br.com.tlmacedo.meuponto.domain.extensions.isFeriasOrFalse
import br.com.tlmacedo.meuponto.domain.extensions.isFolgaOrFalse
import br.com.tlmacedo.meuponto.domain.extensions.prioridade
import br.com.tlmacedo.meuponto.domain.extensions.zeraJornadaOrFalse
import br.com.tlmacedo.meuponto.domain.mapper.toIntervalosPonto
import br.com.tlmacedo.meuponto.domain.model.ContextoJornadaDia
import br.com.tlmacedo.meuponto.domain.model.HorarioDiaSemana
import br.com.tlmacedo.meuponto.domain.model.IntervaloPonto
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.ResumoDia
import br.com.tlmacedo.meuponto.domain.model.StatusResumoDia
import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.feriado.Feriado
import br.com.tlmacedo.meuponto.domain.model.feriado.toTipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.repository.FeriadoRepository
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ferias.CalcularMetadataFeriasUseCase
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ferias.MetadataFerias
import br.com.tlmacedo.meuponto.domain.usecase.jornada.ObterContextoJornadaDiaUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

data class ResumoDiaCompleto(
    val data: LocalDate,
    val resumoDia: ResumoDia,
    val pontos: List<Ponto>,
    val ausencias: List<Ausencia>,
    val feriado: Feriado?,
    val feriadosDoDia: List<Feriado> = listOfNotNull(feriado),
    val horarioDiaSemana: HorarioDiaSemana?,
    val tipoAusencia: TipoAusencia?,
    val descricaoDiaEspecial: String?,
    val contextoJornadaDia: ContextoJornadaDia? = null,
    val metadataFerias: MetadataFerias? = null
) {
    val intervalos: List<IntervaloPonto>
        get() = pontos.toIntervalosPonto(
            intervaloMinimoMinutos = resumoDia.intervaloPrevistoMinutos,
            toleranciaVoltaIntervaloMinutos = resumoDia.toleranciaIntervaloMinutos,
            saidaIntervaloIdeal = contextoJornadaDia?.saidaIntervaloIdeal
                ?: horarioDiaSemana?.saidaIntervaloIdeal
        )

    val temIntervalo: Boolean
        get() = intervalos.any { it.temPausaAntes }

    val horasTrabalhadasMinutos: Int
        get() = intervalos.sumOf { it.duracaoTurnoMinutos }

    val horasTrabalhadas: Duration
        get() = Duration.ofMinutes(horasTrabalhadasMinutos.toLong())

    val tempoAbonadoMinutos: Int
        get() = totalMinutosDeclaracoes

    val cargaHorariaEfetivaMinutos: Int
        get() = resumoDia.jornadaConsideradaMinutos

    val cargaHorariaEfetiva: Duration
        get() = Duration.ofMinutes(cargaHorariaEfetivaMinutos.toLong())

    val saldoDiaMinutos: Int
        get() = horasTrabalhadasMinutos + tempoAbonadoMinutos - cargaHorariaEfetivaMinutos

    val saldoDia: Duration
        get() = Duration.ofMinutes(saldoDiaMinutos.toLong())

    val isDiaEspecial: Boolean
        get() = tipoAusencia != null || temFeriado

    val zeraJornada: Boolean
        get() = tipoAusencia.zeraJornadaOrFalse

    val temPontos: Boolean
        get() = pontos.isNotEmpty()

    val jornadaCompleta: Boolean
        get() = pontos.size >= 4

    val temFeriado: Boolean
        get() = feriadosDoDia.isNotEmpty()

    val ausenciaPrincipal: Ausencia?
        get() = ausencias
            .filter { it.tipo != TipoAusencia.Declaracao }
            .maxByOrNull { it.tipo.prioridade }
            ?: ausencias.maxByOrNull { it.tipo.prioridade }

    val declaracoes: List<Ausencia>
        get() = ausencias.filter { it.tipo == TipoAusencia.Declaracao }

    val totalMinutosDeclaracoes: Int
        get() = declaracoes.sumOf { it.duracaoAbonoMinutos ?: 0 }

    val isFuturo: Boolean
        get() = data.isAfter(LocalDate.now())

    val isHoje: Boolean
        get() = data == LocalDate.now()

    val temProblemas: Boolean
        get() = resumoDia.status in setOf(
            StatusResumoDia.NEGATIVO,
            StatusResumoDia.FALTA
        )

    val isDescanso: Boolean
        get() = tipoAusencia.isDescansoOrFalse

    val isFerias: Boolean
        get() = tipoAusencia.isFeriasOrFalse

    val isAtestado: Boolean
        get() = tipoAusencia.isAtestadoOrFalse

    val isFolga: Boolean
        get() = tipoAusencia.isFolgaOrFalse || tipoAusencia.isDayOffOrFalse

    val isDayOff: Boolean
        get() = tipoAusencia.isDayOffOrFalse

    val isFaltaJustificada: Boolean
        get() = tipoAusencia.isFaltaJustificadaOrFalse

    val isFaltaInjustificada: Boolean
        get() = tipoAusencia.isFaltaInjustificadaOrFalse

    val temToleranciaIntervaloAplicada: Boolean
        get() = intervalos.any { it.toleranciaAplicada }

    val minutosToleranciaIntervalo: Int
        get() = intervalos.sumOf { intervalo ->
            if (intervalo.toleranciaAplicada) {
                ((intervalo.pausaAntesMinutosReal ?: 0) -
                        (intervalo.pausaAntesMinutosConsiderada ?: 0))
                    .coerceAtLeast(0)
            } else {
                0
            }
        }
}

class ObterResumoDiaCompletoUseCase @Inject constructor(
    private val pontoRepository: PontoRepository,
    private val ausenciaRepository: AusenciaRepository,
    private val feriadoRepository: FeriadoRepository,
    private val calcularMetadataFeriasUseCase: CalcularMetadataFeriasUseCase,
    private val obterContextoJornadaDiaUseCase: ObterContextoJornadaDiaUseCase
) {

    suspend operator fun invoke(
        empregoId: Long,
        data: LocalDate
    ): ResumoDiaCompleto {
        val pontos = pontoRepository.buscarPorEmpregoEData(empregoId, data)
        val ausencias = ausenciaRepository
            .buscarPorData(empregoId, data)
            .filter { it.ativo }

        val feriados = feriadoRepository.buscarPorDataEEmprego(data, empregoId)

        val contexto = obterContextoOuFalhar(
            empregoId = empregoId,
            data = data
        )

        val metadataFerias = ausencias
            .firstOrNull { it.tipo == TipoAusencia.Ferias }
            ?.let { calcularMetadataFeriasUseCase(it, data) }

        return invokeComDados(
            data = data,
            pontos = pontos,
            ausencias = ausencias,
            feriados = feriados,
            contextoJornadaDia = contexto,
            metadataFerias = metadataFerias
        )
    }

    fun invokeComDados(
        data: LocalDate,
        pontos: List<Ponto>,
        ausencias: List<Ausencia>,
        feriado: Feriado? = null,
        feriados: List<Feriado> = listOfNotNull(feriado),

        /**
         * Compatibilidade temporária com chamadas antigas.
         *
         * CalcularBancoHorasUseCase e HistoryViewModel ainda chamam este método
         * passando horário/jornada manualmente. Vamos manter por enquanto para
         * destravar o build.
         */
        horarioDia: HorarioDiaSemana? = null,
        cargaHorariaBasePadrao: Int = 480,
        acrescimoPontes: Int = 0,
        toleranciaIntervaloGlobal: Int = 0,

        /**
         * Caminho novo e preferencial.
         *
         * Quando existir contextoJornadaDia, ele tem prioridade absoluta.
         */
        contextoJornadaDia: ContextoJornadaDia? = null,

        metadataFerias: MetadataFerias? = null
    ): ResumoDiaCompleto {
        val pontosOrdenados = pontos.sortedBy { it.dataHora }
        val ausenciasAtivas = ausencias.filter { it.ativo }
        val feriadosDoDia = feriados.ifEmpty { listOfNotNull(feriado) }

        val resultadoDia = determinarTipoDia(
            ausencias = ausenciasAtivas,
            feriados = feriadosDoDia
        )

        val jornadaDoDiaMinutos = contextoJornadaDia?.jornadaDoDiaMinutos
            ?: run {
                val cargaBase = horarioDia?.cargaHorariaMinutos ?: cargaHorariaBasePadrao

                if (cargaBase > 0) {
                    cargaBase + acrescimoPontes
                } else {
                    0
                }
            }

        val intervaloMinimoMinutos = contextoJornadaDia?.intervaloMinimoMinutos
            ?: horarioDia?.intervaloMinimoMinutos
            ?: 60

        val toleranciaVoltaIntervaloMinutos =
            contextoJornadaDia?.toleranciaVoltaIntervaloMinutos
                ?: toleranciaIntervaloGlobal

        val resumoDia = ResumoDia(
            data = data,
            entrada = pontosOrdenados.getOrNull(0)?.hora,
            saidaAlmoco = pontosOrdenados.getOrNull(1)?.hora,
            voltaAlmoco = pontosOrdenados.getOrNull(2)?.hora,
            saida = pontosOrdenados.getOrNull(3)?.hora,
            jornadaPrevistaMinutos = jornadaDoDiaMinutos,
            intervaloPrevistoMinutos = intervaloMinimoMinutos,
            toleranciaIntervaloMinutos = toleranciaVoltaIntervaloMinutos,
            tipoAusencia = resultadoDia.tipoAusencia
        )

        return ResumoDiaCompleto(
            data = data,
            resumoDia = resumoDia,
            pontos = pontosOrdenados,
            ausencias = ausenciasAtivas,
            feriado = feriadosDoDia.firstOrNull(),
            feriadosDoDia = feriadosDoDia,
            horarioDiaSemana = contextoJornadaDia?.horarioDiaSemana ?: horarioDia,
            tipoAusencia = resultadoDia.tipoAusencia,
            descricaoDiaEspecial = resultadoDia.descricao,
            contextoJornadaDia = contextoJornadaDia,
            metadataFerias = metadataFerias
        )
    }
    fun observar(
        empregoId: Long,
        data: LocalDate
    ): Flow<ResumoDiaCompleto> {
        val pontosFlow = pontoRepository.observarPorEmpregoEData(empregoId, data)
        val ausenciasFlow = ausenciaRepository.observarPorData(empregoId, data)
        val feriadosFlow = feriadoRepository.observarPorDataEEmprego(data, empregoId)

        return combine(
            pontosFlow,
            ausenciasFlow,
            feriadosFlow
        ) { pontos, ausencias, feriados ->
            val ausenciasAtivas = ausencias.filter { it.ativo }

            val contexto = obterContextoOuFalhar(
                empregoId = empregoId,
                data = data
            )

            val metadataFerias = ausenciasAtivas
                .firstOrNull { it.tipo == TipoAusencia.Ferias }
                ?.let { calcularMetadataFeriasUseCase(it, data) }

            invokeComDados(
                data = data,
                pontos = pontos,
                ausencias = ausenciasAtivas,
                feriados = feriados,
                contextoJornadaDia = contexto,
                metadataFerias = metadataFerias
            )
        }
    }

    private suspend fun obterContextoOuFalhar(
        empregoId: Long,
        data: LocalDate
    ): ContextoJornadaDia {
        return when (val resultado = obterContextoJornadaDiaUseCase(empregoId, data)) {
            is ObterContextoJornadaDiaUseCase.Resultado.Sucesso -> {
                resultado.contexto
            }

            is ObterContextoJornadaDiaUseCase.Resultado.EmpregoNaoEncontrado -> {
                throw IllegalStateException(
                    "Emprego não encontrado para id=${resultado.empregoId}."
                )
            }

            is ObterContextoJornadaDiaUseCase.Resultado.ConfiguracaoNaoEncontrada -> {
                throw IllegalStateException(
                    "Configuração não encontrada para empregoId=${resultado.empregoId}."
                )
            }

            is ObterContextoJornadaDiaUseCase.Resultado.VersaoNaoEncontrada -> {
                throw IllegalStateException(
                    "Versão de jornada não encontrada para empregoId=${resultado.empregoId} em ${resultado.data}."
                )
            }

            is ObterContextoJornadaDiaUseCase.Resultado.Erro -> {
                throw IllegalStateException(resultado.mensagem)
            }
        }
    }

    private fun determinarTipoDia(
        ausencias: List<Ausencia>,
        feriados: List<Feriado>
    ): ResultadoTipoDia {
        val ausenciaPrioritaria = ausencias
            .filter { it.tipo != TipoAusencia.Declaracao }
            .maxByOrNull { it.tipo.prioridade }
            ?: ausencias.maxByOrNull { it.tipo.prioridade }

        if (ausenciaPrioritaria != null) {
            return ResultadoTipoDia(
                tipoAusencia = ausenciaPrioritaria.tipo,
                descricao = montarDescricaoAusencia(ausenciaPrioritaria)
            )
        }

        val feriado = feriados.firstOrNull()

        if (feriado != null) {
            return ResultadoTipoDia(
                tipoAusencia = feriado.tipo.toTipoAusencia(),
                descricao = feriado.nome
            )
        }

        return ResultadoTipoDia(
            tipoAusencia = null,
            descricao = null
        )
    }

    private fun montarDescricaoAusencia(
        ausencia: Ausencia
    ): String {
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

    private data class ResultadoTipoDia(
        val tipoAusencia: TipoAusencia?,
        val descricao: String?
    )
}