# Configurações
$PROJECT_NAME = "MeuPonto"
$OUTPUT_DIR = "${PROJECT_NAME}_Relatorio"
$ZIP_FILE = "${PROJECT_NAME}_Relatorio.zip"
$SUMMARY_FILE = "${OUTPUT_DIR}\RESUMO.md"

# Criar diretório de saída
New-Item -ItemType Directory -Path $OUTPUT_DIR -Force | Out-Null

# 1. Exportar estrutura de diretórios
tree /F /A | Out-File "${OUTPUT_DIR}\ESTRUTURA_DIRETORIOS.txt"

# 2. Listar arquivos essenciais
Get-ChildItem -Recurse -Include *.kt, *.java, *.xml, *.gradle, *.properties -Exclude *.class, *.jar |
  Where-Object { $_.FullName -notmatch '[\\/](\.gradle|build)[\\/]' } |
  Select-Object -ExpandProperty FullName |
  Out-File "${OUTPUT_DIR}\ARQUIVOS_ESSENCIAIS.txt"

# 3. Copiar código-fonte e recursos
Copy-Item -Recurse -Path "app\src\main\java" -Destination "${OUTPUT_DIR}\src\"
Copy-Item -Recurse -Path "app\src\main\res" -Destination "${OUTPUT_DIR}\src\"
Copy-Item -Path "app\src\main\AndroidManifest.xml" -Destination "${OUTPUT_DIR}\src\"

# 4. Copiar configurações de build
Copy-Item -Path "app\build.gradle.kts" -Destination $OUTPUT_DIR
Copy-Item -Path "build.gradle.kts" -Destination $OUTPUT_DIR
Copy-Item -Path "settings.gradle.kts" -Destination $OUTPUT_DIR

# 5. Gerar resumo em Markdown
@"
# Relatório do Projeto: $PROJECT_NAME

## Estrutura de Diretórios
\`\`\`
$(tree /F /A)
\`\`\`

## Arquivos Essenciais
- **Código Kotlin/Java**: \`$OUTPUT_DIR\src\java\`
- **Recursos**: \`$OUTPUT_DIR\src\res\`
- **Configurações**: \`build.gradle.kts\`, \`settings.gradle.kts\`

## Estatísticas
- **Total de Arquivos Kotlin**: $( (Get-ChildItem -Recurse -Include *.kt -Path app\src\main\java).Count )
- **Total de Layouts XML**: $( (Get-ChildItem -Recurse -Include *.xml -Path app\src\main\res).Count )

## Próximos Passos Sugeridos
1. Verificar módulos pendentes no \`settings.gradle.kts\`.
2. Analisar cobertura de testes em \`app\src\test\`.
3. Revisar dependências no \`app\build.gradle.kts\`.
"@ | Out-File $SUMMARY_FILE -Encoding UTF8

# 6. Compactar tudo
Compress-Archive -Path $OUTPUT_DIR -DestinationPath $ZIP_FILE -Force

Write-Host "✅ Relatório gerado em: $(Get-Location)\${ZIP_FILE}"
