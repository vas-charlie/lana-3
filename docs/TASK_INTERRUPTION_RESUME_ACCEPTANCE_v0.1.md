# LANA 3 — Acceptance scenarios for interruption/resume v0.1

**Status:** executable-test specification, NOT executed tests. Companion to TASK_INTERRUPTION_RESUME_CONTRACT_v0.1.md in draft PR #127.

## Invariants
- Never infer that an external side effect completed.
- Never resume a terminal task implicitly.
- Never bypass STOP, permission or authorization gates.
- Persist the minimum context needed to resume, not a raw sensitive conversation.
- Keep UI/voice language independent from task state.

## Test matrix

| ID | Given | When | Then |
|---|---|---|---|
| IR-01 | ACTIVE task T with step S and next action A | user interrupts | checkpoint for T/S/A is durable before state PAUSED is acknowledged |
| IR-02 | PAUSED T with valid checkpoint | user resumes | same T and S restored, no duplicate execution of completed steps |
| IR-03 | PAUSED T | app process restarts | checkpoint is recovered; task does not auto-execute until authorization/context rechecked |
| IR-04 | PAUSED T | user says resume twice | only one transition and at most one authorized execution for the same idempotency key |
| IR-05 | COMPLETED or CANCELLED task | user says resume | no transition to ACTIVE; user receives truthful terminal status |
| IR-06 | PAUSED task with previously granted permission | permission revoked before resume | execution blocked, task remains safe, reason recorded |
| IR-07 | PAUSED task with outdated location/ride context | user resumes | context re-evaluated; stale decision not executed |
| IR-08 | Safe Stop active | user requests resume | resume blocked until Stop condition explicitly cleared |
| IR-09 | checkpoint absent, malformed or schema-incompatible | user requests resume | no guessed state; safe error/degraded response |
| IR-10 | multiple PAUSED tasks | user says 'nastavi' without reference | deterministic eligible selection only if unambiguous; otherwise ask which task |
| IR-11 | external action started, outcome unknown | interruption occurs | do not mark it completed or blindly retry; reconcile adapter outcome first |
| IR-12 | app offline | user pauses/resumes local-only task | local checkpoint works without cloud; online-required action remains blocked |
| IR-13 | PAUSED task | user asks 'što smo radili?' | read-only status, no side effects |
| IR-14 | language changes between pause and resume | user resumes | same language-neutral task identity and step, localized response |

## Implementation gate
1. Locate actual shared-core task coordinator, transition rules and storage interfaces in the current main tree.
2. Verify persistence and concurrency semantics before selecting an adapter.
3. Implement one narrow checkpoint/resume slice with tests in a separate PR.
4. Run shared-core regression tests; Android lint/build only if Android code changes.
5. Test real process death and device lifecycle on physical Android before claiming reliability.

## Explicit non-goals
No Android launcher/startup changes, no new parallel task engine, no automatic Uber/Bolt action, no assumption that ChatGPT conversation state equals LANA 3 application state.
