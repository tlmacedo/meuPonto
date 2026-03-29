#!/usr/bin/env bash
# -------------------------------------------------
# run.sh – Instala e executa o app no dispositivo conectado
# -------------------------------------------------
set -euo pipefail

if ! adb devices | grep -w "device" &>/dev/null; then
    echo "⚠️ Nenhum dispositivo conectado. Conecte um dispositivo ou inicie um emulador."
    exit 1
fi

APK_PATH="$(find . -path "*/app/build/outputs/apk/debug/app-debug.apk" | head -n 1)"
if [[ -z "${APK_PATH}" ]]; then
    echo "⚠️ APK de debug não encontrado. Execute ./build.sh primeiro."
    exit 1
fi

adb install -r "${APK_PATH}"
echo "✅ APK instalado."

PACKAGE_NAME="br.com.tlmacedo.meuponto"
adb shell monkey -p "${PACKAGE_NAME}" -c android.intent.category.LAUNCHER 1
echo "🚀 Aplicativo iniciado no dispositivo."
