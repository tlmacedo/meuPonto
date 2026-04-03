@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1

:: ═══════════════════════════════════════════════════════════════════════════════
::
::   📦 MeuPonto - Gerenciador de Backup do Banco de Dados (Windows)
::
::   Script para backup e restore do banco SQLite via ADB
::   Compatível com builds debug e release
::
::   Uso:
::     db_backup.bat backup              - Criar backup
::     db_backup.bat restore             - Restaurar (interativo)
::     db_backup.bat restore arquivo.db  - Restaurar arquivo específico
::     db_backup.bat list                - Listar backups
::     db_backup.bat                     - Mostrar ajuda
::
:: ═══════════════════════════════════════════════════════════════════════════════

:: ───────────────────────────────────────────────────────────────
:: CONFIGURAÇÕES
:: ───────────────────────────────────────────────────────────────

set "PACKAGE_BASE=br.com.tlmacedo.meuponto"
set "DB_NAME=meuponto.db"
set "IMAGES_PATH=files/comprovantes"
set "BACKUP_DIR=.\docs\export_meu_ponto"

:: Gerar timestamp
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set "DATETIME=%%I"
set "TIMESTAMP=%DATETIME:~0,4%%DATETIME:~4,2%%DATETIME:~6,2%_%DATETIME:~8,2%%DATETIME:~10,2%%DATETIME:~12,2%"

:: ───────────────────────────────────────────────────────────────
:: VERIFICAÇÃO DO ADB
:: ───────────────────────────────────────────────────────────────

where adb >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo.
    echo [91m❌ ADB não encontrado. Instale o Android SDK e adicione ao PATH.[0m
    echo.
    exit /b 1
)

:: Verificar dispositivo conectado
adb devices 2>nul | findstr /R "device$" >nul
if %ERRORLEVEL% neq 0 (
    echo.
    echo [91m❌ Nenhum dispositivo conectado via ADB.[0m
    echo.
    echo    Verifique:
    echo    1. Dispositivo conectado via USB
    echo    2. Depuração USB ativada
    echo    3. Autorização de depuração aceita no dispositivo
    echo.
    exit /b 1
)

:: ───────────────────────────────────────────────────────────────
:: DETECÇÃO DE PACOTE
:: ───────────────────────────────────────────────────────────────

set "PACKAGE="

:: Verificar debug primeiro
adb shell pm list packages 2>nul | findstr /C:"%PACKAGE_BASE%.debug" >nul
if %ERRORLEVEL% equ 0 (
    set "PACKAGE=%PACKAGE_BASE%.debug"
    goto :package_found
)

:: Verificar release
adb shell pm list packages 2>nul | findstr /C:"%PACKAGE_BASE%" >nul
if %ERRORLEVEL% equ 0 (
    set "PACKAGE=%PACKAGE_BASE%"
    goto :package_found
)

:: Pacote não encontrado
if "%1" neq "list" if "%1" neq "" (
    echo.
    echo [91m❌ Nenhum pacote MeuPonto encontrado no dispositivo[0m
    echo    Verifique se o app está instalado e execute-o pelo menos uma vez
    echo.
    exit /b 1
)

:package_found

:: ───────────────────────────────────────────────────────────────
:: ROTEAMENTO DE COMANDOS
:: ───────────────────────────────────────────────────────────────

if "%1"=="backup" goto :cmd_backup
if "%1"=="restore" goto :cmd_restore
if "%1"=="list" goto :cmd_list
goto :cmd_help

:: ═══════════════════════════════════════════════════════════════════════════════
:: COMANDO: BACKUP
:: ═══════════════════════════════════════════════════════════════════════════════

:cmd_backup
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

set "BACKUP_FILE=%BACKUP_DIR%\meuponto_%TIMESTAMP%.db"
set "WAL_FILE=%BACKUP_FILE%-wal"
set "SHM_FILE=%BACKUP_FILE%-shm"
set "IMAGES_BACKUP=%BACKUP_DIR%\meuponto_%TIMESTAMP%_images.tar.gz"

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║              📦 BACKUP DO BANCO DE DADOS                  ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo    Pacote:  %PACKAGE%
echo    Destino: %BACKUP_DIR%
echo.
echo ═══════════════════════════════════════════════════════════════
echo.

:: 1. Parar o app
echo 🛑 [1/7] Parando app para garantir consistência...
adb shell am force-stop %PACKAGE%
timeout /t 1 /nobreak >nul

:: 2. Tentar checkpoint WAL no dispositivo
echo ⏳ [2/7] Tentando checkpoint WAL no dispositivo...
set "CHECKPOINT_REMOTE=false"
adb shell "run-as %PACKAGE% sqlite3 databases/%DB_NAME% 'PRAGMA wal_checkpoint(TRUNCATE);'" 2>nul | findstr /C:"error" /C:"not found" /C:"inaccessible" >nul
if %ERRORLEVEL% neq 0 (
    echo    ✓ WAL consolidado no dispositivo
    set "CHECKPOINT_REMOTE=true"
) else (
    echo    [93m⚠️  sqlite3 não disponível no dispositivo[0m
    echo    → Checkpoint será feito localmente após cópia
)
timeout /t 1 /nobreak >nul

:: 3. Verificar arquivos
echo 📂 [3/7] Verificando arquivos no dispositivo...
adb shell "run-as %PACKAGE% ls -la databases/" 2>nul | findstr /C:"meuponto" /C:".db"

:: 4. Copiar banco de dados + WAL + SHM
echo ⏳ [4/7] Copiando banco de dados...

:: Copiar arquivo principal
adb shell "run-as %PACKAGE% cat databases/%DB_NAME%" > "%BACKUP_FILE%"

:: Copiar WAL e SHM
adb shell "run-as %PACKAGE% cat databases/%DB_NAME%-wal" > "%WAL_FILE%" 2>nul
adb shell "run-as %PACKAGE% cat databases/%DB_NAME%-shm" > "%SHM_FILE%" 2>nul

:: 5. Consolidar WAL localmente
echo 🔄 [5/7] Consolidando WAL localmente...

:: Verificar tamanho do WAL
for %%A in ("%WAL_FILE%") do set "WAL_SIZE=%%~zA"
if not defined WAL_SIZE set "WAL_SIZE=0"

if %WAL_SIZE% gtr 0 (
    echo    📝 WAL encontrado ^(%WAL_SIZE% bytes^)

    :: Verificar se sqlite3 está disponível localmente
    where sqlite3 >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        sqlite3 "%BACKUP_FILE%" "PRAGMA wal_checkpoint(TRUNCATE);" 2>nul
        if %ERRORLEVEL% equ 0 (
            echo    ✓ WAL consolidado com sucesso
            del /f /q "%WAL_FILE%" 2>nul
            del /f /q "%SHM_FILE%" 2>nul
        ) else (
            echo    [93m⚠️  Erro ao consolidar WAL - mantendo arquivos separados[0m
        )
    ) else (
        echo    [93m⚠️  sqlite3 não encontrado localmente[0m
        echo    → Instale sqlite3 ou os arquivos WAL/SHM serão mantidos separados
    )
) else (
    echo    ✓ WAL vazio ou já consolidado
    del /f /q "%WAL_FILE%" 2>nul
    del /f /q "%SHM_FILE%" 2>nul
)

:: 6. Backup das imagens
echo 📸 [6/7] Realizando backup das imagens...
adb shell "run-as %PACKAGE% [ -d %IMAGES_PATH% ]" 2>nul
if %ERRORLEVEL% equ 0 (
    adb shell "run-as %PACKAGE% tar -cz -C . %IMAGES_PATH% 2>/nul" > "%IMAGES_BACKUP%"
    for %%A in ("%IMAGES_BACKUP%") do set "IMG_SIZE=%%~zA"
    if !IMG_SIZE! leq 64 (
        del /f /q "%IMAGES_BACKUP%" 2>nul
        echo    ℹ️ Nenhuma imagem encontrada para backup
    ) else (
        echo    ✓ Backup de imagens concluído
    )
) else (
    echo    ℹ️ Diretório de imagens não encontrado no dispositivo
)

:: 7. Verificar resultado
echo 🔍 [7/7] Verificando backup...

for %%A in ("%BACKUP_FILE%") do set "SIZE=%%~zA"
if not defined SIZE set "SIZE=0"

if %SIZE% gtr 1000 (
    :: Calcular tamanho legível
    set /a "SIZE_KB=%SIZE%/1024"

    :: Verificar integridade (se sqlite3 disponível)
    set "INTEGRITY=unknown"
    where sqlite3 >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        for /f "tokens=*" %%I in ('sqlite3 "%BACKUP_FILE%" "PRAGMA integrity_check;" 2^>nul') do set "INTEGRITY=%%I"
    )

    :: Contar registros
    set "PONTOS_COUNT=?"
    where sqlite3 >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        for /f "tokens=*" %%I in ('sqlite3 "%BACKUP_FILE%" "SELECT COUNT(*) FROM pontos;" 2^>nul') do set "PONTOS_COUNT=%%I"
    )

    :: Mostrar últimos registros
    echo.
    echo    📋 Últimos 3 pontos no backup:
    where sqlite3 >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        for /f "tokens=*" %%I in ('sqlite3 -separator " | " "%BACKUP_FILE%" "SELECT id, data, hora, nsr FROM pontos ORDER BY id DESC LIMIT 3;" 2^>nul') do (
            echo       %%I
        )
    ) else (
        echo       [sqlite3 não disponível para verificação]
    )

    echo.
    echo ═══════════════════════════════════════════════════════════════
    echo.

    if "!INTEGRITY!"=="ok" (
        echo [92m✅ Backup concluído com sucesso![0m
    ) else (
        echo [92m✅ Backup concluído![0m
        if "!INTEGRITY!" neq "unknown" (
            echo [93m   ⚠️  Integridade: !INTEGRITY![0m
        )
    )

    echo.
    echo    📄 Arquivo:    meuponto_%TIMESTAMP%.db
    echo    📊 Tamanho:    !SIZE_KB! KB
    echo    📝 Registros:  !PONTOS_COUNT! pontos
    echo    📁 Caminho:    %BACKUP_FILE%
    echo.
) else (
    echo.
    echo [91m❌ Erro: arquivo muito pequeno ^(%SIZE% bytes^)[0m
    echo    O backup pode estar corrompido
    del /f /q "%BACKUP_FILE%" 2>nul
    del /f /q "%WAL_FILE%" 2>nul
    del /f /q "%SHM_FILE%" 2>nul
    del /f /q "%IMAGES_BACKUP%" 2>nul
    exit /b 1
)

goto :eof

:: ═══════════════════════════════════════════════════════════════════════════════
:: COMANDO: RESTORE
:: ═══════════════════════════════════════════════════════════════════════════════

:cmd_restore
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║            📥 RESTAURAÇÃO DO BANCO DE DADOS               ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo    Pacote: %PACKAGE%
echo    Origem: %BACKUP_DIR%
echo.

:: Verificar se foi passado um arquivo como argumento
if not "%2"=="" (
    if exist "%2" (
        set "SELECTED_FILE=%2"
        for %%F in ("%2") do set "SELECTED_NAME=%%~nxF"
        echo 📄 Arquivo especificado: !SELECTED_NAME!
        goto :restore_confirm
    ) else (
        echo [91m❌ Arquivo não encontrado: %2[0m
        echo.
        exit /b 1
    )
)

:: Modo interativo: listar e escolher
if not exist "%BACKUP_DIR%\*.db" (
    echo [91m❌ Nenhum backup encontrado em: %BACKUP_DIR%[0m
    echo.
    echo    Execute primeiro: %0 backup
    echo.
    exit /b 1
)

echo 📋 Backups disponíveis (mais recentes primeiro^):
echo.

:: Listar backups com numeração
set "COUNT=0"
for /f "tokens=*" %%F in ('dir /b /o-d "%BACKUP_DIR%\*.db" 2^>nul') do (
    set /a "COUNT+=1"
    set "BACKUP_!COUNT!=%%F"

    :: Obter tamanho
    for %%A in ("%BACKUP_DIR%\%%F") do set /a "SIZE_KB=%%~zA/1024"

    :: Extrair data do nome (formato: meuponto_YYYYMMDD_HHMMSS.db)
    set "FNAME=%%~nF"
    set "FDATE=!FNAME:~9,4!/!FNAME:~13,2!/!FNAME:~15,2! !FNAME:~18,2!:!FNAME:~20,2!:!FNAME:~22,2!"

    set "HAS_IMG=-"
    set "IMG_NAME=%%~nF_images.tar.gz"
    if exist "%BACKUP_DIR%\!IMG_NAME!" set "HAS_IMG=✓"

    echo    [!COUNT!] %%F   !FDATE!   !SIZE_KB! KB   Img: !HAS_IMG!
)

echo.
echo    [0] Cancelar
echo.

set /p "CHOICE=👉 Digite o número do backup para restaurar: "

if "%CHOICE%"=="0" (
    echo.
    echo ❌ Operação cancelada
    echo.
    exit /b 0
)

if "%CHOICE%"=="" (
    echo.
    echo ❌ Operação cancelada
    echo.
    exit /b 0
)

:: Validar escolha
set "SELECTED_NAME=!BACKUP_%CHOICE%!"
if not defined SELECTED_NAME (
    echo.
    echo [91m❌ Opção inválida: %CHOICE%[0m
    echo.
    exit /b 1
)

set "SELECTED_FILE=%BACKUP_DIR%\!SELECTED_NAME!"

:restore_confirm
:: Mostrar informações do arquivo selecionado
for %%A in ("%SELECTED_FILE%") do set /a "SELECTED_SIZE_KB=%%~zA/1024"

set "PONTOS_NO_BACKUP=?"
where sqlite3 >nul 2>&1
if %ERRORLEVEL% equ 0 (
    for /f "tokens=*" %%I in ('sqlite3 "%SELECTED_FILE%" "SELECT COUNT(*) FROM pontos;" 2^>nul') do set "PONTOS_NO_BACKUP=%%I"
)

set "IMAGES_BACKUP=%SELECTED_FILE:.db=_images.tar.gz%"
set "HAS_IMAGES=Não"
if exist "%IMAGES_BACKUP%" set "HAS_IMAGES=Sim"

echo.
echo ═══════════════════════════════════════════════════════════════
echo.
echo [93m⚠️  ATENÇÃO: Esta operação irá SUBSTITUIR o banco atual![0m
echo.
echo    📄 Arquivo:    !SELECTED_NAME!
echo    📊 Tamanho:    !SELECTED_SIZE_KB! KB
echo    📝 Registros:  !PONTOS_NO_BACKUP! pontos
echo    📸 Imagens:    !HAS_IMAGES!
echo    📁 Caminho:    %SELECTED_FILE%
echo.

set /p "CONFIRM=❓ Confirma a restauração? (s/n): "

if /i not "%CONFIRM%"=="s" (
    echo.
    echo ❌ Operação cancelada
    echo.
    exit /b 0
)

echo.
echo ═══════════════════════════════════════════════════════════════
echo.

echo 🛑 [1/8] Parando app...
adb shell am force-stop %PACKAGE%
timeout /t 1 /nobreak >nul

echo 🧹 [2/8] Limpando cache do app...
adb shell "run-as %PACKAGE% rm -rf cache/*" 2>nul

echo 📤 [3/8] Enviando backup para dispositivo...
adb push "%SELECTED_FILE%" /data/local/tmp/restore.db

echo 🗑️  [4/8] Removendo banco atual e arquivos WAL...
adb shell "run-as %PACKAGE% rm -f databases/%DB_NAME% databases/%DB_NAME%-shm databases/%DB_NAME%-wal"

echo 📥 [5/8] Copiando banco restaurado...
adb shell "cat /data/local/tmp/restore.db | run-as %PACKAGE% sh -c 'cat > databases/%DB_NAME%'"

echo 🔒 [6/8] Ajustando permissões...
adb shell "run-as %PACKAGE% chmod 660 databases/%DB_NAME%"

echo 🧹 [7/8] Limpando temporários...
adb shell rm /data/local/tmp/restore.db

echo 📥 [8/8] Restaurando imagens...
if exist "%IMAGES_BACKUP%" (
    adb shell "run-as %PACKAGE% rm -rf %IMAGES_PATH%/*" 2>nul
    adb shell "run-as %PACKAGE% mkdir -p %IMAGES_PATH%" 2>nul
    adb push "%IMAGES_BACKUP%" /data/local/tmp/images.tar.gz
    adb shell "run-as %PACKAGE% tar -xz -f /data/local/tmp/images.tar.gz -C ."
    adb shell rm /data/local/tmp/images.tar.gz
    echo    ✓ Imagens restauradas
) else (
    echo    ℹ️ Nenhuma imagem para restaurar
)

:: Verificar restauração
echo.
echo 🔍 Verificando restauração...

set "PONTOS_RESTAURADOS=?"
for /f "tokens=*" %%I in ('adb shell "run-as %PACKAGE% sqlite3 databases/%DB_NAME% 'SELECT COUNT(*) FROM pontos;'" 2^>nul') do (
    set "PONTOS_RESTAURADOS=%%I"
)
:: Remover caracteres de retorno de carro
set "PONTOS_RESTAURADOS=!PONTOS_RESTAURADOS: =!"

echo.
echo ═══════════════════════════════════════════════════════════════
echo.

if "!PONTOS_RESTAURADOS!"=="!PONTOS_NO_BACKUP!" (
    echo [92m✅ Restauração concluída com sucesso![0m
    echo.
    echo    📝 Pontos restaurados: !PONTOS_RESTAURADOS!
) else (
    echo [93m⚠️  Restauração concluída, mas verifique os dados[0m
    echo.
    echo    📝 Pontos no backup:     !PONTOS_NO_BACKUP!
    echo    📝 Pontos restaurados:   !PONTOS_RESTAURADOS!
)

echo.
set /p "OPEN_APP=🚀 Deseja abrir o app agora? (s/n): "
if /i "%OPEN_APP%"=="s" (
    echo.
    echo ⏳ Iniciando app...
    adb shell monkey -p "%PACKAGE%" -c android.intent.category.LAUNCHER 1 >nul 2>&1
    echo [92m✅ App iniciado![0m
)
echo.

goto :eof

:: ═══════════════════════════════════════════════════════════════════════════════
:: COMANDO: LIST
:: ═══════════════════════════════════════════════════════════════════════════════

:cmd_list
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║              📋 BACKUPS DISPONÍVEIS                       ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo    Diretório: %BACKUP_DIR%
echo.

if not exist "%BACKUP_DIR%\*.db" (
    echo    [93mNenhum backup encontrado[0m
    echo.
    echo    Execute: %0 backup
    echo.
    exit /b 0
)

echo    [96m#    Arquivo                        Data                 Tamanho    Img[0m
echo    ──── ────────────────────────────── ──────────────────── ────────   ───

set "COUNT=0"
set "TOTAL_SIZE=0"

for /f "tokens=*" %%F in ('dir /b /o-d "%BACKUP_DIR%\*.db" 2^>nul') do (
    set /a "COUNT+=1"

    :: Obter tamanho
    for %%A in ("%BACKUP_DIR%\%%F") do (
        set /a "SIZE_KB=%%~zA/1024"
        set /a "TOTAL_SIZE+=%%~zA"

        :: Adicionar tamanho das imagens se existir
        set "IMG_FILE=%%~dpnA_images.tar.gz"
        if exist "!IMG_FILE!" (
            for %%I in ("!IMG_FILE!") do set /a "TOTAL_SIZE+=%%~zI"
            set "HAS_IMG=✓"
        ) else (
            set "HAS_IMG=-"
        )
    )

    :: Extrair data do nome
    set "FNAME=%%~nF"
    set "FDATE=!FNAME:~15,2!/!FNAME:~13,2!/!FNAME:~9,4! !FNAME:~18,2!:!FNAME:~20,2!:!FNAME:~22,2!"

    :: Formatar número
    if !COUNT! lss 10 (
        set "NUM_PAD=[!COUNT!] "
    ) else (
        set "NUM_PAD=[!COUNT!]"
    )

    echo    !NUM_PAD! %%F   !FDATE!   !SIZE_KB! KB   !HAS_IMG!
)

set /a "TOTAL_KB=%TOTAL_SIZE%/1024"
if %TOTAL_KB% gtr 1024 (
    set /a "TOTAL_MB=%TOTAL_KB%/1024"
    set "TOTAL_STR=!TOTAL_MB! MB"
) else (
    set "TOTAL_STR=%TOTAL_KB% KB"
)

echo.
echo ═══════════════════════════════════════════════════════════════
echo.
echo    Total: %COUNT% backup(s), %TOTAL_STR%
echo.

goto :eof

:: ═══════════════════════════════════════════════════════════════════════════════
:: COMANDO: HELP
:: ═══════════════════════════════════════════════════════════════════════════════

:cmd_help
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║     📦 MeuPonto - Gerenciador de Backup do Banco          ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo    Uso: %~nx0 ^<comando^> [opções]
echo.
echo    ┌─────────────────────────────────────────────────────────┐
echo    │  Comandos disponíveis:                                  │
echo    ├─────────────────────────────────────────────────────────┤
echo    │  backup      Cria backup do banco e imagens             │
echo    │  restore     Restaura backup (menu ou arquivo direto)   │
│  list        Lista todos os backups disponíveis         │
    └─────────────────────────────────────────────────────────┘
echo.
echo    Exemplos:
echo      %~nx0 backup                         # Criar backup
echo      %~nx0 restore                        # Restaurar (interativo)
echo      %~nx0 restore arquivo.db             # Restaurar arquivo específico
echo      %~nx0 list                           # Ver backups
echo.
echo    Configurações:
echo      Banco:      %DB_NAME%
echo      Imagens:    %IMAGES_PATH%
echo      Backups:    %BACKUP_DIR%
if defined PACKAGE (
    echo      Pacote:     %PACKAGE%
) else (
    echo      Pacote:     (será detectado automaticamente)
)
echo.

goto :eof
