// path: app/src/main/java/br/com/tlmacedo/meuponto/domain/service/EmailNotificacaoService.kt
package br.com.tlmacedo.meuponto.domain.service

import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.HistoricoChamado

interface EmailNotificacaoService {
    suspend fun notificarNovoChamado(chamado: Chamado)
    suspend fun notificarMudancaStatus(chamado: Chamado, historico: HistoricoChamado): Result<Unit>
    suspend fun notificarChamadoResolvido(chamado: Chamado): Result<Unit>
}