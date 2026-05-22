# Domänmodell

Domänen är pytteliten. Hela appen kretsar kring **ett bröllop**.
Det finns ingen flertenans, ingen användarhierarki, ingen organisation.

## Wedding (rotaggregat)

Singleton i praktiken. En instans per drift.

- `date` — bröllopsdatum, fast 2027-06-05 tills annat sägs
- `rsvp_deadline` — global deadline. Gästspecifika undantag finns inte
- `venue` — fritext, kan vara tomt initialt
- `total_budget` — totalbudget-tak i SEK (heltal hela kronor), null
  tills satt. Får överskridas av summan av kategoribudgets — vi
  varnar bara, vi blockerar inte
- `my_name` — namnet på "jag" i appen. Visas i tilldelningsval
  (att-göra). Default `Daniel` tills annat sägs
- `partner_name` — namnet på partnern. Visas i tilldelningsval.
  Default `Sara` tills annat sägs
- `notes` — fritext

## Household

Den minsta inbjudningsenheten. Ett hushåll representerar dem som
svarar tillsammans — typiskt en familj eller ett par. Singleton-fall
(en person inbjuden) modelleras också som ett hushåll, bara med en
medlem.

Hushållet äger kontaktuppgifterna och token:en. Se ADR 005 för varför
det är hushållsnivån — inte individnivån — som är RSVP-enheten.

- `id`
- `display_name` — fritext, hur vi refererar till hushållet i listan
  (`Familjen Andersson`, `Sara & Magnus`)
- `email` — krävs inte; vissa hushåll får bara muntlig inbjudan
- `phone` — krävs inte
- `tags` — fritextlista (`familj`, `Sara-jobb`, `bara-vigsel`). Inga
  fördefinierade kategorier. Filtreras på i UI
- `rsvp_token` — opaque sträng, används i OSA-URL. En per hushåll
- `rsvp_responded_at` — när någon medlem senast fick sin status ändrad
  från pending
- `notes` — fritext, administratörsanteckningar

## Guest

En rad per person inom ett hushåll. Den person som tar emot OSA-länken
markeras som huvudansvarig och fyller i svaret för alla i hushållet
(inklusive sig själv). Övriga medlemmar har egna fält för namn,
kostpreferens och anteckningar, men har inga egna kontaktuppgifter
eller egen token.

- `id`
- `household_id`
- `name`
- `is_main_contact` — bool. Exakt en per hushåll har detta `true`
- `rsvp_status` — `pending` | `attending` | `declined`. Per person —
  ett hushåll kan svara att två kommer och en inte
- `diet` — fritext (vegan, glutenfri, allergi mot X). Inte enum
- `notes` — fritext

### RSVP-flödet

Huvudansvarig får en länk: `/rsvp/<token>`. Ingen inloggning. Token är
hemlig nog (≥ 32 byte url-safe) och hör till hushållet, inte
individen. Formuläret listar alla hushållsmedlemmar och låter
huvudansvarig sätta status och allergier per person, samt lägga till
fler medlemmar i hushållet (typiskt barn eller en partner som
huvudansvarig inte hade tagit upp ännu).

En huvudansvarig kan ändra sina svar fram till `rsvp_deadline` — efter
det är endpointen 410.

Att lägga till medlemmar är möjligt både via OSA-formuläret (publik
sida) och via appen (administratörsvy). Båda vägarna går mot samma
endpoint i backend.

## BudgetCategory

Den planerande nivån. Vi sätter en budget per kategori — "Mat: 50 000
kr", "Lokal: 80 000 kr". Kategorier skapas vart efter behovet uppstår,
inga fördefinierade.

- `id`
- `name` — fritext (`Mat`, `Lokal`, `Kläder`)
- `budgeted_amount` — planerat belopp i SEK (heltal hela kronor)
- `notes` — fritext

## BudgetItem

Enskilda utgifter inom en kategori. Inget planerat belopp på posten —
gruppen äger planeringen. Posten skapas typiskt när vi bokar något
("Catering Sara & Sons"), och får sitt faktiska belopp först när
fakturan kommer.

- `id`
- `category_id`
- `description` — vad det är
- `actual_amount` — faktiskt belopp i SEK (heltal). Null tills posten
  är betald
- `paid_at` — datum, null tills posten är betald
- `notes` — fritext

En post räknas som "betald" när både `actual_amount` och `paid_at`
har värden. Att markera betald sker via en snabbdialog i appen som
sätter båda samtidigt.

Ingen multi-currency, ingen växelkurs.

## TodoItem

- `id`
- `title`
- `due_date` — datum, kan vara null
- `status` — `open` | `done`
- `assigned_to` — `me` | `partner` | `both`. Enum, för det är bara
  två personer. UI mappar `me` till `my_name`, `partner` till
  `partner_name`, och `both` till "Båda"
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
