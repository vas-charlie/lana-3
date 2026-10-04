# LANA 3 — TECHNICAL ARCHITECTURE v0.1

## 0. Status and authority
Derived from `docs/MASTER_PLAN_v0.2.md`. This document defines technical boundaries before product code is written.

Architecture rules:
- core logic must not depend on Samsung, a single device model, or a single operating system;
- multilingual operation is a foundation, not a later feature;
- understanding/decision-making ("brain") is separated from execution ("hands");
- unknown capability is not assumed;
- permissions are checked at the action boundary;
- expensive cloud/AI work is used only when justified;
- every externally dependent capability must have an explicit adapter and degraded behavior.

## 1. Architectural shape

LANA 3 is split into four zones:

### A. Core domain
Portable business logic with no direct dependency on Android/iOS/desktop APIs:
- intent model
- context model
- Decision Engine
- rules and priorities
- authorization policy
- task state machine
- Smart Ride Acceptance rules
- taxi/business calculations
- explanation/audit model
- Safe Stop semantics

### B. Application orchestration
Coordinates use cases without knowing vendor-specific implementation:
- conversation session orchestration
- task coordinator
- command routing
- capability registry
- permission checks
- memory/knowledge orchestration
- diagnostics events
- offline/degraded-mode routing

### C. Ports and adapters
Interfaces ("ports") live toward the core; implementations ("adapters") live at platform/integration edges.

Initial ports:
- SpeechInputPort
- SpeechOutputPort
- LocationPort
- NavigationPort
- CameraPort
- ScreenContextPort
- PhonePort
- MessagingPort
- MediaPort
- NotificationPort
- BackgroundExecutionPort
- ConnectivityPort
- SecureStoragePort
- LocalDatabasePort
- CloudAiPort
- KnowledgePort
- VehicleDataPort
- BusinessDataPort
- ClockPort

No core module may call Google Maps, Samsung APIs, Android services, Uber/Bolt, YouTube or a model provider directly.

### D. Presentation
Adaptive UI and voice surfaces:
- conversation
- task/status display
- permission prompts
- driving-mode compact presentation
- parked-mode detailed presentation
- diagnostics/test-mode presentation

Presentation consumes application state. It does not contain business rules.

## 2. Platform independence

A `PlatformCapabilities` snapshot describes what the current device/runtime can actually do.

Each capability reports:
- availability: available / unavailable / degraded / unknown
- permission state
- execution mode: local / platform / external service
- relevant limitation
- last verified time/version where appropriate

The Decision Engine uses capabilities as input. It never assumes a feature exists because another device supported it.

Samsung S24 Ultra and the current tablet are first physical test targets only.

A future platform is added by implementing adapters, not by rewriting the core.

## 3. Multilingual foundation

Internally, commands and domain events use language-neutral identifiers. Human language belongs at input/output boundaries.

Required rules:
- Unicode end-to-end;
- locale carried as context, not embedded in business rules;
- automatic language detection may propose a language but confidence must be represented;
- user can override detected language;
- intent schema remains stable across languages;
- dates, numbers, currencies and units are parsed with locale context and normalized before business logic;
- UI text uses localization keys;
- RTL/LTR layout is treated as a presentation concern;
- unsupported/weak provider language quality must be reported, not hidden.

"All languages" is an architectural requirement. Actual quality/availability is measured per selected STT/TTS/AI provider and platform.

## 4. Brain / hands contract

### Brain output
The brain produces an `ActionProposal`, never an uncontrolled side effect.

Minimum fields:
- action type
- normalized parameters
- evidence/input references
- confidence/uncertainty
- reason/rule
- requested authorization level
- expected consequence
- fallback/degraded option

### Hands input
Execution receives only a validated `AuthorizedAction`.

Before execution:
1. capability exists;
2. required permission exists;
3. authorization policy permits the action;
4. task has not been cancelled by Safe Stop;
5. required parameters are complete;
6. current context has not invalidated the decision.

Execution returns structured success/failure/degraded result. It never fabricates success.

## 5. Authorization

Four levels from the Master Plan:
- VIEW
- SUGGEST
- ASK_CONFIRMATION
- EXECUTE

Authorization is evaluated per capability/action, not globally.

An EXECUTE grant does not imply access to unrelated actions.

High-impact actions must be able to force confirmation even when a broader automation mode is enabled.

## 6. Task model and Safe Stop

Every non-trivial action is represented by a task.

Task states:
`created → evaluating → awaiting_confirmation → executing → completed`

Alternative terminal/exception states:
`cancelled | failed | degraded`

Every task has:
- id
- type
- priority
- creation time
- current state
- cancellation token
- parent/child relation where needed
- human-readable status
- diagnostic trace id

Safe Stop sets cancellation on active cancellable tasks and prevents queued execution until the stop condition is cleared.

External integrations that cannot be technically interrupted must report that limitation explicitly.

## 7. Context model

Context is a typed snapshot, not a loose prompt dump.

Domains include:
- user/session
- locale/language
- device/platform/capabilities
- connectivity
- driving state
- location/navigation
- active application/screen context when permitted
- active tasks
- business/taxi state
- temporal context
- relevant memory/knowledge references

Sensitive context must be minimized before cloud transmission.

## 8. Decision Engine

Input:
- normalized intent
- context snapshot
- applicable rules
- capability snapshot
- authorization state
- uncertainty/evidence

Output:
- decision
- action proposal(s)
- explanation
- confidence/unknowns
- required confirmation
- fallback

Decision logic must be testable without microphone, UI, Maps or a physical phone.

For deterministic business rules, deterministic code has priority over generative inference.

## 9. Data and memory boundaries

Separate stores/concepts:
- transient conversation/session context
- user preferences
- durable notes
- Lana diary/events
- business/taxi records
- knowledge documents/index
- task state
- audit/diagnostic events
- secrets/credentials

Secrets never enter normal logs, prompts or business records.

Durable records require schema/versioning and migration strategy.

## 10. AI routing and cost control

AI is a replaceable service behind ports.

Routing classes:
1. local deterministic processing
2. local model/on-device processing where viable
3. low-cost cloud model
4. high-capability multimodal/reasoning model only when required

Routing considers:
- task complexity
- privacy
- latency
- connectivity
- modality
- language support
- current verified provider cost
- user/product policy

Provider pricing is not hard-coded as timeless truth. It is configuration/research data with a verification date.

## 11. Camera and screen

Camera and screen are general input capabilities, not tourism modules.

Pipeline:
`permission → capture/read → minimize/prepare → understand → evidence → intent/context/answer`

Seeing screen content grants no action permission.

Raw visual data retention defaults must be explicit and minimal.

## 12. Offline and degraded mode

Every capability declares one of:
- LOCAL_FULL
- LOCAL_PARTIAL
- ONLINE_REQUIRED
- PLATFORM_DEPENDENT

When dependency fails, application returns a typed degraded result and explains what remains possible.

Core notes, settings, cached authorized knowledge, task state and deterministic calculations should remain local where feasible.

## 13. Diagnostics and observability

Structured diagnostic events include:
- component
- operation
- result
- duration
- error category
- trace id
- capability/platform version context

Do not log secrets or unnecessary sensitive content.

User-facing "Što trenutno radiš?" reads real task state, not generated guesswork.

## 14. Testing architecture

Test pyramid:
- unit: domain rules, parsing normalization, priorities, authorization, state machines
- integration: orchestration + fake adapters
- contract: every adapter against its port behavior
- platform/device: real permissions, lifecycle/background behavior, Bluetooth, camera, screen, navigation
- real-world: vehicle scenarios
- regression: critical end-to-end scenarios

Required cross-platform matrix dimensions:
- OS/platform and version
- form factor/screen
- permission state
- online/offline/degraded
- language/script including RTL
- foreground/background/locked lifecycle
- hardware capability availability

No feature is DONE solely because it works on Charlie's current Samsung.

## 15. Repository boundaries

Proposed top-level structure:

```
/docs
  MASTER_PLAN_v0.2.md
  TECHNICAL_ARCHITECTURE_v0.1.md
/core
  domain/
  decision/
  authorization/
  tasks/
  context/
  contracts/
/application
  orchestration/
  usecases/
/adapters
  platform/
  integrations/
  ai/
  storage/
/presentation
/tests
  unit/
  integration/
  contract/
  e2e/
```

This is a logical boundary map. Concrete framework/language selection is a separate Architecture Decision Record and must be made before generating a framework-specific skeleton.

## 16. Architecture gates before product code

Before the first framework-specific application code:
1. approve/record target platform strategy;
2. choose implementation stack using explicit criteria;
3. define core contracts and data schemas;
4. define secrets/configuration strategy;
5. define minimum CI checks;
6. define first vertical slice and its acceptance test;
7. record known external-integration uncertainties.

## 17. First vertical slice candidate

The first slice should prove the architecture without depending on Uber/Bolt automation or complex external permissions:

**natural-language request → normalized intent → context → Decision Engine → authorization → deterministic action → explanation → task status**

Candidate action: local note or deterministic taxi calculation.

This validates brain/hands separation, multilingual input boundary, task state, authorization, diagnostics and tests before expensive integrations are introduced.

## 18. Open decisions that must not be guessed

- concrete cross-platform application framework/language;
- minimum supported OS/platform versions;
- exact STT/TTS/AI providers;
- backend/sync technology;
- identity/authentication design;
- local database technology;
- whether desktop/web are full clients or companion surfaces;
- exact external integration mechanisms and their legal/technical permissions.

These require evidence-based Architecture Decision Records before implementation.
