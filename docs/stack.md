# Stack

## App

| Lager | Val |
|---|---|
| Språk | Kotlin |
| Multiplatform | Kotlin Multiplatform (KMP) |
| UI Android | Jetpack Compose |
| UI iOS | SwiftUI (port efter v1, struktur från start) |
| Lokal DB | SQLDelight |
| HTTP-klient | Ktor client |
| Serialisering | kotlinx.serialization |
| DI | Manuell — ingen Hilt, ingen Koin |
| Tester | Kotlin Test + Turbine för Flow |

### Versioner

- JDK 21 (LTS)
- Kotlin senaste stable
- Gradle senaste stable
- minSdk 26 (Android 8.0)
- targetSdk 34 (Android 14)

### Package

Root-package: `app.weddingplanner`. Speglar domännamnet.

### Modulstruktur (planerad)

```
shared/                  KMP, domän + repositories + API-klient
composeApp/              Android-app (Compose), tunt UI-lager
iosApp/                  iOS-projekt (skapas tomt initialt)
```

Detaljer hör hemma i `spec/001-kmp-project-skeleton.md`.

## Backend

| Lager | Val |
|---|---|
| Runtime | .NET 9 (senaste stable vid uppstart) |
| Web | ASP.NET Core minimal API |
| Persistens | EF Core 9 |
| Databas | PostgreSQL 16 |
| Hosting | Self-hosted hemma |
| Reverse proxy / TLS | Cloudflare Tunnel |
| Auth (app→API) | Cloudflare Access (delad token initialt) |
| Auth (gäst→OSA) | Token i URL, ingen inloggning |

### Arkitektur i backend

Vertical slice. En fil per feature-endpoint där det är rimligt.
Inga repositories ovanpå EF Core. Inget MediatR. Ingen AutoMapper.
Ingen abstrakt servicebas-klass som ärvs av tre konkreta.

Mappning mellan EF-entiteter och DTOer görs manuellt med
extension-metoder eller inline. Det är trivialt, kostar några
tangenttryck och är läsbart.

## Hosting

- Mini-PC hemma (specifik hårdvara TBD)
- Cloudflare Tunnel exponerar API:t på `api.weddingplanner.app`
  (domännamn TBD — bekräfta att `weddingplanner.app` är registrerad
  eller välj annan)
- Postgres körs i container på samma maskin
- Backup: Backblaze B2, daglig dump (kan ändras till annan tjänst)

Operativa detaljer (vilken container-runtime, hur deploys triggas,
exakt backup-strategi) bestäms när backend-ticketen körs.

## Utvecklingsflöde

- **Mock-first.** Appen byggs mot en mockad API-implementation i
  shared-modulen tills backend finns. API-kontraktet stabiliseras i
  appens kod, sedan implementeras motsvarande endpoints i backend
- **Trunk-based.** Direkt på `main` för solo-arbete och feature
  branches när en agent driver implementation som ska granskas
- **CI senare.** Lokal `./gradlew check` + `dotnet test` initialt.
  GitHub Actions läggs till när manuella checks börjar svida
- **Tester där det är kritiskt.** Datumberäkningar, RSVP-state
  transitions, token-validering. Inte triviala mappers, inte UI
