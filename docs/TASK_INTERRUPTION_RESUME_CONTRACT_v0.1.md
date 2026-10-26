# LANA 3 — Prekid i nastavak zadatka: ugovor v0.1

Status: PRIJEDLOG / ZA REVIZIJU. Ne znači da je funkcija implementirana ili fizički testirana.

## Razlog
Charlie može prekinuti razgovor zbog taksi vožnje bez najave trajanja. Prekid mora sačuvati radni kontekst i omogućiti nastavak bez ponovnog objašnjavanja.

## Postojeći temelji
Engineering Baseline potvrđuje postojeće task-state vrijednosti QUEUED, ACTIVE, PAUSED, COMPLETED, CANCELLED, FAILED, DEGRADED, determinističke prijelaze i semantički status. Ne uvoditi paralelni task engine.

## Predloženi minimalni ugovor
Checkpoint: taskId, taskType, currentStepId, lastConfirmedResultRef, nextActionId, status, updatedAt, schemaVersion. Sadržaj osjetljivih podataka ostaje u odgovarajućoj pohrani; checkpoint čuva reference, ne puni transkript.

- Na korisnički prekid: ako je zadatak ACTIVE i moguće ga je sigurno pauzirati, napravi atomarni checkpoint i postavi PAUSED. Ako izvršava vanjsku nepovratnu radnju, ne tvrdi da je zaustavljena bez potvrde adaptera.
- Na novi dolazni prioritetni događaj: spremi checkpoint prije promjene fokusa; vožnja i sigurnost imaju prednost nad razvojnom konverzacijom.
- Na 'nastavljamo': učitaj zadnji PAUSED zadatak u odgovarajućem kontekstu; prije akcije ponovno provjeri dozvole, dostupnost, kontekst, zastarjelost i potvrde. Ne ponavljaj već potvrđene radnje.
- Ako je više kandidata ili kontekst nije pouzdan, kratko razjasni; ne nagađaj.
- CANCELLED/COMPLETED/FAILED zadaci ne smiju se implicitno nastaviti.
- Globalni STOP ima prednost nad resume naredbom.
- Korisnik može zatražiti 'što smo zadnje radili?' bez pokretanja akcije.

## Minimalni testovi
1. ACTIVE → PAUSED → ACTIVE čuva taskId, currentStepId i nextActionId.
2. Restart aplikacije nakon PAUSED učitava checkpoint bez automatskog izvršenja.
3. Dvostruka naredba 'nastavljamo' ne pokreće isti side-effect dvaput.
4. COMPLETED/CANCELLED ne nastavlja se automatski.
5. Promjena dozvola nakon pauze blokira EXECUTE.
6. Zastarjeli kontekst zahtijeva novu procjenu.
7. Safe Stop sprječava nastavak.
8. Nepoznat ili korumpiran checkpoint vraća sigurno DEGRADED/FAIL stanje, bez izmišljanja uspjeha.

## Granice ove promjene
Samo ugovor i kriteriji testiranja. Nema promjena u poznatoj dobroj Android startup/packaging osnovi, nema migracije podataka, nema produkcijskog obećanja. Sljedeća implementacijska promjena mora najprije pronaći postojeći task coordinator i perzistencijski sloj, a zatim dodati jedan testirani vertikalni presjek na zasebnoj grani.

## Provjera stvarnog Android koda — 26.10.2026.

Izravno pregledano na `main`:
- `androidApp/src/main/java/hr/vascharlie/lana3/TasksActivity.kt`: ručna evidencija `lana_manual_tasks.db`, tablica `tasks(id,title,project,state)`, statusi `Otvoreno / Čeka se / Dovršeno`. Nema polja checkpointa, `PAUSED` ni ugovora za izvršavanje radnji.
- `androidApp/src/main/java/hr/vascharlie/lana3/TaskDashboardActivity.kt`: samo čita istu bazu radi statistike.

**Zaključak:** Android `TasksActivity` je poslovni popis obveza, a semantički `ACTIVE/PAUSED` lifecycle pripada izvršnim zadacima u shared core. Ne spajati ih promjenom postojećeg SQLite statusa niti pretpostaviti da su ekvivalentni.

**Granica implementacije:** prvi presjek treba napraviti u shared-core orkestraciji izvršnih zadataka uz zasebnu, verzioniranu pohranu checkpointa. Integracija s Android ručnim obvezama dolazi tek nakon zasebne odluke o mapiranju; postojeća lokalna baza mora ostati kompatibilna i neizmijenjena u ovom PR-u.

**Dodatni regresijski test:** postojeći Android ručni zadaci i statistika ostaju nepromijenjeni tijekom pauziranja/nastavka izvršnog zadatka. Nije dokazano testom, ovo je kriterij za budući kod.
