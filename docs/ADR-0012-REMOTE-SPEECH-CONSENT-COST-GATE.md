# ADR-0012 — Granica privole i troškova za budući vanjski glas

**Status:** FOUNDATION / NIJE INTEGRIRANO U IZVRŠAVANJE  
**Datum:** 2026-10-21  
**Odluka o dobavljaču:** NEMA  
**Plaćena usluga:** NIJE AKTIVIRANA

## Problem

LANA 3 sada ima samo testni Android TTS. Tri provjerena hrvatska Googleova glasa nisu zadovoljila fizički zahtjev za riječima `me` i `tvrđa`. Kvalitetniji cloud glas možda bude potreban, ali slanje privatnog teksta trećoj strani i obračun po potrošnji ne smiju se aktivirati bez odobrenja, realne cijene i sigurnosnih granica.

Za buduću povoljnu javnu platformu nekoliko centi po funkciji mora imati stvarno ograničenu potrošnju, a ne neograničene skrivene AI troškove.

## Donesena tehnička odluka

Zajednička Kotlin jezgra dobiva `RemoteSpeechGate`, vendor-neutralnu **provjeru prije mogućeg poziva**, ne funkciju za cloud TTS i ne sustav naplate.

Prije nego što postane podoban za backend rezervaciju, svaki zahtjev mora imati:

- neprazan tekst te izričito odabranog dobavljača i glas;
- dobavljača odobrenog za upotrebu i glas s fizički prihvaćenom kvalitetom;
- podršku za traženi jezik;
- korisnički pristanak vezan **točno uz tog dobavljača**;
- dopušteno slanje govornog teksta vanjskom sustavu;
- spreman sigurni backend i dostupnog pružatelja usluge;
- važeću, provjerenu procjenu troška, vezanu uz tog dobavljača i glas;
- poznato, nenegativno ograničenje po zahtjevu i preostali mjesečni budžet;
- cijenu koja ne prelazi niti jedno od dva ograničenja;
- provjerenu troslovnu oznaku obračunske valute koja se mora podudarati između ponude i oba limita, inače je odgovor **BLOCKED**.

Nedostatak dokaza znači **BLOCKED**, ne pretpostavljeni uspjeh. Ne postoji hard-kodirana valuta, dobavljač, jedinična cijena ili besplatna kvota.

### Što znači cijena u `micros`

`Long` vrijednost označava milijuntinu obračunske valute odabrane na backendu (npr. 1 EUR = 1.000.000 micros). Svi limiti i quote moraju biti izraženi u **istoj valuti**; preflight sada eksplicitno uspoređuje valutu ponude s valutom limita, odbijajući nepoznate ili neusklađene valute. Format su tri velika slova, ali postojanje i zakonitost valute mora potvrditi backend. Međovalutna konverzija, porezi, naknade i zaokruživanje moraju biti obrađeni i provjereni prije ovog koraka. Ne slati povjerljive iznose ili račune u logove.

## Što ovaj PR namjerno NE radi

- Ne bira Azure, Google Cloud ili ElevenLabs, niti proglašava Gabrijelu kvalitetnom prije testa.
- Ne dodaje mrežni adapter, ključ, endpoint, API pretplatu ili slanje osobnog sadržaja.
- Ne spaja se s Voice Labom: postojeća aplikacija ostaje bez pozivanja cloud TTS-a.
- Ne čuva privole, ne računa stvarnu dobavljačevu tarifu, ne skida novac, ne rezervira budžet i ne autorizira korisnički račun.
- Ne jamči potpunu pravnu usklađenost s GDPR-om, uvjetima servisa ili licencama; potrebne su zasebne provjere.

**Posebno važno:** `EligibleForBackendReservation` **NIJE odobrenje za plaćanje**. Uslužni backend mora neovisno potvrditi identitet korisnika, provjeriti privolu i pravo pristupa, ponovno izračunati cijenu, **atomski rezervirati** preostali limit i provjeriti stvarni račun dobavljača. Inače paralelni pozivi mogu probiti limit i zaobići ovu provjeru.

## Granice buduće integracije

```
SpeechOutputRequest
  → lokalni/offline glas (ako ispunjava kvalitetu i dostupnost)
  → ako korisnik dopušta cloud i postoji odobren glas:
      RemoteSpeechGate (bez mrežnih poziva)
        → BLOCKED: ostaje lokalni ili jasan nedostupan/degradiran status
        → EligibleForBackendReservation:
            provjereni autentificirani backend, atomarna rezervacija,
            server-side API ključ, dopuštenje i limit
               → provider audio → Android player
```

Povijest privatnih razgovora i prava osobne Lane ne smiju se neprimjetno dijeliti s javnom aplikacijom. Korisnikov identitet i pretplatu također ne smije se izvoditi iz samo klijentskih zastavica.

## Dokazi i kriteriji

- `shared` Kotlin testovi obuhvaćaju dopuštene slučajeve, blokade bez privole ili kvalitete, nepoznatu cijenu, pogrešnog dobavljača/glasa, nepoznate i negativne limite, prekoračenje budžeta i točne granice.
- Testovi su deterministički, offline i besplatni za izvršavanje.
- PR ostaje *draft* dok se kod ne pregleda i plan integracije ne uskladi s odabirom stvarnog glasa.
- Fizički test cloud TTS-a još nije obavljen i ovaj ADR to ne prikriva.
