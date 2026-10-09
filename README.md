# TiredVPN Android

[![CI](https://github.com/igor04091968/tiredvpn-android/actions/workflows/ci.yml/badge.svg)](https://github.com/igor04091968/tiredvpn-android/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/igor04091968/tiredvpn-android)](https://github.com/igor04091968/tiredvpn-android/releases)
[![License: AGPL-3.0](https://img.shields.io/badge/License-AGPL--3.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://developer.android.com/about/versions/nougat)

![TiredVPN](img/github.png)

Android client for TiredVPN - a DPI-resistant VPN designed to operate reliably in censored network environments.

## Имитация TLS-профиля ГОСТ

В форке объединены REALITY Single Flight и ГОСТ TLS 1.3. Для имитации клиентского TLS-профиля записали хэндшейки CryptoPro CSP 5.0 R4 и по ним изменили ClientHello: порядок шифров и расширений, группы, алгоритмы подписи, версию TLS-записи и два key share. При каждом подключении клиент создаёт новые случайные значения и ключи; профиль формируется до вычисления хэша транскрипта.

Соединение использует ГОСТ TLS 1.3. Клиент проверяет SHA-256 pin и срок действия сертификата сервера и отклоняет выбор AES. На Android сокет защищается через VpnService.protect до TCP connect; в логах видны адрес подключения, этап TLS и результат проверки pin.

Профиль приближен к CryptoPro. Post-handshake authentication и PSK-only resumption не реализованы, поэтому TLS-отпечатки различаются. Автотесты проверяют структуру ClientHello, свежие ключи, HelloRetryRequest, отказ от AES и защиту сокета. Передача данных проверена на двух серверах; работу нового APK через МТС и устойчивость к фильтрации ещё предстоит проверить.

В APK [1.12.1-igor.3](https://github.com/igor04091968/tiredvpn-android/releases/tag/v1.12.1-igor.3) встроено ядро [1.12.2-igor.3](https://github.com/igor04091968/tiredvpn/releases/tag/v1.12.2-igor.3). VersionCode 30, прежняя подпись: приложение устанавливается поверх предыдущей версии форка. Минимальная версия Android — 7.0 (API 24). Пакет `com.igor04091968.tiredvpn` устанавливается отдельно от upstream.

Профили `tired://` поддерживают параметры `gostPin` и `gostPort`; стандартный отдельный порт ГОСТ — 12444. [Подробности реализации](https://github.com/igor04091968/tiredvpn/blob/main/docs/gost-cryptopro-clienthello.md).

## Материалы и компоненты сборки

| Источник | Как использован в этой версии |
| --- | --- |
| [TiredVPN](https://github.com/tiredvpn/tiredvpn) и [TiredVPN Android](https://github.com/tiredvpn/tiredvpn-android) | Исходное ядро, сервер, VPN-сервис Android, интерфейс и существующие сетевые стратегии. Изменения форка опубликованы в этих репозиториях с сохранением лицензий. |
| [gogost v3.0.0](https://gitverse.ru/uzer_007/gogost) | Библиотека ГОСТ и TLS в ядре. Локальная копия дополнена профилем ClientHello и проверками; исходные MIT/BSD-лицензии сохранены. |
| [CryptoPro CSP 5.0 R4 и TLS с ГОСТ](https://cryptopro.ru/products/csp/tls) | CSP 5.0.13820-7 и curl 8.21.0-CPRO использовали в лаборатории для записи собственных образцов TLS 1.3. По захватам настроены параметры ClientHello. APK и сервер работают на Go-библиотеке; CryptoPro нужен был для получения образца. |
| [RFC 9367](https://www.rfc-editor.org/rfc/rfc9367.html) | Описание криптонаборов ГОСТ для TLS 1.3, с которым сопоставляли реализацию транспорта. |
| [«О схеме ограничений РКН в июне 2026-го»](https://habr.com/ru/articles/1044396/) | Наблюдения о сериях TLS-хэндшейков использованы при разработке экспериментальной REALITY Single Flight. |
| [DPI-CH / dpi-checkers](https://github.com/hyperion-cs/dpi-checkers/tree/main/ru/dpi-ch/docs) | Материалы для планирования сетевой диагностики и проверки гипотез о фильтрации. |
| [TSPU docs](https://github.com/DanielLavrushin/tspu-docs) | Справочный материал по устройству ТСПУ и возможным признакам сетевых отказов. Автор предупреждает о возможных неточностях конспекта. |

DPI-CH и TSPU docs использовали при исследовании сети. Конкретный профиль ГОСТ построен по собственным захватам CryptoPro; REALITY Single Flight — по наблюдениям из указанной статьи. Результаты проверки связи не подтверждают преимущество при активной фильтрации.

Локальная сборка APK: Go 1.27.1, Java 21, Gradle 9.6.1, Android SDK platform 37, build-tools 36.0.0 и NDK 27.2.12479018. Нативные библиотеки собраны для arm64-v8a, armeabi-v7a и x86_64. APK подписан ключом форка и проверен на выравнивание 16 КБ.

**Related repository:** [igor04091968/tiredvpn](https://github.com/igor04091968/tiredvpn) — Go server and CLI client

## What is it

TiredVPN Android is the mobile client for the [TiredVPN](https://github.com/tiredvpn/tiredvpn) tunnel system. It embeds the Go VPN core as a native shared library (via JNI) and wraps it in a full-featured Android VPN service. The app automatically selects the best bypass strategy for the current network conditions, reconnects after disruptions, and keeps the tunnel alive in the background.

## Features

- **Multiple DPI bypass strategies** - the Go core ships with 20+ strategies (REALITY, Seqovl sequence overlap, QUIC Salamander, HTTP/2 Stego, WebSocket Padded, Traffic Morphing, Protocol Confusion, and more). The client rotates through them automatically when one is blocked, and any strategy can be forced from Settings.
- **Smart auto-reconnect** - survives airplane mode toggles, network switches (Wi-Fi / mobile), and device sleep. A WorkManager-based watchdog ensures the tunnel restarts if the service is killed.
- **Persistent VPN notification** - foreground service with real-time status, latency, and data counters.
- **Split tunneling** - per-app routing: choose which apps go through the tunnel and which use the direct connection.
- **Port hopping** - dynamically switches server ports to evade port-based blocking.
- **Auto-update system** - checks for new versions, downloads APKs, and prompts for installation.
- **Deep link & QR import** - configure servers via `tired://` URI scheme or QR code scan.
- **ADB control** - connect, disconnect, and import configs via broadcast intents (useful for Android TV and headless setups).
- **Boot auto-start** - optionally connects on device boot, including direct boot (before unlock).
- **Android TV support** - leanback launcher category, D-pad navigation, banner icon.
- **Material Design UI** - server list, settings, log viewer, split tunneling picker, onboarding wizard.

## Requirements

| Requirement       | Minimum            |
|-------------------|--------------------|
| Android version   | 7.0 (API 24)      |
| Target SDK        | 33 (Android 13)   |
| Architectures     | `arm64-v8a`, `armeabi-v7a`, `x86_64` |

The APK includes native libraries for all three architectures. Most modern phones use `arm64-v8a`.

## Project Structure

```
app/src/main/java/com/tiredvpn/android/
├── TiredVpnApp.kt              # Application class
├── native/                     # JNI bridge to Go VPN core
│   ├── TiredVpnNative.kt       # JNI function declarations
│   ├── NativeProcess.kt        # Native process lifecycle
│   └── TiredVpnProcess.kt      # Process abstraction
├── vpn/                        # VPN service layer
│   ├── TiredVpnService.kt      # Android VpnService implementation
│   ├── ConnectionManager.kt    # Connection state machine
│   ├── ServerRepository.kt     # Server config persistence
│   ├── VpnConfig.kt            # Configuration model
│   ├── VpnState.kt             # State enum
│   └── VpnWatchdogWorker.kt    # WorkManager keepalive
├── ui/                         # Activities
│   ├── MainActivity.kt         # Dashboard (connect/disconnect)
│   ├── ServerConfigActivity.kt # Add/edit server
│   ├── ServerListActivity.kt   # Server picker
│   ├── SettingsActivity.kt     # App settings
│   ├── SplitTunnelingActivity.kt
│   ├── LogViewerActivity.kt    # Real-time log viewer
│   ├── WelcomeActivity.kt      # Onboarding wizard
│   └── AboutActivity.kt
├── porthopping/                # Port hopping logic
├── receiver/                   # Broadcast receivers
│   ├── BootReceiver.kt         # Auto-start on boot
│   ├── AirplaneModeReceiver.kt # Reconnect after airplane mode
│   ├── ConfigImportReceiver.kt # ADB config import
│   └── VpnControlReceiver.kt   # ADB connect/disconnect
├── update/                     # Auto-update system
│   ├── UpdateManager.kt
│   ├── VersionChecker.kt
│   ├── ApkDownloader.kt
│   ├── ApkInstaller.kt
│   └── UpdateWorker.kt
└── util/                       # Helpers
    ├── BatteryOptimizationHelper.kt
    ├── CountryDetector.kt
    ├── FileLogger.kt
    ├── PingManager.kt
    └── TvUtils.kt
```

## Building from Source

### Prerequisites

- **Android Studio** Hedgehog (2023.1) or newer
- **Android NDK** 27.x (install via SDK Manager)
- **Go** 1.24 or newer
- **JDK** 17

### Step 1: Build the Go native library

Use the provided script. Without arguments it clones the pinned [fork core](https://github.com/igor04091968/tiredvpn/tree/v1.12.2-igor.3) into a fresh temporary directory, cross-compiles for all three architectures, and places the `.so` files in the right directories:

```bash
export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk/27.2.12479018
./scripts/build-jni.sh
```

If you already have the Go core checked out locally:

```bash
./scripts/build-jni.sh --core-dir /path/to/tiredvpn
```

The script outputs `libtiredvpn.so` into `app/src/main/jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/` and writes `app/src/main/jniLibs/.core-revision` with the core revision and a checksum per library.

Gradle packages native code only when that stamp matches the libraries. A `.so` copied in by hand, or one without a stamp, stops the build with an explanation. Gradle can also build the libraries itself from a checkout you name:

```bash
./gradlew assembleDebug -PtiredvpnCoreDir=/path/to/tiredvpn
```

`tiredvpnCoreDir` can live in `~/.gradle/gradle.properties` instead. The revision ends up in the APK as `assets/core-revision.txt`.

### Step 2: Build the APK

```bash
./gradlew assembleDebug
```

The output APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

For a release build with ProGuard minification:

```bash
./gradlew assembleRelease
```

## Configuration

### Via the app UI

1. Open the app and tap **Add Server**.
2. Enter the server address, port, and client secret.
3. Tap **Save** and connect.

### Via deep link

Share a `tired://` URI. The app registers an intent filter for the `tired` scheme and opens the server configuration screen with pre-filled fields.

### Via ADB (useful for Android TV)

```bash
# Import a server config
adb shell am broadcast -a com.tiredvpn.IMPORT_CONFIG \
  --es host "vpn.example.com" \
  --es port "995" \
  --es secret "base64secret"

# Connect
adb shell am broadcast -a com.tiredvpn.ACTION_CONNECT

# Disconnect
adb shell am broadcast -a com.tiredvpn.ACTION_DISCONNECT
```

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| AndroidX Core KTX | 1.18.0 | Kotlin extensions for Android |
| Material Components | 1.14.0 | UI components and theming |
| AndroidX Lifecycle | 2.10.0 | ViewModel and lifecycle-aware components |
| Kotlin Coroutines | 1.11.0 | Asynchronous programming |
| AndroidX WorkManager | 2.9.0 | VPN watchdog and update scheduler |
| OkHttp | 5.4.0 | Update checking and APK downloads |
| ConstraintLayout | 2.1.4 | Responsive layouts |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on submitting patches, running tests, and code style.

## License

This project is licensed under the GNU Affero General Public License v3.0. See [LICENSE](LICENSE) for details.
