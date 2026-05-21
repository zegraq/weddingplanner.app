# 004 — Mock-first utveckling

**Status:** Accepterat
**Datum:** 2026-05-21

## Beslut

Appen byggs mot en mockad API-implementation i shared-modulen innan
backend finns. Två konkreta implementationer av samma `ApiClient`-
interface — en mock, en riktig Ktor-baserad — växlas via manuell DI.

## Varför

- Vi kan se appen köra på riktig hårdvara i veckan, inte i månaden
- API-kontraktet får mogna i appens kod där det faktiskt används.
  Backend implementerar sedan ett kontrakt som redan validerats av
  konsumenten
- Mocken är också vad UI-tester och Compose-previews kör mot
- Backend kan börjas när appens domänmodell har stabiliserats — vi
  slipper bygga om endpoints för att appens behov förändrades

## Kostnad vi accepterar

Två implementationer initialt. Mocken kan komma att skilja sig från
backend om vi inte håller den ärlig. Motåtgärd: när backend finns,
sätt mocken till "uppför sig exakt som backend i happy path" och
behåll den för UI-arbete utan att starta servern.
