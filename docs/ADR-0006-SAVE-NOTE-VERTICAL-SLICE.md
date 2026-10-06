# ADR-0006 — First end-to-end local action: save note

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `stabilize/save-note-task-lifecycle`

## Decision

Use local note creation as the first end-to-end LANA 3 action that crosses the complete brain/hands boundary.

The flow is:

`normalized SaveNote intent → Decision Engine → action authorization → automation-mode gate → task lifecycle → NoteService → NoteRepository → diagnostics`

The Android Notes Lab calls this same flow for new notes.

## Why

Earlier slices proved individual core components. This slice proves that they can be composed without bypassing authorization, automation-mode policy, or task lifecycle rules.

A local note is intentionally low risk:

- no external account;
- no cloud API;
- no Uber/Bolt automation;
- no navigation side effect;
- no phone/message side effect.

That makes it suitable for proving orchestration before high-impact integrations are connected.

## Brain / hands boundary

The Decision Engine receives the normalized intent and context.

Only an `AuthorizedAction` may reach `SaveNoteExecutor`.

The executor does not receive a raw UI command and does not decide authorization itself.

## Automation modes

The flow obeys the global mode gate:

- OBSERVE: no write;
- SUGGEST: no write;
- CONFIRM: no write until explicit confirmation;
- EXECUTE: permitted write after the earlier gates succeed.

## Task state

A real execution now creates a QUEUED `save-note` task and must pass through `TaskTransitionPolicy` before it becomes ACTIVE.

Successful persistence requests the legal ACTIVE → COMPLETED transition.

Storage failure requests ACTIVE → FAILED rather than pretending completion or translating the failure into cancellation.

If persistence succeeds but the task lifecycle cannot be finalized consistently, the flow reports DEGRADED and preserves the fact that the note was actually written.

Requests blocked before execution do not create a fake ACTIVE task.

## Diagnostics and privacy

The orchestration emits structured diagnostic events.

Note text is deliberately excluded from diagnostics.

Android logging uses `DiagnosticSanitizer` before writing diagnostic metadata to Logcat.

## Acceptance criteria

- [x] blank note is rejected before execution;
- [x] OBSERVE mode never writes;
- [x] CONFIRM mode waits for explicit confirmation;
- [x] EXECUTE mode completes the full flow in shared-core tests;
- [x] storage failure becomes FAILED task state rather than an uncaught crash;
- [x] diagnostic events contain no note text;
- [x] task state changes use `TaskTransitionPolicy`;
- [ ] shared-core tests pass in CI for the stabilization change;
- [ ] Android Notes Lab compiles using the same orchestration after stabilization;
- [ ] physical Android test proves note creation through the flow;
- [ ] Charlie acceptance remains a separate gate.

## Lifecycle status

Specification → Architecture → Implementation → **IN TEST** → Physical-device test → Charlie acceptance
