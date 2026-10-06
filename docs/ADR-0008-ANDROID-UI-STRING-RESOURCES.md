# ADR-0008 — Android UI string-resource boundary

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `main` with incremental `i18n/*` slices

## Decision

Android user-facing text must move out of activity code and into Android string resources.

The first controlled slice covers `MainActivity` and establishes:

- Croatian default resources;
- an initial English resource set;
- translatable developer/status copy;
- non-translatable machine state labels such as IDLE/LISTENING/THINKING;
- formatted resources instead of string concatenation in UI code;
- the application label through `@string/app_name`.

## Why

LANA 3 is multilingual by architecture. Hard-coded display text in Kotlin makes later localization slower, increases lint noise, and encourages business/UI logic to become entangled with one language.

This change is deliberately incremental. MainActivity, Voice Lab, Notes Lab, Smart Ride Lab and device-readiness presentation are merged. Auto-updater status is the next slice, using typed updater events so UI text remains in resources.

## Important limits

- This does not implement LANA's conversational language selection.
- Android resource locale follows Android resource resolution, not the shared `LanguageContext`.
- Some non-UI diagnostic/developer strings remain in code.
- Speech-adapter diagnostic/error messages remain a separate boundary decision because some are machine-facing and some surface in the Voice Lab.
- More languages are not added until the resource boundary is stable.

## Acceptance criteria

- [x] MainActivity no longer hard-codes its principal user-facing labels/status text.
- [x] Croatian resources are the default Android UI copy for the migrated main-screen slice.
- [x] English resources exist for the migrated main-screen strings.
- [x] formatted main-screen readiness/status strings avoid UI concatenation.
- [x] MainActivity slice passes Android lint and developer-preview build.
- [x] Voice Lab resource migration passes Android lint and developer-preview build.
- [x] Notes Lab resource migration passes Android lint and developer-preview build.
- [x] Smart Ride Lab resource migration passes Android lint and developer-preview build.
- [x] Device-readiness presentation uses resources while the probe remains presentation-neutral.
- [ ] Auto-updater emits typed status events and UI/notification text comes from resources.
- [ ] physical locale-switch behavior remains a later device test.

## Lifecycle status

Architecture → Incremental localization → **IN TEST** → Broader UI migration → Physical locale test → Charlie acceptance
