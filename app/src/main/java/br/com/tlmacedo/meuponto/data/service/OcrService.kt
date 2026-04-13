package br.com.tlmacedo.meuponto.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import br.com.tlmacedo.meuponto.domain.model.PontoOcrResult
import br.com.tlmacedo.meuponto.util.ImageProcessor
import br.com.tlmacedo.meuponto.util.foto.ImageOrientationCorrector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

import br.com.tlmacedo.meuponto.util.ComprovanteImageStorage
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Serviço de OCR para extração de dados de comprovantes de ponto.
 *
 * @author Thiago
 * @since 10.1.0
 */
@Singleton
class OcrService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val orientationCorrector: ImageOrientationCorrector,
    private val empregoRepository: br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository,
    private val comprovanteImageStorage: ComprovanteImageStorage
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extrai NSR, Data, Hora e Usuário de uma imagem.
     * Agora suporta múltiplos comprovantes na mesma imagem.
     */
    suspend fun extrairDadosMultiplosComprovantes(
        uri: Uri,
        horariosHabituais: List<LocalTime> = emptyList(),
        empregoId: Long
    ): List<PontoOcrResult> = withContext(Dispatchers.Default) {
        try {
            val originalBitmap = orientationCorrector.loadBitmapWithCorrectOrientation(uri)
                ?: return@withContext emptyList()

            val image = InputImage.fromBitmap(originalBitmap, 0)
            val visionText = recognizer.process(image).await()

            val comprovantes = agruparBlocosPorComprovante(visionText)
            val resultados = mutableListOf<PontoOcrResult>()

            for (blocos in comprovantes) {
                val boundingBox = calcularBoundingBox(blocos) ?: continue
                
                // Recorta o comprovante com uma pequena margem
                val padding = (boundingBox.width() * 0.05f).toInt()
                val left = (boundingBox.left - padding).coerceAtLeast(0)
                val top = (boundingBox.top - padding).coerceAtLeast(0)
                val right = (boundingBox.right + padding).coerceAtMost(originalBitmap.width)
                val bottom = (boundingBox.bottom + padding).coerceAtMost(originalBitmap.height)
                
                val croppedBitmap = Bitmap.createBitmap(
                    originalBitmap, 
                    left, top, 
                    right - left, bottom - top
                )
                
                // Melhora a imagem do comprovante recortado
                val processedBitmap = ImageProcessor.processForOcr(croppedBitmap)
                
                val fullText = blocos.joinToString("\n") { it.text }
                val nsr = extrairNsr(fullText)
                val data = extrairData(fullText)
                val hora = extrairHoraMelhorada(fullText, horariosHabituais)
                val nome = extrairNomeTrabalhador(fullText)
                val pis = extrairPis(fullText)
                val cnpj = extrairCnpj(fullText)

                // Salva a imagem recortada e melhorada temporariamente ou associa ao resultado
                // Para simplificar agora, salvamos no diretório de comprovantes com um nome temp
                val fileName = "ocr_crop_${UUID.randomUUID()}.jpg"
                val tempFile = File(context.cacheDir, fileName)
                java.io.FileOutputStream(tempFile).use { out ->
                    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                var razaoSocial: String? = null
                if (cnpj != null) {
                    val empregador = empregoRepository.buscarPorCnpj(cnpj)
                    razaoSocial = empregador?.razaoSocial ?: empregador?.nome
                }

                if (nsr != null || data != null || hora != null) {
                    resultados.add(PontoOcrResult(
                        nsr = nsr,
                        data = data,
                        hora = hora,
                        nomeTrabalhador = nome,
                        pis = pis,
                        cnpj = cnpj,
                        razaoSocial = razaoSocial,
                        imagemRecortadaPath = tempFile.absolutePath
                    ))
                }
                
                processedBitmap.recycle()
                croppedBitmap.recycle()
            }

            originalBitmap.recycle()
            resultados
        } catch (e: Exception) {
            Timber.e(e, "Erro ao processar multi-OCR do comprovante")
            emptyList()
        }
    }

    /**
     * Agrupa blocos de texto que pertencem ao mesmo comprovante baseando-se na proximidade vertical
     * e na presença da palavra "COMPROVANTE".
     */
    private fun agruparBlocosPorComprovante(visionText: Text): List<List<Text.TextBlock>> {
        val blocos = visionText.textBlocks.sortedBy { it.boundingBox?.top ?: 0 }
        if (blocos.isEmpty()) return emptyList()

        val grupos = mutableListOf<MutableList<Text.TextBlock>>()
        var grupoAtual = mutableListOf<Text.TextBlock>()

        for (bloco in blocos) {
            val texto = bloco.text.uppercase()
            // Se encontrarmos a palavra COMPROVANTE e o grupo atual já tiver algo, 
            // ou se houver um grande salto vertical, começamos um novo grupo.
            if (texto.contains("COMPROVANTE") && grupoAtual.isNotEmpty()) {
                grupos.add(grupoAtual)
                grupoAtual = mutableListOf()
            }
            grupoAtual.add(bloco)
        }
        if (grupoAtual.isNotEmpty()) grupos.add(grupoAtual)

        return grupos
    }

    private fun calcularBoundingBox(blocos: List<Text.TextBlock>): android.graphics.Rect? {
        if (blocos.isEmpty()) return null
        var left = Int.MAX_VALUE
        var top = Int.MAX_VALUE
        var right = Int.MIN_VALUE
        var bottom = Int.MIN_VALUE

        for (bloco in blocos) {
            bloco.boundingBox?.let { box ->
                left = minOf(left, box.left)
                top = minOf(top, box.top)
                right = maxOf(right, box.right)
                bottom = maxOf(bottom, box.bottom)
            }
        }
        return android.graphics.Rect(left, top, right, bottom)
    }

    /**
     * Extrai NSR, Data, Hora e Usuário de uma imagem.
     * @param uri URI da imagem
     * @param horariosHabituais Lista de horários de trabalho do usuário para ajudar no "check" da hora
     * @param aplicarFiltros Se true, aplica grayscale e contraste antes do OCR
     */
    suspend fun extrairDadosComprovante(
        uri: Uri,
        horariosHabituais: List<LocalTime> = emptyList(),
        aplicarFiltros: Boolean = false
    ): PontoOcrResult? {
        return try {
            val originalBitmap = orientationCorrector.loadBitmapWithCorrectOrientation(uri)
                ?: return null

            val processedBitmap = if (aplicarFiltros) {
                ImageProcessor.processForOcr(originalBitmap)
            } else {
                originalBitmap
            }

            val image = InputImage.fromBitmap(processedBitmap, 0)
            val visionText = recognizer.process(image).await()

            // Reciclar bitmaps após processamento
            if (processedBitmap !== originalBitmap) processedBitmap.recycle()
            originalBitmap.recycle()

            val text = visionText.text
            val nsr = extrairNsr(text)
            val data = extrairData(text)
            val hora = extrairHoraMelhorada(text, horariosHabituais)
            val nome = extrairNomeTrabalhador(text)
            val pis = extrairPis(text)
            val cnpj = extrairCnpj(text)

            // Busca o empregador pelo CNPJ se encontrado
            var razaoSocial: String? = null
            if (cnpj != null) {
                val empregador = empregoRepository.buscarPorCnpj(cnpj)
                razaoSocial = empregador?.razaoSocial ?: empregador?.nome
            }

            if (nsr != null || data != null || hora != null) {
                PontoOcrResult(
                    nsr = nsr,
                    data = data,
                    hora = hora,
                    nomeTrabalhador = nome,
                    pis = pis,
                    cnpj = cnpj,
                    razaoSocial = razaoSocial
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao processar OCR do comprovante")
            null
        }
    }

    /**
     * Busca um padrão de NSR no texto.
     * Para os modelos Inner REP Plus, o NSR costuma ter 9 dígitos.
     */
    private fun extrairNsr(text: String): String? {
        // Normaliza o texto para tratar erros comuns de leitura do label
        // Adicionado suporte a "N.S.R." e variações com pontos
        val textNormalizado = text.replace(Regex("(?:N[S\\s]R|MSR|N5R|N\\s*S\\s*R|N\\.S\\.R\\.)", RegexOption.IGNORE_CASE), "NSR")
        
        // Identifica o número do REP (17 dígitos) para evitar que ele seja confundido com o NSR
        val repPattern = Pattern.compile("\\b(\\d{17})\\b")
        val repMatcher = repPattern.matcher(textNormalizado)
        val repEncontrado = if (repMatcher.find()) repMatcher.group(1) else null

        val patterns = listOf(
            // Prioridade 1: Label "NSR:" seguido de exatamente 9 dígitos (modelo Inner REP Plus)
            Pattern.compile("NSR[:\\s.]*(\\d{9})\\b", Pattern.CASE_INSENSITIVE),
            // Prioridade 2: Label "NSR:" seguido de 9 a 17 dígitos
            Pattern.compile("NSR[:\\s.]*(\\d{9,17})", Pattern.CASE_INSENSITIVE),
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
     * Busca o nome do trabalhador.
     * Refinado para modelos Inner REP Plus que usam labels como "Nome:" ou "Trabalhador:".
     */
    private fun extrairNomeTrabalhador(text: String): String? {
        val lines = text.lines()
        
        // 1. Busca por labels conhecidos
        val labels = listOf("NOME", "TRABALHADOR", "NOME DO TRABALHADOR", "EMPREGADO")
        for (line in lines) {
            val upperLine = line.uppercase()
            for (label in labels) {
                if (upperLine.contains("$label:") || (upperLine.startsWith(label) && upperLine.length > label.length + 5)) {
                    val nomeExtraido = if (upperLine.contains(":")) {
                        line.substringAfter(":").trim()
                    } else {
                        line.substring(label.length).trim()
                    }
                    
                    // Valida se o que restou parece um nome (pelo menos 2 palavras, apenas letras)
                    if (nomeExtraido.length >= 8 && nomeExtraido.matches(Regex("^[A-Z\\sÁÉÍÓÚÀÈÌÒÙÂÊÎÔÛÃÕÇ]+$", RegexOption.IGNORE_CASE))) {
                        return nomeExtraido.uppercase()
                    }
                }
            }
        }

        // 2. Fallback: Procura por linhas que pareçam nomes (letras maiúsculas longas sem números)
        for (line in lines) {
            val cleaned = line.trim()
            if (cleaned.isEmpty()) continue
            
            // Ignora cabeçalhos e campos técnicos conhecidos
            if (cleaned.contains("COMPROVANTE", true) || 
                cleaned.contains("SÍDIA", true) ||
                cleaned.contains("MATRIZ", true) ||
                cleaned.contains("CNPJ", true) ||
                cleaned.contains("CPF", true) ||
                cleaned.contains("AV.", true) ||
                cleaned.contains("RUA", true) ||
                cleaned.any { it.isDigit() }) continue
            
            // Verifica se a linha tem apenas letras e espaços e é razoavelmente longa (nome completo)
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
     * Busca o CNPJ da empresa.
     */
    private fun extrairCnpj(text: String): String? {
        val pattern = Pattern.compile("\\b(\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2})\\b")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.replace(Regex("[^0-9]"), "")
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
