# Guia de Importação de Dados Históricos

Este guia explica como importar dados históricos de espelhos de ponto para o app MeuPonto.

## 📁 Arquivos de Importação

### 1. **importacao-historica.json**
Arquivo JSON com todos os dados históricos extraídos dos espelhos de ponto.

**Contém:**
- Dados do emprego (SIDIA)
- 2 versões de jornada (2025 e 2026)
- 122 dias úteis com registros de ponto (~480 batidas)
- 8 feriados nacionais
- 10 ausências (Férias, Folgas, Compensados, etc.)

### 2. **importacao-historica.sql**
Arquivo SQL com comandos prontos para inserir no banco de dados do app.

**Contém:**
- INSERT statements para todas as tabelas
- Ordem correta de execução (dependências respeitadas)
- IDs pré-definidos para evitar conflitos

### 3. **gerar-sql-importacao.py**
Script Python para regenerar o arquivo SQL a partir do JSON.

## 🚀 Como Importar os Dados

### Método 1: Importação Direta via SQL (Recomendado)

#### Pré-requisitos
1. **DB Browser for SQLite** (ou similar)
   - Download: https://sqlitebrowser.org/

#### Passos

1. **Localizar o banco de dados do app:**
   ```
   /data/data/br.com.tlmacedo.meuponto/databases/meu_ponto.db
   ```
   
   Para acessar, você precisa de um dispositivo com root ou usar o Android Studio Device File Explorer.

2. **Fazer backup do banco de dados atual:**
   - Copie o arquivo `meu_ponto.db` para uma pasta segura
   - Mantenha uma cópia de segurança caso algo dê errado

3. **Abrir o banco de dados:**
   - Abra o DB Browser for SQLite
   - File → Open Database → selecione `meu_ponto.db`

4. **Executar o script SQL:**
   - Vá na aba "Execute SQL"
   - File → Open → `importacao-historica.sql` (em `docs/dados/`)
   - Clique em "Execute" (F5)
   - Verifique se não houve erros na aba "Database Structure"

5. **Verificar os dados importados:**
   - Abra a aba "Browse Data"
   - Verifique as tabelas:
     - `Emprego` → Deve ter 1 registro
     - `VersaoJornada` → Deve ter 2 registros
     - `HorarioDiaSemana` → Deve ter 14 registros
     - `Feriado` → Deve ter 8 registros
     - `Ausencia` → Deve ter 10 registros
     - `Ponto` → Deve ter ~480 registros

6. **Reiniciar o app:**
   - Force stop do app MeuPonto
   - Abra novamente
   - Verifique se os dados aparecem corretamente

### Método 2: Regenerar o SQL (se precisar modificar os dados)

Se precisar modificar os dados antes de importar:

1. **Edite o arquivo JSON:**
   - Abra `docs/dados/importacao-historica.json`
   - Faça as alterações necessárias
   - Salve o arquivo

2. **Regenere o SQL:**
   ```bash
   cd docs/scripts
   python gerar-sql-importacao.py
   ```

3. **Siga os passos do Método 1** para importar o SQL gerado

## 📊 Estrutura dos Dados Importados

### Emprego
- **Nome:** SIDIA INSTITUTO DE CIENCIA E TECNOLOGIA
- **Descrição:** DESENVOLVEDOR DE SW III
- **Data Início:** 11/08/2025

### Versões de Jornada

#### Jornada 2025 (11/08/2025 a 31/12/2025)
- **Carga horária diária:** 8h10min (490min)
- **Carga horária semanal:** 40h50min (2450min)
- **Horários:**
  - Segunda a Sexta: 08:00 → 12:30 (almoco) → 13:30 → 17:10
  - Sábado/Domingo: Sem jornada

#### Jornada 2026 (01/01/2026 em diante)
- **Carga horária diária:** 8h12min (492min)
- **Carga horária semanal:** 40h50min (2450min)
- **Horários:**
  - Segunda a Sexta: 08:00 → 12:30 (almoco) → 13:30 → 17:12
  - Sábado/Domingo: Sem jornada

### Feriados Importados
1. Dia Nossa Sra. Aparecida - 12/10/2025
2. Proclamação da República - 15/10/2025
3. Aniversário de Manaus - 24/10/2025
4. Finados - 02/11/2025
5. Dia da Consciência Negra - 15/11/2025
6. N. Sra. Conceição - 08/12/2025
7. Natal - 24 a 26/12/2025
8. Ano Novo - 31/12/2025 a 02/01/2026

### Ausências Importadas
- **Férias:** 22 a 26/09/2025 (5 dias)
- **Folgas Compensadas/DSR:** 9 dias (fevereiro 2026)
- **Carnaval:** 16 a 18/02/2026 (3 dias)

## ⚠️ Observações Importantes

### Antes de Importar
- **Sempre faça backup** do banco de dados atual
- Verifique se o app está fechado antes de modificar o banco
- Certifique-se de que a versão do app é compatível com a estrutura dos dados

### Após Importar
- Os saldos serão recalculados automaticamente pelo app
- Verifique se todos os dias aparecem corretamente no histórico
- Confirme se os feriados e ausências estão sendo exibidos corretamente

### Tolerância de Saída
A tolerância de saída está configurada para **20 minutos** nas versões de jornada importadas. Isso significa:
- Se você sair até 20 minutos após o horário ideal, será considerado no horário
- Exemplo: Sair às 17:30 em vez de 17:10 (jornada 2025) → considerado como 17:10

## 🔧 Solução de Problemas

### Erro: "Table already exists"
- Solução: Limpe o banco de dados antes de importar
- No DB Browser: Tools → Vacuum Database

### Erro: "UNIQUE constraint failed"
- Solução: Verifique se já existe um emprego cadastrado com ID=1
- Se necessário, altere o ID no arquivo SQL ou limpe a tabela Emprego

### Dados não aparecem no app
- Verifique se executou TODOS os comandos SQL em ordem
- Reinicie o app completamente (force stop + abrir)
- Verifique os logs do Android Studio para erros

### Saldos incorretos após importação
- O app recalcula os saldos automaticamente
- Se ainda estiver incorreto, verifique:
  - As configurações de jornada
  - Os horários de cada dia da semana
  - As tolerâncias configuradas

## 📝 Personalização

### Para adicionar mais dias de ponto
1. Edite `importacao-historica.json`
2. Adicione novos registros em `pontos`
3. Execute o script Python para regenerar o SQL
4. Importe o SQL gerado

### Para modificar jornada existente
1. Edite `importacao-historica.json`
2. Altere os dados em `versoesJornada`
3. Execute o script Python para regenerar o SQL
4. Importe o SQL gerado

### Para adicionar novos feriados
1. Edite `importacao-historica.json`
2. Adicione novos registros em `feriados`
3. Execute o script Python para regenerar o SQL
4. Importe o SQL gerado

## 📞 Suporte

Se encontrar problemas durante a importação:
1. Verifique os logs do Android Studio
2. Consulte a documentação do app
3. Verifique se a estrutura do banco de dados está compatível

---

**Última atualização:** 02/03/2026
**Versão:** 1.0
**Autor:** Thiago Macedo