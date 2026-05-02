// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/ResumoDia.kt
package br.com.tlmacedo.meuponto.domain.model

import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.dia.ClassificacaoDia
import br.com.tlmacedo.meuponto.domain.model.inconsistencia.StatusDiaResumo
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * Modelo que consolida todas as informações de um dia de trabalho.
 *
 * O [ResumoDia] é o objeto central usado pela UI para exibir o estado de um dia
 * no calendário, histórico ou tela principal. Ele contém os registros de ponto,
 * configurações de jornada aplicadas e o saldo calculado.
 *
 * @property data Data do resumo
 * @property entrada Primeiro registro do dia (Entrada)
 * @property saidaAlmoco Segundo registro (Saída para intervalo)
 * @property voltaAlmoco Terceiro registro (Retorno do intervalo)
 * @property saida Quarto registro (Saída definitiva)
 * @property jornadaPrevistaMinutos Carga horária contratual para este dia
 * @property intervaloPrevistoMinutos Tempo de intervalo padrão configurado
 * @property toleranciaIntervaloMinutos Tolerância permitida para o intervalo
 * @property tipoAusencia Tipo de ausência registrado (folga, feriado, atestado, etc)
 * @property saldoBancoMinutosAnterior Saldo acumulado do banco de horas até o dia anterior
 * @property observacao Observação geral do dia
 *
 * @author Thiago
 * @since 3.0.0
 * @updated 12.0.0 - Adicionadas propriedades auxiliares e lógica de intervalo consolidada
 */
data class ResumoDia(
    val data: LocalDate,

    val entrada: LocalTime? = null,
    val saidaAlmoco: LocalTime? = null,
    val voltaAlmoco: LocalTime? = null,
    val saida: LocalTime? = null,

    val jornadaPrevistaMinutos: Int = 0,
    val intervaloPrevistoMinutos: Int = 60,
    val toleranciaIntervaloMinutos: Int = 0,

    /**
     * Substitui o antigo TipoDiaEspecial.
     *
     * null = dia normal
     * TipoAusencia.Folga = dia sem jornada
     * TipoAusencia.Ferias / Feriado.* = descanso
     * TipoAusencia.Atestado / Falta / Declaração / DayOff / DiminuirBanco = ausência
     */
    val tipoAusencia: TipoAusencia? = null,

    val saldoBancoMinutosAnterior: Int = 0,
    val observacao: String? = null
) {

    val intervaloCalculado: IntervaloCalculado?
        get() {
            if (saidaAlmoco == null || voltaAlmoco == null) return null

            return IntervaloCalculado.calcular(
                saida = saidaAlmoco,
                retorno = voltaAlmoco,
                minutosPrevistos = intervaloPrevistoMinutos,
                toleranciaMinutos = toleranciaIntervaloMinutos
            )
        }

    val minutosIntervaloReal: Int
        get() = intervaloCalculado?.minutosReais ?: 0

    val minutosIntervaloConsiderado: Int
        get() = intervaloCalculado?.minutosParaCalculo ?: 0

    val temToleranciaIntervaloAplicada: Boolean
        get() = intervaloCalculado?.toleranciaAplicada == true

    val classificacaoDia: ClassificacaoDia
        get() = tipoAusencia?.classificacaoDia ?: ClassificacaoDia.NORMAL

    val isNormal: Boolean
        get() = tipoAusencia == null

    val isFolga: Boolean
        get() = tipoAusencia?.classificacaoDia == ClassificacaoDia.FOLGA

    val isDescanso: Boolean
        get() = tipoAusencia?.classificacaoDia == ClassificacaoDia.DESCANSO

    val isAusencia: Boolean
        get() = tipoAusencia?.classificacaoDia == ClassificacaoDia.AUSENCIA

    val isFerias: Boolean
        get() = tipoAusencia == TipoAusencia.Ferias

    val isFeriado: Boolean
        get() = tipoAusencia is TipoAusencia.Feriado

    val isAtestado: Boolean
        get() = tipoAusencia == TipoAusencia.Atestado

    val isDeclaracao: Boolean
        get() = tipoAusencia == TipoAusencia.Declaracao

    val isDayOff: Boolean
        get() = tipoAusencia == TipoAusencia.DayOff

    val isDiminuirBanco: Boolean
        get() = tipoAusencia == TipoAusencia.CompensacaoBanco

    val isFaltaJustificada: Boolean
        get() = tipoAusencia == TipoAusencia.Falta.Justificada

    val isFaltaInjustificada: Boolean
        get() = tipoAusencia == TipoAusencia.Falta.Injustificada

    val zeraJornada: Boolean
        get() = tipoAusencia?.zeraJornada == true

    val descricaoTipoDia: String
        get() = tipoAusencia?.descricao ?: "Dia normal"

    val emojiTipoDia: String
        get() = tipoAusencia?.emoji ?: "📅"

    val possuiPontoCompleto: Boolean
        get() = entrada != null &&
                saidaAlmoco != null &&
                voltaAlmoco != null &&
                saida != null

    val possuiAlgumPonto: Boolean
        get() = entrada != null ||
                saidaAlmoco != null ||
                voltaAlmoco != null ||
                saida != null

    val minutosTrabalhadosBrutos: Int
        get() {
            if (entrada == null || saida == null) return 0

            val total = Duration.between(entrada, saida).toMinutes().toInt()

            val intervalo = when {
                saidaAlmoco != null && voltaAlmoco != null -> minutosIntervaloConsiderado
                else -> 0
            }

            return (total - intervalo).coerceAtLeast(0)
        }

    val excedeuToleranciaIntervalo: Boolean
        get() = intervaloCalculado?.excedeuTolerancia == true

    val temIntervaloInvalido: Boolean
        get() {
            if (saidaAlmoco == null || voltaAlmoco == null) return false
            return voltaAlmoco.isBefore(saidaAlmoco)
        }

    val minutosTrabalhadosValidos: Int
        get() {
            return when {
                isFerias -> 0
                isDayOff -> 0
                isFaltaJustificada -> 0
                isAtestado && !possuiAlgumPonto -> 0
                else -> minutosTrabalhadosBrutos
            }
        }

    val jornadaConsideradaMinutos: Int
        get() {
            return when {
                isFolga -> 0
                isDescanso -> 0
                isDayOff -> 0
                isFaltaJustificada -> 0
                isAtestado && !possuiAlgumPonto -> 0
                else -> jornadaPrevistaMinutos
            }
        }

    val saldoDiaMinutos: Int
        get() {
            return when {
                isFolga && possuiAlgumPonto -> minutosTrabalhadosBrutos
                isDescanso && possuiAlgumPonto -> minutosTrabalhadosBrutos
                isFolga -> 0
                isDescanso -> 0
                isDayOff -> 0
                isFaltaJustificada -> 0
                isAtestado && !possuiAlgumPonto -> 0
                isFaltaInjustificada -> -jornadaPrevistaMinutos
                isDiminuirBanco -> calcularSaldoDiminuirBanco()
                else -> minutosTrabalhadosValidos - jornadaConsideradaMinutos
            }
        }

    val saldoBancoMinutosAtual: Int
        get() = saldoBancoMinutosAnterior + saldoDiaMinutos

    val status: StatusDiaResumo
        get() {
            return when {
                isNormal && !possuiAlgumPonto -> StatusDiaResumo.SEM_REGISTRO
                isFolga && !possuiAlgumPonto -> StatusDiaResumo.FOLGA
                isDescanso && !possuiAlgumPonto -> StatusDiaResumo.DESCANSO
                isDayOff -> StatusDiaResumo.ABONADO
                isFaltaJustificada -> StatusDiaResumo.ABONADO
                isAtestado -> StatusDiaResumo.ABONADO
                isFaltaInjustificada -> StatusDiaResumo.FALTA
                saldoDiaMinutos > 0 -> StatusDiaResumo.POSITIVO
                saldoDiaMinutos < 0 -> StatusDiaResumo.NEGATIVO
                else -> StatusDiaResumo.NEUTRO
            }
        }

    val tituloResumo: String
        get() = when {
            isNormal -> "Dia normal"
            else -> descricaoTipoDia
        }

    val subtituloResumo: String
        get() {
            return when {
                isNormal && !possuiAlgumPonto -> "Nenhum ponto registrado"
                isNormal -> "Jornada registrada"
                isFolga -> "Dia sem jornada prevista"
                isFerias -> "Período de férias"
                isFeriado -> "Descanso por feriado"
                isAtestado -> "Ausência por atestado médico"
                isDeclaracao -> "Ausência parcial por declaração"
                isDayOff -> "Day off abonado"
                isDiminuirBanco -> "Compensação com banco de horas"
                isFaltaJustificada -> "Falta justificada"
                isFaltaInjustificada -> "Falta injustificada"
                else -> descricaoTipoDia
            }
        }

    private fun calcularSaldoDiminuirBanco(): Int {
        if (saldoBancoMinutosAnterior <= 0) return 0

        val debitoPossivel = jornadaPrevistaMinutos.coerceAtMost(saldoBancoMinutosAnterior)

        return -debitoPossivel
    }

    val temRegistro: Boolean
        get() = possuiAlgumPonto

    val cargaHorariaDiariaMinutos: Int
        get() = jornadaPrevistaMinutos

    val cargaHorariaEfetivaMinutos: Int
        get() = jornadaConsideradaMinutos

    val horasTrabalhadasMinutos: Int
        get() = minutosTrabalhadosValidos

    val horasTrabalhadasComAndamentoMinutos: Int
        get() = minutosTrabalhadosValidos

    val saldoDiaComAndamentoMinutos: Int
        get() = saldoDiaMinutos

    val isFuturo: Boolean
        get() = data.isAfter(LocalDate.now())

    val isHoje: Boolean
        get() = data == LocalDate.now()

    val isJornadaZerada: Boolean
        get() = jornadaConsideradaMinutos == 0

    val temProblemas: Boolean
        get() = status == StatusDiaResumo.NEGATIVO ||
                status == StatusDiaResumo.FALTA

    val statusDia: StatusDiaResumo
        get() = status

    val horasTrabalhadasFormatadas: String
        get() = formatarMinutosComoHora(minutosTrabalhadosValidos)

    val saldoDiaFormatado: String
        get() {
            val sinal = if (saldoDiaMinutos >= 0) "+" else "-"
            return "$sinal${formatarMinutosComoHora(kotlin.math.abs(saldoDiaMinutos))}"
        }

    val cargaHorariaDiariaFormatada: String
        get() = formatarMinutosComoHora(jornadaPrevistaMinutos)

    val quantidadePontos: Int
        get() = listOfNotNull(
            entrada,
            saidaAlmoco,
            voltaAlmoco,
            saida
        ).size

    val temIntervalo: Boolean
        get() = saidaAlmoco != null && voltaAlmoco != null

    val listaInconsistencias: List<String>
        get() = buildList {
            if (entrada == null && possuiAlgumPonto) {
                add("Registro de entrada ausente.")
            }

            if (saidaAlmoco != null && voltaAlmoco == null) {
                add("Retorno do intervalo ausente.")
            }

            if (saidaAlmoco == null && voltaAlmoco != null) {
                add("Saída para intervalo ausente.")
            }

            if (saida == null && possuiAlgumPonto && !isFuturo) {
                add("Saída ausente.")
            }

            if (temIntervaloInvalido) {
                add("Intervalo com retorno anterior à saída.")
            }
        }
}

private fun formatarMinutosComoHora(totalMinutos: Int): String {
    val horas = totalMinutos / 60
    val minutos = totalMinutos % 60
    return "${horas}h${minutos.toString().padStart(2, '0')}"
}

