# 000 — Bootstrap

## Mål

Skapa initial dokumentation och projektstruktur genom dialog med
användaren. Detta är den första uppgiften i ett tomt repo. Ingen kod
ska skrivas i denna ticket — bara dokumentation och repo-struktur.

## Filer som ska produceras

```
weddingplanner.app/
├── README.md
├── AGENTS.md
├── CLAUDE.md                    (symlink till AGENTS.md eller kopia)
├── .gitignore
├── docs/
│   ├── vision.md
│   ├── domain.md
│   └── stack.md
├── docs/decisions/
│   ├── 001-kmp-over-maui.md
│   ├── 002-self-hosted-not-cloud.md
│   ├── 003-vertical-slice-architecture.md
│   └── 004-mock-first-development.md
└── spec/
    ├── 000-bootstrap.md         (denna fil, redan finns)
    └── 001-kmp-project-skeleton.md
```

## Hur du jobbar

### 1. Läs först denna fil i sin helhet

Förstå hela uppdraget innan du börjar fråga.

### 2. Intervjua i grupper

Ställ frågor i grupper om 3-5 åt gången. Inte 30 frågor på en gång.
Inte heller en i taget — det blir tjatigt.

Börja brett (vision, scope), gå mot konkret (versioner, namnstandarder).

### 3. Föreslå rimliga defaults

För saker som inte är kritiska: ge ett förslag istället för en öppen
fråga.

Bra: "Jag föreslår min SDK 26 om du inte har preferens — säg till om du
vill ha annat."

Dåligt: "Vilken min SDK vill du ha?"

### 4. Skriv filer iterativt

När du har nog för en fil, skriv den och fråga om användaren vill
justera innan du går vidare. Inte alla filer på slutet.

### 5. Föreslå ordning

Förslag på ordning: vision → domain → stack → decisions → AGENTS.md →
README.md → spec/001 → .gitignore.

AGENTS.md är referensdokumentet som faktiskt läses av agenter under
arbete — investera tid där.

## Vad du redan vet

Från tidigare diskussion finns följande beslut. Anta inte att det är
hela bilden, men du behöver inte argumentera om dem.

**Projekt**
- Privat app för användarens eget bröllop, inte kommersiellt
- Bröllopsdatum: 14 juni 2026
- Slutanvändare: användaren och hens partner
- Första riktiga feature: gästlista med RSVP

**Stack**
- Kotlin Multiplatform (KMP)
- Android först, iOS senare (men struktur från start)
- Jetpack Compose för Android, SwiftUI för iOS senare
- ASP.NET Core minimal API + EF Core + Postgres som backend
- Self-hosted hemma via Cloudflare Tunnel
- Lokal SQLite-databas i appen (SQLDelight för KMP-kompatibilitet)
- Ktor som HTTP-klient

**Arkitektur**
- Vertical slice i backend (en fil per endpoint/feature)
- Inga repositories ovanpå EF Core
- Inget MediatR, AutoMapper eller andra ceremonibibliotek
- Manuell DI för v1, ingen Hilt/Koin
- Mock-first development — bygg appen mot mockad API innan backend finns

## Frågor du sannolikt behöver ställa

Listan är inte uttömmande, och vissa frågor kanske redan besvarats. Fråga
de som är relevanta:

**Vision och scope**
- Vilka features är absolut i v1? (Gästlista med RSVP är bekräftad)
- Vilka features är explicit utanför scope?
- Vilket räknas som "klart" — bröllopsdagen? Något datum innan?

**Domänmodell**
- Modelleras plus-ones som separata Guest-rader eller som fält?
- Behövs grupperingar (familj, arbetskompisar) i v1?
- Aktivitetslogg från start — ja eller nej?
- Skall RSVP-deadline finnas på Wedding eller per-gäst?

**Stack-detaljer**
- Min Android SDK och target SDK?
- JDK-version? (förslag: 21)
- Kotlin-version? (förslag: senaste stable)
- Package-namn? (förslag: `app.weddingplanner` eller `<initialer>.weddingplanner`)
- Postgres-version på servern?
- Tester: TDD eller bara där det är kritiskt (cycle detection, datum)?

**Hosting och drift**
- Hårdvara för self-hosting redan bestämd? (Pi, mini-PC, NAS?)
- Domännamn redan registrerat?
- Backup-mål: B2, R2, OneDrive, lokal disk?

**Auth**
- Privat bruk: räcker delad token eller Cloudflare Access?
- OSA-flödet för gäster: token i URL utan inloggning, bekräftat?

**Process**
- CI från start eller senare?
- Branch-strategi: trunk-based eller feature branches + PR?
- Vill användaren granska varje PR från agenter eller bara stickprov?

## Tonalitet i filerna

- Direkt, opinionerad
- Ingen säljsnack, ingen "i dagens snabbrörliga värld"
- Bullets sparsamt, prosa när det räcker
- Korta filer: vision en sida, domain en sida
- AGENTS.md får vara längre, det är referensdokumentet

## När klar

1. Verifiera att alla filer i listan ovan finns
2. Säg till användaren att bootstrap är klar
3. Föreslå att nästa session startar på `spec/001-kmp-project-skeleton.md`
4. Innehållet i 001 ska du själv ha skapat under bootstrap, baserat på
   stack-besluten ni gjort tillsammans

## Vad du INTE ska göra

- Skriv ingen kod (varken Kotlin, C# eller annat)
- Skapa inget KMP-skelett — det är ticket 001:s uppgift
- Lägg inte till features som inte diskuterats
- Skriv inte långa ADRs som diskuterar 5 alternativ — ADR ska säga
  vad som valdes och varför, inte vara en essä
