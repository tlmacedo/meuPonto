# Guia de Instalação com Importação de Dados Históricos

Este guia explica como instalar o app MeuPonto e opcionalmente importar dados históricos automaticamente.

## 📁 Scripts de Instalação

O projeto inclui dois scripts de instalação automatizada:

- **Windows:** `install-debug.bat`
- **Linux/Mac:** `install-debug.sh`

## 🚀 Como Usar

### Windows

1. Conecte seu dispositivo Android via USB
2. Abra o terminal/prompt de comando na raiz do projeto
3. Execute:
   ```cmd
   install-debug.bat
   ```

### Linux/Mac

1. Conecte seu dispositivo Android via USB
2. Abra o terminal na raiz do projeto
3. Dê permissão de execução (se necessário):
   ```bash
   chmod +x install-debug.sh
   ```
4. Execute:
   ```bash
   ./install-debug.sh
   ```

## 📋 O Que os Scripts Fazem

### 1. Configuração do Ambiente
- Configura JAVA_HOME automaticamente
- Detecta a versão correta do Java no sistema

### 2. Limpeza de Cache
- Remove caches do Gradle para evitar erros

### 3. Compilação e Instalação
- Executa `gradlew clean installDebug`
- Compila e instalaa o APK de debug no dispositivo

### 4. Importação de Dados Históricos (Opcional)
Após a instalação, o script pergunta:
```
Deseja importar os dados historicos (122 dias de ponto, feriados, ausencias)?
ATENCAO: Isso ira LIMPAR o banco de dados atual e substituir pelos dados do arquivo.
Deseja importar dados historicos? (S/N):
```

#### Se você escolher **S** (Sim):

1. **Gera SQL a partir do JSON**
   - Executa `docs/scripts/gerar-sql-importacao.py`
   - Converte `docs/dados/importacao-historica.json` em SQL

2. **Para o App**
   - Força o stop do app MeuPonto
   - Libera o banco de dados para manipulação

3. **Faz Backup**
   - Copia o banco de dados atual para `database_backup/`
   - Nome do arquivo: `meuponto_backup_YYYYMMDD_HHMMSS.db`

4. **Limpa Dados Atuais**
   - Deleta todos os pontos, ausências, feriados, horários e versões de jornada
   - Mantém apenas a estrutura do banco de dados

5. **Importa Novos Dados**
   - Executa `docs/dados/importacao-historica.sql`
   - Insere 122 dias de ponto, 8 feriados, 10 ausências, etc.

6. **Atualiza Banco no Dispositivo**
   - Copia o banco atualizado para o dispositivo Android
   - Ajusta as permissões corretamente

7. **Verifica Importação**
   - Mostra a contagem de registros importados:
     - Empregos: 1
     - Versões Jornada: 2
     - Feriados: 8
     - Ausências: 10
     - Pontos: ~502

#### Se você escolher **N** (Não):

- O banco de dados atual permanece inalterado
- O app é instalado com os dados existentes

### 5. Abre o App
- Abre automaticamente o app MeuPonto no dispositivo
- Se não for possível abrir, mostra instruções para abrir manualmente

## ✅ Requisitos

### Para Compilação
- **Java JDK 21** ou superior
- **Android SDK** configurado
- **Gradle** (incluído no projeto)
- **ADB** (Android Debug Bridge)

### Para Importação de Dados
- **Python 3** (para executar o script de geração de SQL)
- **sqlite3** (CLI do SQLite)

#### Instalando sqlite3

**Windows:**
```cmd
# Baixe o sqlite3.exe de: https://sqlite.org/download.html
# Extraia e coloque em uma das pastas do PATH ou em database_backup/
```

**Ubuntu/Debian:**
```bash
sudo apt-get install sqlite3
```

**macOS:**
```bash
brew install sqlite
```

**Fedora:**
```bash
sudo dnf install sqlite
```

## 📊 Dados Importados

Quando você escolhe importar os dados históricos, serão importados:

### Emprego
- **Nome:** SIDIA INSTITUTO DE CIENCIA E TECNOLOGIA
- **Descrição:** DESENVOLVEDOR DE SW III
- **Data Início:** 11/08/2025

### Versões de Jornada
- **Jornada 2025:** 11/08/2025 a 31/12/2025
  - Carga: 8h10min/dia (490min)
  - Horários: 08:00 → 12:30 → 13:30 → 17:10
- **Jornada 2026:** 01/01/2026 em diante
  - Carga: 8h12min/dia (492min)
  - Horários: 08:00 → 12:30 → 13:30 → 17:12

### Horários por Dia da Semana
- 14 registros (7 dias × 2 versões de jornada)
- Configurações completas de entrada/saída/intervalos/tolerâncias

### Feriados (8 registros)
- Dia Nossa Sra. Aparecida - 12/10/2025
- Proclamação da República - 15/10/2025
- Aniversário de Manaus - 24/10/2025
- Finados - 02/11/2025
- Dia da Consciência Negra - 15/11/2025
- N. Sra. Conceição - 08/12/2025
- Natal - 24/12/2025
- Ano Novo - 31/12/2025

### Ausências (10 registros)
- Férias: 22 a 26/09/2025 (5 dias)
- Folgas Compensadas/DSR/Carnaval: Fevereiro 2026 (10 dias)

### Pontos (~502 registros)
- 122 dias úteis com registros de ponto
- Período: 11/08/2025 a 27/02/2026
- ~480 batidas de ponto (média de 4 por dia)

## 🔧 Solução de Problemas

### Erro: "sqlite3 não encontrado"
- **Solução:** Instale sqlite3 conforme instruções na seção "Requisitos"
- **Alternativa:** Coloque `sqlite3.exe` em `database_backup/`

### Erro: "ADB device not found"
- **Solução:**
  1. Verifique se o dispositivo está conectado via USB
  2. Habilite "Depuração USB" nas opções do desenvolvedor
  3. Execute `adb devices` para verificar a conexão

### Erro: "Python não encontrado"
- **Solução:** Instale Python 3 em https://www.python.org/downloads/

### Erro: "Falha ao importar dados SQL"
- **Solução:**
  1. Verifique se o arquivo `docs/dados/importacao-historica.sql` existe
  2. Execute manualmente `python docs/scripts/gerar-sql-importacao.py`
  3. Verifique se não há erros na geração do SQL

### Erro: "Falha ao copiar banco para o dispositivo"
- **Solução:**
  1. Verifique se o dispositivo tem permissão de root ou é debuggable
  2. Tente copiar manualmente usando ADB:
     ```bash
     adb shell run-as br.com.tlmacedo.meuponto
     ```

## 📝 Fluxo Completo

```
┌─────────────────────────────────────┐
│ 1. Conectar dispositivo Android     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│ 2. Executar install-debug.bat/.sh  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│ 3. Compilar e instalar APK         │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│ 4. Perguntar sobre importação?     │
│    [S] Sim  [N] Não                │
└──────┬──────────────────┬───────────┘
       │                  │
   [S] │              [N] │
       │                  │
┌──────▼──────────┐  ┌────▼─────────────┐
│ 5. Gerar SQL    │  │ 6. Abrir app    │
│    do JSON      │  │    com dados    │
│                 │  │    originais    │
└──────┬──────────┘  └─────────────────┘
       │
┌──────▼──────────┐
│ 7. Fazer backup │
│    do banco     │
└──────┬──────────┘
       │
┌──────▼──────────┐
│ 8. Limpar dados │
│    existentes   │
└──────┬──────────┘
       │
┌──────▼──────────┐
│ 9. Importar SQL │
└──────┬──────────┘
       │
┌──────▼──────────┐
│ 10. Copiar banco│
│     para device │
└──────┬──────────┘
       │
┌──────▼──────────┐
│ 11. Abrir app   │
│    com novos    │
│    dados        │
└─────────────────┘
```

## 🎯 Dicas

### Primeira Instalação
- Use a opção de importação para começar com dados de teste
- Isso permite validar o app com dados reais

### Desenvolvimento
- Use a opção de importação apenas quando precisar resetar os dados
- Para manter seus dados, escolha "N" na pergunta de importação

### Testes
- O backup automático permite restaurar dados anteriores
- Todos os backups ficam em `database_backup/`

## 📞 Suporte

Se encontrar problemas:
1. Verifique os requisitos e pré-requisitos
2. Consulte a documentação em `docs/IMPORTACAO_LEIAME.md`
3. Verifique os logs do script para mensagens de erro

---

**Última atualização:** 02/03/2026
**Versão:** 1.0
**Autor:** Thiago Macedo