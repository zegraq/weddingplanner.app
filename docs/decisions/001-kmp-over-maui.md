# 001 — KMP framför .NET MAUI

**Status:** Accepterat
**Datum:** 2026-05-21

## Beslut

Kotlin Multiplatform med Jetpack Compose på Android och SwiftUI på
iOS. Inte .NET MAUI, inte Flutter, inte React Native.

## Varför

- Native känsla på båda plattformarna är ett krav. KMP ger oss det
  genom att UI-lagret är inhemskt på varje plattform — Compose är
  Android, SwiftUI är iOS
- Vi delar det som faktiskt är värt att dela: domän, repositories,
  API-klient, validering. Inte UI-koden
- KMP-ekosystemet (SQLDelight, Ktor, kotlinx.serialization) är
  matur och välunderhållet
- MAUI ger en känsla av "Xamarin Forms 2.0" på iOS och vi vill inte
  betala den kostnaden

## Kostnad vi accepterar

Två UI-implementationer. Vi skriver listvyn två gånger. Det är priset
för att slippa "halv-native" känslan. På det här projektets skala (en
handfull skärmar) är det överkomligt.
