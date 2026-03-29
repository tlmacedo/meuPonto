#!/usr/bin/env bash
# -------------------------------------------------
# test.sh – Executa testes unitários e de instrumentação
# -------------------------------------------------
set -euo pipefail

./gradlew testDebugUnitTest
# ./gradlew connectedDebugAndroidTest # Descomente se tiver emulador rodando
echo "✅ Todos os testes foram executados."
