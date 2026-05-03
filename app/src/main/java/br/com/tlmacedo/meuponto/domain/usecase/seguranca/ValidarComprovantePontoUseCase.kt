package br.com.tlmacedo.meuponto.domain.usecase.seguranca

import br.com.tlmacedo.meuponto.domain.model.ContextoJornadaDia
import br.com.tlmacedo.meuponto.domain.model.Ponto
import br.com.tlmacedo.meuponto.domain.model.PontoOcrResult
import java.time.Duration
import javax.inject.Inject
import kotlin.math.abs

/**
 * Valida os dados extraídos via OCR contra os dados reais do registro e do emprego.
 */
class ValidarComprovantePontoUseCase @Inject constructor() {

    enum class StatusValidacao {
        VALIDADO,
        DUPLICADO,
        DIVERGENTE,
        OCR_INCONCLUSIVO
    }
    
    data class ResultadoValidacao(
        val status: StatusValidacao,
        val divergencias: List<String> = emptyList()
    )

    operator fun invoke(
        ponto: Ponto,
        ocrResult: PontoOcrResult,
        contexto: ContextoJornadaDia
    ): ResultadoValidacao {
        if (ocrResult.isDuplicado) {
            return ResultadoValidacao(StatusValidacao.DUPLICADO, listOf("Este comprovante já foi utilizado em outro registro."))
        }
        
        val divergencias = mutableListOf<String>()
        
        // 1. Validar CNPJ
        if (!ocrResult.cnpj.isNullOrBlank() && !contexto.emprego.cnpj.isNullOrBlank()) {
            val cnpjOcr = ocrResult.cnpj.filter { it.isDigit() }
            val cnpjEmprego = contexto.emprego.cnpj.filter { it.isDigit() }
            if (cnpjOcr != cnpjEmprego) {
                divergencias.add("CNPJ do comprovante não confere com o da empresa.")
            }
        }
        
        // 2. Validar Data
        if (ocrResult.data != null && ocrResult.data != ponto.data) {
            divergencias.add("Data divergente: Comprovante (${ocrResult.data}) vs Registro (${ponto.data}).")
        }
        
        // 3. Validar Hora (tolerância de 2 minutos para delay de impressão/foto)
        if (ocrResult.hora != null) {
            val diff = abs(Duration.between(ocrResult.hora, ponto.hora).toMinutes())
            if (diff > 2) {
                divergencias.add("Hora divergente: Comprovante (${ocrResult.hora}) vs Registro (${ponto.hora}).")
            }
        }
        
        return when {
            divergencias.isNotEmpty() -> ResultadoValidacao(StatusValidacao.DIVERGENTE, divergencias)
            ocrResult.nsr == null -> ResultadoValidacao(StatusValidacao.OCR_INCONCLUSIVO, listOf("Não foi possível extrair o NSR do comprovante."))
            else -> ResultadoValidacao(StatusValidacao.VALIDADO)
        }
    }
}
