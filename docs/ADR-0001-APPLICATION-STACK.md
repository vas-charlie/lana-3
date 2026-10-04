# ADR-0001 — Application stack and platform strategy

- **Status:** ACCEPTED FOR INITIAL IMPLEMENTATION
- **Date:** 2026-10-04
- **Scope:** LANA 3 client architecture
- **Source:** MASTER_PLAN_v0.2 + TECHNICAL_ARCHITECTURE_v0.1

## Decision

Use **Kotlin Multiplatform (KMP)** as the shared application/core technology and **Compose Multiplatform** for shareable UI where it is technically appropriate.

Platform-specific capabilities remain native adapters:
- Android: Kotlin / Android APIs
- iOS/iPadOS: Kotlin interop plus Swift/Apple APIs where required
- Desktop: Kotlin/JVM plus platform adapters
- Web: not a dependency for the first product milestone; architecture remains open to a later web client

This is not a promise that every operating system exposes identical capabilities. LANA exposes one product model while adapters truthfully report each platform's real abilities and restrictions.

## Why this fits LANA 3

LANA is not primarily a static cross-platform UI. Its difficult requirements are deep device integration:
- continuous/interactive voice
- background/lifecycle behavior
- Bluetooth
- location and navigation
- camera
- screen context/capture with explicit permission
- phone/messaging integrations
- notifications
- local/offline operation
- device permissions
- future vehicle/integration adapters

KMP lets the project share the domain, Decision Engine, authorization, task model, context, data models and substantial UI while retaining direct native API access where LANA needs it.

## Alternatives reviewed

### Flutter
Strengths:
- broad Android/iOS/Windows/macOS/Linux/web deployment support;
- mature shared UI model;
- native APIs available through plugins/platform channels/FFI.

Reason not selected as the primary stack:
LANA's architecture expects many platform-specific, permission-heavy integrations. Flutter can implement them, but this would put a large native integration surface behind Dart-to-host plugin/channel boundaries. That is workable, but gives LANA less direct alignment with Android/Kotlin platform APIs than KMP while still requiring native Swift/Kotlin/C++ work for difficult capabilities.

### React Native
Strengths:
- mature mobile ecosystem;
- native modules and cross-platform modules available.

Reason not selected:
LANA needs a large deterministic core plus deep native/device integration and eventual desktop possibilities. React Native remains viable, but introduces a JavaScript/TypeScript runtime and native-module boundary without giving a decisive advantage for LANA's specific device-heavy architecture.

### Separate fully native applications
Strengths:
- maximum platform fidelity and API access.

Reason not selected:
It would duplicate too much Decision Engine, business logic, schemas, authorization and tests across platforms. Native adapters remain available under KMP without duplicating the core.

## Platform strategy

### Tier 1: Android
First implementation and real-world taxi test platform.

Reason:
Charlie's currently available devices are Android, so we can validate the hardest real-world requirements immediately. Android-first testing does **not** make the architecture Android-only.

### Tier 2: iOS/iPadOS
Architecturally supported from the start through shared KMP core and platform adapters. Build/signing/device validation requires Apple development tooling when this target is actively tested.

### Tier 3: Desktop
Windows/macOS/Linux are legitimate client targets for appropriate LANA functions. Device-specific features may differ.

### Web
Keep core contracts portable, but do not force the first implementation to satisfy browser restrictions. Web can later become a companion/full client according to proven need.

## Shared-code boundary

Must live in shared/core unless technically impossible:
- intent schemas
- context schemas
- Decision Engine
- authorization rules
- task/state machine
- priorities
- Smart Ride Acceptance deterministic logic
- taxi/business calculations
- explanation/audit models
- locale-neutral normalized data models
- repository/use-case interfaces
- diagnostics contracts
- AI routing policy
- tests for all above

Must remain behind platform interfaces:
- microphone/audio session
- TTS/STT implementation
- Bluetooth
- camera capture
- screen capture/context
- location provider
- navigation launch/control
- phone calls
- messaging
- notifications
- background execution
- secure OS credential storage
- lifecycle hooks
- platform-specific permission requests

## UI strategy

Use Compose Multiplatform for shared application surfaces where stable and appropriate.

Do not force shared UI when a native screen/control is safer or materially better. Platform-native components may be embedded behind interfaces.

UI layouts must be adaptive and locale-aware. No screen dimensions, Samsung assumptions, language lengths or LTR-only behavior may be embedded in business logic.

## Screen-awareness rule

Screen awareness is explicitly platform-dependent.

The shared core knows only a `ScreenContextPort` and structured evidence. Each platform adapter must implement the lawful, user-authorized mechanism actually available on that OS.

No adapter may report screen access as available until the platform permission/session is genuinely active.

## Background-work rule

Background capability is not a boolean promise. It is represented as a capability with platform/version/state limitations.

Long-running voice, capture or location behavior must comply with the platform's current foreground/background rules. No workaround that violates platform policy is part of the architecture.

## Development environment consequence

The project can begin on the current Windows development machine with Android and desktop targets.

iOS compilation/signing/testing is not claimed complete from Windows. When iOS reaches active implementation/testing, Apple-required tooling/hardware must be introduced. This is an infrastructure requirement, not a reason to contaminate the shared architecture with Android assumptions.

## Initial module plan

```
lana-3/
  docs/
  shared/
    core/
      model/
      intent/
      context/
      decision/
      authorization/
      tasks/
      diagnostics/
    application/
      usecases/
      orchestration/
    ports/
    data/
  apps/
    android/
    desktop/
    ios/          # activated/tested with required Apple tooling
  adapters/
    android/
    desktop/
    ios/
  tests/
```

Exact Gradle/source-set layout will follow KMP conventions when the skeleton is generated.

## First vertical slice

Implement before Maps/Uber/Bolt automation:

1. user enters a natural-language request through a simple test surface;
2. request becomes a normalized intent;
3. context snapshot is created;
4. Decision Engine evaluates it;
5. authorization is checked;
6. deterministic local action executes;
7. task state and explanation are shown;
8. diagnostics record the flow;
9. unit/integration tests prove the path.

Initial deterministic action: **local taxi profitability calculation or local note**, selected so the architecture can be proven without an external service.

## Acceptance gates for ADR-0001

Before calling the stack decision validated:
- KMP project builds on the development machine;
- shared-core unit test runs;
- Android host consumes shared core;
- desktop host consumes the same shared core;
- one platform adapter is called through a shared port;
- no Android/Samsung class leaks into shared domain;
- multilingual/Unicode sample passes through shared models;
- CI can run shared-core tests.

## Revisit triggers

Reopen this ADR if evidence shows:
- a critical LANA capability cannot be implemented cleanly through KMP/native adapters;
- maintenance cost of Compose Multiplatform becomes materially worse than native UI;
- a required platform becomes unsupported;
- measured performance, battery, latency or reliability fails acceptance criteria;
- platform policy prevents a core product requirement.

Until one of those conditions is demonstrated, KMP + native adapters is the baseline stack.
