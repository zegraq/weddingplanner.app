# Domänmodell

Domänen är pytteliten. Hela appen kretsar kring **ett bröllop**.
Det finns ingen flertenans, ingen användarhierarki, ingen organisation.

## Wedding (rotaggregat)

Singleton i praktiken. En instans per drift.

- `date` — bröllopsdatum, fast 2027-06-05 tills annat sägs
- `rsvp_deadline` — global deadline. Gästspecifika undantag finns inte
- `venue` — fritext, kan vara tomt initialt
- `notes` — fritext

## Guest

En rad per person. Plus-ones är egna rader med pekare till
huvudgästen — inte ett fält. Det betyder att en plus-one får eget
namn, egen kostpreferens, egen RSVP-status, egna kommentarer. Det är
poängen.

- `id`
- `name`
- `email` — krävs inte; vissa gäster får bara muntlig inbjudan
- `phone` — krävs inte
- `tags` — fritextlista (`familj`, `Sara-jobb`, `bara-vigsel`). Inga
  fördefinierade kategorier. Filtreras på i UI
- `plus_one_of` — guest_id eller null. Null = huvudgäst
- `rsvp_status` — `pending` | `attending` | `declined`
- `rsvp_responded_at` — när status senast ändrades från pending
- `rsvp_token` — opaque sträng, används i OSA-URL
- `diet` — fritext (vegan, glutenfri, allergi mot X). Inte enum
- `notes` — fritext

### RSVP-flödet

Gästen får en länk: `/rsvp/<token>`. Ingen inloggning. Token är
hemlig nog (≥ 32 byte url-safe). En gäst kan ändra sin status fram
till `rsvp_deadline` — efter det är endpointen 410.

Plus-ones svarar via sin egen token. Huvudgästen ser inte sin
plus-ones svar i OSA-vyn; det administreras i appen.

## BudgetItem

- `id`
- `category` — fritext (`mat`, `lokal`, `kläder`, `musik`)
- `description` — vad det är
- `budgeted_amount` — i SEK, heltal (öre om vi behöver, men
  utgångspunkt är hela kronor)
- `actual_amount` — null tills något betalats
- `paid_at` — datum, null om inte betalat
- `notes` — fritext

Ingen multi-currency, ingen växelkurs.

## TodoItem

- `id`
- `title`
- `due_date` — datum, kan vara null
- `status` — `open` | `done`
- `assigned_to` — `me` | `partner` | `both`. Enum, för det är bara
  två personer
- `notes` — fritext

Inga subtasks, ingen prioritet, inga taggar. Om vi behöver det får vi
lägga till det när vi behöver det.

## Det vi medvetet skippar i v1

- **Audit-logg.** `created_at` / `updated_at` på rader räcker.
  Två användare som vet vad de gör behöver inte spåra varandra
- **Mjuk delete.** Tar vi bort en gäst är det medvetet
- **Versionerade RSVP-svar.** Senaste svaret gäller. Vill vi se
  historik tittar vi i `rsvp_responded_at`
- **Internationalisering.** Allt är på svenska
- **Tidszoner.** Allt är Europe/Stockholm
