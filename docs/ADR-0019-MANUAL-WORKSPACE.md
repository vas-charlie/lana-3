# LANA 3 — Ručni radni prostor (prva faza)

**Status:** NACRT ZA PREGLED, nije implementirano niti testirano na uređaju.
**Polazište:** postojeći Android projekt, lokalne bilješke i postojeće granice autorizacije.

## Cilj
Lana 3 mora biti korisna i bez glasa: korisnik ručno unosi, pregledava i uređuje zadatke, rokove, projekte i bilješke. Glasovni sloj kasnije poziva iste poslovne operacije, bez druge baze podataka.

## Prva isporuka: Zadaci i podsjetnici
- Popis zadataka: naziv, opis, pripadni projekt, status (otvoreno / čeka se / dovršeno), prioritet, rok (opcionalno), datum promjene.
- Ručno stvaranje, uređivanje, dovršavanje i brisanje uz potvrdu brisanja.
- Filtri po projektu, statusu i roku; jasno označeni zadaci bez roka.
- Lokalna pohrana na uređaju i očuvanje podataka nakon ponovnog pokretanja.
- Bez lažnih obavijesti: sistemske notifikacije i ponavljanje su zasebna faza, uz Android dozvole i testove.
- Ne kopirati privatne financijske podatke u javni GitHub repozitorij.

## Početni projekti (kategorije, ne automatski uvezeni privatni podaci)
Taksi i klijenti; BUSINESS OS; MASTER Excel; Marketing i web; LANA 3; Platforma za vozače; Online zarada; Strip i brend; Administracija, pravo i financije.

## Buduće faze
1. Povezane bilješke i dokumenti, te ručne tablice prihoda i troškova s provjerom formula.
2. Lokalni podsjetnici s dozvolama, ponavljanjem i pouzdanim okidanjem.
3. Sigurna sinkronizacija mobitela i tableta s rješavanjem konflikata i kontrolom pristupa.
4. Glasovno stvaranje i čitanje istih zapisa kroz postojeću autorizacijsku arhitekturu.

## Ograničenja i sigurnost
- Ne mijenjati postojeći MASTER Excel ni njegove podatke bez provjere originala.
- Ne tvrditi da je sinkronizacija implementirana; lokalna pohrana nije isto što i sigurnosna kopija.
- Očuvati postojeće bilješke i izbjegavati destruktivne migracije baze.
- Postojeći režimi OBSERVE / SUGGEST / CONFIRM / EXECUTE moraju ostati mjerodavni za AI-radnje; izričite korisničke radnje u sučelju obrađuju se odvojeno i sigurno.

## Kriteriji prihvaćanja prve faze
- [ ] Android aplikacija se izgradi bez regresije.
- [ ] Zadaci se mogu stvoriti, izmijeniti, filtrirati, dovršiti i izbrisati.
- [ ] Zapisi ostaju nakon zatvaranja i ponovnog otvaranja aplikacije.
- [ ] Testovi pokrivaju nevaljane unose i očuvanje podataka.
- [ ] Charlie potvrdi rad na fizičkom Android uređaju.
