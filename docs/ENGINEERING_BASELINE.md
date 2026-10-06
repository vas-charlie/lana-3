# LANA 3 — ENGINEERING BASELINE

## Status
This repository is the active engineering workshop for LANA 3.

## Source of truth
The approved LANA 3 MASTER PLAN contains 43 functional units. Those 43 units remain the functional scope and are not to be silently changed, renumbered, or expanded during implementation.

## Confirmed amendments
1. LANA 3 is multilingual from the initial architecture. The system must be designed for all languages rather than a fixed shortlist or a later language retrofit.
2. LANA 3 is device- and vendor-independent by architecture. Samsung devices are initial physical test devices, not a product boundary. Platform-specific capabilities must live behind adapters/interfaces, with real limitations tested and documented.

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

## Current repository milestone
- GitHub write access is verified and the active repository is initialized.
- MASTER PLAN v0.2 and TECHNICAL ARCHITECTURE v0.1 are recorded.
- The Kotlin Multiplatform shared core and Android developer-preview skeleton compile in CI.
- Shared-core regression tests run automatically on pull requests and main.
- The Android preview has a secure update path in code; signed release publishing remains intentionally inactive until the private signing secrets are configured.
- Android device readiness now measures phone/tablet form factor, RAM, camera, microphone and location state without Samsung model hard-coding.
- Android runtime permission setup is implemented for camera, microphone and location; physical-device verification remains pending.
- Android device readiness is bridged into the shared platform-capability model and deterministic capability gate.
- Unknown or denied permission state is not treated as usable.
- OBSERVE → SUGGEST → CONFIRM → EXECUTE automation-mode safety is implemented and covered by tests.
- Smart Ride Acceptance includes deterministic €/km and €/h calculation, validation of impossible/invalid inputs, and an Android developer test screen.
- Smart Ride does not invent thresholds and does not automate Uber/Bolt actions.
- Physical S24 Ultra testing, private release signing setup and the first signed installation remain pending manual gates.
- Future Samsung tablet testing is a second physical target, not an architectural dependency.

## Current rule for progress
Work that can be verified safely in CI may continue without waiting for release signing. Anything that depends on real device behavior, Android user consent, private signing secrets, or Charlie's product acceptance remains explicitly pending rather than being marked DONE.
