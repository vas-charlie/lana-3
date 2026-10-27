# LANA 3 — Safety review: interruption and resume v0.1

Status: preliminarna analiza rizika, ne dokaz implementacije. Povezano s ugovorom PR #127.

## Invarijante
1. Pauza ne potvrđuje uspjeh vanjske radnje.
2. Resume nikad ne zaobilazi permission/capability/authorization gate.
3. Globalni Safe Stop uvijek nadjačava automatski nastavak.
4. Terminalni zadatak ne postaje ACTIVE bez nove, izričite radnje.
5. Spremanje checkpointa mora biti atomarno u odnosu na promjenu task statusa.
6. Side-effect mora imati stabilan idempotency key gdje integracija to dopušta; inače prvo provjeriti stvarni ishod.
7. Sirovi transkripti, lokacija i tajne ne kopiraju se nepotrebno u checkpoint.
8. Resume ne znači automatski nastavak govora dok su putnici u vozilu.

## Rizici i kontrole
| Rizik | Kontrola | Dokaz prije prihvata |
|---|---|---|
| Dvostruko slanje poruke/rezervacije | Idempotency key + provjera ishoda | Integracijski test duplog resume |
| Izgubljen korak nakon rušenja | Transakcijska pohrana + verzioniranje | Crash/restart test |
| Stare dozvole ili lokacija | Revalidate pri resume | Test opoziva dozvole |
| Krivi nastavljen zadatak | Kontekstni odabir + razjašnjenje | Test dva PAUSED zadatka |
| Nastavak govora pred putnicima | Quiet/driving context gate | Fizički taxi test |
| Nesigurno ponašanje pri mrežnoj grešci | Degraded state, bez lažnog uspjeha | Offline/timeout test |
| Nepoznata verzija zapisa | Schema version + migration/fail-closed | Migration test |

## Odluke otvorene za implementaciju
- Odabrati postojeći task coordinator i stvarni storage adapter nakon pregleda koda.
- Definirati kada je vanjski ishod 'unknown' i kada je retry dopušten.
- Definirati TTL checkpointa po vrsti zadatka; ne postavljati proizvoljan univerzalni TTL.
- Provjeriti postoje li već ekvivalentni testovi prije dodavanja novih.
- Multi-device sync je zaseban projektni zahtjev, ne pretpostavljati ga.

## Granice
Nema koda, nema promjena u main, nema fizičkog testa i nema potvrde funkcionalnosti.
