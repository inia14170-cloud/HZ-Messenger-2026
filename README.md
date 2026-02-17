# HZ Messenger (TDLib + Compose)

Минимальный Telegram-клиент на TDLib с UI на Jetpack Compose (Material 3):

- Вход по номеру телефона → код → (если нужно) пароль 2FA
- Список чатов
- Экран чата и отправка текстовых сообщений
- Сборка APK на GitHub Actions и раздача через Releases/Artifacts

## Настройка ключей Telegram

В GitHub Repo → **Settings → Secrets and variables → Actions** добавь:

- `TG_API_ID`
- `TG_API_HASH`

Ключи берутся на `my.telegram.org/apps`.

## Сборка APK

Пуш в `main` запускает GitHub Actions workflow.
Скачивай APK из **Actions → Artifacts** или из **Releases**.

> Сборка по умолчанию делает `assembleDebug`, чтобы APK устанавливался без keystore.
