# LANA 3 — Govor: hrvatski TTS, evaluacija i priprema integracije

**Status: ISTRAŽIVANJE / ČEKA FIZIČKI TEST. Nije prihvaćeno.**  
**Datum provjere javne dokumentacije:** 2026-10-20.  
**Ne mijenja aplikacijski kod, ne odabire dobavljača, ne naručuje plaćenu uslugu, ne dira postojeći Android TTS.**

## 1. Polazište i granica

- LANA 3 ima `SpeechInputPort` (STT) i `SpeechOutputPort` (TTS), te Android Voice Lab kao testni adapter. **To nije dovršeni produkcijski govorni sustav.**
- Test Googleova TTS-a na S24 Ultra ukazao je na pogrešan izgovor „tvrđa” → „tvrda” te „me” → „mi” u određenim kontekstima. „Najtvrđa” i „tvrđavica” prošle su ranije izgovorne provjere. **Ovo je zapis ljudskog opažanja, ne automatizirani laboratorijski dokaz.**
- Samsungova instalirana TTS ponuda nije imala hrvatski. ElevenLabs web-demo vratio je generičku pogrešku; iz toga **ne zaključivati** da cijela usluga ne radi. U Microsoftovoj galeriji pronađen je hrvatski ženski glas Gabrijela, ali **još nije reproduciran naš referentni tekst**.
- **Osobna Lana nije komercijalni avatar.** Javna platforma može kasnije koristiti zajedničku tehnologiju govora kroz odvojene konfiguracije, račune i prava pristupa. Nema dijeljenja privatne memorije ili osobnog identiteta.
- Ime, izgled i kvaliteta budućih javnih avatara nisu razlog da sada kupujemo TTS ili mijenjamo sigurnu Android bazu.

## 2. Neovisna provjera dobavljača (javne službene stranice)

| Kandidat | Hrvatski `hr-HR` | Što je dokumentirano | Što **nije** potvrđeno |
|---|---|---|---|
| Android/Google TTS na uređaju | Da na testnom S24 Ultra | Postojeći offline/OS adapter; izbjegava zasebnu cloud naplatu API-ja | Ispravan izgovor problematičnih riječi, jednaka dostupnost na svim uređajima |
| Microsoft Azure Speech | Da, `hr-HR-GabrijelaNeural` i `hr-HR-SreckoNeural` | Standard neural glasovi, Speech SSML i REST API; F0: 500.000 neural znakova/mjesečno uz uvjete besplatnog resursa | Kvaliteta i izgovor na našem testu, cijena u konkretnoj regiji/pretplati, latencija na mobitelu |
| Google Cloud Text-to-Speech Chirp 3 HD | Da, dokumentirano `hr-HR` | Službeni cjenik: prvih 1.000.000 znakova/mjesec besplatno za Chirp 3 HD, zatim **USD 30 / milijun znakova**; za API treba omogućiti naplatu | Kvaliteta hrvatskog izgovora u našim rečenicama, realna latencija |
| ElevenLabs | Hrvatski podržan u višejezičnim modelima | Free plan 10.000 kredita/mj.; besplatni plan ne daje komercijalnu licencu | Demonstracija nije uspjela; nema poštene ocjene glasa niti jedinstvene cijene po znaku bez izbora modela i plana |

**Službeni izvori (ponovno provjeriti prije ugovaranja):**
- Azure lokalizacije: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/language-support?tabs=text-to-speech
- Azure cijene: https://azure.microsoft.com/en-us/pricing/details/speech/
- Azure REST/SSML: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/rest-text-to-speech
- Azure SSML: https://learn.microsoft.com/en-us/azure/ai-services/speech-service/speech-synthesis-markup-pronunciation
- Google jezici Chirp 3: https://docs.cloud.google.com/text-to-speech/docs/chirp3-hd
- Google cijene: https://cloud.google.com/text-to-speech/pricing
- ElevenLabs cijene: https://elevenlabs.io/pricing
- ElevenLabs licenca: https://help.elevenlabs.io/hc/en-us/articles/13313564601361-Can-I-publish-the-content-I-generate-on-the-platform

Napomena: kvote za mali prototip **nisu** besplatna infrastruktura za milijune korisnika; privremene/promotivne ponude, porez, regija, naknade i komercijalni uvjeti ostaju za kasniju validaciju.

## 3. Korpus za objektivnu usporedbu

**Isti tekst, bez ručnog mijenjanja pojedinih riječi, s istim slušalicama/zvučnikom i normalnom brzinom.**

### TTS-HR-01: minimalni kontrasti
1. „Čekaj **me** ispred ulaza. Reci **mi** kada stižeš.”
2. „Pogledaj **me**. Čuješ li **me**? Poslušaj **me**.”
3. „Ova je daska **tvrđa** od one. Treća je **najtvrđa**.”
4. „Čekam te kod **Tvrđe**. **Tvrđavica** je odmah iza ugla.”

### TTS-HR-02: rečenice u svakodnevnom kontekstu
„Molim te, pogledaj me i reci mi razumiješ li me. Čekam te kod Tvrđe. Ova je daska tvrđa, ali treća je najtvrđa. Tvrđavica je odmah iza ugla.”

### TTS-HR-03: produkcijska korisnost (kasnije)
Brojevi, valuta, datumi, adrese, kratke naredbe, duži odgovor, prekid govora, slaba mreža i Bluetooth. Ne proglašavati gotovim na temelju samo hrvatskog demo-teksta.

Ocjenjivanje (za svaki glas, uz datum, platformu i model):
- razlikovanje **me/mi**: prolazi / pada / ne može se utvrditi;
- glas **đ** u „tvrđa”, „Tvrđe”, „najtvrđa”, „tvrđavica”: prolazi / pada / ne može se utvrditi;
- razumljivost, prirodnost, naglasci: 1–5 (subjektivna ocjena);
- kašnjenje prvog glasa, prekid, stabilnost: izmjereno / neizmjereno;
- presuda: **NEOCIJENJENO** dok se glas ne čuje; nikada automatski „PROLAZI” samo zato što usluga podržava hrvatski.

Po želji zatražiti mišljenje dobrovoljnog jezičnog recenzenta; **ne** predstavljati takvu ocjenu kao dokaz objektivne tehničke ispravnosti.

## 4. Integracijska arhitektura bez vendor lock-ina

```
LANA 3 tekst
    → SpeechOutputPort (postojeći neutralni ugovor)
    → Selection Policy [korisnikov odobreni glas, jezik, dostupnost,
                         trošak, privatnost, offline opcija]
    → AndroidTTSAdapter | AzureTTSAdapter | GoogleCloudTTSAdapter | ...
    → Audio stream i potvrđeni događaji → UI/avatar
```

**Nije implementirano; dizajnerski prijedlog.** Prvo provjeriti kompatibilnost postojećeg `SpeechOutputPort` s eventualnim cloud streamom i događajima; ako su potrebne promjene, odvojeni ADR i tests-first PR. Ne pretpostavljati točne foneme/viseme iz događaja `RangeStarted`.

Sigurnosna vrata:
- API ključevi **nikada** u Android aplikaciji, javnom repozitoriju ili logovima. Cloud TTS se koristi preko kontroliranog backenda/token-proxyja s ograničenjima, autentikacijom i poštivanjem uvjeta pružatelja.
- Ne slati osobne razgovore ili osjetljive podatke trećim stranama bez odgovarajuće obavijesti, izbora korisnika i pravne provjere; ne pohranjivati audio/tekst bez potrebe.
- Uvesti limite troška po korisniku, stop/cancel, rate limit, timeout, offline/degraded fallback i potrošnju po dobavljaču.
- Promjene ne smiju pokvariti poznatu Android fizičku bazu; CI, APK inspekcija i test na stvarnom S24 Ultra su zasebne obveze.

## 5. Mikroekonomika (posebno važna za javnu platformu)

**Primjer, ne predviđanje:** Google Chirp 3 HD ima **USD 30 / 1.000.000 znakova** nakon besplatne kvote.

| Znakova / aktivni korisnik mjesečno | 1.000 korisnika / mj. (bez kvote) | 10.000 korisnika / mj. (bez kvote) |
|---:|---:|---:|
| 100 | USD 3 | USD 30 |
| 1.000 | USD 30 | USD 300 |
| 5.000 | USD 150 | USD 1.500 |
| 20.000 | USD 600 | USD 6.000 |

Ovo je **samo izlazni TTS**. Ne uključuje STT, LLM, mrežu, poreze, naknade trgovina, avatar/rendering ili podršku. Kvote se računaju na razini odgovarajućeg računa/usluge, ne zasebno za svakog korisnika. Cijena funkcije od nekoliko centi **ne smije** obećavati neograničenu cloud obradu. Model naplate ostaje poslovna odluka korisnika i nije ovime definiran.

## 6. Sljedeći najbolji pokus, kad Charlie bude imao vremena

**Jedna radnja, bez Samsung postavki ili registracije**: u postojećoj Microsoftovoj Voice Gallery odabrati `hr-HR-GabrijelaNeural`, pokušati `Try it out` s TTS-HR-01; ako web-stranica na mobitelu ne dopušta uređivanje teksta, isprobati na računalu **ili označiti demo kao nedostupan**. Nema ponavljanja istih koraka bez nove informacije.

Kada test da rezultat:
1. upisati izgovor, kvalitetu i radi li demonstracija;
2. usporediti s lokalnim Google TTS-om;
3. provjeriti stvarni API trošak i uvjete nakon izbora jednog glasa;
4. tek tada odrediti mali integracijski PR i fizičku potvrdu.

**Projektni status: istraživanje gotovo; izgovor Gabrijele još NIJE potvrđen.** Ne kupovati, ne mijenjati Googleov zadani TTS, ne dovršavati cloud integraciju bez tog rezultata.
