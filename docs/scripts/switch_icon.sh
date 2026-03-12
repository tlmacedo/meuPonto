#!/bin/zsh

ICON_VERSION=${1:-1}
SOURCE_DIR="app/src/main/ic_launcher_source"
SOURCE_IMAGE="$SOURCE_DIR/ic_launcher_${ICON_VERSION}.png"

if ! command -v magick &> /dev/null; then
    echo "❌ ImageMagick não encontrado! Execute: brew install imagemagick"
    exit 1
fi

if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "❌ Imagem não encontrada: $SOURCE_IMAGE"
    echo "Uso: ./switch_icon.sh [1|2]"
    exit 1
fi

echo "🔄 Trocando para ícone versão $ICON_VERSION..."

magick "$SOURCE_IMAGE" -resize 48x48 "app/src/main/res/mipmap-mdpi/ic_launcher.webp"
magick "$SOURCE_IMAGE" -resize 72x72 "app/src/main/res/mipmap-hdpi/ic_launcher.webp"
magick "$SOURCE_IMAGE" -resize 96x96 "app/src/main/res/mipmap-xhdpi/ic_launcher.webp"
magick "$SOURCE_IMAGE" -resize 144x144 "app/src/main/res/mipmap-xxhdpi/ic_launcher.webp"
magick "$SOURCE_IMAGE" -resize 192x192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp"

magick "$SOURCE_IMAGE" -resize 48x48 "app/src/main/res/mipmap-mdpi/ic_launcher_round.webp"
magick "$SOURCE_IMAGE" -resize 72x72 "app/src/main/res/mipmap-hdpi/ic_launcher_round.webp"
magick "$SOURCE_IMAGE" -resize 96x96 "app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp"
magick "$SOURCE_IMAGE" -resize 144x144 "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp"
magick "$SOURCE_IMAGE" -resize 192x192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp"

magick "$SOURCE_IMAGE" -resize 66x66 -gravity center -background none -extent 108x108 "app/src/main/res/mipmap-mdpi/ic_launcher_foreground.webp"
magick "$SOURCE_IMAGE" -resize 99x99 -gravity center -background none -extent 162x162 "app/src/main/res/mipmap-hdpi/ic_launcher_foreground.webp"
magick "$SOURCE_IMAGE" -resize 132x132 -gravity center -background none -extent 216x216 "app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.webp"
magick "$SOURCE_IMAGE" -resize 198x198 -gravity center -background none -extent 324x324 "app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.webp"
magick "$SOURCE_IMAGE" -resize 264x264 -gravity center -background none -extent 432x432 "app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp"

echo "✅ Ícone trocado para versão $ICON_VERSION!"
echo "🔨 Execute: ./gradlew clean assembleDebug"

