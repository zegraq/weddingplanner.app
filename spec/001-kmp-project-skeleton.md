# 001 — KMP-projektskelett

## Mål

Skapa det Kotlin Multiplatform-skelett som senare tickets bygger
feature på. Efter denna ticket ska:

- `./gradlew assembleDebug` producera en körbar Android-APK
- APK:n starta en tom Compose-skärm med texten "weddingplanner" och
  versionsnumret hämtat från shared-modulen
- `./gradlew check` köra och passera (även om testsviten är liten)
- `iosApp/` finnas som skalprojekt — behöver inte byggas i denna
  ticket, men strukturen ska vara på plats

Ingen feature-kod. Inget UI utöver Hello World. Ingen API-klient
implementerad — bara interface och en tom mock som returnerar
hårdkodade värden.

## Filer som ska produceras

```
settings.gradle.kts
build.gradle.kts                              (root)
gradle/libs.versions.toml                     (version catalog)
gradle.properties
gradlew, gradlew.bat, gradle/wrapper/         (wrapper)

shared/
├── build.gradle.kts
├── src/commonMain/kotlin/app/weddingplanner/
│   ├── BuildConfig.kt                        (versionssträng)
│   ├── api/ApiClient.kt                      (interface, tomt)
│   └── api/MockApiClient.kt                  (implementation, hårdkodad)
└── src/commonTest/kotlin/app/weddingplanner/
    └── api/MockApiClientTest.kt              (en enkel test)

composeApp/
├── build.gradle.kts
├── src/main/AndroidManifest.xml
├── src/main/kotlin/app/weddingplanner/
│   ├── MainActivity.kt
│   ├── App.kt                                (Compose root)
│   └── di/AppContainer.kt                    (manuell DI)
└── src/main/res/                             (minimum för att bygga)

iosApp/
├── iosApp.xcodeproj/                         (tomt skalprojekt)
└── iosApp/
    ├── iOSApp.swift                          (SwiftUI App, tom)
    └── ContentView.swift                     (Hello World)
```

## Hur du jobbar

1. Sätt upp Gradle med Kotlin DSL och version catalog
2. Konfigurera shared-modulen med KMP-pluginnen, Android och iOS-targets
3. Lägg till SQLDelight och Ktor som dependencies i shared (men
   använd dem inte än — det räcker att modulen kompilerar med dem)
4. Skapa Android-modulen `composeApp/` med Compose-beroenden
5. Skapa `iosApp/`-strukturen tomt (Xcode-projektet får vara
   minimalistiskt)
6. Manuell DI i `AppContainer.kt` — en klass som exponerar
   `apiClient: ApiClient` och returnerar `MockApiClient()`
7. Verifiera att `./gradlew assembleDebug` och `./gradlew check` går
   igenom

## Vad du redan vet

- Package: `app.weddingplanner`
- JDK 21, Kotlin senaste stable, minSdk 26, targetSdk 34
- KMP-arkitektur: shared + composeApp + iosApp (se `docs/stack.md`)
- Manuell DI, ingen Hilt eller Koin (se ADR 003 och stack.md)
- Mock-first — `MockApiClient` är default i denna ticket (se ADR 004)
- Ingen audit-tabell, ingen tidszonshantering (se `docs/domain.md`)

## Vad du INTE ska göra

- Implementera feature-skärmar (gästlista, budget, todo) — det är
  egna tickets
- Lägga till bibliotek som inte står i `docs/stack.md` utan att
  fråga
- Implementera backend-kommunikation på riktigt — bara mocken
- Konfigurera CI / GitHub Actions — egen ticket senare
- Bygga iOS-projektet skarpt — bara strukturen, så att en framtida
  iOS-ticket har en plats att börja
- Skriva tester för triviala saker. En test som verifierar att
  `MockApiClient.getWedding()` returnerar förväntad mock-data räcker

## När du är klar

1. Verifiera att alla filer ovan finns
2. Kör `./gradlew check` och `./gradlew assembleDebug` lokalt
3. Säg till användaren att skelettet är klart och föreslå nästa
   ticket — sannolikt `spec/002-guest-list-feature.md`
