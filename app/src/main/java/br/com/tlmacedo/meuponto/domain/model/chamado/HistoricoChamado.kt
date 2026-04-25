// path: app/src/main/java/br/com/tlmacedo/meuponto/domain/model/chamado/HistoricoChamado.kt
package br.com.tlmacedo.meuponto.domain.model.chamado

import java.time.LocalDateTime

data class HistoricoChamado(
    val id: Long = 0,
    val chamadoId: Long,
    val statusAnterior: StatusChamado?,
    val statusNovo: StatusChamado,
    val mensagem: String,
    val autor: String,              // "Sistema" ou email do agente
    val criadoEm: LocalDateTime
)
