package br.com.tlmacedo.meuponto.domain.repository

import br.com.tlmacedo.meuponto.domain.model.FotoComprovante
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repositório para gestão de fotos de comprovantes.
 *
 * @author Thiago
 * @since 12.0.0
 */
interface FotoComprovanteRepository {
    fun listarPorEmprego(empregoId: Long): Flow<List<FotoComprovante>>
    fun listarPorEmpregoEPeriodo(
        empregoId: Long,
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<FotoComprovante>>

    suspend fun buscarPorId(id: Long): FotoComprovante?
    fun observarPorId(id: Long): Flow<FotoComprovante?>
    suspend fun buscarPorPontoId(pontoId: Long): FotoComprovante?
    suspend fun salvar(foto: FotoComprovante): Long
    suspend fun excluir(id: Long)
    suspend fun excluirPorPontoId(pontoId: Long)
    suspend fun buscarPorHash(hash: String): FotoComprovante?
    suspend fun calcularTamanhoTotal(): Long
    suspend fun listarPathsPorEmprego(empregoId: Long): List<String>
}
