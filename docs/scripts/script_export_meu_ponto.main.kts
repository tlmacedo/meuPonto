#!/usr/bin/env kotlin

import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Script utilitário para exportar o código do projeto organizado por camadas.
 * Com limite de 20.000 linhas por arquivo gerado.
 */

fun exportarPorCamadas(diretorioRaiz: String, diretorioDestino: String) {
    val MAX_LINHAS = 20000
    val extensoesPermitidas = setOf("kt", "java", "xml", "gradle", "kts", "properties", "sql")
    val pastasIgnoradas = setOf(".git", ".idea", "captures", "bin", "out", "export_meu_ponto", "build")

    val camadas = mapOf(
        "CORE" to "/core/",
        "DATA" to "/data/",
        "DI" to "/di/",
        "DOMAIN" to "/domain/",
        "PRESENTATION" to "/presentation/",
        "UTIL" to "/util/",
        "WORKER" to "/worker/",
        "RESOURCES" to "/src/main/res/",
        "MANIFEST" to "AndroidManifest.xml"
    )

    val pastaRaiz = File(diretorioRaiz)
    val pastaBaseDestino = File(diretorioDestino, "camadas")

    if (pastaBaseDestino.exists()) {
        pastaBaseDestino.deleteRecursively()
    }
    pastaBaseDestino.mkdirs()

    println("Analisando arquivos e respeitando limite de $MAX_LINHAS linhas...")

    val buffers = mutableMapOf<String, StringBuilder>()
    val contagemLinhas = mutableMapOf<String, Int>()
    val sequencialArquivo = mutableMapOf<String, Int>()

    fun salvarBuffer(camada: String) {
        val buffer = buffers[camada] ?: return
        if (buffer.isEmpty()) return

        val seq = sequencialArquivo.getOrDefault(camada, 0)
        val sufixo = if (seq == 0) "" else "_$seq"
        val nomeArquivo = "PROJETO_${camada}${sufixo}.txt"
        val arquivoSaida = File(pastaBaseDestino, nomeArquivo)

        arquivoSaida.writeText(buffer.toString(), StandardCharsets.UTF_8)
        println("Gerado: $nomeArquivo (${contagemLinhas[camada]} linhas)")

        buffer.clear()
        contagemLinhas[camada] = 0
        sequencialArquivo[camada] = seq + 1
    }

    pastaRaiz.walk()
        .onEnter { it.name !in pastasIgnoradas }
        .filter { it.isFile && (it.extension in extensoesPermitidas || it.name == "AndroidManifest.xml") }
        .forEach { arquivo ->
            val caminhoAbsoluto = arquivo.absolutePath
            val camadaIdentificada = camadas.entries.find { caminhoAbsoluto.contains(it.value, ignoreCase = true) }?.key ?: "OUTROS"

            val conteudo = try {
                arquivo.readText(StandardCharsets.UTF_8)
            } catch (e: Exception) {
                "Erro ao ler ${arquivo.name}: ${e.message}"
            }

            val linhasNoArquivo = conteudo.lines()
            val totalLinhasNovas = linhasNoArquivo.size + 6 // Delimitadores e metadados

            // Verifica se adicionar este arquivo ultrapassa o limite
            if ((contagemLinhas[camadaIdentificada] ?: 0) + totalLinhasNovas > MAX_LINHAS) {
                salvarBuffer(camadaIdentificada)
            }

            val buffer = buffers.getOrPut(camadaIdentificada) { StringBuilder() }
            val delimitador = "=".repeat(80)

            buffer.append("\n$delimitador\n")
            buffer.append("ARQUIVO: $caminhoAbsoluto\n")
            buffer.append("LINHAS: ${linhasNoArquivo.size}\n")
            buffer.append("$delimitador\n\n")
            buffer.append(conteudo)
            buffer.append("\n\n")

            contagemLinhas[camadaIdentificada] = (contagemLinhas[camadaIdentificada] ?: 0) + totalLinhasNovas
        }

    // Salva o restante dos buffers
    buffers.keys.forEach { salvarBuffer(it) }

    println("\nExportação concluída com sucesso!")
    println("Localização: ${pastaBaseDestino.absolutePath}")
}

// Execução do script
val currentDir = File(System.getProperty("user.dir")).parentFile.parentFile
val caminhoProjeto = if (currentDir.name == "scripts" || currentDir.name == "docs") {
    // Tenta encontrar a raiz do projeto subindo níveis
    var root = currentDir
    while (root.parentFile != null && !File(root, "app").exists()) {
        root = root.parentFile
    }
    root.absolutePath
} else {
    currentDir.absolutePath
}

val caminhoDestino = File(caminhoProjeto, "docs/export_codigo_projeto").absolutePath

println("Iniciando exportação do projeto: $caminhoProjeto")
println("Destino: $caminhoDestino")

exportarPorCamadas(caminhoProjeto, caminhoDestino)