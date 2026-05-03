package br.com.tlmacedo.meuponto.domain.usecase.relatorio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import br.com.tlmacedo.meuponto.domain.model.Emprego
import br.com.tlmacedo.meuponto.util.helper.minutosParaHoraMinuto
import br.com.tlmacedo.meuponto.util.helper.minutosParaSaldoFormatado
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Caso de uso para gerar o PDF do Espelho de Ponto Mensal.
 * Utiliza o framework nativo do Android (android.graphics.pdf).
 */
class GerarEspelhoPontoPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    suspend fun execute(
        relatorio: GerarRelatorioMensalUseCase.RelatorioMensal,
        emprego: Emprego,
        outputStream: OutputStream
    ): Result<Unit> {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 40f

            // Cabeçalho do Relatório
            canvas.drawText("ESPELHO DE PONTO MENSAL", 200f, y, titlePaint)
            y += 30f
            
            canvas.drawText("Empresa: ${emprego.nome}", 50f, y, textPaint)
            y += 20f
            canvas.drawText("Período: ${relatorio.dataInicio.format(dateFormatter)} a ${relatorio.dataFim.format(dateFormatter)}", 50f, y, textPaint)
            y += 30f

            // Tabela - Cabeçalho
            val xData = 50f
            val xPontos = 120f
            val xJornada = 280f
            val xTrabalhado = 350f
            val xSaldo = 420f
            val xObs = 490f

            canvas.drawText("DATA", xData, y, headerPaint)
            canvas.drawText("REGISTROS", xPontos, y, headerPaint)
            canvas.drawText("JORN.", xJornada, y, headerPaint)
            canvas.drawText("TRAB.", xTrabalhado, y, headerPaint)
            canvas.drawText("SALDO", xSaldo, y, headerPaint)
            y += 5f
            canvas.drawLine(50f, y, 550f, y, paint)
            y += 15f

            // Linhas da Tabela
            relatorio.dias.forEach { dia ->
                if (y > 800) {
                    // TODO: Implementar paginação se necessário
                }
                
                canvas.drawText(dia.data.format(dateFormatter), xData, y, textPaint.apply { textSize = 9f })
                
                val pontosStr = dia.pontos.joinToString(" ") { it.horaConsiderada.format(timeFormatter) }
                canvas.drawText(pontosStr, xPontos, y, textPaint)
                
                canvas.drawText(dia.cargaHorariaEfetivaMinutos.minutosParaHoraMinuto(), xJornada, y, textPaint)
                canvas.drawText(dia.horasTrabalhadasMinutos.minutosParaHoraMinuto(), xTrabalhado, y, textPaint)
                canvas.drawText(dia.saldoDiaMinutos.minutosParaSaldoFormatado(), xSaldo, y, textPaint)
                
                y += 15f
            }

            y += 10f
            canvas.drawLine(50f, y, 550f, y, paint)
            y += 20f

            // Totais
            canvas.drawText("TOTAIS DO PERÍODO", 50f, y, titlePaint.apply { textSize = 12f })
            y += 20f
            canvas.drawText("Esperado: ${relatorio.totalEsperadoFormatado}", 50f, y, textPaint)
            y += 15f
            canvas.drawText("Trabalhado: ${relatorio.totalTrabalhadoFormatado}", 50f, y, textPaint)
            y += 15f
            canvas.drawText("Saldo Total: ${relatorio.saldoFormatado}", 50f, y, textPaint.apply { typeface = Typeface.DEFAULT_BOLD })

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
