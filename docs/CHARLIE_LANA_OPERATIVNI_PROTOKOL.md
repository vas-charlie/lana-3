# LANA 3 — OPERATIVNI PROTOKOL CHARLIE + LANA

## Svrha

Ovaj dokument je obvezna operativna memorija za rad na LANI 3.

MASTER PLAN govori **što gradimo**. Ovaj protokol govori **kako radimo**.

Prije ozbiljne analize, izmjene koda, builda, releasea ili dijagnostike treba prvo pročitati ovaj dokument, `KNOWN_GOOD_PHYSICAL_BASELINES.md` i `LANA_MIRROR_RULE.md`, pa provjeriti aktualno stanje repozitorija. Ne oslanjati se na spontano sjećanje razgovora.

---

## 1. Charliejeva opažanja su hipoteze koje se moraju provjeriti

Kad Charlie kaže da nešto "možda" izgleda kao problem:
- to nije tvrdnja;
- ne tretira se kao dokaz;
- ne odbacuje se bez provjere;
- prvo se provjerava ako je konkretno i jeftino provjerljivo.

Primjer obrasca:
**opažanje → provjera → dokaz ili odbacivanje → tek onda sljedeća hipoteza**.

Cilj nije dokazati da je Charlie ili Lana bio u pravu. Cilj je najbrže doći do stvarnog uzroka.

---

## 2. Redoslijed zaključivanja

Uvijek jasno razlikovati:

1. **SUMNJA / HIPOTEZA** — mogući uzrok.
2. **DOKAZ** — konkretan nalaz iz koda, konfiguracije, loga, artefakta ili uređaja.
3. **POTVRĐENI UZROK** — dokaz objašnjava kvar.
4. **POPRAVAK** — kontrolirana promjena usmjerena na potvrđeni uzrok.
5. **REGRESIJSKA ZAŠTITA** — test ili provjera koja sprečava povratak istog kvara.
6. **FIZIČKA POTVRDA** — stvarno ponašanje na uređaju kada je uređaj dio problema.
7. **PRIHVAT** — tek nakon relevantnih provjera može se reći da nešto radi pouzdano.

Ne koristiti izraze "riješeno", "radi" ili "gotovo" prije odgovarajuće potvrde.

---

## 3. Najprije provjeri najjednostavniji i najjeftiniji uzrok

Prije kompleksne teorije:
- provjeri naziv, vrijednost, dopuštenje, putanju, branch, manifest, plugin, resource i stvarni sadržaj artefakta;
- provjeri je li ono za što vjerujemo da postoji zaista završilo u `main`;
- provjeri je li ono za što vjerujemo da je u APK-u zaista zapakirano u APK.

Ne preskakati jednostavan signal zato što izgleda "previše trivijalno".

---

## 4. Stvarni artefakt je važniji od pretpostavke o buildu

Zeleni CI znači samo da su prošle provjere koje CI stvarno provodi.

Ne znači automatski:
- da APK sadrži očekivane klase;
- da se aplikacija pokreće;
- da je ikona ispravna;
- da platforma radi na stvarnom uređaju;
- da je funkcija fizički prihvaćena.

Za release-kritične promjene pregledati stvarni proizvedeni APK/artefakt kada je to izvedivo.

---

## 5. Pravilo CLEAN REBUILD za neispravnu komponentu

Ne prepravljati cijelu LANA 3 zato što je jedan dio neispravan.

Ali kada je mala komponenta:
- korumpirana;
- strukturno pogrešno napravljena;
- višestruko krpana bez pouzdanog rezultata;
- lakše i sigurnije reproducirati iz poznatog ispravnog izvora;

prednost ima **čista nova izvedba te komponente** umjesto daljnjeg krpanja stare.

Postupak:
1. sačuvaj dokazano dobre dijelove;
2. izoliraj neispravnu komponentu;
3. vrati se na pouzdan izvor/ugovor;
4. napravi komponentu iznova minimalno i čisto;
5. testiraj samostalno;
6. integriraj;
7. dodaj regresijsku provjeru.

Primjer: korumpirani launcher asset ne "liječiti" dodatnim konverzijama. Ponovno ga izraditi iz zdravog službenog izvornog PNG-a u ispravnim Android formatima i veličinama.

---

## 6. Jedna kontrolirana promjena po uzroku

Ne mijenjati više nepovezanih stvari u istom pokušaju kada pokušavamo dokazati uzrok.

Ako se istodobno promijene startup, ikona, updater i UI, a rezultat postane bolji ili lošiji, gubi se uzročna veza.

Iznimka je samo kada promjene zajedno čine jednu nedjeljivu tehničku cjelinu.

---

## 7. Svaki ozbiljan bug ostavlja test iza sebe

Kad se potvrdi uzrok:
- popravak nije dovoljan;
- dodaje se provjera koja bi pala na starom kvaru;
- provjera treba štititi stvarno svojstvo, ne samo određenu implementaciju.

Primjer iz 07.10.2026.:
stari Android build mogao je biti zelen iako APK nije sadržavao `MainActivity`.
Nakon potvrde uzroka dodana je APK-level provjera DEX klasa.

---

## 8. Fizički uređaj ima zadnju riječ za fizičko ponašanje

Za Android funkcije koje ovise o stvarnom uređaju:
- CI = tehnička provjera;
- APK inspekcija = artefaktna provjera;
- S24 Ultra / drugi uređaj = fizička provjera.

Ne proglašavati fizičko ponašanje prihvaćenim samo zato što emulator, compile ili CI prolazi.

---

## 9. Poznato dobro stanje se ne dira bez razloga

Kada komponenta postane dokazano dobra i fizički prihvaćena:
- evidentirati poznatu dobru verziju/commit;
- nove promjene raditi odvojeno;
- zadržati regresijske testove;
- ne mijenjati dokazano dobar dio usputno ako nije dio zadatka.

Zaštita stabilnosti dolazi tek nakon dokazane stabilnosti.

---

## 10. Obvezni početak svakog ozbiljnog rada

Prije prve promjene:

1. pročitaj ovaj protokol;
2. pročitaj `KNOWN_GOOD_PHYSICAL_BASELINES.md`;
3. pročitaj `LANA_MIRROR_RULE.md`;
4. pročitaj relevantni dio MASTER PLANA / Engineering Baselinea;
5. provjeri aktualni `main`, otvorene PR-ove i stvarno stanje;
6. odvoji potvrđene činjenice od pretpostavki;
7. navedi što se pokušava dokazati;
8. tek tada mijenjaj kod.

---

## 10A. Temeljni standard izvršenja: 8 načela

Prije pokretanja bilo kojeg ozbiljnog rada na LANI 3, ova načela moraju se ponovno pročitati zajedno s ovim protokolom. Redoslijed izražava prioritet sigurnosti i kvalitete; brzina nikada ne poništava prethodna načela.

1. **OPREZ** — prvo zaštititi poznato dobro stanje, podatke, kompatibilnost i mogućnost sigurnog povratka.
2. **PAMETNO** — raditi prema dokazima, ovisnostima i najjeftinijoj pouzdanoj provjeri; ne trošiti vrijeme na rad koji ne smanjuje neizvjesnost ili ne donosi vrijednost.
3. **OZBILJNO** — svaku promjenu tretirati kao dio stvarnog proizvoda, s jasnim ciljem, granicom i kriterijem prihvata.
4. **PROFESIONALNO** — koristiti kontrolirane grane/PR-ove, pregledive promjene, testove, dokumentirane ugovore i sljedivost odluka.
5. **MAX PARALELNO** — neovisne tokove izvršavati istodobno kad imaju jasne granice i ne povećavaju nekontrolirano integracijski rizik. Zajedničke ugovore i ista područja koda ne mijenjati konkurentno bez koordinacije.
6. **MAX BRZO** — uklanjati nepotrebno čekanje i serijski rad, ali nikada preskakanjem dokaza, testova, sigurnosnih granica ili potrebne fizičke potvrde.
7. **ODGOVORNO** — ne skrivati neizvjesnost, kvar, trošak ni rizik. Za nepovratne, financijske, sigurnosno osjetljive ili fizički rizične korake tražiti odgovarajuću potvrdu.
8. **POUZDANO** — rezultat vrijedi tek kada je potvrđen na razini koju zahtijeva njegova priroda: kod/test, integracija, stvarni artefakt i, kada je potrebno, fizički uređaj.

### Pravilo paralelizacije

**Paralelizacija je ubrzanje samo dok ne smanjuje dokazivost, sigurnost ili pouzdanost.** Ako dva toka dijele isti nestabilni ugovor, isti kritični kod ili ne mogu biti neovisno verificirana, privremeno ih sekvencijalizirati. `main` ostaje jedina integrirana istina projekta.

### Obvezni pre-flight

Prije pokretanja bilo čega što mijenja projekt: pročitaj 8 načela, izvrši korake iz odjeljka 10, odredi ovisnosti i vlasništvo promjene, provjeri može li se sigurno paralelizirati te definiraj dokaz kojim će se rezultat potvrditi. Tek tada pokreni izvedbu.

---

## 11. Kratka arhiva važnih lekcija

### 864 / B64
Konkretno sumnjiv naziv ne odbacivati kao vizualnu ili nevažnu sitnicu prije provjere. Sitna razlika u nazivu može biti stvarni uzrok.

### GitHub pristup / 403
Prvo dokazati dopuštenja i stvarni pristup prije teorije o kodu.

### Android APK bez aplikacijskih Kotlin klasa — 07.10.2026.
CI je gradio i potpisivao APK, ali Android modul nije primjenjivao Kotlin Android plugin. Manifest je referencirao `MainActivity`, a klasa nije bila u DEX-u. To je potvrđen primjer da "zeleni build" nije isto što i "ispravan proizvod".

### Launcher ikona — 07.10.2026.
U release je završio oštećen WebP launcher asset. Službeni izvorni PNG je zdrav. Pravilo: ne popravljati korumpirani izvedeni asset; generirati novi launcher set iz provjerenog izvora.

---

## 12. Glavno pravilo

**Ne pogađaj. Ne krpaj naslijepo. Ne vjeruj zelenoj kvačici više nego stvarnom dokazu.**

Charlie donosi opažanja, taktiku i stvarni kontekst.
Lana donosi provjeru, analizu, implementaciju i testiranje.
Zaključak nastaje tek kad se ta dva pogleda spoje u dokaz.
