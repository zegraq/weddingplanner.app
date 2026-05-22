# 005 — Inköpslista

## Mål

Bygg en separat inköpsflik för småposter (servetter, dekor, namn-
skyltar, ringkudde, ljus etc.) som kompletterar att-göra-listan.
Items har namn, antal, valfri butik, anteckning och avbockning
("köpt"). Allt går mot `MockApiClient` — backend kommer senare.

Efter denna ticket ska:

- Inköp ligger som femte flik i bottom-nav efter Att-göra
- Listan visar items sorterade: ej-köpta först, sedan alfabetiskt
  per namn; köpta sist, nedtonade och med genomstreckning
- Klick på checkbox togglar köpt-status med dagens datum som
  `boughtAt` (eller null vid återställning)
- Filterchips: `Att köpa` (default) / `Alla`
- Skapa/redigera/ta bort items via FAB respektive rad-klick
- `./gradlew check` passerar med nya tester

## Tagna beslut innan ticket

- Egen modell `ShoppingItem`, inte en flagga på `TodoItem`. Anledning
  i samtalet: inköpslistan förväntas bli 20+ poster och inte tjäna på
  deadlines/assignees. Att hålla dem separat håller också att-göra-
  listan ren
- Fält: `name`, `quantity (Int, default 1)`, `store: String?`,
  `notes: String?`, `boughtAt: String? (yyyy-MM-dd)`, `createdAt`
- Ingen pris-/budgetkoppling i v1 — inköpslistan är fristående.
  Vi gör explicit val: småinköp budgeteras inte per post, de täcks
  av en kategori i budget-vyn
- Ingen sektion-gruppering per butik i v1 — butik visas som rad-
  subtext. Vi lägger till gruppering om listan blir riktigt lång
- Snabbtoggle av köpt-status på raden (checkbox). `boughtAt` sätts
  till idag vid markering, nullas vid avmarkering. Datumvärdet kan
  inte redigeras manuellt — om det blir fel: tryck igen
- API-kontrakt: alla mutationer returnerar uppdaterad `ShoppingItem`
  (eller `Unit` vid delete) — samma stil som todo-API:t

## Filer som ska produceras / ändras

```
docs/domain.md                                (utöka — ShoppingItem-sektion)

shared/
├── src/commonMain/kotlin/app/weddingplanner/
│   ├── api/ApiClient.kt                      (utöka — shopping-endpoints)
│   ├── api/MockApiClient.kt                  (utöka — shopping-state + seed)
│   └── domain/
│       ├── ShoppingItem.kt                   (ny)
│       └── ShoppingItemInput.kt              (ny)
└── src/commonTest/kotlin/app/weddingplanner/
    └── api/MockShoppingTest.kt               (ny)

composeApp/
└── src/main/kotlin/app/weddingplanner/ui/
    ├── nav/RootNavigation.kt                 (utöka — inkop-route + tab)
    └── shopping/
        ├── ShoppingListScreen.kt             (lista + filter + FAB)
        ├── ShoppingListViewModel.kt
        ├── ShoppingEditScreen.kt
        ├── ShoppingEditViewModel.kt
        └── ShoppingRow.kt
```

## Vad du redan vet

- Package: `app.weddingplanner`
- Mock-first (ADR 004), Manual DI (ADR 003)
- ViewModel-mönstret: `StateFlow<UiState>`, ladda i `init`
- Bottom-nav klarar 5 tabs (Material3 NavigationBar)
- Datumjämförelser: ISO-prefix på `clock.nowIso()`

## Vad du INTE ska göra

- Koppla inte inköp till budget — inga belopp på items
- Bygg inte butiks-gruppering, kategorier eller listor av listor
- Bygg inte multi-select eller bulk-actions
- Bygg inte kalender-picker — `boughtAt` sätts automatiskt
- Lägg inte till bekräftelse vid borttag

## När du är klar

1. `./gradlew check` ska passera
2. `./gradlew assembleDebug` ska producera APK
3. Commit till `agent/005-inkop`
