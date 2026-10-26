# Task interruption/resume — executable acceptance scenarios (design v0.1)

Scope: proposed scenarios for the existing shared-core task engine; not executed tests. Companion to TASK_INTERRUPTION_RESUME_CONTRACT_v0.1.md.

## Required invariants
- A resume request must never itself be treated as authorization for a financial, external, or irreversible action.
- The same task and operation idempotency key cannot produce the same side effect twice.
- Persist checkpoint before announcing a successful pause.
- Recovering a checkpoint must not automatically execute its next action.
- The system must never claim PAUSED if an external operation could not be stopped; represent an explicit pending/unknown external outcome.
- No checkpoint may contain passwords, bank-card data, raw private transcripts or API tokens.

## Given / When / Then matrix

| ID | Given | When | Then |
|---|---|---|---|
| R01 | ACTIVE task T, step S2 | user interrupts | checkpoint for T/S2 saved; state PAUSED; nextAction remains S3 |
| R02 | PAUSED T with valid checkpoint | user says 'nastavljamo' | restore same T and context; reevaluate permissions before transition |
| R03 | PAUSED T | process is killed and restarted | checkpoint can be read; no automatic execution |
| R04 | PAUSED T | two identical resume requests arrive | one transition and at most one authorized action |
| R05 | COMPLETED or CANCELLED T | user says 'nastavljamo' | no implicit resurrection; explain state |
| R06 | PAUSED T, permission revoked | resume requested | EXECUTE blocked; explain permission need |
| R07 | PAUSED T, stale time/location/business inputs | resume requested | re-evaluate before any decision or side effect |
| R08 | global STOP active | resume requested | no execution until STOP condition cleared |
| R09 | two PAUSED tasks with equal recency | 'nastavljamo' | ask which one; do not guess |
| R10 | checkpoint corrupted/unknown schema | resume requested | safe error/degraded response, no fabricated completion |
| R11 | external operation in progress | interrupt requested | report actual cancellation/unknown outcome, never promise stop without adapter confirmation |
| R12 | active task, driver begins trip | driving safety priority arrives | pause conversational work safely, no distracting screen interaction |
| R13 | task contains sensitive note references | checkpoint persisted | only opaque references saved; no raw sensitive content |
| R14 | user asks 'što smo zadnje radili?' | paused checkpoint exists | return task and next step, with zero side effects |

## Implementation gate
Before writing code:
1. Locate existing task model/state transitions, persistence ports, and tests in actual current main tree.
2. Identify whether PAUSED transitions and restart persistence already exist; do not duplicate.
3. Implement the smallest missing behavior behind existing ports.
4. Add tests for R01–R04 first, then safety cases; run shared-core CI.
5. Keep Android packaging, launcher, startup, signing and known-good physical baseline untouched.
6. Require real-device validation before claiming background/interruption reliability.

This document is a test specification, not a claim of implementation or test success.
