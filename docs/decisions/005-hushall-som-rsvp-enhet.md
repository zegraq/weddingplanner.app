# 005 — Hushåll som RSVP-enhet

**Status:** Accepterat
**Datum:** 2026-05-22

## Beslut

RSVP-enheten är hushållet, inte individen. Ett hushåll har en token,
en kontaktperson (huvudansvarig) och en OSA-länk. Huvudansvarig
svarar för alla medlemmar i hushållet via samma formulär. Status
sätts per individ — kostpreferens och närvaro varierar mellan
medlemmar — men formuläret fylls i av en person.

Detta ersätter den tidigare modellen där varje gäst (inklusive
plus-ones) hade egen token och svarade separat.

## Varför

- Matchar hur familjer faktiskt beter sig: en förälder anmäler
  hushållet, väljer åt barnen, vet vad alla har för allergier
- Färre länkar att skicka, färre tokens att hålla reda på
- En partner som har dålig email får ändå svar genom den andre
- Plus-ones modelleras inte som specialfall — alla extra-medlemmar
  läggs till på samma sätt, oavsett om de finns från start eller
  läggs till av huvudansvarig i OSA-formuläret

## Kostnad vi accepterar

- Hushållsmedlemmar utan huvudansvarig kan inte själva ändra sitt
  svar. Om en barnvakt-situation ändras får huvudansvarig öppna
  länken igen. För två administratörer som känner gästerna är detta
  ingen praktisk friktion
- Huvudansvarig ser allas svar. Det finns ingen integritet inom
  hushållet. Givet att alternativet är att inte få komma på
  bröllopet är detta okontroversiellt
- Modellen kräver att vi väljer en huvudansvarig per hushåll i
  appen vid registrering. Default: första personen som läggs till
  i hushållet
