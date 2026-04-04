package br.com.tlmacedo.meuponto.data.local.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.tlmacedo.meuponto.data.local.database.MeuPontoDatabase
import br.com.tlmacedo.meuponto.data.local.database.entity.EmpregoEntity
import br.com.tlmacedo.meuponto.data.local.database.entity.PontoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class PontoDaoTest {

    private lateinit var database: MeuPontoDatabase
    private lateinit var pontoDao: PontoDao
    private lateinit var empregoDao: EmpregoDao
    private var empregoId: Long = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MeuPontoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        pontoDao = database.pontoDao()
        empregoDao = database.empregoDao()

        // Criar um emprego para associar aos pontos
        val emprego = EmpregoEntity(nome = "Emprego Teste")
        empregoId = empregoDao.inserir(emprego)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun inserirEBuscarPonto() = runBlocking {
        val agora = LocalDateTime.now()
        val ponto = PontoEntity(
            empregoId = empregoId,
            dataHora = agora,
            data = agora.toLocalDate(),
            hora = agora.toLocalTime(),
            horaConsiderada = agora.toLocalTime()
        )

        val id = pontoDao.inserir(ponto)
        val buscado = pontoDao.buscarPorId(id)

        assertNotNull(buscado)
        assertEquals(empregoId, buscado?.empregoId)
        assertEquals(agora.toLocalDate(), buscado?.data)
    }

    @Test
    fun buscarPorEmpregoEData() = runBlocking {
        val data = LocalDate.of(2024, 5, 13)
        val ponto1 = PontoEntity(
            empregoId = empregoId,
            dataHora = LocalDateTime.of(data, LocalTime.of(8, 0)),
            data = data,
            hora = LocalTime.of(8, 0),
            horaConsiderada = LocalTime.of(8, 0)
        )
        val ponto2 = PontoEntity(
            empregoId = empregoId,
            dataHora = LocalDateTime.of(data, LocalTime.of(12, 0)),
            data = data,
            hora = LocalTime.of(12, 0),
            horaConsiderada = LocalTime.of(12, 0)
        )

        pontoDao.inserir(ponto1)
        pontoDao.inserir(ponto2)

        val pontos = pontoDao.buscarPorEmpregoEData(empregoId, data)

        assertEquals(2, pontos.size)
        assertEquals(LocalTime.of(8, 0), pontos[0].hora)
        assertEquals(LocalTime.of(12, 0), pontos[1].hora)
    }

    @Test
    fun softDeleteERestaurar() = runBlocking {
        val agora = LocalDateTime.now()
        val ponto = PontoEntity(
            empregoId = empregoId,
            dataHora = agora,
            data = agora.toLocalDate(),
            hora = agora.toLocalTime(),
            horaConsiderada = agora.toLocalTime()
        )

        val id = pontoDao.inserir(ponto)
        
        // Soft delete
        pontoDao.softDelete(id, System.currentTimeMillis())
        
        val buscadoAtivo = pontoDao.buscarPorId(id)
        assertNull(buscadoAtivo)

        val deletados = pontoDao.listarDeletados()
        assertEquals(1, deletados.size)
        assertTrue(deletados[0].isDeleted)

        // Restaurar
        pontoDao.restaurar(id, System.currentTimeMillis())
        
        val restaurado = pontoDao.buscarPorId(id)
        assertNotNull(restaurado)
        assertEquals(false, restaurado?.isDeleted)
    }

    @Test
    fun observarPontosPorEmprego() = runBlocking {
        val agora = LocalDateTime.now()
        val ponto = PontoEntity(
            empregoId = empregoId,
            dataHora = agora,
            data = agora.toLocalDate(),
            hora = agora.toLocalTime(),
            horaConsiderada = agora.toLocalTime()
        )

        pontoDao.inserir(ponto)

        val lista = pontoDao.observarPorEmprego(empregoId).first()
        assertEquals(1, lista.size)
    }
}
