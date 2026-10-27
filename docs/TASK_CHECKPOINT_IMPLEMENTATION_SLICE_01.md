# LANA 3 — Implementation slice 01: task checkpoint persistence

Status: ENGINEERING PLAN, not implemented.

## Verified source boundary (26 October 2026)
- `androidApp/src/main/java/hr/vascharlie/lana3/TasksActivity.kt` is a manual SQLite task list with `Otvoreno / Čeka se / Dovršeno`.
- `docs/ENGINEERING_BASELINE.md` records a distinct shared-core task lifecycle with `ACTIVE / PAUSED` states.
- The two task types must not share state columns or be conflated.

## First executable slice, scoped
Introduce a small versioned `ExecutionCheckpoint` contract in the shared core, with a storage port and an in-memory fake. Keep Android persistence for the *next* slice, after shared-core tests pass.

Fields: `schemaVersion`, `taskId`, `stepId`, `nextActionId`, `status`, `updatedAt`, `revision`. Keep business data out of the checkpoint; reference it separately.

## Safety invariants
1. A resume requires a checkpoint whose taskId matches the selected paused task.
2. A stale revision must not overwrite a newer checkpoint.
3. Loading a checkpoint never executes an action by itself.
4. A resume proposal is subject to current authorization and context checks.
5. A completed or cancelled task is not resumed implicitly.
6. The checkpoint contract never touches `lana_manual_tasks.db`.

## Tests before Android integration
- round-trip save/load of PAUSED checkpoint;
- overwrite conflict with older revision rejected;
- missing or unsupported schema version returns typed failure;
- loading does not invoke an execution adapter;
- resume of completed/cancelled task rejected;
- two resume requests cannot dispatch the same action twice (idempotency token at action boundary).

## Release boundary
This document is preparation only. Implementation needs review of actual shared-core package names, task interfaces, and current PR/CI status before modifying code. Existing physically verified Android baseline must be preserved.
