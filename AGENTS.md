# AGENTS.md

Detta är referensdokumentet för agenter som arbetar i repot. Läs det
i sin helhet innan du börjar på en ny ticket. CLAUDE.md är en symlink
hit — samma innehåll för alla agenttyper.

## Vad det här projektet är

En privat bröllopsplaneringsapp för **5 juni 2027**, byggd av och för
två användare. Inte en produkt, inte en plattform, inte ett SaaS.

Läs `docs/vision.md` för scope. Läs `docs/domain.md` för datamodellen.
Läs `docs/stack.md` för teknikvalen. Läs `docs/decisions/` för varför
det ser ut som det gör.

Om en uppgift känns inkonsekvent mot dessa filer — fråga istället för
att lösa det åt något håll. ADR:erna är medvetet snäva.

## Sättet att arbeta

### Intervjua i grupper

När en ticket har okända detaljer, ställ frågor i grupper om 3–5,
inte en i taget och inte 30 på en gång. Börja brett (mål, scope) och
gå mot konkret (versioner, namn).

För beslut som inte är kritiska — föreslå en default istället för att
fråga öppet. Användaren säger ifrån om hen vill annat.

### Skriv iterativt

När du har nog för en fil, skriv den. Vänta inte på att hela tickets
output ska vara klar i ditt huvud. Användaren får då en chans att
justera tidigt.

### Använd auto-mode signaler

Om systemet säger att auto-mode är aktivt — fortsätt och fatta
rimliga beslut. Stoppa ändå om du är genuint blockerad eller om
beslutet bara användaren kan ta (datum, domännamn, hårdvara).

## Vad du inte gör utan att fråga

- Lägger till en feature som inte står i `docs/vision.md`
- Lägger till ett bibliotek som inte står i `docs/stack.md`
- Inför en arkitekturändring som motsäger en ADR
- Skapar abstraktioner (repositories, services, generic base
  classes, factory patterns) "för framtiden"
- Skriver tester för triviala mappers eller getters/setters
- Lägger till feature flags eller backwards-compat-skim för kod
  ingen utanför detta repo använder
- Internationaliserar texter — allt är på svenska
- Hanterar tidszoner — allt är Europe/Stockholm

Om en ticket *kräver* något ovan: lyft det med användaren först.

## Kodstil

### Kotlin

- Idiomatisk Kotlin. `data class` för värdetyper, sealed-typer för
  varianter, Result-typer eller exceptions (välj per kontext, inte
  båda i samma kodbas)
- Coroutines + Flow för asynkront. Inget RxJava
- Inga `!!`-operatorer i produktionskod. Är något null så är det null
- En klass per fil när klassen är stor. Flera typer per fil när de
  hör ihop tätt (sealed-hierarkier, små värdetyper)

### C# (backend)

- File-scoped namespaces
- `record` för DTOer och immutable värden
- `var` när typen är tydlig från höger sida, explicit typ annars
- Async hela vägen genom request-handlern. Inga `.Result` eller `.Wait()`
- Minimal API i Program.cs eller separata `Endpoints/<Feature>.cs`-filer

### Generellt

- Korta funktioner när det är naturligt, inte som regel
- Inga kommentarer för vad koden gör — variabelnamn räcker. Bara
  kommentarer för *varför* när det inte är uppenbart
- Inga TODO:n som inte också är ärenden — antingen fixa eller skriv
  ner i en ticket. Inte både

## ADR:er

ADR:er ligger i `docs/decisions/NNN-kebab-case.md`.

Mall:

```
# NNN — kort titel

**Status:** Accepterat | Föreslaget | Övergivet
**Datum:** YYYY-MM-DD

## Beslut
Vad valdes. En till tre meningar.

## Varför
Punkterna som faktiskt avgjorde valet.

## Kostnad vi accepterar
Vad vi betalar för detta. Om det här fältet är tomt — du har
förmodligen missat något.
```

ADR ska inte vara en essä som väger fem alternativ. Det är ett
beslut, inte ett seminarium.

## Specs (tickets)

Specs ligger i `spec/NNN-kebab-case.md`. Format:

```
# NNN — Titel

## Mål
Vad denna ticket producerar. Konkret.

## Filer som ska produceras / ändras
Lista.

## Hur du jobbar
Vid behov — beskriv arbetssätt om det skiljer från standardflödet.

## Vad du redan vet
Kontext som inte behöver återupptäckas.

## Vad du INTE ska göra
Saker som ligger nära men ska skippas. Förebygger scope creep.
```

`spec/000-bootstrap.md` är ett exempel på hur en spec ser ut.

## Branches och commits

- Trunk-based för solo-arbete: commits direkt på `main`
- Agent-driven implementation som ska granskas: feature branch +
  PR. Branch-namn: `agent/NNN-kort-beskrivning` där NNN är
  ticketnummer
- Commit-meddelanden: en rad imperativ ("Lägg till GuestRow",
  "Fixa RSVP-token-validering"). Kropp bara när det inte är
  uppenbart vad förändringen gör

## Tester

- Skriv tester för: datumberäkningar, RSVP-state transitions,
  token-generering och validering, valutaformatering, allt som
  involverar gränssnitt mellan moduler
- Skriv inte tester för: DTO-mappers, trivial UI-state, ren
  Compose-rendering, getters
- Mock-implementationen av API:t (se ADR 004) ska ha en testfil som
  bevisar att den uppför sig som backend i happy path

## När du är osäker

- Är det ett beslut som påverkar produktens form (domän, scope,
  UX-känsla)? **Fråga användaren.**
- Är det en teknisk detalj som inte påverkar produkten (versions,
  filnamn, intern struktur)? **Fatta beslutet och dokumentera det
  kort i koden eller PR-beskrivningen.**
- Motsäger uppgiften en ADR? **Fråga.** Det är förmodligen menat,
  men då vill användaren veta att ADR:n ska uppdateras eller
  ersättas.
