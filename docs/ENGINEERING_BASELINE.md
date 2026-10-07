# LANA 3 — ENGINEERING BASELINE

## Status
This repository is the active engineering workshop for LANA 3.

## Source of truth
The approved LANA 3 MASTER PLAN contains 43 functional units. Those 43 units remain the functional scope and are not to be silently changed, renumbered, or expanded during implementation.

## Confirmed amendments
1. LANA 3 is multilingual from the initial architecture. The system must be designed for all languages rather than a fixed shortlist or a later language retrofit.
2. LANA 3 is device- and vendor-independent by architecture. Samsung devices are initial physical test devices, not a product boundary. Platform-specific capabilities must live behind adapters/interfaces, with real limitations tested and documented.
3. Android is the first physical client, not the definition of LANA. iOS, desktop, web and future platforms must be addable through platform adapters without rewriting shared business logic.
4. LANA follows a local-first knowledge policy: verified local knowledge should be reused before paid external reasoning, while unverified, stale, low-confidence or privacy-incompatible material must be rechecked rather than guessed.

## Engineering rules
1. Inspect the existing state before making a change.
2. Make one controlled change at a time.
3. Test the change before treating it as complete.
4. Commit only work that has been checked.
5. Do not invent missing requirements. Return to the approved MASTER PLAN when detail is required.
6. Keep AI understanding/reasoning separated from deterministic tool execution.
7. Camera and screen access are general multimodal capabilities across LANA 3, subject to user permission, not tourism-only features.
8. Multimodal context may include text, original audio/voice tone, camera, screen, and conversation context, subject to permissions and technical feasibility.
9. Prefer local/on-device processing for basic and frequent functions when technically sensible.
10. Use more expensive cloud/AI processing only when needed. Before implementing a cost-sensitive capability, verify current service/API pricing and estimate realistic per-user cost.
11. If the full form of a capability is too expensive, engineer the best technically and financially sustainable version rather than silently deleting the capability.
12. Security, privacy, authorization, dependencies, regression testing, Android constraints, and real-world taxi testing are first-class engineering concerns.
13. Existing older LANA applications are not to be damaged or modified as part of LANA 3 work.

## Build discipline
Architecture first. Implementation follows the approved dependency/build order. A feature is not DONE merely because code exists: its relevant tests and acceptance criteria must pass.

Fast development is allowed only inside this discipline:
- one controlled logical slice at a time;
- separate branch and pull request;
- shared-core tests where relevant;
- Android lint for Android changes;
- Android preview build for Android changes;
- merge only after the required CI checks pass;
- physical-device-dependent behavior remains IN TEST until measured on a real device.

## Current repository milestone
- GitHub write access is verified and the active repository is initialized.
- MASTER PLAN v0.2 and TECHNICAL ARCHITECTURE v0.1 are recorded.
- The Kotlin Multiplatform shared core and Android developer-preview skeleton compile in CI.
- Shared-core regression tests run automatically on pull requests and main.
- Android lint is a CI quality gate before the developer-preview APK build.
- The Android preview has a secure update path in code; private signing is configured and the signed-release pipeline has been verified end to end in GitHub Actions through publication of release `dev-195`.
- The updater verifies package identity, version progression and signing identity before installation is offered.
- Android device readiness measures phone/tablet form factor, RAM, camera, microphone and location state without Samsung model hard-coding.
- Android runtime permission setup is implemented for camera, microphone and location; physical-device verification remains pending.
- Android device readiness is bridged into the shared platform-capability model and deterministic capability gate.
- Unknown or denied permission state is not treated as usable.
- OBSERVE → SUGGEST → CONFIRM → EXECUTE automation-mode safety is implemented and covered by tests.
- Task states include QUEUED, ACTIVE, PAUSED, COMPLETED, CANCELLED, FAILED and DEGRADED.
- Task lifecycle transitions are deterministic; terminal tasks cannot silently restart.
- Active-task status in shared core is semantic and language-neutral rather than carrying English UI text.
- Smart Ride Acceptance includes deterministic €/km and €/h calculation over pickup + passenger trip plus an optional explicitly entered empty return, validation of impossible/invalid inputs, structured language-neutral decision explanations (reason, applied thresholds, failed metrics), and an Android developer test screen.
- Smart Ride does not invent thresholds and does not automate Uber/Bolt actions.
- The Android Smart Ride surface has a structured prefill contract so voice, future screen-awareness or integration adapters can hand off normalized offer fields without coupling those sources to Smart Ride business logic.
- A structured prefill is automatically assessed when all required offer fields and saved Charlie thresholds are available; partial prefills remain review-only and are never completed by guessing.
- Shared speech input/output ports are implemented with an Android Voice Lab adapter for STT/TTS testing.
- Voice Lab can now normalize explicitly labelled Croatian/English ride-offer transcripts through a replaceable shared-core boundary and hand recognized fields to Smart Ride for review or immediate deterministic assessment when enough data exists; unsupported languages and unrecognized data fail explicitly rather than being guessed.
- Voice-to-note handoff prefills Notes Lab but does not silently persist recognized speech.
- Offline local notes use a shared repository/service boundary with an Android app-private SQLite implementation.
- New note creation has a complete low-risk brain → authorization → automation gate → task lifecycle → storage → diagnostics vertical slice.
- Android backup/data-transfer exclusions are explicit; future multi-device sync must be a deliberate LANA feature.
- The current Android developer surfaces have an incremental localization boundary with Croatian default and English resources.
- Structured diagnostics redact sensitive attribute names and now support optional trace ID, outcome, duration and event time metadata.
- The first real signed release was built, signature-verified and published by `LANA 3 CI #195`; first physical installation, physical acceptance and later in-place signed update continuity remain pending.
- Shared core now contains the first local-first Knowledge Core policy/contracts: external AI output begins as unverified candidate knowledge, and only verified/fresh/sufficiently-confident knowledge is eligible for local reuse.
- Future Samsung tablet testing is a second physical target, not an architectural dependency.

## Current manual gates
These items must not be marked complete until Charlie performs or confirms the real-world step:
- install the first signed build on the S24 Ultra;
- test runtime permissions, STT/TTS, local notes, updater behavior and Smart Ride on a physical Android device;
- later repeat the relevant device matrix on the intended tablet.

## Current rule for progress
Work that can be verified safely in CI may continue without waiting for release signing. Anything that depends on real device behavior, Android user consent, private signing secrets, external service behavior, or Charlie's product acceptance remains explicitly pending rather than being marked DONE.
