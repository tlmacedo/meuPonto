// path: app/src/main/java/br/com/tlmacedo/meuponto/data/service/OcrService.kt
package br.com.tlmacedo.meuponto.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import br.com.tlmacedo.meuponto.data.local.database.dao.PontoDao
import br.com.tlmacedo.meuponto.domain.model.PontoOcrResult
import br.com.tlmacedo.meuponto.domain.repository.EmpregoRepository
import br.com.tlmacedo.meuponto.util.ImageProcessor
import br.com.tlmacedo.meuponto.util.foto.ImageOrientationCorrector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço de OCR para extração de dados de comprovantes de ponto.
 *
 * Melhorias:
 * - Normalização de texto antes das heurísticas (corrige erros sistemáticos de OCR).
 * - isComprovantePonto mais tolerante a variações de impressão.
 * - Primeira tentativa em grayscale/contraste suave, binário apenas como fallback.
 * - Máximo de 2 passes de OCR por recorte; reciclagem explícita de bitmaps.
 */
@Singleton
class OcrService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val orientationCorrector: ImageOrientationCorrector,
    private val empregoRepository: EmpregoRepository,
    private val pontoDao: PontoDao,
) {

    // Modelo Latin (suporta bem pt‑BR)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // =====================================================================
    // PÚBLICO
    // =====================================================================

    /**
     * Extrai NSR, Data, Hora, Nome, PIS e CNPJ de uma imagem que pode conter
     * um ou mais comprovantes de ponto.
     *
     * Fluxo:
     *  1. OCR na imagem inteira para localizar blocos de comprovantes.
     *  2. Agrupa blocos por comprovante.
     *  3. Para cada grupo:
     *     - recorta com padding,
     *     - tentativa 1: OCR em grayscale/contraste,
     *     - se não reconhecer comprovante, tentativa 2: OCR binário,
     *     - normaliza texto,
     *     - extrai campos,
     *     - gera imagem com destaques e salva em cache.
     */
    suspend fun extrairDadosMultiplosComprovantes(
        uri: Uri,
        horariosHabituais: List<LocalTime> = emptyList(),
        empregoId: Long
    ): List<PontoOcrResult> = withContext(Dispatchers.Default) {
        try {
            val originalBitmap = orientationCorrector.loadBitmapWithCorrectOrientation(uri)
                ?: return@withContext emptyList()

            // 1) OCR na imagem inteira para localizar comprovantes
            val firstPassText = recognizer
                .process(InputImage.fromBitmap(originalBitmap, 0))
                .await()

            val gruposDeBlocos = agruparBlocosPorComprovante(firstPassText)
            val resultados = mutableListOf<PontoOcrResult>()

            for (grupo in gruposDeBlocos) {
                val boundingBox = calcularBoundingBox(grupo) ?: continue

                // Recorte com pequena margem
                val padding = (boundingBox.width() * 0.05f).toInt()
                val left = (boundingBox.left - padding).coerceAtLeast(0)
                val top = (boundingBox.top - padding).coerceAtLeast(0)
                val right = (boundingBox.right + padding).coerceAtMost(originalBitmap.width)
                val bottom = (boundingBox.bottom + padding).coerceAtMost(originalBitmap.height)

                val cropped = Bitmap.createBitmap(
                    originalBitmap,
                    left,
                    top,
                    right - left,
                    bottom - top
                )

                // 2) Preparar duas versões do recorte:
                //    - displayBitmap: grayscale + contraste (para OCR principal + exibição)
                //    - binaryBitmap: grayscale + contraste + blur + binarização (fallback)
                val displayBitmap = ImageProcessor.applyOcrFilters(
                    src = cropped,
                    contrast = 1.4f,
                    binarize = false,
                    blurBeforeBinary = false
                )

                val binaryBitmap = ImageProcessor.applyOcrFilters(
                    src = cropped,
                    contrast = 1.6f,
                    binarize = true,
                    blurBeforeBinary = true
                )

                // 3) Tentativa 1: OCR em grayscale/contraste
                var mlText = recognizer
                    .process(InputImage.fromBitmap(displayBitmap, 0))
                    .await()
                var fullTextRaw = mlText.text
                var fullText = normalizarTextoOcr(fullTextRaw)

                Timber.d("OCR_TEXT (GRAY):\n$fullText")

                // 4) Se não parece comprovante, tentar fallback binário
                if (!isComprovantePonto(fullText)) {
                    mlText = recognizer
                        .process(InputImage.fromBitmap(binaryBitmap, 0))
                        .await()
                    fullTextRaw = mlText.text
                    fullText = normalizarTextoOcr(fullTextRaw)

                    Timber.d("OCR_TEXT (BINARY_FALLBACK):\n$fullText")
                }

                // 5) Mesmo após fallback, se não parecer comprovante, descarta
                if (!isComprovantePonto(fullText)) {
                    displayBitmap.recycle()
                    binaryBitmap.recycle()
                    cropped.recycle()
                    continue
                }

                // 6) Extração de campos
                val nsr = extrairNsr(fullText)
                val data = extrairData(fullText)
                val hora = extrairHoraMelhorada(fullText, horariosHabituais)
                val nome = extrairNomeTrabalhador(fullText)
                val pis = extrairPis(fullText)
                val cnpj = extrairCnpj(fullText)

                // 7) Destaques de linhas importantes
                val rects = coletarRetangulosRelevantes(mlText, nsr, data, hora, nome)

                val finalBitmap = if (rects.isNotEmpty()) {
                    ImageProcessor.drawHighlights(displayBitmap, rects)
                } else {
                    displayBitmap
                }

                val isDuplicado = nsr?.let { pontoDao.existeNsr(it, empregoId) } ?: false

                // 8) Salvar imagem recortada em cache
                val tempFile = salvarBitmapEmCache(finalBitmap)

                // 9) Se houver CNPJ, buscar razão social
                var razaoSocial: String? = null
                if (cnpj != null) {
                    val empregador = empregoRepository.buscarPorCnpj(cnpj)
                    razaoSocial = empregador?.razaoSocial ?: empregador?.nome
                }

                // Apenas adiciona resultado se tiver alguma informação útil
                if (nsr != null || data != null || hora != null) {
                    resultados.add(
                        PontoOcrResult(
                            nsr = nsr,
                            data = data,
                            hora = hora,
                            nomeTrabalhador = nome,
                            pis = pis,
                            cnpj = cnpj,
                            razaoSocial = razaoSocial,
                            imagemRecortadaPath = tempFile.absolutePath,
                            isDuplicado = isDuplicado
                        )
                    )
                }

                if (finalBitmap !== displayBitmap) finalBitmap.recycle()
                binaryBitmap.recycle()
                displayBitmap.recycle()
                cropped.recycle()
            }

            originalBitmap.recycle()
            resultados
        } catch (e: Exception) {
            Timber.e(e, "Erro ao processar OCR de comprovante")
            emptyList()
        }
    }

    // =====================================================================
    // NORMALIZAÇÃO DE TEXTO
    // =====================================================================

    /**
     * Normaliza texto bruto de OCR para reduzir erros sistemáticos:
     * - Corrige variações comuns (CONPROVANTE, N5R, etc.).
     * - Remove caracteres de controle estranhos.
     * - Unifica espaçamentos.
     */
    private fun normalizarTextoOcr(text: String): String {
        var t = text
            .replace('\u00A0', ' ')  // non‑breaking space
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[|]+"), "|")

        // erros frequentes em COMPROVANTE / REGISTRO / TRABALHADOR
        t = t.replace(Regex("\\bCONPROVANTE\\b", RegexOption.IGNORE_CASE), "COMPROVANTE")
            .replace(Regex("\\bCOMPROVANE\\b", RegexOption.IGNORE_CASE), "COMPROVANTE")
            .replace(Regex("\\bTRABALHADOR[:-]", RegexOption.IGNORE_CASE), "TRABALHADOR:")
            .replace(Regex("N[5S]\\s*R", RegexOption.IGNORE_CASE), "NSR")
            .replace(Regex("N\\.S\\.R\\.", RegexOption.IGNORE_CASE), "NSR")

        // limpar zeros e letras grudadas em numerações longas
        t = t.replace(Regex("([0-9])O"), "$1 0")

        return t.trim()
    }

    // =====================================================================
    // HEURÍSTICAS DE DETECÇÃO DE COMPROVANTE
    // =====================================================================

    /**
     * Verifica se o texto parece ser um comprovante de ponto.
     * Mais generoso com erros de OCR, mas ainda restritivo para evitar falsos positivos.
     */
    private fun isComprovantePonto(text: String): Boolean {
        val t = text.uppercase()

        val temLabelComprovante =
            t.contains("COMPROVANTE") ||
                    t.contains("CONPROVANTE") ||
                    t.contains("COMPROVANE") ||
                    t.contains("COMPROVANTE DE REGISTRO") ||
                    t.contains("REGISTRO DE PONTO")

        val temPalavrasChave =
            t.contains("REGISTRO") ||
                    t.contains("PONTO") ||
                    t.contains("NSR") ||
                    t.contains("TRABALHADOR") ||
                    t.contains("PIS")

        // exige pelo menos uma data
        val temData = Regex("\\b\\d{2}[/\\-]\\d{2}[/\\-]\\d{2,4}\\b").containsMatchIn(t)

        return temLabelComprovante && temPalavrasChave && temData
    }

    // =====================================================================
    // EXTRAÇÃO DE CAMPOS
    // =====================================================================

    /**
     * Extrai NSR tentando evitar confusão com o número do REP (17 dígitos).
     */
    private fun extrairNsr(text: String): String? {
        val textNormalizado = text.replace(
            Regex("N[S\\s]R|MSR|N5R|N\\s*S\\s*R|N\\.S\\.R\\.", RegexOption.IGNORE_CASE),
            "NSR"
        )

        // números de 17 dígitos (REP) para excluir
        val repPattern = Pattern.compile("\\b(\\d{17})\\b")
        val repMatcher = repPattern.matcher(textNormalizado)
        val repsEncontrados = mutableListOf<String>()
        while (repMatcher.find()) {
            repMatcher.group(1)?.let { repsEncontrados.add(it) }
        }

        val patterns = listOf(
            Pattern.compile("NSR[:\\s.]*(\\d{9})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("NSR[:\\s.]*(\\d{1,17})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(\\d{9})\\b"),
            Pattern.compile("NSR:(\\d{1,9})", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(textNormalizado)
            while (matcher.find()) {
                val match = matcher.group(1) ?: matcher.group()
                if (repsEncontrados.any { it.contains(match) }) continue
                return match.replaceFirst(Regex("^0+"), "").ifEmpty { "0" }
            }
        }
        return null
    }

    private fun extrairData(text: String): LocalDate? {
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
                val dateStr = matcher.group(1) ?: continue
                try {
                    return LocalDate.parse(dateStr, formatters[i])
                } catch (_: Exception) {
                }
            }
        }
        return null
    }

    private fun extrairNomeTrabalhador(text: String): String? {
        val lines = text.lines()

        // 1) labels típicos
        val labels = listOf("NOME", "TRABALHADOR", "NOME DO TRABALHADOR", "EMPREGADO")
        for (line in lines) {
            val upper = line.uppercase()
            for (label in labels) {
                if (upper.contains("$label:") || (upper.startsWith(label) && upper.length > label.length + 4)) {
                    val nomeExtraido = if (upper.contains(":")) {
                        line.substringAfter(":").trim()
                    } else {
                        line.substring(label.length).trim()
                    }
                    val nomeLimpo = nomeExtraido.split(Regex("[-/|]")).first().trim()
                    if (nomeLimpo.length >= 5 &&
                        nomeLimpo.matches(Regex("^[A-Z\\sÁÉÍÓÚÀÈÌÒÙÂÊÎÔÛÃÕÇ]+$", RegexOption.IGNORE_CASE))
                    ) {
                        return nomeLimpo.uppercase()
                    }
                }
            }
        }

        // 2) fallback: linha que pareça nome
        for (line in lines) {
            val cleaned = line.trim()
            if (cleaned.isEmpty()) continue

            val upper = cleaned.uppercase()
            if (upper.startsWith("COMPROVANTE") ||
                upper.startsWith("CNPJ") ||
                upper.startsWith("CPF") ||
                upper.startsWith("AV.") ||
                upper.startsWith("RUA") ||
                cleaned.count { it.isDigit() } > 5
            ) continue

            val possivelNome = cleaned.split(Regex("[-|]")).first().trim()
            if (possivelNome.matches(Regex("^[A-Z\\sÁÉÍÓÚÂÊÎÔÛÃÕÇ]{8,50}$", RegexOption.IGNORE_CASE))) {
                return possivelNome.uppercase()
            }
        }
        return null
    }

    private fun extrairPis(text: String): String? {
        val pattern = Pattern.compile("PIS[:\\s]*(\\d{11,12})", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extrairCnpj(text: String): String? {
        val pattern = Pattern.compile("\\b(\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2})\\b")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.replace(Regex("[^0-9]"), "")
        }
        return null
    }

    /**
     * Extração de hora com:
     * - remoção de datas para não confundir 11/03 com 11:03;
     * - preferência por horários perto dos horários habituais;
     * - filtro para não pegar pedaços de outros números.
     */
    private fun extrairHoraMelhorada(
        text: String,
        horariosHabituais: List<LocalTime>
    ): LocalTime? {
        val textoSemDatas = text
            .replace(Regex("\\b\\d{2}[/.-]\\d{2}[/.-]\\d{4}\\b"), " [DATA] ")
            .replace(Regex("\\b\\d{2}/\\d{2}/\\d{2}\\b"), " [DATA] ")

        val horasEncontradas = mutableListOf<LocalTime>()

        // Caso especial: hora imediatamente antes de "REP"
        val patternRep = Pattern.compile("(\\d{2}[:.,\\s]\\s*\\d{2})\\s*REP", Pattern.CASE_INSENSITIVE)
        val matcherRep = patternRep.matcher(textoSemDatas)
        if (matcherRep.find()) {
            val horaStr = matcherRep.group(1)
            try {
                val partes = horaStr!!.split(Regex("[:.,\\s]")).filter { it.isNotBlank() }
                if (partes.size >= 2) {
                    val h = partes[0].toInt()
                    val m = partes[1].toInt()
                    return LocalTime.of(h, m)
                }
            } catch (_: Exception) {
            }
        }

        // Busca geral de hora
        val pattern = Pattern.compile(
            "\\b([01]?[0-9]|2[0-3])[:.,\\s]\\s*([0-5][0-9])(?:[:.,\\s]\\s*([0-5][0-9]))?\\b"
        )
        val matcher = pattern.matcher(textoSemDatas)

        while (matcher.find()) {
            try {
                val hStr = matcher.group(1)
                val mStr = matcher.group(2)
                if (hStr != null && mStr != null) {
                    val h = hStr.toInt()
                    val m = mStr.toInt()
                    val s = matcher.group(3)?.toInt() ?: 0

                    val start = matcher.start()
                    val end = matcher.end()
                    val charAntes = if (start > 0) textoSemDatas[start - 1] else ' '
                    val charDepois = if (end < textoSemDatas.length) textoSemDatas[end] else ' '

                    // evita capturar dentro de números maiores
                    if (charAntes.isDigit() || charDepois.isDigit()) continue

                    horasEncontradas.add(LocalTime.of(h, m, s))
                }
            } catch (_: Exception) {
            }
        }

        if (horasEncontradas.isEmpty()) return null

        // Se houver horários habituais, usa o mais próximo (até 3h de diferença)
        if (horariosHabituais.isNotEmpty()) {
            val melhor = horasEncontradas.minByOrNull { encontrada ->
                horariosHabituais.minOf { habitual ->
                    java.time.Duration.between(encontrada, habitual).abs().toMinutes()
                }
            }

            melhor?.let { encontrada ->
                val menorDiffHoras = horariosHabituais.minOf { habitual ->
                    java.time.Duration.between(encontrada, habitual).abs().toHours()
                }
                if (menorDiffHoras <= 3) return encontrada
            }
        }

        // fallback: primeiro horário diferente de meia‑noite
        return horasEncontradas.find { it != LocalTime.MIDNIGHT } ?: horasEncontradas.firstOrNull()
    }

    // =====================================================================
    // SUPORTE – bounding boxes, highlights, cache
    // =====================================================================

    private fun agruparBlocosPorComprovante(visionText: Text): List<List<Text.TextBlock>> {
        val blocos = visionText.textBlocks.sortedBy { it.boundingBox?.top ?: 0 }
        if (blocos.isEmpty()) return emptyList()

        val grupos = mutableListOf<MutableList<Text.TextBlock>>()
        var grupoAtual = mutableListOf<Text.TextBlock>()

        for (bloco in blocos) {
            val texto = bloco.text.uppercase()
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
        return Rect(left, top, right, bottom)
    }

    private fun coletarRetangulosRelevantes(
        text: Text,
        nsr: String?,
        data: LocalDate?,
        hora: LocalTime?,
        nome: String?
    ): List<Rect> {
        val rects = mutableListOf<Rect>()
        val dataStr = data?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val dataCurta = data?.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
        val horaStr = hora?.format(DateTimeFormatter.ofPattern("HH:mm"))

        for (block in text.textBlocks) {
            for (line in block.lines) {
                val l = normalizarTextoOcr(line.text).uppercase()

                if (nsr != null && (l.contains("NSR") || l.contains(nsr))) {
                    line.boundingBox?.let { rects.add(it) }
                }

                if (data != null &&
                    (l.contains(dataStr ?: "") ||
                            l.contains(dataCurta ?: "") ||
                            (l.contains("/") && l.length >= 8))
                ) {
                    line.boundingBox?.let { rects.add(it) }
                }

                if (horaStr != null &&
                    (l.contains(horaStr) ||
                            (l.contains(":") && l.length in 4..8))
                ) {
                    line.boundingBox?.let { rects.add(it) }
                }

                if (nome != null && l.contains(nome.uppercase())) {
                    line.boundingBox?.let { rects.add(it) }
                }
            }
        }

        return rects
    }

    private suspend fun salvarBitmapEmCache(bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        val fileName = "ocr_crop_${UUID.randomUUID()}.jpg"
        val tempFile = File(context.cacheDir, fileName)
        java.io.FileOutputStream(tempFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        tempFile
    }
}