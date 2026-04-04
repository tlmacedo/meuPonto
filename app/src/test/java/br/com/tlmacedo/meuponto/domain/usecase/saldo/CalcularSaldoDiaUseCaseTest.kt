package br.com.tlmacedo.meuponto.domain.usecase.saldo

import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.repository.PontoRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CalcularSaldoDiaUseCaseTest {

    private lateinit var repository: PontoRepository
    private lateinit var useCase: CalcularSaldoDiaUseCase
    private val dataTeste = LocalDate.of(2024, 5, 13) // Segunda-feira (Dia Útil)
    private val dataFimDeSemana = LocalDate.of(2024, 5, 12) // Domingo (Não Útil)

    @Before
    fun setup() {
        repository = mockk()
        useCase = CalcularSaldoDiaUseCase(repository)
    }

    private fun criarPonto(hora: String, horaConsiderada: String? = null): Ponto {
        val h = LocalTime.parse(hora)
        val hc = horaConsiderada?.let { LocalTime.parse(it) } ?: h
        return Ponto(
            empregoId = 1L,
            dataHora = LocalDateTime.of(dataTeste, h),
            horaConsiderada = hc
        )
    }

    @Test
    fun `deve calcular saldo positivo quando trabalha mais que o esperado`() {
        val pontos = listOf(
            criarPonto("08:00"),
            criarPonto("12:00"),
            criarPonto("13:00"),
            criarPonto("18:00")
        )
        // 4h + 5h = 9h (540 min). Esperado 8h (480 min). Saldo +1h (60 min).

        val resultado = useCase.calcularComPontos(pontos, 480L)

        assertEquals(540L, resultado.trabalhadoMinutos)
        assertEquals(480L, resultado.esperadoMinutos)
        assertEquals(60L, resultado.saldoMinutos)
        assertEquals("+01h 00min", resultado.saldoFormatado)
        assertTrue(resultado.isDiaUtil)
    }

    @Test
    fun `deve calcular saldo negativo quando trabalha menos que o esperado`() {
        val pontos = listOf(
            criarPonto("08:00"),
            criarPonto("12:00"),
            criarPonto("13:00"),
            criarPonto("16:00")
        )
        // 4h + 3h = 7h (420 min). Esperado 8h (480 min). Saldo -1h (-60 min).

        val resultado = useCase.calcularComPontos(pontos, 480L)

        assertEquals(420L, resultado.trabalhadoMinutos)
        assertEquals(-60L, resultado.saldoMinutos)
        assertEquals("-01h 00min", resultado.saldoFormatado)
    }

    @Test
    fun `deve calcular intervalo corretamente`() {
        val pontos = listOf(
            criarPonto("08:00"),
            criarPonto("12:00"),
            criarPonto("13:30"),
            criarPonto("18:00")
        )
        // Intervalo 12:00 ate 13:30 = 90 min

        val resultado = useCase.calcularComPontos(pontos, 480L)

        assertEquals(90L, resultado.intervaloRealMinutos)
        assertEquals("01h 30min", resultado.intervaloRealFormatado)
    }

    @Test
    fun `deve considerar tolerancias nos calculos`() {
        val pontos = listOf(
            criarPonto("07:55", "08:00"), // Entrada real 07:55, considerada 08:00
            criarPonto("12:05", "12:00"), // Saida real 12:05, considerada 12:00
            criarPonto("12:55", "13:00"),
            criarPonto("18:05", "18:00")
        )
        // Trabalhado considerado: 08:00-12:00 (4h) + 13:00-18:00 (5h) = 9h
        // Intervalo real: 12:05-12:55 (50 min)
        // Intervalo considerado: 12:00-13:00 (60 min)

        val resultado = useCase.calcularComPontos(pontos, 480L)

        assertEquals(540L, resultado.trabalhadoMinutos)
        assertEquals(50L, resultado.intervaloRealMinutos)
        assertEquals(60L, resultado.intervaloConsideradoMinutos)
        assertTrue(resultado.temAjusteTolerancia)
    }

    @Test
    fun `deve zerar esperado em fins de semana`() {
        val pontosSabado = listOf(
            Ponto(
                empregoId = 1L,
                dataHora = LocalDateTime.of(dataFimDeSemana, LocalTime.of(8, 0)),
                horaConsiderada = LocalTime.of(8, 0)
            ),
            Ponto(
                empregoId = 1L,
                dataHora = LocalDateTime.of(dataFimDeSemana, LocalTime.of(12, 0)),
                horaConsiderada = LocalTime.of(12, 0)
            )
        )

        val resultado = useCase.calcularComPontos(pontosSabado, 480L)

        assertEquals(240L, resultado.trabalhadoMinutos)
        assertEquals(0L, resultado.esperadoMinutos)
        assertEquals(240L, resultado.saldoMinutos)
        assertFalse(resultado.isDiaUtil)
    }

    @Test
    fun `deve retornar zero quando lista de pontos esta incompleta`() {
        val pontos = listOf(criarPonto("08:00"))

        val resultado = useCase.calcularComPontos(pontos, 480L)

        assertEquals(0L, resultado.trabalhadoMinutos)
        assertEquals(0L, resultado.intervaloRealMinutos)
    }
}
