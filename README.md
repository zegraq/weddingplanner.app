# weddingplanner.app

Privat bröllopsplaneringsapp. Två användare. Skarp deadline.

- **Bröllop:** 5 juni 2027
- **v1 i drift:** 5 juni 2026
- **Status:** Bootstrap. Ingen kod skriven än

## Vad det är

Kotlin Multiplatform-app (Android först, iOS senare) mot en
ASP.NET Core-backend som körs self-hosted hemma via Cloudflare Tunnel.
Lokalt persistens via SQLDelight, server-side via Postgres.

För kontext, läs i ordning:

1. [`docs/vision.md`](docs/vision.md) — vad appen ska göra och inte göra
2. [`docs/domain.md`](docs/domain.md) — domänmodellen
3. [`docs/stack.md`](docs/stack.md) — teknikval och versioner
4. [`docs/decisions/`](docs/decisions/) — ADR:er

## För agenter

Läs [`AGENTS.md`](AGENTS.md). Det är referensdokumentet.

## För människor som råkar landa här

Det här är ett privat projekt och kommer aldrig att bli en produkt.
Källkoden är öppen för att jag/agenter ska kunna arbeta i den utan
friktion, inte för att den ska användas av andra.

## Bygga

Kommer i nästa ticket (`spec/001-kmp-project-skeleton.md`). Just nu
finns ingen kod.
