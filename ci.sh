#!/usr/bin/env bash
# -------------------------------------------------
# ci.sh – Script de Integração Contínua (CI) simplificado
# -------------------------------------------------
set -euo pipefail

./gradlew clean
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew assembleRelease
echo "✅ Pipeline CI concluído com sucesso."
