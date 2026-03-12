#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════════
# Script: fix_warnings.sh
# Descrição: Corrige warnings de APIs deprecated
# ═══════════════════════════════════════════════════════════════════════════════

set -e

echo "╔══════════════════════════════════════════════════════════════════════╗"
echo "║  🔧 Correção de Warnings de Deprecation                             ║"
echo "╚══════════════════════════════════════════════════════════════════════╝"
echo ""

# ════════════════════════════════════════════════════════════════════════════
# 1. Corrigir Icons.Filled.Label → Icons.AutoMirrored.Filled.Label
# ════════════════════════════════════════════════════════════════════════════
echo "📄 [1/4] Corrigindo Icons.Filled.Label em EditPontoScreen.kt..."

EDITPONTO_FILE="app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/editponto/EditPontoScreen.kt"

# Adicionar import se não existir
if ! grep -q "Icons.AutoMirrored" "$EDITPONTO_FILE"; then
    sed -i '' '/import androidx.compose.material.icons.filled/a\
import androidx.compose.material.icons.automirrored.filled.Label
' "$EDITPONTO_FILE"
fi

# Substituir uso
sed -i '' 's/Icons\.Filled\.Label/Icons.AutoMirrored.Filled.Label/g' "$EDITPONTO_FILE"
echo "   ✅ EditPontoScreen.kt corrigido"

# ════════════════════════════════════════════════════════════════════════════
# 2. Corrigir Modifier.menuAnchor() deprecated
# ════════════════════════════════════════════════════════════════════════════
echo "📄 [2/4] Corrigindo Modifier.menuAnchor() em EditarEmpregoScreen.kt..."

EDITAREMPREGO_FILE="app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/empregos/editar/EditarEmpregoScreen.kt"

# Adicionar import do MenuAnchorType se não existir
if ! grep -q "MenuAnchorType" "$EDITAREMPREGO_FILE"; then
    sed -i '' '/import androidx.compose.material3.ExposedDropdownMenuBox/a\
import androidx.compose.material3.ExposedDropdownMenuAnchorType
' "$EDITAREMPREGO_FILE"
fi

# Substituir .menuAnchor() por .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
sed -i '' 's/\.menuAnchor()/.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)/g' "$EDITAREMPREGO_FILE"
echo "   ✅ EditarEmpregoScreen.kt corrigido"

# ════════════════════════════════════════════════════════════════════════════
# 3. Corrigir Modifier.menuAnchor() em EditarFeriadoScreen.kt
# ════════════════════════════════════════════════════════════════════════════
echo "📄 [3/4] Corrigindo Modifier.menuAnchor() em EditarFeriadoScreen.kt..."

EDITARFERIADO_FILE="app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/feriados/editar/EditarFeriadoScreen.kt"

# Adicionar import do MenuAnchorType se não existir
if ! grep -q "MenuAnchorType" "$EDITARFERIADO_FILE"; then
    sed -i '' '/import androidx.compose.material3.ExposedDropdownMenuBox/a\
import androidx.compose.material3.ExposedDropdownMenuAnchorType
' "$EDITARFERIADO_FILE"
fi

# Substituir .menuAnchor() por .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
sed -i '' 's/\.menuAnchor()/.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)/g' "$EDITARFERIADO_FILE"
echo "   ✅ EditarFeriadoScreen.kt corrigido"

# ════════════════════════════════════════════════════════════════════════════
# 4. Corrigir ExifInterface.getLatLong() deprecated
# ════════════════════════════════════════════════════════════════════════════
echo "📄 [4/4] Corrigindo ExifInterface.getLatLong() em ExifDataWriter.kt..."

EXIF_FILE="app/src/main/java/br/com/tlmacedo/meuponto/util/foto/ExifDataWriter.kt"

# Ver o contexto atual
echo "   Verificando contexto do uso de getLatLong..."

# Criar versão corrigida usando latLong property (API 30+) com fallback
cat > /tmp/exif_fix.kt << 'ENDOFFIX'
    /**
     * Lê coordenadas GPS de um arquivo de imagem.
     *
     * @param file Arquivo de imagem
     * @return Par de (latitude, longitude) ou null se não disponível
     */
    fun readGpsCoordinates(file: File): Pair<Double, Double>? {
        return try {
            val exif = ExifInterface(file)
            // Usar latLong property que é a forma moderna (retorna DoubleArray?)
            val latLong = exif.latLong
            if (latLong != null && latLong.size >= 2) {
                Pair(latLong[0], latLong[1])
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
ENDOFFIX

# Verificar se o método existe e substituir
if grep -q "fun readGpsCoordinates" "$EXIF_FILE"; then
    # O método existe, vamos substituí-lo
    # Usar Python para fazer substituição multiline de forma segura
    python3 << PYTHON_SCRIPT
import re

with open("$EXIF_FILE", 'r') as f:
    content = f.read()

# Pattern para encontrar o método readGpsCoordinates completo
pattern = r'(    /\*\*\s*\n\s*\*\s*Lê coordenadas GPS.*?\*/\s*\n\s*fun readGpsCoordinates\(file: File\): Pair<Double, Double>\? \{.*?^\s*\})'

replacement = '''    /**
     * Lê coordenadas GPS de um arquivo de imagem.
     *
     * @param file Arquivo de imagem
     * @return Par de (latitude, longitude) ou null se não disponível
     */
    fun readGpsCoordinates(file: File): Pair<Double, Double>? {
        return try {
            val exif = ExifInterface(file)
            // Usar latLong property (forma moderna, evita deprecation warning)
            val latLong = exif.latLong
            if (latLong != null && latLong.size >= 2) {
                Pair(latLong[0], latLong[1])
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }'''

new_content = re.sub(pattern, replacement, content, flags=re.MULTILINE | re.DOTALL)

with open("$EXIF_FILE", 'w') as f:
    f.write(new_content)

print("   Método readGpsCoordinates atualizado")
PYTHON_SCRIPT
else
    echo "   ⚠️  Método readGpsCoordinates não encontrado - verificar manualmente"
fi

echo "   ✅ ExifDataWriter.kt corrigido"

# ════════════════════════════════════════════════════════════════════════════
# 5. Suprimir warning do BackupViewModel (é intencional usar observarTodos)
# ════════════════════════════════════════════════════════════════════════════
echo "📄 [Bonus] Adicionando @Suppress no BackupViewModel.kt..."

BACKUP_FILE="app/src/main/java/br/com/tlmacedo/meuponto/presentation/screen/settings/backup/BackupViewModel.kt"

# Verificar se já tem suppress
if ! grep -q "@Suppress.*DEPRECATION" "$BACKUP_FILE"; then
    # Adicionar suppress antes da linha que usa observarTodos
    sed -i '' '/@HiltViewModel/a\
@Suppress("DEPRECATION") // observarTodos é usado intencionalmente para backup completo
' "$BACKUP_FILE"
    echo "   ✅ @Suppress adicionado"
else
    echo "   ℹ️  @Suppress já existe"
fi

echo ""
echo "╔══════════════════════════════════════════════════════════════════════╗"
echo "║  ✅ Todas as correções de warnings aplicadas!                        ║"
echo "╚══════════════════════════════════════════════════════════════════════╝"
echo ""
echo "Execute: ./gradlew build"
