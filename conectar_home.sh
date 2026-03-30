#!/bin/bash

echo "🔍 Buscando o arquivo de navegação..."
# Encontra o arquivo que está chamando a tela temporária de Dashboard
NAV_FILE=$(grep -rl "DashboardScreen" app/src/main/java/br/com/tlmacedo/meuponto/presentation/ | grep "\.kt$" | head -n 1)

if [ -z "$NAV_FILE" ]; then
    echo "⚠️ Arquivo de navegação não encontrado. Pode ser que a rota esteja configurada de outra forma."
else
    echo "✅ Arquivo encontrado: $NAV_FILE"

    # Substitui a chamada da tela
    sed -i '' 's/DashboardScreen()/HomeScreen()/g' "$NAV_FILE"

    # Adiciona o import da HomeScreen caso não exista
    if ! grep -q "br.com.tlmacedo.meuponto.presentation.screen.home.HomeScreen" "$NAV_FILE"; then
        sed -i '' '/import /a\
import br.com.tlmacedo.meuponto.presentation.screen.home.HomeScreen
' "$NAV_FILE"
    fi

    echo "✅ Rota atualizada para HomeScreen!"
fi

echo "🚀 Compilando o projeto..."
./build.sh
