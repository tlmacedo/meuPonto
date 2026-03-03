Para obter a **impressão digital SHA-1** do seu certificado de debug (e release), execute os seguintes comandos:

---

## 1. SHA-1 do Certificado de Debug (desenvolvimento)

```bash
# No macOS/Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

```bash
# No Windows
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

---

## 2. Saída esperada

Você verá algo assim:

```
Alias name: androiddebugkey
Creation date: ...
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: C=US, O=Android, CN=Android Debug
Issuer: C=US, O=Android, CN=Android Debug
Serial number: 1
Valid from: ...
Certificate fingerprints:
	 SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD
	 SHA256: ...
```

**Copie o valor do SHA1** (formato `AA:BB:CC:DD:...`)

---

## 3. Via Gradle (alternativa mais fácil)

Execute na raiz do projeto:

```bash
./gradlew signingReport
```

Isso mostrará os SHA-1 de todas as variantes:

```
> Task :app:signingReport
Variant: debug
Config: debug
Store: /Users/thiagomacedo/.android/debug.keystore
Alias: AndroidDebugKey
MD5: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD
SHA-256: ...
Valid until: ...

Variant: release
...
```

---

## 4. Configurar no Google Cloud Console

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Vá em **APIs e Serviços** > **Credenciais**
3. Clique na sua **Chave de API**
4. Em **Restrições de aplicativos**, selecione **Apps Android**
5. Clique em **Adicionar um item**
6. Preencha:
    - **Nome do pacote**: `br.com.tlmacedo.meuponto`
    - **Impressão digital do certificado SHA-1**: Cole o SHA-1 copiado

---

## 5. Para Release (produção)

Quando for publicar na Play Store, você precisará adicionar também o SHA-1 do certificado de release:

```bash
keytool -list -v -keystore /caminho/para/seu/release-keystore.jks -alias seu_alias
```

E se usar **Google Play App Signing**, pegue o SHA-1 no:
- **Google Play Console** > **Seu App** > **Configuração** > **Integridade do app** > **Certificado de assinatura do app**

---

## Resumo rápido

```bash
# Execute isso no terminal do projeto
./gradlew signingReport
```

Copie o **SHA1** da variante **debug** e cole no Google Cloud Console.

Depois de configurar, sua API Key estará restrita apenas ao seu app, aumentando a segurança!