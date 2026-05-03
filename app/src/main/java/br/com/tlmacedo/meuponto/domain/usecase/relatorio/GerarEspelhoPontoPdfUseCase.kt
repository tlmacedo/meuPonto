package br.com.tlmacedo.meuponto.domain.usecase.relatorio

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.domain.usecase.ponto.ResumoDiaCompleto
import br.com.tlmacedo.meuponto.util.helper.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.helper.minutosParaSaldoFormatado
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/**
 * Caso de uso para gerar o PDF do Espelho de Ponto Mensal.
 * Utiliza o framework nativo do Android (android.graphics.pdf).
 *
 * @author Thiago
 * @since 14.0.0
 * @updated 14.2.0 - Motivos de ausência movidos para a coluna de registros e nova coluna de Saldo Banco.
 */
class GerarEspelhoPontoPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val localePtBR = Locale("pt", "BR")

    fun execute(
        relatorio: GerarRelatorioMensalUseCase.RelatorioMensal,
        emprego: Emprego,
        outputStream: OutputStream
    ): Result<Unit> {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            
            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 7f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 40f

            // Cabeçalho do Relatório
            canvas.drawText("ESPELHO DE PONTO MENSAL", 200f, y, titlePaint)
            y += 25f
            
            canvas.drawText("Empresa: ${emprego.nome}", 30f, y, textPaint.apply { textSize = 10f; isFakeBoldText = true })
            y += 15f
            canvas.drawText("Período: ${relatorio.dataInicio.format(dateFormatter)} a ${relatorio.dataFim.format(dateFormatter)}", 30f, y, textPaint.apply { isFakeBoldText = false })
            y += 25f

            // Tabela - Cabeçalho
            val xData = 30f
            val xRegistros = 110f
            val xJornada = 250f
            val xAbonoInt = 295f
            val xTrabalhado = 350f
            val xSaldo = 410f
            val xBanco = 475f

            canvas.drawText("DATA", xData, y, headerPaint)
            canvas.drawText("REGISTROS / MOTIVO", xRegistros, y, headerPaint)
            canvas.drawText("JORN.", xJornada, y, headerPaint)
            canvas.drawText("ABONO INT.", xAbonoInt, y, headerPaint)
            canvas.drawText("TRAB.", xTrabalhado, y, headerPaint)
            canvas.drawText("SALDO", xSaldo, y, headerPaint)
            canvas.drawText("SALDO BANCO", xBanco, y, headerPaint)
            y += 5f
            canvas.drawLine(30f, y, 565f, y, paint)
            y += 12f

            var saldoBancoAcumulado = relatorio.saldoInicialBancoMinutos

            // Linhas da Tabela
            relatorio.dias.forEach { dia ->
                if (y > 800) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                    // Repetir cabeçalho
                    canvas.drawText("DATA", xData, y, headerPaint)
                    canvas.drawText("REGISTROS / MOTIVO", xRegistros, y, headerPaint)
                    canvas.drawText("JORN.", xJornada, y, headerPaint)
                    canvas.drawText("ABONO INT.", xAbonoInt, y, headerPaint)
                    canvas.drawText("TRAB.", xTrabalhado, y, headerPaint)
                    canvas.drawText("SALDO", xSaldo, y, headerPaint)
                    canvas.drawText("SALDO BANCO", xBanco, y, headerPaint)
                    y += 5f
                    canvas.drawLine(30f, y, 565f, y, paint)
                    y += 12f
                }
                
                val diaSemanaStr = dia.data.dayOfWeek.getDisplayName(TextStyle.SHORT, localePtBR).lowercase()
                val dataFormatada = "${dia.data.format(dateFormatter)} ($diaSemanaStr)"
                canvas.drawText(dataFormatada, xData, y, textPaint.apply { textSize = 7f; isFakeBoldText = false })
                
                // Lógica de Registros ou Motivo na mesma coluna
                val motivo = getMotivoAusencia(dia)
                if (motivo != null && dia.pontos.isEmpty()) {
                    canvas.drawText(motivo, xRegistros, y, textPaint.apply { isFakeBoldText = true; color = Color.DKGRAY })
                } else {
                    val pontosStr = dia.pontos.joinToString(" ") { it.horaConsiderada.format(timeFormatter) }
                    canvas.drawText(pontosStr, xRegistros, y, textPaint.apply { isFakeBoldText = false; color = Color.BLACK })
                }
                
                canvas.drawText(dia.cargaHorariaEfetivaMinutos.minutosParaHoraMinuto(), xJornada, y, textPaint)
                canvas.drawText(dia.minutosToleranciaIntervalo.minutosParaHoraMinuto(), xAbonoInt, y, textPaint)
                canvas.drawText(dia.horasTrabalhadasMinutos.minutosParaHoraMinuto(), xTrabalhado, y, textPaint)
                canvas.drawText(dia.saldoDiaMinutos.minutosParaSaldoFormatado(), xSaldo, y, textPaint)
                
                saldoBancoAcumulado += dia.saldoDiaMinutos
                canvas.drawText(saldoBancoAcumulado.minutosParaSaldoFormatado(), xBanco, y, textPaint)
                
                y += 12f
            }

            y += 10f
            canvas.drawLine(30f, y, 565f, y, paint)
            y += 20f

            // Totais
            canvas.drawText("TOTAIS DO PERÍODO", 30f, y, titlePaint.apply { textSize = 11f })
            y += 18f
            canvas.drawText("Esperado: ${relatorio.totalEsperadoFormatado}", 30f, y, textPaint.apply { textSize = 9f })
            y += 12f
            canvas.drawText("Trabalhado: ${relatorio.totalTrabalhadoFormatado}", 30f, y, textPaint)
            y += 12f
            canvas.drawText("Saldo do Período: ${relatorio.saldoFormatado}", 30f, y, textPaint)
            y += 12f
            canvas.drawText("Saldo Final Banco: ${saldoBancoAcumulado.minutosParaSaldoFormatado()}", 30f, y, textPaint.apply { isFakeBoldText = true })

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getMotivoAusencia(dia: ResumoDiaCompleto): String? {
        return when {
            dia.isFerias -> "FÉRIAS"
            dia.isAtestado -> "ATESTADO"
            dia.isDayOff -> "FALTA JUSTIFICADA - DAY OFF"
            dia.isFaltaJustificada -> "FALTA JUSTIFICADA"
            dia.isFaltaInjustificada -> "FALTA INJUSTIFICADA"
            dia.temFeriado -> "FERIADO"
            dia.isDescanso -> "FOLGA SEMANAL"
            dia.cargaHorariaEfetivaMinutos == 0 -> "FOLGA SEMANAL"
            else -> null
        }
    }
}
