#!/usr/bin/env bash
# -------------------------------------------------
# format.sh – Formata o código com ktlint
# -------------------------------------------------
set -euo pipefail

if ! command -v ktlint &>/dev/null; then
    echo "🔧 Instalando ktlint..."
    curl -sSLO https://github.com/pinterest/ktlint/releases/download/1.2.1/ktlint && chmod a+x ktlint && sudo mv ktlint /usr/local/bin/
fi

ktlint -F "**/*.kt"
echo "✅ Código formatado com ktlint."
