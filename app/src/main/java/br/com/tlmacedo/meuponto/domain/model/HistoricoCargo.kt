package br.com.tlmacedo.meuponto.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo de domínio que representa o histórico de cargos e salários em um emprego.
 */
data class HistoricoCargo(
    val id: Long = 0,
    val empregoId: Long,
    val funcao: String,
    val salarioInicial: Double,
    val dataInicio: LocalDate,
    val dataFim: LocalDate? = null,
    val ajustes: List<AjusteSalarial> = emptyList(),
    val criadoEm: LocalDateTime = LocalDateTime.now(),
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
)

/**
 * Modelo que representa um ajuste ou dissídio salarial.
 */
data class AjusteSalarial(
    val id: Long = 0,
    val historicoCargoId: Long,
    val dataAjuste: LocalDate,
    val novoSalario: Double,
    val observacao: String? = null
)
