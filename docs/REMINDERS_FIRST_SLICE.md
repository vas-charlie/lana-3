# LANA 3 — podsjetnici, prvi lokalni vertikalni rez

**Status:** developer preview, fizički test i Charliejev prihvat nisu provedeni.
**MASTER PLAN:** funkcionalna cjelina 24. Nije završena u punom opsegu.

## Što sada postoji
- Namjenski zaslon **Podsjetnici** s ručnim unosom naziva, kategorije i datuma.
- Pregled **Danas i propušteni rokovi**, **Ovaj tjedan**, **Kasnije**, **Dovršeno**.
- Lokalna SQLite evidencija samo na Android uređaju, ne ovisi o AI servisu.
- Korisnik može označiti zadatak dovršenim i potvrditi brisanje.
- Izbor najave 7 dana prije, 1 dan prije, na dan roka ili bez obavijesti (sve u 09:00 lokalno).
- Android inexact RTC alarm, kanal obavijesti, upit za Android 13+ dopuštenje nakon prve kreirane najave.
- Nakon ponovnog pokretanja ili ažuriranja aplikacije alarmi se ponovno zakazuju ako im vrijeme nije prošlo.

## Granice i rizici
- Alarm **nije precizan po minuti**. Android štednja baterije, restrikcije pozadine ili uskraćena dozvola za obavijesti mogu odgoditi ili spriječiti prikaz.
- Korisnik unosi rokove ručno. **Nema automatskog uvoza iz ChatGPT razgovora, memorije ili kalendara**.
- Nema sinkronizacije telefon/tablet i nema cloud backupa; postojećim pravilom aplikacije backup je isključen.
- Nema uređivanja postojećeg roka, godišnjeg ponavljanja, odgode, više obavijesti ili automatskog izvlačenja rokova iz prirodnog govora.
- Ako je odabrano vrijeme najave već prošlo, zadatak se sprema, ali alarm se ne zakazuje. Takvi zadaci i dalje su vidljivi na zaslonu.
- Ne upisivati osobne podatke ili obveze korisnika u javni GitHub repozitorij.

## Testna vrata
1. Kotlin shared-core testovi: valjan/nevаljan rok; propušteni rokovi; završavanje i brisanje; ponovno učitavanje stanja.
2. Android lint, build i provjera da se deklarirane aktivnosti i receiveri nalaze u APK-u.
3. Fizički test S24 Ultra: spremanje, zatvaranje/ponovno otvaranje aplikacije, obavijesti s dopuštenjem i bez njega, nakon restarta, potvrda točnosti lokalnog sata/vremenske zone.
4. Charlie potvrđuje ponašanje na uređaju. Tek potom podići status odgovarajućeg dijela funkcije.

**Za buduću integraciju:** povezati centralnu osobnu evidenciju rokova i kalendar s LANA 3 uz korisnikovu privolu, sljediv izvor i kontrolu duplikata.
