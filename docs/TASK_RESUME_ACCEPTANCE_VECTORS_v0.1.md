# LANA 3 — Task interruption/resume: acceptance test vectors v0.1

Status: TEST SPECIFICATION ONLY. These vectors are not executed tests. Linked to TASK_INTERRUPTION_RESUME_CONTRACT_v0.1.md and MASTER PLAN units 1, 24, 33, 34, 35.

## Test harness contract
Given an in-memory task coordinator with a fake checkpoint repository, fake clock, fake capability/authorization policy and fake execution adapter, record (a) persisted checkpoint, (b) task transitions, (c) external execution count and (d) user-facing semantic status. Do not rely on exact Croatian utterance matching for domain logic: normalized intents are RESUME_LATEST, PAUSE_CURRENT, QUERY_CURRENT and SAFE_STOP.

Checkpoint fields: taskId, taskType, currentStepId, lastConfirmedResultRef, nextActionId, status, updatedAt, schemaVersion.

### V01 — Repeat 'Nastavi' is idempotent
GIVEN task A PAUSED at step 2 with nextActionId action-3, capability and permission valid
WHEN RESUME_LATEST occurs twice without an intervening task state change
THEN exactly one resume transition is committed; task A is ACTIVE; taskId and step 2 remain; execution count of action-3 is at most one (zero until separately authorized if needed).

### V02 — Interrupted by taxi request
GIVEN task A ACTIVE and resumable, checkpoint storage available
WHEN PAUSE_CURRENT occurs due to higher-priority driving event
THEN checkpoint is committed before switching conversational focus; A becomes PAUSED; no further non-authorized action executes.

### V03 — Process restart
GIVEN task A PAUSED with a valid persisted checkpoint
WHEN application process restarts and QUERY_CURRENT is received
THEN the persisted step and next action are returned; no external action executes; explicit RESUME_LATEST is still required.

### V04 — Terminal task
GIVEN task A COMPLETED (repeat for CANCELLED and FAILED)
WHEN RESUME_LATEST is received
THEN A is not reactivated and no external action executes; report no resumable task or select a distinct eligible PAUSED task.

### V05 — Permission revoked while paused
GIVEN task A PAUSED, previously authorized to execute action-3
WHEN permission is revoked and RESUME_LATEST is received
THEN the current authorization gate blocks action-3; execution count remains zero; report explicit permission requirement.

### V06 — Context became stale
GIVEN task A PAUSED with old contextual references
WHEN location, driving state, business data or authorization context has changed
THEN re-evaluate prerequisites and do not replay old action proposals as if still valid.

### V07 — Safe Stop
GIVEN task A PAUSED or ACTIVE
WHEN SAFE_STOP is set and RESUME_LATEST is received
THEN no new execution starts until the safety stop condition is explicitly cleared.

### V08 — Corrupt checkpoint
GIVEN persisted checkpoint missing taskId, containing an unknown schemaVersion or inconsistent terminal status
WHEN coordinator loads it
THEN reject or quarantine it safely; do not fabricate success or execute an action; produce a diagnostic without sensitive content.

### V09 — Ambiguous paused tasks
GIVEN A and B both PAUSED and no uniquely determined latest task in the relevant context
WHEN RESUME_LATEST is received
THEN ask a short clarification; execute neither task.

### V10 — Interrupted external action
GIVEN task A has sent an external action that may not be cancellable
WHEN PAUSE_CURRENT occurs
THEN do not report that the external action was stopped unless the adapter confirms it; retain an outcome-unknown state and reconcile before retrying; do not issue a duplicate request.

## Nonfunctional acceptance
- Checkpoint writes and state transitions must be atomic or have a documented crash-safe recovery protocol.
- At-most-once effects require idempotency keys and/or external reconciliation, not just a UI flag.
- Diagnostic records must not include full conversation transcripts or financial credentials.
- Physical Android background/restart/voice tests remain separate gates.
- No existing known-good startup/packaging/signing behavior may be altered for these vectors.

## Implementation gate
Locate actual task coordinator, repository interfaces and existing tests on main before adding executable tests. Build/CI and physical acceptance must be reported independently. No automatic merge of this proposal.
