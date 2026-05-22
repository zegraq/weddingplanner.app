# 004 — Att-göra-lista

## Mål

Bygg att-göra-skärmen. Vi ska kunna lägga till uppgifter med titel,
deadline och vem som har dem, bocka av dem direkt i listan och
redigera/ta bort dem. Allt går mot `MockApiClient` som hanterar
mutable state — backend kommer senare.

Efter denna ticket ska:

- Att-göra-fliken (just nu placeholder) visa en platt lista med
  uppgifter sorterade på deadline (närmast först, null sist)
- Varje rad visa titel, deadline (med "Försenad" eller "Idag"
  framhävt), tilldelad-namn (riktiga namn — se identitet nedan)
  och en checkbox för att bocka av
- Man kunna skapa, redigera och ta bort uppgifter
- Det finnas två filterrader: status (`Öppna` / `Alla`, default
  `Öppna`) och tilldelad (`Alla` / `Mina` / `Partners`, default `Alla`)
- `./gradlew check` köra och passera med nya tester

## Tagna beslut innan ticket

- Sortering: stigande `dueDate`, null-deadlines hamnar sist.
  Inom samma datum: skapelseordning. Avbockade följer samma
  sortering men döljs när status-filtret är `Öppna`
- **Filter:** två oberoende chip-rader:
  - Status (single-select): `Öppna` (default) / `Alla`
  - Tilldelad (single-select): `Alla` (default) / `Mina` / `Partners`.
    `Mina` matchar `Me` + `Both`. `Partners` matchar `Partner` + `Both`
- Status togglas direkt på raden via en checkbox. Ingen separat
  knapp eller swipe — vi har två användare och det räcker
- Deadline visas som ISO (`2026-12-01`) plus en chip när det är
  relevant: `Försenad` (rött) om `dueDate < idag` och status öppen,
  `Idag` (highlight) om `dueDate == idag` och status öppen. Inget
  "om 3 dagar"-relativt än
- Datumfält i dialogen är en `OutlinedTextField` som tar
  `yyyy-MM-dd`. Ingen kalender-picker än (samma som budget-posten)
- **Identitet:** `TodoAssignee`-enum förblir `Me | Partner | Both` i
  domänen, men UI visar riktiga namn (`Daniel` / `<partner>` / `Båda`).
  Namnen lagras på `Wedding` som två nya fält `myName` och
  `partnerName`, defaultas till `"Daniel"` + `"Sara"` i mock-seed.
  Redigering av namnen sker via en framtida wedding-settings-skärm —
  utanför denna ticket. `docs/domain.md` uppdateras med fälten
- Default-assignee vid skapelse: `Both` (`Båda`)
- **Hemskärm:** rörs inte i denna ticket. Sammanfattningskort
  ("X öppna, Y försenade") är en separat ticket
- **Gruppering:** platt lista. Inga sektioner per period/milstolpe
- **Ångra/bekräfta:** ingen bekräftelse vid delete, ingen undo vid
  toggle. Två användare som vet vad de gör räcker
- API-kontrakt: alla mutationer returnerar uppdaterad uppgift
  (`TodoItem`) eller `Unit` vid borttag — UI hämtar resten via
  separat `listTodos()`. Vi optimerar inte mot full state-snapshot
  som budget gjorde, för det här är en platt lista utan aggregat

## Filer som ska produceras / ändras

```
docs/domain.md                                (utöka — myName/partnerName på Wedding)

shared/
├── src/commonMain/kotlin/app/weddingplanner/
│   ├── api/ApiClient.kt                      (utöka — todo-endpoints)
│   ├── api/Wedding.kt                        (utöka — myName + partnerName)
│   ├── api/MockApiClient.kt                  (utöka — todo-state + seed + namn)
│   └── domain/
│       ├── TodoItem.kt                       (ny)
│       ├── TodoStatus.kt                     (ny — enum)
│       ├── TodoAssignee.kt                   (ny — enum)
│       └── TodoInput.kt                      (ny — input-dto)
└── src/commonTest/kotlin/app/weddingplanner/
    └── api/MockTodoTest.kt                   (ny — CRUD + toggle)

composeApp/
└── src/main/kotlin/app/weddingplanner/ui/
    ├── nav/RootNavigation.kt                 (utöka — todo-route)
    └── todo/
        ├── TodoListScreen.kt                 (lista + filter + FAB)
        ├── TodoListViewModel.kt
        ├── TodoEditScreen.kt                 (skapa + redigera)
        ├── TodoEditViewModel.kt
        └── TodoRow.kt                        (rad-komponent)
```

## Hur du jobbar

1. **Domain-uppdatering.** Lägg `myName: String` och
   `partnerName: String` på `Wedding` (api/Wedding.kt). Defaultar i
   mock-seed till `"Daniel"` + `"Sara"`. Uppdatera `docs/domain.md`
   Wedding-sektionen kort
2. **Domänlager.** Lägg `TodoStatus` (`Open`, `Done`), `TodoAssignee`
   (`Me`, `Partner`, `Both`) och `TodoItem`:
   ```
   id: String
   title: String
   dueDate: String?      // yyyy-MM-dd
   status: TodoStatus
   assignee: TodoAssignee
   notes: String?
   createdAt: String     // ISO instant, för stabil sortering
   ```
   `TodoInput` är samma shape utan `id`/`createdAt`/`status` (status
   sätts av servern vid create — alltid `Open`)
3. **API-kontrakt.** Utöka `ApiClient` med:
   - `suspend fun listTodos(): Result<List<TodoItem>>`
   - `suspend fun createTodo(input: TodoInput): Result<TodoItem>`
   - `suspend fun updateTodo(id: String, input: TodoInput): Result<TodoItem>`
   - `suspend fun setTodoStatus(id: String, status: TodoStatus): Result<TodoItem>`
   - `suspend fun deleteTodo(id: String): Result<Unit>`
4. **MockApiClient.** Lägg till mutable `todos`-lista och seed:a med
   4–6 realistiska exempel som speglar bröllopets nuläge: någon
   bokning som behöver göras, en som är försenad (dueDate < idag,
   status öppen), en markerad klar, en utan deadline, mix av
   assignees. Validera att titel inte är blank, att `dueDate` om den
   finns matchar `^\d{4}-\d{2}-\d{2}$`
5. **Tester** för create/update/setStatus/delete och för att
   `listTodos` returnerar dem oförändrade. Inget mock-sortering-tes
   — sortering görs i ViewModel
6. **UI.** `TodoListScreen`:
   - Två filterrader i toppen:
     - Status: chips `Öppna` (default) / `Alla`
     - Tilldelad: chips `Alla` (default) / `Mina` / `Partners`
   - Lista med `TodoRow` per uppgift. Rad har: checkbox vänster,
     titel + deadline-rad mitten, assignee-chip (namn) höger. Klick
     på rad → edit. Klick på checkbox → `setTodoStatus`. Overflow-
     menu (tre prickar) på raden → `Ta bort`. Ingen bekräftelse-
     dialog
   - FAB: `Ny uppgift` → navigerar till `TodoEditScreen`
   - Tom-state-text när listan är tom (filter beaktas): "Inget på
     listan" om `Alla`+`Alla`, annars "Inga uppgifter matchar filtret"
7. **`TodoEditScreen`** har fält för titel, deadline (text
   yyyy-MM-dd), tilldelad (segmented control med tre alternativ —
   `<myName>` / `<partnerName>` / `Båda`, hämtas från Wedding via
   ViewModel), anteckningar. Spara/avbryt. Vid edit: extra "Ta
   bort"-knapp. Validering: titel krävs, `dueDate` om ifyllt måste
   matcha regex
8. **Datum-jämförelse.** `Clock` finns redan (`nowIso()` ger
   `2026-05-22T...`). Jämför första 10 tecknen för dagens datum
   (`clock.nowIso().substring(0, 10)`). Inget kotlinx-datetime
9. **Sortering i ViewModel.** Comparator: `Open` före `Done` vid
   `Alla`-filter; inom samma status: stigande `dueDate` med null
   sist; inom samma datum: stigande `createdAt`
10. **Assignee-filter.** `Mina` → `assignee in (Me, Both)`;
    `Partners` → `assignee in (Partner, Both)`; `Alla` → ingen filter
11. **Verifiera** med `./gradlew check` och `assembleDebug`

## Vad du redan vet

- Package: `app.weddingplanner`
- Mock-first (ADR 004) — backend kommer i senare ticket
- Manual DI (ADR 003) — `AppContainer` injicerar `ApiClient`
- ViewModel-mönstret: `StateFlow<UiState>` med data class state,
  ladda i `init`, mutation-metoder kör suspend och uppdaterar state
- Sealed/enum-mönstret: lättviktiga enums är ok när varianterna inte
  bär data
- Inga subtasks, prioritet eller taggar (domain.md säger så)
- Allt på svenska, allt Europe/Stockholm

## Vad du INTE ska göra

- Lägg inte till påminnelser, notiser, eller "due soon"-aggregat på
  hemskärmen — separat ticket
- Bygg inte kalender-picker. ISO-text räcker tills vi gör ett
  UI-pass över alla datumfält
- Bygg inte en wedding-settings-skärm för att redigera `myName` /
  `partnerName`. Mock-defaultarna räcker för denna ticket
- Inför ingen sektion-gruppering ("Före bröllopet", "Dagen då").
  Platt lista, sorterad på datum
- Implementera inte multi-select eller bulk-actions
- Lägg inte till bekräftelse-dialog vid borttag eller undo vid
  toggle. Råkar man bocka av — bocka i igen
- Persistera inte till SQLDelight än

## När du är klar

1. `./gradlew check` ska gå igenom (inkl `MockTodoTest`)
2. `./gradlew assembleDebug` ska producera APK
3. Commit till `agent/004-att-gora`, pusha, föreslå PR-flöde
