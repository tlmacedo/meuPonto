#!/bin/bash

# ═══════════════════════════════════════════════════════════════════════════════
#
#   📦 MeuPonto - Gerenciador de Backup do Banco de Dados
#
#   Script para backup e restore do banco SQLite via ADB
#   Compatível com builds debug e release
#
#   Uso:
#     ./db_backup.sh backup              # Criar backup
#     ./db_backup.sh restore             # Restaurar (interativo)
#     ./db_backup.sh restore arquivo.db  # Restaurar arquivo específico
#     ./db_backup.sh list                # Listar backups
#     ./db_backup.sh                     # Mostrar ajuda
#
# ═══════════════════════════════════════════════════════════════════════════════

# ───────────────────────────────────────────────────────────────
# CONFIGURAÇÕES
# ───────────────────────────────────────────────────────────────

PACKAGE_BASE="br.com.tlmacedo.meuponto"
DB_NAME="meuponto.db"
BACKUP_DIR="./docs/export_meu_ponto"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# ───────────────────────────────────────────────────────────────
# CORES PARA OUTPUT
# ───────────────────────────────────────────────────────────────

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ───────────────────────────────────────────────────────────────
# FUNÇÕES AUXILIARES
# ───────────────────────────────────────────────────────────────

# Detecta qual variante do pacote está instalada (debug ou release)
detect_package() {
    local debug_pkg="${PACKAGE_BASE}.debug"
    local release_pkg="${PACKAGE_BASE}"

    # Verificar se debug está instalado
    if adb shell pm list packages 2>/dev/null | grep -q "$debug_pkg"; then
        echo "$debug_pkg"
        return 0
    fi

    # Verificar se release está instalado
    if adb shell pm list packages 2>/dev/null | grep -q "$release_pkg"; then
        echo "$release_pkg"
        return 0
    fi

    return 1
}

# Formata tamanho em bytes para formato legível
format_size() {
    local bytes=$1
    if [ $bytes -ge 1048576 ]; then
        echo "$(echo "scale=2; $bytes/1048576" | bc) MB"
    elif [ $bytes -ge 1024 ]; then
        echo "$(echo "scale=2; $bytes/1024" | bc) KB"
    else
        echo "$bytes bytes"
    fi
}

# Formata data do arquivo para exibição
format_date() {
    local filename=$1
    # Extrai data do nome do arquivo (formato: meuponto_YYYYMMDD_HHMMSS.db)
    if [[ $filename =~ ([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})([0-9]{2})([0-9]{2}) ]]; then
        echo "${BASH_REMATCH[3]}/${BASH_REMATCH[2]}/${BASH_REMATCH[1]} ${BASH_REMATCH[4]}:${BASH_REMATCH[5]}:${BASH_REMATCH[6]}"
    else
        echo "Data desconhecida"
    fi
}

# Lista backups em formato de tabela
show_backup_table() {
    # Array para armazenar arquivos ordenados por data (mais recente primeiro)
    BACKUP_FILES=($(ls -t "$BACKUP_DIR"/*.db 2>/dev/null))

    if [ ${#BACKUP_FILES[@]} -eq 0 ]; then
        echo "  Nenhum backup encontrado"
        return 1
    fi

    printf "  ${CYAN}%-4s %-28s %-20s %s${NC}\n" "#" "Arquivo" "Data" "Tamanho"
    printf "  %-4s %-28s %-20s %s\n" "────" "────────────────────────────" "────────────────────" "────────"

    local index=1
    for file in "${BACKUP_FILES[@]}"; do
        local filename=$(basename "$file")
        local size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null)
        local size_formatted=$(format_size $size)
        local date_formatted=$(format_date "$filename")

        printf "  %-4s %-28s %-20s %s\n" "[$index]" "$filename" "$date_formatted" "$size_formatted"
        ((index++))
    done

    return 0
}

# Verifica conexão ADB
check_adb() {
    if ! command -v adb &> /dev/null; then
        echo -e "${RED}❌ ADB não encontrado. Instale o Android SDK.${NC}"
        exit 1
    fi

    if ! adb devices 2>/dev/null | grep -q "device$"; then
        echo -e "${RED}❌ Nenhum dispositivo conectado via ADB.${NC}"
        echo ""
        echo "   Verifique:"
        echo "   1. Dispositivo conectado via USB"
        echo "   2. Depuração USB ativada"
        echo "   3. Autorização de depuração aceita no dispositivo"
        echo ""
        exit 1
    fi
}

# ───────────────────────────────────────────────────────────────
# VERIFICAÇÕES INICIAIS
# ───────────────────────────────────────────────────────────────

check_adb

# ───────────────────────────────────────────────────────────────
# DETECÇÃO DE PACOTE
# ───────────────────────────────────────────────────────────────

PACKAGE=$(detect_package)

if [ -z "$PACKAGE" ] && [ "$1" != "list" ] && [ "$1" != "" ]; then
    echo -e "${RED}❌ Nenhum pacote MeuPonto encontrado no dispositivo${NC}"
    echo "   Verifique se o app está instalado e execute-o pelo menos uma vez"
    exit 1
fi

# ───────────────────────────────────────────────────────────────
# COMANDOS
# ───────────────────────────────────────────────────────────────

case "$1" in

    # ───────────────────────────────────────────────────────────
    # BACKUP
    # ───────────────────────────────────────────────────────────
    backup)
        mkdir -p "$BACKUP_DIR"
        BACKUP_FILE="$BACKUP_DIR/${DB_NAME%.db}_$TIMESTAMP.db"

        echo ""
        echo "╔═══════════════════════════════════════════════════════════╗"
        echo "║              📦 BACKUP DO BANCO DE DADOS                  ║"
        echo "╚═══════════════════════════════════════════════════════════╝"
        echo ""
        echo "   Pacote:  $PACKAGE"
        echo "   Destino: $BACKUP_DIR"
        echo ""
        echo "═══════════════════════════════════════════════════════════════"
        echo ""

        # 1. Parar o app para liberar locks
        echo "🛑 [1/6] Parando app para garantir consistência..."
        adb shell am force-stop $PACKAGE
        sleep 1

        # 2. Tentar checkpoint WAL no dispositivo (pode falhar se sqlite3 não existir)
        echo "⏳ [2/6] Tentando checkpoint WAL no dispositivo..."
        CHECKPOINT_RESULT=$(adb shell "run-as $PACKAGE sqlite3 databases/$DB_NAME 'PRAGMA wal_checkpoint(TRUNCATE);'" 2>&1)

        if [[ "$CHECKPOINT_RESULT" == *"not found"* ]] || [[ "$CHECKPOINT_RESULT" == *"error"* ]] || [[ "$CHECKPOINT_RESULT" == *"Error"* ]] || [[ "$CHECKPOINT_RESULT" == *"inaccessible"* ]]; then
            echo -e "   ${YELLOW}⚠️  sqlite3 não disponível no dispositivo${NC}"
            echo "   → Checkpoint será feito localmente após cópia"
            CHECKPOINT_REMOTE=false
        else
            echo "   ✓ WAL consolidado no dispositivo"
            CHECKPOINT_REMOTE=true
        fi
        sleep 1

        # 3. Verificar arquivos antes do backup
        echo "📂 [3/6] Verificando arquivos no dispositivo..."
        adb shell "run-as $PACKAGE ls -la databases/" 2>/dev/null | grep -E "meuponto|\.db" | while read line; do
            echo "   $line"
        done

        # 4. Copiar banco de dados + WAL + SHM
        echo "⏳ [4/6] Copiando banco de dados..."

        # Copiar arquivo principal
        adb shell "run-as $PACKAGE cat databases/$DB_NAME" > "$BACKUP_FILE"

        # Sempre copiar WAL e SHM (para garantir consistência)
        WAL_FILE="${BACKUP_FILE}-wal"
        SHM_FILE="${BACKUP_FILE}-shm"

        adb shell "run-as $PACKAGE cat databases/${DB_NAME}-wal" > "$WAL_FILE" 2>/dev/null
        adb shell "run-as $PACKAGE cat databases/${DB_NAME}-shm" > "$SHM_FILE" 2>/dev/null

        # 5. Consolidar WAL localmente (garantia extra)
        echo "🔄 [5/6] Consolidando WAL localmente..."

        WAL_SIZE=$(stat -f%z "$WAL_FILE" 2>/dev/null || stat -c%s "$WAL_FILE" 2>/dev/null || echo "0")

        if [ "$WAL_SIZE" -gt 0 ]; then
            echo "   📝 WAL encontrado ($WAL_SIZE bytes)"

            # Fazer checkpoint local usando sqlite3 do Mac
            sqlite3 "$BACKUP_FILE" "PRAGMA wal_checkpoint(TRUNCATE);" 2>/dev/null

            if [ $? -eq 0 ]; then
                echo "   ✓ WAL consolidado com sucesso"
                # Remover arquivos WAL/SHM após merge bem-sucedido
                rm -f "$WAL_FILE" "$SHM_FILE"
            else
                echo -e "   ${YELLOW}⚠️  Erro ao consolidar WAL - mantendo arquivos separados${NC}"
                echo "   → Os arquivos .db-wal e .db-shm foram preservados"
            fi
        else
            echo "   ✓ WAL vazio ou já consolidado"
            # Remover arquivos vazios
            rm -f "$WAL_FILE" "$SHM_FILE"
        fi

        # 6. Verificar resultado
        echo "🔍 [6/6] Verificando backup..."

        SIZE=$(stat -f%z "$BACKUP_FILE" 2>/dev/null || stat -c%s "$BACKUP_FILE" 2>/dev/null)

        if [ "$SIZE" -gt 1000 ]; then
            SIZE_FORMATTED=$(format_size "$SIZE")

            # Verificar integridade
            INTEGRITY=$(sqlite3 "$BACKUP_FILE" "PRAGMA integrity_check;" 2>/dev/null)

            if [ "$INTEGRITY" == "ok" ]; then
                # Contar registros para confirmar
                PONTOS_COUNT=$(sqlite3 "$BACKUP_FILE" "SELECT COUNT(*) FROM pontos;" 2>/dev/null || echo "?")

                # Mostrar últimos registros para conferência
                echo ""
                echo "   📋 Últimos 3 pontos no backup:"
                sqlite3 -separator " | " "$BACKUP_FILE" "SELECT id, data, hora, nsr FROM pontos ORDER BY id DESC LIMIT 3;" 2>/dev/null | while read line; do
                    echo "      $line"
                done

                echo ""
                echo "═══════════════════════════════════════════════════════════════"
                echo ""
                echo -e "${GREEN}✅ Backup concluído com sucesso!${NC}"
                echo ""
                echo "   📄 Arquivo:    $(basename "$BACKUP_FILE")"
                echo "   📊 Tamanho:    $SIZE_FORMATTED"
                echo "   📝 Registros:  $PONTOS_COUNT pontos"
                echo "   📁 Caminho:    $BACKUP_FILE"
                echo ""
            else
                echo ""
                echo -e "${YELLOW}⚠️  Backup criado, mas com possíveis problemas de integridade${NC}"
                echo "   Verifique: sqlite3 '$BACKUP_FILE' 'PRAGMA integrity_check;'"
                echo ""
            fi
        else
            echo ""
            echo -e "${RED}❌ Erro: arquivo muito pequeno ($SIZE bytes)${NC}"
            echo "   O backup pode estar corrompido"
            rm -f "$BACKUP_FILE" "$WAL_FILE" "$SHM_FILE"
            exit 1
        fi
        ;;

    # ───────────────────────────────────────────────────────────
    # RESTORE
    # ───────────────────────────────────────────────────────────
    restore)
        echo ""
        echo "╔═══════════════════════════════════════════════════════════╗"
        echo "║            📥 RESTAURAÇÃO DO BANCO DE DADOS               ║"
        echo "╚═══════════════════════════════════════════════════════════╝"
        echo ""
        echo "   Pacote: $PACKAGE"
        echo "   Origem: $BACKUP_DIR"
        echo ""

        # Verificar se foi passado um arquivo como argumento
        if [ -n "$2" ]; then
            # Modo direto: arquivo especificado
            if [ -f "$2" ]; then
                SELECTED_FILE="$2"
                SELECTED_NAME=$(basename "$SELECTED_FILE")
                echo "📄 Arquivo especificado: $SELECTED_NAME"
            else
                echo -e "${RED}❌ Arquivo não encontrado: $2${NC}"
                echo ""
                exit 1
            fi
        else
            # Modo interativo: listar e escolher
            if ! ls "$BACKUP_DIR"/*.db 1>/dev/null 2>&1; then
                echo -e "${RED}❌ Nenhum backup encontrado em: $BACKUP_DIR${NC}"
                echo ""
                echo "   Execute primeiro: $0 backup"
                echo ""
                exit 1
            fi

            echo "📋 Backups disponíveis (mais recentes primeiro):"
            echo ""

            show_backup_table

            echo ""
            echo "  [0] Cancelar"
            echo ""

            read -p "👉 Digite o número do backup para restaurar: " CHOICE

            if [[ "$CHOICE" == "0" ]] || [[ -z "$CHOICE" ]]; then
                echo ""
                echo "❌ Operação cancelada"
                echo ""
                exit 0
            fi

            if ! [[ "$CHOICE" =~ ^[0-9]+$ ]]; then
                echo ""
                echo -e "${RED}❌ Opção inválida: $CHOICE${NC}"
                echo ""
                exit 1
            fi

            if [ "$CHOICE" -lt 1 ] || [ "$CHOICE" -gt "${#BACKUP_FILES[@]}" ]; then
                echo ""
                echo -e "${RED}❌ Opção fora do intervalo: $CHOICE${NC}"
                echo "   Escolha um número entre 1 e ${#BACKUP_FILES[@]}"
                echo ""
                exit 1
            fi

            SELECTED_FILE="${BACKUP_FILES[$((CHOICE-1))]}"
            SELECTED_NAME=$(basename "$SELECTED_FILE")
        fi

        # Validar se é um arquivo .db
        if [[ ! "$SELECTED_FILE" == *.db ]]; then
            echo ""
            echo -e "${RED}❌ Arquivo inválido: deve ser um arquivo .db${NC}"
            echo ""
            exit 1
        fi

        # Mostrar informações do arquivo selecionado
        SELECTED_SIZE=$(stat -f%z "$SELECTED_FILE" 2>/dev/null || stat -c%s "$SELECTED_FILE" 2>/dev/null)
        SELECTED_SIZE_FORMATTED=$(format_size "$SELECTED_SIZE")
        SELECTED_DATE=$(format_date "$SELECTED_NAME")
        PONTOS_NO_BACKUP=$(sqlite3 "$SELECTED_FILE" "SELECT COUNT(*) FROM pontos;" 2>/dev/null || echo "?")

        echo ""
        echo "═══════════════════════════════════════════════════════════════"
        echo ""
        echo -e "${YELLOW}⚠️  ATENÇÃO: Esta operação irá SUBSTITUIR o banco atual!${NC}"
        echo ""
        echo "   📄 Arquivo:    $SELECTED_NAME"
        echo "   📅 Data:       $SELECTED_DATE"
        echo "   📊 Tamanho:    $SELECTED_SIZE_FORMATTED"
        echo "   📝 Registros:  $PONTOS_NO_BACKUP pontos"
        echo "   📁 Caminho:    $SELECTED_FILE"
        echo ""
        read -p "❓ Confirma a restauração? (s/n): " CONFIRM

        if [[ ! "$CONFIRM" =~ ^[Ss]$ ]]; then
            echo ""
            echo "❌ Operação cancelada"
            echo ""
            exit 0
        fi

        echo ""
        echo "═══════════════════════════════════════════════════════════════"
        echo ""

        echo "🛑 [1/7] Parando app..."
        adb shell am force-stop $PACKAGE
        sleep 1

        echo "🧹 [2/7] Limpando cache do app..."
        adb shell "run-as $PACKAGE rm -rf cache/*" 2>/dev/null

        echo "📤 [3/7] Enviando backup para dispositivo..."
        adb push "$SELECTED_FILE" /data/local/tmp/restore.db

        echo "🗑️  [4/7] Removendo banco atual e arquivos WAL..."
        adb shell "run-as $PACKAGE rm -f databases/$DB_NAME databases/$DB_NAME-shm databases/$DB_NAME-wal"

        echo "📥 [5/7] Copiando banco restaurado..."
        adb shell "cat /data/local/tmp/restore.db | run-as $PACKAGE sh -c 'cat > databases/$DB_NAME'"

        echo "🔒 [6/7] Ajustando permissões..."
        adb shell "run-as $PACKAGE chmod 660 databases/$DB_NAME"

        echo "🧹 [7/7] Limpando temporários..."
        adb shell rm /data/local/tmp/restore.db

        # Verificar restauração
        echo ""
        echo "🔍 Verificando restauração..."
        PONTOS_RESTAURADOS=$(adb shell "run-as $PACKAGE sqlite3 databases/$DB_NAME 'SELECT COUNT(*) FROM pontos;'" 2>/dev/null | tr -d '\r\n')

        echo ""
        echo "═══════════════════════════════════════════════════════════════"
        echo ""

        if [ "$PONTOS_RESTAURADOS" == "$PONTOS_NO_BACKUP" ]; then
            echo -e "${GREEN}✅ Restauração concluída com sucesso!${NC}"
            echo ""
            echo "   📝 Pontos restaurados: $PONTOS_RESTAURADOS"
        else
            echo -e "${YELLOW}⚠️  Restauração concluída, mas verifique os dados${NC}"
            echo ""
            echo "   📝 Pontos no backup:     $PONTOS_NO_BACKUP"
            echo "   📝 Pontos restaurados:   $PONTOS_RESTAURADOS"
        fi

        echo ""
        read -p "🚀 Deseja abrir o app agora? (s/n): " OPEN_APP
        if [[ "$OPEN_APP" =~ ^[Ss]$ ]]; then
            echo ""
            echo "⏳ Iniciando app..."
            adb shell monkey -p "$PACKAGE" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
            echo -e "${GREEN}✅ App iniciado!${NC}"
        fi
        echo ""
        ;;

    # ───────────────────────────────────────────────────────────
    # LIST
    # ───────────────────────────────────────────────────────────
    list)
        echo ""
        echo "╔═══════════════════════════════════════════════════════════╗"
        echo "║              📋 BACKUPS DISPONÍVEIS                       ║"
        echo "╚═══════════════════════════════════════════════════════════╝"
        echo ""
        echo "   Diretório: $BACKUP_DIR"
        echo ""

        if ! ls "$BACKUP_DIR"/*.db 1>/dev/null 2>&1; then
            echo -e "   ${YELLOW}Nenhum backup encontrado${NC}"
            echo ""
            echo "   Execute: $0 backup"
            echo ""
            exit 0
        fi

        show_backup_table

        # Calcular total
        TOTAL_SIZE=0
        TOTAL_COUNT=0
        for file in "$BACKUP_DIR"/*.db; do
            size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null)
            TOTAL_SIZE=$((TOTAL_SIZE + size))
            ((TOTAL_COUNT++))
        done

        echo ""
        echo "═══════════════════════════════════════════════════════════════"
        echo ""
        echo "   Total: $TOTAL_COUNT backup(s), $(format_size $TOTAL_SIZE)"
        echo ""
        ;;

    # ───────────────────────────────────────────────────────────
    # HELP (default)
    # ───────────────────────────────────────────────────────────
    *)
        echo ""
        echo "╔═══════════════════════════════════════════════════════════╗"
        echo "║     📦 MeuPonto - Gerenciador de Backup do Banco          ║"
        echo "╚═══════════════════════════════════════════════════════════╝"
        echo ""
        echo "   Uso: $0 <comando> [opções]"
        echo ""
        echo "   ┌─────────────────────────────────────────────────────────┐"
        echo "   │  Comandos disponíveis:                                  │"
        echo "   ├─────────────────────────────────────────────────────────┤"
        echo "   │  backup      Cria backup do banco de dados              │"
        echo "   │  restore     Restaura backup (menu ou arquivo direto)   │"
        echo "   │  list        Lista todos os backups disponíveis         │"
        echo "   └─────────────────────────────────────────────────────────┘"
        echo ""
        echo "   Exemplos:"
        echo "     $0 backup                         # Criar backup"
        echo "     $0 restore                        # Restaurar (interativo)"
        echo "     $0 restore arquivo.db             # Restaurar arquivo específico"
        echo "     $0 list                           # Ver backups"
        echo ""
        echo "   Configurações:"
        echo "     Banco:      $DB_NAME"
        echo "     Backups:    $BACKUP_DIR"
        if [ -n "$PACKAGE" ]; then
            echo "     Pacote:     $PACKAGE"
        else
            echo "     Pacote:     (será detectado automaticamente)"
        fi
        echo ""
        ;;
esac
