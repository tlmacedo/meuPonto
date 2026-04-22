# 📱 Guia de Utilização: Widget Meu Ponto

O widget do **Meu Ponto** foi projetado para oferecer acesso rápido às suas informações de jornada e permitir o registro de ponto diretamente da tela inicial do seu Android, sem a necessidade de abrir o aplicativo completo.

---

## 🛠 Como Instalar o Widget

1. **Vá para a Tela Inicial:** Pressione e segure em qualquer área vazia da sua tela inicial.
2. **Abra o Menu de Widgets:** Toque no ícone ou opção **"Widgets"** que aparecerá.
3. **Localize o Meu Ponto:** Role a lista até encontrar o aplicativo **"Meu Ponto"**.
4. **Selecione o Widget:** Toque e segure o widget do Meu Ponto (geralmente no tamanho 2x2 ou 4x2).
5. **Posicione:** Arraste-o para o local desejado na sua tela inicial e solte.
6. **Redimensione (Opcional):** Se o seu launcher permitir, você pode ajustar o tamanho do widget para exibir mais ou menos informações.

---

## 📖 Funcionalidades do Widget

O widget exibe informações em tempo real e oferece atalhos práticos:

1. **Resumo de Horas:** Exibe o total de horas trabalhadas no dia atual.
2. **Próximo Ponto:** Indica qual é o próximo registro esperado (ex: "Entrada", "Saída para Intervalo", etc.).
3. **Botão de Registro Rápido:** Um botão que abre o aplicativo diretamente na tela de registro de ponto para o tipo indicado.
4. **Status do Emprego:** Exibe o apelido do emprego que está ativo no momento.
5. **Cores Dinâmicas:** O widget utiliza as cores do seu papel de parede (Material You) em dispositivos com Android 12 ou superior, garantindo uma integração visual perfeita.

---

## 🔄 Atualização de Dados

* **Automática:** O widget se atualiza sempre que você registra um ponto no aplicativo ou quando há mudanças significativas no saldo do dia.
* **Segundo Plano:** Utiliza o `WorkManager` do Android para garantir que as informações estejam sempre sincronizadas, mesmo que o aplicativo não esteja aberto em primeiro plano.

---

## ❓ Solução de Problemas

* **O widget não aparece na lista:** Certifique-se de que o aplicativo está instalado na memória interna do dispositivo (widgets não funcionam se o app estiver no cartão SD).
* **Os dados parecem desatualizados:** Toque no widget para abrir o app; isso forçará uma atualização imediata dos dados.
* **O botão de registro não funciona:** Verifique se o aplicativo possui as permissões necessárias (como localização, se configurado) para realizar o registro.

---
*Dica: Adicione o widget em uma tela de fácil acesso para nunca mais esquecer de registrar seu ponto!*
