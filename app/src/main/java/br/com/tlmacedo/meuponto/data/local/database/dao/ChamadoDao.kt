// path: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/ChamadoDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.ChamadoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.HistoricoChamadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChamadoDao {

    // ── Chamados ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(chamado: ChamadoEntity): Long

    @Update
    suspend fun atualizar(chamado: ChamadoEntity)

    @Query("SELECT * FROM chamados ORDER BY criadoEm DESC")
    fun observarTodos(): Flow<List<ChamadoEntity>>

    @Query("SELECT * FROM chamados WHERE status IN ('ABERTO', 'EM_ANDAMENTO') ORDER BY criadoEm DESC")
    fun observarAbertos(): Flow<List<ChamadoEntity>>

    @Query("SELECT * FROM chamados WHERE id = :id")
    fun observarPorId(id: Long): Flow<ChamadoEntity?>

    @Query("SELECT * FROM chamados WHERE identificador = :identificador")
    suspend fun buscarPorIdentificador(identificador: String): ChamadoEntity?

    @Query("SELECT * FROM chamados WHERE id = :id")
    suspend fun buscarPorId(id: Long): ChamadoEntity?

    @Query("UPDATE chamados SET status = :status, atualizadoEm = :atualizadoEm WHERE id = :id")
    suspend fun atualizarStatus(id: Long, status: String, atualizadoEm: String)

    @Query("""
        UPDATE chamados 
        SET avaliacaoNota = :nota, avaliacaoComentario = :comentario, 
            avaliadoEm = :avaliadoEm, atualizadoEm = :atualizadoEm 
        WHERE id = :id
    """)
    suspend fun registrarAvaliacao(
        id: Long,
        nota: String,
        comentario: String?,
        avaliadoEm: String,
        atualizadoEm: String
    )

    // ── Histórico ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirHistorico(historico: HistoricoChamadoEntity): Long

    @Query("SELECT * FROM historico_chamados WHERE chamadoId = :chamadoId ORDER BY criadoEm ASC")
    fun observarHistorico(chamadoId: Long): Flow<List<HistoricoChamadoEntity>>

    @Query("SELECT * FROM historico_chamados WHERE chamadoId = :chamadoId ORDER BY criadoEm ASC")
    suspend fun buscarHistorico(chamadoId: Long): List<HistoricoChamadoEntity>
}