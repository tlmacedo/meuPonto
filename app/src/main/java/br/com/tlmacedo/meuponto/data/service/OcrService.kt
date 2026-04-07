package br.com.tlmacedo.meuponto.data.service

import android.content.Context
import android.net.Uri
import br.com.tlmacedo.meuponto.util.ComprovanteImageStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import br.com.tlmacedo.meuponto.domain.model.PontoOcrResult
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Serviço de OCR para extração de dados de comprovantes de ponto.
 *
 * @author Thiago
 * @since 10.1.0
 */
@Singleton
class OcrService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extrai NSR, Data, Hora e Usuário de uma imagem.
     */
    suspend fun extrairDadosComprovante(uri: Uri): PontoOcrResult? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        val nsr = extrairNsr(text)
                        val data = extrairData(text)
                        val hora = extrairHora(text)
                        val nome = extrairNomeTrabalhador(text)
                        val pis = extrairPis(text)
                        
                        if (nsr != null || data != null || hora != null) {
                            continuation.resume(PontoOcrResult(
                                nsr = nsr, 
                                data = data, 
                                hora = hora,
                                nomeTrabalhador = nome,
                                pis = pis
                            ))
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Erro no OCR")
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar imagem para OCR")
                continuation.resume(null)
            }
        }
    }

    /**
     * Busca um padrão de NSR no texto.
     * Para os modelos Inner REP Plus, o NSR costuma ter 9 dígitos.
     */
    private fun extrairNsr(text: String): String? {
        val patterns = listOf(
            // Prioridade 1: "NSR:" seguido de 9 a 17 dígitos
            Pattern.compile("NSR[:\\s]*(\\d{9,17})", Pattern.CASE_INSENSITIVE),
            // Prioridade 2: Apenas a sequência de 9 dígitos (comum nesse modelo)
            Pattern.compile("\\b(\\d{9})\\b")
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                // Se houver grupo de captura (parênteses), pega ele, senão pega o match todo
                return matcher.group(1) ?: matcher.group()
            }
        }
        return null
    }

    /**
     * Busca um padrão de data (dd/MM/yyyy) no texto.
     */
    private fun extrairData(text: String): java.time.LocalDate? {
        val pattern = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b")
        val matcher = pattern.matcher(text)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        
        if (matcher.find()) {
            return try {
                java.time.LocalDate.parse(matcher.group(1), formatter)
            } catch (e: Exception) { null }
        }
        return null
    }

    /**
     * Busca o nome do trabalhador. Geralmente aparece em uma linha isolada.
     */
    private fun extrairNomeTrabalhador(text: String): String? {
        val lines = text.lines()
        // Procura por linhas que contenham o nome, geralmente após o CNPJ ou Endereço
        // Em modelos Inner REP, o nome do trabalhador costuma vir após o PIS ou antes dele
        // Vamos procurar por padrões conhecidos ou linhas que pareçam nomes (letras maiúsculas longas)
        
        for (line in lines) {
            val cleaned = line.trim()
            if (cleaned.isEmpty()) continue
            
            // Ignora cabeçalhos conhecidos
            if (cleaned.contains("COMPROVANTE", true) || 
                cleaned.contains("TRABALHADOR", true) ||
                cleaned.contains("SÍDIA", true) ||
                cleaned.contains("MATRIZ", true) ||
                cleaned.contains("AV.", true)) continue
            
            // Verifica se a linha tem apenas letras e espaços e é razoavelmente longa
            if (cleaned.matches(Regex("^[A-Z\\s]{10,50}$"))) {
                return cleaned
            }
        }
        return null
    }

    /**
     * Busca o PIS do trabalhador.
     */
    private fun extrairPis(text: String): String? {
        val pattern = Pattern.compile("PIS[:\\s]*(\\d{11,12})", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    /**
     * Busca um padrão de hora (HH:mm ou HH:mm:ss) no texto.
     */
    private fun extrairHora(text: String): LocalTime? {
        // Regex para HH:mm:ss ou HH:mm
        val patterns = listOf(
            Pattern.compile("\\b([01]?[0-9]|2[0-3]):[0-5][0-9](?::[0-5][0-9])?\\b")
        )

        val formatters = listOf(
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm")
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            
            // Em comprovantes Inner, a hora do registro costuma aparecer 
            // após a data ou isolada. Vamos tentar validar cada match.
            while (matcher.find()) {
                val horaStr = matcher.group()
                for (formatter in formatters) {
                    try {
                        return LocalTime.parse(horaStr, formatter)
                    } catch (e: Exception) { continue }
                }
            }
        }
        return null
    }
}
