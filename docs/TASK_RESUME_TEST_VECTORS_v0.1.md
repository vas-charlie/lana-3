# LANA 3 — test vectors for safe task resume v0.1

Status: SPECIFICATION ONLY. No test runner execution claimed.

## Shared fixture
Task T-101 is ACTIVE at step S-2, next action A-3; checkpoint schemaVersion=1. A-1 and A-2 have confirmed outcomes. Executor records idempotency key (taskId, actionId). Each fixture uses a fresh isolated in-memory repository, fake clock, fake permissions and fake action adapter. Persistent restart scenarios use a durable test store.

| ID | Given | When | Then |
|---|---|---|---|
| R01 | T-101 ACTIVE | interrupt | T-101 PAUSED; checkpoint stores S-2/A-3; A-1/A-2 not replayed |
| R02 | T-101 PAUSED; valid checkpoint | resume | T-101 becomes eligible for ACTIVE only after authorization/context validation |
| R03 | T-101 PAUSED | process restart then resume | durable checkpoint is recovered; no automatic action on boot |
| R04 | T-101 PAUSED | resume twice concurrently | A-3 executed at most once (atomic claim/idempotency) |
| R05 | T-101 COMPLETED | resume | rejected without side effect |
| R06 | T-101 CANCELLED | resume | rejected without side effect |
| R07 | T-101 PAUSED; permission revoked | resume | no EXECUTE; user-visible authorization-needed result |
| R08 | T-101 PAUSED; context expired | resume | re-evaluate, do not execute stale action |
| R09 | T-101 PAUSED; Safe Stop engaged | resume | no EXECUTE until explicitly cleared |
| R10 | corrupt/unsupported checkpoint version | resume | safe error/degraded state; never invent completion |
| R11 | two PAUSED tasks equally relevant | generic 'nastavljamo' | request minimal disambiguation, no execution |
| R12 | adapter operation in-flight, cancellation unconfirmed | interrupt | report pending/uncertain; never claim remote action stopped |
| R13 | completed A-3 result saved; crash before status update | restart | reconcile by idempotency key; no duplicate side effect |
| R14 | driving mode active | request long interactive step | defer unsafe UI work; no distracting prompt |

## Required invariants
- Task state and checkpoint are committed atomically, or recovery has a proven reconciliation path.
- Resume is an intent to revalidate and continue, not unconditional execution.
- A completed, cancelled or failed task is never silently restarted.
- Side-effectful actions need durable idempotency/reconciliation; if adapter cannot guarantee it, resume must ask or enter degraded mode.
- Unknown state never maps to success.
- User-facing Croatian strings are localized presentation, not core-state identifiers.

## Implementation gate
Before writing tests or production code, locate existing task lifecycle, storage port, and action coordinator in current main; reuse them. Run shared-core tests in CI and keep physical Android behavior unaccepted until measured. No new generic task engine or changes to known-good Android startup.
