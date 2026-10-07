# LANA 3 — Implementation status matrix

## Purpose

This matrix prevents code volume from being confused with product completion.

Status meanings:

- **NOT STARTED** — no meaningful implementation beyond architecture notes.
- **FOUNDATION** — contracts/policy/skeleton exist, but the functional unit is not yet a usable end-to-end feature.
- **IN TEST** — meaningful end-to-end or platform implementation exists, but physical/product acceptance is still pending.
- **PARTIAL** — useful behavior exists, but only a subset of the MASTER PLAN unit is implemented.
- **ACCEPTED** — relevant technical, integration, regression and Charlie acceptance gates have all passed.

No unit is marked ACCEPTED solely because CI is green.

| # | MASTER PLAN unit | Current status | Current evidence / boundary |
|---:|---|---|---|
| 1 | Razgovor i osobnost | FOUNDATION | UI/state/avatar semantics exist; no production conversational AI session yet. |
| 2 | Glas | IN TEST | Shared STT/TTS ports, Android Voice Lab, permission/error handling; physical/Bluetooth/language matrix pending. |
| 3 | Razumijevanje namjere | PARTIAL | Typed intents and deterministic handling exist for selected flows; natural-language normalization remains incomplete. |
| 4 | Smart Ride Acceptance | IN TEST | Deterministic €/km and €/h rules now cover pickup + passenger trip and an optional explicit empty return; validation, structured decision evidence/reasons and Android test UI exist; real taxi acceptance testing pending. |
| 5 | Decision Engine | PARTIAL | Shared DecisionEngine and first brain→hands slice exist; broad cross-domain decision coverage remains incomplete. |
| 6 | Navigacija | FOUNDATION | Shared navigation intent/policy contracts exist; real turn-by-turn Android/Maps execution not implemented. |
| 7 | Lokacija | FOUNDATION | Location context/policies and Android capability readiness exist; full real location adapter/use cases remain pending. |
| 8 | Kamera / Lanine oči | FOUNDATION | Permission/capability foundation only; camera capture/understanding pipeline not implemented. |
| 9 | Svijest o ekranu | NOT STARTED | Architecture boundary documented; no MediaProjection/screen-context implementation. |
| 10 | Rad u pozadini | FOUNDATION | Shared background behavior policy exists; real Android lifecycle/background execution not physically proven. |
| 11 | Turistički vodič u vozilu | NOT STARTED | No feature implementation. |
| 12 | Lana Walk | NOT STARTED | No feature implementation. |
| 13 | Bilješke | IN TEST | Local SQLite CRUD/search, shared service/repository, voice prefill and guarded save flow; physical test pending. |
| 14 | Lana dnevnik | NOT STARTED | No durable diary/event flow. |
| 15 | Memorija | FOUNDATION | Data/memory boundaries documented; no LANA product memory subsystem yet. |
| 16 | Poslovni podaci | NOT STARTED | MASTER PLAN boundary exists; VAŠ CHARLIE Business OS integration not implemented in LANA 3. |
| 17 | Taxi funkcije | PARTIAL | Smart Ride is implemented as one deterministic taxi function; broader taxi toolset remains pending. |
| 18 | Rezervacije | NOT STARTED | No reservation data model/workflow. |
| 19 | Telefon i komunikacija | FOUNDATION | Shared communication policy exists; real call/message Android adapters and confirmation flows are pending. |
| 20 | Glazba / YouTube | NOT STARTED | No media adapter. |
| 21 | Prijevod i jezici | FOUNDATION | Language-neutral core boundaries plus Croatian/English Android resources; translation/detection/provider quality remains pending. |
| 22 | Kodiaq kao poslovni sustav | NOT STARTED | No verified vehicle-data adapter or telemetry. |
| 23 | Analitika | NOT STARTED | No LANA 3 analytics subsystem. |
| 24 | Podsjetnici i zadaci | FOUNDATION | Task model/lifecycle/priorities exist; durable scheduling/reminder delivery is not implemented. |
| 25 | Univerzalna pretraga | FOUNDATION | Notes search exists only inside Notes Lab; cross-store universal search is not implemented. |
| 26 | Više uređaja | FOUNDATION | Device-independent architecture and backup boundary exist; account/sync/handoff not implemented. |
| 27 | Sigurnost i dozvole | IN TEST | Authorization modes, capability gates, contextual permission work, backup/privacy/security CI and signing protections exist; physical permission matrix pending. |
| 28 | Offline | PARTIAL | Local notes, deterministic Smart Ride and offline/degraded policies exist; broader offline capability set remains incomplete. |
| 29 | Vizualni identitet | IN TEST | Developer preview states, semantic avatar presenter and local IDLE micro-motion exist; final renderer/asset/adaptive layout pending. |
| 30 | Arhitektura projekta | IN TEST | KMP shared core + Android adapters, ADR discipline, CI and modular boundaries are active; broader platform proof pending. |
| 31 | Laninin mozak i ruke | IN TEST | Save-note vertical slice proves DecisionEngine → authorization → execution boundary; more action types pending. |
| 32 | Autorizacijski sustav | IN TEST | VIEW/SUGGEST/ASK_CONFIRMATION/EXECUTE model and OBSERVE/SUGGEST/CONFIRM/EXECUTE gate covered by tests; high-impact integrations pending. |
| 33 | Safe Stop | FOUNDATION | Shared Safe Stop semantics/tests exist; interrupting real external/platform work remains unproven. |
| 34 | “Što trenutno radiš?” | FOUNDATION | Semantic active-task resolver exists; full user-facing task status experience not integrated. |
| 35 | Prioriteti | FOUNDATION | Deterministic task priority/arbitration exists; real concurrent platform tasks remain unproven. |
| 36 | Objašnjenje odluke | PARTIAL | Smart Ride now emits normalized reason codes, applied thresholds and failed profitability metrics rendered through localized UI; cross-domain explanation/audit coverage remains incomplete. |
| 37 | Test mode | IN TEST | OBSERVE → SUGGEST → CONFIRM → EXECUTE gate is implemented/tested; more real actions must pass through it. |
| 38 | Knowledge Center | NOT STARTED | No document knowledge/index subsystem. |
| 39 | “Ne gnjavi me” | FOUNDATION | Background/degraded policies exist; durable delegated monitoring/notification workflow not implemented. |
| 40 | Kontekst vožnje | FOUNDATION | Driving/context architecture exists; reliable Android driving-state detection and UI adaptation remain pending. |
| 41 | Učenje navika | NOT STARTED | No habit-learning/proposal subsystem. |
| 42 | “Lana ne pogađa” | PARTIAL | Unknown/denied capability policies and rejection paths exist; uncertainty handling is not yet universal. |
| 43 | Samodijagnostika / samopopravak / nadogradnja | IN TEST | Structured diagnostics, lint/tests, secure updater policy, signing smoke test and update verification exist; real signed update/rollback and production approval loop pending. |

## Current manual gates

The largest immediate gates that code alone cannot close are:

1. finish private GitHub signing secrets;
2. run the real signed-release job and verify that signing/publishing steps actually execute;
3. perform first signed installation on the S24 Ultra;
4. run the physical Android acceptance checklist;
5. publish a later signed build and prove in-place automatic update continuity;
6. later repeat relevant device tests on the intended tablet.

## Progress interpretation

A rough project percentage should be based on this matrix plus acceptance depth, not on file/commit count.

Many foundational units are now real and testable, but physical Android verification, production conversational intelligence, integrations, business data, navigation, camera/screen, multi-device sync and the knowledge/memory layers still represent large remaining work.
