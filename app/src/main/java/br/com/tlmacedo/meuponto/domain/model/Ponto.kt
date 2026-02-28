// Arquivo: Ponto.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Modelo de domínio que representa um registro de ponto.
 *
 * O tipo do ponto (ENTRADA/SAÍDA) NÃO é armazenado.
 * É calculado em runtime baseado na posição na lista ordenada por dataHora:
 * - Índice par (0, 2, 4...) = ENTRADA
 * - Índice ímpar (1, 3, 5...) = SAÍDA
 *
 * Use as funções de extensão para determinar o tipo:
 * - ponto.isEntrada(indice)
 * - ponto.isSaida(indice)
 * - ponto.getTipoDescricao(indice)
 *
 * @author Thiago
 * @since 1.0.0
 * @updated 2.1.0 - Removido campo tipo (calculado em runtime por posição)
 */
data class Ponto(
    val id: Long = 0,
    val empregoId: Long = 1,
    val dataHora: LocalDateTime,
    val isEditadoManualmente: Boolean = false,
    val observacao: String? = null,
    val nsr: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val endereco: String? = null,
    val marcadorId: Long? = null,
    val justificativaInconsistencia: String? = null,
    val horaConsiderada: LocalDateTime? = null,
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
) {
    val data: LocalDate get() = dataHora.toLocalDate()
    val hora: LocalTime get() = dataHora.toLocalTime()
    val horaFormatada: String get() = String.format("%02d:%02d", hora.hour, hora.minute)
    val horaEfetiva: LocalTime get() = horaConsiderada?.toLocalTime() ?: hora
    val horaEfetivaFormatada: String get() = String.format("%02d:%02d", horaEfetiva.hour, horaEfetiva.minute)
    val temAjusteTolerancia: Boolean get() = horaConsiderada != null && horaConsiderada != dataHora
    val temLocalizacao: Boolean get() = latitude != null && longitude != null
    val temInconsistencia: Boolean get() = !justificativaInconsistencia.isNullOrBlank()
}

// ============================================================================
// Funções de Extensão para Tipo de Ponto (calculado em runtime)
// ============================================================================

/** Verifica se é ENTRADA baseado no índice (par = entrada) */
fun Ponto.isEntrada(indice: Int): Boolean = indice % 2 == 0

/** Verifica se é SAÍDA baseado no índice (ímpar = saída) */
fun Ponto.isSaida(indice: Int): Boolean = indice % 2 == 1

/** Retorna descrição do tipo baseado no índice */
fun Ponto.getTipoDescricao(indice: Int): String = if (isEntrada(indice)) "Entrada" else "Saída"

