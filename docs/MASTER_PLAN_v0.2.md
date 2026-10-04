# LANA 3 — MASTER PLAN v0.2

## Status
Radna inženjerska specifikacija za arhitekturu, razvoj, integraciju, testiranje i stabilizaciju LANA 3.

**Izvor:** MASTER PLAN v0.1 (43 funkcionalne cjeline), uz Charliejeve naknadno potvrđene izmjene.

## Pravilo projekta
ODLUČENO nije isto što i GOTOVO.

Svaka funkcija prolazi:
**specifikacija → arhitektura → implementacija → integracija → testiranje → popravak → regresija → prihvat → GOTOVO**

Statusi: ODLUČENO, U ARHITEKTURI, U RAZVOJU, INTEGRACIJA, TEST, POPRAVAK, REGRESIJA, PRIHVAT, GOTOVO.

Ako je ideja tehnički nemoguća ili ozbiljno ograničena, bilježi se stvarno ograničenje i najbliža izvediva alternativa. Ne pretpostavlja se da operativni sustav, Maps, Uber, Bolt ili druga aplikacija dopušta nešto što nije dokazano.

## Potvrđene izmjene u odnosu na v0.1

### A1 — Svi jezici od početka
LANA 3 se od početne arhitekture projektira za **sve jezike**, bez fiksne liste jezika i bez naknadnog "dodavanja višejezičnosti" kao zasebne nadogradnje.

To uključuje:
- automatsko prepoznavanje jezika gdje je tehnički izvedivo;
- govor, tekst, prijevod, TTS/STT i korisničko sučelje bez arhitektonske ovisnosti o jednom jeziku;
- Unicode i lokalizacijski ispravne podatkovne strukture;
- mogućnost da kvaliteta pojedinog jezika ovisi o stvarnim mogućnostima odabranih modela/servisa, što se mora testirati i dokumentirati.

### A2 — Uređajno neovisna arhitektura
LANA 3 se ne projektira za Samsung niti za jedan određeni proizvođač ili model uređaja.

Samsung Galaxy S24 Ultra i Samsung tablet mogu biti **početni fizički testni uređaji**, ali nisu arhitektonska granica proizvoda.

Jezgra mora biti odvojena od platformskih adaptera tako da se podrška za druge uređaje i platforme može dodavati bez preprojektiranja poslovne logike, Decision Enginea, memorije i glavnih podatkovnih modela.

"Radi na svakom uređaju" ne znači da se unaprijed obećava identična funkcionalnost na svakoj platformi. Svaka platforma ima vlastite dozvole i ograničenja. Razlike se moraju otkriti testom, dokumentirati i riješiti adapterom ili najboljom izvedivom alternativom.

---

# 1. Ciljna arhitektura

LANA 3 ima jedno logičko srce i više kontroliranih načina djelovanja.

Glavni slojevi:
1. **Razgovor / UI** — glas, tekst, vizualni prikaz, statusi i minimalne kontrole.
2. **Intent** — prirodni govor pretvara u strukturiranu namjeru.
3. **Context** — uređaj, platforma, vožnja, lokacija, vrijeme, aktivni zadaci, aktivna aplikacija i relevantni podaci.
4. **Decision Engine** — odlučuje ŠTO napraviti prema cilju, pravilima, ograničenjima, kontekstu, nesigurnosti i posljedicama.
5. **Execution Layer** — odlučuje KAKO tehnički izvršiti odluku.
6. **Integracije / platform adapters** — Maps, telefon, poruke, YouTube, podaci o vožnji, datoteke i vanjski servisi.
7. **Memory / Knowledge** — kratkoročni kontekst, trajne bilješke, poslovni podaci, pravila i dokumenti.
8. **Permissions / Safety** — VIEW, SUGGEST, ASK CONFIRMATION, EXECUTE.
9. **Diagnostics** — logovi, testni način, greške, verzije i regresijski testovi.

## Mozak i ruke
**Mozak:** razumije korisnika, namjeru, kontekst, pravila i podatke.

**Ruke:** izvršavaju dopuštene radnje kroz platformu ili integraciju.

Vidjeti podatak nije isto što i imati pravo djelovati.

## Sigurnosna kočnica
Globalna naredba **"Lana, STOP"** mora zaustaviti aktivne zadatke u mjeri u kojoj konkretna platforma/integracija to tehnički dopušta.

---

# 2. Temeljni principi

- Lana ne pogađa. Nepoznat podatak ostaje označen kao nepoznat.
- Nesigurnost se ne skriva iza samouvjerenog odgovora.
- Veća nesigurnost kod važnih radnji znači nižu razinu automatizacije.
- Pita se samo za podatak koji stvarno nedostaje.
- Tok odluke: **ulaz → namjera/cilj → pravila i ograničenja → kontekst → procjena → odluka → akcija**.
- Sigurnost i izričita zabrana imaju prednost pred praktičnošću.
- Trajna pravila i privremene upute moraju biti različiti tipovi pravila.
- Nova informacija može pokrenuti ponovnu procjenu aktivne odluke.
- Konkurentni zadaci imaju determinističke prioritete.
- Za rizičnije automatizacije vrijedi: **OBSERVE → SUGGEST → CONFIRM → EXECUTE**.
- Uočeni obrazac nije automatski novo poslovno pravilo.

---

# 3. Funkcionalne cjeline 1–43

## 1. Razgovor i osobnost
Prirodna komunikacija bez učenja posebnih naredbi, promjene tema, više zahtjeva u jednoj rečenici i povratak na prethodnu temu. Arhitektura je višejezična od početka.

**Prihvat:** Lana razlikuje razgovor od zahtjeva za akcijom i radi prirodno u podržanom jeziku.

## 2. Glas
Slušanje kada je uključeno, aktivacija, pauza/nastavak, automatsko prepoznavanje jezika, TTS/STT, Bluetooth i različiti uređaji.

**Prihvat:** pouzdan glasovni tok u realnim uvjetima, jasni statusi i kontrola privatnosti.

## 3. Razumijevanje namjere
Prirodni govor → strukturirana namjera; korekcije usred govora; reference poput "tamo"; brojevi, datumi i novac; namjera odvojena od izvršenja.

**Prihvat:** rizična radnja ne izvršava se na temelju nejasne pretpostavke.

## 4. Smart Ride Acceptance
Procjena Uber/Bolt/taksi zahtjeva prema cijeni, pickupu, udaljenosti, vremenu, lokaciji, kontekstu, €/km, €/h, pravilima i ciljevima. Ishod: PRIHVATI / RAZMOTRI / PRESKOČI.

**Prihvat:** odluka je objašnjiva stvarnim podacima i pravilima; Charlie je konačni autoritet osim izričito dodijeljenog EXECUTE.

## 5. Decision Engine
Jedinstveni mehanizam odlučivanja za taksi, navigaciju, podsjetnike, poslovanje i druge funkcije.

**Prihvat:** odluka je odvojena od razgovora i tehničkog izvršenja te podržava pravila, iznimke, prioritete i nesigurnost.

## 6. Navigacija
Prirodno odredište, Maps/integracije, stvarna turn-by-turn navigacija, minimalno dodira, glasovne promjene odredišta/rute i rad Lane dok je navigacija u prvom planu.

**Prihvat:** na stvarnim uređajima dokazati razliku između prikaza rute i stvarnog pokretanja navigacije; ručni korak poput "NASTAVI" tretira se kao testni rezultat.

## 7. Lokacija
Trenutna lokacija, odredište, udaljenost, vrijeme putovanja, obližnja mjesta i geografski kontekst.

**Prihvat:** jasno ponašanje kada je lokacija isključena, netočna ili nedostupna.

## 8. Kamera / Lanine oči
Kamera je opća sposobnost Lane: fotografiranje, čitanje dokumenata/računa/cjenika, vizualno razumijevanje i analiza fotografije.

**Prihvat:** "Lana, pogledaj ovo" daje koristan rezultat uz jasno označenu nesigurnost i dopuštenje korisnika.

## 9. Svijest o ekranu
Opća sposobnost razumijevanja onoga što korisnik vidi na zaslonu, uključujući aktivnu aplikaciju, tekst, brojeve i kontekst.

**Prihvat:** pristup ekranu je odvojen od prava na akciju; automatizacija drugih aplikacija ne pretpostavlja se prije tehničke i pravne provjere.

## 10. Rad u pozadini
Rad dok je druga aplikacija u prvom planu, uz jasna stanja aktivno/pauzirano/ugašeno.

**Prihvat:** testirati promjenu aplikacija, zaključavanje ekrana, povratak i platform-specific ograničenja.

## 11. Turistički vodič u vozilu
Lokacijski vodič za znamenitosti, povijest, muzeje, mostove, parkove, plaže i događaje; glas, slike, karte i svi jezici koje sustav može kvalitetno obraditi.

**Prihvat:** kratki, normalni i dubinski način bez zatrpavanja putnika.

## 12. Lana Walk
Pješački digitalni vodič uz Bluetooth, kameru, lokaciju po potrebi, spontana pitanja, slike/kartu i višejezični rad.

**Prihvat:** privatni Charlie način i grupni turistički način su odvojivi.

## 13. Bilješke
Tekstualne i glasovne bilješke, uređivanje, brisanje, pretraga i povezivanje s klijentom, vožnjom ili datumom.

**Prihvat:** bilješka se sprema glasom i nalazi bez potrebe da korisnik zna fizičko mjesto pohrane.

## 14. Lana dnevnik
Automatsko bilježenje važnih događaja, eksplicitno "zapamti", dnevni sažetak i pretraga događaja.

**Prihvat:** jasno je što se bilježi automatski, a što izričitim zahtjevom.

## 15. Memorija
Kratkoročni kontekst, trajne bilješke, poslovni podaci, osobne postavke i upravljanje pamćenjem.

**Prihvat:** privremeni kontekst i trajno spremljena informacija nisu ista stvar.

## 16. Poslovni podaci
Prihodi, troškovi, kilometri, sati, €/h, €/km, gorivo, Uber, Bolt, taksimetar, napojnice, bonusi i statistika; veza s VAŠ CHARLIE Business OS.

**Prihvat:** jedinstven provjerljiv izvor podataka, bez dvostrukog ručnog unosa gdje je izvedivo.

## 17. Taxi funkcije
Kalkulator cijene, tarife, lokacija, kilometraža, vožnje, rezervacije, klijenti, budući vlastiti taksimetar te kasnije fiskalizacija/R1.

**Prihvat:** poslovna logika je odvojena od prikaza i spremna za naknadno provjerene zakonske zahtjeve.

## 18. Rezervacije
Glasovna rezervacija, izmjena/otkazivanje, podsjetnici, podaci o klijentu i prethodne vožnje.

**Prihvat:** jasan status i zaštita od slučajnog udvostručavanja.

## 19. Telefon i komunikacija
Pozivi, poruke, obrada poruka, potencijalne komunikacijske integracije, glasovno upravljanje i kontekst primatelja.

**Prihvat:** osjetljive radnje imaju potvrdu gdje je potrebna; nema potrebe za tipkanjem tijekom vožnje.

## 20. Glazba / YouTube
Glasovno pokretanje, pretraga, reprodukcija, pauza, sljedeće i kontrola glasnoće gdje platforma dopušta.

**Prihvat:** minimalan dodir ekrana i bez ometanja prioritetnih zadataka.

## 21. Prijevod i jezici
**Svi jezici od početka arhitekture.** Prijevod i promjena jezika unutar prirodnog razgovora, uz automatsko prepoznavanje jezika gdje je izvedivo.

**Prihvat:** prijevod zadržava kontekst; kvaliteta i dostupnost pojedinih jezika moraju biti testirane, ne pretpostavljene.

## 22. Kodiaq kao poslovni sustav
Gorivo, potrošnja, kilometri, servis, održavanje, gume, kvarovi, registracija, osiguranje i troškovi vozila.

**Prihvat:** pristup stvarnim podacima vozila mora biti potvrđen; nema izmišljene telemetrije.

## 23. Analitika
Dnevna, tjedna, mjesečna i godišnja analiza prihoda, troškova, sati, učinkovitosti i praznog hoda.

**Prihvat:** svaki izračun sljediv je do ulaznih podataka.

## 24. Podsjetnici i zadaci
Jednokratni, ponavljajući, poslovni, osobni, servisni, administrativni i financijski zadaci.

**Prihvat:** vrijeme, status i način obavijesti su trajni i zadatak se ne gubi nakon ponovnog pokretanja.

## 25. Univerzalna pretraga
Jedan upit pretražuje bilješke, dnevnik, klijente, vožnje, poslovne podatke i dokumente.

**Prihvat:** korisnik ne mora znati u kojem je podsustavu podatak spremljen.

## 26. Više uređaja
Isti Lana identitet, sinkronizacija, postavke, razgovor, ažuriranje i kasniji handoff između uređaja. Nije ograničeno na telefon + tablet niti na Samsung.

**Prihvat:** stanje i postavke su konzistentni; offline uređaj ima jasno definirano ponašanje; nova platforma ne zahtijeva prepisivanje jezgre.

## 27. Sigurnost i dozvole
Mikrofon, kamera, lokacija, pozivi, poruke, zaslon i vanjske aplikacije; jasna kontrola prava po platformi.

**Prihvat:** svaka osjetljiva sposobnost ima vidljivu i provjerljivu razinu autorizacije.

## 28. Offline
Osnovne naredbe, postavke, bilješke, lokalni podaci i osnovne taxi funkcije gdje je moguće.

**Prihvat:** Lana jasno razlikuje lokalne sposobnosti od onih koje zahtijevaju internet.

## 29. Vizualni identitet
Lana lijevo, komunikacija desno, bez nepotrebnog navigacijskog panela, profesionalan izgled i suptilne animacije poruka. UI mora biti prilagodljiv različitim veličinama i oblicima zaslona.

**Prihvat:** raspored ostaje upotrebljiv na podržanim form-factorima bez vezivanja za jedan model uređaja.

## 30. Arhitektura projekta
Čist lana-3 projekt, modularnost, stabilna podatkovna struktura i dodavanje funkcija bez rušenja temelja. Platformski specifičan kod ide iza jasnih adaptera/sučelja.

**Prihvat:** stari projekti ostaju netaknuti; nova funkcija ne mijenja neplanirano postojeće module; platforma nije ugrađena u poslovnu jezgru.

## 31. Laninin mozak i ruke
Formalna podjela razumijevanja/odlučivanja od tehničkog izvršenja.

**Prihvat:** način izvršenja ili platforma mogu se promijeniti bez promjene poslovne odluke.

## 32. Autorizacijski sustav
VIEW / SUGGEST / ASK CONFIRMATION / EXECUTE po funkciji i, gdje treba, po platformi/integraciji.

**Prihvat:** dopuštenje je provjerljivo prije svake osjetljive radnje.

## 33. Safe Stop
Globalna naredba za zaustavljanje aktivnih zadataka.

**Prihvat:** aktivni zadaci dobivaju signal prekida i stanje sustava postaje jasno.

## 34. "Što trenutno radiš?"
Lana objašnjava aktivni zadatak i njegov status.

**Prihvat:** odgovor je kratak, konkretan i temeljen na stvarnom stanju.

## 35. Prioriteti
Upravljanje konkurentnim događajima poput vožnje, navigacije, poziva i novih zahtjeva.

**Prihvat:** deterministička pravila sprječavaju sukob zadataka.

## 36. Objašnjenje odluke
Na pitanje "Zašto?" Lana prikazuje stvarne podatke i pravilo koje je dovelo do odluke.

**Prihvat:** objašnjenje je sljedivo do ulaza i pravila.

## 37. Test mode
OBSERVE → SUGGEST → CONFIRM → EXECUTE prije automatskih akcija.

**Prihvat:** odluke se mogu trenirati i provjeravati bez neželjenog izvršenja.

## 38. Knowledge Center
Dokumenti, cjenici, tablice, procedure, pravila i interne upute kao ovlašteno znanje sustava.

**Prihvat:** dokumentirani podatak i procjena jasno su različiti.

## 39. "Ne gnjavi me"
Delegiranje pozadinske provjere uz obavijest samo kada je potrebna korisnikova odluka.

**Prihvat:** zadatak ima stvarno stanje i ne proizvodi nepotrebne prekide.

## 40. Kontekst vožnje
Stanja vozi / parkiran / pauza / izvan vozila i prilagodba ponašanja.

**Prihvat:** u vožnji govor i ekran su sažeti; parkirano stanje dopušta detaljniji prikaz.

## 41. Učenje navika
Uočavanje obrazaca i predlaganje promjene bez tihog mijenjanja poslovnih pravila.

**Prihvat:** Lana predlaže; Charlie odlučuje.

## 42. "Lana ne pogađa"
Sustavno označavanje nepoznatih podataka, nesigurnosti i potrebe za potvrdom.

**Prihvat:** priznanje neizvjesnosti ima prednost pred lažnom sigurnošću.

## 43. Samodijagnostika / samopopravak / nadogradnja
Otkrivanje problema, analiza logova/koda, prijedlog ili priprema popravka, testiranje, preview i rollback; produkciju odobrava Charlie.

**Prihvat:** Lana ne povećava vlastite dozvole, ne nabavlja tajne ključeve, ne dira stare projekte i ne promovira sama sebe u produkciju.

---

# 4. Ovisnosti i razvojni red

## Faza A — Temelj
- projektna struktura i uređajno neovisna arhitektura
- razgovor / glas
- višejezični temelj
- intent
- context
- Decision Engine
- authorization
- task / priorities
- diagnostics
- osnovni offline sloj

## Faza B — Jezgra vozača
- lokacija
- navigacija
- background behavior
- Safe Stop
- status aktivnog zadatka
- telefon / Bluetooth / platform adapters
- driving context

## Faza C — Taxi poslovanje
- Smart Ride Acceptance
- screen awareness gdje je izvedivo
- test mode
- taxi funkcije
- rezervacije
- poslovni podaci
- analitika

## Faza D — Znanje i svakodnevni pomoćnik
- bilješke
- dnevnik
- memorija
- univerzalna pretraga
- Knowledge Center
- podsjetnici
- telefon / poruke
- prijevod
- YouTube

## Faza E — Napredne mogućnosti
- kamera
- turistički vodič
- Lana Walk
- Kodiaq podaci
- učenje navika
- samodijagnostika i razvojni ciklus

## Faza F — Integracija i stabilizacija
- puni regresijski test
- testovi na više stvarnih uređaja i platformi
- vožnja
- Maps/Uber/Bolt testovi gdje su relevantni i dopušteni
- offline testovi
- dozvole
- oporavak od greške
- performance
- finalni prihvat

---

# 5. Testiranje

Razine:
- unit testovi za čiste funkcije i poslovna pravila;
- integration testovi između modula;
- platform/device testovi na dostupnim fizičkim uređajima i reprezentativnim emulatorima/simulatorima;
- real-world testovi u automobilu uz Bluetooth, navigaciju i stvarne uvjete;
- regression suite nakon većih promjena;
- security/permission testovi;
- offline i degraded-mode testovi;
- internacionalizacijski testovi: jezici, pisma, RTL/LTR, formati brojeva/datuma/novca i promjena jezika u tijeku rada.

Početni fizički uređaji koje posjedujemo služe za rani razvoj, ali test-matrica se ne definira po marki uređaja nego po platformi, verziji OS-a, veličini zaslona, hardverskim sposobnostima i dozvolama.

Obavezni scenariji greške uključuju: internet isključen, odbijen mikrofon/lokacija/kamera/zaslon, druga aplikacija u prvom planu, zaključan ekran, nejasan govor, nepotpuni podaci, vanjski servis ne odgovara, prekid procesa i regresija nakon nove verzije.

## Poseban navigacijski test
Mora se dokazati cijeli tok:
**otvaranje navigacije → prikaz rute → aktivna turn-by-turn navigacija**.

Ručni korak poput "NASTAVI" nije sitnica nego konkretan testni rezultat.

## GOTOVO
Funkcija je GOTOVA tek kada:
1. tehnički radi;
2. integrirana je;
3. testirana je;
4. relevantna regresija prolazi;
5. poznata ograničenja su dokumentirana;
6. Charlie je potvrdio ponašanje.

---

# 6. Sigurnost, privatnost i granice

- Lana ne povećava sama vlastite dozvole.
- Lana ne nabavlja niti otkriva tajne ključeve, tokene ili lozinke.
- LANA 3 ne mijenja stare projekte.
- Produkcijska objava zahtijeva Charliejevo odobrenje.
- Osjetljive akcije imaju potvrdu gdje je potrebna.
- Dostupan podatak nije automatski podatak koji se smije dijeliti izvan sustava.
- Logovi ne smiju nepotrebno sadržavati osjetljive podatke.
- Offline i online načini rada moraju biti jasno odvojeni.
- Svaka automatizacija mora imati prekid i oporavak.
- Kamera i zaslon su opće sposobnosti, ali uvijek pod kontrolom dozvola.
- Platform-specific ograničenja ne zaobilaze se nesigurnim rješenjima.

---

# 7. Kontrola AI i infrastrukturnih troškova

- Česte i osnovne funkcije obrađivati lokalno gdje je tehnički smisleno.
- Cloud/AI pozivati kada donosi stvarnu vrijednost: složeni razgovor, glas, slika, kamera, zaslon ili drugo zahtjevno razumijevanje.
- Prije implementacije troškovno osjetljive funkcije provjeriti tada aktualne cijene API-ja/servisa.
- Procijeniti stvarni trošak po korisniku i tipičnom scenariju.
- Ako je puna izvedba preskupa, projektirati najbolju održivu verziju, a ne tiho ukloniti cijelu funkciju.

---

# 8. Planiranje 89 dana

89 dana nije 89 jednakih jedinica rada. Prvi kompletni razvojni ciklusi služe za mjerenje stvarne brzine.

Mjerimo:
- vrijeme od specifikacije do prvog rada;
- vrijeme do integracije;
- broj testnih ciklusa;
- broj regresija;
- vanjske integracije;
- probleme platforme/OS-a nasuprot problemima našeg koda.

Prioritet:
**temelj → kritične funkcije → vanjske integracije → testiranje → stabilizacija → manje funkcije → vizualno poliranje**.

Precizna procjena ostatka projekta radi se tek nakon prvih stvarno kompletiranih ciklusa.

---

# 9. Završna integracija

Produkcijski kandidat postoji tek kada:
- ključne funkcije rade pojedinačno;
- integracije rade zajedno;
- glas + intent + context + decision + execution rade kao cjelina;
- navigacija je dokazana na stvarnom uređaju;
- pozadinski rad je testiran;
- dozvole i sigurnosne granice su provjerene;
- offline/degraded ponašanje je poznato;
- regression suite prolazi;
- nema poznatog kritičnog kvara bez dokumentirane odluke;
- Charlie je odradio stvarni testni scenarij.

## Trenutni status
- [x] MASTER PLAN v0.1 pronađen i provjeren
- [x] 43 funkcionalne cjeline prenesene
- [x] Izmjena: svi jezici od početka
- [x] Izmjena: uređajno neovisna arhitektura
- [ ] Tehnička arhitektura izvedena iz v0.2
- [ ] Početni skeleton projekta
- [ ] Prvi kompletni razvojni ciklus
