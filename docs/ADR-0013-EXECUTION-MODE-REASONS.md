# ADR-0013 — Language-neutral execution-mode block reasons

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `i18n/execution-mode-reasons`

## Decision

`ExecutionModeGate` returns semantic `ExecutionModeBlockReason` values instead of English sentences.

Current reasons:

- `OBSERVE_MODE`
- `SUGGEST_MODE`
- `USER_CONFIRMATION_REQUIRED`
- `ACTION_CONFIRMATION_REQUIRED`

The Save Note orchestration preserves those reason values to the presentation boundary, where Android renders localized Croatian/English copy.

Authorization rejection remains a separate flow result so it cannot be confused with an automation-mode decision.

## Why

Automation policy is core business logic. It must not depend on a display language, and tests should verify policy identity rather than exact English wording.

This also prevents an execution-mode message from becoming an accidental protocol between core and UI.

## Acceptance

- [x] shared gate tests assert semantic reasons;
- [x] Save Note orchestration tests assert semantic execution-block reasons;
- [ ] shared-core CI passes;
- [ ] Android Notes Lab compiles and lint passes;
- [ ] physical locale behavior remains pending.
