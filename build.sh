#!/usr/bin/env bash
# -------------------------------------------------
# build.sh – Compila o projeto Android (Gradle)
# -------------------------------------------------
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
./gradlew clean
./gradlew assembleDebug
echo "✅ Build concluído. APK(s) disponíveis em ${PROJECT_ROOT}/app/build/outputs/apk/"
