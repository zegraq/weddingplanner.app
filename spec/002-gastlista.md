# 002 — Gästlista (Android, admin-vy)

## Mål

Bygg den första riktiga featuren: en gästlistskärm i Android-appen
där vi två administratörer kan se, lägga till, redigera och ta bort
hushåll och deras medlemmar. Allt går mot `MockApiClient` — mocken
blir mutable och fungerar som "fake backend" tills riktig backend
finns.

Efter denna ticket ska:

- Appen starta på en `Gäster`-flik som listar alla hushåll
- Två andra placeholder-flikar finnas (`Budget`, `Att-göra`) — tomma,
  bara för att navigationen är på plats
- Man kunna skapa ett hushåll, lägga till medlemmar, sätta
  huvudansvarig, ändra RSVP-status manuellt och kopiera OSA-länk
- `./gradlew check` köra och passera (inkluderar tester för
  MockApiClient och token-genereringen)

## Tagna beslut innan ticket

- Domänmodellen ändras till hushåll + medlemmar (se uppdaterad
  `docs/domain.md` och ADR 005). Tidigare plus-one-modell ersätts
- Data är mutable i `MockApiClient`, in-memory, försvinner vid
  app-omstart. Persistens (SQLDelight) är egen ticket
- API-kontraktet är REST-stil med suspend-funktioner som returnerar
  `Result<T>`. Endpoints matchar vad backend kommer ha
- Den publika `/rsvp/<token>`-sidan byggs inte i denna ticket. Bara
  admin-vyn i appen
- Inbjudningslänkar visas i appen, vi kopierar och skickar manuellt
  (SMS/iMessage/email)

## Filer som ska produceras / ändras

```
docs/domain.md                                   (redan uppdaterad)
docs/decisions/005-hushall-som-rsvp-enhet.md     (redan skapad)

shared/
├── src/commonMain/kotlin/app/weddingplanner/
│   ├── api/ApiClient.kt                         (utöka)
│   ├── api/MockApiClient.kt                     (mutable state)
│   ├── api/Wedding.kt                           (orörd)
│   ├── domain/Household.kt                      (ny)
│   ├── domain/Guest.kt                          (ny)
│   ├── domain/RsvpStatus.kt                     (ny)
│   └── domain/RsvpToken.kt                      (ny — generator)
└── src/commonTest/kotlin/app/weddingplanner/
    ├── api/MockApiClientTest.kt                 (utöka med CRUD)
    └── domain/RsvpTokenTest.kt                  (ny)

composeApp/
├── build.gradle.kts                             (lägg till navigation)
└── src/main/kotlin/app/weddingplanner/
    ├── App.kt                                   (host för nav)
    ├── di/AppContainer.kt                       (orörd)
    ├── ui/nav/RootNavigation.kt                 (ny — NavHost + bottom-nav)
    ├── ui/guests/GuestListScreen.kt             (ny)
    ├── ui/guests/HouseholdDetailScreen.kt       (ny — visa + redigera)
    ├── ui/guests/HouseholdEditScreen.kt         (ny — skapa + edit-fält)
    ├── ui/guests/GuestListViewModel.kt          (ny)
    ├── ui/guests/HouseholdDetailViewModel.kt    (ny)
    └── ui/components/RsvpStatusChip.kt          (ny — återanvändbar)
```

## Hur du jobbar

1. **Domänlager först.** Definiera `Household`, `Guest`, `RsvpStatus`,
   och en token-generator. Tester för token-generatorn (rätt längd,
   url-safe, unika)
2. **API-kontraktet.** Utöka `ApiClient` med:
   - `suspend fun listHouseholds(): Result<List<Household>>`
   - `suspend fun getHousehold(id: String): Result<Household>`
   - `suspend fun createHousehold(input: HouseholdInput): Result<Household>`
   - `suspend fun updateHousehold(id: String, input: HouseholdInput): Result<Household>`
   - `suspend fun deleteHousehold(id: String): Result<Unit>`
   - `suspend fun setGuestRsvpStatus(householdId: String, guestId: String, status: RsvpStatus): Result<Household>`

   `HouseholdInput` är samma fält som `Household` utan id, token,
   responded_at — det användaren faktiskt fyller i
3. **Mock-implementation.** `MockApiClient` håller en
   `MutableStateFlow<List<Household>>` (eller bara en `mutableListOf`
   med `Mutex` för trådsäkerhet). Seed:a med 2-3 exempel-hushåll
4. **Tester för mocken.** Verifiera CRUD-cyklerna och att RSVP-status
   sätts korrekt på rätt person
5. **Compose Navigation.** Lägg till `androidx.navigation.compose` i
   version catalog och `composeApp/build.gradle.kts`. `RootNavigation`
   = `NavHost` med tre routes (`guests`, `budget`, `todo`) och en
   `Scaffold` med `NavigationBar`
6. **GuestListScreen.** Lista grupperad per hushåll: hushållsnamn +
   chip-rad med statusar för varje medlem, taggar undertill.
   Filter-chips överst (status: alla/pending/attending/declined).
   FAB för "Nytt hushåll". Klick på rad öppnar detalj
7. **HouseholdDetailScreen.** Alla fält visas, lista av medlemmar
   med RSVP-status och dropdown för att ändra status manuellt.
   Knappar: `Redigera`, `Lägg till medlem`, `Kopiera OSA-länk`,
   `Ta bort hushåll`
8. **HouseholdEditScreen.** Formulär — display_name, email, phone,
   tags (chip-input), notes. Medlemmar redigeras separat via
   detaljvyn för att hålla skärmen enkel
9. **ViewModels.** Stateholders som anropar `ApiClient` via
   `AppContainer`. Använd `viewModelScope` och `StateFlow<UiState>`
10. **Verifiera.** `./gradlew check` + manuell smoke test i emulator
    (om tid finns)

## Vad du redan vet

- Package: `app.weddingplanner`
- Material 3 Compose, ingen ny temapalett
- Manuell DI via `AppContainer` (ADR 003 / spec/001)
- Mock-first (ADR 004) — denna ticket "uppfyller" mocken med riktigt
  beteende
- Hushållsmodell + en token per hushåll (ADR 005)
- OSA-token är opaque ≥ 32 byte url-safe (domain.md)
- All text på svenska, datumformat enligt sv-SE, tidszon
  Europe/Stockholm
- Bröllopets URL är inte hårdkodad i token-konstruktionen ännu —
  använd platshållare `https://weddingplanner.local/rsvp/<token>`
  tills domännamn bestäms

## Vad du INTE ska göra

- Bygg ingen `/rsvp/<token>`-publik vy. Den är egen ticket
- Implementera ingen riktig backend-anrop — bara mocken
- Sätt inte upp SQLDelight för Guest/Household — mock är in-memory
- Lägg inte till Budget eller Todo-skärmar (utöver placeholder med
  "Kommer snart"-text)
- Lägg inte till bibliotek utöver `androidx.navigation.compose` och
  ev. ikonpaket som redan finns i Material — fråga annars
- Bygg inte importer från CSV eller liknande — vi lägger till för
  hand i denna fas
- Validera inte fältlängder eller email-format hårt — vi två är
  enda användare; tillit över validering

## När du är klar

1. `./gradlew check` ska gå igenom utan röda flaggor
2. `./gradlew assembleDebug` ska ge APK
3. Commit till `agent/002-gastlista`, pusha, föreslå PR-flöde
