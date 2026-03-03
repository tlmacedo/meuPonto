# Solução de Problema: Permissão Negada do Banco de Dados

## 🚨 Problema

Quando você executa o script de instalação com importação de dados, pode ocorrer o seguinte erro:

```
SQLiteCantOpenDatabaseException: Cannot open database [unable to open database file 
(code 2318 SQLITE_CANTOPEN_EACCES[2318]): Permission denied]
/data/user/0/br.com.tlmacedo.meuponto.debug/databases/meuponto.db
```

## 📋 Causa Raiz

O erro ocorre porque o arquivo de banco de dados tem permissões incorretas após ser copiado para o dispositivo Android. Especificamente:

- O arquivo pertence a um usuário diferente (UID 10000)
- O app roda com outro UID (10383)
- O app não tem permissão para ler/escrever o arquivo

## ✅ Solução Implementada

Os scripts de instalação foram atualizados para corrigir automaticamente as permissões:

### Windows (install-debug.bat)
```batch
adb push database_backup\meuponto.db /sdcard/meuponto_import.db
adb shell run-as %PACKAGE_NAME% cp /sdcard/meuponto_import.db /data/data/%PACKAGE_NAME%/databases/meu_ponto.db

# Ajustar permissões para 664 (leitura/escrita para owner e grupo)
adb shell run-as %PACKAGE_NAME% chmod 664 /data/data/%PACKAGE_NAME%/databases/meu_ponto.db

# Ajustar owner para o package correto
adb shell run-as %PACKAGE_NAME% chown %PACKAGE_NAME%:%PACKAGE_NAME% /data/data/%PACKAGE_NAME%/databases/meu_ponto.db

# Verificar permissões
adb shell run-as %PACKAGE_NAME% ls -la /data/data/%PACKAGE_NAME%/databases/meu_ponto.db
```

### Linux/Mac (install-debug.sh)
```bash
adb push database_backup/meuponto.db /sdcard/meuponto_import.db
adb shell run-as $PACKAGE_NAME cp /sdcard/meuponto_import.db /data/data/$PACKAGE_NAME/databases/meu_ponto.db

# Ajustar permissões para 664 (leitura/escrita para owner e grupo)
adb shell run-as $PACKAGE_NAME chmod 664 /data/data/$PACKAGE_NAME/databases/meu_ponto.db

# Ajustar owner para o package correto
adb shell run-as $PACKAGE_NAME chown $PACKAGE_NAME:$PACKAGE_NAME /data/data/$PACKAGE_NAME/databases/meu_ponto.db

# Verificar permissões
adb shell run-as $PACKAGE_NAME ls -la /data/data/$PACKAGE_NAME/databases/meu_ponto.db
```

## 🔧 Se o Problema Persistir

Se mesmo após as correções o erro continuar, siga estes passos:

### Método 1: Limpar Dados do App

1. **Desinstale o app:**
   ```bash
   adb uninstall br.com.tlmacedo.meuponto.debug
   ```

2. **Reinstale o app:**
   ```bash
   # Windows
   install-debug.bat
   
   # Linux/Mac
   ./install-debug.sh
   ```

3. **Se não quiser importar dados**, responda **N** quando perguntado sobre importação histórica

### Método 2: Recriar Banco de Dados Manualmente

1. **Force stop do app:**
   ```bash
   adb shell am force-stop br.com.tlmacedo.meuponto.debug
   ```

2. **Delete o banco de dados corrompido:**
   ```bash
   adb shell run-as br.com.tlmacedo.meuponto.debug rm /data/data/br.com.tlmacedo.meuponto.debug/databases/meu_ponto.db
   ```

3. **Execute o script de instalação novamente:**
   ```bash
   # Windows
   install-debug.bat
   
   # Linux/Mac
   ./install-debug.sh
   ```

### Método 3: Verificar Permissões Atuais

1. **Conecte ao dispositivo:**
   ```bash
   adb shell
   ```

2. **Mude para o contexto do app:**
   ```bash
   run-as br.com.tlmacedo.meuponto.debug
   ```

3. **Verifique permissões do banco:**
   ```bash
   ls -la /data/data/br.com.tlmacedo.meuponto.debug/databases/
   ```

4. **Saída esperada:**
   ```
   -rw-rw-r-- 1 u0_a383 u0_a383 135168 2026-03-02 16:04 meu_ponto.db
   ```

   Se as permissões não forem `-rw-rw-r--` ou o owner não for `u0_a383`, corrija:

5. **Corrigir permissões manualmente:**
   ```bash
   chmod 664 /data/data/br.com.tlmacedo.meuponto.debug/databases/meu_ponto.db
   chown u0_a383:u0_a383 /data/data/br.com.tlmacedo.meuponto.debug/databases/meu_ponto.db
   ```

6. **Saia do contexto:**
   ```bash
   exit
   exit
   ```

### Método 4: Apenas Limpar Dados (Manter Banco Vazio)

Se você não quiser importar dados históricos e apenas criar um banco vazio:

1. **Abra as configurações do app no dispositivo**

2. **Vá em "Armazenamento" ou "Dados"**

3. **Toque em "Limpar dados"**

4. **Reinicie o app**

O app criará um novo banco de dados vazio com as permissões corretas.

## 📝 Permissões Corretas

As permissões corretas para o banco de dados são:

### Permissões de Arquivo
- **644** ou **664** = `rw-r--r--` ou `rw-rw-r--`
- Owner: Leitura e Escrita
- Group: Leitura (opcionalmente Escrita)
- Others: Leitura

### Owner e Group
- **Owner:** O UID do app (ex: `u0_a383` = UID 10383)
- **Group:** O GID do app (ex: `u0_a383`)

### Exemplo de Saída Correta
```
-rw-rw-r-- 1 u0_a383 u0_a383 135168 2026-03-02 16:04 meu_ponto.db
-rw-rw-r-- 1 u0_a383 u0_a383  32768 2026-03-02 16:04 meu_ponto.db-shm
-rw-rw-r-- 1 u0_a383 u0_a383      0 2026-03-02 16:04 meu_ponto.db-wal
```

## 🐛 Debug Avançado

Se precisar de mais informações sobre o erro:

1. **Ver logs do sistema Android:**
   ```bash
   adb logcat | grep -i sqlite
   adb logcat | grep -i permission
   ```

2. **Ver se há SELinux bloqueando:**
   ```bash
   adb shell getenforce
   # Se mostrar "Enforcing", pode ser necessário ajustar políticas
   ```

3. **Ver contexto SELinux do arquivo:**
   ```bash
   adb shell ls -Z /data/data/br.com.tlmacedo.meuponto.debug/databases/meu_ponto.db
   ```

## 💡 Dicas Preventivas

### Durante Desenvolvimento
- Sempre use `adb shell run-as` para manipular arquivos do app
- Nunca copie arquivos diretamente para `/data/data/` sem ajustar permissões
- Use `/sdcard/` como área temporária para copiar arquivos

### Após Importação de Dados
- Sempre verifique as permissões após copiar o banco
- Use `ls -la` para confirmar permissões antes de abrir o app
- Mantenha backups em `database_backup/` caso precise restaurar

### Antes de Push
- Teste a importação em um emulador primeiro
- Verifique se o dispositivo é debuggable
- Confirme que o app tem permissão `android:debuggable="true"` no AndroidManifest.xml

## 📞 Suporte

Se após seguir todos os passos o problema persistir:

1. Cole o log completo do erro (da seção "SQLiteLog" e "SQLiteDatabase")
2. Informe qual método você tentou
3. Informe qual dispositivo e versão do Android você está usando
4. Cole a saída do comando `adb shell run-as br.com.tlmacedo.meuponto.debug ls -la /data/data/br.com.tlmacedo.meuponto.debug/databases/`

---

**Última atualização:** 02/03/2026
**Versão:** 1.0
**Autor:** Thiago Macedo