# Terminal Mensagens — App Android

App nativo em Kotlin + Jetpack Compose que captura novas notificações do
**WhatsApp**, **WhatsApp Business** e **Instagram** via
`NotificationListenerService` e envia para a API do Terminal Web.

## Como abrir

1. Abra o Android Studio (Iguana ou mais recente).
2. **File → Open** e selecione a pasta `terminal-mensagens-android`.
3. Aguarde o Gradle sincronizar. O wrapper baixa o Gradle 8.9 automaticamente.
4. Rode no emulador ou aparelho físico (Android 7.0+, `minSdk = 24`).

## Como usar

1. Abra o app.
2. Cole a URL da API (ex.: `https://seu-projeto.lovable.app`) e toque em **Salvar URL**.
3. Toque em **ATIVAR ACESSO ÀS NOTIFICAÇÕES** e ative "Terminal Mensagens" na tela do sistema.
4. Volte ao app — o status muda para 🟢 **Serviço ativo**.
5. Novas notificações do WhatsApp/Instagram aparecem no Terminal Web em tempo real.

## O que o app NÃO faz

- Não envia mensagens.
- Não lê conversas antigas.
- Não abre nem clica em nada.
- Não acessa arquivos internos dos aplicativos monitorados.
- Trabalha apenas com o texto que a própria notificação disponibiliza.

## Endpoints usados

- `POST {URL}/api/public/messages` — envia cada notificação capturada.
- `POST {URL}/api/public/heartbeat` — mantém o status "online" do aparelho.

## Estrutura

```
app/src/main/java/com/terminalmensagens/app/
  MainActivity.kt            # UI Compose (status, botão, URL da API)
  NotificationListener.kt    # NotificationListenerService (WhatsApp/Insta)
  ApiClient.kt               # POST JSON via HttpURLConnection
  DeviceManager.kt           # device_token + URL da API (SharedPreferences)
  HeartbeatWorker.kt         # heartbeat periódico (WorkManager, 15 min)
```
