# LANA 3 — Task resume acceptance matrix v0.1

Status: prijedlog testnog plana, bez tvrdnje da su testovi izvršeni. Ovisnost: TASK_INTERRUPTION_RESUME_CONTRACT_v0.1.md u PR #127.

## Izolirani testovi shared core
| ID | Ulaz | Očekivani rezultat |
|---|---|---|
| TR-01 | ACTIVE, naredba prekid | PAUSED, sačuvan taskId/currentStepId/nextActionId |
| TR-02 | PAUSED, naredba nastavak | Ponovna validacija, ACTIVE tek nakon uspjeha |
| TR-03 | Dvostruki resume | Jedan prijelaz; bez dvostrukog side-effecta |
| TR-04 | COMPLETED/CANCELLED/FAILED + resume | Bez implicitnog ponovnog pokretanja |
| TR-05 | STOP + resume | STOP ima prednost |
| TR-06 | Više PAUSED zadataka | Bez nasumičnog izbora; zahtjev za razjašnjenje |
| TR-07 | Promijenjena dozvola | Bez EXECUTE, jasno odbijanje |
| TR-08 | Istekli kontekst vožnje/lokacije | Re-evaluation prije prijedloga |
| TR-09 | Oštećen checkpoint | Bez izvršavanja; kontrolirani error/degraded |
| TR-10 | Restart nakon pohrane | Checkpoint se učitava, ništa se samo ne izvršava |

## Integracijski testovi
- Prekid tijekom poziva vanjskog adaptera: ishod UNKNOWN dok adapter ne potvrdi rezultat; nema automatskog ponavljanja nepovratne radnje.
- Usporedno aktivirani STOP i resume: serijalizacija, STOP ima prioritet.
- Checkpoint version migration: nepoznata verzija ne prolazi bez migracije.
- Dvije konkurentne sesije: optimistic version/transaction conflict mora spriječiti dvostruki nastavak.

## Fizički Android testovi (kasnije)
- Zaključavanje/otključavanje, background/foreground, process kill, restart uređaja.
- Glasovna naredba preko Bluetootha dok je vozilo sigurno parkirano.
- Dolazak poziva i prebacivanje na Uber/Bolt; bez zahtjeva za tipkanjem tijekom vožnje.
- Offline nastavak samo za lokalno podržane funkcije.
- Test na S24 Ultra, zatim tabletu. Bez pretpostavke da je sinkronizacija između uređaja već izvedena.

## Izvještaj po testu
Zabilježiti: commit, build, okruženje, korake, expected/actual, PASS/FAIL/BLOCKED, dokaz. 'ACCEPTED' tek nakon relevantne fizičke provjere i Charliejeva prihvata.

## Opseg
Samo testni plan. Ne mijenja postojeći test runner, kod ni stabilnu APK osnovu.
