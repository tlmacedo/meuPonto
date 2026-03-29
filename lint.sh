#!/usr/bin/env bash
# -------------------------------------------------
# lint.sh – Executa análise estática (Lint) e verifica erros
# -------------------------------------------------
set -euo pipefail

./gradlew lintDebug
REPORT="${PWD}/app/build/reports/lint-results-debug.html"
if [[ -f "${REPORT}" ]]; then
    echo "📊 Relatório Lint gerado: ${REPORT}"
    if command -v xdg-open &>/dev/null; then
        xdg-open "${REPORT}"
    elif command -v open &>/dev/null; then
        open "${REPORT}"
    fi
else
    echo "⚠️ Relatório Lint não encontrado."
fi
