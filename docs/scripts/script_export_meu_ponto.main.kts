#!/usr/bin/env kotlin

import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Script utilitário para exportar o código do projeto organizado por camadas.
 * Para rodar, use: kotlin scripts/script_export_meu_ponto.main.kts
 * Ou via IDE clicando com o botão direito e "Run 'script_export_meu_ponto.main.kts'"
 */

fun exportarPorCamadas(diretorioRaiz: String, diretorioDestino: String) {
    val extensoesPermitidas = setOf("kt", "java", "xml")
    val pastasIgnoradas = setOf("build", ".gradle", ".git", ".idea", "captures", "bin", "out", "export_meu_ponto")

    // Definição das camadas para divisão dos arquivos
    val camadas = mapOf(
        "CORE" to "/core/",
        "DATA" to "/data/",
        "DI" to "/di/",
        "DOMAIN" to "/domain/",
        "PRESENTATION" to "/presentation/",
        "UTIL" to "/util/",
        "RESOURCES" to "/src/main/res/",
        "MANIFEST" to "AndroidManifest.xml"
    )

    val pastaRaiz = File(diretorioRaiz)
    val pastaBaseDestino = File(diretorioDestino, "camadas")

    if (pastaBaseDestino.exists()) {
        pastaBaseDestino.deleteRecursively()
    }
    pastaBaseDestino.mkdirs()

    println("Analisando arquivos...")
    val arquivosPorCamada = mutableMapOf<String, StringBuilder>()

    pastaRaiz.walk()
        .onEnter { it.name !in pastasIgnoradas }
        .filter { it.isFile && it.extension in extensoesPermitidas }
        .forEach { arquivo ->
            val caminhoAbsoluto = arquivo.absolutePath
            val camadaIdentificada = camadas.entries.find { caminhoAbsoluto.contains(it.value, ignoreCase = true) }?.key ?: "OUTROS"

            val buffer = arquivosPorCamada.getOrPut(camadaIdentificada) { StringBuilder() }

            val delimitador = "=".repeat(80)
            buffer.append("\n$delimitador\n")
            buffer.append("ARQUIVO: $caminhoAbsoluto\n")
            buffer.append("$delimitador\n\n")

            try {
                buffer.append(arquivo.readText(StandardCharsets.UTF_8))
            } catch (e: Exception) {
                buffer.append("Erro ao ler ${arquivo.name}: ${e.message}\n")
            }
            buffer.append("\n\n")
        }

    arquivosPorCamada.forEach { (nomeCamada, buffer) ->
        val arquivoSaida = File(pastaBaseDestino, "PROJETO_${nomeCamada}.txt")
        arquivoSaida.writeText(buffer.toString(), StandardCharsets.UTF_8)
        println("Gerado: ${arquivoSaida.name} (${buffer.length / 1024} KB)")
    }

    println("\nExportação concluída com sucesso!")
    println("Localização: ${pastaBaseDestino.absolutePath}")
}

// Execução do script
val currentDir = File(System.getProperty("user.dir"))
// Se o script for executado de dentro da pasta scripts, sobe um nível para pegar a raiz do projeto
val caminhoProjeto = if (currentDir.name == "scripts") currentDir.parentFile.absolutePath else currentDir.absolutePath
val caminhoDestino = File(caminhoProjeto, "export_meu_ponto").absolutePath

println("Iniciando exportação do projeto: $caminhoProjeto")
println("Destino: $caminhoDestino")

exportarPorCamadas(caminhoProjeto, caminhoDestino)