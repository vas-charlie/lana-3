# LANA 3 — Task interruption/resume acceptance scenarios

This file is a **proposed behavior fixture**, not evidence that production Kotlin code passes the scenarios. It is intentionally language-neutral and should be wired into shared-core tests once the existing task coordinator and storage boundaries are inspected.

| ID | Starting state | Event / condition | Expected state | Resume execution? |
|---|---|---|---|---|
| pause-preserves-checkpoint | ACTIVE | PAUSE | PAUSED | checkpoint preserved |
| resume-paused | PAUSED | RESUME; permissions/context valid | ACTIVE | yes |
| no-resume-completed | COMPLETED | RESUME | COMPLETED | no |
| no-resume-cancelled | CANCELLED | RESUME | CANCELLED | no |
| no-resume-failed | FAILED | RESUME | FAILED | no |
| no-resume-after-stop | PAUSED | RESUME; Safe Stop active | PAUSED | no |
| no-resume-with-revoked-permission | PAUSED | RESUME; permission revoked | PAUSED | no |
| no-resume-with-stale-context | PAUSED | RESUME; context stale | PAUSED | no |
| idempotent-double-resume | ACTIVE | RESUME again | ACTIVE | no second execution |

Additional non-state assertions for implementation:
- Persist and reload task ID, step ID, next action ID, schema version, and last confirmed result reference.
- A checkpoint must be committed before PAUSED is reported; crash/restart must not imply successful action execution.
- External actions need idempotency keys or explicit reconciliation before retry; state transition alone does not prove exactly-once execution.
- Corrupt/unknown schema must fail closed and emit redacted diagnostics.
- Checkpoint must not contain sensitive transcripts or credentials.
- If an external action is non-cancellable, report its true status rather than claiming cancellation.

Fixture syntax is descriptive and is not yet bound to a Kotlin test runner. No Android or release behavior was changed.
