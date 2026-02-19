// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/feriado/ConfiguracaoPontesAno.kt
package br.com.tlmacedo.meuponto.domain.model.feriado

import java.time.LocalDate

/**
 * Configuração de feriados ponte para um ano específico de um emprego.
 *
 * Esta entidade armazena o cálculo de como os dias de ponte são
 * distribuídos na jornada diária ao longo do ano.
 *
 * Exemplo:
 * - Ano 2025: 3 dias de ponte = 24h (3 * 8h)
 * - Dias úteis em 2025: 248 dias
 * - Adicional diário: 24h / 248 = ~5.8 min ≈ 10 min (arredondado)
 *
 * @property id Identificador único
 * @property empregoId ID do emprego
 * @property ano Ano de referência
 * @property diasPonte Quantidade de dias de ponte no ano
 * @property cargaHorariaPonteMinutos Total de minutos a compensar (dias * carga diária)
 * @property diasUteisAno Quantidade de dias úteis no ano (excluindo feriados e fins de semana)
 * @property adicionalDiarioMinutos Minutos adicionais por dia útil
 * @property observacao Observação sobre o cálculo
 * @property calculadoEm Data/hora do cálculo
 *
 * @author Thiago
 * @since 3.0.0
 */
data class ConfiguracaoPontesAno(
    val id: Long = 0,
    val empregoId: Long,
    val ano: Int,
    val diasPonte: Int,
    val cargaHorariaPonteMinutos: Int,
    val diasUteisAno: Int,
    val adicionalDiarioMinutos: Int,
    val observacao: String? = null,
    val calculadoEm: java.time.LocalDateTime = java.time.LocalDateTime.now()
) {
    /**
     * Retorna o adicional formatado (ex: "10min").
     */
    val adicionalFormatado: String
        get() = "${adicionalDiarioMinutos}min"

    /**
     * Retorna o total de horas de ponte formatado (ex: "24h").
     */
    val totalPonteFormatado: String
        get() {
            val horas = cargaHorariaPonteMinutos / 60
            val minutos = cargaHorariaPonteMinutos % 60
            return if (minutos > 0) "${horas}h ${minutos}min" else "${horas}h"
        }

    /**
     * Valida se o cálculo está correto.
     * O adicional diário * dias úteis deve ser >= total de ponte.
     */
    fun validarCalculo(): Boolean {
        val totalCompensado = adicionalDiarioMinutos * diasUteisAno
        return totalCompensado >= cargaHorariaPonteMinutos
    }

    /**
     * Calcula a diferença entre o compensado e o necessário.
     * Valor positivo indica que está compensando a mais (margem de segurança).
     */
    val diferencaMinutos: Int
        get() = (adicionalDiarioMinutos * diasUteisAno) - cargaHorariaPonteMinutos

    companion object {
        /**
         * Calcula a configuração de pontes para um ano.
         *
         * @param empregoId ID do emprego
         * @param ano Ano de referência
         * @param diasPonte Quantidade de dias de ponte
         * @param cargaHorariaDiariaMinutos Carga horária diária base (ex: 480 = 8h)
         * @param diasUteisAno Quantidade de dias úteis no ano
         * @return Configuração calculada
         */
        fun calcular(
            empregoId: Long,
            ano: Int,
            diasPonte: Int,
            cargaHorariaDiariaMinutos: Int,
            diasUteisAno: Int
        ): ConfiguracaoPontesAno {
            val totalMinutosPonte = diasPonte * cargaHorariaDiariaMinutos

            // Calcula adicional diário (arredondando para cima para garantir compensação)
            val adicionalDiario = if (diasUteisAno > 0) {
                kotlin.math.ceil(totalMinutosPonte.toDouble() / diasUteisAno).toInt()
            } else {
                0
            }

            return ConfiguracaoPontesAno(
                empregoId = empregoId,
                ano = ano,
                diasPonte = diasPonte,
                cargaHorariaPonteMinutos = totalMinutosPonte,
                diasUteisAno = diasUteisAno,
                adicionalDiarioMinutos = adicionalDiario,
                observacao = "Calculado automaticamente: $diasPonte dias × ${cargaHorariaDiariaMinutos/60}h = ${totalMinutosPonte/60}h / $diasUteisAno dias = ${adicionalDiario}min/dia"
            )
        }
    }
}
