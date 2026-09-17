# Cavalera Barbearia — APK sem Android Studio

Este projeto transforma o Web App da Cavalera Barbearia em um aplicativo Android
instalável usando uma WebView nativa.

Você NÃO precisa instalar Android Studio, Java, Gradle ou SDK no computador.

O APK é compilado gratuitamente na nuvem com GitHub Actions.

## O que o APK faz

- Abre o Web App publicado no Google Apps Script.
- Mantém JavaScript, `google.script.run`, ADMIN, horários e serviços.
- Mantém cookies e armazenamento do WebView.
- Abre WhatsApp fora do WebView.
- Abre Google Maps fora do WebView.
- Mostra tela de erro e botão de tentar novamente quando não houver conexão.
- Usa apenas HTTPS.
- Funciona a partir do Android 7.0 (API 24).

## MUITO IMPORTANTE

Seu Google Apps Script precisa estar publicado como:

- Tipo: Aplicativo da Web
- Executar como: você
- Quem tem acesso: qualquer pessoa

Copie a URL terminada em:

`/exec`

Exemplo:

`https://script.google.com/macros/s/SEU_ID_AQUI/exec`

Não use a URL `/dev`.

---

# Gerar o APK pelo navegador

## 1. Crie uma conta no GitHub

Acesse:

https://github.com/

## 2. Crie um repositório

Clique em:

New repository

Sugestão:

`cavalera-barbearia-apk`

Para evitar expor os arquivos, você pode usar repositório PRIVATE.

O GitHub Free possui cota gratuita de GitHub Actions para repositórios privados.
Repositórios públicos com runners padrão também podem usar Actions gratuitamente.

## 3. Envie este projeto

Abra o repositório pelo navegador.

Use:

Add file > Upload files

Envie TODOS os arquivos e pastas deste ZIP, preservando a estrutura.

A pasta `.github` precisa existir no repositório.

## 4. Cadastre a URL do seu Apps Script

No repositório:

Settings
> Secrets and variables
> Actions
> Variables
> New repository variable

Nome:

`WEB_APP_URL`

Valor:

a URL `/exec` do seu Apps Script.

Exemplo:

`https://script.google.com/macros/s/SEU_ID/exec`

Salve.

## 5. Gere o APK

Abra:

Actions
> Gerar APK Cavalera
> Run workflow
> Run workflow

A compilação ocorrerá no servidor do GitHub.

## 6. Baixe

Quando a execução ficar verde:

Abra a execução
> Artifacts
> Cavalera-Barbearia-APK

O GitHub baixa um ZIP.

Abra esse ZIP e você encontrará:

`Cavalera-Barbearia.apk`

## 7. Instale no Android

Envie o APK para o celular ou baixe o artifact diretamente nele.

Abra o arquivo `.apk`.

O Android pode pedir autorização para:

"Instalar apps desconhecidos"

Autorize apenas para o navegador/gerenciador de arquivos que você está usando.

Depois instale normalmente.

---

# Atualizações do Web App

O APK NÃO contém uma cópia do `Index.html`.

Ele abre sua implantação do Apps Script.

Isso é proposital.

Se você alterar:

- Codigo.gs
- Index.html
- layout
- horários
- animações
- painel ADMIN

e publicar uma nova versão mantendo a MESMA URL `/exec`,
o APK recebe a atualização sem precisar gerar outro APK.

Você só precisa gerar um novo APK quando quiser alterar código Android,
ícone, permissões, nome do aplicativo ou versão nativa.

---

# WhatsApp

O Android intercepta links:

- `wa.me`
- `api.whatsapp.com`
- `web.whatsapp.com`

e abre fora da WebView.

Se o WhatsApp estiver instalado, o Android normalmente oferece/abre o WhatsApp.
Caso contrário, abre uma alternativa compatível pelo sistema.

Isso evita o problema de tentar abrir o WhatsApp dentro do iframe/WebView.

---

# Google Maps

Links do Google Maps são enviados para fora da WebView.

Assim o usuário pode abrir o Google Maps ou navegador normalmente.

---

# APK de teste x versão final

O workflow atual gera um APK DEBUG.

Ele é instalável e serve perfeitamente para testes e distribuição interna.

Porém o GitHub gera o ambiente de build novamente a cada execução,
então a chave DEBUG pode mudar.

Consequência:

ao gerar outro APK no futuro, o Android pode exigir desinstalar o anterior
antes de instalar o novo.

Quando o projeto estiver finalizado, crie uma assinatura RELEASE permanente.
Isso é necessário para atualizações normais e para publicação profissional.

A assinatura release também pode ser montada inteiramente pelo GitHub Actions,
sem Android Studio.

---

# Arquivos do Web App atual

A pasta:

`webapp-atual`

contém uma cópia da versão atual de:

- Codigo.gs
- Index.html

Ela serve apenas como referência.

O APK usa a URL implantada do Apps Script definida em `WEB_APP_URL`.
