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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço de OCR para extração de dados de comprovantes de ponto.
 *
 * @author Thiago
 * @since 10.1.0
 */
@Singleton
class OcrService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val orientationCorrector: ImageOrientationCorrector,
    private val empregoRepository: EmpregoRepository,
    private val pontoDao: PontoDao,
) {
    // Utiliza o modelo Latin por padrão, que é o recomendado para Português Brasileiro (suporta ç, ã, é, etc)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extrai NSR, Data, Hora e Usuário de uma imagem com alta precisão.
     * Utiliza processamento de imagem e OCR de duplo passo.
     */
    suspend fun extrairDadosMultiplosComprovantes(
        uri: Uri,
        horariosHabituais: List<LocalTime> = emptyList(),
        empregoId: Long
    ): List<PontoOcrResult> = withContext(Dispatchers.Default) {
        try {
            val originalBitmap = orientationCorrector.loadBitmapWithCorrectOrientation(uri)
                ?: return@withContext emptyList()

            // PASSO 1: Localizar possíveis comprovantes na imagem completa
            val firstPassImage = InputImage.fromBitmap(originalBitmap, 0)
            val firstPassText = recognizer.process(firstPassImage).await()

            val gruposDeBlocos = agruparBlocosPorComprovante(firstPassText)
            val resultados = mutableListOf<PontoOcrResult>()

            for (blocosPass1 in gruposDeBlocos) {
                val boundingBox = calcularBoundingBox(blocosPass1) ?: continue
                
                // Recorta o comprovante com margem de segurança
                val padding = (boundingBox.width() * 0.05f).toInt()
                val left = (boundingBox.left - padding).coerceAtLeast(0)
                val top = (boundingBox.top - padding).coerceAtLeast(0)
                val right = (boundingBox.right + padding).coerceAtMost(originalBitmap.width)
                val bottom = (boundingBox.bottom + padding).coerceAtMost(originalBitmap.height)
                
                val croppedBitmap = Bitmap.createBitmap(originalBitmap, left, top, right - left, bottom - top)
                
                // PASSO 2: OCR de alta precisão no recorte
                // Criamos duas versões: uma binarizada para o OCR e uma de alto contraste para o usuário
                val forOcrBitmap = ImageProcessor.applyOcrFilters(croppedBitmap, contrast = 2.2f, binarize = true)
                val forDisplayBitmap = ImageProcessor.applyOcrFilters(croppedBitmap, contrast = 1.8f, binarize = false)
                
                val secondPassImage = InputImage.fromBitmap(forOcrBitmap, 0)
                var secondPassText = recognizer.process(secondPassImage).await()
                
                var fullText = secondPassText.text
                Timber.d("OCR_TEXT_EXTRAIDO (BINARIZED): \n$fullText")
                
                // FALLBACK: Se falhou na versão binarizada, tenta na versão de alto contraste (tons de cinza)
                if (!isComprovantePonto(fullText)) {
                    val fallbackImage = InputImage.fromBitmap(forDisplayBitmap, 0)
                    secondPassText = recognizer.process(fallbackImage).await()
                    fullText = secondPassText.text
                    Timber.d("OCR_TEXT_EXTRAIDO (FALLBACK_GRAY): \n$fullText")
                }

                if (!isComprovantePonto(fullText)) {
                    forOcrBitmap.recycle()
                    forDisplayBitmap.recycle()
                    croppedBitmap.recycle()
                    continue
                }
                
                val nsr = extrairNsr(fullText)
                val data = extrairData(fullText)
                val hora = extrairHoraMelhorada(fullText, horariosHabituais)
                val nome = extrairNomeTrabalhador(fullText)
                val pis = extrairPis(fullText)
                val cnpj = extrairCnpj(fullText)

                // Identifica linhas específicas para destaque preciso
                val highlightedRects = mutableListOf<Rect>()
                for (block in secondPassText.textBlocks) {
                    for (line in block.lines) {
                        val text = line.text.uppercase()
                        
                        // Destaque NSR (procura o label ou o valor)
                        if (nsr != null && (text.contains("NSR") || text.contains(nsr))) {
                            line.boundingBox?.let { highlightedRects.add(it) }
                        }
                        
                        // Destaque Data (procura formatos comuns de data)
                        if (data != null) {
                            val dataStr = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            val dataCurta = data.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
                            if (text.contains(dataStr) || text.contains(dataCurta) || (text.contains("/") && text.length >= 8)) {
                                line.boundingBox?.let { highlightedRects.add(it) }
                            }
                        }
                        
                        // Destaque Hora (procura o horário extraído)
                        if (hora != null) {
                            val horaStr = hora.format(DateTimeFormatter.ofPattern("HH:mm"))
                            if (text.contains(horaStr) || (text.contains(":") && text.length >= 4 && text.length <= 8)) {
                                line.boundingBox?.let { highlightedRects.add(it) }
                            }
                        }
                        
                        // Destaque Nome do Usuário
                        if (nome != null && text.contains(nome.uppercase())) {
                            line.boundingBox?.let { highlightedRects.add(it) }
                        }
                    }
                }

                val finalBitmap = if (highlightedRects.isNotEmpty()) {
                    ImageProcessor.drawHighlights(forDisplayBitmap, highlightedRects)
                } else {
                    forDisplayBitmap
                }

                val isDuplicado = nsr?.let { pontoDao.existeNsr(it, empregoId) } ?: false

                val fileName = "ocr_crop_${UUID.randomUUID()}.jpg"
                val tempFile = File(context.cacheDir, fileName)
                withContext(Dispatchers.IO) {
                    java.io.FileOutputStream(tempFile).use { out ->
                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
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
                        imagemRecortadaPath = tempFile.absolutePath,
                        isDuplicado = isDuplicado
                    ))
                }
                
                if (finalBitmap != forDisplayBitmap) finalBitmap.recycle()
                forOcrBitmap.recycle()
                forDisplayBitmap.recycle()
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
        return Rect(left, top, right, bottom)
    }

    /**
     * Verifica se o texto extraído pertence a um comprovante de registro de ponto.
     */
    private fun isComprovantePonto(text: String): Boolean {
        val t = text.uppercase()
        // Mais resiliente a erros de OCR comuns no label principal
        val temLabelComprovante = t.contains("COMPROVANTE") || 
                                 t.contains("CONPROVANTE") || 
                                 t.contains("COMPROVANE") ||
                                 t.contains("RELAÇÃO")
        
        val temKeywordsPonto = t.contains("REGISTRO") || 
                              t.contains("PONTO") || 
                              t.contains("NSR") || 
                              t.contains("TRABALHADOR") ||
                              t.contains("PIS:")
                              
        return temLabelComprovante && temKeywordsPonto
    }

    /**
     * Busca um padrão de NSR no texto.
     * Para os modelos Inner REP Plus, o NSR costuma ter 9 dígitos.
     * Agora retorna o número como String sem zeros à esquerda.
     */
    private fun extrairNsr(text: String): String? {
        // Normaliza o texto para tratar erros comuns de leitura do label
        // Adicionado suporte a "N.S.R." e variações com pontos
        val textNormalizado = text.replace(Regex("N[S\\s]R|MSR|N5R|N\\s*S\\s*R|N\\.S\\.R\\.", RegexOption.IGNORE_CASE), "NSR")
        
        // Identifica o número do REP (17 dígitos) para evitar que ele seja confundido com o NSR
        val repPattern = Pattern.compile("\\b(\\d{17})\\b")
        val repMatcher = repPattern.matcher(textNormalizado)
        val repsEncontrados = mutableListOf<String>()
        while (repMatcher.find()) {
            repMatcher.group(1)?.let { repsEncontrados.add(it) }
        }

        val patterns = listOf(
            // Prioridade 1: Label "NSR:" seguido de exatamente 9 dígitos (modelo Inner REP Plus)
            Pattern.compile("NSR[:\\s.]*(\\d{9})", Pattern.CASE_INSENSITIVE),
            // Prioridade 2: Label "NSR:" seguido de 1 a 17 dígitos
            Pattern.compile("NSR[:\\s.]*(\\d{1,17})", Pattern.CASE_INSENSITIVE),
            // Prioridade 3: Sequência isolada de 9 dígitos que NÃO faça parte do número do REP
            Pattern.compile("\\b(\\d{9})\\b"),
            // Prioridade 4: NSR colado com data ou CNPJ (comum em leituras ruins)
            Pattern.compile("NSR:(\\d{1,9})", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(textNormalizado)
            while (matcher.find()) {
                val match = matcher.group(1) ?: matcher.group()
                // Validação: se o que encontramos é apenas parte do número do REP, ignoramos
                if (repsEncontrados.any { it.contains(match) }) continue
                
                // Remove zeros à esquerda e retorna
                return match.replaceFirst(Regex("^0+"), "").ifEmpty { "0" }
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
                val dateStr = matcher.group(1) ?: continue
                return try {
                    java.time.LocalDate.parse(dateStr, formatters[i])
                } catch (_: Exception) {
                    null
                }
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
                    
                    // Remove sufixos comuns que vêm na mesma linha (ex: PIS ou empresa)
                    val nomeLimpo = nomeExtraido.split(Regex("[-/|]")).first().trim()
                    
                    if (nomeLimpo.length >= 5 && nomeLimpo.matches(Regex("^[A-Z\\sÁÉÍÓÚÀÈÌÒÙÂÊÎÔÛÃÕÇ]+$", RegexOption.IGNORE_CASE))) {
                        return nomeLimpo.uppercase()
                    }
                }
            }
        }

        // 2. Fallback: Procura por linhas que pareçam nomes (letras maiúsculas longas sem muitos números)
        // Refinado para aceitar nomes mesmo que contenham "SÍDIA" ou "MATRIZ" no final da linha
        for (line in lines) {
            val cleaned = line.trim()
            if (cleaned.isEmpty()) continue
            
            // Ignora cabeçalhos e campos técnicos conhecidos (mas mantém a linha se o nome estiver no início)
            val upperCleaned = cleaned.uppercase()
            if (upperCleaned.startsWith("COMPROVANTE") || 
                upperCleaned.startsWith("CNPJ") || 
                upperCleaned.startsWith("CPF") || 
                upperCleaned.startsWith("AV.") || 
                upperCleaned.startsWith("RUA") ||
                cleaned.count { it.isDigit() } > 5) continue
            
            // Tenta pegar a parte antes de um hífen ou pipe
            val possivelNome = cleaned.split(Regex("[-|]")).first().trim()
            
            // Verifica se a parte inicial tem apenas letras e espaços e é razoavelmente longa
            if (possivelNome.matches(Regex("^[A-Z\\sÁÉÍÓÚÀÈÌÒÙÂÊÎÔÛÃÕÇ]{8,50}$", RegexOption.IGNORE_CASE))) {
                return possivelNome.uppercase()
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
        // Ou que estão no formato HH:mm isolado. Suporta espaço após o separador.
        val pattern = Pattern.compile("\\b([01]?[0-9]|2[0-3])[:.,\\s]\\s*([0-5][0-9])(?:[:.,\\s]\\s*([0-5][0-9]))?\\b")
        val matcher = pattern.matcher(textoSemDatas)
        
        val horasEncontradas = mutableListOf<LocalTime>()
        
        // Também vamos buscar especificamente o que vem antes de "REP"
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
            } catch (_: Exception) { /* ignora e segue para busca geral */ }
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
            } catch (_: Exception) { continue }
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
