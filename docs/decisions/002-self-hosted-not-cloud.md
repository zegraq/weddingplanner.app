# 002 — Self-hosted hemma istället för moln

**Status:** Accepterat
**Datum:** 2026-05-21

## Beslut

API och databas körs på en maskin hemma. Cloudflare Tunnel
exponerar API:t mot internet. Ingen Azure, ingen AWS, ingen Fly.io.

## Varför

- Gästdata är personuppgifter (namn, e-post, kostpreferenser). Vi
  vill inte ha den hos en tredjepart vi inte behöver
- Kostnad: noll utöver el och hårdvara vi redan har
- Cloudflare Tunnel löser TLS, DNS och NAT utan portforwarding
- Operations är ett lärobjekt — moln-PaaS gömmer för mycket av det
  som är intressant att förstå

## Kostnad vi accepterar

- En person är driftansvarig (mig). Backups, uppdateringar, övervakning
- Om hemnätet ligger nere ligger OSA-formuläret nere. Acceptabelt —
  gäster kan svara senare
- Ingen autoskalning. Inte ett problem för två administrativa
  användare och högst ett par hundra OSA-svar
