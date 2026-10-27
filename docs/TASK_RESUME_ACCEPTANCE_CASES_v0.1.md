# LANA 3 — Task resume acceptance cases v0.1

Status: test specification only; **not executed**. Scope: shared-core executable tasks, not Android manual `lana_manual_tasks.db`.

## Fixture conventions
- `t1`: ACTIVE, step=`analyze`, next=`save-result`, checkpointVersion=1
- `t2`: COMPLETED, step=`done`
- `t3`: PAUSED, step=`confirm-payment`, authorization required
- `executeCount`: fake-adapter side-effect counter, initially zero
- Each test starts with fresh isolated fake repository and fake action adapter.

| ID | Given | When | Expected |
|---|---|---|---|
| R01 | t1 ACTIVE | interrupt | PAUSED checkpoint persisted with same id/step/next action; no execution |
| R02 | t1 PAUSED persisted | application session recreated | PAUSED is readable, no auto execution |
| R03 | t1 PAUSED and permission valid | resume | context and authorization rechecked before ACTIVE |
| R04 | t1 PAUSED | resume twice | `executeCount` at most one for same idempotency key |
| R05 | t2 COMPLETED | resume | rejected, zero execution |
| R06 | t3 PAUSED and permission revoked | resume | blocked pending authorization, zero execution |
| R07 | t1 PAUSED with stale driving context | resume | re-evaluate and hold; zero execution until safe |
| R08 | global Safe Stop active | resume | blocked, zero execution |
| R09 | corrupted/unknown checkpoint schema | restore | typed degraded/error result, zero execution |
| R10 | multiple PAUSED tasks | generic 'nastavljamo' | ask to disambiguate; no implicit action |
| R11 | pending external action with unknown outcome | interrupt and resume | reconcile action result; do not replay blindly |
| R12 | Android manual tasks prepopulated | shared-core interrupt/resume | unchanged `tasks` rows and dashboard counts |

## Implementation acceptance gates
1. Identify existing shared-core task lifecycle implementation and its current transition contract.
2. Choose explicit checkpoint storage port and versioned serialization; never reuse Android manual task table.
3. Prove R01–R11 with automated unit/integration tests and fake adapters.
4. Prove R12 with an Android-level compatibility test when Android integration is added.
5. Run existing shared-core regression suite and Android checks for touched modules.
6. Physical acceptance only after a real-device interruption/restart/resume test.

No release, merge, or 'DONE' status is implied by this file.
