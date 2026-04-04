package br.com.tlmacedo.meuponto.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

/**
 * Testes unitários para as extensões de minutos e tempo.
 *
 * Garante que a formatação de saldo, duração e descrições permaneça
 * consistente entre as versões Int, Long e Duration.
 */
class MinutosExtensionsTest {

    // ========================================================================
    // TESTES: minutosParaHoraMinuto (Int e Long)
    // = : "00h 00min" (sem sinal)
    // ========================================================================

    @Test
    fun `minutosParaHoraMinuto deve formatar corretamente valores positivos`() {
        assertEquals("00h 00min", 0.minutosParaHoraMinuto())
        assertEquals("01h 30min", 90.minutosParaHoraMinuto())
        assertEquals("01h 30min", 90L.minutosParaHoraMinuto())
        assertEquals("24h 00min", 1440.minutosParaHoraMinuto())
    }

    @Test
    fun `minutosParaHoraMinuto deve formatar valores negativos usando valor absoluto`() {
        assertEquals("01h 30min", (-90).minutosParaHoraMinuto())
        assertEquals("01h 30min", (-90L).minutosParaHoraMinuto())
    }

    // ========================================================================
    // TESTES: minutosParaSaldoFormatado (Int e Long)
    // = : "+00h 00min" ou "-00h 00min" (sinal obrigatório, zero positivo)
    // ========================================================================

    @Test
    fun `minutosParaSaldoFormatado deve retornar sinal positivo para zero`() {
        assertEquals("+00h 00min", 0.minutosParaSaldoFormatado())
        assertEquals("+00h 00min", 0L.minutosParaSaldoFormatado())
    }

    @Test
    fun `minutosParaSaldoFormatado deve formatar corretamente saldos positivos`() {
        assertEquals("+01h 30min", 90.minutosParaSaldoFormatado())
        assertEquals("+01h 30min", 90L.minutosParaSaldoFormatado())
        assertEquals("+10h 05min", 605.minutosParaSaldoFormatado())
    }

    @Test
    fun `minutosParaSaldoFormatado deve formatar corretamente saldos negativos`() {
        assertEquals("-01h 30min", (-90).minutosParaSaldoFormatado())
        assertEquals("-01h 30min", (-90L).minutosParaSaldoFormatado())
        assertEquals("-00h 45min", (-45).minutosParaSaldoFormatado())
    }

    // ========================================================================
    // TESTES: minutosParaDescricao
    // ========================================================================

    @Test
    fun `minutosParaDescricao deve formatar corretamente singular e plural`() {
        assertEquals("0 minutos", 0.minutosParaDescricao())
        assertEquals("1 minuto", 1.minutosParaDescricao())
        assertEquals("2 minutos", 2.minutosParaDescricao())
        assertEquals("1 hora", 60.minutosParaDescricao())
        assertEquals("2 horas", 120.minutosParaDescricao())
    }

    @Test
    fun `minutosParaDescricao deve formatar combinacoes de horas e minutos`() {
        assertEquals("1 hora e 1 minuto", 61.minutosParaDescricao())
        assertEquals("1 hora e 30 minutos", 90.minutosParaDescricao())
        assertEquals("2 horas e 2 minutos", 122.minutosParaDescricao())
    }

    // ========================================================================
    // TESTES: Duration extensions
    // ========================================================================

    @Test
    fun `formatarDuracao deve seguir o padrao sem sinal`() {
        val duration = Duration.ofMinutes(90)
        assertEquals("01h 30min", duration.formatarDuracao())
        
        val negativeDuration = Duration.ofMinutes(-90)
        assertEquals("01h 30min", negativeDuration.formatarDuracao())
    }

    @Test
    fun `formatarSaldo deve seguir o padrao com sinal`() {
        assertEquals("+01h 30min", Duration.ofMinutes(90).formatarSaldo())
        assertEquals("-01h 30min", Duration.ofMinutes(-90).formatarSaldo())
        assertEquals("+00h 00min", Duration.ZERO.formatarSaldo())
    }

    // ========================================================================
    // TESTES: Funcoes utilitarias
    // ========================================================================

    @Test
    fun `horasParaMinutos deve calcular corretamente`() {
        assertEquals(0, horasParaMinutos(0))
        assertEquals(60, horasParaMinutos(1))
        assertEquals(90, horasParaMinutos(1, 30))
        assertEquals(150, horasParaMinutos(2, 30))
    }
}
