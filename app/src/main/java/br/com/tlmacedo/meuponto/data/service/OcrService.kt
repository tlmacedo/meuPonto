package br.com.tlmacedo.meuponto.data.service

import android.content.Context
import android.net.Uri
import br.com.tlmacedo.meuponto.domain.model.PontoOcrResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
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
     * @param uri URI da imagem
     * @param horariosHabituais Lista de horários de trabalho do usuário para ajudar no "check" da hora
     */
    suspend fun extrairDadosComprovante(uri: Uri, horariosHabituais: List<LocalTime> = emptyList()): PontoOcrResult? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        val nsr = extrairNsr(text)
                        val data = extrairData(text)
                        val hora = extrairHoraMelhorada(text, horariosHabituais)
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
        // Normaliza o texto para tratar erros comuns de leitura do label
        val textNormalizado = text.replace(Regex("(?:N[S\\s]R|MSR|N5R|N\\s*S\\s*R)", RegexOption.IGNORE_CASE), "NSR")
        
        // Identifica o número do REP (17 dígitos) para evitar que ele seja confundido com o NSR
        // Adicionada detecção de modelos Inner (REPSIDIA) que costumam ter o NSR após o label
        val repPattern = Pattern.compile("\\b(\\d{17})\\b")
        val repMatcher = repPattern.matcher(textNormalizado)
        val repEncontrado = if (repMatcher.find()) repMatcher.group(1) else null

        val patterns = listOf(
            // Prioridade 1: Label "NSR:" seguido de exatamente 9 dígitos (modelo do usuário)
            Pattern.compile("NSR[:\\s]*(\\d{9})\\b", Pattern.CASE_INSENSITIVE),
            // Prioridade 2: Label "NSR:" seguido de 9 a 17 dígitos
            Pattern.compile("NSR[:\\s]*(\\d{9,17})", Pattern.CASE_INSENSITIVE),
            // Prioridade 3: Sequência isolada de 9 dígitos que NÃO faça parte do número do REP
            Pattern.compile("\\b(\\d{9,10})\\b")
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(textNormalizado)
            while (matcher.find()) {
                val match = matcher.group(1) ?: matcher.group()
                // Validação: se o que encontramos é apenas parte do número do REP, ignoramos
                if (repEncontrado != null && repEncontrado.contains(match)) continue
                return match
            }
        }
        return null
    }

    /**
     * Busca um padrão de data (dd/MM/yyyy ou dd-MM-yyyy) no texto.
     */
    private fun extrairData(text: String): java.time.LocalDate? {
        val patterns = listOf(
            Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b"),
            Pattern.compile("\\b(\\d{2}-\\d{2}-\\d{4})\\b"),
            Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{2})\\b")
        )
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yy")
        )
        
        for (i in patterns.indices) {
            val matcher = patterns[i].matcher(text)
            if (matcher.find()) {
                try {
                    return java.time.LocalDate.parse(matcher.group(1), formatters[i])
                } catch (e: Exception) { continue }
            }
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
     * Faz uma varredura melhorada comparando com horários habituais e aceitando erros de separador.
     */
    private fun extrairHoraMelhorada(text: String, horariosHabituais: List<LocalTime>): LocalTime? {
        // Remove as datas do texto para não confundir "11/03" com "11:03"
        val textoSemDatas = text.replace(Regex("\\b\\d{2}[/.-]\\d{2}[/.-]\\d{4}\\b"), " [DATA] ")
            .replace(Regex("\\b\\d{2}/\\d{2}/\\d{2}\\b"), " [DATA] ")

        // Regex para capturar horas, dando preferência para aquelas que estão perto de "REP" ou "PIS"
        // Ou que estão no formato HH:mm isolado
        val pattern = Pattern.compile("\\b([01]?[0-9]|2[0-3])[:.,\\s]([0-5][0-9])(?:[:.,\\s]([0-5][0-9]))?\\b")
        val matcher = pattern.matcher(textoSemDatas)
        
        val horasEncontradas = mutableListOf<LocalTime>()
        
        // Também vamos buscar especificamente o que vem antes de "REP"
        val patternRep = Pattern.compile("(\\d{2}[:.,\\s]\\d{2})\\s*REP", Pattern.CASE_INSENSITIVE)
        val matcherRep = patternRep.matcher(textoSemDatas)
        if (matcherRep.find()) {
            val horaStr = matcherRep.group(1)
            try {
                val partes = horaStr!!.split(Regex("[:.,\\s]"))
                if (partes.size >= 2) {
                    val h = partes[0].toInt()
                    val m = partes[1].toInt()
                    return LocalTime.of(h, m)
                }
            } catch (e: Exception) { /* ignora e segue para busca geral */ }
        }

        while (matcher.find()) {
            try {
                val hStr = matcher.group(1)
                val mStr = matcher.group(2)
                if (hStr != null && mStr != null) {
                    val h = hStr.toInt()
                    val m = mStr.toInt()
                    val s = matcher.group(3)?.toInt() ?: 0
                    
                    // Validação simples: evitar horários que foram pegos de pedaços de outros números
                    // Se o match for parte de uma sequência muito maior, ignoramos
                    val start = matcher.start()
                    val end = matcher.end()
                    val charAntes = if (start > 0) textoSemDatas[start - 1] else ' '
                    val charDepois = if (end < textoSemDatas.length) textoSemDatas[end] else ' '
                    
                    if (charAntes.isDigit() || charDepois.isDigit()) continue

                    horasEncontradas.add(LocalTime.of(h, m, s))
                }
            } catch (e: Exception) { continue }
        }

        if (horasEncontradas.isEmpty()) return null

        // Se houver horários habituais, busca o que tem menor diferença (até 3h)
        if (horariosHabituais.isNotEmpty()) {
            val melhorMatch = horasEncontradas.minByOrNull { encontrada ->
                horariosHabituais.minOf { habitual ->
                    java.time.Duration.between(encontrada, habitual).abs().toMinutes()
                }
            }
            
            melhorMatch?.let { encontrada ->
                val menorDiffHoras = horariosHabituais.minOf { habitual ->
                    java.time.Duration.between(encontrada, habitual).abs().toHours()
                }
                if (menorDiffHoras <= 3) return encontrada
            }
        }

        // Se não houver habitual, pega o primeiro que não seja meia-noite (comum de erro)
        return horasEncontradas.find { it != LocalTime.MIDNIGHT } ?: horasEncontradas.firstOrNull()
    }
}
