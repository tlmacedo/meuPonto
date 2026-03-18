#!/bin/bash

# Configurações
PROJECT_NAME="MeuPonto"
OUTPUT_DIR="${PROJECT_NAME}_Relatorio"
ZIP_FILE="${PROJECT_NAME}_Relatorio.zip"
SUMMARY_FILE="${OUTPUT_DIR}/RESUMO.md"
BASE_DIR="../.."  # Subindo dois níveis

# Criar diretório de saída
mkdir -p "${OUTPUT_DIR}"

# 1. Exportar estrutura de diretórios (a partir do novo nível)
tree -d -I 'build|.gradle' "${BASE_DIR}" > "${OUTPUT_DIR}/ESTRUTURA_DIRETORIOS.txt"

# 2. Listar arquivos essenciais (a partir do novo nível)
find "${BASE_DIR}" -type f \( -name "*.kt" -o -name "*.java" -o -name "*.xml" -o -name "*.gradle" -o -name "*.properties" \) \
  ! -path "*/.gradle/*" ! -path "*/build/*" > "${OUTPUT_DIR}/ARQUIVOS_ESSENCIAIS.txt"

# 3. Copiar código-fonte e recursos (com paths absolutos)
mkdir -p "${OUTPUT_DIR}/src"
cp -r "${BASE_DIR}/app/src/main/java" "${OUTPUT_DIR}/src/"
cp -r "${BASE_DIR}/app/src/main/res" "${OUTPUT_DIR}/src/"
cp "${BASE_DIR}/app/src/main/AndroidManifest.xml" "${OUTPUT_DIR}/src/"

# 4. Copiar configurações de build
cp "${BASE_DIR}/app/build.gradle.kts" "${OUTPUT_DIR}/"
cp "${BASE_DIR}/build.gradle.kts" "${OUTPUT_DIR}/"
cp "${BASE_DIR}/settings.gradle.kts" "${OUTPUT_DIR}/"

# 5. Gerar resumo em Markdown
cat > "${SUMMARY_FILE}" <<EOF
# Relatório do Projeto: ${PROJECT_NAME}

## Estrutura de Diretórios
\`\`\`
$(tree -d -L 3 -I 'build|.gradle' "${BASE_DIR}")
\`\`\`

## Arquivos Essenciais
- **Código Kotlin/Java**: \`${OUTPUT_DIR}/src/java/\`
- **Recursos**: \`${OUTPUT_DIR}/src/res/\`
- **Configurações**: \`build.gradle.kts\`, \`settings.gradle.kts\`

## Estatísticas
- **Total de Arquivos Kotlin**: $(find "${BASE_DIR}/app/src/main/java" -name "*.kt" | wc -l)
- **Total de Layouts XML**: $(find "${BASE_DIR}/app/src/main/res" -name "*.xml" | wc -l)

## Próximos Passos Sugeridos
1. Verificar módulos pendentes no \`settings.gradle.kts\`.
2. Analisar cobertura de testes em \`app/src/test/\`.
3. Revisar dependências no \`app/build.gradle.kts\`.
EOF

# 6. Compactar tudo
zip -r "${ZIP_FILE}" "${OUTPUT_DIR}"

echo "✅ Relatório gerado em: $(pwd)/${ZIP_FILE}"
