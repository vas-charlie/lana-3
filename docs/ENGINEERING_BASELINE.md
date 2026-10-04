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
- GitHub write access verified.
- Repository initialized.
- Engineering baseline recorded.
- MASTER PLAN v0.2 recorded with all 43 functional units and confirmed amendments.
- Next: derive the technical architecture and implementation build sequence from v0.2 before writing product code.
