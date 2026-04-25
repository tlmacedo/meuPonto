// path: app/src/main/java/br/com/tlmacedo/meuponto/data/local/database/dao/CloudSyncQueueDao.kt
package br.com.tlmacedo.meuponto.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.tlmacedo.meuponto.data.local.database.entity.CloudSyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudSyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(item: CloudSyncQueueEntity): Long

    @Update
    suspend fun atualizar(item: CloudSyncQueueEntity)

    @Delete
    suspend fun excluir(item: CloudSyncQueueEntity)

    @Query("SELECT * FROM cloud_sync_queue ORDER BY criadoEm ASC")
    fun observarTodos(): Flow<List<CloudSyncQueueEntity>>

    @Query("SELECT * FROM cloud_sync_queue ORDER BY criadoEm ASC")
    suspend fun listarTodos(): List<CloudSyncQueueEntity>

    @Query("SELECT * FROM cloud_sync_queue WHERE sincronizadoEm IS NULL ORDER BY criadoEm ASC")
    suspend fun listarPendentes(): List<CloudSyncQueueEntity>

    @Query("SELECT * FROM cloud_sync_queue WHERE id = :id")
    suspend fun buscarPorId(id: Long): CloudSyncQueueEntity?

    @Query("DELETE FROM cloud_sync_queue WHERE sincronizadoEm IS NOT NULL")
    suspend fun limparSincronizados()

    @Query("DELETE FROM cloud_sync_queue")
    suspend fun limparTodos()

    @Query("SELECT COUNT(*) FROM cloud_sync_queue WHERE sincronizadoEm IS NULL")
    fun contarPendentes(): Flow<Int>
}