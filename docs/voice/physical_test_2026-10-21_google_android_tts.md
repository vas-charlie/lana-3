# LANA 3 — Fizička provjera hrvatskog Google TTS-a, 21.10.2026.

**Status: OPAŽENO NEZADOVOLJAVAJUĆE — NEMA PRIHVATA GLASA.**
Ovaj zapis je korisnikov slušni nalaz s uređaja, ne snimljeni audio niti automatizirano mjerenje akustičke točnosti.

## Okruženje

- Aplikacija: LANA 3 Developer Preview `0.1.329-dev`.
- Uređaj: Samsung SM-S928B (Galaxy S24 Ultra), Android 16.
- Ekran: LANA Voice Lab; traženi jezik `hr-HR`.
- Android TTS engine: `com.google.android.tts`; prepoznavanje govora (STT) je prijavljeno dostupnim.
- TTS u početku prikazuje privremeno `TTS se priprema ili nije dostupan`; kasnije prikazuje `TTS spreman`. Taj početni status **nije** dokaz trajne nedostupnosti.
- Preko gumba `Sljedeći hrvatski TTS glas` prijavljeni su sljedeći identifikatori:

| ID prikazan u Voice Labu | Lokalno / mreža prema dijagnostici | Izgovor `me` | Izgovor `tvrđa` | Status |
|---|---|---|---|---|
| `hr-hr-x-hra-local` | `mreža=false` | pogrešan (korisnikov nalaz) | pogrešan (korisnikov nalaz) | NIJE PRIHVAĆEN |
| `hr-hr-x-hrb-local` | `mreža=false` | pogrešan (korisnikov nalaz) | pogrešan (korisnikov nalaz) | NIJE PRIHVAĆEN |
| `hr-hr-x-hrb-network` | `mreža=true` | pogrešan (korisnikov nalaz) | pogrešan (korisnikov nalaz) | NIJE PRIHVAĆEN |

## Točan referentni tekst iz zadnje provjere

> Pogledaj me. Reci mi, ova je daska tvrđa, a ona je najtvrđa.

Prikaz je potvrdio da je tekst pravilno napisan u polju, te `Govor završen` za `hr-hr-x-hrb-network`. Charlie je zasebno javio: *"Sva tri glasa izgovaraju pogrešno 'me' i 'tvrđa'."*

**Granice dokaza:** nemamo audio datoteku, točan fonetski prijepis svakog od tri rezultata, mjerenja latencije, niti potvrdu točnosti riječi `mi` i `najtvrđa` za svaki glas. Ne izmišljati te podatke.

## Odluka za idući ciklus

1. Za kvalitetan hrvatski govor **ne ponavljati ista tri Googleova glasa** bez nove verzije/novog razloga; potvrđeni slušni kriterij nije prošao.
2. Postojeći Android TTS ostaje tehnički testni i potencijalni fallback sloj, ali **nije prihvaćen kao finalni glas Lane**.
3. Sljedeći kandidat za test s istim tekstom: Azure `hr-HR-GabrijelaNeural` (ako službeni demo to dopušta), zatim drugi stvarno dostupan pružatelj. Dok kvaliteta i cijena nisu poznati, dobavljač ostaje neodabran.
4. Ne ugrađivati API ključeve na mobitel i ne kupovati usluge bez provjere cijene, prava korištenja i potrebne privatnosti.
5. Evidencija nije potvrda da je kod budućeg jednim-dodirom testa (#120) bio fizički dostupan: u `dev-329` tog gumba još nema.

Ovaj zapis dopunjuje `docs/VOICE_HR_TTS_EVALUATION_2026-10-20.md`, ne mijenja kod i ne nameće fizički ponovni test.
