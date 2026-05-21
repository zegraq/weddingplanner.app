# 003 — Vertical slice i backend

**Status:** Accepterat
**Datum:** 2026-05-21

## Beslut

En fil per feature/endpoint i backend. Ingen repository-layer ovanpå
EF Core. Inget MediatR, ingen AutoMapper, inga andra
ceremonibibliotek. Mappning mellan entiteter och DTOer görs manuellt.

## Varför

- Backend har, vid v1, kanske 10–15 endpoints. Det rättfärdigar inte
  arkitekturlager som är gjorda för 200 endpoints och fem team
- Repository ovanpå EF Core är dubbel abstraktion — EF Core är redan
  en repository
- MediatR-pipelines är värdefulla när cross-cutting concerns växer,
  men på vår skala blir de en kostnad utan motsvarande nytta
- Att läsa en endpoint från första raden till sista i en fil är
  snabbare och mindre felbenäget än att hoppa mellan handler,
  validator, service, repository och DTO

## Kostnad vi accepterar

Lite duplicering mellan endpoints som gör liknande saker. Det är
billigare än fel abstraktion. När vi ser tre endpoints som gör
exakt samma sak extraherar vi en hjälpfunktion — inte ett lager.
