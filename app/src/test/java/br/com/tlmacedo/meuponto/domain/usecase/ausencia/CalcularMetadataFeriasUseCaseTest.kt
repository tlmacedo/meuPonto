package br.com.tlmacedo.meuponto.domain.usecase.ausencia

import br.com.tlmacedo.meuponto.domain.model.ausencia.Ausencia
import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia
import br.com.tlmacedo.meuponto.domain.repository.AusenciaRepository
import br.com.tlmacedo.meuponto.domain.usecase.ausencia.ferias.CalcularMetadataFeriasUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CalcularMetadataFeriasUseCaseTest {

    private lateinit var useCase: CalcularMetadataFeriasUseCase
    private val repository: AusenciaRepository = mockk()

    @Before
    fun setup() {
        useCase = CalcularMetadataFeriasUseCase(repository)
    }

    @Test
    fun `deve retornar null se a ausencia nao for de ferias`() = runBlocking {
        val ausencia = Ausencia(
            id = 1,
            empregoId = 1,
            tipo = TipoAusencia.ATESTADO,
            dataInicio = LocalDate.now(),
            dataFim = LocalDate.now()
        )

        val resultado = useCase(ausencia)

        assertNull(resultado)
    }

    @Test
    fun `deve calcular sequencia e dias restantes corretamente para o primeiro periodo`() = runBlocking {
        val inicioPA = LocalDate.of(2023, 1, 1)
        val fimPA = LocalDate.of(2023, 12, 31)
        
        val ausencia = Ausencia(
            id = 1,
            empregoId = 1,
            tipo = TipoAusencia.FERIAS,
            dataInicio = LocalDate.of(2024, 1, 10),
            dataFim = LocalDate.of(2024, 1, 19), // 10 dias
            dataInicioPeriodoAquisitivo = inicioPA,
            dataFimPeriodoAquisitivo = fimPA
        )

        coEvery { 
            repository.buscarFeriasPorPeriodoAquisitivo(1, inicioPA, fimPA) 
        } returns listOf(ausencia)

        val resultado = useCase(ausencia)

        assertEquals(1, resultado?.sequenciaPeriodo)
        assertEquals(20, resultado?.diasRestantes)
    }

    @Test
    fun `deve calcular sequencia e dias restantes corretamente para o segundo periodo`() = runBlocking {
        val inicioPA = LocalDate.of(2023, 1, 1)
        val fimPA = LocalDate.of(2023, 12, 31)
        
        val ferias1 = Ausencia(
            id = 1,
            empregoId = 1,
            tipo = TipoAusencia.FERIAS,
            dataInicio = LocalDate.of(2024, 1, 10),
            dataFim = LocalDate.of(2024, 1, 19), // 10 dias
            dataInicioPeriodoAquisitivo = inicioPA,
            dataFimPeriodoAquisitivo = fimPA
        )

        val ferias2 = Ausencia(
            id = 2,
            empregoId = 1,
            tipo = TipoAusencia.FERIAS,
            dataInicio = LocalDate.of(2024, 6, 1),
            dataFim = LocalDate.of(2024, 6, 15), // 15 dias
            dataInicioPeriodoAquisitivo = inicioPA,
            dataFimPeriodoAquisitivo = fimPA
        )

        coEvery { 
            repository.buscarFeriasPorPeriodoAquisitivo(1, inicioPA, fimPA) 
        } returns listOf(ferias1, ferias2)

        val resultado = useCase(ferias2)

        assertEquals(2, resultado?.sequenciaPeriodo)
        assertEquals(5, resultado?.diasRestantes)
    }
}
