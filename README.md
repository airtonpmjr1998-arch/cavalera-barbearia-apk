# Cavalera Barbearia — APK online sem Android Studio

Este projeto gera um APK Android da Cavalera Barbearia usando GitHub Actions.

Você não precisa instalar:
- Android Studio
- Java
- Gradle
- Android SDK

O GitHub compila tudo online.

## Antes de começar

Publique seu Google Apps Script como Aplicativo da Web.

Use a URL que termina em:

`/exec`

Exemplo:

`https://script.google.com/macros/s/SEU_ID/exec`

## Estrutura que deve aparecer na raiz do GitHub

```text
.github/
app/
webapp-atual/
.gitignore
README.md
build.gradle
gradle.properties
settings.gradle
```

## 1. Enviar os arquivos para o GitHub

Crie ou abra o repositório:

`cavalera-barbearia-apk`

Na aba Code:

`Add file > Upload files`

Envie o conteúdo deste ZIP preservando as pastas.

## 2. Conferir o workflow

Confirme que existe:

`.github/workflows/build-apk.yml`

## 3. Criar a variável WEB_APP_URL

No repositório:

`Settings > Secrets and variables > Actions > Variables`

Clique:

`New repository variable`

Nome:

`WEB_APP_URL`

Valor:

a URL `/exec` do seu Apps Script.

## 4. Gerar o APK

Vá em:

`Actions > Gerar APK Cavalera`

Clique:

`Run workflow > Run workflow`

Aguarde o build ficar verde.

## 5. Baixar

Abra a execução concluída.

Na seção Artifacts clique:

`Cavalera-Barbearia-APK`

Dentro do ZIP estará:

`Cavalera-Barbearia.apk`

## 6. Instalar

Abra o APK no Android.

Pode ser necessário autorizar temporariamente "Instalar apps desconhecidos"
para o navegador ou gerenciador de arquivos usado.

## Atualizações

O APK abre a sua URL publicada do Apps Script.

Se você atualizar `Codigo.gs` ou `Index.html` e publicar mantendo a mesma URL `/exec`,
o app recebe a nova versão do site sem precisar recompilar o APK.

Você recompila o APK somente quando alterar código Android, ícone, nome,
permissões ou outros recursos nativos.

## Observação

Este workflow gera um APK DEBUG, ideal para testes e instalação direta.

Para distribuição definitiva e atualizações sem desinstalar o app anterior,
o próximo passo é configurar um APK/AAB RELEASE com assinatura permanente.
