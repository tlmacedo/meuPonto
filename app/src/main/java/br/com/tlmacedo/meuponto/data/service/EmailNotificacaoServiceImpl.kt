// path: app/src/main/java/br/com/tlmacedo/meuponto/data/service/EmailNotificacaoServiceImpl.kt
package br.com.tlmacedo.meuponto.data.service

import br.com.tlmacedo.meuponto.data.remote.api.ChamadoApiService
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.HistoricoChamado
import br.com.tlmacedo.meuponto.domain.service.EmailNotificacaoService
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmailNotificacaoServiceImpl @Inject constructor(chamadoApiService: ChamadoApiService) :
    EmailNotificacaoService {

    override suspend fun notificarNovoChamado(chamado: Chamado) {
        try {
            val request = chamado.toNotificacaoRequest()
            Timber.d("Notificação de novo chamado enviada: ${request.identificador}")
            // TODO: integrar com serviço de e-mail real (ex: SendGrid, Mailgun)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao notificar novo chamado: ${chamado.identificador}")
        }
    }

    override suspend fun notificarMudancaStatus(
        chamado: Chamado,
        historico: HistoricoChamado
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun notificarChamadoResolvido(chamado: Chamado): Result<Unit> {
        TODO("Not yet implemented")
    }

    // Extensão local para montar o payload de notificação
    private fun Chamado.toNotificacaoRequest(): NotificacaoChamadoRequest =
        NotificacaoChamadoRequest(
            identificador = identificador,
            titulo = titulo,
            descricao = descricao,
            categoria = categoria.name,
            prioridade = prioridade.name,
            usuarioEmail = usuarioEmail,
            usuarioNome = usuarioNome
        )
}

private data class NotificacaoChamadoRequest(
    val identificador: String,
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val prioridade: String,
    val usuarioEmail: String,
    val usuarioNome: String
)