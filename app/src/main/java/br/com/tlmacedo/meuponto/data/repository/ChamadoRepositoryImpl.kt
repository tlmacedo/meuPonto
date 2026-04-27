// path: app/src/main/java/br/com/tlmacedo/meuponto/data/repository/ChamadoRepositoryImpl.kt
package br.com.tlmacedo.meuponto.data.repository

import android.content.Context
import android.net.Uri
import br.com.tlmacedo.meuponto.data.local.database.dao.ChamadoDao
import br.com.tlmacedo.meuponto.data.mapper.toDomain
import br.com.tlmacedo.meuponto.data.mapper.toEntity
import br.com.tlmacedo.meuponto.data.remote.api.ChamadoApiService
import br.com.tlmacedo.meuponto.data.remote.dto.chamado.AvaliacaoRequest
import br.com.tlmacedo.meuponto.data.remote.dto.chamado.CriarChamadoRequest
import br.com.tlmacedo.meuponto.domain.model.chamado.AvaliacaoChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.Chamado
import br.com.tlmacedo.meuponto.domain.model.chamado.HistoricoChamado
import br.com.tlmacedo.meuponto.domain.model.chamado.StatusChamado
import br.com.tlmacedo.meuponto.domain.repository.ChamadoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChamadoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chamadoDao: ChamadoDao,
    private val chamadoApiService: ChamadoApiService
) : ChamadoRepository {

    override fun observarTodos(): Flow<List<Chamado>> =
        chamadoDao.observarTodos().map { list -> list.map { it.toDomain() } }

    override fun observarTodosPorUsuario(usuarioEmail: String): Flow<List<Chamado>> =
        chamadoDao.observarTodosPorUsuario(usuarioEmail).map { list -> list.map { it.toDomain() } }

    override fun observarPorId(id: Long): Flow<Chamado?> =
        chamadoDao.observarPorId(id).map { it?.toDomain() }

    override fun observarHistorico(chamadoId: Long): Flow<List<HistoricoChamado>> =
        chamadoDao.observarHistorico(chamadoId).map { list -> list.map { it.toDomain() } }

    override suspend fun criar(chamado: Chamado, anexos: List<Uri>): Result<Chamado> {
        val chamadoComAnexos = chamado.copy(
            anexos = if (anexos.isNotEmpty()) ArrayList(anexos.map { it.toString() }) else null
        )
        return criarChamado(chamadoComAnexos)
    }

    override suspend fun atualizarStatus(
        chamadoId: Long,
        novoStatus: StatusChamado,
        mensagem: String
    ): Result<Chamado> {
        TODO("Not yet implemented")
    }

    override suspend fun salvarAvaliacao(
        chamadoId: Long,
        avaliacao: AvaliacaoChamado
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun sincronizarChamado(identificador: String): Result<Chamado> {
        TODO("Not yet implemented")
    }

    override suspend fun buscarPorId(id: Long): Chamado? =
        chamadoDao.buscarPorId(id)?.toDomain()

    override suspend fun criarChamado(chamado: Chamado): Result<Chamado> {
        return try {
            // Salva localmente primeiro e captura o ID gerado pelo Room
            val localId = chamadoDao.inserir(chamado.toEntity())
            val chamadoComId = chamado.copy(id = localId)

            // Envia para a API
            val request = CriarChamadoRequest(
                titulo = chamadoComId.titulo,
                descricao = chamadoComId.descricao,
                passosParaReproduzir = chamadoComId.passosParaReproduzir,
                deviceInfo = chamadoComId.deviceInfo,
                categoria = chamadoComId.categoria,
                prioridade = chamadoComId.prioridade,
                empregoId = chamadoComId.empregoId,
                usuarioEmail = chamadoComId.usuarioEmail,
                usuarioNome = chamadoComId.usuarioNome
            )

            val response = chamadoApiService.criarChamado(request)
            if (response.isSuccessful) {
                val body = response.body()!!
                // Atualizamos com os dados retornados pela API (ID remoto e Identificador oficial)
                // Se o seu backend usa IDs diferentes para local/remoto, você pode precisar ajustar isso.
                // Aqui estamos assumindo que o identificador CHM-... é o que importa para o usuário.
                val chamadoSincronizado = chamadoComId.copy(
                    identificador = body.identificador
                )
                chamadoDao.atualizar(chamadoSincronizado.toEntity())
                Result.success(chamadoSincronizado)
            } else {
                Timber.w("Falha ao criar chamado na API: ${response.code()}")
                // Retorna o chamado com o ID local mesmo se falhar na API
                Result.success(chamadoComId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao criar chamado")
            Result.failure(e)
        }
    }

    override suspend fun sincronizarStatus(id: String): Result<Chamado> {
        return try {
            val response = chamadoApiService.buscarChamado(id)
            if (response.isSuccessful) {
                val chamadoRemoto = response.body()!!.toDomain()
                chamadoDao.atualizar(chamadoRemoto.toEntity())
                Result.success(chamadoRemoto)
            } else {
                Result.failure(Exception("Erro HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao sincronizar status do chamado $id")
            Result.failure(e)
        }
    }

    override suspend fun avaliarChamado(
        id: String,
        nota: Int,
        comentario: String?
    ): Result<Unit> {
        return try {
            val request = AvaliacaoRequest(nota = nota, comentario = comentario)
            val response = chamadoApiService.avaliarChamado(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao avaliar chamado $id")
            Result.failure(e)
        }
    }

    override fun listarChamadosAbertos(): Flow<List<Chamado>> {
        TODO("Not yet implemented")
    }
}