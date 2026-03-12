#!/bin/bash
# =============================================================================
# SCRIPT ZERAR BANCO DE TESTES - MEU PONTO
# ./scripts/zerar_db_test.sh
# =============================================================================
set -e

ADB=~/Library/Android/sdk/platform-tools/adb
PACKAGE="br.com.tlmacedo.meuponto.debug"

echo "=== ZERANDO BANCO DE DADOS ==="

if ! $ADB devices | grep -q "device$"; then
    echo "❌ Erro: Nenhum dispositivo conectado!"
    exit 1
fi

echo "Fechando o app..."
$ADB shell am force-stop $PACKAGE 2>/dev/null || true

echo "Limpando dados do app..."
$ADB shell pm clear $PACKAGE

echo ""
echo "=== BANCO ZERADO COM SUCESSO ==="
echo "Abra o app para criar um novo banco vazio."
