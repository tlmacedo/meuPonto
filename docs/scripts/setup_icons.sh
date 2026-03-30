#!/bin/zsh

echo "🎨 Configurando ícones do MeuPonto..."

# Verificar ImageMagick
if ! command -v magick &> /dev/null; then
    echo "❌ ImageMagick não encontrado!"
    echo "   Execute: brew install imagemagick"
    exit 1
fi

SOURCE_DIR="app/src/main/res"
SOURCE_1="$SOURCE_DIR/icone_meu_ponto.png"
SOURCE_2="$SOURCE_DIR/LogoMeuPonto.png"

# Verificar imagens fonte
if [ ! -f "$SOURCE_1" ]; then
    echo "❌ Imagem não encontrada: $SOURCE_1"
    echo ""
    echo "Coloque as imagens em:"
    echo "  $SOURCE_1"
    echo "  $SOURCE_2"
    exit 1
fi

echo "📁 Criando diretórios..."
mkdir -p app/src/main/res/mipmap-mdpi
mkdir -p app/src/main/res/mipmap-hdpi
mkdir -p app/src/main/res/mipmap-xhdpi
mkdir -p app/src/main/res/mipmap-xxhdpi
mkdir -p app/src/main/res/mipmap-xxxhdpi
mkdir -p app/src/main/res/mipmap-anydpi-v26

echo "🖼️  Gerando ícones a partir de: $SOURCE_1"

# Ícone padrão
magick "$SOURCE_1" -resize 48x48 "app/src/main/res/mipmap-mdpi/ic_launcher.webp"
magick "$SOURCE_1" -resize 72x72 "app/src/main/res/mipmap-hdpi/ic_launcher.webp"
magick "$SOURCE_1" -resize 96x96 "app/src/main/res/mipmap-xhdpi/ic_launcher.webp"
magick "$SOURCE_1" -resize 144x144 "app/src/main/res/mipmap-xxhdpi/ic_launcher.webp"
magick "$SOURCE_1" -resize 192x192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp"

# Ícone redondo
magick "$SOURCE_1" -resize 48x48 "app/src/main/res/mipmap-mdpi/ic_launcher_round.webp"
magick "$SOURCE_1" -resize 72x72 "app/src/main/res/mipmap-hdpi/ic_launcher_round.webp"
magick "$SOURCE_1" -resize 96x96 "app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp"
magick "$SOURCE_1" -resize 144x144 "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp"
magick "$SOURCE_1" -resize 192x192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp"

# Foreground para Adaptive Icon
magick "$SOURCE_1" -resize 66x66 -gravity center -background none -extent 108x108 "app/src/main/res/mipmap-mdpi/ic_launcher_foreground.webp"
magick "$SOURCE_1" -resize 99x99 -gravity center -background none -extent 162x162 "app/src/main/res/mipmap-hdpi/ic_launcher_foreground.webp"
magick "$SOURCE_1" -resize 132x132 -gravity center -background none -extent 216x216 "app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.webp"
magick "$SOURCE_1" -resize 198x198 -gravity center -background none -extent 324x324 "app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.webp"
magick "$SOURCE_1" -resize 264x264 -gravity center -background none -extent 432x432 "app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp"

# Background gradiente
echo "🎨 Gerando background..."
magick -size 108x108 gradient:"#1565C0"-"#2E7D32" "app/src/main/res/mipmap-mdpi/ic_launcher_background.webp"
magick -size 162x162 gradient:"#1565C0"-"#2E7D32" "app/src/main/res/mipmap-hdpi/ic_launcher_background.webp"
magick -size 216x216 gradient:"#1565C0"-"#2E7D32" "app/src/main/res/mipmap-xhdpi/ic_launcher_background.webp"
magick -size 324x324 gradient:"#1565C0"-"#2E7D32" "app/src/main/res/mipmap-xxhdpi/ic_launcher_background.webp"
magick -size 432x432 gradient:"#1565C0"-"#2E7D32" "app/src/main/res/mipmap-xxxhdpi/ic_launcher_background.webp"

# XML Adaptive Icon
echo "📄 Criando XMLs..."

cat > app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml << 'XML_END'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
    <monochrome android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
XML_END

cat > app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml << 'XML_END'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
    <monochrome android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
XML_END

echo ""
echo "✅ Ícones configurados com sucesso!"
echo ""
echo "🔨 Execute: ./gradlew clean assembleDebug"

