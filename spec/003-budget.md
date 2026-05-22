# 003 — Budget

## Mål

Bygg budget-skärmen. Vi ska kunna sätta ett totalbudget-tak,
skapa kategorier med var sin budget, och lägga in poster under
varje kategori. När en post betalas registrerar vi faktiskt belopp
och datum via en snabbdialog. Allt går mot `MockApiClient` som
hanterar mutable state — backend kommer senare.

Efter denna ticket ska:

- Budget-fliken (just nu placeholder) visa: total-cap, summa
  gruppbudgets, oallokerat
- Kategorierna listas med planerat belopp, summa betalt, kvar
- Poster under varje kategori visa status (betald / väntar)
- Man kunna sätta totalbudget, skapa/redigera/ta bort kategorier,
  skapa/redigera/ta bort poster, snabbt markera post som betald
- `./gradlew check` köra och passera med nya tester

## Tagna beslut innan ticket

- Totalbudget är en separat siffra (tak), inte derivat av kategorierna.
  Den får underskridas eller överskridas — vi visar "kvar att fördela"
  eller "över taket" men blockerar inte. Domain.md uppdaterad
- Kategorier äger planeringsbeloppet. Posterna har bara faktiskt
  belopp + paid_at — inget per-post-planerat belopp
- Kategorier skapas vart efter behov — ingen fördefinierad lista
- Att markera en post som betald sker via snabbdialog på raden
  (belopp + datum). Annan redigering går via separat redigera-flöde
- API-kontraktet: mutationer returnerar hela `BudgetView` (totalCap +
  kategorier med items) så UI alltid får full state efter en ändring

## Filer som ska produceras / ändras

```
docs/domain.md                                    (redan uppdaterad)

shared/
├── src/commonMain/kotlin/app/weddingplanner/
│   ├── api/ApiClient.kt                          (utöka)
│   ├── api/MockApiClient.kt                      (utöka — budget-state)
│   ├── api/Wedding.kt                            (utöka — totalBudget-fält)
│   └── domain/
│       ├── BudgetView.kt                         (ny — view-aggregat)
│       ├── BudgetCategory.kt                     (ny)
│       └── BudgetItem.kt                         (ny)
└── src/commonTest/kotlin/app/weddingplanner/
    └── api/MockBudgetTest.kt                     (ny — CRUD-tester)

composeApp/
└── src/main/kotlin/app/weddingplanner/ui/
    ├── nav/RootNavigation.kt                     (utöka — budget-route)
    └── budget/
        ├── BudgetScreen.kt                       (lista + header)
        ├── BudgetViewModel.kt
        ├── EditTotalDialog.kt                    (sätt total-cap)
        ├── EditCategoryDialog.kt                 (skapa + redigera)
        ├── EditItemDialog.kt                     (skapa + redigera)
        └── PayItemDialog.kt                      (belopp + datum)
```

## Hur du jobbar

1. **Domänlager.** Definiera `BudgetCategory`, `BudgetItem`,
   `BudgetView` (= totalCap + categories). Allt serialiserbart
2. **Wedding utökas** med `totalBudget: Long?`. Default null
3. **API-kontrakt.** Utöka `ApiClient` med:
   - `suspend fun getBudget(): Result<BudgetView>`
   - `suspend fun setTotalBudget(amount: Long?): Result<BudgetView>`
   - `suspend fun createCategory(name: String, budget: Long, notes: String?): Result<BudgetView>`
   - `suspend fun updateCategory(id: String, name: String, budget: Long, notes: String?): Result<BudgetView>`
   - `suspend fun deleteCategory(id: String): Result<BudgetView>`
   - `suspend fun addItem(categoryId: String, description: String, notes: String?): Result<BudgetView>`
   - `suspend fun updateItem(itemId: String, description: String, notes: String?): Result<BudgetView>`
   - `suspend fun markItemPaid(itemId: String, amount: Long, paidAt: String): Result<BudgetView>`
   - `suspend fun markItemUnpaid(itemId: String): Result<BudgetView>` (för felklick)
   - `suspend fun deleteItem(itemId: String): Result<BudgetView>`
4. **MockApiClient.** Lägg till mutable state för totalCap + kategorier
   med items. Seed:a med 1-2 exempelkategorier ("Mat", "Lokal") med
   var sin post (en betald, en väntande). Total-cap default null
5. **Tester** för budget-CRUD och betalningsflödet
6. **UI.** `BudgetScreen` har:
   - Header-card med "Tak: 350 000 kr" (klickbar för edit), "Planerat:
     320 000 kr (summa kategorier)", "Kvar att fördela: 30 000 kr"
     (eller "Över taket med X kr" rött om över)
   - Lista av kategori-cards: namn, budget, summa betalt, antal poster
     väntande. Klick → expandera/dialog. "Lägg till post"-knapp per
     kategori
   - FAB: "Ny kategori"
   - Per post: beskrivning, status-chip (Betald / Väntar betalning),
     snabb "Betalt"-knapp om väntande
7. **Pengaformat.** Visa SEK som "30 000 kr". Behöver inte vara perfekt
   svenska gruppering — `String.format("%,d kr", amount).replace(",", " ")`
   räcker. Inget kotlinx-datetime, inget extra bibliotek
8. **Verifiera** med `./gradlew check` och `assembleDebug`

## Vad du redan vet

- Package: `app.weddingplanner`
- Mock-first (ADR 004) — denna ticket fyller mocken med riktigt beteende
- Manual DI (ADR 003) — gör inget med Hilt/Koin
- Hela kronor som `Long`, ingen öre, ingen multi-currency (domain.md)
- ingen total-cap-validering — låt grupperna överskrida om användaren vill
- En post är "betald" när både `actualAmount` och `paidAt` är icke-null

## Vad du INTE ska göra

- Lägg inte till en separat "Betalningar"-historik-vy. Vi använder
  `paidAt` på posten direkt
- Bygg inte progressbar-grafik eller pie-charts — bara siffror.
  Visuell polish kommer i UI-passet
- Validera inte stränga datumformat — bara `LocalDate`-stil
  (`yyyy-MM-dd`)-input i dialogen, ingen kalender-picker än
- Persistera inte till SQLDelight än
- Lägg inte till nya bibliotek utan att fråga

## När du är klar

1. `./gradlew check` ska gå igenom
2. `./gradlew assembleDebug` ska producera APK
3. Commit till `agent/003-budget`, pusha, föreslå PR-flöde
