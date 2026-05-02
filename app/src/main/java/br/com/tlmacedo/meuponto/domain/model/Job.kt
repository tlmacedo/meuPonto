// Arquivo: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/Job.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo de domínio que representa um emprego/trabalho do usuário.
 *
 * @property id Identificador único do emprego
 * @property name Nome do emprego
 * @property startDate Data de início no trabalho (para férias/benefícios)
 * @property description Descrição opcional
 * @property active Indica se o emprego está ativo
 * @property archived Indica se o emprego foi arquivado
 * @property order Ordem de exibição
 * @property createdAt Timestamp de criação
 * @property updatedAt Timestamp da última atualização
 *
 * @author Thiago
 * @since 1.0.0
 */
data class Job(
    val id: Long = 0,
    val name: String,
    val startDate: LocalDate? = null,
    val description: String? = null,
    val active: Boolean = true,
    val archived: Boolean = false,
    val order: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /** Verifica se o emprego está visível na UI principal */
    val isVisible: Boolean get() = active && !archived

    /** Verifica se é permitido registrar ponto para este emprego */
    val canRegisterClockIn: Boolean get() = active && !archived
}
