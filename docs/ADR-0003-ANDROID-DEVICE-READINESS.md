# ADR-0003 — Android device readiness and permission setup

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `feature/android-device-readiness`

## Decision

The Android developer preview gets a small platform-readiness layer before camera, voice, location, screen awareness, or background automation are connected.

The first Android readiness slice must:

1. detect the current Android device without hard-coding Samsung models;
2. distinguish phone/tablet form factor by Android configuration;
3. report camera, microphone, and location capability honestly;
4. request only the runtime permissions needed for capabilities that exist on the device;
5. request the initial permission set once, not on every app launch;
6. keep a visible retry button when a permission was denied or later revoked;
7. treat approximate location as degraded rather than pretending precise location exists;
8. avoid requesting screen-capture permission until the user actually invokes a screen-awareness feature.

## Why

LANA 3 is device-independent by architecture, but real Android permissions and hardware capabilities must be proven on physical devices. The first physical target is Charlie's current phone, with the future tablet used as another test target rather than a product boundary.

This slice creates a reusable platform probe so later camera, voice, and location adapters can consume measured capability state instead of guessing from device brand or model.

## Security and privacy

- Camera, microphone, and location remain under Android runtime permission control.
- No permission is silently escalated.
- If the user denies a permission, the application records only that the initial request was already shown; it does not nag again on every launch.
- The user can explicitly retry missing permissions from the readiness control.
- Screen capture is intentionally excluded because Android requires a separate user-approved MediaProjection flow when screen access is actually needed.
- Contacts, phone, messages, and other sensitive permissions are not requested before the corresponding feature exists.

## Acceptance criteria

- [ ] Android developer preview compiles in CI.
- [ ] Existing shared-core tests continue to pass.
- [ ] First launch requests available camera/microphone/location permissions once.
- [ ] Relaunch does not automatically repeat a denied permission request.
- [ ] Manual readiness button can retry missing permissions.
- [ ] UI shows the actual device model, Android version, form factor, RAM, and sensor permission state.
- [ ] Approximate location is shown as approximate, not precise.
- [ ] Device without a capability reports it as unavailable instead of requesting a useless permission.
- [ ] Physical-device behavior is verified on at least one phone before acceptance.

## Lifecycle status

Specification → Architecture → Implementation → **IN TEST** → Physical-device test → Charlie acceptance
