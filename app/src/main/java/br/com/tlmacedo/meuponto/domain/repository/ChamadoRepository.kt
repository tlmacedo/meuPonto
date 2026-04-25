// path: app/src/main/java/br/com/tlmacedo/meuponto/domain/repository/ChamadoRepository.kt
package br.com.tlmacedo.meuponto.domain.repository

import android.net.Uri
import br.com.tlmacedo.meuponto.domain.model.chamado.AvaliacaoChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.HistoricoChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import kotlinx.coroutines.flow.Flow

interface ChamadoRepository {
    fun observarTodos(): Flow<List<Chamado>>
    fun observarPorId(id: Long): Flow<Chamado?>
    fun observarHistorico(chamadoId: Long): Flow<List<HistoricoChamado>>
    suspend fun criar(chamado: Chamado, anexos: List<Uri>): Result<Chamado>
    suspend fun atualizarStatus(
        chamadoId: Long,
        novoStatus: StatusChamado,
        mensagem: String
    ): Result<Chamado>

    suspend fun salvarAvaliacao(chamadoId: Long, avaliacao: AvaliacaoChamado): Result<Unit>
    suspend fun sincronizarChamado(identificador: String): Result<Chamado>
    suspend fun avaliarChamado(id: String, nota: Int, comentario: String?): Result<Unit>
    fun listarChamadosAbertos(): Flow<List<Chamado>>
    suspend fun criarChamado(chamado: Chamado): Result<Chamado>
    suspend fun buscarPorId(id: Long): Chamado?
    suspend fun sincronizarStatus(id: String): Result<Chamado>
}