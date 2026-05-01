// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/extensions/AusenciaRegras.kt
package br.com.tlmacedo.meuponto.domain.extensions

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoFolga
import kotlin.math.max
import kotlin.math.min

/**
 * Define como uma ausência impacta a jornada, o ponto e o banco de horas.
 *
 * Esta camada concentra a regra de negócio para evitar if/when espalhado.
 */
enum class ComportamentoAusencia {
    /**
     * Jornada 0, ponto permitido.
     * Se trabalhar, tempo vira saldo positivo.
     *
     * Ex:
     * - Folga semanal
     * - Feriado
     * - Dia ponte
     * - Facultativo
     */
    JORNADA_ZERO_PONTO_PERMITIDO,

    /**
     * Jornada 0, ponto bloqueado, banco não altera.
     *
     * Ex:
     * - Férias
     * - Falta justificada
     * - Day off
     */
    JORNADA_ZERO_PONTO_BLOQUEADO,

    /**
     * Jornada normal, ponto permitido, abono parcial.
     *
     * Ex:
     * - Declaração
     */
    JORNADA_NORMAL_ABONO_PARCIAL,

    /**
     * Jornada normal, ponto pode ter ocorrido antes,
     * mas o restante é abonado.
     *
     * Ex:
     * - Atestado
     */
    JORNADA_NORMAL_ABONA_RESTANTE,

    /**
     * Jornada normal, ponto bloqueado, abate a jornada inteira.
     *
     * Ex:
     * - Falta injustificada
     * - Compensação / diminuir banco
     */
    JORNADA_NORMAL_DEBITA_INTEGRAL
}

/**
 * Resultado consolidado das regras de ausência para um dia.
 */
data class RegraAusenciaDia(
    val comportamento: ComportamentoAusencia,
    val documentoObrigatorio: Boolean,
    val permiteAnexo: Boolean,
    val permiteNovoRegistroPonto: Boolean,
    val descricaoRegra: String
) {
    val jornadaZero: Boolean
        get() = comportamento == ComportamentoAusencia.JORNADA_ZERO_PONTO_PERMITIDO ||
                comportamento == ComportamentoAusencia.JORNADA_ZERO_PONTO_BLOQUEADO

    val usaJornadaNormal: Boolean
        get() = !jornadaZero

    val abonaParcial: Boolean
        get() = comportamento == ComportamentoAusencia.JORNADA_NORMAL_ABONO_PARCIAL

    val abonaRestante: Boolean
        get() = comportamento == ComportamentoAusencia.JORNADA_NORMAL_ABONA_RESTANTE

    val debitaIntegral: Boolean
        get() = comportamento == ComportamentoAusencia.JORNADA_NORMAL_DEBITA_INTEGRAL

    val trabalhoViraExtra: Boolean
        get() = comportamento == ComportamentoAusencia.JORNADA_ZERO_PONTO_PERMITIDO
}

/**
 * Regra principal baseada na ausência completa.
 *
 * Importante:
 * TipoFolga.DAY_OFF e TipoFolga.COMPENSACAO alteram o comportamento da ausência
 * quando o tipo permitir subtipo de folga/compensação.
 */
val Ausencia.regraNegocio: RegraAusenciaDia
    get() {
        return when (tipo) {
            TipoAusencia.Folga -> {
                when (tipoFolga) {
                    TipoFolga.DAY_OFF -> RegraAusenciaDia(
                        comportamento = ComportamentoAusencia.JORNADA_ZERO_PONTO_BLOQUEADO,
                        documentoObrigatorio = false,
                        permiteAnexo = true,
                        permiteNovoRegistroPonto = false,
                        descricaoRegra = "Day off: jornada zerada, ponto bloqueado e banco não altera."
                    )

                    TipoFolga.COMPENSACAO -> RegraAusenciaDia(
                        comportamento = ComportamentoAusencia.JORNADA_NORMAL_DEBITA_INTEGRAL,
                        documentoObrigatorio = false,
                        permiteAnexo = true,
                        permiteNovoRegistroPonto = false,
                        descricaoRegra = "Compensação: usa banco de horas e abate a jornada do dia."
                    )

                    null -> RegraAusenciaDia(
                        comportamento = ComportamentoAusencia.JORNADA_ZERO_PONTO_PERMITIDO,
                        documentoObrigatorio = false,
                        permiteAnexo = true,
                        permiteNovoRegistroPonto = true,
                        descricaoRegra = "Folga: jornada zerada; se houver trabalho, vira saldo positivo."
                    )
                }
            }

            TipoAusencia.Ferias -> RegraAusenciaDia(
                comportamento = ComportamentoAusencia.JORNADA_ZERO_PONTO_BLOQUEADO,
                documentoObrigatorio = false,
                permiteAnexo = true,
                permiteNovoRegistroPonto = false,
                descricaoRegra = "Férias: jornada zerada, ponto bloqueado e banco não altera."
            )

            TipoAusencia.Feriado.Oficial,
            TipoAusencia.Feriado.DiaPonte,
            TipoAusencia.Feriado.Facultativo -> RegraAusenciaDia(
                comportamento = ComportamentoAusencia.JORNADA_ZERO_PONTO_PERMITIDO,
                documentoObrigatorio = false,
                permiteAnexo = true,
                permiteNovoRegistroPonto = true,
                descricaoRegra = "Descanso/feriado: jornada zerada; se houver trabalho, vira saldo positivo."
            )

            TipoAusencia.Atestado -> RegraAusenciaDia(
                comportamento = ComportamentoAusencia.JORNADA_NORMAL_ABONA_RESTANTE,
                documentoObrigatorio = true,
                permiteAnexo = true,
                permiteNovoRegistroPonto = false,
                descricaoRegra = "Atestado: documento obrigatório; abona o restante da jornada."
            )

            TipoAusencia.Declaracao -> RegraAusenciaDia(
                comportamento = ComportamentoAusencia.JORNADA_NORMAL_ABONO_PARCIAL,
                documentoObrigatorio = true,
                permiteAnexo = true,
                permiteNovoRegistroPonto = true,
                descricaoRegra = "Declaração: documento obrigatório; soma tempo considerado ao cálculo do saldo."
            )

            TipoAusencia.Falta.Justificada -> RegraAusenciaDia(
                comportamento = ComportamentoAusencia.JORNADA_ZERO_PONTO_BLOQUEADO,
                documentoObrigatorio = true,
                permiteAnexo = true,
                permiteNovoRegistroPonto = false,
                descricaoRegra = "Falta justificada: documento obrigatório, jornada zerada e banco não altera."
            )

            TipoAusencia.Falta.Injustificada -> RegraAusenciaDia(
                comportamento = ComportamentoAusencia.JORNADA_NORMAL_DEBITA_INTEGRAL,
                documentoObrigatorio = false,
                permiteAnexo = true,
                permiteNovoRegistroPonto = false,
                descricaoRegra = "Falta injustificada: abate a jornada inteira do banco."
            )

            TipoAusencia.DayOff -> RegraAusenciaDia(
                comportamento = ComportamentoAusencia.JORNADA_ZERO_PONTO_BLOQUEADO,
                documentoObrigatorio = false,
                permiteAnexo = true,
                permiteNovoRegistroPonto = false,
                descricaoRegra = "Day off: jornada zerada, ponto bloqueado e banco não altera."
            )

            TipoAusencia.CompensacaoBanco -> RegraAusenciaDia(
                comportamento = ComportamentoAusencia.JORNADA_NORMAL_DEBITA_INTEGRAL,
                documentoObrigatorio = false,
                permiteAnexo = true,
                permiteNovoRegistroPonto = false,
                descricaoRegra = "Compensação/diminuir banco: mantém jornada normal, bloqueia ponto e abate a jornada inteira do banco."
            )
        }
    }

/**
 * Documento obrigatório por ausência.
 */
val Ausencia.documentoObrigatorio: Boolean
    get() = regraNegocio.documentoObrigatorio

/**
 * Todas as ausências criadas pelo usuário podem ter anexo.
 */
val Ausencia.permiteAnexo: Boolean
    get() = regraNegocio.permiteAnexo

/**
 * Indica se, com essa ausência ativa no dia, ainda pode haver novo registro de ponto.
 */
val Ausencia.permiteNovoRegistroPonto: Boolean
    get() = regraNegocio.permiteNovoRegistroPonto

/**
 * Indica se a ausência deve ser usada como tipo principal do dia.
 *
 * Declaração não deve classificar o dia como ausência principal.
 * Ela é detalhe parcial do dia.
 */
val Ausencia.entraComoTipoPrincipalDoDia: Boolean
    get() = tipo != TipoAusencia.Declaracao

/**
 * Jornada considerada pela ausência.
 *
 * Regras:
 * - Jornada zero: retorna 0
 * - Jornada normal: mantém jornada do dia
 */
fun Ausencia.calcularJornadaConsideradaMinutos(
    jornadaDoDiaMinutos: Int
): Int {
    return if (regraNegocio.jornadaZero) {
        0
    } else {
        jornadaDoDiaMinutos
    }
}

/**
 * Calcula tempo abonado da ausência para o saldo do dia.
 *
 * Regras:
 * - Declaração: usa duracaoAbonoMinutos, limitada ao tempo real da declaração e ao faltante da jornada.
 * - Atestado: abona o restante para completar a jornada.
 * - Demais tipos: 0.
 */
fun Ausencia.calcularTempoAbonadoMinutos(
    jornadaDoDiaMinutos: Int,
    minutosTrabalhados: Int
): Int {
    val regra = regraNegocio

    return when {
        regra.abonaParcial -> {
            val tempoRealDeclaracao = duracaoDeclaracaoMinutos ?: 0
            val tempoConsideradoInformado = duracaoAbonoMinutos ?: 0
            val tempoFaltanteParaJornada = (jornadaDoDiaMinutos - minutosTrabalhados)
                .coerceAtLeast(0)

            min(
                tempoConsideradoInformado,
                min(tempoRealDeclaracao, tempoFaltanteParaJornada)
            ).coerceAtLeast(0)
        }

        regra.abonaRestante -> {
            max(jornadaDoDiaMinutos - minutosTrabalhados, 0)
        }

        else -> 0
    }
}

/**
 * Calcula saldo do dia quando a ausência possui comportamento de débito integral.
 *
 * Ex:
 * - Falta injustificada
 * - Diminuir banco / compensação
 */
fun Ausencia.calcularDebitoIntegralMinutos(
    jornadaDoDiaMinutos: Int
): Int {
    return if (regraNegocio.debitaIntegral) {
        -jornadaDoDiaMinutos
    } else {
        0
    }
}