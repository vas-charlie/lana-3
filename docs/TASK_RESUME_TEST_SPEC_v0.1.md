# LANA 3 — Task resume: test specification v0.1

Status: proposed test specification, NOT executed. Linked to TASK_INTERRUPTION_RESUME_CONTRACT_v0.1.md.
Scope: shared-core executable task lifecycle, NOT androidApp manual business task list.

## Invariants
- At most one active execution lease for one taskId.
- Checkpoint writes are versioned, atomic or transactionally recoverable.
- Resume is an explicit intent; restoring an app does not execute effects.
- Every resume rechecks Safe Stop, capabilities, permissions, context freshness, authorization and task terminality.
- External non-idempotent effects require an execution token/idempotency key or a reconciliation path; never claim exactly-once semantics if the adapter cannot guarantee it.
- Store references to prior confirmed results, not full private conversation transcripts.

## Arrange / Act / Assert acceptance cases

| ID | Arrange | Act | Assert |
|---|---|---|---|
| R01 | ACTIVE task, checkpoint store available | Interrupt | PAUSED persisted with same taskId, step, nextAction; no next action executed |
| R02 | PAUSED checkpoint in storage | Process restart and reload | PAUSED restored; zero execution side effects |
| R03 | PAUSED task with valid context | Resume | ACTIVE exactly once; continues from nextAction, not from first step |
| R04 | PAUSED task | Send resume twice concurrently | One execution lease; one side-effect request |
| R05 | COMPLETED/CANCELLED/FAILED task | Resume | Refused without side effects; no terminal-state mutation |
| R06 | PAUSED task, microphone/location permission revoked | Resume action requiring permission | No execute; permission failure surfaced |
| R07 | PAUSED task, route or user context changed | Resume | Re-evaluation/confirmation before action |
| R08 | PAUSED task, Safe Stop active | Resume | Blocked until Safe Stop cleared under authorized policy |
| R09 | Corrupt or unknown schema checkpoint | Restore | Safe failure/degraded state; no action, diagnostics without secrets |
| R10 | Two eligible PAUSED tasks, ambiguous 'nastavi' | Resume | No arbitrary choice; ask concise clarification |
| R11 | PAUSED task, external action was submitted before crash, outcome unknown | Resume | Reconcile via adapter or ask; never blindly resubmit |
| R12 | PAUSED task while manual Android tasks DB exists | Pause/resume | Existing tasks table and dashboard unaffected |

## Required evidence before merge
- Locate actual shared-core task coordinator and persistence contracts on current main.
- Unit tests: R01–R10 using fake clock/store/permissions/action adapter.
- Integration test: R11 with controlled fake external adapter.
- Regression/Android contract check: R12 where practical; otherwise explicitly record pending device evidence.
- CI shared-core test success, no changes to Android packaging or known-good startup path.
- Review PR before merging; physical acceptance remains pending where device behavior is relevant.

## Implementation sequence
1. Map current task lifecycle and existing persistence boundaries; record exact paths.
2. Add versioned checkpoint contract and fake in-memory repository.
3. Add pause/resume orchestration with safety checks and idempotency protection.
4. Add R01–R11 tests and run CI.
5. Add Android integration only in a later controlled PR; preserve existing manual task SQLite schema.

No claim of code completion or passing tests is made by this document.
