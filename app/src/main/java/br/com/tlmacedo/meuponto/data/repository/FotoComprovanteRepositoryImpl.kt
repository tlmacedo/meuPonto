package br.com.tlmacedo.meuponto.data.repository

import br.com.tlmacedo.meuponto.data.local.database.dao.FotoComprovanteDao
import br.com.tlmacedo.meuponto.data.local.database.entity.toDomain
import br.com.tlmacedo.meuponto.data.local.database.entity.toEntity
import br.com.tlmacedo.meuponto.domain.model.FotoComprovante
import br.com.tlmacedo.meuponto.domain.repository.FotoComprovanteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class FotoComprovanteRepositoryImpl @Inject constructor(
    private val fotoComprovanteDao: FotoComprovanteDao
) : FotoComprovanteRepository {

    override fun listarPorEmprego(empregoId: Long): Flow<List<FotoComprovante>> =
        fotoComprovanteDao.listarPorEmprego(empregoId).map { list -> list.map { it.toDomain() } }

    override fun listarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<FotoComprovante>> =
        fotoComprovanteDao.listarPorEmpregoEPeriodo(empregoId, dataInicio, dataFim)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun buscarPorId(id: Long): FotoComprovante? =
        fotoComprovanteDao.buscarPorId(id)?.toDomain()

    override fun observarPorId(id: Long): Flow<FotoComprovante?> =
        fotoComprovanteDao.observarPorId(id).map { it?.toDomain() }

    override suspend fun buscarPorPontoId(pontoId: Long): FotoComprovante? =
        fotoComprovanteDao.buscarPorPontoId(pontoId)?.toDomain()

    override suspend fun salvar(foto: FotoComprovante): Long =
        fotoComprovanteDao.inserir(foto.toEntity())

    override suspend fun excluir(id: Long) =
        fotoComprovanteDao.excluirPorId(id)

    override suspend fun excluirPorPontoId(pontoId: Long) =
        fotoComprovanteDao.excluirPorPontoId(pontoId)

    override suspend fun buscarPorHash(hash: String): FotoComprovante? =
        fotoComprovanteDao.buscarPorHash(hash)?.toDomain()

    override suspend fun calcularTamanhoTotal(): Long =
        fotoComprovanteDao.calcularTamanhoTotal() ?: 0L
}
