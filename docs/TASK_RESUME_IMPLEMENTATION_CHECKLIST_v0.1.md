# LANA 3 — Resume: implementacijska kontrolna lista v0.1

Status: radna priprema, ne implementacija.

## Potvrđeno iz repozitorija
- Shared task lifecycle i semantička stanja već postoje.
- Task statusi uključuju PAUSED, ACTIVE i terminalna stanja.
- Durable task resume i restart checkpoint nisu potvrđeni.
- Poznata fizička Android baseline verzija dev-215 ne smije se mijenjati bez potrebe.

## Sljedeći kontrolirani korak prije koda
1. Identificirati stvarne Kotlin putanje task lifecycle/coordinator i testova na main.
2. Utvrditi postoji li perzistencija task checkpointa i kakav joj je schema/version ugovor.
3. Odabrati najmanju promjenu unutar shared core, bez diranja Android startup/packaginga.
4. Prvo napisati test koji pada za PAUSED → resume i blokadu terminalnih zadataka.
5. Tek potom dodati minimalnu implementaciju, pokrenuti shared-core testove i CI.
6. Android stvarno ponašanje i restart ostaviti IN TEST do fizičke potvrde.

## Definicija prihvata
- Isti taskId i nextActionId nakon pauze i nastavka.
- Nema dupliciranih side-effectova.
- Revalidacija konteksta, ovlasti i Safe Stop prije EXECUTE.
- Restart bez samostalnog pokretanja radnje.
- Bez regresije postojećih task testova.

## Granice
Nema tvrdnje da su Kotlin putanje pregledane, da je test pokrenut ili da je funkcija gotova. Ovo je dokumentiran plan za jedan implementacijski presjek.
