@echo off
setlocal enabledelayedexpansion

REM Arquivo: db_backup.bat
REM Backup e Restore do banco de dados MeuPonto

set PACKAGE_RELEASE=br.com.tlmacedo.meuponto
set PACKAGE_DEBUG=br.com.tlmacedo.meuponto.debug
set DB_NAME=meuponto.db
set BACKUP_DIR=backup_meuponto

REM Gerar timestamp
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set TIMESTAMP=%datetime:~0,8%_%datetime:~8,6%

REM Detectar pacote instalado
set PACKAGE=
adb shell "run-as %PACKAGE_DEBUG% ls databases/%DB_NAME%" >nul 2>&1
if !errorlevel! equ 0 (
    set PACKAGE=%PACKAGE_DEBUG%
    set MAIN_ACTIVITY=%PACKAGE_DEBUG%/br.com.tlmacedo.meuponto.MainActivity
) else (
    adb shell "run-as %PACKAGE_RELEASE% ls databases/%DB_NAME%" >nul 2>&1
    if !errorlevel! equ 0 (
        set PACKAGE=%PACKAGE_RELEASE%
        set MAIN_ACTIVITY=%PACKAGE_RELEASE%/.MainActivity
    )
)

if "%PACKAGE%"=="" (
    if not "%1"=="" (
        echo.
        echo [ERRO] Nenhum pacote MeuPonto encontrado no dispositivo
        echo        Verifique se o app esta instalado e execute-o pelo menos uma vez
        goto :end
    )
)

if "%1"=="" goto :usage
if "%1"=="backup" goto :backup
if "%1"=="restore" goto :restore
if "%1"=="list" goto :list
if "%1"=="check" goto :check
if "%1"=="info" goto :info
goto :usage

:backup
echo.
echo [BACKUP] Fazendo backup do banco de dados...
echo          Pacote: %PACKAGE%
echo.

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

set BACKUP_FILE=%BACKUP_DIR%\meuponto_%TIMESTAMP%.db

REM Checkpoint WAL antes do backup
adb shell "run-as %PACKAGE% sqlite3 databases/%DB_NAME% 'PRAGMA wal_checkpoint(TRUNCATE);'" 2>nul

adb shell "run-as %PACKAGE% cat databases/%DB_NAME%" > "%BACKUP_FILE%"

for %%A in ("%BACKUP_FILE%") do set SIZE=%%~zA

if !SIZE! GTR 1000 (
    echo [OK] Backup salvo: %BACKUP_FILE% ^(!SIZE! bytes^)
) else (
    echo [ERRO] Arquivo muito pequeno ^(!SIZE! bytes^). Backup falhou.
    del "%BACKUP_FILE%" 2>nul
)
goto :end

:restore
if "%2"=="" (
    echo.
    echo [ERRO] Especifique o arquivo de backup.
    echo.
    echo Uso: %0 restore ^<arquivo.db^>
    echo.
    goto :list
)

if not exist "%2" (
    echo.
    echo [ERRO] Arquivo nao encontrado: %2
    goto :end
)

echo.
echo [RESTORE] Restaurando banco de dados...
echo           Pacote: %PACKAGE%
echo           Arquivo: %2
echo.

echo [1/6] Parando app...
adb shell am force-stop %PACKAGE%
timeout /t 1 >nul

echo [2/6] Enviando backup para dispositivo...
adb push "%2" /data/local/tmp/restore.db

echo [3/6] Removendo arquivos WAL antigos...
adb shell "run-as %PACKAGE% rm -f databases/%DB_NAME%-shm databases/%DB_NAME%-wal"

echo [4/6] Copiando banco...
adb shell "cat /data/local/tmp/restore.db | run-as %PACKAGE% sh -c 'cat > databases/%DB_NAME%'"

echo [5/6] Ajustando permissoes...
adb shell "run-as %PACKAGE% chmod 660 databases/%DB_NAME%"

echo [6/6] Limpando arquivos temporarios...
adb shell rm /data/local/tmp/restore.db

echo.
echo [OK] Restauracao concluida!
echo.

set /p OPEN_APP="Deseja abrir o app? (S/N): "
if /i "%OPEN_APP%"=="S" (
    adb shell am start -n %MAIN_ACTIVITY%
)
goto :end

:list
echo.
echo [LIST] Backups disponiveis em %BACKUP_DIR%:
echo.
if exist "%BACKUP_DIR%\*.db" (
    echo -----------------------------------------------
    for %%F in ("%BACKUP_DIR%\*.db") do (
        echo   %%~nxF  ^(%%~zF bytes^)
    )
    echo -----------------------------------------------
) else (
    echo Nenhum backup encontrado
)
echo.
goto :end

:check
echo.
echo [CHECK] Verificando banco no dispositivo...
echo         Pacote: %PACKAGE%
echo.
adb shell "run-as %PACKAGE% ls -la databases/"
goto :end

:info
echo.
echo [INFO] Informacoes do banco...
echo        Pacote: %PACKAGE%
echo.
echo Tabelas:
adb shell "run-as %PACKAGE% sqlite3 databases/%DB_NAME% '.tables'"
echo.
echo Contagem de registros:
for %%T in (emprego configuracao_emprego versao_jornada ponto) do (
    for /f %%C in ('adb shell "run-as %PACKAGE% sqlite3 databases/%DB_NAME% 'SELECT COUNT(*) FROM %%T;'" 2^>nul') do (
        echo   %%T: %%C
    )
)
goto :end

:usage
echo.
echo ===================================
echo   MeuPonto - Backup de Banco
echo ===================================
echo.
if not "%PACKAGE%"=="" (
    echo Pacote detectado: %PACKAGE%
) else (
    echo Pacote detectado: Nenhum
)
echo.
echo Uso: %0 [comando]
echo.
echo Comandos:
echo   backup              Cria backup do banco
echo   restore ^<arquivo^>   Restaura um backup
echo   list                Lista backups disponiveis
echo   check               Verifica banco no dispositivo
echo   info                Mostra informacoes do banco
echo.
echo Exemplos:
echo   %0 backup
echo   %0 restore backup_meuponto\meuponto_20260303_220000.db
echo   %0 list
echo.
goto :end

:end
endlocal
