# LANA 3 — Testni slučajevi: prekid i nastavak (v0.1)

Status: acceptance fixtures; nisu izvršeni automatizirani testovi. Nadopunjuje TASK_INTERRUPTION_RESUME_CONTRACT_v0.1.md, bez promjene aplikacijskog koda.

| ID | Početno stanje | Događaj | Očekivani rezultat |
|---|---|---|---|
| R01 | ACTIVE, step=S2, next=A3 | korisnik: 'prekini' | atomarni checkpoint S2/A3; task PAUSED; bez A3 |
| R02 | PAUSED, checkpoint S2/A3 | restart procesa | checkpoint učitan; nema automatskog A3 |
| R03 | PAUSED, dozvole iste | 'nastavljamo' | revalidacija; isti taskId; nastavak najviše jednom |
| R04 | PAUSED | 'nastavljamo' dvaput | jedna izvršna instanca; bez duplog side-effecta |
| R05 | COMPLETED | 'nastavljamo' | nema implicitnog ponovnog izvršenja |
| R06 | CANCELLED | 'nastavljamo' | nema implicitnog ponovnog izvršenja |
| R07 | PAUSED, permission=revoked | 'nastavljamo' | izvršenje odbijeno; objašnjenje nedostajuće dozvole |
| R08 | PAUSED, kontekst promijenjen | 'nastavljamo' | ponovna procjena; nema slijepog nastavka |
| R09 | PAUSED, Safe Stop aktivan | 'nastavljamo' | bez izvršenja |
| R10 | PAUSED, checkpoint nečitljiv | 'nastavljamo' | kontrolirani DEGRADED/FAIL; bez izmišljanja rezultata |
| R11 | dva PAUSED kandidata | 'nastavljamo' | bez pogađanja; kratko razjašnjenje |
| R12 | ACTIVE, vanjska nepovratna radnja u tijeku | 'prekini' | status ne smije tvrditi da je vanjska radnja zaustavljena bez potvrde |
| R13 | ACTIVE, korisnik traži status | 'što trenutno radiš?' | čitanje stvarnog stanja bez nove akcije |
| R14 | Android TasksActivity ručni zadaci postoje | pauziranje shared-core izvršnog zadatka | SQLite ručni zadaci i dashboard ostaju neizmijenjeni |

## Pravila za automatizaciju

- Testovi R01–R13 pripadaju shared-core testnoj razini i koriste fake storage, clock, permission i action adaptere.
- R14 je regresijska granica Android modula; ne dirati postojeću tablicu `tasks` radi uvođenja izvršnog checkpointa.
- Prije implementacije identificirati stvarne source putanje i postojeće ugovore; testove pisati u njihovoj postojećoj konvenciji.
- Potvrda u CI ne zamjenjuje fizičko testiranje prekida pri stvarnoj vožnji.
