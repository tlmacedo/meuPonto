// File: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/Job.kt
package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Domain model representing a user's job/work.
 *
 * @property id Unique identifier for the job
 * @property name Job name
 * @property startDate Work start date (for vacations/benefits)
 * @property description Optional description
 * @property active Indicates if the job is active
 * @property archived Indicates if the job was archived
 * @property order Display order
 * @property createdAt Creation timestamp
 * @property updatedAt Last update timestamp
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
    val isVisible: Boolean get() = active && !archived
    val canRegisterClockIn: Boolean get() = active && !archived
}
