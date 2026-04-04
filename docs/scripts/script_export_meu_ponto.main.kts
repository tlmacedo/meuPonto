#!/usr/bin/env kotlin

import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Utility script to export project code organized by layers.
 * With a limit of 20,000 lines per generated file.
 */

fun exportByLayers(rootDirectory: String, destinationDirectory: String) {
    val MAX_LINES = 12000
    val allowedExtensions = setOf("kt", "java", "xml", "gradle", "kts", "properties", "sql")
    val ignoredFolders = setOf(".git", ".idea", "captures", "bin", "out", "export_meu_ponto", "build")

    val layers = mapOf(
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

    val rootFolder = File(rootDirectory)
    val destinationBaseFolder = File(destinationDirectory, "layers")

    if (destinationBaseFolder.exists()) {
        destinationBaseFolder.deleteRecursively()
    }
    destinationBaseFolder.mkdirs()

    println("Analyzing files and respecting limit of $MAX_LINES lines...")

    val buffers = mutableMapOf<String, StringBuilder>()
    val lineCount = mutableMapOf<String, Int>()
    val fileSequence = mutableMapOf<String, Int>()

    fun saveBuffer(layer: String) {
        val buffer = buffers[layer] ?: return
        if (buffer.isEmpty()) return

        val seq = fileSequence.getOrDefault(layer, 0)
        val suffix = if (seq == 0) "" else "_$seq"
        val fileName = "PROJECT_${layer}${suffix}.txt"
        val outputFile = File(destinationBaseFolder, fileName)

        outputFile.writeText(buffer.toString(), StandardCharsets.UTF_8)
        println("Generated: $fileName (${lineCount[layer]} lines)")

        buffer.clear()
        lineCount[layer] = 0
        fileSequence[layer] = seq + 1
    }

    rootFolder.walk()
        .onEnter { it.name !in ignoredFolders }
        .filter { it.isFile && (it.extension in allowedExtensions || it.name == "AndroidManifest.xml") }
        .forEach { file ->
            val absolutePath = file.absolutePath
            val identifiedLayer = layers.entries.find { absolutePath.contains(it.value, ignoreCase = true) }?.key ?: "OTHERS"

            val content = try {
                file.readText(StandardCharsets.UTF_8)
            } catch (e: Exception) {
                "Error reading ${file.name}: ${e.message}"
            }

            val linesInFile = content.lines()
            val totalNewLines = linesInFile.size + 6 // Delimiters and metadata

            // Check if adding this file exceeds the limit
            if ((lineCount[identifiedLayer] ?: 0) + totalNewLines > MAX_LINES) {
                saveBuffer(identifiedLayer)
            }

            val buffer = buffers.getOrPut(identifiedLayer) { StringBuilder() }
            val delimiter = "=".repeat(80)

            buffer.append("\n$delimiter\n")
            buffer.append("FILE: $absolutePath\n")
            buffer.append("LINES: ${linesInFile.size}\n")
            buffer.append("$delimiter\n\n")
            buffer.append(content)
            buffer.append("\n\n")

            lineCount[identifiedLayer] = (lineCount[identifiedLayer] ?: 0) + totalNewLines
        }

    // Save the remaining buffers
    buffers.keys.forEach { saveBuffer(it) }

    println("\nExport completed successfully!")
    println("Location: ${destinationBaseFolder.absolutePath}")
}

// Script execution
val currentDir = File(System.getProperty("user.dir")).parentFile.parentFile
val projectPath = if (currentDir.name == "scripts" || currentDir.name == "docs") {
    // Try to find the project root by going up levels
    var root = currentDir
    while (root.parentFile != null && !File(root, "app").exists()) {
        root = root.parentFile
    }
    root.absolutePath
} else {
    currentDir.absolutePath
}

val destinationPath = File(projectPath, "docs/export_codigo_projeto").absolutePath

println("Starting project export: $projectPath")
println("Destination: $destinationPath")

exportByLayers(projectPath, destinationPath)