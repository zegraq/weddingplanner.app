# Vision

En privat app för att planera mitt och min partners eget bröllop den
**5 juni 2027**. Två användare totalt. Inte ett kommersiellt projekt
och kommer aldrig att bli ett.

## Vad appen ska göra

- Hålla reda på gästlistan och samla in OSA från gäster via en länk
- Spåra budget mot faktiska kostnader
- Hålla en att-göra-lista med deadlines fram till bröllopsdagen

Det är allt. Om något inte ger värde till just oss två i planeringen
av just det här bröllopet hör det inte hemma här.

## Vad det inte ska göra

- Stödja flera bröllop, organisationer eller team
- Skala till okända framtida användare
- Hantera "edge cases" som inte gäller oss
- Vara generisk, återanvändbar eller säljbar

Konkret out-of-scope för v1: bordsplacering, leverantörsregister,
inbjudningskort-rendering, betalningar.

## När är klart

v1 ska vara funktionellt och i drift **5 juni 2026** — ett år innan
bröllopet. Då ska OSA-länkar kunna skickas ut. Buffert mellan v1 och
bröllopet används för buggar, iOS-port och de detaljer som dyker upp
först när vi börjar använda appen på riktigt.

## Varför bygga själv

Det finns kommersiella appar för det här. Vi bygger ändå, av tre
anledningar:

1. Vi äger vår data och våra gästers data fullt ut
2. Den här stacken (KMP + .NET backend + self-hosted) är ett
   lärobjekt på ett verkligt projekt med skarpt slutdatum
3. Inga prenumerationsavgifter och ingen funktionalitet som plötsligt
   gömmer sig bakom en betalvägg vecka 38 i planeringen

## Tonalitet

Appen är för oss två. Den får ha våra preferenser inbakade. Den
behöver inte vara snäll mot hypotetiska andra användare. Om jag eller
min partner tycker något är dumt — vi ändrar koden direkt, vi
diskuterar inte en feature-toggle.
