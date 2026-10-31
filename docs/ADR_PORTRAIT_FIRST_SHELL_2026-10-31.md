# ADR — LANA 3: avatar-first, functions behind the portrait

Date: 2026-10-31
Status: product direction confirmed by Charlie; implementation pending
Scope: LANA 3 Android user interface only; no changes to the 43 MASTER PLAN units.

## Decision

The default main screen prioritizes Lana's portrait/avatar. Technical features, tools, status and diagnostics remain behind the portrait and are revealed only when needed. An explicit user request or relevant task may bring a minimal, contextual surface in front of the portrait; after completion it returns to the portrait-first state. Do not expose a permanent developer dashboard as the normal user experience.

## First safe implementation slice

1. Preserve the existing signed, physically verified developer-preview startup and visual-state controls; do not remove developer diagnostics.
2. Add a separate presentation state contract for `PORTRAIT`, `CONTEXT_PANEL` and `DEVELOPER_TOOLS`. This controls display only and must never grant permissions or trigger actions.
3. Default to `PORTRAIT` in normal user mode. Keep developer preview/test controls accessible through an explicit developer entry point, not silently deleted.
4. A context panel must be dismissible and must not persist over the portrait after its task ends.
5. No feature is presented as functional merely because its button or panel exists.

## Acceptance / regression gates

- Portrait is the default normal-mode surface.
- Developer-preview test controls remain reachable without changing the verified Android launch path.
- Opening and dismissing a contextual panel does not trigger a privileged operation.
- Screen-state changes preserve the existing IDLE/LISTENING/THINKING/SPEAKING/OFFLINE/ERROR behavior.
- Existing shared-core tests, Android lint, preview build and APK class verification must pass before merge.
- Actual avatar visual quality, gestures, background operation and voice behavior remain pending physical acceptance.

## Safety and rollout

Implement on a feature branch and review through a PR. Do not alter main, the existing production deployment, signing secrets or known-good APK. A portrait placeholder is permitted until Charlie approves the final visual asset. This ADR does not claim implementation or physical acceptance.
